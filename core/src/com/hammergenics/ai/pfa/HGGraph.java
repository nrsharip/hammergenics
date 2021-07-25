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
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedHierarchicalGraph;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGraph extends IndexedHierarchicalGraph<HGGraphNode> {
    public final Array<HGGraphNodesGrid> grids = new Array<>(true, 16, HGGraphNodesGrid.class);
    public IndexedAStarPathFinder<HGGraphNode> indexedAstar;
    public float globalScale = 1f;
    public int lastIndex = 0;

    public HGGraph() { this(1); }
    public HGGraph(int levelCount) { super(levelCount); }

    // Interface HierarchicalGraph (IndexedHierarchicalGraph)
    @Override public int getLevelCount() { return super.getLevelCount(); }
    @Override public void setLevel(int level) { super.setLevel(level); }
    @Override public HGGraphNode convertNodeBetweenLevels(int inputLevel, HGGraphNode node, int outputLevel) { return null; }

    // Interface IndexedGraph
    @Override public int getIndex(HGGraphNode node) { return node.index; }
    @Override
    public int getNodeCount() {
        // see IndexedAStarPathFinder.<init>:
        //     this.nodeRecords = (NodeRecord<N>[])new NodeRecord[graph.getNodeCount()];
        // see IndexedAStarPathFinder.initSearch:
        //     NodeRecord<N> startRecord = getNodeRecord(startNode);
        // see IndexedAStarPathFinder.getNodeRecord
        //     int index = graph.getIndex(node);
        //     NodeRecord<N> nr = nodeRecords[index];
        
        return lastIndex;
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

        applyIndices(newGrid.graphNodes);

        grids.add(newGrid);

        // indexedAstar should be initialized each time the graph structure is changed, since
        // it has a fixed array of node records (see IndexedAStarPathFinder.NodeRecord<N>[] nodeRecords)
        // which is set only in the constructor
        indexedAstar = new IndexedAStarPathFinder<>(this);
    }

    public void applyIndices(Array<HGGraphNode> graphNodes) {
        if (graphNodes == null) { return; }

        for(HGGraphNode graphNode: graphNodes) { graphNode.index = lastIndex++; }
    }

    public boolean searchConnectionPath(Vector3 start, Vector3 end, GraphPath<Connection<HGGraphNode>> outPath) {
        if (start == null || end == null) { return false; }
        HGGraphNode startNode = null, endNode = null;
        //Gdx.app.debug("graph", "search: "
        //        + " start: " + start
        //        + " end: " + end
        //);
        for (HGGraphNodesGrid grid: grids) {
//            float startX = (start.x / globalScale) - grid.getX0();
//            float startZ = (start.z / globalScale) - grid.getZ0();
//            float endX = (end.x / globalScale) - grid.getX0();
//            float endZ = (end.z / globalScale) - grid.getZ0();

            float startX = start.x / globalScale;
            float startZ = start.z / globalScale;
            float endX = end.x / globalScale;
            float endZ = end.z / globalScale;

            boolean startXBelongsToGrid = startX - grid.getX0() >= 0 && startX - grid.getX0() < grid.getWidth(true);
            boolean startZBelongsToGrid = startZ - grid.getZ0() >= 0 && startZ - grid.getZ0() < grid.getHeight(true);
            boolean endXBelongsToGrid = endX - grid.getX0() >= 0 && endX - grid.getX0() < grid.getWidth(true);
            boolean endZBelongsToGrid = endZ - grid.getZ0() >= 0 && endZ - grid.getZ0() < grid.getHeight(true);

            //Gdx.app.debug("graph", "search: "
            //        + " globalScale: " + globalScale
            //        + " x0: " + grid.getX0()
            //        + " z0: " + grid.getZ0()
            //        + " startX: " + startX
            //        + " startZ: " + startZ
            //        + " endX: " + endX
            //        + " endZ: " + endZ
            //);

            if (startNode == null && startXBelongsToGrid && startZBelongsToGrid) {
                startNode = grid.getGraphNode(startX - grid.getX0(), startZ - grid.getZ0());
            }
            if (endNode == null && endXBelongsToGrid && endZBelongsToGrid) {
                endNode = grid.getGraphNode(endX - grid.getX0(), endZ - grid.getZ0());
            }

            if (startNode != null && endNode != null) { break; }
        }
        if (startNode == null || endNode == null) { return false; }

        return indexedAstar.searchConnectionPath(startNode, endNode, HGGraphNode.heuristic, outPath);
    }

    public boolean searchConnectionPath(HGGraphNode startNode, HGGraphNode endNode, GraphPath<Connection<HGGraphNode>> outPath) {
        return indexedAstar.searchConnectionPath(startNode, endNode, HGGraphNode.heuristic, outPath);
    }
}
