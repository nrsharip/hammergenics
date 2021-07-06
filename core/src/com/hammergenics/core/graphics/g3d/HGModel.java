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

package com.hammergenics.core.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGModel implements Disposable {
    /**
     * model object
     */
    public Model obj;
    /**
     * asset file handle
     */
    public FileHandle afh;

    public ArrayMap<Mesh, MeshData> mesh2data = new ArrayMap<>(Mesh.class, MeshData.class);
    private Array<Vector3> vertices = new Array<>(Vector3.class);

    public HGModel(Model model) { this(model, null); }

    public HGModel(Model model, FileHandle assetFileHandle) {
        this.obj = model;
        this.afh = assetFileHandle;

        getMeshData();
        // this is done mostly for the simplicity of rigid body calculations
        // so there would be no need to take care of the offset between the
        // rigid body (with the box shape) and the Bounding Box of the model.
        // For now only models loaded from files are being centered to origin by default.
        if (assetFileHandle != null) { centerToOrigin(); }
    }

    @Override
    public void dispose() { obj.dispose(); }

    public boolean hasAnimations() { return obj.animations.size != 0; }
    public boolean hasMaterials() { return obj.materials.size != 0; }
    public boolean hasMeshes() { return obj.meshes.size != 0; }
    public boolean hasMeshParts() { return obj.meshParts.size != 0; }
    public boolean hasNodes() { return obj.nodes.size != 0; }

    public void getMeshData() {
        for (Mesh mesh: obj.meshes) { mesh2data.put(mesh, new MeshData(mesh)); }
    }

    public void centerToOrigin() {
        HGModelInstance mi = new HGModelInstance(this);
        BoundingBox bb = mi.calculateBoundingBox(new BoundingBox());
        Vector3 center = bb.getCenter(new Vector3());

        if (!center.equals(Vector3.Zero)) {
            for (Node node: obj.nodes) {
                //Gdx.app.debug("node to center: ",  "b node.id: " + node.id + " translation: " + node.translation);
                node.translation.add(Vector3.Zero.cpy().sub(center));
                node.calculateTransforms(true);
                node.calculateBoneTransforms(true);
                //Gdx.app.debug("node to center: ",  "a node.id: " + node.id + " translation: " + node.translation);

                // only root nodes matter for world translation
                // so not making it recursive on children
            }
        }
    }

    public static class MeshData {
        VertexAttributes vertexAttributes;
        public short[] indices;
        public float[] vertices;

        public MeshData(Mesh mesh) {
            vertexAttributes = mesh.getVertexAttributes();
            int vs = vertexAttributes.vertexSize / 4;
            short[] indices = new short[mesh.getNumIndices()];
            float[] vertices = new float[vs * mesh.getNumVertices()];
            mesh.getIndices(indices);
            mesh.getVertices(vertices);

            this.indices = indices;
            this.vertices = vertices;
        }
    }

    public Vector3 closestVertex(Vector3 point, Vector3 out) {
        Array<Vector3> vertices = getVertices();
        if (vertices.size == 0) { return null; }
        float min = Float.MAX_VALUE;
        float dst2;
        for (Vector3 vertex: vertices) {
            dst2 = point.dst2(vertex);
            if (min > dst2) { out.set(vertex); min = dst2; }
        }
        //Gdx.app.debug("closest", "dist: " + min);
        return out;
    }

    public Array<Vector3> getVertices() {
        if (vertices.size == 0) { verticesPos(vertices); }
        return vertices;
    }

    public Array<Vector3> verticesPos(Array<Vector3> out) {
        // only for models with no more than one root node for now
        if (obj.nodes.size != 1) { return out; }

        Matrix4 globalTransform = obj.nodes.get(0).globalTransform;

        for (Mesh mesh: obj.meshes) {
            HGModel.MeshData meshData = mesh2data.get(mesh);

            if (meshData == null) { continue; }

            VertexAttributes vertexAttributes = meshData.vertexAttributes;
            // IMPORTANT: vertex size is in bytes, float is 4 bytes long
            int vs = vertexAttributes.vertexSize / 4;

            float[] vertices = meshData.vertices;

            // Attribute: Position
            VertexAttribute vaPosition = vertexAttributes.findByUsage(Position);
            if (vaPosition == null) { return out; }
            int po = vaPosition.offset / 4; // NOTE: the offset is in bytes as well, see VertexAttribute.offset
            int pn = vaPosition.numComponents;

            // expecting 3 components (x, y, z) for now...
            if (pn != 3) {
                Gdx.app.error(getClass().getSimpleName(), "world vertices: WRONG number of components " + pn);
                return out;
            }

            for (int i = 0; i < vertices.length/vs; i++) {
                out.add(new Vector3(vertices[i*vs + po], vertices[i*vs + po + 1], vertices[i*vs + po + 2])
                        .mul(globalTransform));
            }
        }

        return out;
    }
}
