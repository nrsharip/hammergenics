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
import com.hammergenics.core.stages.ui.physics.bullet.dynamics.btDynamicsWorldVisWindow;
import com.hammergenics.core.stages.ui.physics.bullet.dynamics.btRigidBodyVisWindow;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicsManagerVisTable extends ManagerVisTable {

    public btDynamicsWorldVisWindow dynamicsWindow;
    public btRigidBodyVisWindow rigidBodyWindow;

    public PhysicsManagerVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        dynamicsWindow = new btDynamicsWorldVisWindow(modelES, stage);
        rigidBodyWindow = new btRigidBodyVisWindow(modelES, stage);

        VisTable windows = new VisTable();

        windows.add(dynamicsWindow).expand().top().left();
        windows.add(rigidBodyWindow).expand().top().right();

        add(windows).expand().fill();

        dynamicsWindow.update(0f);
    }

    @Override
    protected void init() {
    }


    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        dynamicsWindow.update(0f);
        rigidBodyWindow.setDbgModelInstances(mis);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        dynamicsWindow.update(delta);
        rigidBodyWindow.update(delta);
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}
