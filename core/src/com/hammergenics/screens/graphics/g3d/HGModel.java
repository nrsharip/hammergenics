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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGModel {
    /**
     * model object
     */
    public Model obj;
    /**
     * asset file handle
     */
    public FileHandle afh;

    public HGModel(Model model, FileHandle assetFileHandle) {
        this.obj = model;
        this.afh = assetFileHandle;
    }

    public boolean hasAnimations() { return obj.animations.size != 0; }
    public boolean hasMaterials() { return obj.materials.size != 0; }
    public boolean hasMeshes() { return obj.meshes.size != 0; }
    public boolean hasMeshParts() { return obj.meshParts.size != 0; }
    public boolean hasNodes() { return obj.nodes.size != 0; }
}
