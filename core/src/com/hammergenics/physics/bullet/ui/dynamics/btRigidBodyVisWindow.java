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

package com.hammergenics.physics.bullet.ui.dynamics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.IntVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Matrix3VisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Matrix4VisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.QuaternionVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.hammergenics.physics.bullet.dynamics.btRigidBodyProxy;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

public class btRigidBodyVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public btRigidBody rb;
    public btRigidBodyProxy rbp;

    public Vector3VisTable rbAabbMinVisTable;
    public Vector3VisTable rbAabbMaxVisTable;
    public FloatVisTable rbAngularDampingVisTable;
    public Vector3VisTable rbAngularFactorVisTable;
    public FloatVisTable rbAngularSleepingThresholdVisTable;
    public Vector3VisTable rbAngularVelocityVisTable;
    //public btBroadphaseProxy rbBroadphaseProxy;
    //public btBroadphaseProxy rbBroadphaseProxyConst;
    public Vector3VisTable rbCenterOfMassPositionVisTable;
    public Matrix4VisTable rbCenterOfMassTransformVisTable;
    //public btTypedConstraint rbConstraintRef;
    public IntVisTable rbContactSolverTypeVisTable;
    public IntVisTable rbFlagsVisTable;
    public IntVisTable rbFrictionSolverTypeVisTable;
    public Vector3VisTable rbGravityVisTable;
    public Vector3VisTable rbInvInertiaDiagLocalVisTable;
    public Matrix3VisTable rbInvInertiaTensorWorldVisTable;
    public FloatVisTable rbInvMassVisTable;
    public FloatVisTable rbLinearDampingVisTable;
    public Vector3VisTable rbLinearFactorVisTable;
    public FloatVisTable rbLinearSleepingThresholdVisTable;
    public Vector3VisTable rbLinearVelocityVisTable;
    public Vector3VisTable rbLocalInertiaVisTable;
    public IntVisTable rbNumConstraintRefsVisTable;
    public QuaternionVisTable rbOrientationVisTable;
    public Vector3VisTable rbTotalForceVisTable;
    public Vector3VisTable rbTotalTorqueVisTable;
    public Vector3VisTable rbVelocityInLocalPointVisTable;
    public BooleanVisTable isInWorldVisTable;
    public BooleanVisTable wantsSleepingVisTable;

    public btRigidBodyVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Rigid Body");

        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        init();

        VisTable main = new VisTable();
        main.add().padRight(5f).right();
        main.add(rbAabbMinVisTable.labelsT).expandX().fillX().row();
        main.add(rbAabbMinVisTable.titleL).padRight(5f).right();
        main.add(rbAabbMinVisTable.valueT).expandX().fillX().row();
        main.add(rbAabbMaxVisTable.titleL).padRight(5f).right();
        main.add(rbAabbMaxVisTable.valueT).expandX().fillX().row();
        main.add(rbAngularDampingVisTable.titleL).padRight(5f).right();
        main.add(rbAngularDampingVisTable.valueT).expandX().fillX().row();
        main.add(rbAngularFactorVisTable.titleL).padRight(5f).right();
        main.add(rbAngularFactorVisTable.valueT).expandX().fillX().row();
        main.add(rbAngularSleepingThresholdVisTable.titleL).padRight(5f).right();
        main.add(rbAngularSleepingThresholdVisTable.valueT).expandX().fillX().row();
        main.add(rbAngularVelocityVisTable.titleL).padRight(5f).right();
        main.add(rbAngularVelocityVisTable.valueT).expandX().fillX().row();
        //public btBroadphaseProxy rbBroadphaseProxy;
        //public btBroadphaseProxy rbBroadphaseProxyConst;
        main.add(rbCenterOfMassPositionVisTable.titleL).padRight(5f).right();
        main.add(rbCenterOfMassPositionVisTable.valueT).expandX().fillX().row();
        main.add(rbCenterOfMassTransformVisTable.titleL).padRight(5f).right();
        main.add(rbCenterOfMassTransformVisTable.valueT).expandX().fillX().row();
        //public btTypedConstraint rbConstraintRef;
        main.add(rbContactSolverTypeVisTable.titleL).padRight(5f).right();
        main.add(rbContactSolverTypeVisTable.valueT).expandX().fillX().row();
        main.add(rbFlagsVisTable.titleL).padRight(5f).right();
        main.add(rbFlagsVisTable.valueT).expandX().fillX().row();
        main.add(rbFrictionSolverTypeVisTable.titleL).padRight(5f).right();
        main.add(rbFrictionSolverTypeVisTable.valueT).expandX().fillX().row();
        main.add(rbGravityVisTable.titleL).padRight(5f).right();
        main.add(rbGravityVisTable.valueT).expandX().fillX().row();
        main.add(rbInvInertiaDiagLocalVisTable.titleL).padRight(5f).right();
        main.add(rbInvInertiaDiagLocalVisTable.valueT).expandX().fillX().row();
        main.add(rbInvInertiaTensorWorldVisTable.titleL).padRight(5f).right();
        main.add(rbInvInertiaTensorWorldVisTable.valueT).expandX().fillX().row();
        main.add(rbInvMassVisTable.titleL).padRight(5f).right();
        main.add(rbInvMassVisTable.valueT).expandX().fillX().row();
        main.add(rbLinearDampingVisTable.titleL).padRight(5f).right();
        main.add(rbLinearDampingVisTable.valueT).expandX().fillX().row();
        main.add(rbLinearFactorVisTable.titleL).padRight(5f).right();
        main.add(rbLinearFactorVisTable.valueT).expandX().fillX().row();
        main.add(rbLinearSleepingThresholdVisTable.titleL).padRight(5f).right();
        main.add(rbLinearSleepingThresholdVisTable.valueT).expandX().fillX().row();
        main.add(rbLinearVelocityVisTable.titleL).padRight(5f).right();
        main.add(rbLinearVelocityVisTable.valueT).expandX().fillX().row();
        main.add(rbLocalInertiaVisTable.titleL).padRight(5f).right();
        main.add(rbLocalInertiaVisTable.valueT).expandX().fillX().row();
        main.add(rbNumConstraintRefsVisTable.titleL).padRight(5f).right();
        main.add(rbNumConstraintRefsVisTable.valueT).expandX().fillX().row();
        main.add(rbOrientationVisTable.titleL).padRight(5f).right();
        main.add(rbOrientationVisTable.valueT).expandX().fillX().row();
        main.add(rbTotalForceVisTable.titleL).padRight(5f).right();
        main.add(rbTotalForceVisTable.valueT).expandX().fillX().row();
        main.add(rbTotalTorqueVisTable.titleL).padRight(5f).right();
        main.add(rbTotalTorqueVisTable.valueT).expandX().fillX().row();
        main.add(rbVelocityInLocalPointVisTable.titleL).padRight(5f).right();
        main.add(rbVelocityInLocalPointVisTable.valueT).expandX().fillX().row();
        main.add(isInWorldVisTable.titleL).padRight(5f).right();
        main.add(isInWorldVisTable.valueT).expandX().fillX().row();
        main.add(wantsSleepingVisTable.titleL).padRight(5f).right();
        main.add(wantsSleepingVisTable.valueT).expandX().fillX().row();
        add(main).expandX().center();
    }

    public void init() {
        rbAabbMinVisTable = new Vector3VisTable(false, true, true, new VisLabel("AABB min: "));
        rbAabbMaxVisTable = new Vector3VisTable(false, true, false, new VisLabel("AABB max: "));
        rbAngularDampingVisTable = new FloatVisTable(true, new VisLabel("Angular Damping: "));
        rbAngularFactorVisTable = new Vector3VisTable(false, true, false, new VisLabel("Angular Factor: "));
        rbAngularSleepingThresholdVisTable = new FloatVisTable(true, new VisLabel("Angular Sleeping Threshold: "));
        rbAngularVelocityVisTable = new Vector3VisTable(false, true, false, new VisLabel("Angular Velocity: "));
        //public btBroadphaseProxy rbBroadphaseProxy;
        //public btBroadphaseProxy rbBroadphaseProxyConst;
        rbCenterOfMassPositionVisTable = new Vector3VisTable(false, true, false, new VisLabel("Center Of Mass Position: "));
        rbCenterOfMassTransformVisTable = new Matrix4VisTable(true, new VisLabel("Center Of Mass Transform: "));
        //public btTypedConstraint rbConstraintRef;
        rbContactSolverTypeVisTable = new IntVisTable(true, new VisLabel("Contact Solver Type: "));
        rbFlagsVisTable = new IntVisTable(true, new VisLabel("Flags: "));
        rbFrictionSolverTypeVisTable = new IntVisTable(true, new VisLabel("Friction Solver Type: "));
        rbGravityVisTable = new Vector3VisTable(false, true, false, new VisLabel("Gravity: "));
        rbInvInertiaDiagLocalVisTable = new Vector3VisTable(false, true, false, new VisLabel("Inv Inertia Diag Local: "));
        rbInvInertiaTensorWorldVisTable = new Matrix3VisTable(true, new VisLabel("Inv Inertia Tensor World: "));
        rbInvMassVisTable = new FloatVisTable(true, new VisLabel("Inv Mass: "));
        rbLinearDampingVisTable = new FloatVisTable(true, new VisLabel("Linear Damping: "));
        rbLinearFactorVisTable = new Vector3VisTable(false, true, false, new VisLabel("Linear Factor: "));
        rbLinearSleepingThresholdVisTable = new FloatVisTable(true, new VisLabel("Linear Sleeping Threshold: "));
        rbLinearVelocityVisTable = new Vector3VisTable(false, true, false, new VisLabel("Linear Velocity: "));
        rbLocalInertiaVisTable = new Vector3VisTable(false, true, false, new VisLabel("Local Inertia: "));
        rbNumConstraintRefsVisTable = new IntVisTable(true, new VisLabel("Num Constraint Refs: "));
        rbOrientationVisTable = new QuaternionVisTable(false, true, false, new VisLabel("Orientation: "));
        rbTotalForceVisTable = new Vector3VisTable(false, true, false, new VisLabel("Total Force: "));
        rbTotalTorqueVisTable = new Vector3VisTable(false, true, false, new VisLabel("Total Torque: "));
        rbVelocityInLocalPointVisTable = new Vector3VisTable(false, true, false, new VisLabel("Velocity In Local Point: "));
        isInWorldVisTable = new BooleanVisTable(false, true, new VisLabel("Is In World: "));
        wantsSleepingVisTable = new BooleanVisTable(false, true, new VisLabel("Wants Sleeping: "));;
    }

    public void updateRigidBody(EditableModelInstance mi) {
        rb = null;
        rbp = null;
        if (mi != null && mi.rigidBody != null) {
            rb = mi.rigidBody;
            if (mi.rigidBodyProxy == null) {
                mi.rigidBodyProxy = new btRigidBodyProxy(mi.rigidBody);
            }
            rbp = mi.rigidBodyProxy;
            rbp.setInstance(rb); // reassuring that rigid body is legitimate
            rbp.update();

            rbAabbMinVisTable.setVector3(rbp.rbAabbMin);
            rbAabbMaxVisTable.setVector3(rbp.rbAabbMax);
            rbAngularDampingVisTable.setFloat(rbp.rbAngularDamping);
            rbAngularFactorVisTable.setVector3(rbp.rbAngularFactor);
            rbAngularSleepingThresholdVisTable.setFloat(rbp.rbAngularSleepingThreshold);
            rbAngularVelocityVisTable.setVector3(rbp.rbAngularVelocity);
            //public btBroadphaseProxy rbBroadphaseProxy;
            //public btBroadphaseProxy rbBroadphaseProxyConst;
            rbCenterOfMassPositionVisTable.setVector3(rbp.rbCenterOfMassPosition);
            rbCenterOfMassTransformVisTable.setMatrix4(rbp.rbCenterOfMassTransform);
            //public btTypedConstraint rbConstraintRef;
            rbContactSolverTypeVisTable.setInt(rbp.rbContactSolverType);
            rbFlagsVisTable.setInt(rbp.rbFlags);
            rbFrictionSolverTypeVisTable.setInt(rbp.rbFrictionSolverType);
            rbGravityVisTable.setVector3(rbp.rbGravity);
            rbInvInertiaDiagLocalVisTable.setVector3(rbp.rbInvInertiaDiagLocal);
            rbInvInertiaTensorWorldVisTable.setMatrix3(rbp.rbInvInertiaTensorWorld);
            rbInvMassVisTable.setFloat(rbp.rbInvMass);
            rbLinearDampingVisTable.setFloat(rbp.rbLinearDamping);
            rbLinearFactorVisTable.setVector3(rbp.rbLinearFactor);
            rbLinearSleepingThresholdVisTable.setFloat(rbp.rbLinearSleepingThreshold);
            rbLinearVelocityVisTable.setVector3(rbp.rbLinearVelocity);
            rbLocalInertiaVisTable.setVector3(rbp.rbLocalInertia);
            rbNumConstraintRefsVisTable.setInt(rbp.rbNumConstraintRefs);
            rbOrientationVisTable.setQuaternion(rbp.rbOrientation);
            rbTotalForceVisTable.setVector3(rbp.rbTotalForce);
            rbTotalTorqueVisTable.setVector3(rbp.rbTotalTorque);
            rbVelocityInLocalPointVisTable.setVector3(rbp.rbVelocityInLocalPoint);
            isInWorldVisTable.setBoolean(rbp.isInWorld);
            wantsSleepingVisTable.setBoolean(rbp.wantsSleeping);
        } else {
            rbAabbMinVisTable.setVector3(null);
            rbAabbMaxVisTable.setVector3(null);
            rbAngularDampingVisTable.setFloat(0f);
            rbAngularFactorVisTable.setVector3(null);
            rbAngularSleepingThresholdVisTable.setFloat(0f);
            rbAngularVelocityVisTable.setVector3(null);
            //public btBroadphaseProxy rbBroadphaseProxy;
            //public btBroadphaseProxy rbBroadphaseProxyConst;
            rbCenterOfMassPositionVisTable.setVector3(null);
            rbCenterOfMassTransformVisTable.setMatrix4(null);
            //public btTypedConstraint rbConstraintRef;
            rbContactSolverTypeVisTable.setInt(0);
            rbFlagsVisTable.setInt(0);
            rbFrictionSolverTypeVisTable.setInt(0);
            rbGravityVisTable.setVector3(null);
            rbInvInertiaDiagLocalVisTable.setVector3(null);
            rbInvInertiaTensorWorldVisTable.setMatrix3(null);
            rbInvMassVisTable.setFloat(0f);
            rbLinearDampingVisTable.setFloat(0f);
            rbLinearFactorVisTable.setVector3(null);
            rbLinearSleepingThresholdVisTable.setFloat(0f);
            rbLinearVelocityVisTable.setVector3(null);
            rbLocalInertiaVisTable.setVector3(null);
            rbNumConstraintRefsVisTable.setInt(0);
            rbOrientationVisTable.setQuaternion(null);
            rbTotalForceVisTable.setVector3(null);
            rbTotalTorqueVisTable.setVector3(null);
            rbVelocityInLocalPointVisTable.setVector3(null);
            isInWorldVisTable.setBoolean(false);
            wantsSleepingVisTable.setBoolean(false);
        }
    }
}