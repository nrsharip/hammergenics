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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGraphNode {

    public int index = -1;
    public Vector3 coordinates = new Vector3();
    public Array<HGGraphNodeConnection> connections = new Array<>(true, 16, HGGraphNodeConnection.class);

    public HGGraphNode() { this(-1, null, null); }
    public HGGraphNode(int index) { this(index, null, null); }
    public HGGraphNode(Vector3 coordinates) { this(-1, coordinates, null); }
    public HGGraphNode(int index, Vector3 coordinates) { this(index, coordinates, null); }
    public HGGraphNode(int index, Vector3 coordinates, Array<HGGraphNodeConnection> connections) {
        setIndex(index);
        setCoordinates(coordinates);
        setConnections(connections);
    }

    public HGGraphNode setIndex(int index) {
        this.index = index;
        return this;
    }

    public Vector3 getCoordinates() { return coordinates; }
    public HGGraphNode setCoordinates(Vector3 coordinates) {
        if (coordinates != null) { this.coordinates.set(coordinates); }
        return this;
    }

    public Array<HGGraphNodeConnection> getConnections() { return connections; }
    public HGGraphNode setConnections(Array<HGGraphNodeConnection> connections) {
        this.connections.clear();
        return addConnections(connections);
    }
    public HGGraphNode addConnections(Array<HGGraphNodeConnection> connections) {
        if (connections != null) { this.connections.addAll(connections); }
        return this;
    }
}
