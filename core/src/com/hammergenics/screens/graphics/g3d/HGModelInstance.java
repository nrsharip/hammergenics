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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.screens.graphics.glutils.HGImmediateModeRenderer20;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.BoneWeight;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createBoundingBoxModel;
import static com.hammergenics.utils.LibgdxUtils.aux_colors;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGModelInstance extends ModelInstance implements Disposable {
    public HGModel hgModel;
    /**
     * asset file handle
     */
    public FileHandle afh;
    /**
     * root node ids
     */
    public String[] rni;

    private final BoundingBox bb = new BoundingBox();
    public final float maxD;
    public AnimationController animationController = null;
    public AnimationController.AnimationDesc animationDesc = null;
    public int animationIndex = 0;

    // TODO: keep this separate for now - move to another class?
    public HGModel bbHgModel = null;
    public HGModelInstance bbHgMI = null;
    public Array<HGModelInstance> bbCornerMIs = null;
    // TODO: keep this separate for now - move to another class?
    public int auxMeshCounter;

    public HGModelInstance (final Model model) { this(new HGModel(model), null, (String[])null); }

    public HGModelInstance (final Model model, final String... rootNodeIds) { this(new HGModel(model), null, rootNodeIds); }

    public HGModelInstance (final HGModel hgModel) { this(hgModel, null, (String[])null); }

    public HGModelInstance (final HGModel hgModel, final String... rootNodeIds) { this(hgModel, null, rootNodeIds); }

    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL) {
        this(hgModel, assetFL, (String[])null);
    }

    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL, final String... rootNodeIds) {
        super(hgModel.obj, rootNodeIds);

        this.hgModel = hgModel;
        this.afh = assetFL;
        this.rni = rootNodeIds;

        calculateBoundingBox(bb);
        Vector3 dims = bb.getDimensions(new Vector3());
        maxD = Math.max(Math.max(dims.x, dims.y), dims.z);

        createBBModel();
    }

    @Override
    public void dispose() {
        // hgModel is being disposed by the AssetManager
        bbHgModel.dispose();
        if (bbHgMI != null) { bbHgMI.dispose(); }
        if (bbCornerMIs != null) { bbCornerMIs.forEach(HGModelInstance::dispose); }
    }

    public String getTag(int depth) {
        return Thread.currentThread().getStackTrace()[depth].getMethodName() + ":" + afh.name() + "@" + hashCode();
    }

    public void debug() {
        Gdx.app.debug(getTag(3), "bb.getDimensions = " + bb.getDimensions(new Vector3()));
        Gdx.app.debug(getTag(3), "bb.getCenter = " + bb.getCenter(new Vector3()));
        Gdx.app.debug(getTag(3), "bb.getMin = " + bb.getMin(new Vector3())); // the bb corner nearest to (0,0,0) = bb.getCorner000()
        Gdx.app.debug(getTag(3), "bb.getMax = " + bb.getMax(new Vector3())); // the bb corner farthest from (0,0,0) = bb.getCorner111()
        Gdx.app.debug(getTag(3), "transform.getTranslation = " + transform.getTranslation(new Vector3()));
        Gdx.app.debug(getTag(3), "transform.getScale = " + transform.getScale(new Vector3()));
        Gdx.app.debug(getTag(3), "transform.getRotation (nor false) = " + transform.getRotation(new Quaternion(0, 0, 0, 0)));
        Gdx.app.debug(getTag(3), "transform.getRotation (nor  true) = " + transform.getRotation(new Quaternion(0, 0, 0, 0), true));
        Gdx.app.debug(getTag(3), "transform = \n" + transform);
    }

    // NOTE:
    // Make a clear distinction between Matrix4: setToScaling(), scl() and scale().
    //     setToScaling() - overrides M00 M11 M22 with an (x,y,z) scaling factor /* sets the matrix to an identity matrix first */
    //              scl() - multiplies M00 M11 M22 by the (x,y,z) scaling factor
    //            scale() - multiplies 3x3 submatrix by the (x,y,z) scaling factor (M00*x M01*y M02*z M10*x M11*y M12*z...)
    //
    // Make a clear distinction between Matrix4: setTranslation(), trn() and translate().
    //   setTranslation() - overrides M03 M13 M23 with an (x,y,z) translation /* sets the matrix to an identity matrix first */
    //              trn() - adds the (x,y,z) translation to M03 M13 M23
    //        translate() - adds to M03 M13 M23 M33 (see implementation)

    public void moveBy(Vector3 vector) { transform.trn(vector); }
    public void moveBy(float x, float y, float z) { transform.trn(x, y, z); }
    public void moveTo(Vector3 vector) { transform.setToTranslation(vector); }
    public void moveTo(float x, float y, float z) { transform.setToTranslation(x, y, z); }
    public void moveAndScaleTo(Vector3 vector, Vector3 factor) { transform.setToTranslationAndScaling(vector, factor); }
    public void scaleBy(float factor) { transform.scl(factor, factor, factor); }
    public void scaleBy(Vector3 factor) { transform.scl(factor); }
    public void scaleTo(float factor) { transform.setToScaling(factor, factor, factor); }
    public void scaleTo(Vector3 factor) { transform.setToScaling(factor); }

    public float getMaxScale() { Vector3 s = transform.getScale(new Vector3()); return Math.max(Math.max(s.x, s.y), s.z); }
    // see Matrix4 set(...) for the entire map of translation rotation and scale
    public BoundingBox getBB() { return new BoundingBox(bb).mul(transform); }

    public void setAttributes(final Attribute... attributes) {
        for (int i = 0; i < materials.size; i++) { materials.get(i).set(attributes); }
    }

    public void setAttributes(int mtlIndex, final Attribute... attributes) {
        if (materials.size == 0 || mtlIndex < 0 || mtlIndex > materials.size - 1) { return; }
        materials.get(mtlIndex).set(attributes);
    }

    public void setAttributes(String mtlId, final Attribute... attributes) {
        Material material = getMaterial(mtlId);
        if (material != null) { getMaterial(mtlId).set(attributes); }
    }

    // TODO: keep this separate for now - move to another class?
    private void createBBModel() {
        if (bbHgModel != null) { bbHgModel.dispose(); }

        bbHgModel = new HGModel(createBoundingBoxModel());
    }

    // TODO: keep this separate for now - move to another class?
    public HGModelInstance getBBHgModelInstance(Color boxColor) {
        if (bbHgModel == null) { return null; }
        if (bbHgMI != null) { bbHgMI.dispose(); bbHgMI = null; }

        final BoundingBox tmp = getBB();

        bbHgMI = new HGModelInstance(bbHgModel, "box");

        bbHgMI.getMaterial("box").set(ColorAttribute.createDiffuse(boxColor), new BlendingAttribute(0.1f));
        bbHgMI.getNode("box").globalTransform.setToTranslationAndScaling(tmp.getCenter(new Vector3()), tmp.getDimensions(new Vector3()));

        return bbHgMI;
    }

    public Array<HGModelInstance> getCornerHgModelInstances(Color cornerColor) {
        if (bbHgModel == null) { return null; }
        if (bbCornerMIs != null && bbCornerMIs.size > 0) {
            bbCornerMIs.forEach(HGModelInstance::dispose);
            bbCornerMIs.clear();
            bbCornerMIs = null;
        }

        bbCornerMIs = new Array<>(true, 16, HGModelInstance.class);

        final BoundingBox tmp = getBB();
        final Vector3 bbMin = tmp.getMin(new Vector3());
        final Vector3 bbMax = tmp.getMax(new Vector3());

        for (int i = 0; i < 8; i++) { // BB corners
            String id = String.format("corner%3s", Integer.toBinaryString(i)).replace(' ', '0');

            HGModelInstance bbCornerHgMI = new HGModelInstance(bbHgModel, id);

            bbCornerHgMI.getMaterial(id).set(ColorAttribute.createDiffuse(cornerColor), new BlendingAttribute(0.3f));

            Vector3 translate = Vector3.Zero.cpy();
            // 000 - translate(min.x, min.y, min.z)
            // 001 - translate(min.x, min.y, max.z)
            // 010 - translate(min.x, max.y, min.z)
            // ...
            if ((i & (1 << 2)) == 0) { translate.x = bbMin.x; } else { translate.x = bbMax.x; }
            if ((i & (1 << 1)) == 0) { translate.y = bbMin.y; } else { translate.y = bbMax.y; }
            if ((i & (1 << 0)) == 0) { translate.z = bbMin.z; } else { translate.z = bbMax.z; }

            Vector3 scale = Vector3.Zero.cpy();
            scale.add(maxD * getMaxScale()).scl(1f/30f);

            bbCornerHgMI.transform.setToTranslationAndScaling(translate, scale);

            bbCornerMIs.add(bbCornerHgMI);
        }
        return bbCornerMIs;
    }
    // TODO: keep this separate for now - move to another class?
    public void addNodesToRenderer(HGImmediateModeRenderer20 imr) {
        for (Node node:nodes) {
            //if (node.id.equals("characterMedium"))
            addNodeToRenderer(imr, node, Color.RED, Color.GREEN);
        }
    }
    // TODO: keep this separate for now - move to another class?
    public void addNodeToRenderer(HGImmediateModeRenderer20 imr, Node node, Color c1, Color c2) {
        addNodeToRenderer(imr, node, c1, c2, -1);
    }

    // TODO: keep this separate for now - move to another class?
    public void addNodeToRenderer(HGImmediateModeRenderer20 imr, Node node, Color c1, Color c2, int depth) {
        Matrix4 tmpM4 = node.globalTransform.cpy().mulLeft(transform.cpy());
        //Gdx.app.debug(getClass().getSimpleName(), "node: " + node.id + "\n" + tmpM4);
        imr.box(tmpM4, 1/10f, Color.CYAN);

        if (--depth == 0) { return; }

        Iterable<Node> children = node.getChildren();
        if (children != null && children.iterator().hasNext()) {
            for (Node child:children) {
                Vector3 p1 = node.globalTransform.cpy().mulLeft(transform).getTranslation(new Vector3());
                Vector3 p2 = child.globalTransform.cpy().mulLeft(transform).getTranslation(new Vector3());
                imr.line(p1, p2, c1, c2);
                addNodeToRenderer(imr, child, Color.PURPLE, Color.GREEN, depth);
            }
        }
    }

    // TODO: keep this separate for now - move to another class?
    public void addBonesToRenderer(HGImmediateModeRenderer20 imr) {
        for (Node node:nodes) {
            //if (node.id.equals("characterMedium"))
            addBonesToRenderer(imr, node, Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE);
        }
    }

    // TODO: keep this separate for now - move to another class?
    public void addBonesToRenderer(HGImmediateModeRenderer20 imr, Node node,
                                   Color c1, Color c2, Color c3, Color c4) {
        for (NodePart nodePart:node.parts) {
            if (nodePart.bones == null) { continue; }
            if (nodePart.invBoneBindTransforms != null && nodePart.bones.length != nodePart.invBoneBindTransforms.size) { continue; }
            for (int i = 0; i < nodePart.bones.length; i++) {
                // IMPORTANT:
                // see Node.calculateBoneTransforms (boolean recursive)
                //     part.bones[i].set(part.invBoneBindTransforms.keys[i].globalTransform).mul(part.invBoneBindTransforms.values[i]);
                //
                // see Model.loadNodes:
                //     e.key.invBoneBindTransforms.put(getNode(b.key), new Matrix4(b.value).inv());
                //
                // ? basically this means that a bone is the Node's globalTransform multiplied by the inverse of the actual bone
                Matrix4 tmpM4 = nodePart.bones[i].cpy();
                tmpM4.mul(nodePart.invBoneBindTransforms.values[i].cpy().inv()); // undoing the inverse
                // now the "bone" actually become a Node's "globalTransform"
                tmpM4.mulLeft(transform); // applying the ModelInstance's transform

                imr.box(tmpM4, 1/10f, c1);

                Vector3 tmpV1 = tmpM4.getTranslation(new Vector3());
                Vector3 tmpV2 = node.globalTransform.cpy().mulLeft(transform).getTranslation(new Vector3());
                imr.line(tmpV1, tmpV2, c1, c2);

                if (nodePart.meshPart != null) {
                    // is multiplication by node.globalTransform here really needed?
                    tmpV2 = nodePart.meshPart.center.cpy().mul(node.globalTransform).mul(transform);
                    imr.line(tmpV1, tmpV2, c3, c4);
                }
            }
        }

        Iterable<Node> children = node.getChildren();
        if (children != null && children.iterator().hasNext()) {
            for (Node child:children) {
                addBonesToRenderer(imr, child, c1, c2, c3, c4);
            }
        }
    }

    // TODO: keep this separate for now - move to another class?
    public void addMeshPartsToRenderer(HGImmediateModeRenderer20 imr) {
        auxMeshCounter = 0;
        for (Node node:nodes) { addMeshPartsToRenderer(imr, node); }
    }

    // TODO: keep this separate for now - move to another class?
    public void addMeshPartsToRenderer(HGImmediateModeRenderer20 imr, Node node) {
        for (NodePart nodePart:node.parts) {
            addMeshPartToRenderer(imr, node, nodePart, nodePart.meshPart);
        }

        Iterable<Node> children = node.getChildren();
        if (children != null && children.iterator().hasNext()) {
            for (Node child:children) {
                addMeshPartsToRenderer(imr, child);
            }
        }
    }

    // TODO: keep this separate for now - move to another class?
    public void addMeshPartToRenderer(HGImmediateModeRenderer20 imr, Node node, NodePart nodePart, MeshPart mp) {
        if (mp == null || mp.mesh == null) { return; }

        // TODO: this should be the part of HGModel
        VertexAttributes vertexAttributes = mp.mesh.getVertexAttributes();
        // IMPORTANT: vertex size is in bytes, float is 4 bytes long
        // TODO: there's also a notion of 'OpenGL type': GL_FLOAT or GL_UNSIGNED_BYTE stored in VertexAttribute.type
        int vs = vertexAttributes.vertexSize / 4;

        short[] indices = new short[mp.mesh.getNumIndices()];
        float[] vertices = new float[vs * mp.mesh.getNumVertices()];
        mp.mesh.getIndices(indices);
        mp.mesh.getVertices(vertices);

        // 0000 0000 0001 Position = 1;
        // 0000 0000 0010 ColorUnpacked = 2;
        // 0000 0000 0100 ColorPacked = 4;
        // 0000 0000 1000 Normal = 8;
        // 0000 0001 0000 TextureCoordinates = 16;
        // 0000 0010 0000 Generic = 32;
        // 0000 0100 0000 BoneWeight = 64;
        // 0000 1000 0000 Tangent = 128;
        // 0001 0000 0000 BiNormal = 256;

//        Gdx.app.debug(getClass().getSimpleName(), "vertex attributes mask: 0b"
//                + Long.toBinaryString(vertexAttributes.getMask()));
        // 0000 0101 1011 - (Position, ColorUnpacked, Normal, TextureCoordinates, BoneWeight)
        // Attribute: Position
        VertexAttribute vaPosition = vertexAttributes.findByUsage(Position);
        if (vaPosition == null) { return; }
        int po = vaPosition.offset / 4; // NOTE: the offset is in bytes as well, see VertexAttribute.offset
        int pn = vaPosition.numComponents;
        // The components number for position in MeshBuilder is 3
        // see MeshBuilder.createAttributes (long usage):
        //		if ((usage & Usage.Position) == Usage.Position)
        //			attrs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        // short MeshBuilder.vertex (Vector3 pos, Vector3 nor, Color col, Vector2 uv)
        // the index returned is short meaning there's no more than 32767 indices available

        // expecting 3 components (x, y, z) for now...
        if (pn != 3) {
            Gdx.app.error(getClass().getSimpleName(), "add mesh part: WRONG number of components " + pn);
            return;
        }

        // Attribute: BoneWeight
        // IMPORTANT:
        // see NodePart.setRenderable (final Renderable out)
        // see Renderable.bones description (source code one, not the javadoc)
        VertexAttribute vaBoneWeight = vertexAttributes.findByUsage(BoneWeight);
        int bwo = -1; int bwn = -1;
        if (vaBoneWeight != null) { bwo = vaBoneWeight.offset / 4; bwn = vaBoneWeight.numComponents; }

//        Gdx.app.debug(getClass().getSimpleName(),
//                " po: " + po + " pn: " + pn + " bwo: " + bwo + " bwn: " + bwn);

        Vector3 tmp1 = Vector3.Zero.cpy();
        Vector3 tmp2 = Vector3.Zero.cpy();
        Vector3 tmp3 = Vector3.Zero.cpy();

        Color color = aux_colors.get(auxMeshCounter++ % aux_colors.size);
        switch (mp.primitiveType) {
            case GL_LINES:
                //...
                break;
            case GL_TRIANGLES:
                //Gdx.app.debug(getClass().getSimpleName(),
                //        "mesh part: " + mp.id + " offset: " + mp.offset + " size: " + mp.size);
                for (int i = mp.offset; i < mp.offset + mp.size; i += 3) { // 3 corners of a triangle
                    tmp1.set(vertices[vs*indices[i+0]+po], vertices[vs*indices[i+0]+po+1], vertices[vs*indices[i+0]+po+2]);
                    tmp2.set(vertices[vs*indices[i+1]+po], vertices[vs*indices[i+1]+po+1], vertices[vs*indices[i+1]+po+2]);
                    tmp3.set(vertices[vs*indices[i+2]+po], vertices[vs*indices[i+2]+po+1], vertices[vs*indices[i+2]+po+2]);
                    if (bwo > 0) {
                        // ignoring bwn for now...
                        tmp1.mul(transform.cpy().mul(nodePart.bones[(short)vertices[vs*indices[i+0]+bwo]]));
                        tmp2.mul(transform.cpy().mul(nodePart.bones[(short)vertices[vs*indices[i+1]+bwo]]));
                        tmp3.mul(transform.cpy().mul(nodePart.bones[(short)vertices[vs*indices[i+2]+bwo]]));
                        // TODO: using tmpM variables gives the matrix full of NaN's for some unknown reasons...
                        // e.g.
                        // Matrix4 tmpM1 = transform.cpy();
                        // tmpM4.mul(nodePart.bones...) would be equal to:
                        //[NaN|NaN|NaN|NaN]
                        //[NaN|NaN|NaN|NaN]
                        //[NaN|NaN|NaN|NaN]
                        //[0.0|0.0|0.0|1.0]
                    } else {
                        tmp1.mul(transform.cpy());
                        tmp2.mul(transform.cpy());
                        tmp3.mul(transform.cpy());
                    }
                    //tmp1.mul(tmpM1); tmp2.mul(tmpM2); tmp3.mul(tmpM3);

                    // Gdx.app.debug(getClass().getSimpleName(), "1: " + tmp1 + " 2: " + tmp2 + " 3: " + tmp3);
                    // see https://www.khronos.org/opengl/wiki/Vertex_Specification
                    // see https://www.khronos.org/opengl/wiki/Vertex_Rendering
                    // see https://www.khronos.org/opengl/wiki/Primitive
                    // GL_TRIANGLES, VertexAttributes.Usage.Position:
                    // indices:  [0, 1, 2, 2, 3, 0, 5, 4, 7, 7, 6, 5, 0, 3, 7, 7, 4, 0, 5, 6, 2, 2, 1, 5, 5, 1, 0, 0, 4, 5, 2, 6, 7, 7, 3, 2] : 36 indices, 12 triangles
                    // vertices: [-0.5, -0.5, -0.5, 0.5, -0.5, -0.5, 0.5, 0.5, -0.5, -0.5, 0.5, -0.5, -0.5, -0.5, 0.5, 0.5, -0.5, 0.5, 0.5, 0.5, 0.5, -0.5, 0.5, 0.5]

                    imr.triangle(tmp1, tmp2, tmp3, color, color, color);
                }
                break;
            default:
                Gdx.app.error(getClass().getSimpleName(), "add mesh part: UNSUPPORTED primitive type " + mp.primitiveType);
                break;
        }
    }
}
