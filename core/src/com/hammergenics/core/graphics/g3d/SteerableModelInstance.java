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

package com.hammergenics.core.graphics.g3d;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.ai.steer.behaviors.Flee;
import com.badlogic.gdx.ai.steer.behaviors.FollowFlowField;
import com.badlogic.gdx.ai.steer.behaviors.Interpose;
import com.badlogic.gdx.ai.steer.behaviors.Jump;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.MatchVelocity;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum;
import com.hammergenics.ai.steer.behaviors.JumpCallbackAdapter;
import com.hammergenics.ai.steer.behaviors.Y3DGravityComponentHandler;
import com.hammergenics.ai.utils.LocationAdapter;

import static com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum.*;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SteerableModelInstance extends PhysicalModelInstance implements Disposable, Steerable<Vector3> {
    // the vector indicating the linear velocity of this Steerable.
    public final Vector3 linearVelocity = new Vector3(Vector3.Zero);
    // the float value indicating the the angular velocity in radians of this Steerable
    public float angularVelocity = 0f;
    // the bounding radius of this Steerable
    public float boundingRadius = 1f;
    // tag/untag this Steerable. This is a generic flag utilized in a variety of ways
    public boolean tagged = false;

    // Returns the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
    // Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
    public float zeroLinearSpeedThreshold = 0.1f;
    public float maxLinearSpeed = 2.0f;              // the maximum linear speed
    public float maxLinearAcceleration = 1.0f;       // the maximum linear acceleration
    public float maxAngularSpeed = 1.0472f;          // (~ 60 degrees) the maximum angular speed
    public float maxAngularAcceleration = 0.174533f; // (~ 10 degrees) the maximum angular acceleration

    // the vector indicating the position of this location
    public final Vector3 position = new Vector3();
    // Returns the float value indicating the orientation of this location.
    // The orientation is the angle in radians representing the direction that this location is facing
    public float orientation = 0f;

    // Steering Behaviors related:
    public boolean steeringEnabled = false;
    public SteeringBehaviorsVector3Enum currentSteeringBehavior;
    public final SteeringAcceleration<Vector3> steeringAcceleration = new SteeringAcceleration<>(new Vector3()).setZero();
    // SteeringBehavior
    public Steerable<Vector3> steeringBehaviorOwner;
    public Limiter steeringBehaviorLimiter = null;
    public boolean steeringBehaviorEnabled = true;
    // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
    // SteeringBehavior -> Arrive
    public Location<Vector3> arriveTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    public float arriveArrivalTolerance = 0.1f;
    public float arriveDecelerationRadius = 1f;
    public float arriveTimeToTarget = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    // SteeringBehavior -> Pursue -> Evade
    public Steerable<Vector3> evadeTarget = this;
    public float evadeMaxPredictionTime = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
    // ReachOrientation -> Face
    public Location<Vector3> faceTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    public float faceAlignTolerance = 0.1f;
    public float faceDecelerationRadius = 1f;
    public float faceTimeToTarget = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    // SteeringBehavior -> Seek -> Flee
    public Location<Vector3> fleeTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
    // SteeringBehavior -> FollowFlowField
    public FollowFlowField.FlowField<Vector3> flowField = Vector3::new;
    public float followFlowFieldPredictionTime = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
    // SteeringBehavior -> Arrive -> FollowPath
    public LinePath<Vector3> followPath = new LinePath<>(new Array<>(new Vector3[]{Vector3.X.cpy(), Vector3.Z.cpy()}));
    public float followPathOffset = 1f;
    public LinePath.LinePathParam followPathParam = new LinePath.LinePathParam();
    public boolean followPathArriveEnabled = true;
    public float followPathPredictionTime = 1f;
    // FollowPath debug
    public Vector3 followPathInternalTargetPosition = new Vector3();
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
    // SteeringBehavior -> Arrive -> Interpose
    public Steerable<Vector3> interposeAgentA = this;
    public Steerable<Vector3> interposeAgentB = this;
    public float interpositionRatio = 1f;
    // Interpose debug
    public Vector3 interposeInternalTargetPosition = new Vector3();
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
    // SteeringBehavior -> MatchVelocity -> Jump
    public Jump.JumpDescriptor<Vector3> jumpDescriptor;
    public Y3DGravityComponentHandler jumpY3DGravityComponentHandler;
    public JumpCallbackAdapter jumpCallback;
    public Vector3 jumpGravity = new Vector3();
    public float jumpTakeoffPositionTolerance = 0.5f;
    public float jumpTakeoffVelocityTolerance = 0.5f;
    public float jumpTakeoffTolerance = 0.5f;
    public float jumpMaxVerticalVelocity = 2f;
    public float jumpAirborneTime = 0;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
    // SteeringBehavior -> ReachOrientation -> LookWhereYouAreGoing
    public Location<Vector3> lwyagTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    public float lwyagAlignTolerance = 0.1f;
    public float lwyagDecelerationRadius = 1f;
    public float lwyagTimeToTarget = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
    // SteeringBehavior -> MatchVelocity
    public Steerable<Vector3> matchVelocityTarget = this;
    public float matchVelocityTimeToTarget = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    // SteeringBehavior -> Pursue
    public Steerable<Vector3> pursueTarget = this;
    public float pursueMaxPredictionTime = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
    // SteeringBehavior -> ReachOrientation
    public Location<Vector3> reachOrientationTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    public float reachOrientationAlignTolerance = 0.1f;
    public float reachOrientationDecelerationRadius = 1f;
    public float reachOrientationTimeToTarget = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    // SteeringBehavior -> Seek
    public Location<Vector3> seekTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
    // ReachOrientation -> Face -> Wander
    public float wanderLastTime = 0f;
    public float wanderOffset = 2f;
    public float wanderRadius = 2f;
    public float wanderRate = 0.2f;
    public float wanderOrientation = 0f;
    public boolean wanderFaceEnabled = true;
    // Wander debug
    public Vector3 wanderInternalTargetPosition = new Vector3();
    public Vector3 wanderCenter = new Vector3();

    public Array<Steerable<Vector3>> agents = new Array<>(true, 16, Steerable.class);
    // FieldOfViewProximity
    // InfiniteProximity
    // RadiusProximity
    public Proximity<Vector3> proximity = new RadiusProximity<>(this, agents, 1f);

    public SteerableModelInstance(Model model, float mass, ShapesEnum shape) { this(new HGModel(model), null, mass, shape, (String[])null); }
    public SteerableModelInstance(Model model, float mass, ShapesEnum shape, String... rootNodeIds) { this(new HGModel(model), null, mass, shape, rootNodeIds); }
    public SteerableModelInstance(HGModel hgModel, float mass, ShapesEnum shape) { this(hgModel, null, mass, shape, (String[])null); }
    public SteerableModelInstance(HGModel hgModel, float mass, ShapesEnum shape, String... rootNodeIds) { this(hgModel, null, mass, shape, rootNodeIds); }
    public SteerableModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape) { this(hgModel, assetFL, mass, shape, (String[])null); }
    public SteerableModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape, String... rootNodeIds) {
        super(hgModel, assetFL, mass, shape, rootNodeIds);

        steeringBehaviorOwner = this;

        currentSteeringBehavior = ARRIVE;
    }

    @Override public Vector3 getLinearVelocity() { return linearVelocity; }
    @Override public float getAngularVelocity() { return angularVelocity; }
    public void setAngularVelocity(float value) { angularVelocity = value; }
    @Override public float getBoundingRadius() { return boundingRadius; }
    public void setBoundingRadius(float value) { boundingRadius = value; }
    @Override public boolean isTagged() { return tagged; }
    @Override public void setTagged(boolean tagged) { this.tagged = tagged; }

    @Override public float getZeroLinearSpeedThreshold() { return zeroLinearSpeedThreshold; }
    @Override public void setZeroLinearSpeedThreshold(float value) { zeroLinearSpeedThreshold = value; }
    @Override public float getMaxLinearSpeed() { return maxLinearSpeed; }
    @Override public void setMaxLinearSpeed(float maxLinearSpeed) { this.maxLinearSpeed = maxLinearSpeed; }
    @Override public float getMaxLinearAcceleration() { return maxLinearAcceleration; }
    @Override public void setMaxLinearAcceleration(float maxLinearAcceleration) { this.maxLinearAcceleration = maxLinearAcceleration; }
    @Override public float getMaxAngularSpeed() { return maxAngularSpeed; }
    @Override public void setMaxAngularSpeed(float maxAngularSpeed) { this.maxAngularSpeed = maxAngularSpeed; }
    @Override public float getMaxAngularAcceleration() { return maxAngularAcceleration; }
    @Override public void setMaxAngularAcceleration(float maxAngularAcceleration) { this.maxAngularAcceleration = maxAngularAcceleration; }

    @Override public Vector3 getPosition() { return position; }
    @Override public float getOrientation() { return orientation; }
    @Override public void setOrientation(float orientation) { this.orientation = orientation; }
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    @Override public float vectorToAngle(Vector3 vector) { return (float)Math.atan2(-vector.x, vector.z); }
    @Override public Vector3 angleToVector(Vector3 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = 0;
        outVector.z = (float)Math.cos(angle);
        return outVector;
    }
    @Override public Location<Vector3> newLocation() { return null; }

    @Override public void trn(Vector3 vector) { super.trn(vector); syncLocationWithTransform(); }
    @Override public void trn(float x, float y, float z) { super.trn(x, y, z); syncLocationWithTransform(); }
    @Override public void translate(Vector3 translation) { super.translate(translation); syncLocationWithTransform(); }
    @Override public void setToTranslation(Vector3 vector) { super.setToTranslation(vector); syncLocationWithTransform(); }
    @Override public void setToTranslation(float x, float y, float z) { super.setToTranslation(x, y, z); syncLocationWithTransform(); }
    @Override public void setToTranslationAndScaling(Vector3 translation, Vector3 scaling) { super.setToTranslationAndScaling(translation, scaling); syncLocationWithTransform(); }
    @Override public void setToTranslationAndScaling(float x, float y, float z, float sX, float sY, float sZ) { super.setToTranslationAndScaling(x, y, z, sX, sY, sZ); syncLocationWithTransform();}
    @Override public void scl(float factor) { super.scl(factor); syncLocationWithTransform(); }
    @Override public void scl(Vector3 factor) { super.scl(factor); syncLocationWithTransform(); }
    @Override public void scale(float scaleX, float scaleY, float scaleZ) { super.scale(scaleX, scaleY, scaleZ); syncLocationWithTransform(); }
    @Override public void setToScaling(float factor) { super.setToScaling(factor); syncLocationWithTransform(); }
    @Override public void setToScaling(Vector3 factor) { super.setToScaling(factor); syncLocationWithTransform(); }
    @Override public void rotate(Vector3 axis, float degrees) { super.rotate(axis, degrees); syncLocationWithTransform(); }
    @Override public void rotate(Quaternion rotation) { super.rotate(rotation); syncLocationWithTransform(); }
    @Override public void rotate(Vector3 v1, Vector3 v2) { super.rotate(v1, v2); syncLocationWithTransform(); }

    public void syncLocationWithTransform() {
        transform.getTranslation(position);
    }

    public void setSteeringAccelerationAngular(float angular) { steeringAcceleration.angular = angular; }
    public void setSteeringBehaviorOwner(Steerable<Vector3> steeringBehaviorOwner) { this.steeringBehaviorOwner = steeringBehaviorOwner; }
    public void setSteeringBehaviorLimiter(Limiter steeringBehaviorLimiter) { this.steeringBehaviorLimiter = steeringBehaviorLimiter; }
    public void setSteeringBehaviorEnabled(boolean steeringBehaviorEnabled) { this.steeringBehaviorEnabled = steeringBehaviorEnabled; }
    public void setArriveTarget(Location<Vector3> arriveTarget) { this.arriveTarget = arriveTarget; }
    public void setArriveArrivalTolerance(float arriveArrivalTolerance) { this.arriveArrivalTolerance = arriveArrivalTolerance; }
    public void setArriveDecelerationRadius(float arriveDecelerationRadius) { this.arriveDecelerationRadius = arriveDecelerationRadius; }
    public void setArriveTimeToTarget(float arriveTimeToTarget) { this.arriveTimeToTarget = arriveTimeToTarget; }
    public void setEvadeTarget(Steerable<Vector3> evadeTarget) { this.evadeTarget = evadeTarget; }
    public void setEvadeMaxPredictionTime(float evadeMaxPredictionTime) { this.evadeMaxPredictionTime = evadeMaxPredictionTime; }
    public void setFaceTarget(Location<Vector3> faceTarget) { this.faceTarget = faceTarget; }
    public void setFaceAlignTolerance(float faceAlignTolerance) { this.faceAlignTolerance = faceAlignTolerance; }
    public void setFaceDecelerationRadius(float faceDecelerationRadius) { this.faceDecelerationRadius = faceDecelerationRadius; }
    public void setFaceTimeToTarget(float faceTimeToTarget) { this.faceTimeToTarget = faceTimeToTarget; }
    public void setFleeTarget(Location<Vector3> fleeTarget) { this.fleeTarget = fleeTarget; }
    public void setFlowField(FollowFlowField.FlowField<Vector3> flowField) { this.flowField = flowField; }
    public void setFollowFlowFieldPredictionTime(float followFlowFieldPredictionTime) { this.followFlowFieldPredictionTime = followFlowFieldPredictionTime; }
    public void setFollowPath(LinePath<Vector3> followPath) { this.followPath = followPath; }
    public void setFollowPathOffset(float followPathOffset) { this.followPathOffset = followPathOffset; }
    public void setFollowPathParam(LinePath.LinePathParam followPathParam) { this.followPathParam = followPathParam; }
    public void setFollowPathArriveEnabled(boolean followPathArriveEnabled) { this.followPathArriveEnabled = followPathArriveEnabled; }
    public void setFollowPathPredictionTime(float followPathPredictionTime) { this.followPathPredictionTime = followPathPredictionTime; }
    public void setInterposeAgentA(Steerable<Vector3> interposeAgentA) { this.interposeAgentA = interposeAgentA; }
    public void setInterposeAgentB(Steerable<Vector3> interposeAgentB) { this.interposeAgentB = interposeAgentB; }
    public void setInterpositionRatio(float interpositionRatio) { this.interpositionRatio = interpositionRatio; }
    public void setJumpDescriptor(Jump.JumpDescriptor<Vector3> jumpDescriptor) { this.jumpDescriptor = jumpDescriptor; }
    public void setJumpY3DGravityComponentHandler(Y3DGravityComponentHandler jumpY3DGravityComponentHandler) { this.jumpY3DGravityComponentHandler = jumpY3DGravityComponentHandler; }
    public void setJumpCallback(JumpCallbackAdapter jumpCallback) { this.jumpCallback = jumpCallback; }
    public void setJumpGravity(Vector3 jumpGravity) { this.jumpGravity = jumpGravity; }
    public void setJumpTakeoffPositionTolerance(float jumpTakeoffPositionTolerance) { this.jumpTakeoffPositionTolerance = jumpTakeoffPositionTolerance; }
    public void setJumpTakeoffVelocityTolerance(float jumpTakeoffVelocityTolerance) { this.jumpTakeoffVelocityTolerance = jumpTakeoffVelocityTolerance; }
    public void setJumpTakeoffTolerance(float jumpTakeoffTolerance) { this.jumpTakeoffTolerance = jumpTakeoffTolerance; }
    public void setJumpMaxVerticalVelocity(float jumpMaxVerticalVelocity) { this.jumpMaxVerticalVelocity = jumpMaxVerticalVelocity; }
    public void setJumpAirborneTime(float jumpAirborneTime) { this.jumpAirborneTime = jumpAirborneTime; }
    public void setLwyagTarget(Location<Vector3> lwyagTarget) { this.lwyagTarget = lwyagTarget; }
    public void setLwyagAlignTolerance(float lwyagAlignTolerance) { this.lwyagAlignTolerance = lwyagAlignTolerance; }
    public void setLwyagDecelerationRadius(float lwyagDecelerationRadius) { this.lwyagDecelerationRadius = lwyagDecelerationRadius; }
    public void setLwyagTimeToTarget(float lwyagTimeToTarget) { this.lwyagTimeToTarget = lwyagTimeToTarget; }
    public void setMatchVelocityTarget(Steerable<Vector3> matchVelocityTarget) { this.matchVelocityTarget = matchVelocityTarget; }
    public void setMatchVelocityTimeToTarget(float matchVelocityTimeToTarget) { this.matchVelocityTimeToTarget = matchVelocityTimeToTarget; }
    public void setPursueTarget(Steerable<Vector3> pursueTarget) { this.pursueTarget = pursueTarget; }
    public void setPursueMaxPredictionTime(float pursueMaxPredictionTime) { this.pursueMaxPredictionTime = pursueMaxPredictionTime; }
    public void setReachOrientationTarget(Location<Vector3> reachOrientationTarget) { this.reachOrientationTarget = reachOrientationTarget; }
    public void setReachOrientationAlignTolerance(float reachOrientationAlignTolerance) { this.reachOrientationAlignTolerance = reachOrientationAlignTolerance; }
    public void setReachOrientationDecelerationRadius(float reachOrientationDecelerationRadius) { this.reachOrientationDecelerationRadius = reachOrientationDecelerationRadius; }
    public void setReachOrientationTimeToTarget(float reachOrientationTimeToTarget) { this.reachOrientationTimeToTarget = reachOrientationTimeToTarget; }
    public void setSeekTarget(Location<Vector3> seekTarget) { this.seekTarget = seekTarget; }
    public void setWanderLastTime(float wanderLastTime) { this.wanderLastTime = wanderLastTime; }
    public void setWanderOffset(float wanderOffset) { this.wanderOffset = wanderOffset; }
    public void setWanderRadius(float wanderRadius) { this.wanderRadius = wanderRadius; }
    public void setWanderRate(float wanderRate) { this.wanderRate = wanderRate; }
    public void setWanderOrientation(float wanderOrientation) { this.wanderOrientation = wanderOrientation; }
    public void setWanderFaceEnabled(boolean wanderFaceEnabled) { this.wanderFaceEnabled = wanderFaceEnabled; }

    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    public void update (float delta) {
        if (!steeringEnabled) { return; }

        steeringAcceleration.setZero();

        SteeringBehavior<Vector3> sb = null;
        // Calculate steering acceleration for selected behavior
        switch (currentSteeringBehavior) {
            // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
            case ARRIVE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
                Arrive<Vector3> arrive = (Arrive<Vector3>) ARRIVE.getInstance();
                SteeringBehaviorsVector3Enum.initArrive(arrive,
                        arriveTarget,
                        arriveArrivalTolerance,
                        arriveDecelerationRadius,
                        arriveTimeToTarget);
                sb = arrive;
                break;
            case EVADE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
                Evade<Vector3> evade = (Evade<Vector3>) EVADE.getInstance();
                SteeringBehaviorsVector3Enum.initPursue(evade,
                        evadeTarget,
                        evadeMaxPredictionTime);
                sb = evade;
                break;
            case FACE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
                Face<Vector3> face = (Face<Vector3>) FACE.getInstance();
                SteeringBehaviorsVector3Enum.initReachOrientation(face,
                        faceTarget,
                        faceAlignTolerance,
                        faceDecelerationRadius,
                        faceTimeToTarget);
                sb = face;
                break;
            case FLEE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
                Flee<Vector3> flee = (Flee<Vector3>) FLEE.getInstance();
                SteeringBehaviorsVector3Enum.initSeek(flee,
                        fleeTarget);
                sb = flee;
                break;
            case FOLLOW_FLOW_FIELD: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
                FollowFlowField<Vector3> followFlowField = (FollowFlowField<Vector3>) FOLLOW_FLOW_FIELD.getInstance();
                SteeringBehaviorsVector3Enum.initFollowFlowField(followFlowField,
                        flowField,
                        followFlowFieldPredictionTime
                );
                sb = followFlowField;
                break;
            case FOLLOW_PATH: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
                HG3DFollowLinePath followLinePath = (HG3DFollowLinePath) FOLLOW_PATH.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(followLinePath,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                SteeringBehaviorsVector3Enum.initFollowPath(followLinePath,
                        followPath,
                        followPathPredictionTime,
                        followPathArriveEnabled,
                        followPathOffset,
                        followPathParam);
                followLinePath.calculateSteering(steeringAcceleration);
                // debug
                followPathInternalTargetPosition.set(followLinePath.getInternalTargetPosition());
                break;
            case INTERPOSE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
                Interpose<Vector3> interpose = (Interpose<Vector3>) INTERPOSE.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(interpose,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                SteeringBehaviorsVector3Enum.initInterpose(interpose,
                        interposeAgentA,
                        interposeAgentB,
                        interpositionRatio);
                interpose.calculateSteering(steeringAcceleration);
                // debug
                interposeInternalTargetPosition.set(interpose.getInternalTargetPosition());
                break;
            case JUMP: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
                HG3DJump jump = (HG3DJump) JUMP.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(jump,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                SteeringBehaviorsVector3Enum.initJump(jump,
                        jumpDescriptor,
                        jumpGravity,
                        jumpY3DGravityComponentHandler,
                        jumpCallback,
                        jumpMaxVerticalVelocity,
                        jumpTakeoffPositionTolerance,
                        jumpTakeoffVelocityTolerance,
                        jumpTakeoffTolerance);
                jump.calculateSteering(steeringAcceleration);
                jumpAirborneTime = jump.getAirborneTime();
                break;
            case LOOK_WHERE_YOU_ARE_GOING: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
                LookWhereYouAreGoing<Vector3> lookWhereYouAreGoing = (LookWhereYouAreGoing<Vector3>) LOOK_WHERE_YOU_ARE_GOING.getInstance();
                SteeringBehaviorsVector3Enum.initReachOrientation(lookWhereYouAreGoing,
                        lwyagTarget,
                        lwyagAlignTolerance,
                        lwyagDecelerationRadius,
                        lwyagTimeToTarget);
                sb = lookWhereYouAreGoing;
                break;
            case MATCH_VELOCITY: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
                MatchVelocity<Vector3> matchVelocity = (MatchVelocity<Vector3>) MATCH_VELOCITY.getInstance();
                SteeringBehaviorsVector3Enum.initMatchVelocity(matchVelocity,
                        matchVelocityTarget,
                        matchVelocityTimeToTarget);
                sb = matchVelocity;
                break;
            case PURSUE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
                Pursue<Vector3> pursue = (Pursue<Vector3>) PURSUE.getInstance();
                SteeringBehaviorsVector3Enum.initPursue(pursue,
                        pursueTarget,
                        pursueMaxPredictionTime);
                sb = pursue;
                break;
            case REACH_ORIENTATION: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
                ReachOrientation<Vector3> reachOrientation = (ReachOrientation<Vector3>) REACH_ORIENTATION.getInstance();
                SteeringBehaviorsVector3Enum.initReachOrientation(reachOrientation,
                        reachOrientationTarget,
                        reachOrientationAlignTolerance,
                        reachOrientationDecelerationRadius,
                        reachOrientationTimeToTarget);
                sb = reachOrientation;
                break;
            case SEEK: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
                Seek<Vector3> seek = (Seek<Vector3>) SEEK.getInstance();
                SteeringBehaviorsVector3Enum.initSeek(seek,
                        seekTarget);
                sb = seek;
                break;
            case WANDER: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
                HG3DWander wander = (HG3DWander) WANDER.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(wander,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                //SteeringBehaviorsVector3Enum.initReachOrientation(wander,...); // see Wander.internalTargetPosition
                //SteeringBehaviorsVector3Enum.initFace(wander);
                SteeringBehaviorsVector3Enum.initWander(wander,
                        wanderLastTime,
                        wanderOffset,
                        wanderRadius,
                        wanderRate,
                        wanderOrientation,
                        wanderFaceEnabled);
                wander.calculateSteering(steeringAcceleration);
                wanderLastTime = wander.getLastTime();
                // debug
                wanderInternalTargetPosition.set(wander.getInternalTargetPosition());
                wanderCenter.set(wander.getWanderCenter());
                break;

            // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
            case ALIGNMENT: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
            case COHESION: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
            case COLLISION_AVOIDANCE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
            case HIDE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
            case RAY_CAST_OBSTACLE_AVOIDANCE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
            case SEPARATION: break;

            // COMBINING STEERING BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#combining-steering-behaviors
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#blended-steering
            case BLENDED_STEERING: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#priority-steering
            case PRIORITY_STEERING: break;
        }

        if (sb != null) {
            SteeringBehaviorsVector3Enum.initSteeringBehavior(sb,
                    steeringBehaviorOwner,
                    steeringBehaviorLimiter,
                    steeringEnabled);
            sb.calculateSteering(steeringAcceleration);
        }

        /*
         * Here you might want to add a motor control layer filtering steering accelerations.
         *
         * For instance, a car in a driving game has physical constraints on its movement:
         * - it cannot turn while stationary
         * - the faster it moves, the slower it can turn (without going into a skid)
         * - it can brake much more quickly than it can accelerate
         * - it only moves in the direction it is facing (ignoring power slides)
         */

        // Apply steering acceleration to move this agent
        applySteering(steeringAcceleration, delta);
    }

    private final Vector3 translation = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Vector3 scale = new Vector3();
    private final Matrix4 tmpM4 = new Matrix4();

    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    private void applySteering(SteeringAcceleration<Vector3> steering, float time) {
        transform.getScale(scale);
        // Update position and linear velocity. Velocity is trimmed to maximum speed
        this.position.mulAdd(linearVelocity, time);
        this.orientation += angularVelocity * time;
        //Gdx.app.debug("steerable", "steering.linear: " + steering.linear);
        //Gdx.app.debug("steerable", "steering.angular: " + steering.angular);
        this.linearVelocity.mulAdd(steering.linear, time).limit(this.getMaxLinearSpeed());
        this.angularVelocity += steering.angular * time;
        //Gdx.app.debug("steerable", "linearVelocity: " + this.linearVelocity);
        //Gdx.app.debug("steerable", "angularVelocity: " + this.angularVelocity);

        rotation.setEulerAnglesRad(-this.orientation, 0f, 0f);

        setToTranslation(this.position);
        rotate(rotation);
        scale(scale.x, scale.y, scale.z);
    }
}
