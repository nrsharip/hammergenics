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

package com.hammergenics.core.stages.ui.ai;

import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.auxiliary.types.BooleanVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

public class SteeringVisTable extends VisTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public VisCheckBox steerCheckBox;

    public Vector3VisTable linearVelocityVisTable;
    public FloatVisTable angularVelocityVisTable;
    public FloatVisTable boundingRadiusVisTable;
    public BooleanVisTable taggedVisTable;

    public FloatVisTable zeroLinearSpeedThresholdVisTable;
    public FloatVisTable maxLinearSpeedVisTable;
    public FloatVisTable maxLinearAccelerationVisTable;
    public FloatVisTable maxAngularSpeedVisTable;
    public FloatVisTable maxAngularAccelerationVisTable;

    public Vector3VisTable positionVisTable;
    public FloatVisTable orientationVisTable;

    public SteeringVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        init();

        add(steerCheckBox).padRight(5f).right();
        add(linearVelocityVisTable.labelsT).expandX().fillX().row();
        add(linearVelocityVisTable.titleL).padRight(5f).right();
        add(linearVelocityVisTable.valueT).expandX().fillX().row();
        add(angularVelocityVisTable.titleL).padRight(5f).right();
        add(angularVelocityVisTable.valueT).expandX().fillX().row();
        add(boundingRadiusVisTable.titleL).padRight(5f).right();
        add(boundingRadiusVisTable.valueT).expandX().fillX().row();
        add(taggedVisTable.titleL).padRight(5f).right();
        add(taggedVisTable.valueT).expandX().fillX().row();

        add(zeroLinearSpeedThresholdVisTable.titleL).padRight(5f).right();
        add(zeroLinearSpeedThresholdVisTable.valueT).expandX().fillX().row();
        add(maxLinearSpeedVisTable.titleL).padRight(5f).right();
        add(maxLinearSpeedVisTable.valueT).expandX().fillX().row();
        add(maxLinearAccelerationVisTable.titleL).padRight(5f).right();
        add(maxLinearAccelerationVisTable.valueT).expandX().fillX().row();
        add(maxAngularSpeedVisTable.titleL).padRight(5f).right();
        add(maxAngularSpeedVisTable.valueT).expandX().fillX().row();
        add(maxAngularAccelerationVisTable.titleL).padRight(5f).right();
        add(maxAngularAccelerationVisTable.valueT).expandX().fillX().row();

        add().padRight(5f).right();
        add(positionVisTable.labelsT).expandX().fillX().row();
        add(positionVisTable.titleL).padRight(5f).right();
        add(positionVisTable.valueT).expandX().fillX().row();
        add(orientationVisTable.titleL).padRight(5f).right();
        add(orientationVisTable.valueT).expandX().fillX().row();
    }

    public void init() {
        steerCheckBox = new VisCheckBox("enable steering");
        steerCheckBox.setChecked(false);

        linearVelocityVisTable = new Vector3VisTable(false, true, true, new VisLabel("Linear Velocity: "));
        angularVelocityVisTable = new FloatVisTable(true, new VisLabel("Angular Velocity: "));
        boundingRadiusVisTable = new FloatVisTable(true, new VisLabel("Bounding Radius: "));
        taggedVisTable = new BooleanVisTable(false, true, new VisLabel("Tagged: "));

        zeroLinearSpeedThresholdVisTable = new FloatVisTable(true, new VisLabel("Zero Linear Speed Threshold: "));
        maxLinearSpeedVisTable = new FloatVisTable(true, new VisLabel("Max Linear Speed: "));
        maxLinearAccelerationVisTable = new FloatVisTable(true, new VisLabel("Max Linear Acceleration: "));
        maxAngularSpeedVisTable = new FloatVisTable(true, new VisLabel("Max Angular Speed: "));
        maxAngularAccelerationVisTable = new FloatVisTable(true, new VisLabel("Max Angular Acceleration: "));

        positionVisTable = new Vector3VisTable(false, true, true, new VisLabel("Position: "));
        orientationVisTable = new FloatVisTable(true, new VisLabel("Orientation: "));
    }

    public void updateSteerable(EditableModelInstance mi) {
        if (mi != null) {
            linearVelocityVisTable.setVector3(mi.linearVelocity);
            angularVelocityVisTable.setFloat(mi.angularVelocity);
            boundingRadiusVisTable.setFloat(mi.boundingRadius);
            taggedVisTable.setBoolean(mi.tagged);

            zeroLinearSpeedThresholdVisTable.setFloat(mi.zeroLinearSpeedThreshold);
            maxLinearSpeedVisTable.setFloat(mi.maxLinearSpeed);
            maxLinearAccelerationVisTable.setFloat(mi.maxLinearAcceleration);
            maxAngularSpeedVisTable.setFloat(mi.maxAngularSpeed);
            maxAngularAccelerationVisTable.setFloat(mi.maxAngularAcceleration);

            positionVisTable.setVector3(mi.position);
            orientationVisTable.setFloat(mi.orientation);
        } else {
            linearVelocityVisTable.setVector3(null);
            angularVelocityVisTable.setFloat(0f);
            boundingRadiusVisTable.setFloat(0f);
            taggedVisTable.setBoolean(false);

            zeroLinearSpeedThresholdVisTable.setFloat(0f);
            maxLinearSpeedVisTable.setFloat(0f);
            maxLinearAccelerationVisTable.setFloat(0f);
            maxAngularSpeedVisTable.setFloat(0f);
            maxAngularAccelerationVisTable.setFloat(0f);

            positionVisTable.setVector3(null);
            orientationVisTable.setFloat(0f);
        }
    }
}