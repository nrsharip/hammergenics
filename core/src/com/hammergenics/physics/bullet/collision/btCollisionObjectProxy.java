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

package com.hammergenics.physics.bullet.collision;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class btCollisionObjectProxy {
    public btCollisionObject instance;

    public btCollisionObjectProxy() {}

    public btCollisionObjectProxy(btCollisionObject instance) {
        this.instance = instance;
    }

    public void setInstance(btCollisionObject instance) { this.instance = instance; }

    // https://github.com/libgdx/libgdx/blob/024282e47e9b5d8ec25373d3e1e5ddfe55122596/extensions/gdx-bullet/jni/src/bullet/BulletCollision/CollisionDispatch/btCollisionObject.h#L21
    // #define ACTIVE_TAG 1
    // #define ISLAND_SLEEPING 2
    // #define WANTS_DEACTIVATION 3
    // #define DISABLE_DEACTIVATION 4
    // #define DISABLE_SIMULATION 5
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletCollision/CollisionDispatch/btCollisionObject.h#L21
    // #define FIXED_BASE_MULTI_BODY 6
    public enum coActivationStatesEnum {
        ACTIVE_TAG(1),
        ISLAND_SLEEPING(2),
        WANTS_DEACTIVATION(3),
        DISABLE_DEACTIVATION(4),
        DISABLE_SIMULATION(5);
        public final int define;
        coActivationStatesEnum(int define) { this.define = define; }

        public static coActivationStatesEnum findByDefine(int define) {
            for (coActivationStatesEnum as: coActivationStatesEnum.values()) {
                if (as.define == define) { return as; }
            }
            return null;
        }
    }

    public void update() {
        if (instance == null) { return; }

        // hard to predict if the returned references are valid throughout the execution - polling everything for now
        coActivationState = instance.getActivationState ();
        coAnisotropicFriction = instance.getAnisotropicFriction ();
        coCcdMotionThreshold = instance.getCcdMotionThreshold ();
        coCcdSquareMotionThreshold = instance.getCcdSquareMotionThreshold ();
        coCcdSweptSphereRadius = instance.getCcdSweptSphereRadius ();
        coCollisionFlags = instance.getCollisionFlags ();
        coCollisionShape = instance.getCollisionShape ();
        coCompanionId = instance.getCompanionId ();
        coContactCallbackFilter = instance.getContactCallbackFilter ();
        coContactCallbackFlag = instance.getContactCallbackFlag ();
        coContactDamping = instance.getContactDamping ();
        coContactProcessingThreshold = instance.getContactProcessingThreshold ();
        coContactStiffness = instance.getContactStiffness ();
        instance.getCustomDebugColor (coCustomDebugColor);
        coDeactivationTime = instance.getDeactivationTime ();
        coFriction = instance.getFriction ();
        coHitFraction = instance.getHitFraction ();
        coInternalType = instance.getInternalType ();
        coInterpolationAngularVelocity = instance.getInterpolationAngularVelocity ();
        coInterpolationLinearVelocity = instance.getInterpolationLinearVelocity ();
        coInterpolationWorldTransform = instance.getInterpolationWorldTransform ();
        coIslandTag = instance.getIslandTag ();
        coRestitution = instance.getRestitution ();
        coRollingFriction = instance.getRollingFriction ();
        coSpinningFriction = instance.getSpinningFriction ();
        coUpdateRevisionInternal = instance.getUpdateRevisionInternal ();
        coUserIndex = instance.getUserIndex ();
        coUserIndex2 = instance.getUserIndex2 ();
        coUserPointer = instance.getUserPointer ();
        coUserValue = instance.getUserValue ();
        coWorldArrayIndex = instance.getWorldArrayIndex ();
        coWorldTransform = instance.getWorldTransform ();
        hasAnisotropicFriction = instance.hasAnisotropicFriction ();
        hasContactResponse = instance.hasContactResponse ();
        isActive = instance.isActive ();
        isKinematicObject = instance.isKinematicObject ();
        isStaticObject = instance.isStaticObject ();
        isStaticOrKinematicObject = instance.isStaticOrKinematicObject ();
        mergesSimulationIslands = instance.mergesSimulationIslands ();
    }

    public int coActivationState;
    public Vector3 coAnisotropicFriction;
    public float coCcdMotionThreshold;
    public float coCcdSquareMotionThreshold;
    public float coCcdSweptSphereRadius;
    public int coCollisionFlags;
    public btCollisionShape coCollisionShape;
    public int coCompanionId;
    public int coContactCallbackFilter;
    public int coContactCallbackFlag;
    public float coContactDamping;
    public float coContactProcessingThreshold;
    public float coContactStiffness;
    public Vector3 coCustomDebugColor = new Vector3();
    public float coDeactivationTime;
    public float coFriction;
    public float coHitFraction;
    public int coInternalType;
    public Vector3 coInterpolationAngularVelocity;
    public Vector3 coInterpolationLinearVelocity;
    public Matrix4 coInterpolationWorldTransform;
    public int coIslandTag;
    public float coRestitution;
    public float coRollingFriction;
    public float coSpinningFriction;
    public int coUpdateRevisionInternal;
    public int coUserIndex;
    public int coUserIndex2;
    public long coUserPointer;
    public int coUserValue;
    public int coWorldArrayIndex;
    public Matrix4 coWorldTransform;
    public boolean hasAnisotropicFriction;
    public boolean hasContactResponse;
    public boolean isActive;
    public boolean isKinematicObject;
    public boolean isStaticObject;
    public boolean isStaticOrKinematicObject;
    public boolean mergesSimulationIslands;
}