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

package com.hammergenics.screens.ai.steer;

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
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.MatchVelocity;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.screens.graphics.g3d.PhysicalModelInstance.ShapesEnum;
import com.hammergenics.screens.graphics.g3d.SteerableModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum SteeringBehaviorsVector3Enum {
    ALIGNMENT(Alignment.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Alignment<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    ARRIVE(Arrive.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Arrive<>(stubOwner); }
            return instance;
        }
    },
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
    COHESION(Cohesion.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Cohesion<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    COLLISION_AVOIDANCE(CollisionAvoidance.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new CollisionAvoidance<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    EVADE(Evade.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Evade<>(stubOwner, stubAgent1); }
            return instance;
        }
    },
    FACE(Face.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Face<>(stubOwner); }
            return instance;
        }
    },
    FLEE(Flee.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Flee<>(stubOwner); }
            return instance;
        }
    },
    FOLLOW_FLOW_FIELD(FollowFlowField.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new FollowFlowField<>(stubOwner); }
            return instance;
        }
    },
    FOLLOW_PATH(FollowPath.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new FollowPath<>(stubOwner, stubPath); }
            return instance;
        }
    },
    HIDE(Hide.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Hide<>(stubOwner); }
            return instance;
        }
    },
    INTERPOSE(Interpose.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Interpose<>(stubOwner, stubAgent1, stubAgent2); }
            return instance;
        }
    },
    //JUMP(Jump.class) {
    //    @Override
    //    public SteeringBehavior<Vector3> getInstance() {
    //        // single threaded processing is assumed: returning a singleton
    //        if (instance == null) { instance = new Jump<>(); }
    //        return instance;
    //    }
    //},
    LOOK_WHERE_YOU_ARE_GOING(LookWhereYouAreGoing.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new LookWhereYouAreGoing<>(stubOwner); }
            return instance;
        }
    },
    MATCH_VELOCITY(MatchVelocity.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new MatchVelocity<>(stubOwner); }
            return instance;
        }
    },
    PRIORITY_STEERING(PrioritySteering.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new PrioritySteering<>(stubOwner); }
            return instance;
        }
    },
    PURSUE(Pursue.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Pursue<>(stubOwner, stubAgent1); }
            return instance;
        }
    },
    //RAY_CAST_OBSTACLE_AVOIDANCE(RaycastObstacleAvoidance.class) {
    //    @Override
    //    public SteeringBehavior<Vector3> getInstance() {
    //        // single threaded processing is assumed: returning a singleton
    //        if (instance == null) { instance = new RaycastObstacleAvoidance<>(); }
    //        return instance;
    //    }
    //},
    REACH_ORIENTATION(ReachOrientation.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new ReachOrientation<>(stubOwner); }
            return instance;
        }
    },
    SEEK(Seek.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Seek<>(stubOwner); }
            return instance;
        }
    },
    SEPARATION(Separation.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Separation<>(stubOwner, stubProximity); }
            return instance;
        }
    },
    WANDER(Wander.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new Wander<>(stubOwner); }
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
    public static final Path<Vector3, LinePath.LinePathParam> stubPath;

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
}