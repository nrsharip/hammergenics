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

package com.hammergenics.core.stages.ui.ai.steer;

import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SteeringVisTable extends ContextAwareVisTable {
    public SteerableVisTable steerableVisTable;
    public SteeringBehaviorsVisTable steeringBehaviorsVisTable;

    public SteeringVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        init();

        steerableVisTable = new SteerableVisTable(modelES, stage);
        steeringBehaviorsVisTable = new SteeringBehaviorsVisTable(modelES, stage);

        add(steerableVisTable).expandX().center();
        row();
        add(new Separator("menu")).expandX().fillX().pad(5f);
        row();
        add(steeringBehaviorsVisTable).expandX().center();
    }

    public void init() {

    }

    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);
        steerableVisTable.setDbgModelInstance(mi);
        steeringBehaviorsVisTable.setDbgModelInstance(mi);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        steerableVisTable.update(delta);
        steeringBehaviorsVisTable.update(delta);
    }

    @Override
    public void applyLocale() { }
}