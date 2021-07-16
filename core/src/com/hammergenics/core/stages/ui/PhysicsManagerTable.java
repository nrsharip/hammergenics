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

import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.physics.bullet.ui.dynamics.btDynamicsWorldVisWindow;
import com.hammergenics.physics.bullet.ui.dynamics.btRigidBodyVisWindow;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicsManagerTable extends ManagerTable {

    public btDynamicsWorldVisWindow dynamicsWindow;
    public btRigidBodyVisWindow rigidBodyWindow;

    public PhysicsManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        dynamicsWindow = new btDynamicsWorldVisWindow(modelES, stage);
        rigidBodyWindow = new btRigidBodyVisWindow(modelES, stage);

        VisTable windows = new VisTable();

        windows.add(dynamicsWindow).expandX().fillX();
        windows.row();
        windows.add(rigidBodyWindow).expandX().fillX();

        add(windows).expand().top().right();

        dynamicsWindow.updateDynamicsWorld();
    }

    @Override
    protected void init() {
    }


    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);

        dynamicsWindow.updateDynamicsWorld();
        rigidBodyWindow.updateRigidBody(mi);
    }

    public void updateRigidBody() {
        rigidBodyWindow.updateRigidBody(dbgModelInstance);
    }

    public void applyLocale() {

    }
}
