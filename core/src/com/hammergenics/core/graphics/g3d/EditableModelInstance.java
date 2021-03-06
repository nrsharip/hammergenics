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
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.model.AnimationInfo;
import com.hammergenics.core.graphics.glutils.HGImmediateModeRenderer20;
import com.hammergenics.core.stages.ui.attributes.AttributesManagerTable;
import com.hammergenics.core.stages.ui.attributes.BaseAttributeTable;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.BoneWeight;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;
import static com.hammergenics.core.graphics.g3d.utils.Models.createBoundingBoxModel;
import static com.hammergenics.HGUtils.aux_colors;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class EditableModelInstance extends SteerableModelInstance implements Disposable, Steerable<Vector3> {
    // Nodes related
    public final ArrayMap<Node, Array<NodePart>> n2np = new ArrayMap<>(Node.class, Array.class);
    public final ArrayMap<Node, BoundingBox> n2bb = new ArrayMap<>(Node.class, BoundingBox.class);
    public final ArrayMap<BoundingBox, Node> bb2n = new ArrayMap<>(BoundingBox.class, Node.class);
    // This is for sub-models created out of this model if it has more than one node part
    public final ArrayMap<Node, HGModel> node2model = new ArrayMap<>(Node.class, HGModel.class);
    public final ArrayMap<String, HGModel> nodeid2model = new ArrayMap<>(String.class, HGModel.class);
    public final ArrayMap<HGModel, Node> model2node = new ArrayMap<>(HGModel.class, Node.class);
    // Materials related
    public Array<String> mtlIds = new Array<>(true, 16, String.class);
    public final ArrayMap<Material, AttributesManagerTable> mtl2atable = new ArrayMap<>(Material.class, AttributesManagerTable.class);
    public final ArrayMap<String, AttributesManagerTable> mtlid2atable = new ArrayMap<>(String.class, AttributesManagerTable.class);
    // Animations related
    public float currKeyTime = 0f;
    public boolean animLoop = true;
    public Animation selectedAnimation = null;
    public final ArrayMap<Animation, AnimationInfo> anim2info = new ArrayMap<>(Animation.class, AnimationInfo.class);

    public HGModel bbHgModel = null;
    public HGModelInstance bbHgMI = null;
    public Array<HGModelInstance> bbCornerMIs = null;
    public int auxMeshCounter;
    public Node hoveredOverNode = null;

    // see: https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/#using-motion-states
    public static class EditableBtMotionState extends HGbtMotionState {
        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            super.setWorldTransform(worldTrans);
            ((EditableModelInstance)mi).bbHgModelInstanceReset();
            ((EditableModelInstance)mi).bbCornersReset();
        }
        public EditableBtMotionState(EditableModelInstance mi) { super(mi); }
    }

    public EditableModelInstance(Model model, float mass, ShapesEnum shape) { this(new HGModel(model), null, mass, shape, (String[])null); }
    public EditableModelInstance(Model model, float mass, ShapesEnum shape, String... rootNodeIds) { this(new HGModel(model), null, mass, shape, rootNodeIds); }
    public EditableModelInstance(HGModel hgModel, float mass, ShapesEnum shape) { this(hgModel, null, mass, shape, (String[])null); }
    public EditableModelInstance(HGModel hgModel, float mass, ShapesEnum shape, String... rootNodeIds) { this(hgModel, null, mass, shape, rootNodeIds); }
    public EditableModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape) { this(hgModel, assetFL, mass, shape, (String[])null); }
    public EditableModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape, String... rootNodeIds) {
        super(hgModel, assetFL, mass, shape, rootNodeIds);
        createBBModel();

        checkNodeParts();
        createNodePartModels();
        checkMaterials();
        checkAnimations();

        setRbMotionState(new EditableBtMotionState(this));
    }

    @Override
    public void dispose() {
        // hgModel is being disposed by the AssetManager
        bbHgModel.dispose();
        if (bbHgMI != null) { bbHgMI.dispose(); }
        if (bbCornerMIs != null) { bbCornerMIs.forEach(HGModelInstance::dispose); }
        // TODO: clear things up with model disposal
        //node2model.values().forEach(HGModel::dispose);
        super.dispose();
    }

    @Override public void trn(Vector3 vector) { super.trn(vector); syncAuxWithTransform(); }
    @Override public void trn(float x, float y, float z) { super.trn(x, y, z); syncAuxWithTransform(); }
    @Override public void translate(Vector3 translation) { super.translate(translation); syncAuxWithTransform(); }
    @Override public void setTranslation(Vector3 vector) { super.setTranslation(vector); syncAuxWithTransform(); }
    @Override public void setTranslation(float x, float y, float z) { super.setTranslation(x, y, z); syncAuxWithTransform(); }
    @Override public void setToTranslation(Vector3 vector) { super.setToTranslation(vector); syncAuxWithTransform(); }
    @Override public void setToTranslation(float x, float y, float z) { super.setToTranslation(x, y, z); syncAuxWithTransform(); }
    @Override public void setToTranslationAndScaling(Vector3 translation, Vector3 scaling) { super.setToTranslationAndScaling(translation, scaling); syncAuxWithTransform(); }
    @Override public void setToTranslationAndScaling(float x, float y, float z, float sX, float sY, float sZ) { super.setToTranslationAndScaling(x, y, z, sX, sY, sZ); syncAuxWithTransform();}
    @Override public void scl(float factor) { super.scl(factor); syncAuxWithTransform(); }
    @Override public void scl(Vector3 factor) { super.scl(factor); syncAuxWithTransform(); }
    @Override public void scale(float scaleX, float scaleY, float scaleZ) { super.scale(scaleX, scaleY, scaleZ); syncAuxWithTransform(); }
    @Override public void setToScaling(float factor) { super.setToScaling(factor); syncAuxWithTransform(); }
    @Override public void setToScaling(Vector3 factor) { super.setToScaling(factor); syncAuxWithTransform(); }
    @Override public void rotate(Vector3 axis, float degrees) { super.rotate(axis, degrees); syncAuxWithTransform(); }
    @Override public void rotate(Quaternion rotation) { super.rotate(rotation); syncAuxWithTransform(); }
    @Override public void rotate(Vector3 v1, Vector3 v2) { super.rotate(v1, v2); syncAuxWithTransform(); }

    public void syncAuxWithTransform() {
        bbHgModelInstanceReset();
        bbCornersReset();
    }

    public void checkAnimations() {
        anim2info.clear();
        for (Animation anim:animations) { anim2info.put(anim, new AnimationInfo(this, anim)); }
    }

    public Array<String> getAnimIds(Array<String> out) {
        for (Animation anim: anim2info.keys()) { out.add(anim.id); }
        return out;
    }

    public Animation createAnimation() { return createAnimation(null); }

    public Animation createAnimation(String animId) {
        Animation newAnim = new Animation();
        String id;
        Array<String> ids = getAnimIds(new Array<>());
        if (animId == null || ids.contains(animId, false)) {
            int index = 0;
            id = "Animation " + index;
            while (ids.contains(id, false)) { id = "Animation " + (++index); }
        } else {
            id = animId;
        }
        newAnim.id = id;
        newAnim.duration = 0f;

        animations.add(newAnim);
        anim2info.put(newAnim, new AnimationInfo(this, newAnim));

        return newAnim;
    }

    public void deleteAnimation(Animation toDelete) {
        if (toDelete == null) { return; }

        int index = -1;
        if (selectedAnimation != null && selectedAnimation.equals(toDelete)) {
            index = animations.indexOf(toDelete, false);
            if (index == animations.size - 1) { index--; }
        }
        animations.removeValue(toDelete, false);
        anim2info.removeKey(toDelete);

        if (animations.size == 0 || anim2info.size == 0) { selectedAnimation = null; undoAnimations(); }
        else if (index >= 0) { selectedAnimation = animations.get(index); }
    }

    public void undoAnimations() {
        animationDesc = null;
        if (animationController != null) { animationController.setAnimation(null); }
        for (Animation a: animations) {
            for (final NodeAnimation na : a.nodeAnimations) { na.node.isAnimated = false; }
        }
        calculateTransforms();
    }

    public boolean isAnimEditMode() { return !animLoop && selectedAnimation != null; }

    public void checkMaterials() { mtlIds.clear(); for (Material mtl:materials) { mtlIds.add(mtl.id); } }

    public void checkNodeParts() { for (Node node:nodes) { checkNodeParts(node); } }

    public void checkNodeParts(Node node) {
        //Gdx.app.debug(getClass().getSimpleName(), ""
        //        + "node.id: " + node.id + " globalTransform:\n" + node.globalTransform);
        for (NodePart nodePart:node.parts) {
            if (n2np.containsKey(node)) { n2np.get(node).add(nodePart); }
            else { n2np.put(node, new Array<>(new NodePart[]{nodePart})); }

            //Gdx.app.debug(getClass().getSimpleName(), ""
            //        + (afh != null ? afh.name() : "empty") + ": node.id: " + node.id
            //        + " mesh.id: " + nodePart.meshPart.id + " material: " + nodePart.material.id);
        }

        Iterable<Node> children = node.getChildren();
        if (children != null && children.iterator().hasNext()) {
            for (Node child:children) { checkNodeParts(child); }
        }
    }

    public void createMtlAttributeTables(BaseAttributeTable.EventListener eventListener, ModelEditScreen modelES) {
        if (mtl2atable.size > 0) { return; } // tables already created
        for (Material mtl:materials) {
            AttributesManagerTable mtlAttrTable = new AttributesManagerTable(mtl, modelES);
            mtlAttrTable.setListener(eventListener);

            mtl2atable.put(mtl, mtlAttrTable);
            mtlid2atable.put(mtl.id, mtlAttrTable);
        }
    }

    public void createMtlAttributeTable(String mtlId, BaseAttributeTable.EventListener eventListener, ModelEditScreen modelES) {
        if (mtlid2atable.containsKey(mtlId)) { return; } // table already created
        Material mtl = getMaterial(mtlId);
        if (mtl != null) {
            AttributesManagerTable mtlAttrTable = new AttributesManagerTable(mtl, modelES);
            mtlAttrTable.setListener(eventListener);

            mtl2atable.put(mtl, mtlAttrTable);
            mtlid2atable.put(mtl.id, mtlAttrTable);
        }
    }

    public void createNodePartModels() {
        ModelBuilder mb = new ModelBuilder();
        if (n2np.size > 1) {
            for (ObjectMap.Entry<Node, Array<NodePart>> entry:n2np) {
                Gdx.app.debug(getClass().getSimpleName(), ""
                        + (afh != null ? afh.name() : "empty")
                        + ": creating model for node.id: " + entry.key.id
                );
                mb.begin();
                for (NodePart nodePart:entry.value) { mb.part(nodePart.meshPart, nodePart.material); }
                HGModel tmpModel = new HGModel(mb.end());

                node2model.put(entry.key, tmpModel);
                nodeid2model.put(entry.key.id, tmpModel);
                model2node.put(tmpModel, entry.key);
            }
        }
    }

    private void createBBModel() {
        if (bbHgModel != null) { bbHgModel.dispose(); }

        bbHgModel = new HGModel(createBoundingBoxModel());
    }

    public HGModelInstance getBBHgModelInstance(Color boxColor) {
        if (bbHgModel == null) { return null; }
        if (bbHgMI != null) { bbHgMI.dispose(); bbHgMI = null; }

        bbHgMI = new HGModelInstance(bbHgModel, "box");

        bbHgModelInstanceReset(boxColor);

        return bbHgMI;
    }

    public void bbHgModelInstanceReset() {
        bbHgModelInstanceReset(Color.BLACK);
    }

    public void bbHgModelInstanceReset(Color boxColor) {
        if (bbHgMI == null) { return; }
        final BoundingBox tmp = getBB();
        bbHgMI.getMaterial("box").set(ColorAttribute.createDiffuse(boxColor), new BlendingAttribute(0.1f));
        bbHgMI.getNode("box").globalTransform.setToTranslationAndScaling(tmp.getCenter(new Vector3()), tmp.getDimensions(new Vector3()));
    }

    public Array<HGModelInstance> getCornerHgModelInstances(Color cornerColor) {
        if (bbHgModel == null) { return null; }
        if (bbCornerMIs != null && bbCornerMIs.size > 0) {
            bbCornerMIs.forEach(HGModelInstance::dispose);
            bbCornerMIs.clear();
            bbCornerMIs = null;
        }

        bbCornerMIs = new Array<>(true, 16, HGModelInstance.class);
        for (int i = 0; i < 8; i++) { // BB corners
            String id = String.format("corner%3s", Integer.toBinaryString(i)).replace(' ', '0');

            HGModelInstance bbCornerHgMI = new HGModelInstance(bbHgModel, id);

            bbCornerMIs.add(bbCornerHgMI);
        }

        bbCornersReset(cornerColor);

        return bbCornerMIs;
    }

    public void bbCornersReset() { bbCornersReset(Color.RED); }

    public void bbCornersReset(Color cornerColor) {
        if (bbCornerMIs == null || bbCornerMIs.size != 8) { return; }

        BoundingBox boundingBox = getBB();
        final Vector3 bbMin = boundingBox.getMin(new Vector3());
        final Vector3 bbMax = boundingBox.getMax(new Vector3());
        for (int i = 0; i < 8; i++) { // BB corners
            String id = String.format("corner%3s", Integer.toBinaryString(i)).replace(' ', '0');

            HGModelInstance bbCornerHgMI = bbCornerMIs.get(i);

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

            bbCornerHgMI.setToTranslationAndScaling(translate, scale);
        }
    }

    public void addNodesToRenderer(HGImmediateModeRenderer20 imr) {
        n2bb.clear();
        bb2n.clear();
        for (Node node:nodes) {
            addNodeToRenderer(imr, node, Color.RED, Color.GREEN);
        }
    }

    public void addNodeToRenderer(HGImmediateModeRenderer20 imr, Node node, Color c1, Color c2) {
        addNodeToRenderer(imr, node, c1, c2, -1);
    }

    public void addNodeToRenderer(HGImmediateModeRenderer20 imr, Node node, Color c1, Color c2, int depth) {
        Matrix4 tmpM4 = transform.cpy().mul(node.globalTransform);
        Vector3 trn = tmpM4.getTranslation(new Vector3());
        Quaternion rot = tmpM4.getRotation(new Quaternion(), true);
        Vector3 scl = tmpM4.getScale(new Vector3()).nor().scl(getMaxDimension()/40f);
        tmpM4.set(trn, rot, scl);

        Color base = Color.CYAN;
        if (isAnimEditMode()) {
            base = Color.PINK;
            AnimationInfo info = anim2info.get(selectedAnimation);
            for (NodeAnimation nodeAnim: info.nAnim2keyTimes.keys().toArray()) {
                if (nodeAnim.node.equals(node)) { base = Color.ORANGE; break; }
            }
        }
        Color clr = !node.equals(hoveredOverNode) ? base : Color.PURPLE;

        BoundingBox bb = imr.box(tmpM4, clr);
        n2bb.put(node, bb);
        bb2n.put(bb, node);

        if (--depth == 0) { return; }

        Iterable<Node> children = node.getChildren();
        if (children != null && children.iterator().hasNext()) {
            for (Node child:children) {
                Vector3 p1 = transform.cpy().mul(node.globalTransform).getTranslation(new Vector3());
                Vector3 p2 = transform.cpy().mul(child.globalTransform).getTranslation(new Vector3());
                imr.line(p1, p2, c1, c2);
                addNodeToRenderer(imr, child, Color.PURPLE, Color.GREEN, depth);
            }
        }
    }

    public void addBonesToRenderer(HGImmediateModeRenderer20 imr, boolean invert) {
        for (Node node:nodes) {
            addBonesToRenderer(imr, node, invert, Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE);
        }
    }

    public void addBonesToRenderer(HGImmediateModeRenderer20 imr, Node node, boolean invert,
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
                Matrix4 tmpM4 = transform.cpy().mul(nodePart.bones[i]);
                if (invert) {
                    tmpM4.mul(nodePart.invBoneBindTransforms.values[i].cpy().inv()); // undoing the inverse
                    // now the "bone" actually become a Node's "globalTransform"
                }
                Vector3 trn = tmpM4.getTranslation(new Vector3());
                Quaternion rot = tmpM4.getRotation(new Quaternion(), true);
                Vector3 scl = tmpM4.getScale(new Vector3()).nor().scl(getMaxDimension()/40f);
                tmpM4.set(trn, rot, scl);

                imr.box(tmpM4, c1);

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
                addBonesToRenderer(imr, child, invert, c1, c2, c3, c4);
            }
        }
    }

    public void addMeshPartsToRenderer(HGImmediateModeRenderer20 imr) {
        auxMeshCounter = 0;
        for (Node node:nodes) { addMeshPartsToRenderer(imr, node); }
    }

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

    public void addMeshPartToRenderer(HGImmediateModeRenderer20 imr, Node node, NodePart nodePart, MeshPart mp) {
        if (hgModel == null || node == null || nodePart == null || !nodePart.enabled || mp == null
                || mp.mesh == null) { return; }

        HGModel.MeshData meshData = hgModel.mesh2data.get(mp.mesh);

        if (meshData == null) { return; }

        VertexAttributes vertexAttributes = meshData.vertexAttributes;
        // IMPORTANT: vertex size is in bytes, float is 4 bytes long
        // TODO: there's also a notion of 'OpenGL type': GL_FLOAT or GL_UNSIGNED_BYTE stored in VertexAttribute.type
        int vs = vertexAttributes.vertexSize / 4;

        short[] indices = meshData.indices;
        float[] vertices = meshData.vertices;

        //Gdx.app.debug(getClass().getSimpleName(), "indices: \n" + Arrays.toString(indices));
        //Gdx.app.debug(getClass().getSimpleName(), "vertices: \n" + Arrays.toString(vertices));

        // 0000 0000 0001 Position = 1;
        // 0000 0000 0010 ColorUnpacked = 2;
        // 0000 0000 0100 ColorPacked = 4;
        // 0000 0000 1000 Normal = 8;
        // 0000 0001 0000 TextureCoordinates = 16;
        // 0000 0010 0000 Generic = 32;
        // 0000 0100 0000 BoneWeight = 64;
        // 0000 1000 0000 Tangent = 128;
        // 0001 0000 0000 BiNormal = 256;

        //Gdx.app.debug(getClass().getSimpleName(), "vertex attributes mask: 0b"
        //        + Long.toBinaryString(vertexAttributes.getMask()));

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

        // Attribute: Normal
        VertexAttribute vaNormal = vertexAttributes.findByUsage(Normal);
        int no = -1; int nn = -1;
        if (vaNormal != null) { no = vaNormal.offset / 4; nn = vaNormal.numComponents; }

        // Attribute: TextureCoordinates
        VertexAttribute vaTextureCoordinates = vertexAttributes.findByUsage(TextureCoordinates);
        int tco = -1; int tcn = -1;
        if (vaTextureCoordinates != null) { tco = vaTextureCoordinates.offset / 4; tcn = vaTextureCoordinates.numComponents; }

        // Attribute: BoneWeight
        // IMPORTANT:
        // see NodePart.setRenderable (final Renderable out)
        // see Renderable.bones description (source code one, not the javadoc)
        VertexAttribute vaBoneWeight = vertexAttributes.findByUsage(BoneWeight);
        int bwo = -1; int bwn = -1;
        if (vaBoneWeight != null) { bwo = vaBoneWeight.offset / 4; bwn = vaBoneWeight.numComponents; }

        //Gdx.app.debug(getClass().getSimpleName(), ""
        //        + " po: " + po + " pn: " + pn + " no: " + no + " nn: " + nn
        //        + " tco: " + tco + " tcn: " + tcn + " bwo: " + bwo + " bwn: " + bwn);
        //Gdx.app.debug(getClass().getSimpleName(), ""
        //        + " mesh part: " + mp.id + " prim type: " + mp.primitiveType
        //        + " offset: " + mp.offset + " size: " + mp.size
        //        + "\ntransform: \n" + transform + "node.globalTransform: \n" + node.globalTransform);

        Vector3 tmpV1 = Vector3.Zero.cpy();
        Vector3 tmpV2 = Vector3.Zero.cpy();
        Vector3 tmpV3 = Vector3.Zero.cpy();
        Matrix4 tmpM1 = new Matrix4();
        Matrix4 tmpM2 = new Matrix4();
        Matrix4 tmpM3 = new Matrix4();

        Color color = aux_colors.get(auxMeshCounter++ % aux_colors.size);
        switch (mp.primitiveType) {
            case GL_LINES:
                //...
                break;
            case GL_TRIANGLES:
                for (int i = mp.offset; i < mp.offset + mp.size; i += 3) { // 3 corners of a triangle
                    tmpV1.set(vertices[vs*indices[i+0]+po], vertices[vs*indices[i+0]+po+1], vertices[vs*indices[i+0]+po+2]);
                    tmpV2.set(vertices[vs*indices[i+1]+po], vertices[vs*indices[i+1]+po+1], vertices[vs*indices[i+1]+po+2]);
                    tmpV3.set(vertices[vs*indices[i+2]+po], vertices[vs*indices[i+2]+po+1], vertices[vs*indices[i+2]+po+2]);
                    //Gdx.app.debug(getClass().getSimpleName(), "1: " + tmpV1 + " 2: " + tmpV2 + " 3: " + tmpV3);

                    tmpM1.set(transform); tmpM2.set(transform); tmpM3.set(transform);
                    if (bwo > 0) {
                        // ignoring bwn for now...
                        tmpM1.mul(nodePart.bones[(short)vertices[vs*indices[i+0]+bwo]]);
                        tmpM2.mul(nodePart.bones[(short)vertices[vs*indices[i+1]+bwo]]);
                        tmpM3.mul(nodePart.bones[(short)vertices[vs*indices[i+2]+bwo]]);
                    } else {
                        tmpM1.mul(node.globalTransform);
                        tmpM2.mul(node.globalTransform);
                        tmpM3.mul(node.globalTransform);
                    }
                    //Gdx.app.debug(getClass().getSimpleName(), "\n1:\n" + tmpM1 + "2:\n" + tmpM2 + "3:\n" + tmpM3);
                    tmpV1.mul(tmpM1); tmpV2.mul(tmpM2); tmpV3.mul(tmpM3);
                    //Gdx.app.debug(getClass().getSimpleName(), "1: " + tmpV1 + " 2: " + tmpV2 + " 3: " + tmpV3);

                    // see https://www.khronos.org/opengl/wiki/Vertex_Specification
                    // see https://www.khronos.org/opengl/wiki/Vertex_Rendering
                    // see https://www.khronos.org/opengl/wiki/Primitive
                    // GL_TRIANGLES, VertexAttributes.Usage.Position:
                    // indices:  [0, 1, 2, 2, 3, 0, 5, 4, 7, 7, 6, 5, 0, 3, 7, 7, 4, 0, 5, 6, 2, 2, 1, 5, 5, 1, 0, 0, 4, 5, 2, 6, 7, 7, 3, 2] : 36 indices, 12 triangles
                    // vertices: [-0.5, -0.5, -0.5, 0.5, -0.5, -0.5, 0.5, 0.5, -0.5, -0.5, 0.5, -0.5, -0.5, -0.5, 0.5, 0.5, -0.5, 0.5, 0.5, 0.5, 0.5, -0.5, 0.5, 0.5]

                    imr.triangle(tmpV1, tmpV2, tmpV3, color, color, color);
                }
                break;
            default:
                Gdx.app.error(getClass().getSimpleName(), "add mesh part: UNSUPPORTED primitive type " + mp.primitiveType);
                break;
        }
    }

    public void addVerticesToRenderer(HGImmediateModeRenderer20 imr) {
        addVerticesToRenderer(imr, Color.PINK, Color.CYAN);
    }

    public void addVerticesToRenderer(HGImmediateModeRenderer20 imr, Color cCenter, Color cVertex) {
        Vector3 tmpV1 = new Vector3();
        Vector3 tmpV2 = new Vector3();

        //Gdx.app.debug("vertices", hgModel.getVertices().toString(","));
        for (Vector3 vertex: hgModel.getVertices()) {
            tmpV1.set(Vector3.Zero).mul(transform);
            tmpV2.set(vertex).mul(transform);

            //Gdx.app.debug("vertices", "vertex: " + tmpV2);
            imr.line(tmpV1, tmpV2, cCenter, cVertex);
        }
    }

    public void addClosestVerticesToRenderer(HGImmediateModeRenderer20 imr) {
        addClosestVerticesToRenderer(imr, Color.BLUE, Color.GREEN);
    }

    public void addClosestVerticesToRenderer(HGImmediateModeRenderer20 imr, Color cCorner, Color cVertex) {
        Vector3 tmpV1 = new Vector3();
        Vector3 tmpV2 = new Vector3();
        ArrayMap<Vector3, Vector3> corner2closest = new ArrayMap<>(Vector3.class, Vector3.class);
        for (int corner = 0; corner < 8; corner++) {
            getBBCorner(corner, tmpV1);
            hgModel.closestVertex(tmpV1, tmpV2);
            corner2closest.put(tmpV1.cpy(), tmpV2.cpy());
        }

        if (corner2closest.size != 8) { return; } // expecting all 8 corners

        for (ObjectMap.Entry<Vector3, Vector3> entry: corner2closest) {
            imr.line(entry.key.mul(transform), entry.value.mul(transform), cCorner, cVertex);
        }
    }

    public void addSelectionBoxToRenderer(HGImmediateModeRenderer20 imr, Color clr) {
        BoundingBox bb = getBB(true);
        Vector3 bottomBoxCenter = bb.getCenter(new Vector3()).sub(0f, bb.getHeight()/2f, 0f);
        float half = getMaxDimension()/2f;
        imr.line(bottomBoxCenter.cpy().sub(half, 0f, half), bottomBoxCenter.cpy().sub(half, 0f, -half), clr, clr);
        imr.line(bottomBoxCenter.cpy().sub(half, 0f, -half), bottomBoxCenter.cpy().sub(-half, 0f, -half), clr, clr);
        imr.line(bottomBoxCenter.cpy().sub(-half, 0f, -half), bottomBoxCenter.cpy().sub(-half, 0f, half), clr, clr);
        imr.line(bottomBoxCenter.cpy().sub(-half, 0f, half), bottomBoxCenter.cpy().sub(half, 0f, half), clr, clr);
    }

    public void animApplyKeyTime() { animApplyKeyTime(currKeyTime); }

    public void animApplyKeyTime(float keytime) {
        if (selectedAnimation == null || keytime < 0 || keytime > selectedAnimation.duration) { return; }
        currKeyTime = keytime;
        //Gdx.app.debug("ChangeListener", ""
        //        + " anim id: " + selectedAnimation.id + " value: " + keytime);

        for (NodeAnimation nodeAnim:selectedAnimation.nodeAnimations) { animApplyNodeAnimation(nodeAnim, keytime); }

        // see ModelInstance.calculateTransforms:
        // calculate both local and global transforms for each node and subnodes recursively.
        // IMPORTANT to have isAnimated set to true so local transform is not reset
        // seemingly bones transforms is based on the updated global transforms
        calculateTransforms();
    }

    public void animApplyNodeAnimation(NodeAnimation nodeAnim, float keytime) {
        // Getting the default local values for the node (prior any animations applied)
        Vector3 tmpTrans = nodeAnim.node.translation.cpy();
        Quaternion tmpRot = nodeAnim.node.rotation.cpy();
        Vector3 tmpScale = nodeAnim.node.scale.cpy();

        // the translation keyframes if any (might be null), sorted by time ascending
        if (nodeAnim.translation != null) {
            for (NodeKeyframe<Vector3> nTrans:nodeAnim.translation) {
                if (nTrans.keytime <= keytime) { tmpTrans.set(nTrans.value); }
                else { break; }}}
        // the rotation keyframes if any (might be null), sorted by time ascending
        if (nodeAnim.rotation != null) {
            for (NodeKeyframe<Quaternion> nRot:nodeAnim.rotation) {
                if (nRot.keytime <= keytime) { tmpRot.set(nRot.value); }
                else { break; }}}
        // the scaling keyframes if any (might be null), sorted by time ascending
        if (nodeAnim.scaling != null) {
            for (NodeKeyframe<Vector3> nScale:nodeAnim.scaling) {
                if (nScale.keytime <= keytime) { tmpScale.set(nScale.value); }
                else { break; }}}
        //Gdx.app.debug("", ""
        //        + " node.id: " + nodeAnim.node.id + " node.parts.size: " + nodeAnim.node.parts.size
        //        + " trans: " + tmpTrans + " rot: " + tmpRot + " scale: " + tmpScale
        //);
        // setting isAnimated to true so the localTransform isn't reset to the base values.
        // the real check happens in node.calculateLocalTransform()
        nodeAnim.node.isAnimated = true;

        // setting the local transform to the values from key frames (if not, the default used)
        nodeAnim.node.localTransform.set(tmpTrans, tmpRot, tmpScale);
    }

    public void setSelectedAnimation(String animId) { selectedAnimation = getAnimation(animId); }
}
