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
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;

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
    TRRN_SIDE_CORN_INN("inner corner surface") {
        @Override
        public boolean parseMesh(HGModel model, HGModelInstance sample) {

            return true;
        }
    },
    TRRN_SIDE_CORN_OUT("outer corner surface") {
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
    public Vector3 leadingCorner = new Vector3(Vector3.Zero);
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
        this.leadingCorner.set(Vector3.Zero);
        this.ready = false;
    }

    public static void clearAll() {
        for (TerrainPartsEnum tp: TerrainPartsEnum.values()) { tp.clear(); }
    }

    public abstract boolean parseMesh(HGModel model, HGModelInstance sample);
}