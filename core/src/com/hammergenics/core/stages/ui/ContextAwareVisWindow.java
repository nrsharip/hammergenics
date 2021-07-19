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

package com.hammergenics.core.stages.ui;

import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class ContextAwareVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;
    public Array<EditableModelInstance> dbgModelInstances = new Array<>(true, 16, EditableModelInstance.class);
    public HGEngine eng;

    public ContextAwareVisWindow(String title, ModelEditScreen modelES, ModelEditStage stage) {
        super(title);
        this.modelES = modelES;
        this.eng = modelES.eng;
        this.stage = stage;
    }

    public void setDbgModelInstance(EditableModelInstance mi) { this.dbgModelInstance = mi; }

    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        dbgModelInstances.clear();
        if (mis == null) { return; }
        this.dbgModelInstances.addAll(mis);
    }

    public void update(float delta) { }

    public abstract void applyLocale();
}
