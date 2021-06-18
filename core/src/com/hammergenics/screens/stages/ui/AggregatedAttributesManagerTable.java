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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;
import com.hammergenics.utils.LibgdxUtils;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AggregatedAttributesManagerTable extends HGTable {
    public ModelEditScreen modelES;
    public DebugModelInstance dbgModelInstance;
    public ModelEditStage stage;

    public Cell<?> attrTableCell;
    
    public TextButton mtlTextButton;
    public TextButton envTextButton;
    public SelectBox<String> mtlSelectBox;

    public AggregatedAttributesManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.stage = stage;

        init();

        Table topPanel = new Table();
        topPanel.add(envTextButton).padRight(5f);
        topPanel.add(mtlTextButton).padRight(5f);
        topPanel.add(mtlSelectBox);
        topPanel.add().expandX();
        add(topPanel).fillX();
        row();
        attrTableCell = add();
    }

    private void init() {
        mtlTextButton = new TextButton("MTL", stage.skin);
        stage.unpressButton(mtlTextButton);
        mtlTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressMtl();
                return super.touchDown(event, x, y, pointer, button); // false
            }
        });

        envTextButton = new TextButton("ENV", stage.skin);
        stage.unpressButton(envTextButton);
        envTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressEnv();
                return super.touchDown(event, x, y, pointer, button); // false
            }
        });

        mtlSelectBox = new SelectBox<>(stage.skin);
        mtlSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetActors();
            }
        });
    }

    public void setDbgModelInstance(DebugModelInstance dbgModelInstance) {
        this.dbgModelInstance = dbgModelInstance;
        mtlSelectBox.getSelection().setProgrammaticChangeEvents(false);
        mtlSelectBox.clearItems();
        if (dbgModelInstance != null) {
            dbgModelInstance.createMtlAttributeTables(stage.skin, stage.eventListener, modelES);
            mtlSelectBox.setItems(dbgModelInstance.mtlid2atable.keys().toArray());
        }
        mtlSelectBox.getSelection().setProgrammaticChangeEvents(true);

        resetActors();
    }

    public void unpressAllButtons() { stage.unpressButton(mtlTextButton); stage.unpressButton(envTextButton); }
    public boolean isAnyButtonPressed() { return stage.isPressed(mtlTextButton) || stage.isPressed(envTextButton); }

    public void pressEnv() {
        if (!stage.isPressed(envTextButton)) {
            unpressAllButtons();
            stage.pressButton(envTextButton);

            stage.infoTCell.setActor(stage.envLabel);
        } else {
//            stage.unpressButton(envTextButton);
//
//            stage.infoTCell.clearActor();
//            stage.infoBCell.clearActor();
//            attrTableCell.clearActor();
        }
        resetActors();
    }

    public void pressMtl() {
        if (!stage.isPressed(mtlTextButton)) {
            unpressAllButtons();
            stage.pressButton(mtlTextButton);

            stage.infoTCell.setActor(stage.miLabel);
            stage.infoBCell.setActor(stage.textureImage);
        } else {
//            stage.unpressButton(mtlTextButton);
//
//            stage.infoTCell.clearActor();
//            stage.infoBCell.clearActor();
//            attrTableCell.clearActor();
        }
        resetActors();
    }

    public void resetActors() {
        //stage.infoTCell.clearActor();
        //stage.infoBCell.clearActor();
        //attrTableCell.clearActor();

        // **************************
        // **** ATTRIBUTES 2D UI ****
        // **************************
        if (modelES.environment != null && stage.isPressed(envTextButton)) {
            stage.envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelES.environment,"", ""));
            attrTableCell.setActor(stage.envAttrTable);
        }

        if (dbgModelInstance != null && stage.isPressed(mtlTextButton)) {
            stage.miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelES.eng.currMI));
            attrTableCell.setActor(dbgModelInstance.mtl2atable.getValueAt(mtlSelectBox.getSelectedIndex()));
        } else if (stage.isPressed(mtlTextButton)) {
            stage.miLabel.setText("");
            pressEnv();
        }
    }
}
