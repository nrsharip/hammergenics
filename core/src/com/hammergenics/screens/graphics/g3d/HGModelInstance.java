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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;

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
    public Vector3 center = new Vector3();
    public Vector3 dims = new Vector3();
    public final float maxD;
    public AnimationController animationController = null;
    public AnimationController.AnimationDesc animationDesc = null;

    public HGModelInstance (final Model model) { this(new HGModel(model), null, (String[])null); }
    public HGModelInstance (final Model model, final String... rootNodeIds) { this(new HGModel(model), null, rootNodeIds); }
    public HGModelInstance (final HGModel hgModel) { this(hgModel, null, (String[])null); }
    public HGModelInstance (final HGModel hgModel, final String... rootNodeIds) { this(hgModel, null, rootNodeIds); }
    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL) { this(hgModel, assetFL, (String[])null); }

    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL, final String... rootNodeIds) {
        super(hgModel.obj, rootNodeIds);

        this.hgModel = hgModel;
        this.afh = assetFL;
        this.rni = rootNodeIds;
        animationController = new AnimationController(this);

        calculateBoundingBox(bb);
        bb.getCenter(center);
        bb.getDimensions(dims);
        maxD = Math.max(Math.max(dims.x, dims.y), dims.z);
    }

    @Override
    public void dispose() {
        // hgModel is being disposed by the AssetManager
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
    public BoundingBox getBB() { return getBB(true); }
    public BoundingBox getBB(boolean applyTrans) {
        if (applyTrans) { return new BoundingBox(bb).mul(transform); }
        else { return new BoundingBox(bb); }
    }
    public float getMaxDimension() {
        Vector3 dims = getBB().getDimensions(new Vector3());
        return Math.max(Math.max(dims.x, dims.y), dims.z);
    }

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
}
