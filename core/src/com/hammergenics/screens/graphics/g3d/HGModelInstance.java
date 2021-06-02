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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGModelInstance extends ModelInstance {
    public HGModel hgModel;
    /**
     * asset file handle
     */
    public FileHandle afh;
    /**
     * root node ids
     */
    public String[] rni;
    /**
     * scaling factor (default 1)
     */
    public float sf = 1f;

    public BoundingBox bb = new BoundingBox();
    public float maxD;
    public AnimationController animationController = null;
    public AnimationController.AnimationDesc animationDesc = null;
    public int animationIndex = 0;

    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL) {
        this(hgModel, assetFL, (String[])null);
    }

    public HGModelInstance (final HGModel hgModel, final FileHandle assetFL, final String... rootNodeIds) {
        super(hgModel.obj, rootNodeIds);

        this.hgModel = hgModel;
        this.afh = assetFL;
        this.rni = rootNodeIds;
    }

    public String getTag(int depth) {
        return Thread.currentThread().getStackTrace()[depth].getMethodName() + ":" + afh.name() + "@" + hashCode();
    }

    public void debug() {
        Gdx.app.debug(getTag(3), "absCenter = " + absCenter(new Vector3()));
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

    /**
     * ATTENTION: scaling factor (sf) applied to the Bounding Box's center vector.
     *
     * @param vector
     * @return
     */
    public Vector3 absCenter(Vector3 vector) {
        return transform.getTranslation(vector).add(bb.getCenter(new Vector3()).scl(sf));
    }

    public void moveTo(Vector3 vector) { transform.setToTranslation(vector); }

    public void moveTo(float x, float y, float z) { transform.setToTranslation(x, y, z); }

    public void moveAndScale(Vector3 vector, float factor) {
        this.sf = factor;
        transform.setToTranslationAndScaling(vector.x, vector.y, vector.z, factor, factor, factor);
    }

    public void scale(float factor) {
        this.sf = factor;
        transform.setToScaling(factor, factor, factor);
    }

    public void recalculate() {
        calculateBoundingBox(bb);
        Vector3 dims = bb.getDimensions(new Vector3());
        maxD = Math.max(Math.max(dims.x, dims.y), dims.z);
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
