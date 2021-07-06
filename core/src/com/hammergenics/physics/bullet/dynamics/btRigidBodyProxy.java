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

package com.hammergenics.physics.bullet.dynamics;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.hammergenics.physics.bullet.collision.btCollisionObjectProxy;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class btRigidBodyProxy extends btCollisionObjectProxy {
    public btRigidBody instance;
    public Vector3 localPoint = new Vector3();

    public btRigidBodyProxy() {}

    public btRigidBodyProxy(btRigidBody instance) {
        super(instance);
        this.instance = instance;
    }

    public void setInstance(btRigidBody instance) {
        super.setInstance(instance);
        this.instance = instance;
    }
    
    @Override
    public void update() {
        super.update();
        if (instance == null) { return; }

        // hard to predict if the returned references are valid throughout the execution - polling everything for now
        instance.getAabb (rbAabbMin, rbAabbMax);
        rbAngularDamping = instance.getAngularDamping ();
        rbAngularFactor = instance.getAngularFactor ();
        rbAngularSleepingThreshold = instance.getAngularSleepingThreshold ();
        rbAngularVelocity = instance.getAngularVelocity ();
        rbBroadphaseProxy = instance.getBroadphaseProxy ();
        rbBroadphaseProxyConst = instance.getBroadphaseProxyConst ();
        rbCenterOfMassPosition = instance.getCenterOfMassPosition ();
        rbCenterOfMassTransform = instance.getCenterOfMassTransform ();
        //rbConstraintRef = instance.getConstraintRef (0);
        rbContactSolverType = instance.getContactSolverType ();
        rbFlags = instance.getFlags ();
        rbFrictionSolverType = instance.getFrictionSolverType ();
        rbGravity = instance.getGravity ();
        rbInvInertiaDiagLocal = instance.getInvInertiaDiagLocal ();
        rbInvInertiaTensorWorld = instance.getInvInertiaTensorWorld ();
        rbInvMass = instance.getInvMass ();
        rbLinearDamping = instance.getLinearDamping ();
        rbLinearFactor = instance.getLinearFactor ();
        rbLinearSleepingThreshold = instance.getLinearSleepingThreshold ();
        rbLinearVelocity = instance.getLinearVelocity ();
        rbLocalInertia = instance.getLocalInertia ();
        rbNumConstraintRefs = instance.getNumConstraintRefs ();
        rbOrientation = instance.getOrientation ();
        rbTotalForce = instance.getTotalForce ();
        rbTotalTorque = instance.getTotalTorque ();
        rbVelocityInLocalPoint = instance.getVelocityInLocalPoint (localPoint);
        isInWorld = instance.isInWorld ();
        wantsSleeping = instance.wantsSleeping ();
    }

    public Vector3 rbAabbMin = new Vector3();
    public Vector3 rbAabbMax = new Vector3();
    public float rbAngularDamping;
    public Vector3 rbAngularFactor;
    public float rbAngularSleepingThreshold;
    public Vector3 rbAngularVelocity;
    public btBroadphaseProxy rbBroadphaseProxy;
    public btBroadphaseProxy rbBroadphaseProxyConst;
    public Vector3 rbCenterOfMassPosition;
    public Matrix4 rbCenterOfMassTransform;
    public btTypedConstraint rbConstraintRef;
    public int rbContactSolverType;
    public int rbFlags;
    public int rbFrictionSolverType;
    public Vector3 rbGravity;
    public Vector3 rbInvInertiaDiagLocal;
    public Matrix3 rbInvInertiaTensorWorld;
    public float rbInvMass;
    public float rbLinearDamping;
    public Vector3 rbLinearFactor;
    public float rbLinearSleepingThreshold;
    public Vector3 rbLinearVelocity;
    public Vector3 rbLocalInertia;
    public int rbNumConstraintRefs;
    public Quaternion rbOrientation;
    public Vector3 rbTotalForce;
    public Vector3 rbTotalTorque;
    public Vector3 rbVelocityInLocalPoint = new Vector3();
    public boolean isInWorld;
    public boolean wantsSleeping;
}