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

import com.badlogic.gdx.ai.fma.FormationMember;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Alignment;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Cohesion;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.ai.steer.behaviors.Flee;
import com.badlogic.gdx.ai.steer.behaviors.FollowFlowField;
import com.badlogic.gdx.ai.steer.behaviors.Hide;
import com.badlogic.gdx.ai.steer.behaviors.Interpose;
import com.badlogic.gdx.ai.steer.behaviors.Jump;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.MatchVelocity;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.steer.utils.rays.SingleRayConfiguration;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.ai.pfa.HGGraphNode;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3EnumCombining;
import com.hammergenics.ai.steer.SteeringBehaviorsVector3EnumCombining.HGBlendedSteering;
import com.hammergenics.ai.steer.behaviors.JumpCallbackAdapter;
import com.hammergenics.ai.steer.behaviors.Y3DGravityComponentHandler;
import com.hammergenics.ai.utils.LocationAdapter;
import com.hammergenics.core.graphics.glutils.HGImmediateModeRenderer20;

import static com.hammergenics.ai.steer.SteeringBehaviorsVector3Enum.*;
import static com.hammergenics.ai.steer.SteeringBehaviorsVector3EnumCombining.*;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SteerableModelInstance extends PhysicalModelInstance implements Disposable, Steerable<Vector3>, FormationMember<Vector3> {
    // INTERFACE Steerable:
    // the vector indicating the linear velocity of this Steerable.
    public final Vector3 linearVelocity = new Vector3(Vector3.Zero);
    // the float value indicating the the angular velocity in radians of this Steerable
    public float angularVelocity = 0f;
    // the bounding radius of this Steerable
    public float boundingRadius = 1f;
    // tag/untag this Steerable. This is a generic flag utilized in a variety of ways
    public boolean tagged = false;

    // INTERFACE Limiter:
    // Returns the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
    // Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
    public float zeroLinearSpeedThreshold = 0.1f;
    public float maxLinearSpeed = 5.0f;          // the maximum linear speed
    public float maxLinearAcceleration = 100.0f; // the maximum linear acceleration
                                                 // NOTE: keeping a large number so there's always enough speed on the slopes
    public float maxAngularSpeed = 10f;          // the maximum angular speed
    public float maxAngularAcceleration = 30f;   // the maximum angular acceleration

    // INTERFACE Location:
    // the vector indicating the position of this location
    public final Vector3 position = new Vector3();
    // Returns the float value indicating the orientation of this location.
    // The orientation is the angle in radians representing the direction that this location is facing
    public float orientation = 0f;

    //////////////////////////////////////
    // Steering Behaviors related:
    public boolean steeringEnabled = false;
    public SteeringBehaviorsVector3Enum currentSteeringBehavior;
    public final SteeringAcceleration<Vector3> steeringAcceleration = new SteeringAcceleration<>(new Vector3()).setZero();
    // SteeringBehavior
    public Steerable<Vector3> steeringBehaviorOwner;
    public Limiter steeringBehaviorLimiter = null;
    public boolean steeringBehaviorEnabled = true;

    // I. INDIVIDUAL BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#individual-behaviors
    // I.1. Arrive: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#arrive
    //      SteeringBehavior -> Arrive
    public Location<Vector3> arriveTarget = this;
    public float arriveArrivalTolerance = 0.01f;
    public float arriveDecelerationRadius = 2f;
    public float arriveTimeToTarget = 0.5f;
    // I.2. Evade: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    //      SteeringBehavior -> Pursue -> Evade
    public Steerable<Vector3> evadeTarget = this;
    public float evadeMaxPredictionTime = 1f;
    // I.3. Face: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#face
    //      ReachOrientation -> Face
    public Location<Vector3> faceTarget = this;
    public float faceAlignTolerance = 0.01f;
    public float faceDecelerationRadius = 2f;
    public float faceTimeToTarget = 0.5f;
    // I.4. Flee: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    //      SteeringBehavior -> Seek -> Flee
    public Location<Vector3> fleeTarget = this;
    // I.5. Follow Flow Field: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#flow-field-following
    //      SteeringBehavior -> FollowFlowField
    public FollowFlowField.FlowField<Vector3> flowField = Vector3::new;
    public float followFlowFieldPredictionTime = 1f;
    // I.6. Follow Path: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#path-following
    //      SteeringBehavior -> Arrive -> FollowPath
    // I.6.1 Arrive part
    public float followPathArrivalTolerance = 0.01f;
    public float followPathDecelerationRadius = 2f;
    public float followPathTimeToTarget = 0.5f;
    // I.6.2 FollowPath part
    public LinePath<Vector3> followPath = new LinePath<>(new Array<>(new Vector3[]{
            new Vector3(-3, 1, -3), new Vector3(-3, 1, 3), new Vector3(3, 1, 3), new Vector3(3, 1, -3), new Vector3(-3, 1, -3),
            new Vector3(-6, 1, -6), new Vector3(-6, 1, 6), new Vector3(6, 1, 6), new Vector3(6, 1, -6), new Vector3(-6, 1, -6),
            new Vector3(-9, 1, -9), new Vector3(-9, 1, 9), new Vector3(9, 1, 9), new Vector3(9, 1, -9), new Vector3(-9, 1, -9),
    }), true);
    public float followPathOffset = 1f;
    public LinePath.LinePathParam followPathParam = new LinePath.LinePathParam();
    public int followPathParamSegmentIndex;
    public float followPathParamDistance;
    public boolean followPathArriveEnabled = true;
    public float followPathPredictionTime = 0.0000000001f; // making almost non-predictive path following
                                                           // so path segment indices are switched precisely
                                                           // at the point where the object goes from one segment to another
    // I.6.3 FollowPath debug
    public Vector3 followPathInternalTargetPosition = new Vector3();
    // I.7. Interpose: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
    //      SteeringBehavior -> Arrive -> Interpose
    // I.7.1 Arrive part
    public float interposeArrivalTolerance = 0.01f;
    public float interposeDecelerationRadius = 2f;
    public float interposeTimeToTarget = 0.5f;
    // I.7.2 Interpose part
    public Steerable<Vector3> interposeAgentA = this;
    public Steerable<Vector3> interposeAgentB = this;
    public float interpositionRatio = 0.5f;
    // I.7.3 Interpose debug
    public Vector3 interposeInternalTargetPosition = new Vector3();
    // I.8. Jump: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#jump
    //      SteeringBehavior -> MatchVelocity -> Jump
    public Jump.JumpDescriptor<Vector3> jumpDescriptor = new Jump.JumpDescriptor<>(new Vector3(), new Vector3());
    public Y3DGravityComponentHandler jumpY3DGravityComponentHandler = new Y3DGravityComponentHandler();
    public boolean jumpCallbackAchievable = false;
    public Jump.JumpCallback jumpCallback = new Jump.JumpCallback() {
        @Override public void reportAchievability(boolean achievable) { jumpCallbackAchievable = achievable; }
        @Override public void takeoff(float maxVerticalVelocity, float time) {
            linearVelocity.y = maxVerticalVelocity;
        }
    };
    public Vector3 jumpGravity = new Vector3(0f, -10f, 0f);
    public float jumpTakeoffPositionTolerance = 0.1f;
    public float jumpTakeoffVelocityTolerance = 0.1f;
    public float jumpTakeoffTolerance = 0.1f;
    public float jumpMaxVerticalVelocity = 10f;
    public float jumpAirborneTime = 0;
    // I.9. Look Where You Are Going: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#look-where-you-are-going
    //      SteeringBehavior -> ReachOrientation -> LookWhereYouAreGoing
    public Location<Vector3> lwyagTarget = this;
    public float lwyagAlignTolerance = 0.01f;
    public float lwyagDecelerationRadius = 2f;
    public float lwyagTimeToTarget = 0.1f;
    // I.10. Match Velocity: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#match-velocity
    //       SteeringBehavior -> MatchVelocity
    public Steerable<Vector3> matchVelocityTarget = this;
    public float matchVelocityTimeToTarget = 1f;
    // I.11. Pursue: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#pursue-and-evade
    //       SteeringBehavior -> Pursue
    public Steerable<Vector3> pursueTarget = this;
    public float pursueMaxPredictionTime = 1f;
    // I.12. Reach Orientation: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#reach-orientation
    //       SteeringBehavior -> ReachOrientation
    public Location<Vector3> reachOrientationTarget = this;
    public float reachOrientationAlignTolerance = 0.01f;
    public float reachOrientationDecelerationRadius = 2f;
    public float reachOrientationTimeToTarget = 0.5f;
    // I.13. Seek: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#seek-and-flee
    //       SteeringBehavior -> Seek
    public Location<Vector3> seekTarget = this;
    // I.14. Wander: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
    //       ReachOrientation -> Face -> Wander
    public float wanderLastTime = 0f;
    public float wanderOffset = 0f;
    public float wanderRadius = 1f;
    public float wanderRate = 0.01f;
    public float wanderOrientation = 0f;
    public boolean wanderFaceEnabled = true;
    // I.14.1 Wander debug
    public Vector3 wanderInternalTargetPosition = new Vector3();
    public Vector3 wanderCenter = new Vector3();

    // II. GROUP BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#group-behaviors
    // Proximity implementations:
    // * FieldOfViewProximity
    // * InfiniteProximity
    // * RadiusProximity
    // II.1 Alignment: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#alignment
    public Array<Steerable<Vector3>> alignmentAgents = new Array<>(true, 16, Steerable.class);
    public Proximity<Vector3> alignmentProximity = new RadiusProximity<>(this, alignmentAgents, 50f);
    // II.2 Cohesion: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
    public Array<Steerable<Vector3>> cohesionAgents = new Array<>(true, 16, Steerable.class);
    public Proximity<Vector3> cohesionProximity = new RadiusProximity<>(this, cohesionAgents, 50f);
    // II.3 Collision Avoidance: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
    public Array<Steerable<Vector3>> collisionAvoidanceAgents = new Array<>(true, 16, Steerable.class);
    public Proximity<Vector3> collisionAvoidanceProximity = new RadiusProximity<>(this, collisionAvoidanceAgents, 50f);
    // II.4 Hide: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
    public Array<Steerable<Vector3>> hideAgents = new Array<>(true, 16, Steerable.class);
    public Proximity<Vector3> hideProximity = new RadiusProximity<>(this, hideAgents, 50f);
    public Location<Vector3> hideHunter = new LocationAdapter<>(new Vector3(0f, 0f, 0f), 0f);
    public float hideDistanceFromBoundary = 0.5f;
    public float hideArrivalTolerance = 0.01f;
    public float hideDecelerationRadius = 2f;
    public float hideTimeToTarget = 0.5f;
    // II.5 Raycast Obstacle Avoidance: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
    //      RayConfiguration implementations:
    //      * CentralRayWithWhiskersConfiguration
    //      * ParallelSideRayConfiguration
    //      * SingleRayConfiguration
    public SingleRayConfiguration<Vector3> roaSingleRayConfiguration;
    public float roaSingleRayConfigurationLength = 50f;
    public RaycastCollisionDetector<Vector3> roaRaycastCollisionDetector = new RaycastCollisionDetector<Vector3>() {
        @Override public boolean collides(Ray<Vector3> ray) { return false; }
        @Override public boolean findCollision(Collision<Vector3> outputCollision, Ray<Vector3> inputRay) { return false; }
    };
    public float roaDistanceFromBoundary = 0.5f;
    // II.6 Separation: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
    public Array<Steerable<Vector3>> separationAgents = new Array<>(true, 16, Steerable.class);
    public Proximity<Vector3> separationProximity = new RadiusProximity<>(this, separationAgents, 50f);
    public float separationDecayCoefficient = 0.5f;

    // Path Finding:
    public GraphPath<Connection<HGGraphNode>> outPath = null;
    public Array<Vector3> waypoints = new Array<>(true, 16, Vector3.class);
    public final Vector3 tmpV1 = new Vector3();
    public final Vector3 tmpV2 = new Vector3();
    public void setOutPath(GraphPath<Connection<HGGraphNode>> outPath) {
        if (this.outPath != null) { this.outPath.clear(); }
        this.outPath = outPath;

        if (outPath == null || outPath.getCount() < 1) { steeringEnabled = false; return; }

        waypoints.clear();
        float halfHeight = getBB().getHeight()/2f;
        outPath.forEach(hgGraphNodeConnection -> {
            tmpV1.set(hgGraphNodeConnection.getFromNode().coordinates).add(0f, halfHeight, 0f);
            waypoints.add(tmpV1.cpy());
        });
        tmpV1.set(outPath.get(outPath.getCount() - 1).getToNode().coordinates).add(0f, halfHeight, 0f);
        waypoints.add(tmpV1.cpy());
        // adding one extra waypoint with some (meaningless) offset so this segment could be used to stop steering later
        waypoints.add(tmpV2.set(tmpV1).add(tmpV1).add(1f).cpy());

        if (waypoints.size > 0) { followPath.createPath(waypoints); }
        else { outPath = null; }
    }

    public void addFollowPathSegmentsToRenderer(HGImmediateModeRenderer20 imr, Color clr1, Color clr2) {
        if (outPath == null) { return; }
        LinePath.Segment<Vector3> segment;
        for (int i = 0; i < followPath.getSegments().size - 1; i++) { // size - 1: skipping the last auxiliary segment
            segment = followPath.getSegments().get(i);
            imr.line(segment.getBegin(), segment.getEnd(), clr1, clr2);
        }
    }

    // Constructors
    public SteerableModelInstance(Model model, float mass, ShapesEnum shape) { this(new HGModel(model), null, mass, shape, (String[])null); }
    public SteerableModelInstance(Model model, float mass, ShapesEnum shape, String... rootNodeIds) { this(new HGModel(model), null, mass, shape, rootNodeIds); }
    public SteerableModelInstance(HGModel hgModel, float mass, ShapesEnum shape) { this(hgModel, null, mass, shape, (String[])null); }
    public SteerableModelInstance(HGModel hgModel, float mass, ShapesEnum shape, String... rootNodeIds) { this(hgModel, null, mass, shape, rootNodeIds); }
    public SteerableModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape) { this(hgModel, assetFL, mass, shape, (String[])null); }
    public SteerableModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape, String... rootNodeIds) {
        super(hgModel, assetFL, mass, shape, rootNodeIds);

        steeringBehaviorOwner = this;

        roaSingleRayConfiguration = new SingleRayConfiguration<>(this, 50f);

        currentSteeringBehavior = ARRIVE;
    }

    // INTERFACE Steerable:
    @Override public Vector3 getLinearVelocity() { return linearVelocity; }
    @Override public float getAngularVelocity() { return angularVelocity; }
    public void setAngularVelocity(float value) { angularVelocity = value; }
    @Override public float getBoundingRadius() { return boundingRadius; }
    public void setBoundingRadius(float value) { boundingRadius = value; }
    @Override public boolean isTagged() { return tagged; }
    @Override public void setTagged(boolean tagged) { this.tagged = tagged; }

    // INTERFACE Limiter:
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

    // INTERFACE Location:
    @Override public Vector3 getPosition() { return position; }
    @Override public float getOrientation() { return orientation; }
    @Override public void setOrientation(float orientation) {
        this.orientation = orientation;
        // With steering set to null and time = 0 no velocities or accelerations are applied.
        // Just Location.position and Location.orientation is being applied to the actual model instance's transform
        // This is a first approach to sync the outside Location manipulation (e.g. from formation slots update)
        // NOTE: it is just a good coincidence that Formation.updateSlots has a sequence of
        //       1. getPosition (-> further Vector3 update by reference)
        //       2. getOrientation
        //       3. setOrientation
        //       Otherwise Position component update wouldn't be taken into account.
        //       TODO: need to figure out the way to have Position update handled independently
        applySteering(null, 0f);
    }
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    @Override public float vectorToAngle(Vector3 vector) { return (float)Math.atan2(-vector.x, vector.z); }
    @Override public Vector3 angleToVector(Vector3 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = 0;
        outVector.z = (float)Math.cos(angle);
        return outVector;
    }
    @Override public Location<Vector3> newLocation() { return null; }
    // INTERFACE FormationMember:
    @Override public Location<Vector3> getTargetLocation() { return this; }

    @Override public void trn(Vector3 vector) { super.trn(vector); syncLocationWithTransform(); }
    @Override public void trn(float x, float y, float z) { super.trn(x, y, z); syncLocationWithTransform(); }
    @Override public void translate(Vector3 translation) { super.translate(translation); syncLocationWithTransform(); }
    @Override public void setTranslation(Vector3 vector) { super.setTranslation(vector); syncLocationWithTransform(); }
    @Override public void setTranslation(float x, float y, float z) { super.setTranslation(x, y, z); syncLocationWithTransform(); }
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
        // TODO: add orientation component
    }

    // Individual Steering Behaviors Setters:
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
    public void setFollowPathArrivalTolerance(float followPathArrivalTolerance) { this.followPathArrivalTolerance = followPathArrivalTolerance; }
    public void setFollowPathDecelerationRadius(float followPathDecelerationRadius) { this.followPathDecelerationRadius = followPathDecelerationRadius; }
    public void setFollowPathTimeToTarget(float followPathTimeToTarget) { this.followPathTimeToTarget = followPathTimeToTarget; }
    public void setFollowPath(LinePath<Vector3> followPath) { this.followPath = followPath; }
    public void setFollowPathOffset(float followPathOffset) { this.followPathOffset = followPathOffset; }
    public void setFollowPathParam(LinePath.LinePathParam followPathParam) { this.followPathParam = followPathParam; }
    public void setFollowPathParamSegmentIndex(int followPathParamSegmentIndex) { this.followPathParamSegmentIndex = followPathParamSegmentIndex; }
    public void setFollowPathParamDistance(float followPathParamDistance) { this.followPathParamDistance = followPathParamDistance; }
    public void setFollowPathArriveEnabled(boolean followPathArriveEnabled) { this.followPathArriveEnabled = followPathArriveEnabled; }
    public void setFollowPathPredictionTime(float followPathPredictionTime) { this.followPathPredictionTime = followPathPredictionTime; }
    public void setInterposeArrivalTolerance(float interposeArrivalTolerance) { this.interposeArrivalTolerance = interposeArrivalTolerance; }
    public void setInterposeDecelerationRadius(float interposeDecelerationRadius) { this.interposeDecelerationRadius = interposeDecelerationRadius; }
    public void setInterposeTimeToTarget(float interposeTimeToTarget) { this.interposeTimeToTarget = interposeTimeToTarget; }
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
    // Group Steering Behaviors Setters:
    public void setHideDistanceFromBoundary(float hideDistanceFromBoundary) { this.hideDistanceFromBoundary = hideDistanceFromBoundary; }
    public void setHideArrivalTolerance(float hideArrivalTolerance) { this.hideArrivalTolerance = hideArrivalTolerance; }
    public void setHideDecelerationRadius(float hideDecelerationRadius) { this.hideDecelerationRadius = hideDecelerationRadius; }
    public void setHideTimeToTarget(float hideTimeToTarget) { this.hideTimeToTarget = hideTimeToTarget; }
    public void setRoaSingleRayConfigurationLength(float roaSingleRayConfigurationLength) { this.roaSingleRayConfigurationLength = roaSingleRayConfigurationLength; }
    public void setRoaDistanceFromBoundary(float roaDistanceFromBoundary) { this.roaDistanceFromBoundary = roaDistanceFromBoundary; }
    public void setSeparationDecayCoefficient(float separationDecayCoefficient) { this.separationDecayCoefficient = separationDecayCoefficient; }

    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    public void update (float delta) {
        if (!steeringEnabled) { return; }

        steeringAcceleration.setZero();

        if (outPath != null) {
            // COMBINING STEERING BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#combining-steering-behaviors
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#blended-steering
            HGBlendedSteering blendedSteering = (HGBlendedSteering) BLENDED_STEERING.getInstance();
            SteeringBehaviorsVector3Enum.initSteeringBehavior(blendedSteering,
                    steeringBehaviorOwner,
                    steeringBehaviorOwner,
                    steeringEnabled);
            // IMPORTANT: the BlendedSteering.list should be cleared
            SteeringBehaviorsVector3EnumCombining.initBlendedSteering(blendedSteering);

            // Follow Path
            HG3DFollowLinePath followLinePath = (HG3DFollowLinePath) FOLLOW_PATH.getInstance();
            SteeringBehaviorsVector3Enum.initSteeringBehavior(followLinePath,
                    steeringBehaviorOwner,
                    steeringBehaviorOwner,
                    steeringEnabled);
            SteeringBehaviorsVector3Enum.initArrive(followLinePath,
                    null,
                    followPathArrivalTolerance,
                    followPathDecelerationRadius,
                    followPathTimeToTarget);
            SteeringBehaviorsVector3Enum.initFollowPath(followLinePath,
                    followPath,
                    followPathPredictionTime,
                    followPathArriveEnabled,
                    followPathOffset,
                    followPathParam);

            // Look Where You Are Going
            LookWhereYouAreGoing<Vector3> lookWhereYouAreGoing = (LookWhereYouAreGoing<Vector3>) LOOK_WHERE_YOU_ARE_GOING.getInstance();
            SteeringBehaviorsVector3Enum.initSteeringBehavior(lookWhereYouAreGoing,
                    steeringBehaviorOwner,
                    steeringBehaviorOwner,
                    steeringEnabled);
            SteeringBehaviorsVector3Enum.initReachOrientation(lookWhereYouAreGoing,
                    // LookWhereYouAreGoing calls ReachOrientation's:
                    // reachOrientation (SteeringAcceleration<T> steering, float targetOrientation)
                    // directly bypassing ReachOrientation.calculateRealSteering (SteeringAcceleration<T> steering)
                    // Thus the target is not playing any role, the targetOrientation is calculated from the linear velocity
                    lwyagTarget,
                    lwyagAlignTolerance,
                    lwyagDecelerationRadius,
                    lwyagTimeToTarget);

            // the weights could be taken as 1f since FollowPath and LWYAG produce different
            // types of acceleration: FollowPath - linear, LWYAG - angular
            blendedSteering.add(followLinePath, 1f);
            blendedSteering.add(lookWhereYouAreGoing, 1f);
            blendedSteering.calculateSteering(steeringAcceleration);

            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#priority-steering
            //case PRIORITY_STEERING: break;

            // Apply steering acceleration to move this agent
            applySteering(steeringAcceleration, delta);

            followPathParamSegmentIndex = followPathParam.getSegmentIndex();
            followPathParamDistance = followPathParam.getDistance();

            // Using the last auxiliary segment to stop the steering.
            // The check below will stop steering right at the beginning of the last extra segment
            if (followPathParamSegmentIndex == followPath.getSegments().size - 1) { setOutPath(null); }

            return;
        }

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
                SteeringBehaviorsVector3Enum.initArrive(followLinePath,
                        null,
                        followPathArrivalTolerance,
                        followPathDecelerationRadius,
                        followPathTimeToTarget);
                SteeringBehaviorsVector3Enum.initFollowPath(followLinePath,
                        followPath,
                        followPathPredictionTime,
                        followPathArriveEnabled,
                        followPathOffset,
                        followPathParam);
                followLinePath.calculateSteering(steeringAcceleration);
                // debug
                followPathInternalTargetPosition.set(followLinePath.getInternalTargetPosition());
                followPathParamSegmentIndex = followPathParam.getSegmentIndex();
                followPathParamDistance = followPathParam.getDistance();
                break;
            case INTERPOSE: // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#interpose
                Interpose<Vector3> interpose = (Interpose<Vector3>) INTERPOSE.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(interpose,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                SteeringBehaviorsVector3Enum.initArrive(interpose,
                        null,
                        interposeArrivalTolerance,
                        interposeDecelerationRadius,
                        interposeTimeToTarget);
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
                jumpDescriptor.takeoffPosition.set(0f, getPosition().y, 0f);
                jumpDescriptor.landingPosition.set(1f, getPosition().y, 1f);
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
            case ALIGNMENT:
                Alignment<Vector3> alignment = (Alignment<Vector3>) ALIGNMENT.getInstance();
                SteeringBehaviorsVector3Enum.initGroupBehavior(alignment,
                        alignmentProximity);
                sb = alignment;
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#cohesion
            case COHESION:
                Cohesion<Vector3> cohesion = (Cohesion<Vector3>) COHESION.getInstance();
                SteeringBehaviorsVector3Enum.initGroupBehavior(cohesion,
                        cohesionProximity);
                sb = cohesion;
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#collision-avoidance
            case COLLISION_AVOIDANCE:
                CollisionAvoidance<Vector3> collisionAvoidance = (CollisionAvoidance<Vector3>) COLLISION_AVOIDANCE.getInstance();
                SteeringBehaviorsVector3Enum.initGroupBehavior(collisionAvoidance,
                        collisionAvoidanceProximity);
                sb = collisionAvoidance;
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#hide
            case HIDE:
                Hide<Vector3> hide = (Hide<Vector3>) HIDE.getInstance();
                SteeringBehaviorsVector3Enum.initArrive(hide,
                        hideHunter,
                        hideArrivalTolerance,
                        hideDecelerationRadius,
                        hideTimeToTarget);
                SteeringBehaviorsVector3Enum.initHide(hide,
                        hideProximity,
                        hideDistanceFromBoundary);
                sb = hide;
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#raycast-obstacle-avoidance
            case RAY_CAST_OBSTACLE_AVOIDANCE:
                RaycastObstacleAvoidance<Vector3> raycastObstacleAvoidance = (RaycastObstacleAvoidance<Vector3>) RAY_CAST_OBSTACLE_AVOIDANCE.getInstance();
                roaSingleRayConfiguration.setLength(roaSingleRayConfigurationLength);
                SteeringBehaviorsVector3Enum.initRaycastObstacleAvoidance(raycastObstacleAvoidance,
                        roaSingleRayConfiguration,
                        roaRaycastCollisionDetector,
                        roaDistanceFromBoundary);
                sb = raycastObstacleAvoidance;
                break;
            // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#separation
            case SEPARATION:
                Separation<Vector3> separation = (Separation<Vector3>) SEPARATION.getInstance();
                SteeringBehaviorsVector3Enum.initSteeringBehavior(separation,
                        steeringBehaviorOwner,
                        steeringBehaviorLimiter,
                        steeringEnabled);
                SteeringBehaviorsVector3Enum.initGroupBehavior(separation,
                        separationProximity);
                SteeringBehaviorsVector3Enum.initSeparation(separation,
                        separationDecayCoefficient);
                separation.calculateSteering(steeringAcceleration);
                steeringAcceleration.linear.y = 0; // cancelling any vertical linear acceleration for now
                break;
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
    private final Quaternion tmpRotation = new Quaternion();
    private final Vector3 tmpScale = new Vector3();
    private final Matrix4 tmpM4 = new Matrix4();

    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    private void applySteering(SteeringAcceleration<Vector3> steering, float time) {
        transform.getScale(tmpScale);
        // Update position and linear velocity. Velocity is trimmed to maximum speed
        this.position.mulAdd(linearVelocity, time);
        this.orientation += angularVelocity * time;
        //Gdx.app.debug("steerable", "steering.linear: " + steering.linear);
        //Gdx.app.debug("steerable", "steering.angular: " + steering.angular);
        if (steering != null) {
            this.linearVelocity.mulAdd(steering.linear, time).limit(this.getMaxLinearSpeed());
            this.angularVelocity += steering.angular * time;
        }
        //Gdx.app.debug("steerable", "linearVelocity: " + this.linearVelocity);
        //Gdx.app.debug("steerable", "angularVelocity: " + this.angularVelocity);

        tmpRotation.setEulerAnglesRad(-this.orientation, 0f, 0f);

        setToTranslation(this.position);
        rotate(tmpRotation);
        scale(tmpScale.x, tmpScale.y, tmpScale.z);
    }
}
