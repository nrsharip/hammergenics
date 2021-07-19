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

package com.hammergenics.core.stages.ui.ai;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.HGEngine;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;

public class SteeringBehaviorsVisTable extends VisTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;
    public HGEngine eng;

    public BooleanVisTable steeringEnabledVisTable;
    public VisSelectBox<SteeringBehaviorsVector3Enum> steeringBehaviorSB;

    public SteeringBehaviorsVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        init();

        add(steeringEnabledVisTable).right().pad(2f);
        add(new VisLabel("Steering Behaviors: "));
        add(steeringBehaviorSB).expandX().fillX().row();
    }

    public void init() {
        steeringEnabledVisTable = new BooleanVisTable(false, true, new VisLabel("enabled: ")) {
            @Override
            public void handleChanged(boolean value, ChangeListener.ChangeEvent event, Actor actor) {
                if (dbgModelInstance == null) { return; }

                dbgModelInstance.steeringEnabled = value;
                super.handleChanged(value, event, actor);
            }
        };

        steeringBehaviorSB = new VisSelectBox<>();
        steeringBehaviorSB.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (dbgModelInstance == null) { return; }

                dbgModelInstance.currentSteeringBehavior = steeringBehaviorSB.getSelected();
            }
        });
    }

    public void update(EditableModelInstance mi) {
        dbgModelInstance = mi;
        if (mi != null) {
            steeringEnabledVisTable.setBoolean(mi.steeringEnabled);

            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(false);
            steeringBehaviorSB.clearItems();
            steeringBehaviorSB.setItems(SteeringBehaviorsVector3Enum.values());
            steeringBehaviorSB.setSelected(mi.currentSteeringBehavior);
            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(true);
        } else {
            steeringEnabledVisTable.setBoolean(false);

            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(false);
            steeringBehaviorSB.clearItems();
            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(true);
        }
    }
}