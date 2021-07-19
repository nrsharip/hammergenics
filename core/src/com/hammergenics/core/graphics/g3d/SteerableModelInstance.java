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
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.FollowFlowField;
import com.badlogic.gdx.ai.steer.behaviors.Jump;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
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
    public float maxLinearSpeed = 2f;                // the maximum linear speed
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
    public Location<Vector3> evadeTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
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
    public FollowFlowField.FlowField<Vector3> flowField = position -> null;
    public float flowFieldPredictionTime = 1f;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
    // SteeringBehavior -> Arrive -> FollowPath
    public LinePath<Vector3> path = new LinePath<>(new Array<>(new Vector3[]{Vector3.X.cpy(), Vector3.Z.cpy()}));
    public float pathOffset;
    public Path.PathParam pathParam = new LinePath.LinePathParam();
    public boolean arriveEnabled;
    public float predictionTime;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
    // SteeringBehavior -> Arrive -> Interpose
    protected Steerable<Vector3> agentA;
    protected Steerable<Vector3> agentB;
    protected float interpositionRatio;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
    // SteeringBehavior -> MatchVelocity -> Jump
    public Jump.JumpDescriptor<Vector3> jumpDescriptor;
    public Vector3 jumpGravity = new Vector3();
    public Y3DGravityComponentHandler y3DGravityComponentHandler;
    public JumpCallbackAdapter jumpCallback;
    public float takeoffPositionTolerance;
    public float takeoffVelocityTolerance;
    public float maxVerticalVelocity;
    public float airborneTime = 0;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
    // SteeringBehavior -> ReachOrientation -> LookWhereYouAreGoing
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
    // SteeringBehavior -> MatchVelocity
    protected Steerable<Vector3> matchVelocityTarget;
    protected float matchVelocityTimeToTarget;
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    // SteeringBehavior -> Pursue
    public Location<Vector3> pursueTarget = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
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
    public float wanderOffset = 1f;
    public float wanderRadius = 10f;
    public float wanderRate = 1f;
    public float wanderOrientation = 0f;
    public boolean faceEnabled = true;

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
    @Override public float getBoundingRadius() { return boundingRadius; }
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

    public void syncLocationWithTransform() { transform.getTranslation(position); }

    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    public void update (float delta) {
        if (!steeringEnabled) { return; }

        steeringAcceleration.setZero();
        // Calculate steering acceleration for selected behavior
        switch (currentSteeringBehavior) {
            // INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
            case ARRIVE:
                Arrive<Vector3> arrive = (Arrive<Vector3>) ARRIVE.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(arrive,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                SteeringBehaviorsVector3Enum.initArrive(arrive,
                        arriveTarget,
                        arriveArrivalTolerance,
                        arriveDecelerationRadius,
                        arriveTimeToTarget);
                arrive.calculateSteering(steeringAcceleration);
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
            case EVADE:
                Evade<Vector3> evade = (Evade<Vector3>) EVADE.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(evade,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
            case FACE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
            case FLEE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
            case FOLLOW_FLOW_FIELD: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
            case FOLLOW_PATH: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
            case INTERPOSE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
            case JUMP: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
            case LOOK_WHERE_YOU_ARE_GOING: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
            case MATCH_VELOCITY: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
            case PURSUE: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
            case REACH_ORIENTATION: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
            case SEEK: break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
            case WANDER:
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
                        faceEnabled);
                wander.calculateSteering(steeringAcceleration);
                wanderLastTime = wander.getLastTime();
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
