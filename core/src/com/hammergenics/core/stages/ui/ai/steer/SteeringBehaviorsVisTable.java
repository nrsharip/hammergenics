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

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.ai.utils.RadiusProximityVisTable;
import com.hammergenics.core.stages.ui.ai.utils.Location3DVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.IntVisTable;
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
    public Location3DVisTable targetAVisTable;
    public Location3DVisTable targetBVisTable;
    public RadiusProximityVisTable radiusProximityVisTable;

    // I. INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
    // I.1. Arrive: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
    public FloatVisTable arriveArrivalToleranceVisTable;
    public FloatVisTable arriveDecelerationRadiusVisTable;
    public FloatVisTable arriveTimeToTargetVisTable;
    // I.2. Evade: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    public FloatVisTable evadeMaxPredictionTimeVisTable;
    // I.3. Face: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
    public FloatVisTable faceAlignToleranceVisTable;
    public FloatVisTable faceDecelerationRadiusVisTable;
    public FloatVisTable faceTimeToTargetVisTable;
    // I.4. Flee: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    // I.5. Follow Flow Field: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
    public FloatVisTable followFlowFieldPredictionTimeVisTable;
    // I.6. Follow Path: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
    // I.6.1 Arrive part
    public FloatVisTable followPathArrivalToleranceVisTable;
    public FloatVisTable followPathDecelerationRadiusVisTable;
    public FloatVisTable followPathTimeToTargetVisTable;
    // I.6.2 FollowPath part
    public FloatVisTable followPathOffsetVisTable;
    public IntVisTable followPathParamSegmentIndexVisTable;
    public FloatVisTable followPathParamDistanceVisTable;
    public BooleanVisTable followPathArriveEnabledVisTable;
    public FloatVisTable followPathPredictionTimeVisTable;
    // I.6.3 FollowPath debug
    public Vector3VisTable followPathInternalTargetPositionVisTable;
    // I.7. Interpose: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
    // I.7.1 Arrive part
    public FloatVisTable interposeArrivalToleranceVisTable;
    public FloatVisTable interposeDecelerationRadiusVisTable;
    public FloatVisTable interposeTimeToTargetVisTable;
    // I.7.2 Interpose part
    public FloatVisTable interpositionRatioVisTable;
    // I.7.3 Interpose debug
    public Vector3VisTable interposeInternalTargetPositionVisTable;
    // I.8. Jump: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
    public Vector3VisTable jumpGravityVisTable;
    public FloatVisTable jumpTakeoffPositionToleranceVisTable;
    public FloatVisTable jumpTakeoffVelocityToleranceVisTable;
    public FloatVisTable jumpTakeoffToleranceVisTable;
    public FloatVisTable jumpMaxVerticalVelocityVisTable;
    // callback values
    public BooleanVisTable jumpCallbackAchievableVisTable;
    // HGJump debug
    public FloatVisTable jumpAirborneTimeVisTable;
    // I.9. Look Where You Are Going: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
    public FloatVisTable lwyagAlignToleranceVisTable;
    public FloatVisTable lwyagDecelerationRadiusVisTable;
    public FloatVisTable lwyagTimeToTargetVisTable;
    // I.10. Match Velocity: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
    protected FloatVisTable matchVelocityTimeToTargetVisTable;
    // I.11. Pursue: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    public FloatVisTable pursueMaxPredictionTimeVisTable;
    // I.12. Reach Orientation: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
    public FloatVisTable reachOrientationAlignToleranceVisTable;
    public FloatVisTable reachOrientationDecelerationRadiusVisTable;
    public FloatVisTable reachOrientationTimeToTargetVisTable;
    // I.13. Seek: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    // I.14. Wander: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
    public FloatVisTable wanderLastTimeVisTable;
    public FloatVisTable wanderOffsetVisTable;
    public FloatVisTable wanderRadiusVisTable;
    public FloatVisTable wanderRateVisTable;
    public FloatVisTable wanderOrientationVisTable;
    public BooleanVisTable wanderFaceEnabledVisTable;
    // I.14.1 Wander debug
    public Vector3VisTable wanderInternalTargetPositionVisTable;
    public Vector3VisTable wanderCenterVisTable;

    // II. GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
    // II.1 Alignment: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
    // II.2 Cohesion: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
    // II.3 Collision Avoidance: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
    // II.4 Hide: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
    public FloatVisTable hideDistanceFromBoundary;
    public FloatVisTable hideArrivalTolerance;
    public FloatVisTable hideDecelerationRadius;
    public FloatVisTable hideTimeToTarget;
    // II.5 Raycast Obstacle Avoidance: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
    //      RayConfiguration implementations:
    //      * CentralRayWithWhiskersConfiguration
    //      * ParallelSideRayConfiguration
    //      * SingleRayConfiguration
    public FloatVisTable roaSingleRayConfigurationLength;
    public FloatVisTable roaDistanceFromBoundary;
    // II.6 Separation: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
    public FloatVisTable separationDecayCoefficient;

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
                //dbgModelInstance.steeringEnabled = value;
                dbgModelInstances.forEach(mi -> mi.steeringEnabled = value);
                super.handleChanged(value, event, actor);
            }
        };

        steeringBehaviorSB = new VisSelectBox<>();
        steeringBehaviorSB.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (dbgModelInstance == null) { return; }
                //dbgModelInstance.currentSteeringBehavior = steeringBehaviorSB.getSelected();
                dbgModelInstances.forEach(mi -> mi.currentSteeringBehavior = steeringBehaviorSB.getSelected());
                setSteeringParamsVisTable();
            }
        });

        targetAVisTable = new Location3DVisTable(new VisLabel("Target A"), modelES, stage);
        targetBVisTable = new Location3DVisTable(new VisLabel("Target B"), modelES, stage);
        radiusProximityVisTable = new RadiusProximityVisTable(new VisLabel("Proximity"), new VisLabel("Agents: "), modelES, stage);
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
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
        followFlowFieldPredictionTimeVisTable = new FloatVisTable(true, new VisLabel("Prediction Time: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
        // Arrive part
        followPathArrivalToleranceVisTable = new FloatVisTable(true, new VisLabel("Arrival Tolerance: "));
        followPathDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        followPathTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // FollowPath part
        followPathOffsetVisTable = new FloatVisTable(true, new VisLabel("Offset: "));
        followPathParamSegmentIndexVisTable = new IntVisTable(true, new VisLabel("Segment Index: "));;
        followPathParamDistanceVisTable = new FloatVisTable(true, new VisLabel("Distance: "));
        followPathArriveEnabledVisTable = new BooleanVisTable(false, true, new VisLabel("Arrive Enabled: "));
        followPathPredictionTimeVisTable = new FloatVisTable(true, new VisLabel("Prediction Time: "));
        // FollowPath debug
        followPathInternalTargetPositionVisTable = new Vector3VisTable(false, true, true, new VisLabel("Internal Target Position: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
        interposeArrivalToleranceVisTable = new FloatVisTable(true, new VisLabel("Arrival Tolerance: "));
        interposeDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        interposeTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));

        interpositionRatioVisTable = new FloatVisTable(true, new VisLabel("Interposition Ratio: "));
        // Interpose debug
        interposeInternalTargetPositionVisTable = new Vector3VisTable(false, true, true, new VisLabel("Internal Target Position: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
        jumpGravityVisTable = new Vector3VisTable(false, true, true, new VisLabel("Gravity: "));
        jumpTakeoffPositionToleranceVisTable = new FloatVisTable(true, new VisLabel("Takeoff Position Tolerance: "));
        jumpTakeoffVelocityToleranceVisTable = new FloatVisTable(true, new VisLabel("Takeoff Velocity Tolerance: "));
        jumpTakeoffToleranceVisTable = new FloatVisTable(true, new VisLabel("Takeoff Tolerance: "));
        jumpMaxVerticalVelocityVisTable = new FloatVisTable(true, new VisLabel("Max Vertical Velocity: "));
        jumpCallbackAchievableVisTable = new BooleanVisTable(false, true, new VisLabel("Jump Achievable: "));
        jumpAirborneTimeVisTable = new FloatVisTable(true, new VisLabel("Airborne Time: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
        lwyagAlignToleranceVisTable = new FloatVisTable(true, new VisLabel("Align Tolerance: "));
        lwyagDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        lwyagTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
        matchVelocityTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
        pursueMaxPredictionTimeVisTable = new FloatVisTable(true, new VisLabel("Max Prediction Time: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
        reachOrientationAlignToleranceVisTable = new FloatVisTable(true, new VisLabel("Align Tolerance: "));
        reachOrientationDecelerationRadiusVisTable = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        reachOrientationTimeToTargetVisTable = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
        wanderLastTimeVisTable = new FloatVisTable(true, new VisLabel("Last Time: "));
        wanderOffsetVisTable = new FloatVisTable(true, new VisLabel("Offset: "));
        wanderRadiusVisTable = new FloatVisTable(true, new VisLabel("Radius: "));
        wanderRateVisTable = new FloatVisTable(true, new VisLabel("Rate: "));
        wanderOrientationVisTable = new FloatVisTable(true, new VisLabel("Orientation: "));
        wanderFaceEnabledVisTable = new BooleanVisTable(false, true, new VisLabel("Face Enabled: "));
        // Wander debug
        wanderInternalTargetPositionVisTable = new Vector3VisTable(false, true, true, new VisLabel("Internal Target Position: "));
        wanderCenterVisTable = new Vector3VisTable(false, true, true, new VisLabel("Wander Center: "));

        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
        hideDistanceFromBoundary = new FloatVisTable(true, new VisLabel("Distance From Boundary: "));
        hideArrivalTolerance = new FloatVisTable(true, new VisLabel("Arrival Tolerance: "));
        hideDecelerationRadius = new FloatVisTable(true, new VisLabel("Deceleration Radius: "));
        hideTimeToTarget = new FloatVisTable(true, new VisLabel("Time To Target: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
        roaSingleRayConfigurationLength = new FloatVisTable(true, new VisLabel("Single Ray Length: "));
        roaDistanceFromBoundary = new FloatVisTable(true, new VisLabel("Distance From Boundary: "));
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
        separationDecayCoefficient = new FloatVisTable(true, new VisLabel("Decay Coefficient: "));
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

            clearSteeringParamsVisTable();
        }
        update(0f);
    }

    public void setSteeringParamsVisTable() {
        steeringParamsVisTable.clearChildren();

        setCurrentTargetA(getSecondaryModelInstance());
        setCurrentTargetB(getModelInstanceN(2));
        setCurrentAgents(getNonPrimaryModelInstances());

        targetAVisTable.setLocation(null);
        targetBVisTable.setLocation(null);
        radiusProximityVisTable.setProximity(null);

        EditableModelInstance mi = dbgModelInstance;
        switch (dbgModelInstance.currentSteeringBehavior) {
            // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
            case ARRIVE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
                targetAVisTable.setLocation(getCurrentTargetA());
                arriveArrivalToleranceVisTable.setFloat(mi.arriveArrivalTolerance).setSetter(mi::setArriveArrivalTolerance);
                arriveDecelerationRadiusVisTable.setFloat(mi.arriveDecelerationRadius).setSetter(mi::setArriveDecelerationRadius);
                arriveTimeToTargetVisTable.setFloat(mi.arriveTimeToTarget).setSetter(mi::setArriveTimeToTarget);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(arriveArrivalToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(arriveArrivalToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(arriveDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(arriveDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(arriveTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(arriveTimeToTargetVisTable.valueT).left().row();
                break;
            case EVADE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
                targetAVisTable.setLocation(getCurrentTargetA());
                evadeMaxPredictionTimeVisTable.setFloat(mi.evadeMaxPredictionTime).setSetter(mi::setEvadeMaxPredictionTime);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(evadeMaxPredictionTimeVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(evadeMaxPredictionTimeVisTable.valueT).left().row();
                break;
            case FACE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
                targetAVisTable.setLocation(getCurrentTargetA());

                faceAlignToleranceVisTable.setFloat(mi.faceAlignTolerance).setSetter(mi::setFaceAlignTolerance);
                faceDecelerationRadiusVisTable.setFloat(mi.faceDecelerationRadius).setSetter(mi::setFaceDecelerationRadius);
                faceTimeToTargetVisTable.setFloat(mi.faceTimeToTarget).setSetter(mi::setFaceTimeToTarget);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(faceAlignToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(faceAlignToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(faceDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(faceDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(faceTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(faceTimeToTargetVisTable.valueT).left().row();
                break;
            case FLEE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
                targetAVisTable.setLocation(getCurrentTargetA());

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                break;
            case FOLLOW_FLOW_FIELD: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
                followFlowFieldPredictionTimeVisTable.setFloat(mi.followFlowFieldPredictionTime).setSetter(mi::setFollowFlowFieldPredictionTime);

                steeringParamsVisTable.add(followFlowFieldPredictionTimeVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followFlowFieldPredictionTimeVisTable.valueT).left().row();
                break;
            case FOLLOW_PATH: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
                followPathArrivalToleranceVisTable.setFloat(mi.followPathArrivalTolerance).setSetter(mi::setFollowPathArrivalTolerance);
                followPathDecelerationRadiusVisTable.setFloat(mi.followPathDecelerationRadius).setSetter(mi::setFollowPathDecelerationRadius);
                followPathTimeToTargetVisTable.setFloat(mi.followPathTimeToTarget).setSetter(mi::setFollowPathTimeToTarget);

                followPathOffsetVisTable.setFloat(mi.followPathOffset).setSetter(mi::setFollowPathOffset);
                followPathParamSegmentIndexVisTable.setInt(mi.followPathParamSegmentIndex).setSetter(mi::setFollowPathParamSegmentIndex);
                followPathParamDistanceVisTable.setFloat(mi.followPathParamDistance).setSetter(mi::setFollowPathParamDistance);
                followPathPredictionTimeVisTable.setFloat(mi.followPathPredictionTime).setSetter(mi::setFollowPathPredictionTime);
                followPathArriveEnabledVisTable.setBoolean(mi.followPathArriveEnabled).setSetter(mi::setFollowPathArriveEnabled);
                // FollowPath debug
                followPathInternalTargetPositionVisTable.setVector3(mi.followPathInternalTargetPosition);

                steeringParamsVisTable.add(followPathArrivalToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathArrivalToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(followPathDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(followPathTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathTimeToTargetVisTable.valueT).left().row();

                steeringParamsVisTable.add(followPathOffsetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathOffsetVisTable.valueT).left().row();
                steeringParamsVisTable.add(followPathPredictionTimeVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathPredictionTimeVisTable.valueT).left().row();
                steeringParamsVisTable.add(followPathArriveEnabledVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathArriveEnabledVisTable.valueT).left().row();

                steeringParamsVisTable.add().padRight(5f).right();
                steeringParamsVisTable.add(followPathInternalTargetPositionVisTable.labelsT).expandX().fillX().row();
                steeringParamsVisTable.add(followPathInternalTargetPositionVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathInternalTargetPositionVisTable.valueT).left().row();
                steeringParamsVisTable.add(followPathParamSegmentIndexVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathParamSegmentIndexVisTable.valueT).left().row();
                steeringParamsVisTable.add(followPathParamDistanceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(followPathParamDistanceVisTable.valueT).left().row();
                break;
            case INTERPOSE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
                targetAVisTable.setLocation(getCurrentTargetA());
                targetBVisTable.setLocation(getCurrentTargetB());

                interposeArrivalToleranceVisTable.setFloat(mi.interposeArrivalTolerance).setSetter(mi::setInterposeArrivalTolerance);
                interposeDecelerationRadiusVisTable.setFloat(mi.interposeDecelerationRadius).setSetter(mi::setInterposeDecelerationRadius);
                interposeTimeToTargetVisTable.setFloat(mi.interposeTimeToTarget).setSetter(mi::setInterposeTimeToTarget);

                interpositionRatioVisTable.setFloat(mi.interpositionRatio).setSetter(mi::setInterpositionRatio);
                // Interpose debug
                interposeInternalTargetPositionVisTable.setVector3(mi.interposeInternalTargetPosition);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(targetBVisTable).colspan(2).row();
                steeringParamsVisTable.add().padRight(5f).right();

                steeringParamsVisTable.add(interposeInternalTargetPositionVisTable.labelsT).expandX().fillX().row();
                steeringParamsVisTable.add(interposeInternalTargetPositionVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(interposeInternalTargetPositionVisTable.valueT).left().row();
                steeringParamsVisTable.add(interposeArrivalToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(interposeArrivalToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(interposeDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(interposeDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(interposeTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(interposeTimeToTargetVisTable.valueT).left().row();
                steeringParamsVisTable.add(interpositionRatioVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(interpositionRatioVisTable.valueT).left().row();
                break;
            case JUMP: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
                jumpGravityVisTable.setVector3(mi.jumpGravity);
                jumpTakeoffPositionToleranceVisTable.setFloat(mi.jumpTakeoffPositionTolerance).setSetter(mi::setJumpTakeoffPositionTolerance);
                jumpTakeoffVelocityToleranceVisTable.setFloat(mi.jumpTakeoffVelocityTolerance).setSetter(mi::setJumpTakeoffVelocityTolerance);
                jumpTakeoffToleranceVisTable.setFloat(mi.jumpTakeoffTolerance).setSetter(mi::setJumpTakeoffTolerance);
                jumpMaxVerticalVelocityVisTable.setFloat(mi.jumpMaxVerticalVelocity).setSetter(mi::setJumpMaxVerticalVelocity);
                jumpCallbackAchievableVisTable.setBoolean(mi.jumpCallbackAchievable);
                jumpAirborneTimeVisTable.setFloat(mi.jumpAirborneTime).setSetter(mi::setJumpAirborneTime);

                steeringParamsVisTable.add().padRight(5f).right();
                steeringParamsVisTable.add(jumpGravityVisTable.labelsT).expandX().fillX().row();
                steeringParamsVisTable.add(jumpGravityVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpGravityVisTable.valueT).left().row();

                steeringParamsVisTable.add(jumpTakeoffPositionToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpTakeoffPositionToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(jumpTakeoffVelocityToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpTakeoffVelocityToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(jumpTakeoffToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpTakeoffToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(jumpMaxVerticalVelocityVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpMaxVerticalVelocityVisTable.valueT).left().row();
                steeringParamsVisTable.add(jumpCallbackAchievableVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpCallbackAchievableVisTable.valueT).left().row();
                steeringParamsVisTable.add(jumpAirborneTimeVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(jumpAirborneTimeVisTable.valueT).left().row();
                break;
            case LOOK_WHERE_YOU_ARE_GOING: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
                targetAVisTable.setLocation(getCurrentTargetA());

                lwyagAlignToleranceVisTable.setFloat(mi.lwyagAlignTolerance).setSetter(mi::setLwyagAlignTolerance);
                lwyagDecelerationRadiusVisTable.setFloat(mi.lwyagDecelerationRadius).setSetter(mi::setLwyagDecelerationRadius);
                lwyagTimeToTargetVisTable.setFloat(mi.lwyagTimeToTarget).setSetter(mi::setLwyagTimeToTarget);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(lwyagAlignToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(lwyagAlignToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(lwyagDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(lwyagDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(lwyagTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(lwyagTimeToTargetVisTable.valueT).left().row();
                break;
            case MATCH_VELOCITY: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
                targetAVisTable.setLocation(getCurrentTargetA());

                matchVelocityTimeToTargetVisTable.setFloat(mi.matchVelocityTimeToTarget).setSetter(mi::setMatchVelocityTimeToTarget);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(matchVelocityTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(matchVelocityTimeToTargetVisTable.valueT).left().row();
                break;
            case PURSUE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
                targetAVisTable.setLocation(getCurrentTargetA());
                pursueMaxPredictionTimeVisTable.setFloat(mi.pursueMaxPredictionTime).setSetter(mi::setPursueMaxPredictionTime);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(pursueMaxPredictionTimeVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(pursueMaxPredictionTimeVisTable.valueT).left().row();
                break;
            case REACH_ORIENTATION: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
                targetAVisTable.setLocation(getCurrentTargetA());

                reachOrientationAlignToleranceVisTable.setFloat(mi.reachOrientationAlignTolerance).setSetter(mi::setReachOrientationAlignTolerance);
                reachOrientationDecelerationRadiusVisTable.setFloat(mi.reachOrientationAlignTolerance).setSetter(mi::setReachOrientationAlignTolerance);
                reachOrientationTimeToTargetVisTable.setFloat(mi.reachOrientationAlignTolerance).setSetter(mi::setReachOrientationAlignTolerance);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(reachOrientationAlignToleranceVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(reachOrientationAlignToleranceVisTable.valueT).left().row();
                steeringParamsVisTable.add(reachOrientationDecelerationRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(reachOrientationDecelerationRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(reachOrientationTimeToTargetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(reachOrientationTimeToTargetVisTable.valueT).left().row();
                break;
            case SEEK: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
                targetAVisTable.setLocation(getCurrentTargetA());

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                break;
            case WANDER: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
                wanderLastTimeVisTable.setFloat(mi.wanderLastTime).setSetter(mi::setWanderLastTime);
                wanderOffsetVisTable.setFloat(mi.wanderOffset).setSetter(mi::setWanderOffset);
                wanderRadiusVisTable.setFloat(mi.wanderRadius).setSetter(mi::setWanderRadius);
                wanderRateVisTable.setFloat(mi.wanderRate).setSetter(mi::setWanderRate);
                wanderOrientationVisTable.setFloat(mi.wanderOrientation).setSetter(mi::setWanderOrientation);
                wanderFaceEnabledVisTable.setBoolean(mi.wanderFaceEnabled).setSetter(mi::setWanderFaceEnabled);

                wanderInternalTargetPositionVisTable.setVector3(mi.wanderInternalTargetPosition);
                wanderCenterVisTable.setVector3(mi.wanderCenter);

                steeringParamsVisTable.add(wanderLastTimeVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderLastTimeVisTable.valueT).left().row();
                steeringParamsVisTable.add(wanderOffsetVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderOffsetVisTable.valueT).left().row();
                steeringParamsVisTable.add(wanderRadiusVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderRadiusVisTable.valueT).left().row();
                steeringParamsVisTable.add(wanderRateVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderRateVisTable.valueT).left().row();
                steeringParamsVisTable.add(wanderOrientationVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderOrientationVisTable.valueT).left().row();
                steeringParamsVisTable.add(wanderFaceEnabledVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderFaceEnabledVisTable.valueT).left().row();

                steeringParamsVisTable.add().padRight(5f).right();
                steeringParamsVisTable.add(wanderInternalTargetPositionVisTable.labelsT).expandX().fillX().row();
                steeringParamsVisTable.add(wanderInternalTargetPositionVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderInternalTargetPositionVisTable.valueT).left().row();
                steeringParamsVisTable.add(wanderCenterVisTable.titleL).padRight(5f).right();
                steeringParamsVisTable.add(wanderCenterVisTable.valueT).left().row();
                break;

            // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
            case ALIGNMENT:
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
            case COHESION:
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
            case COLLISION_AVOIDANCE:
                radiusProximityVisTable.setProximity((RadiusProximity<Vector3>)getCurrentProximity());

                steeringParamsVisTable.add(radiusProximityVisTable).colspan(2).row();
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
            case HIDE:
                if (getCurrentTargetA() instanceof Steerable) {
                    mi.hideAgents.removeValue((Steerable<Vector3>) getCurrentTargetA(), true);
                }
                targetAVisTable.setLocation(getCurrentTargetA());

                radiusProximityVisTable.setProximity((RadiusProximity<Vector3>)getCurrentProximity());

                hideDistanceFromBoundary.setFloat(mi.hideDistanceFromBoundary).setSetter(mi::setHideDistanceFromBoundary);
                hideArrivalTolerance.setFloat(mi.hideArrivalTolerance).setSetter(mi::setHideArrivalTolerance);
                hideDecelerationRadius.setFloat(mi.hideDecelerationRadius).setSetter(mi::setHideDecelerationRadius);
                hideTimeToTarget.setFloat(mi.hideTimeToTarget).setSetter(mi::setHideTimeToTarget);

                steeringParamsVisTable.add(targetAVisTable).colspan(2).row();
                steeringParamsVisTable.add(radiusProximityVisTable).colspan(2).row();
                steeringParamsVisTable.add(hideDistanceFromBoundary.titleL).padRight(5f).right();
                steeringParamsVisTable.add(hideDistanceFromBoundary.valueT).left().row();
                steeringParamsVisTable.add(hideArrivalTolerance.titleL).padRight(5f).right();
                steeringParamsVisTable.add(hideArrivalTolerance.valueT).left().row();
                steeringParamsVisTable.add(hideDecelerationRadius.titleL).padRight(5f).right();
                steeringParamsVisTable.add(hideDecelerationRadius.valueT).left().row();
                steeringParamsVisTable.add(hideTimeToTarget.titleL).padRight(5f).right();
                steeringParamsVisTable.add(hideTimeToTarget.valueT).left().row();
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
            case RAY_CAST_OBSTACLE_AVOIDANCE:
                roaSingleRayConfigurationLength.setFloat(mi.roaSingleRayConfigurationLength).setSetter(mi::setRoaSingleRayConfigurationLength);
                roaDistanceFromBoundary.setFloat(mi.roaDistanceFromBoundary).setSetter(mi::setRoaDistanceFromBoundary);

                steeringParamsVisTable.add(roaSingleRayConfigurationLength.titleL).padRight(5f).right();
                steeringParamsVisTable.add(roaSingleRayConfigurationLength.valueT).left().row();
                steeringParamsVisTable.add(roaDistanceFromBoundary.titleL).padRight(5f).right();
                steeringParamsVisTable.add(roaDistanceFromBoundary.valueT).left().row();
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
            case SEPARATION:
                radiusProximityVisTable.setProximity((RadiusProximity<Vector3>)getCurrentProximity());
                separationDecayCoefficient.setFloat(mi.separationDecayCoefficient).setSetter(mi::setSeparationDecayCoefficient);

                steeringParamsVisTable.add(radiusProximityVisTable).colspan(2).row();
                steeringParamsVisTable.add(separationDecayCoefficient.titleL).padRight(5f).right();
                steeringParamsVisTable.add(separationDecayCoefficient.valueT).left().row();
                break;
        }
    }

    public void clearSteeringParamsVisTable() {
        steeringParamsVisTable.clearChildren();
        targetAVisTable.setLocation(null);
        targetBVisTable.setLocation(null);
        radiusProximityVisTable.setProximity(null);

        // I. INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
        arriveArrivalToleranceVisTable.setFloat(0f).clearSetter();
        arriveDecelerationRadiusVisTable.setFloat(0f).clearSetter();
        arriveTimeToTargetVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
        evadeMaxPredictionTimeVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
        faceAlignToleranceVisTable.setFloat(0f).clearSetter();
        faceDecelerationRadiusVisTable.setFloat(0f).clearSetter();
        faceTimeToTargetVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
        followFlowFieldPredictionTimeVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
        followPathArrivalToleranceVisTable.setFloat(0f).clearSetter();
        followPathDecelerationRadiusVisTable.setFloat(0f).clearSetter();
        followPathTimeToTargetVisTable.setFloat(0f).clearSetter();

        followPathOffsetVisTable.setFloat(0f).clearSetter();
        followPathParamSegmentIndexVisTable.setInt(0).clearSetter();
        followPathParamDistanceVisTable.setFloat(0f).clearSetter();
        followPathArriveEnabledVisTable.setBoolean(false).clearSetter();
        followPathPredictionTimeVisTable.setFloat(0f).clearSetter();
        // debug
        followPathInternalTargetPositionVisTable.setVector3(null);
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
        interposeArrivalToleranceVisTable.setFloat(0f).clearSetter();
        interposeDecelerationRadiusVisTable.setFloat(0f).clearSetter();
        interposeTimeToTargetVisTable.setFloat(0f).clearSetter();

        interpositionRatioVisTable.setFloat(0f).clearSetter();
        // debug
        interposeInternalTargetPositionVisTable.setVector3(null);
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
        jumpGravityVisTable.setVector3(null);
        jumpTakeoffPositionToleranceVisTable.setFloat(0f).clearSetter();
        jumpTakeoffVelocityToleranceVisTable.setFloat(0f).clearSetter();
        jumpTakeoffToleranceVisTable.setFloat(0f).clearSetter();
        jumpMaxVerticalVelocityVisTable.setFloat(0f).clearSetter();
        jumpCallbackAchievableVisTable.setBoolean(false);
        // debug
        jumpAirborneTimeVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
        lwyagAlignToleranceVisTable.setFloat(0f).clearSetter();
        lwyagDecelerationRadiusVisTable.setFloat(0f).clearSetter();
        lwyagTimeToTargetVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
        matchVelocityTimeToTargetVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
        pursueMaxPredictionTimeVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
        reachOrientationAlignToleranceVisTable.setFloat(0f).clearSetter();
        reachOrientationDecelerationRadiusVisTable.setFloat(0f).clearSetter();
        reachOrientationTimeToTargetVisTable.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
        wanderLastTimeVisTable.setFloat(0f).clearSetter();
        wanderOffsetVisTable.setFloat(0f).clearSetter();
        wanderRadiusVisTable.setFloat(0f).clearSetter();
        wanderRateVisTable.setFloat(0f).clearSetter();
        wanderOrientationVisTable.setFloat(0f).clearSetter();
        wanderFaceEnabledVisTable.setBoolean(false).clearSetter();
        // debug
        wanderInternalTargetPositionVisTable.setVector3(null);
        wanderCenterVisTable.setVector3(null);

        // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
        hideDistanceFromBoundary.setFloat(0f).clearSetter();
        hideArrivalTolerance.setFloat(0f).clearSetter();
        hideDecelerationRadius.setFloat(0f).clearSetter();
        hideTimeToTarget.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
        roaSingleRayConfigurationLength.setFloat(0f).clearSetter();
        roaDistanceFromBoundary.setFloat(0f).clearSetter();
        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
        separationDecayCoefficient.setFloat(0f).clearSetter();
    }

    public void updateSteeringParamsVisTable(float delta) {
        if (dbgModelInstance == null) { return; }
        EditableModelInstance mi = dbgModelInstance;

        targetAVisTable.update(delta);
        targetBVisTable.update(delta);
        radiusProximityVisTable.update(delta);
        switch (dbgModelInstance.currentSteeringBehavior) {
            // I. INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
            case ARRIVE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
                arriveArrivalToleranceVisTable.setFloat(dbgModelInstance.arriveArrivalTolerance);
                arriveDecelerationRadiusVisTable.setFloat(dbgModelInstance.arriveDecelerationRadius);
                arriveTimeToTargetVisTable.setFloat(dbgModelInstance.arriveTimeToTarget);
                break;
            case EVADE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
                evadeMaxPredictionTimeVisTable.setFloat(dbgModelInstance.evadeMaxPredictionTime);
                break;
            case FACE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
                faceAlignToleranceVisTable.setFloat(mi.faceAlignTolerance);
                faceDecelerationRadiusVisTable.setFloat(mi.faceDecelerationRadius);
                faceTimeToTargetVisTable.setFloat(mi.faceTimeToTarget);
                break;
            case FLEE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
                break;
            case FOLLOW_FLOW_FIELD: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
                followFlowFieldPredictionTimeVisTable.setFloat(mi.followFlowFieldPredictionTime);
                break;
            case FOLLOW_PATH: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
                followPathArrivalToleranceVisTable.setFloat(mi.followPathArrivalTolerance);
                followPathDecelerationRadiusVisTable.setFloat(mi.followPathDecelerationRadius);
                followPathTimeToTargetVisTable.setFloat(mi.followPathTimeToTarget);

                followPathOffsetVisTable.setFloat(mi.followPathOffset);
                followPathParamSegmentIndexVisTable.setInt(mi.followPathParamSegmentIndex);
                followPathParamDistanceVisTable.setFloat(mi.followPathParamDistance);
                followPathPredictionTimeVisTable.setFloat(mi.followPathPredictionTime);
                followPathArriveEnabledVisTable.setBoolean(mi.followPathArriveEnabled);
                // FollowPath debug
                followPathInternalTargetPositionVisTable.update();
                break;
            case INTERPOSE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
                interposeArrivalToleranceVisTable.setFloat(mi.interposeArrivalTolerance);
                interposeDecelerationRadiusVisTable.setFloat(mi.interposeDecelerationRadius);
                interposeTimeToTargetVisTable.setFloat(mi.interposeTimeToTarget);

                interpositionRatioVisTable.setFloat(mi.interpositionRatio);
                // Interpose debug
                interposeInternalTargetPositionVisTable.update();
                break;
            case JUMP: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
                jumpGravityVisTable.update();
                jumpTakeoffPositionToleranceVisTable.setFloat(mi.jumpTakeoffPositionTolerance);
                jumpTakeoffVelocityToleranceVisTable.setFloat(mi.jumpTakeoffVelocityTolerance);
                jumpTakeoffToleranceVisTable.setFloat(mi.jumpTakeoffTolerance);
                jumpMaxVerticalVelocityVisTable.setFloat(mi.jumpMaxVerticalVelocity);
                jumpCallbackAchievableVisTable.setBoolean(mi.jumpCallbackAchievable);
                jumpAirborneTimeVisTable.setFloat(mi.jumpAirborneTime);
                break;
            case LOOK_WHERE_YOU_ARE_GOING: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
                lwyagAlignToleranceVisTable.setFloat(mi.lwyagAlignTolerance);
                lwyagDecelerationRadiusVisTable.setFloat(mi.lwyagDecelerationRadius);
                lwyagTimeToTargetVisTable.setFloat(mi.lwyagTimeToTarget);
                break;
            case MATCH_VELOCITY: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
                matchVelocityTimeToTargetVisTable.setFloat(mi.matchVelocityTimeToTarget);
                break;
            case PURSUE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
                pursueMaxPredictionTimeVisTable.setFloat(dbgModelInstance.pursueMaxPredictionTime);
                break;
            case REACH_ORIENTATION: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
                reachOrientationAlignToleranceVisTable.setFloat(mi.reachOrientationAlignTolerance);
                reachOrientationDecelerationRadiusVisTable.setFloat(mi.reachOrientationAlignTolerance);
                reachOrientationTimeToTargetVisTable.setFloat(mi.reachOrientationAlignTolerance);
                break;
            case SEEK: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
                break;
            case WANDER: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
                wanderLastTimeVisTable.setFloat(mi.wanderLastTime);
                wanderOffsetVisTable.setFloat(mi.wanderOffset);
                wanderRadiusVisTable.setFloat(mi.wanderRadius);
                wanderRateVisTable.setFloat(mi.wanderRate);
                wanderOrientationVisTable.setFloat(mi.wanderOrientation);
                wanderFaceEnabledVisTable.setBoolean(mi.wanderFaceEnabled);
                // Wander debug
                wanderInternalTargetPositionVisTable.update();
                wanderCenterVisTable.update();
                break;

            // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
            case ALIGNMENT: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
            case COHESION: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
            case COLLISION_AVOIDANCE: break;
            case HIDE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
                hideDistanceFromBoundary.setFloat(mi.hideDistanceFromBoundary);
                hideArrivalTolerance.setFloat(mi.hideArrivalTolerance);
                hideDecelerationRadius.setFloat(mi.hideDecelerationRadius);
                hideTimeToTarget.setFloat(mi.hideTimeToTarget);
                break;
            case RAY_CAST_OBSTACLE_AVOIDANCE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
                roaSingleRayConfigurationLength.setFloat(mi.roaSingleRayConfigurationLength);
                roaDistanceFromBoundary.setFloat(mi.roaDistanceFromBoundary);
                break;
            case SEPARATION: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
                separationDecayCoefficient.setFloat(mi.separationDecayCoefficient);
                break;
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        updateSteeringParamsVisTable(delta);
    }

    public Location<Vector3> getCurrentTargetA() {
        if (dbgModelInstance == null) { return null; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return null; }
        switch (sb) {
            // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
            case ARRIVE: return dbgModelInstance.arriveTarget;
            case EVADE: return dbgModelInstance.evadeTarget;
            case FACE: return dbgModelInstance.faceTarget;
            case FLEE: return dbgModelInstance.fleeTarget;
            case INTERPOSE: return dbgModelInstance.interposeAgentA;
            case LOOK_WHERE_YOU_ARE_GOING: return dbgModelInstance.lwyagTarget;
            case MATCH_VELOCITY: return dbgModelInstance.matchVelocityTarget;
            case PURSUE: return dbgModelInstance.pursueTarget;
            case REACH_ORIENTATION: return dbgModelInstance.reachOrientationTarget;
            case SEEK: return dbgModelInstance.seekTarget;
            // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
            case HIDE: return dbgModelInstance.hideHunter;
            default: return null;
        }
    }

    public Location<Vector3> getCurrentTargetB() {
        if (dbgModelInstance == null) { return null; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return null; }
        switch (sb) {
            case INTERPOSE: return dbgModelInstance.interposeAgentB;
            default: return null;
        }
    }

    public void setCurrentTargetA(EditableModelInstance target) {
        if (dbgModelInstance == null || target == null) { return; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return; }
        switch (sb) {
            // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
            case ARRIVE: dbgModelInstance.arriveTarget = target; break;
            case EVADE: dbgModelInstance.evadeTarget = target; break;
            case FACE: dbgModelInstance.faceTarget = target; break;
            case FLEE: dbgModelInstance.fleeTarget = target; break;
            case INTERPOSE: dbgModelInstance.interposeAgentA = target; break;
            case LOOK_WHERE_YOU_ARE_GOING: dbgModelInstance.lwyagTarget = target; break;
            case MATCH_VELOCITY: dbgModelInstance.matchVelocityTarget = target; break;
            case PURSUE: dbgModelInstance.pursueTarget = target; break;
            case REACH_ORIENTATION: dbgModelInstance.reachOrientationTarget = target; break;
            case SEEK: dbgModelInstance.seekTarget = target; break;
            // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
            case HIDE: dbgModelInstance.hideHunter = target; break;
        }
    }

    public void setCurrentTargetB(EditableModelInstance target) {
        if (dbgModelInstance == null || target == null) { return; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return; }
        switch (sb) {
            case INTERPOSE: dbgModelInstance.interposeAgentB = target; break;
        }
    }

    public void setCurrentAgents(Array<EditableModelInstance> agents) {
        if (dbgModelInstance == null || agents == null || agents.size == 0) { return; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return; }
        EditableModelInstance mi = dbgModelInstance;
        switch (sb) {
            case ALIGNMENT: mi.alignmentAgents.clear(); mi.alignmentAgents.addAll(agents); break;
            case COHESION: mi.cohesionAgents.clear(); mi.cohesionAgents.addAll(agents); break;
            case COLLISION_AVOIDANCE: mi.collisionAvoidanceAgents.clear(); mi.collisionAvoidanceAgents.addAll(agents); break;
            case HIDE: mi.hideAgents.clear(); mi.hideAgents.addAll(agents); break;
            case SEPARATION: mi.separationAgents.clear(); mi.separationAgents.addAll(agents); break;
        }
    }

    public Proximity<Vector3> getCurrentProximity() {
        if (dbgModelInstance == null) { return null; }
        SteeringBehaviorsVector3Enum sb = steeringBehaviorSB.getSelected();
        if (sb == null) { return null; }
        switch (sb) {
            case ALIGNMENT: return dbgModelInstance.alignmentProximity;
            case COHESION: return dbgModelInstance.cohesionProximity;
            case COLLISION_AVOIDANCE: return dbgModelInstance.collisionAvoidanceProximity;
            case HIDE: return dbgModelInstance.hideProximity;
            case SEPARATION: return dbgModelInstance.separationProximity;
            default: return null;
        }
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}