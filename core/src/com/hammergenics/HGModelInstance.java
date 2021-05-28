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

package com.hammergenics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGModelInstance extends ModelInstance {
    public Model model;
    /**
     * asset name
     */
    public String an;
    /**
     * root node ids
     */
    public String[] rni;
    /**
     * scaling factor (default 1)
     */
    public float sf = 1f;

    public BoundingBox bb;
    public Vector3 dims;
    public Vector3 center;
    public float maxD;
    public AnimationController animationController = null;
    public AnimationController.AnimationDesc animationDesc = null;
    public int animationIndex = 0;

    public HGModelInstance (final Model model, final String assetName) {
        this(model, assetName, (String[])null);
    }

    public HGModelInstance (final Model model, final String assetName, final String... rootNodeIds) {
        super(model, rootNodeIds);

        this.model = model;
        this.an = assetName;
        this.rni = rootNodeIds;
    }

    public void moveTo(Vector3 vector) { transform.setToTranslation(vector); }

    public void moveTo(float x, float y, float z) { transform.setToTranslation(x, y, z); }

    public void scale(float factor) { this.sf = factor; transform.setToScaling(factor, factor, factor); }

    public void recalculate() {
        bb = new BoundingBox();
        calculateBoundingBox(bb);
        dims = bb.getDimensions(new Vector3());
        center = bb.getCenter(new Vector3());
        maxD = Math.max(Math.max(dims.x, dims.y), dims.z);
    }
}
