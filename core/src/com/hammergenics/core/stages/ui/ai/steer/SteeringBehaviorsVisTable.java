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

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.ai.steer.utils.TargetVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SteeringBehaviorsVisTable extends ContextAwareVisTable {
    public BooleanVisTable steeringEnabledVisTable;
    public VisSelectBox<SteeringBehaviorsVector3Enum> steeringBehaviorSB;

    public TargetVisTable targetVisTable;

    public SteeringBehaviorsVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        init();

        targetVisTable = new TargetVisTable(modelES, stage);

        add(steeringEnabledVisTable).padRight(5f).right();
        add(new VisLabel("Steering Behaviors: "));
        add(steeringBehaviorSB).expandX().fillX();
        row();

        add(targetVisTable).colspan(3).expandX().fillX();
        row();
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

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        update(0f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (dbgModelInstance != null) {
            steeringEnabledVisTable.setBoolean(dbgModelInstance.steeringEnabled);

            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(false);
            steeringBehaviorSB.clearItems();
            steeringBehaviorSB.setItems(SteeringBehaviorsVector3Enum.values());
            steeringBehaviorSB.setSelected(dbgModelInstance.currentSteeringBehavior);
            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(true);

            setCurrentTarget(getSecondaryModelInstance());
            targetVisTable.setTarget(getCurrentTarget());
        } else {
            steeringEnabledVisTable.setBoolean(false);

            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(false);
            steeringBehaviorSB.clearItems();
            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(true);

            targetVisTable.setTarget(null);
        }
    }

    public Location<Vector3> getCurrentTarget() {
        if (dbgModelInstance == null) { return null; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return null; }
        switch (sb) {
            case ARRIVE: return dbgModelInstance.arriveTarget;
            default: return null;
        }
    }

    public void setCurrentTarget(Location<Vector3> target) {
        if (dbgModelInstance == null) { return; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return; }
        switch (sb) {
            case ARRIVE: if (target != null) { dbgModelInstance.arriveTarget = target; } break;
            default: return;
        }
    }

    @Override
    public void applyLocale() { }
}