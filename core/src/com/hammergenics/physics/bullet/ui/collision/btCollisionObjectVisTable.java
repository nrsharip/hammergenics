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

package com.hammergenics.physics.bullet.ui.collision;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.IntVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Matrix4VisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.hammergenics.physics.bullet.collision.btCollisionObjectProxy;
import com.hammergenics.physics.bullet.dynamics.btRigidBodyProxy;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

public class btCollisionObjectVisTable extends VisTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public btCollisionObject co;
    public btCollisionObjectProxy cop;

    public IntVisTable coActivationStateVisTable;
    public Vector3VisTable coAnisotropicFrictionVisTable;
    public FloatVisTable coCcdMotionThresholdVisTable;
    public FloatVisTable coCcdSquareMotionThresholdVisTable;
    public FloatVisTable coCcdSweptSphereRadiusVisTable;
    public IntVisTable coCollisionFlagsVisTable;
    //public btCollisionShape coCollisionShape;
    public IntVisTable coCompanionIdVisTable;
    public IntVisTable coContactCallbackFilterVisTable;
    public IntVisTable coContactCallbackFlagVisTable;
    public FloatVisTable coContactDampingVisTable;
    public FloatVisTable coContactProcessingThresholdVisTable;
    public FloatVisTable coContactStiffnessVisTable;
    public Vector3VisTable coCustomDebugColorVisTable;
    public FloatVisTable coDeactivationTimeVisTable;
    public FloatVisTable coFrictionVisTable;
    public FloatVisTable coHitFractionVisTable;
    public IntVisTable coInternalTypeVisTable;
    public Vector3VisTable coInterpolationAngularVelocityVisTable;
    public Vector3VisTable coInterpolationLinearVelocityVisTable;
    public Matrix4VisTable coInterpolationWorldTransformVisTable;
    public IntVisTable coIslandTagVisTable;
    public FloatVisTable coRestitutionVisTable;
    public FloatVisTable coRollingFrictionVisTable;
    public FloatVisTable coSpinningFrictionVisTable;
    public IntVisTable coUpdateRevisionInternalVisTable;
    public IntVisTable coUserIndexVisTable;
    public IntVisTable coUserIndex2VisTable;
    //public long coUserPointer;
    public IntVisTable coUserValueVisTable;
    public IntVisTable coWorldArrayIndexVisTable;
    public Matrix4VisTable coWorldTransformVisTable;
    public BooleanVisTable hasAnisotropicFrictionVisTable;
    public BooleanVisTable hasContactResponseVisTable;
    public BooleanVisTable isActiveVisTable;
    public BooleanVisTable isKinematicObjectVisTable;
    public BooleanVisTable isStaticObjectVisTable;
    public BooleanVisTable isStaticOrKinematicObjectVisTable;
    public BooleanVisTable mergesSimulationIslandsVisTable;

    public btCollisionObjectVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        init();

        add(coActivationStateVisTable.titleL).padRight(5f).right();
        add(coActivationStateVisTable.valueT).expandX().fillX().row();
        add(coAnisotropicFrictionVisTable.titleL).padRight(5f).right();
        add(coAnisotropicFrictionVisTable.valueT).expandX().fillX().row();
        add(coCcdMotionThresholdVisTable.titleL).padRight(5f).right();
        add(coCcdMotionThresholdVisTable.valueT).expandX().fillX().row();
        add(coCcdSquareMotionThresholdVisTable.titleL).padRight(5f).right();
        add(coCcdSquareMotionThresholdVisTable.valueT).expandX().fillX().row();
        add(coCcdSweptSphereRadiusVisTable.titleL).padRight(5f).right();
        add(coCcdSweptSphereRadiusVisTable.valueT).expandX().fillX().row();
        add(coCollisionFlagsVisTable.titleL).padRight(5f).right();
        add(coCollisionFlagsVisTable.valueT).expandX().fillX().row();
        //coCollisionShape;
        add(coCompanionIdVisTable.titleL).padRight(5f).right();
        add(coCompanionIdVisTable.valueT).expandX().fillX().row();
        add(coContactCallbackFilterVisTable.titleL).padRight(5f).right();
        add(coContactCallbackFilterVisTable.valueT).expandX().fillX().row();
        add(coContactCallbackFlagVisTable.titleL).padRight(5f).right();
        add(coContactCallbackFlagVisTable.valueT).expandX().fillX().row();
        add(coContactDampingVisTable.titleL).padRight(5f).right();
        add(coContactDampingVisTable.valueT).expandX().fillX().row();
        add(coContactProcessingThresholdVisTable.titleL).padRight(5f).right();
        add(coContactProcessingThresholdVisTable.valueT).expandX().fillX().row();
        add(coContactStiffnessVisTable.titleL).padRight(5f).right();
        add(coContactStiffnessVisTable.valueT).expandX().fillX().row();
        add(coCustomDebugColorVisTable.titleL).padRight(5f).right();
        add(coCustomDebugColorVisTable.valueT).expandX().fillX().row();
        add(coDeactivationTimeVisTable.titleL).padRight(5f).right();
        add(coDeactivationTimeVisTable.valueT).expandX().fillX().row();
        add(coFrictionVisTable.titleL).padRight(5f).right();
        add(coFrictionVisTable.valueT).expandX().fillX().row();
        add(coHitFractionVisTable.titleL).padRight(5f).right();
        add(coHitFractionVisTable.valueT).expandX().fillX().row();
        add(coInternalTypeVisTable.titleL).padRight(5f).right();
        add(coInternalTypeVisTable.valueT).expandX().fillX().row();
        add(coInterpolationAngularVelocityVisTable.titleL).padRight(5f).right();
        add(coInterpolationAngularVelocityVisTable.valueT).expandX().fillX().row();
        add(coInterpolationLinearVelocityVisTable.titleL).padRight(5f).right();
        add(coInterpolationLinearVelocityVisTable.valueT).expandX().fillX().row();
        add(coInterpolationWorldTransformVisTable.titleL).padRight(5f).right();
        add(coInterpolationWorldTransformVisTable.valueT).expandX().fillX().row();
        add(coIslandTagVisTable.titleL).padRight(5f).right();
        add(coIslandTagVisTable.valueT).expandX().fillX().row();
        add(coRestitutionVisTable.titleL).padRight(5f).right();
        add(coRestitutionVisTable.valueT).expandX().fillX().row();
        add(coRollingFrictionVisTable.titleL).padRight(5f).right();
        add(coRollingFrictionVisTable.valueT).expandX().fillX().row();
        add(coSpinningFrictionVisTable.titleL).padRight(5f).right();
        add(coSpinningFrictionVisTable.valueT).expandX().fillX().row();
        add(coUpdateRevisionInternalVisTable.titleL).padRight(5f).right();
        add(coUpdateRevisionInternalVisTable.valueT).expandX().fillX().row();
        add(coUserIndexVisTable.titleL).padRight(5f).right();
        add(coUserIndexVisTable.valueT).expandX().fillX().row();
        add(coUserIndex2VisTable.titleL).padRight(5f).right();
        add(coUserIndex2VisTable.valueT).expandX().fillX().row();
        //coUserPointer;
        add(coUserValueVisTable.titleL).padRight(5f).right();
        add(coUserValueVisTable.valueT).expandX().fillX().row();
        add(coWorldArrayIndexVisTable.titleL).padRight(5f).right();
        add(coWorldArrayIndexVisTable.valueT).expandX().fillX().row();
        add(coWorldTransformVisTable.titleL).padRight(5f).right();
        add(coWorldTransformVisTable.valueT).expandX().fillX().row();
        add(hasAnisotropicFrictionVisTable.titleL).padRight(5f).right();
        add(hasAnisotropicFrictionVisTable.valueT).expandX().fillX().row();
        add(hasContactResponseVisTable.titleL).padRight(5f).right();
        add(hasContactResponseVisTable.valueT).expandX().fillX().row();
        add(isActiveVisTable.titleL).padRight(5f).right();
        add(isActiveVisTable.valueT).expandX().fillX().row();
        add(isKinematicObjectVisTable.titleL).padRight(5f).right();
        add(isKinematicObjectVisTable.valueT).expandX().fillX().row();
        add(isStaticObjectVisTable.titleL).padRight(5f).right();
        add(isStaticObjectVisTable.valueT).expandX().fillX().row();
        add(isStaticOrKinematicObjectVisTable.titleL).padRight(5f).right();
        add(isStaticOrKinematicObjectVisTable.valueT).expandX().fillX().row();
        add(mergesSimulationIslandsVisTable.titleL).padRight(5f).right();
        add(mergesSimulationIslandsVisTable.valueT).expandX().fillX().row();
    }

    public void init() {
        coActivationStateVisTable = new IntVisTable(true, new VisLabel("Activation State: "));
        coAnisotropicFrictionVisTable = new Vector3VisTable(false, true, true, new VisLabel("Anisotropic Friction: "));
        coCcdMotionThresholdVisTable = new FloatVisTable(true, new VisLabel("Ccd Motion Threshold: "));
        coCcdSquareMotionThresholdVisTable = new FloatVisTable(true, new VisLabel("Ccd Square Motion Threshold: "));
        coCcdSweptSphereRadiusVisTable = new FloatVisTable(true, new VisLabel("Ccd Swept Sphere Radius: "));
        coCollisionFlagsVisTable = new IntVisTable(true, new VisLabel("Collision Flags: "));
        coCompanionIdVisTable = new IntVisTable(true, new VisLabel("Companion Id: "));
        coContactCallbackFilterVisTable = new IntVisTable(true, new VisLabel("Contact Callback Filter: "));
        coContactCallbackFlagVisTable = new IntVisTable(true, new VisLabel("Contact Callback Flag: "));
        coContactDampingVisTable = new FloatVisTable(true, new VisLabel("Contact Damping: "));
        coContactProcessingThresholdVisTable = new FloatVisTable(true, new VisLabel("Contact Processing Threshold: "));
        coContactStiffnessVisTable = new FloatVisTable(true, new VisLabel("Contact Stiffness: "));
        coCustomDebugColorVisTable = new Vector3VisTable(false, true, true, new VisLabel("Custom Debug Color: "));
        coDeactivationTimeVisTable = new FloatVisTable(true, new VisLabel("Deactivation Time: "));
        coFrictionVisTable = new FloatVisTable(true, new VisLabel("Friction: "));
        coHitFractionVisTable = new FloatVisTable(true, new VisLabel("Hit Fraction: "));
        coInternalTypeVisTable = new IntVisTable(true, new VisLabel("Internal Type: "));
        coInterpolationAngularVelocityVisTable = new Vector3VisTable(false, true, true, new VisLabel("Interpolation Angular Velocity: "));
        coInterpolationLinearVelocityVisTable = new Vector3VisTable(false, true, true, new VisLabel("Interpolation Linear Velocity: "));
        coInterpolationWorldTransformVisTable = new Matrix4VisTable(true, new VisLabel("Interpolation World Transform: "));
        coIslandTagVisTable = new IntVisTable(true, new VisLabel("Island Tag: "));
        coRestitutionVisTable = new FloatVisTable(true, new VisLabel("Restitution: "));
        coRollingFrictionVisTable = new FloatVisTable(true, new VisLabel("Rolling Friction: "));
        coSpinningFrictionVisTable = new FloatVisTable(true, new VisLabel("Spinning Friction: "));
        coUpdateRevisionInternalVisTable = new IntVisTable(true, new VisLabel("Update Revision Internal: "));
        coUserIndexVisTable = new IntVisTable(true, new VisLabel("User Index: "));
        coUserIndex2VisTable = new IntVisTable(true, new VisLabel("User Index 2: "));
        coUserValueVisTable = new IntVisTable(true, new VisLabel("User Value: "));
        coWorldArrayIndexVisTable = new IntVisTable(true, new VisLabel("World Array Index: "));
        coWorldTransformVisTable = new Matrix4VisTable(true, new VisLabel("World Transform: "));
        hasAnisotropicFrictionVisTable = new BooleanVisTable(false, true, new VisLabel("Has Anisotropic Friction: "));
        hasContactResponseVisTable = new BooleanVisTable(false, true, new VisLabel("Has Contact Response: "));
        isActiveVisTable = new BooleanVisTable(false, true, new VisLabel("Is Active: "));
        isKinematicObjectVisTable = new BooleanVisTable(false, true, new VisLabel("Is Kinematic Object: "));
        isStaticObjectVisTable = new BooleanVisTable(false, true, new VisLabel("Is Static Object: "));
        isStaticOrKinematicObjectVisTable = new BooleanVisTable(false, true, new VisLabel("Is Static Or Kinematic Object: "));
        mergesSimulationIslandsVisTable = new BooleanVisTable(false, true, new VisLabel("Merges Simulation Islands: "));
    }

    public void updateCollisionObject(EditableModelInstance mi) {
        co = null;
        cop = null;
        if (mi != null && mi.rigidBody != null) {
            co = mi.rigidBody;
            if (mi.rigidBodyProxy == null) {
                mi.rigidBodyProxy = new btRigidBodyProxy(mi.rigidBody);
            }
            cop = mi.rigidBodyProxy;
            cop.setInstance(co); // reassuring that collision object is legitimate
            cop.update();

            coActivationStateVisTable.setInt(cop.coActivationState);
            coAnisotropicFrictionVisTable.setVector3(cop.coAnisotropicFriction);
            coCcdMotionThresholdVisTable.setFloat(cop.coCcdMotionThreshold);
            coCcdSquareMotionThresholdVisTable.setFloat(cop.coCcdSquareMotionThreshold);
            coCcdSweptSphereRadiusVisTable.setFloat(cop.coCcdSweptSphereRadius);
            coCollisionFlagsVisTable.setInt(cop.coCollisionFlags);
            //coCollisionShape;
            coCompanionIdVisTable.setInt(cop.coCompanionId);
            coContactCallbackFilterVisTable.setInt(cop.coContactCallbackFilter);
            coContactCallbackFlagVisTable.setInt(cop.coContactCallbackFlag);
            coContactDampingVisTable.setFloat(cop.coContactDamping);
            coContactProcessingThresholdVisTable.setFloat(cop.coContactProcessingThreshold);
            coContactStiffnessVisTable.setFloat(cop.coContactStiffness);
            coCustomDebugColorVisTable.setVector3(cop.coCustomDebugColor);
            coDeactivationTimeVisTable.setFloat(cop.coDeactivationTime);
            coFrictionVisTable.setFloat(cop.coFriction);
            coHitFractionVisTable.setFloat(cop.coHitFraction);
            coInternalTypeVisTable.setInt(cop.coInternalType);
            coInterpolationAngularVelocityVisTable.setVector3(cop.coInterpolationAngularVelocity);
            coInterpolationLinearVelocityVisTable.setVector3(cop.coInterpolationLinearVelocity);
            coInterpolationWorldTransformVisTable.setMatrix4(cop.coInterpolationWorldTransform);
            coIslandTagVisTable.setInt(cop.coIslandTag);
            coRestitutionVisTable.setFloat(cop.coRestitution);
            coRollingFrictionVisTable.setFloat(cop.coRollingFriction);
            coSpinningFrictionVisTable.setFloat(cop.coSpinningFriction);
            coUpdateRevisionInternalVisTable.setInt(cop.coUpdateRevisionInternal);
            coUserIndexVisTable.setInt(cop.coUserIndex);
            coUserIndex2VisTable.setInt(cop.coUserIndex2);
            //coUserPointer;
            coUserValueVisTable.setInt(cop.coUserValue);
            coWorldArrayIndexVisTable.setInt(cop.coWorldArrayIndex);
            coWorldTransformVisTable.setMatrix4(cop.coWorldTransform);
            hasAnisotropicFrictionVisTable.setBoolean(cop.hasAnisotropicFriction);
            hasContactResponseVisTable.setBoolean(cop.hasContactResponse);
            isActiveVisTable.setBoolean(cop.isActive);
            isKinematicObjectVisTable.setBoolean(cop.isKinematicObject);
            isStaticObjectVisTable.setBoolean(cop.isStaticObject);
            isStaticOrKinematicObjectVisTable.setBoolean(cop.isStaticOrKinematicObject);
            mergesSimulationIslandsVisTable.setBoolean(cop.mergesSimulationIslands);
        } else {
            coActivationStateVisTable.setInt(0);
            coAnisotropicFrictionVisTable.setVector3(null);
            coCcdMotionThresholdVisTable.setFloat(0f);
            coCcdSquareMotionThresholdVisTable.setFloat(0f);
            coCcdSweptSphereRadiusVisTable.setFloat(0f);
            coCollisionFlagsVisTable.setInt(0);
            //coCollisionShape;
            coCompanionIdVisTable.setInt(0);
            coContactCallbackFilterVisTable.setInt(0);
            coContactCallbackFlagVisTable.setInt(0);
            coContactDampingVisTable.setFloat(0f);
            coContactProcessingThresholdVisTable.setFloat(0f);
            coContactStiffnessVisTable.setFloat(0f);
            coCustomDebugColorVisTable.setVector3(null);
            coDeactivationTimeVisTable.setFloat(0f);
            coFrictionVisTable.setFloat(0f);
            coHitFractionVisTable.setFloat(0f);
            coInternalTypeVisTable.setInt(0);
            coInterpolationAngularVelocityVisTable.setVector3(null);
            coInterpolationLinearVelocityVisTable.setVector3(null);
            coInterpolationWorldTransformVisTable.setMatrix4(null);
            coIslandTagVisTable.setInt(0);
            coRestitutionVisTable.setFloat(0f);
            coRollingFrictionVisTable.setFloat(0f);
            coSpinningFrictionVisTable.setFloat(0f);
            coUpdateRevisionInternalVisTable.setInt(0);
            coUserIndexVisTable.setInt(0);
            coUserIndex2VisTable.setInt(0);
            //coUserPointer;
            coUserValueVisTable.setInt(0);
            coWorldArrayIndexVisTable.setInt(0);
            coWorldTransformVisTable.setMatrix4(null);
            hasAnisotropicFrictionVisTable.setBoolean(false);
            hasContactResponseVisTable.setBoolean(false);
            isActiveVisTable.setBoolean(false);
            isKinematicObjectVisTable.setBoolean(false);
            isStaticObjectVisTable.setBoolean(false);
            isStaticOrKinematicObjectVisTable.setBoolean(false);
            mergesSimulationIslandsVisTable.setBoolean(false);
        }
    }
}