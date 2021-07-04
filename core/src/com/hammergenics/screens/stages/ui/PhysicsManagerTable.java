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

package com.hammergenics.screens.stages.ui;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.EditableModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicsManagerTable extends HGTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;

    public CheckBox dynamicsCheckBox;
    public CheckBox rbCheckBox;
    public CheckBox groundCheckBox;

    public PhysicsManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.stage = stage;

        init();

        Table row01 = new Table();
        row01.add(dynamicsCheckBox).pad(3f);
        row01.add(rbCheckBox).pad(3f);
        row01.add(groundCheckBox).pad(3f);

        add(row01).center().expandX().fillX();
    }

    private void init() {
        dynamicsCheckBox = new CheckBox("enable dynamics", stage.skin);
        dynamicsCheckBox.setChecked(false);
        dynamicsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (!dynamicsCheckBox.isChecked()) modelES.eng.arrangeInSpiral(stage.origScaleCheckBox.isChecked());
            }
        });

        rbCheckBox = new CheckBox("rigid body", stage.skin);
        rbCheckBox.setChecked(false);

        groundCheckBox = new CheckBox("ground", stage.skin);
        groundCheckBox.setChecked(false);
    }

    public void setDbgModelInstance(EditableModelInstance mi) {
        this.dbgModelInstance = mi;
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.editCell.setActor(this);
    }
}
