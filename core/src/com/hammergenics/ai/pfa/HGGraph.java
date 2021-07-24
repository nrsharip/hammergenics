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

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedHierarchicalGraph;
import com.badlogic.gdx.utils.Array;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGraph extends IndexedHierarchicalGraph<HGGraphNode> {
    public final Array<HGGraphNodesGrid> grids = new Array<>(true, 16, HGGraphNodesGrid.class);

    public HGGraph() { this(1); }
    public HGGraph(int levelCount) {
        super(levelCount);
    }

    // Interface HierarchicalGraph (IndexedHierarchicalGraph)
    @Override public int getLevelCount() { return super.getLevelCount(); }
    @Override public void setLevel(int level) { super.setLevel(level); }
    @Override public HGGraphNode convertNodeBetweenLevels(int inputLevel, HGGraphNode node, int outputLevel) { return null; }

    // Interface IndexedGraph
    @Override public int getIndex(HGGraphNode node) { return node.index; }
    @Override
    public int getNodeCount() {
        int result = 0;
        for (HGGraphNodesGrid grid: grids) { result += grid.graphNodes.size; }
        return result;
    }

    // Interface Graph
    @Override
    public Array<Connection<HGGraphNode>> getConnections(HGGraphNode fromNode) { return fromNode.connections; }

    public void addGraphNodesGrid(HGGraphNodesGrid newGrid) {
        if (newGrid == null) { return; }

        for (HGGraphNodesGrid grid: grids) {
            if (newGrid.x0 == grid.x0 && newGrid.z0 == grid.z0) {
                // grids added cannot have the same origin
                return;
            }
        }
        //Gdx.app.debug("graph", " add: " + " newGrid.x0: " + newGrid.x0 + " newGrid.z0: " + newGrid.z0);
        for (HGGraphNodesGrid grid: grids) {
            //Gdx.app.debug("graph", " connect: " + " grid.x0: " + grid.x0 + " grid.z0: " + grid.z0);

            // simply go through all 4 sides and if the grids are adjacent - they get connected
            grid.connectBottom(newGrid);
            grid.connectRight(newGrid);
            newGrid.connectBottom(grid);
            newGrid.connectRight(grid);
        }

        applyIndex(grids.size, newGrid.graphNodeIndexHexSize, newGrid.graphNodes);

        grids.add(newGrid);
    }

    public void applyIndex(int index, int hexOffset, Array<HGGraphNode> graphNodes) {
        if (graphNodes == null) { return; }

        for(HGGraphNode graphNode: graphNodes) {
            graphNode.index |= (index << hexOffset * 4);
        }
    }
}
