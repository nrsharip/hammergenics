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

import com.hammergenics.core.stages.ui.ai.AIVisWindow;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AIManagerTable extends ManagerTable {
    public AIVisWindow aiVisWindow;

    public AIManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        VisTable windows = new VisTable();

        windows.add().expand().top().left();
        windows.add(aiVisWindow).expand().top().right();

        add(windows).expand().fill();
    }

    @Override
    protected void init() {
        aiVisWindow = new AIVisWindow(modelES, stage);
    }

    public void update() {
        EditableModelInstance mi = dbgModelInstance;

        aiVisWindow.update(mi);
    }

    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);

        update();
    }

    public void applyLocale() {

    }
}
