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

package com.hammergenics.screens.graphics.g3d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

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

    public HGModel(Model model) { this(model, null); }

    public HGModel(Model model, FileHandle assetFileHandle) {
        this.obj = model;
        this.afh = assetFileHandle;

        getMeshData();
        // this is done mostly for the simplicity of rigid body calculations
        // so there would be no need to take care of the offset between the
        // rigid body (with the box shape) and the Bounding Box of the model
        centerToOrigin();
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
}
