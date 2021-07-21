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

package com.hammergenics.ai.steer;

import com.badlogic.gdx.ai.steer.GroupBehavior;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Alignment;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.Cohesion;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.ai.steer.behaviors.Flee;
import com.badlogic.gdx.ai.steer.behaviors.FollowFlowField;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.behaviors.Hide;
import com.badlogic.gdx.ai.steer.behaviors.Interpose;
import com.badlogic.gdx.ai.steer.behaviors.Jump;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.MatchVelocity;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.ai.steer.behaviors.JumpCallbackAdapter;
import com.hammergenics.ai.steer.behaviors.Y3DGravityComponentHandler;
import com.hammergenics.core.graphics.g3d.PhysicalModelInstance.ShapesEnum;
import com.hammergenics.core.graphics.g3d.SteerableModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum SteeringBehaviorsVector3Enum {
    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
    ARRIVE(Arrive.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Arrive<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    EVADE(Evade.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Evade<>(stubOwner, stubAgent1); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
    FACE(Face.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Face<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    FLEE(Flee.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Flee<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
    FOLLOW_FLOW_FIELD(FollowFlowField.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new FollowFlowField<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
    FOLLOW_PATH(FollowPath.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new HG3DFollowLinePath(stubOwner, stubPath); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
    INTERPOSE(Interpose.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Interpose<>(stubOwner, stubAgent1, stubAgent2); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
    JUMP(Jump.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new HG3DJump(stubOwner, stubJumpDescriptor, stubV1, stubY3DGravityComponentHandler, stubJumpCallback); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
    LOOK_WHERE_YOU_ARE_GOING(LookWhereYouAreGoing.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new LookWhereYouAreGoing<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
    MATCH_VELOCITY(MatchVelocity.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new MatchVelocity<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    PURSUE(Pursue.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Pursue<>(stubOwner, stubAgent1); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
    REACH_ORIENTATION(ReachOrientation.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new ReachOrientation<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    SEEK(Seek.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Seek<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
    WANDER(Wander.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new HG3DWander(stubOwner); }
            return instance;
        }
    },

    // GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
    ALIGNMENT(Alignment.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Alignment<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
    COHESION(Cohesion.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Cohesion<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
    COLLISION_AVOIDANCE(CollisionAvoidance.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new CollisionAvoidance<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
    HIDE(Hide.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Hide<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
    RAY_CAST_OBSTACLE_AVOIDANCE(RaycastObstacleAvoidance.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new RaycastObstacleAvoidance<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
    SEPARATION(Separation.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Separation<>(stubOwner, stubProximity); }
            return instance;
        }
    },

    // COMBINING STEERING BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#combining-steering-behaviors
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#blended-steering
    // This combination behavior simply sums up all the behaviors,
    // applies their weights, and truncates the result before returning.
    BLENDED_STEERING(BlendedSteering.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new BlendedSteering<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#priority-steering
    PRIORITY_STEERING(PrioritySteering.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new PrioritySteering<>(stubOwner); }
            return instance;
        }
    };

    // SteeringBehavior (and it's descendants) seem to be the "stateless" algorithm providers
    // meaning they don't store the actual state of a particular steering of a particular owner etc.
    // That is why it is safe to initialize the SteeringBehavior instances with the stub components
    // and replace them on a particular use.
    public static final Steerable<Vector3> stubOwner;
    public static final Steerable<Vector3> stubAgent1;
    public static final Steerable<Vector3> stubAgent2;
    public static final Proximity<Vector3> stubProximity;
    public static final LinePath<Vector3> stubPath;
    public static final Jump.JumpDescriptor<Vector3> stubJumpDescriptor;
    public static final Y3DGravityComponentHandler stubY3DGravityComponentHandler;
    public static final JumpCallbackAdapter stubJumpCallback;
    private static final Vector3 stubV1 = new Vector3();
    private static final Vector3 stubV2 = new Vector3();

    static {
        stubOwner = new SteerableModelInstance(HGEngine.boxHgModel, 0f, ShapesEnum.BOX);
        stubAgent1 = new SteerableModelInstance(HGEngine.boxHgModel, 0f, ShapesEnum.BOX);
        stubAgent2 = new SteerableModelInstance(HGEngine.boxHgModel, 0f, ShapesEnum.BOX);
        Array<Steerable<Vector3>> agents = new Array<>(Steerable.class);
        agents.add(stubAgent1);
        agents.add(stubAgent2);
        stubProximity = new RadiusProximity<>(stubOwner, agents, 1f);
        Array<Vector3> waypoints = new Array<>(Vector3.class);
        waypoints.addAll(Vector3.X.cpy(), Vector3.Y.cpy(), Vector3.Z.cpy(), Vector3.Zero.cpy());
        stubPath = new LinePath<>(waypoints);
        stubJumpDescriptor = new Jump.JumpDescriptor<>(stubV1, stubV2);
        stubY3DGravityComponentHandler = new Y3DGravityComponentHandler();
        stubJumpCallback = new JumpCallbackAdapter();
    }

    public Class<?> clazz;
    protected SteeringBehavior<Vector3> instance = null;

    SteeringBehaviorsVector3Enum(Class<?> clazz) {
        this.clazz = clazz;
    }

    // SteeringBehavior (and it's descendants) seem to be the "stateless" algorithm providers
    // meaning they don't store the actual state of a particular steering of a particular owner etc.
    // That is why one instance could be shared among different multiple calls for acceleration
    // calculations. In case of multithreaded processing pooling should be considered as the way
    // to acquire instances of SteeringBehavior, for now single threaded processing is assumed
    // and getInstance() is expected to return a singleton.
    public abstract SteeringBehavior<Vector3> getInstance();

    public static class HG3DJump extends Jump<Vector3> {
        public HG3DJump(Steerable<Vector3> owner, JumpDescriptor<Vector3> jumpDescriptor, Vector3 gravity, GravityComponentHandler<Vector3> gravityComponentHandler, JumpCallback callback) {
            super(owner, jumpDescriptor, gravity, gravityComponentHandler, callback);
        }
        public void setCallback(JumpCallback callback) { this.callback = callback; }
        public void setGravityComponentHandler(Jump.GravityComponentHandler<Vector3> gravityComponentHandler) {
            this.gravityComponentHandler = gravityComponentHandler;
        }
        public float getAirborneTime() { return this.airborneTime; }
    }
    public static class HG3DFollowLinePath extends FollowPath<Vector3, LinePath.LinePathParam> {
        public HG3DFollowLinePath(Steerable<Vector3> owner, LinePath<Vector3> path) {
            super(owner, path);
        }
        public void setPathParam(LinePath.LinePathParam pathParam) {
            this.pathParam = pathParam;
        }
    }
    public static class HG3DWander extends Wander<Vector3> {
        public HG3DWander(Steerable<Vector3> owner) {
            super(owner);
        }
        public float getLastTime() { return this.lastTime; }
        public void setLastTime(float lastTime) { this.lastTime = lastTime; }
    }

    // Init Methods
    public static void initSteeringBehavior(SteeringBehavior<Vector3> steeringBehavior,
                                            Steerable<Vector3> owner, Limiter limiter, boolean enabled) {
        steeringBehavior.setOwner(owner);
        steeringBehavior.setLimiter(limiter);
        steeringBehavior.setEnabled(enabled);
    }
    public static void initGroupBehavior(GroupBehavior<Vector3> groupBehavior, Proximity<Vector3> proximity) {
        // Consider also:
        // initSteeringBehavior
        groupBehavior.setProximity(proximity);
    }
    public static void initAlignment() { }
    public static void initArrive(Arrive<Vector3> arrive, Location<Vector3> target,
                                  float arrivalTolerance, float decelerationRadius, float timeToTarget) {
        // Consider also:
        // initSteeringBehavior
        if (target != null) { arrive.setTarget(target); }
        arrive.setArrivalTolerance(arrivalTolerance);
        arrive.setDecelerationRadius(decelerationRadius);
        arrive.setTimeToTarget(timeToTarget);
    }
    public static void initBlendedSteering() { }
    public static void initCohesion() { }
    public static void initCollisionAvoidance() { }
    public static void initEvade(Evade<Vector3> evade) {
        // Consider also:
        // initSteeringBehavior
        // initPursue
    }
    public static void initFace(Face<Vector3> face) {
        // Consider also:
        // initSteeringBehavior
        // initReachOrientation
    }
    public static void initFlee() {
        // Consider also:
        // initSteeringBehavior
        // initSeek
    }
    public static void initFollowFlowField(FollowFlowField<Vector3> followFlowField,
                                           FollowFlowField.FlowField<Vector3> flowField,
                                           float predictionTime) {
        // Consider also:
        // initSteeringBehavior
        followFlowField.setFlowField(flowField);
        followFlowField.setPredictionTime(predictionTime);
    }
    public static void initFollowPath(HG3DFollowLinePath followPath, LinePath<Vector3> path, float predictionTime,
                                      boolean arriveEnabled, float pathOffset, LinePath.LinePathParam pathParam) {
        // Consider also:
        // initSteeringBehavior
        // initArrive
        followPath.setPath(path);
        followPath.setPredictionTime(predictionTime);
        followPath.setArriveEnabled(arriveEnabled);
        followPath.setPathOffset(pathOffset);
        followPath.setPathParam(pathParam);
    }
    public static void initHide() { }
    public static void initInterpose(Interpose<Vector3> interpose, Steerable<Vector3> agentA,
                                     Steerable<Vector3> agentB, float interpositionRatio) {
        // Consider also:
        // initSteeringBehavior
        // initArrive
        interpose.setAgentA(agentA);
        interpose.setAgentB(agentB);
        interpose.setInterpositionRatio(interpositionRatio);
    }
    public static void initJump(HG3DJump jump, Jump.JumpDescriptor<Vector3> jumpDescriptor, Vector3 gravity,
                                Jump.GravityComponentHandler<Vector3> gravityComponentHandler, Jump.JumpCallback callback,
                                float maxVerticalVelocity, float takeoffPositionTolerance,
                                float takeoffVelocityTolerance, float takeoffTolerance) {
        // Consider also:
        // initSteeringBehavior
        // initMatchVelocity
        jump.setJumpDescriptor(jumpDescriptor);
        jump.setGravity(gravity);
        jump.setGravityComponentHandler(gravityComponentHandler);
        jump.setCallback(callback);
        jump.setMaxVerticalVelocity(maxVerticalVelocity);
        jump.setTakeoffPositionTolerance(takeoffPositionTolerance);
        jump.setTakeoffVelocityTolerance(takeoffVelocityTolerance);
        jump.setTakeoffTolerance(takeoffTolerance);
    }
    public static void initLookWhereYouAreGoing(LookWhereYouAreGoing<Vector3> lookWhereYouAreGoing) {
        // Consider also:
        // initSteeringBehavior
        // initReachOrientation
    }
    public static void initMatchVelocity(MatchVelocity<Vector3> matchVelocity, Steerable<Vector3> target, float timeToTarget) {
        // Consider also:
        // initSteeringBehavior
        matchVelocity.setTarget(target);
        matchVelocity.setTimeToTarget(timeToTarget);
    }
    public static void initPrioritySteering() { }
    public static void initPursue(Pursue<Vector3> pursue, Steerable<Vector3> target, float maxPredictionTime) {
        // Consider also:
        // initSteeringBehavior
        pursue.setTarget(target);
        pursue.setMaxPredictionTime(maxPredictionTime);
    }
    public static void initRaycastObstacleAvoidance() { }
    public static void initReachOrientation(ReachOrientation<Vector3> reachOrientation, Location<Vector3> target,
                                            float alignTolerance, float decelerationRadius, float timeToTarget) {
        // Consider also:
        // initSteeringBehavior
        reachOrientation.setTarget(target);
        reachOrientation.setAlignTolerance(alignTolerance);
        reachOrientation.setDecelerationRadius(decelerationRadius);
        reachOrientation.setTimeToTarget(timeToTarget);
    }
    public static void initSeek(Seek<Vector3> seek, Location<Vector3> target) {
        // Consider also:
        // initSteeringBehavior
        seek.setTarget(target);
    }
    public static void initSeparation() { }
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
    public static void initWander(HG3DWander wander, float lastTime, float wanderOffset, float wanderRadius, float wanderRate,
                                  float wanderOrientation, boolean faceEnabled) {
        // Consider also:
        // initSteeringBehavior
        // initReachOrientation
        // initFace
        wander.setLastTime(lastTime);
        wander.setWanderOffset(wanderOffset);
        wander.setWanderRadius(wanderRadius);
        wander.setWanderRate(wanderRate);
        wander.setWanderOrientation(wanderOrientation);
        wander.setFaceEnabled(faceEnabled);
    }
}