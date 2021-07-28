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
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.animations.AnimationsVisWindow;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationsManagerVisTable extends ManagerVisTable {
    public AnimationsVisWindow animationsVisWindow;
    public AnimationsManagerVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        VisTable windows = new VisTable();

        windows.add().expand().top().left();
        windows.add(animationsVisWindow).expand().top().right();

        add(windows).expand().fill();
    }

    @Override
    protected void init() {
        animationsVisWindow = new AnimationsVisWindow(modelES, stage);
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        animationsVisWindow.setDbgModelInstances(mis);
    }

    @Override
    public void resetActors() {
        super.resetActors();

    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}
