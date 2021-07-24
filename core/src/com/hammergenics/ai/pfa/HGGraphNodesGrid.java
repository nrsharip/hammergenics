/*******************************************************************************
 * Copyright 2021 Nail Sharipov (sharipovn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.hammergenics.ai.pfa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.graphics.glutils.HGImmediateModeRenderer20;
import com.hammergenics.map.HGGrid;

import java.util.Arrays;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGraphNodesGrid extends HGGrid {
    // https://libgdx.badlogicgames.com/ci/gdx-ai/docs/com/badlogic/gdx/ai/pfa/Graph.html
    // https://libgdx.badlogicgames.com/ci/gdx-ai/docs/com/badlogic/gdx/ai/pfa/Connection.html
    // https://libgdx.badlogicgames.com/ci/gdx-ai/docs/com/badlogic/gdx/ai/pfa/GraphPath.html
    // https://libgdx.badlogicgames.com/ci/gdx-ai/docs/com/badlogic/gdx/ai/pfa/DefaultGraphPath.html
    public final Array<HGGraphNode> graphNodes = new Array<>(true, 16, HGGraphNode.class);
    public int graphNodeIndexHexSize;

    public HGGraphNodesGrid(int size, int x0, int z0) {
        super(size, x0, z0);

        int width = getWidth(true);
        int height = getHeight(true);

        // Node indices consist of two parts: [chunk index] [node index within chunk]
        // graphNodeIndexHexSize - is the size of the internal node's index (node index within chunk)
        // Later when the nodes are aggregated in a graph the chunk index is applied after the graphNodeIndexHexSize
        graphNodeIndexHexSize = Integer.toHexString(width * height).length();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                graphNodes.add(new HGGraphNode(getGraphNodeIndex(x, z), new Vector3(x + 0.5f + getX0(), 0, z + 0.5f + getZ0())));
            }
        }

        connectWithin();
    }

    @Override
    public void discard() {
        super.discard();
        Arrays.stream(graphNodes.toArray())
                .map(HGGraphNode::getCoordinates)
                .forEach(coordinates -> coordinates.y = 0);
    }

    @Override
    public void roundToDigits(int digits) {
        super.roundToDigits(digits);
        recalculate();
    }

    @Override
    public void roundToStep(float step) {
        super.roundToStep(step);
        recalculate();
    }

    @Override
    public void generateNoise(float yScale, Array<NoiseStageInfo> stages) {
        super.generateNoise(yScale, stages);
        recalculate();
    }

    public void recalculate() {
        int width = getWidth(true);
        int height = getHeight(true);
        HGGraphNode node;
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                double[] values = new double[]{
                        get(x    , z    ),
                        get(x    , z + 1),
                        get(x + 1, z    ),
                        get(x + 1, z + 1)
                };
                node = getGraphNode(x, z);
                node.getCoordinates().y = (float) Arrays.stream(values).average().orElse(0f);
            }
        }
    }

    public HGGraphNode getGraphNode(int x, int z) { return graphNodes.get(getGraphNodeIndex(x, z)); }
    public int getGraphNodeIndex(int x, int z) { return z * getWidth(true) + x; }

    public void connectNode(HGGraphNode node, Array<HGGraphNode> others) {
        if (node == null || others == null || others.size == 0) { return; }
        Array<HGGraphNodeConnection> connections = new Array<>(true, 16, HGGraphNodeConnection.class);
        for (HGGraphNode other: others) {
            connections.add(new HGGraphNodeConnection(node, other));
            //Gdx.app.debug("grid", " node: " + node.coordinates + " other: " + other.coordinates);
        }
        node.addConnections(connections);

    }

    public void connectWithin() {
        HGGraphNode node;
        Array<HGGraphNode> tmpArray = new Array<>(HGGraphNode.class);

        int width = getWidth(true);
        int height = getHeight(true);
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                //Gdx.app.debug("grid", ""
                //        + " x: " + x + " z: " + z
                //        + " size: " + graphNodes.size);
                //Gdx.app.debug("grid", " index: " + getGraphNodeIndex(x, z));
                node = getGraphNode(x, z);

                tmpArray.clear();
                if (z > 0) {
                    if (x > 0) { tmpArray.add(getGraphNode(x - 1, z - 1)); }         // row 1 col 1
                    tmpArray.add(getGraphNode(x, z - 1));                            // row 1 col 2
                    if (x < width - 1) { tmpArray.add(getGraphNode(x + 1, z - 1)); } // row 1 col 3
                }
                if (x > 0) { tmpArray.add(getGraphNode(x - 1, z)); }                 // row 2 col 1
                if (x < width - 1) { tmpArray.add(getGraphNode(x + 1, z)); }         // row 2 col 3
                if (z < height - 1) {
                    if (x > 0) { tmpArray.add(getGraphNode(x - 1, z + 1)); }         // row 3 col 1
                    tmpArray.add(getGraphNode(x    , z + 1));                        // row 3 col 2
                    if (x < width - 1) { tmpArray.add(getGraphNode(x + 1, z + 1)); } // row 3 col 3
                }
                connectNode(node, tmpArray);
            }
        }
    }

    public void connectBottom(HGGraphNodesGrid other) {
        if (x0 != other.x0 || (z0 + getHeight(true) != other.z0)) { return; }

        //Gdx.app.debug("grid", " connect bottom: "
        //        + " x0: " + x0 + " z0: " + z0
        //        + " other.x0: " + other.x0 + " other.z0: " + other.z0);

        HGGraphNode node;
        Array<HGGraphNode> tmpArray = new Array<>(HGGraphNode.class);

        int width = Math.min(getWidth(true), other.getWidth(true));
        int height = getHeight(true);
        for (int x = 0; x < width; x++) {
            node = getGraphNode(x, height - 1);

            tmpArray.clear();
            if (x > 0) { tmpArray.add(other.getGraphNode(x - 1, 0)); }         // other's top row col 1
            tmpArray.add(other.getGraphNode(x, 0));                            // other's top row col 2
            if (x < width - 1) { tmpArray.add(other.getGraphNode(x + 1, 0)); } // other's top row col 3

            connectNode(node, tmpArray);

            node = other.getGraphNode(x, 0);
            tmpArray.clear();
            if (x > 0) { tmpArray.add(getGraphNode(x - 1, height - 1)); }         // this one's bottom row col 1
            tmpArray.add(getGraphNode(x, height - 1));                            // this one's bottom row col 2
            if (x < width - 1) { tmpArray.add(getGraphNode(x + 1, height - 1)); } // this one's bottom row col 3

            connectNode(node, tmpArray);
        }
    }

    public void connectRight(HGGraphNodesGrid other) {
        if (z0 != other.z0 || (x0 + getWidth(true) != other.x0)) { return; }

        //Gdx.app.debug("grid", " connect right: "
        //        + " x0: " + x0 + " z0: " + z0
        //        + " other.x0: " + other.x0 + " other.z0: " + other.z0);

        HGGraphNode node;
        Array<HGGraphNode> tmpArray = new Array<>(HGGraphNode.class);

        int width = getWidth(true);
        int height = Math.min(getHeight(true), other.getHeight(true));
        for (int z = 0; z < height; z++) {
            node = getGraphNode(width - 1, z);

            tmpArray.clear();
            if (z > 0) { tmpArray.add(other.getGraphNode(0, z - 1)); }         // other's left col row 1
            tmpArray.add(other.getGraphNode(0, z));                            // other's left col row 2
            if (z < height - 1) { tmpArray.add(other.getGraphNode(0, z + 1)); } // other's left col row 3

            connectNode(node, tmpArray);

            node = other.getGraphNode(0, z);
            tmpArray.clear();
            if (z > 0) { tmpArray.add(getGraphNode(width - 1, z - 1)); }         // this one's right col row 1
            tmpArray.add(getGraphNode(width - 1, z));                            // this one's right col row 2
            if (z < height - 1) { tmpArray.add(getGraphNode(width - 1, z + 1)); } // this one's right col row 3

            connectNode(node, tmpArray);
        }
    }


    public void addGraphNodesToRenderer(HGImmediateModeRenderer20 imr, Color clr, float scl) {
        for (HGGraphNode graphNode: graphNodes) {
            imr.point(graphNode.coordinates, clr, scl);
        }
    }
    public void addGraphNodesConnectionsToRenderer(HGImmediateModeRenderer20 imr, Color clr1, Color clr2) {
        for (HGGraphNode graphNode: graphNodes) {
            for (HGGraphNodeConnection connection: graphNode.connections) {
                imr.line(connection.getFromNode().coordinates, connection.getToNode().coordinates, clr1, clr2);
            }
        }
    }
}
