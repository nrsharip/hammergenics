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


import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
    
    public TextButton mtlTextButton = null;
    public TextButton envTextButton = null;

    public AggregatedAttributesManagerTable(Skin skin, ModelEditScreen modelES, DebugModelInstance dbgModelInstance) {
        super(skin);
        this.modelES = modelES;
        this.stage = modelES.stage;
        this.dbgModelInstance = dbgModelInstance;

        init();

        Table topPanel = new Table();
        topPanel.add(envTextButton).fillX();
        topPanel.add(mtlTextButton).fillX();
        topPanel.add().expandX();

        add(topPanel);
        row();
        attrTableCell = add();
    }

    private void init() {
        dbgModelInstance.createMtlAttributeTables(stage.skin, stage.eventListener, modelES);

        mtlTextButton = new TextButton("MTL", stage.skin);
        mtlTextButton.getColor().set(stage.COLOR_UNPRESSED);
        mtlTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (mtlTextButton.getColor().equals(stage.COLOR_UNPRESSED)) {
                    // clearing all buttons first
                    mtlTextButton.getColor().set(stage.COLOR_UNPRESSED);
                    envTextButton.getColor().set(stage.COLOR_UNPRESSED);

                    // setting MTL specific actors
                    mtlTextButton.getColor().set(stage.COLOR_PRESSED);
                    stage.infoTCell.setActor(stage.miLabel);
                    stage.infoBCell.setActor(stage.textureImage);
                } else if (mtlTextButton.getColor().equals(stage.COLOR_PRESSED)) {
                    mtlTextButton.getColor().set(stage.COLOR_UNPRESSED);
                    stage.infoTCell.clearActor();
                    stage.infoBCell.clearActor();
                }
                reset();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        envTextButton = new TextButton("ENV", stage.skin);
        envTextButton.getColor().set(stage.COLOR_UNPRESSED);
        envTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (envTextButton.getColor().equals(stage.COLOR_UNPRESSED)) {
                    // clearing all buttons first
                    mtlTextButton.getColor().set(stage.COLOR_UNPRESSED);
                    envTextButton.getColor().set(stage.COLOR_UNPRESSED);

                    // setting ENV specific actors
                    envTextButton.getColor().set(stage.COLOR_PRESSED);
                    stage.infoTCell.setActor(stage.envLabel);
                } else if (envTextButton.getColor().equals(stage.COLOR_PRESSED)) {
                    envTextButton.getColor().set(stage.COLOR_UNPRESSED);
                    stage.infoTCell.clearActor();
                    stage.infoBCell.clearActor();
                }
                reset();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });
    }

    public void reset() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        attrTableCell.clearActor();
        // **************************
        // **** ATTRIBUTES 2D UI ****
        // **************************
        if (modelES.environment != null) {
            stage.envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelES.environment,"", ""));
            if (envTextButton.getColor().equals(stage.COLOR_PRESSED)) {
                attrTableCell.setActor(stage.envAttrTable);
            }
        }

        if (dbgModelInstance != null) {
            if (mtlTextButton.getColor().equals(stage.COLOR_PRESSED)) {
                attrTableCell.setActor(dbgModelInstance.mtl2atable.firstValue());
            }
        }
    }
}
