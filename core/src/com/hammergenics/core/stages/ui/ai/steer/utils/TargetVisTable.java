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

package com.hammergenics.core.stages.ui.ai.steer.utils;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TargetVisTable extends ContextAwareVisTable {
    public Location<Vector3> target;
    public Vector3VisTable targetPosition;
    public FloatVisTable targetOrientation;

    public VisLabel titleL;

    public TargetVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        this(new VisLabel("Target"), modelES, stage);
    }
    public TargetVisTable(VisLabel titleL, ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        this.titleL = titleL != null ? titleL : new VisLabel("Target");

        init();

        add(this.titleL).center();
        add(targetPosition.titleL).padRight(5f).right();
        add(targetPosition.valueT).expandX().fillX();
        add().row();

        add().center().pad(2f);
        add(targetOrientation.titleL).padRight(5f).right();
        add(targetOrientation.valueT).expandX().fillX();
        add().row();
    }

    public void init() {
        targetPosition = new Vector3VisTable(false, true, true, new VisLabel("Position: "));
        targetOrientation = new FloatVisTable(true, new VisLabel("Orientation: "));
    }

    public TargetVisTable setTarget(Location<Vector3> target) {
        this.target = target;
        if (target != null) {
            targetPosition.setVector3(target.getPosition());
            targetOrientation.setFloat(target.getOrientation()).setSetter(target::setOrientation);
        } else {
            targetPosition.setVector3(null);
            targetOrientation.setFloat(0f).clearSetter();
        }
        return this;
    }

    @Override
    public void update(float delta) {
        targetPosition.update();
        targetOrientation.setFloat(target != null ? target.getOrientation() : 0f);
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);
    }

    @Override
    public void applyLocale() { }
}