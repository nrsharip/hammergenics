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
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SteeringBehaviorsVisTable extends ContextAwareVisTable {
    public BooleanVisTable steeringEnabledVisTable;
    public VisSelectBox<SteeringBehaviorsVector3Enum> steeringBehaviorSB;

    public VisTable steeringParamsVisTable;
    public TargetVisTable targetVisTable;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
    public FloatVisTable arriveArrivalToleranceVisTable; // = 0.1f;
    public FloatVisTable arriveDecelerationRadiusVisTable; // = 1f;
    public FloatVisTable arriveTimeToTargetVisTable; // = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    public FloatVisTable evadeMaxPredictionTimeVisTable; // = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
    public FloatVisTable faceAlignToleranceVisTable; // = 0.1f;
    public FloatVisTable faceDecelerationRadiusVisTable; // = 1f;
    public FloatVisTable faceTimeToTargetVisTable; // = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
    public Vector3VisTable jumpGravityVisTable; // = new Vector3();
    public FloatVisTable jumpTakeoffPositionToleranceVisTable;
    public FloatVisTable jumpTakeoffVelocityToleranceVisTable;
    public FloatVisTable jumpMaxVerticalVelocityVisTable;
    public FloatVisTable jumpAirborneTimeVisTable; // = 0;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
    //protected Steerable<Vector3> matchVelocityTarget;
    protected FloatVisTable matchVelocityTimeToTargetVisTable;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    public FloatVisTable pursueMaxPredictionTimeVisTable; // = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
    public FloatVisTable reachOrientationAlignToleranceVisTable; // = 0.1f;
    public FloatVisTable reachOrientationDecelerationRadiusVisTable; // = 1f;
    public FloatVisTable reachOrientationTimeToTargetVisTable; // = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
    public FloatVisTable wanderLastTimeVisTable; // = 0f;
    public FloatVisTable wanderOffsetVisTable; // = 1f;
    public FloatVisTable wanderRadiusVisTable; // = 10f;
    public FloatVisTable wanderRateVisTable; // = 1f;
    public FloatVisTable wanderOrientationVisTable; // = 0f;
    public BooleanVisTable faceEnabledVisTable; // = true;

    public SteeringBehaviorsVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        init();

        add(steeringEnabledVisTable).padRight(5f).right();
        add(new VisLabel("Steering Behaviors: "));
        add(steeringBehaviorSB).expandX().fillX();
        row();

        add(steeringParamsVisTable).colspan(3).expandX().fillX();
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
                setSteeringParamsVisTable();
            }
        });

        targetVisTable = new TargetVisTable(modelES, stage);
        steeringParamsVisTable = new VisTable();

        // Steering Behaviors Parameters
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
        arriveArrivalToleranceVisTable = new FloatVisTable(true, new VisLabel("Arrival Tolerance: "));
        arriveDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        arriveTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
        evadeMaxPredictionTimeVisTable = new FloatVisTable(true, new VisLabel("Max Prediction Time: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
        faceAlignToleranceVisTable = new FloatVisTable(true, new VisLabel("Align Tolerance: "));
        faceDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        faceTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
        jumpGravityVisTable = new Vector3VisTable(false, true, true, new VisLabel("Gravity: "));
        jumpTakeoffPositionToleranceVisTable = new FloatVisTable(true, new VisLabel("Takeoff Position Tolerance: "));
        jumpTakeoffVelocityToleranceVisTable = new FloatVisTable(true, new VisLabel("Takeoff Velocity Tolerance: "));
        jumpMaxVerticalVelocityVisTable = new FloatVisTable(true, new VisLabel("Max Vertical Velocity: "));
        jumpAirborneTimeVisTable = new FloatVisTable(true, new VisLabel("Airborne Time: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
        //matchVelocityTarget;
        matchVelocityTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
        pursueMaxPredictionTimeVisTable = new FloatVisTable(true, new VisLabel("Max Prediction Time: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
        reachOrientationAlignToleranceVisTable = new FloatVisTable(true, new VisLabel("Align Tolerance: "));
        reachOrientationDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        reachOrientationTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
        wanderLastTimeVisTable = new FloatVisTable(true, new VisLabel("Last Time: "));
        wanderOffsetVisTable = new FloatVisTable(true, new VisLabel("Offset: "));
        wanderRadiusVisTable = new FloatVisTable(true, new VisLabel("Radius: "));
        wanderRateVisTable = new FloatVisTable(true, new VisLabel("Rate: "));
        wanderOrientationVisTable = new FloatVisTable(true, new VisLabel("Orientation: "));
        faceEnabledVisTable = new BooleanVisTable(false, true, new VisLabel("Face Enabled: "));
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        if (dbgModelInstance != null) {
            steeringEnabledVisTable.setBoolean(dbgModelInstance.steeringEnabled);

            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(false);
            steeringBehaviorSB.clearItems();
            steeringBehaviorSB.setItems(SteeringBehaviorsVector3Enum.values());
            steeringBehaviorSB.setSelected(dbgModelInstance.currentSteeringBehavior);
            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(true);

            setSteeringParamsVisTable();
        } else {
            steeringEnabledVisTable.setBoolean(false);

            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(false);
            steeringBehaviorSB.clearItems();
            steeringBehaviorSB.getSelection().setProgrammaticChangeEvents(true);

            steeringParamsVisTable.clearChildren();
            targetVisTable.setTarget(null);
            targetVisTable.targetOrientation.clearSetter();
            arriveArrivalToleranceVisTable.clearSetter();
            arriveDecelerationRadiusVisTable.clearSetter();
            arriveTimeToTargetVisTable.clearSetter();
        }

        update(0f);
    }

    public void setSteeringParamsVisTable() {
        steeringParamsVisTable.clearChildren();
        switch (dbgModelInstance.currentSteeringBehavior) {
            case ARRIVE:
                setCurrentTarget(getSecondaryModelInstance());
                //targetVisTable.targetOrientation.setSetter(getCurrentTarget()::setOrientation);
                arriveArrivalToleranceVisTable.setSetter(dbgModelInstance::setArriveArrivalTolerance);
                arriveDecelerationRadiusVisTable.setSetter(dbgModelInstance::setArriveDecelerationRadius);
                arriveTimeToTargetVisTable.setSetter(dbgModelInstance::setArriveTimeToTarget);

                targetVisTable.setTarget(getCurrentTarget());
                arriveArrivalToleranceVisTable.setFloat(dbgModelInstance.arriveArrivalTolerance);
                arriveDecelerationRadiusVisTable.setFloat(dbgModelInstance.arriveDecelerationRadius);
                arriveTimeToTargetVisTable.setFloat(dbgModelInstance.arriveTimeToTarget);

                steeringParamsVisTable.add(targetVisTable).colspan(2).row();
                steeringParamsVisTable.add(arriveArrivalToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(arriveArrivalToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(arriveDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(arriveDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(arriveTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(arriveTimeToTargetVisTable.valueT).left().row();
                break;
            case WANDER:
                break;
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (dbgModelInstance != null) {
            targetVisTable.update(delta);
            arriveArrivalToleranceVisTable.setFloat(dbgModelInstance.arriveArrivalTolerance);
            arriveDecelerationRadiusVisTable.setFloat(dbgModelInstance.arriveDecelerationRadius);
            arriveTimeToTargetVisTable.setFloat(dbgModelInstance.arriveTimeToTarget);
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