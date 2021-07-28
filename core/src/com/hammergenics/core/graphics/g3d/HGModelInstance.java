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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.core.graphics.g3d.utils.HGAnimationController;

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

    public static final Array<Integer> indices2d = new Array<>(new Integer[]{0b00, 0b01, 0b11, 0b10});

    private final BoundingBox bb = new BoundingBox();
    public Vector3 center = new Vector3();
    public Vector3 dims = new Vector3();
    public final float maxD;
    public final float radius;
    public HGAnimationController animationController = null;
    public AnimationController.AnimationDesc animationDesc = null;

    public HGModelInstance (final Model model) { this(new HGModel(model), null, (String[])null); }
    public HGModelInstance (final Model model, final String... rootNodeIds) { this(new HGModel(model), null, rootNodeIds); }
    public HGModelInstance (final HGModel hgModel) { this(hgModel, null, (String[])null); }
    public HGModelInstance (final HGModel hgModel, final String... rootNodeIds) { this(hgModel, null, rootNodeIds); }
    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL) { this(hgModel, assetFL, (String[])null); }

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL, final String... rootNodeIds) {
        super(hgModel.obj, rootNodeIds);

        this.hgModel = hgModel;
        this.afh = assetFL;
        this.rni = rootNodeIds;
        animationController = new HGAnimationController(this);

        calculateBoundingBox(bb);
        bb.getCenter(center); // should be Vector3.Zero after HGModel::centerToOrigin
        bb.getDimensions(dims);
        maxD = Math.max(Math.max(dims.x, dims.y), dims.z);
        // see: https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
        radius = dims.len() / 2f;
    }

    @Override
    public void dispose() {
        // hgModel is being disposed by the AssetManager
    }

    @Override
    public String toString() {
        return hgModel.afh != null ? hgModel.afh.nameWithoutExtension() : nodes.get(0).id;
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

    public void trn(Vector3 vector) { transform.trn(vector); }
    public void trn(float x, float y, float z) { transform.trn(x, y, z); }
    public void translate(Vector3 translation) { transform.translate(translation); }
    public void setTranslation(Vector3 vector) { transform.setTranslation(vector); }
    public void setTranslation(float x, float y, float z) { transform.setTranslation(x, y, z); }
    public void setToTranslation(Vector3 vector) { transform.setToTranslation(vector); }
    public void setToTranslation(float x, float y, float z) { transform.setToTranslation(x, y, z); }
    public void setToTranslationAndScaling(Vector3 translation, Vector3 scaling) { transform.setToTranslationAndScaling(translation, scaling); }
    public void setToTranslationAndScaling(float x, float y, float z, float sX, float sY, float sZ) { transform.setToTranslationAndScaling(x, y, z, sX, sY, sZ); }
    public void scl(float factor) { transform.scl(factor, factor, factor); }
    public void scl(Vector3 factor) { transform.scl(factor); }
    public void scale(float scaleX, float scaleY, float scaleZ) { transform.scale(scaleX, scaleY, scaleZ); }
    public void setToScaling(float factor) { transform.setToScaling(factor, factor, factor); }
    public void setToScaling(Vector3 factor) { transform.setToScaling(factor); }
    public void rotate(Vector3 axis, float degrees) { transform.rotate(axis, degrees); }
    public void rotate(Quaternion rotation) { transform.rotate(rotation); }
    public void rotate(final Vector3 v1, final Vector3 v2) { transform.rotate(v1, v2); }

    public float getMaxScale() {
        transform.getScale(tmpV1);
        return Math.max(Math.max(tmpV1.x, tmpV1.y), tmpV1.z);
    }
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

    public Vector3 getBBCorner(int corner, Vector3 out) { return getBBCorner(corner, false, out); }

    public Vector3 getBBCorner(int corner, boolean transform, Vector3 out) {
        if (out == null) { return null; }
        BoundingBox bb = transform ? getBB(true) : this.bb;

        // By default the screen camera is looking 111 -> 000 direction
        //
        //       010 **─────────** 110
        //         ** │       ** │
        //       **   │     **   │
        // 011 **─────────** 111 │
        //     │      │    │     │
        //     │ 000 **────│────** 100
        //     │   **      │  **
        //     │ **        │**
        // 001 **─────────** 101
        //
        // created with https://asciiflow.com/
        if (((corner % 8) & (1 << 2)) == 0) { out.x = bb.min.x; } else { out.x = bb.max.x; }
        if (((corner % 8) & (1 << 1)) == 0) { out.y = bb.min.y; } else { out.y = bb.max.y; }
        if (((corner % 8) & (1 << 0)) == 0) { out.z = bb.min.z; } else { out.z = bb.max.z; }
        return out;
    }

    public static int getNext2dIndex(int ind2d) {
        return indices2d.get((indices2d.indexOf(ind2d & 0b11, false) + 1) & 0b11);
    }

    public static int getNext3dIndex(int ind3d) {
        int ind2d = ((ind3d & 0b100) >> 1) | (ind3d & 0b001);
        ind2d = getNext2dIndex(ind2d);
        return ((ind2d & 0b10) << 1) | (ind3d & 0b010) | (ind2d & 0b01);
    }

    public static int get90DegreeRotationSteps(int fromCorner2D, int toCorner2D) {
        int start = indices2d.indexOf(fromCorner2D & 0b11, false);
        int end = indices2d.indexOf(toCorner2D & 0b11, false);
        return end - start;
    }
}
