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

package com.hammergenics.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;

import java.util.Arrays;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum TerrainPartsEnum {
    TRRN_FLAT("flat surface") {
        @Override
        public boolean parseMesh(HGModel model, HGModelInstance sample) {

            return true;
        }
    },
    TRRN_SIDE("side surface") {
        @Override
        public boolean parseMesh(HGModel model, HGModelInstance sample) {

            return true;
        }
    },
    TRRN_CORN_INN("inner corner surface") {
        @Override
        public boolean parseMesh(HGModel model, HGModelInstance sample) {
            // expecting one of the top BB corners to be significantly farther
            // from the vertices than the rest three top corners

            int[] indices = {0b010, 0b011, 0b110, 0b111};

            for (int i: indices) { sample.getBBCorner(i, false, cornerVs.get(i)); }
            for (int i: indices) { model.closestVertex(cornerVs.get(i), vertexVs.get(i)); }

            final ArrayMap<Integer, Float> index2distance = new ArrayMap<>(Integer.class, Float.class);

            Arrays.stream(indices).forEach(i -> index2distance.put(i, cornerVs.get(i).dst2(vertexVs.get(i))));

            float max = Float.MIN_VALUE;
            int tmp = -1;

            for (ObjectMap.Entry<Integer, Float> entry: index2distance) {
                if (max < entry.value) {
                    tmp = entry.key;
                    max = entry.value;
                }
            }

            // setting the actual leading corner to be the one below the top corner with the most distance
            tmp ^= 0b010;

            leadingCornerV.set(sample.getBBCorner(tmp, false, cornerVs.get(tmp)));
            leadingCornerI2d = ((tmp & 0b100) >> 1) | (tmp & 0b001);
            leadingCornerI3d = tmp;

            //Gdx.app.debug("TRRN_CORN_INN", "index: " + leadingCornerI + " vector: " + leadingCornerV);
            return true;
        }
    },
    TRRN_CORN_OUT("outer corner surface") {
        @Override
        public boolean parseMesh(HGModel model, HGModelInstance sample) {

            return true;
        }
    };

    public String description;

    TerrainPartsEnum(String description) { this.description = description; }

    public FileHandle fh = null;
    public HGModel model = null;
    public HGModelInstance sample = null;
    public Vector3 leadingCornerV = new Vector3(Vector3.Zero);
    public int leadingCornerI2d = -1;
    public int leadingCornerI3d = -1;
    public boolean ready = false;

    public boolean processFileHandle(AssetManager am, FileHandle fh) {
        HGModel model;
        if (am == null || fh == null || !am.contains(fh.path())) { return false; }
        model = new HGModel(am.get(fh.path(), Model.class), fh);
        if (!model.hasMeshes()) { return false; }
        HGModelInstance sample = new HGModelInstance(model);

        if (!parseMesh(model, sample)) { return false; }

        this.fh = fh;
        this.model = model;
        this.sample = sample;
        this.ready = true;
        return true;
    }

    public void clear() {
        this.fh = null;
        // no need to dispose the model - should be taken care of by the asset manager
        this.model = null;
        this.sample = null;
        this.leadingCornerV.set(Vector3.Zero);
        this.leadingCornerI2d = -1;
        this.leadingCornerI3d = -1;
        this.ready = false;
    }

    public static void clearAll() {
        for (TerrainPartsEnum tp: TerrainPartsEnum.values()) { tp.clear(); }
    }

    public abstract boolean parseMesh(HGModel model, HGModelInstance sample);


    private final static Array<Vector3> cornerVs = new Array<>(new Vector3[]{
            new Vector3(), new Vector3(), new Vector3(), new Vector3(),
            new Vector3(), new Vector3(), new Vector3(), new Vector3()
    });

    private final static Array<Vector3> vertexVs = new Array<>(new Vector3[]{
            new Vector3(), new Vector3(), new Vector3(), new Vector3(),
            new Vector3(), new Vector3(), new Vector3(), new Vector3()
    });
}