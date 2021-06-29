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
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum TerrainPartsEnum {
    TRRN_FLAT("flat surface"),
    TRRN_SIDE("side surface"),
    TRRN_SIDE_CORN_INN("inner corner surface"),
    TRRN_SIDE_CORN_OUT("outer corner surface");

    public String description;

    TerrainPartsEnum(String description) { this.description = description; }

    public FileHandle fh = null;
    public HGModel model = null;
    public HGModelInstance sample = null;

    public boolean processFileHandle(AssetManager am, FileHandle fh) {
        HGModel model;
        if (am == null || fh == null || !am.contains(fh.path())) { return false; }
        model = new HGModel(am.get(fh.path(), Model.class), fh);
        if (!model.hasMeshes()) { return false; }

        this.fh = fh;
        this.model = model;
        sample = new HGModelInstance(model);
        return true;
    }

    public static void clearAll() {
        for (TerrainPartsEnum tp: TerrainPartsEnum.values()) {
            tp.fh = null;
            // no need to dispose the model - should be taken care of by the asset manager
            tp.model = null;
            tp.sample = null;
        }
    }
}