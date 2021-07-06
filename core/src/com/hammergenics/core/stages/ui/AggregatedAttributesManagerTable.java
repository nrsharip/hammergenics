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


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AggregatedAttributesManagerTable extends VisTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;

    public Cell<?> attrTableCell;
    
    public VisTextButton mtlTextButton;
    public VisTextButton envTextButton;
    public VisSelectBox<String> mtlSelectBox;

    public AggregatedAttributesManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        this.modelES = modelES;
        this.stage = stage;

        init();

        VisTable topPanel = new VisTable();
        topPanel.add(envTextButton).padRight(5f);
        topPanel.add(mtlTextButton).padRight(5f);
        topPanel.add(mtlSelectBox);
        topPanel.add().expandX();
        add(topPanel).fillX();
        row();
        attrTableCell = add();
    }

    private void init() {
        mtlTextButton = new VisTextButton("MTL");
        stage.unpressButton(mtlTextButton);
        mtlTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressMtl();
                return super.touchDown(event, x, y, pointer, button); // false
            }
        });

        envTextButton = new VisTextButton("ENV");
        stage.unpressButton(envTextButton);
        envTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressEnv();
                return super.touchDown(event, x, y, pointer, button); // false
            }
        });

        mtlSelectBox = new VisSelectBox<>();
        mtlSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetActors();
            }
        });
    }

    public void setDbgModelInstance(EditableModelInstance dbgModelInstance) {
        this.dbgModelInstance = dbgModelInstance;
        mtlSelectBox.getSelection().setProgrammaticChangeEvents(false);
        mtlSelectBox.clearItems();
        if (dbgModelInstance != null) { mtlSelectBox.setItems(dbgModelInstance.mtlIds); }
        mtlSelectBox.getSelection().setProgrammaticChangeEvents(true);

        resetActors();
    }

    public void unpressAllButtons() { stage.unpressButton(mtlTextButton); stage.unpressButton(envTextButton); }
    public boolean isAnyButtonPressed() { return stage.isPressed(mtlTextButton) || stage.isPressed(envTextButton); }

    public void pressEnv() {
        if (!stage.isPressed(envTextButton)) {
            unpressAllButtons();
            stage.pressButton(envTextButton);
            resetActors();
        }
    }

    public void pressMtl() {
        if (!stage.isPressed(mtlTextButton)) {
            unpressAllButtons();
            stage.pressButton(mtlTextButton);
            resetActors();
        }
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        if (!isAnyButtonPressed()) { pressEnv(); }

        if (stage.isPressed(envTextButton) && modelES.environment != null) {
            attrTableCell.setActor(stage.envAttrTable);

            stage.envLabel.setText("Environment:\n" + HGUtils.extractAttributes(modelES.environment,"", ""));

            stage.infoTCell.setActor(stage.envLabel);
        }

        if (stage.isPressed(mtlTextButton) && dbgModelInstance != null) {
            dbgModelInstance.createMtlAttributeTable(mtlSelectBox.getSelected(), stage.eventListener, modelES);
            attrTableCell.setActor(dbgModelInstance.mtlid2atable.get(mtlSelectBox.getSelected()));

            stage.miLabel.setText(HGUtils.getModelInstanceInfo(modelES.eng.currMI));

            stage.infoTCell.setActor(stage.miLabel);
            stage.infoBCell.setActor(stage.textureImage);
        } else if (stage.isPressed(mtlTextButton)) {
            pressEnv();
        }

        stage.editCell.setActor(this);
    }

    public void applyLocale() {

    }
}
