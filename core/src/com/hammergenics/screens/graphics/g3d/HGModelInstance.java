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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.screens.graphics.glutils.HGImmediateModeRenderer20;

import static com.hammergenics.screens.graphics.g3d.utils.Models.createBoundingBoxModel;

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
    public HGModelInstance getBBHgModelInstance(Color boxColor, Color cornerColor) {
        if (bbHgModel == null) { return null; }

        final BoundingBox bb = getBB();
        final Vector3 bbMin = bb.getMin(new Vector3());
        final Vector3 bbMax = bb.getMax(new Vector3());

        HGModelInstance bbHgMI = new HGModelInstance(bbHgModel);

        bbHgMI.getMaterial("box").set(ColorAttribute.createDiffuse(boxColor), new BlendingAttribute(0.1f));
        bbHgMI.getNode("box").globalTransform.setToTranslationAndScaling(bb.getCenter(new Vector3()), bb.getDimensions(new Vector3()));

        for (int i = 0; i < 8; i++) { // BB corners
            String id = String.format("corner%3s", Integer.toBinaryString(i)).replace(' ', '0');
            bbHgMI.getMaterial(id).set(ColorAttribute.createDiffuse(cornerColor), new BlendingAttribute(0.3f));

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

            bbHgMI.getNode(id).globalTransform.setToTranslationAndScaling(translate, scale);
        }
        return bbHgMI;
    }
    // TODO: keep this separate for now - move to another class?
    public void addNodesToRenderer(HGImmediateModeRenderer20 imr) {
        for (Node node:nodes) {
            addNodeToRenderer(imr, node, Color.RED, Color.GREEN);
        }
    }
    // TODO: keep this separate for now - move to another class?
    public void addNodeToRenderer(HGImmediateModeRenderer20 imr, Node node, Color c1, Color c2) {
        Iterable<Node> children = node.getChildren();

        if (children == null || !children.iterator().hasNext()) {
            // TODO: do something for no children
        } else {
            for (Node child:children) {
                Vector3 p1 = node.globalTransform.cpy().mulLeft(transform).getTranslation(new Vector3());
                Vector3 p2 = child.globalTransform.cpy().mulLeft(transform).getTranslation(new Vector3());
                imr.line(p1, p2, c1, c2);
                addNodeToRenderer(imr, child, Color.PURPLE, Color.GREEN);
            }
        }
    }
}
