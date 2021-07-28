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

package com.hammergenics.core.stages.ui.ai.utils;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
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
public class Location3DVisTable extends ContextAwareVisTable {
    public Location<Vector3> location;
    public Vector3VisTable locationPosition;
    public FloatVisTable locationOrientation;

    public VisLabel titleL;

    public Location3DVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        this(new VisLabel("Location"), modelES, stage);
    }
    public Location3DVisTable(VisLabel titleL, ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        this.titleL = titleL != null ? titleL : new VisLabel("Location");

        init();

        add(this.titleL).center();
        add(locationPosition.titleL).padRight(5f).right();
        add(locationPosition.valueT).expandX().fillX();
        add().row();

        add().center().pad(2f);
        add(locationOrientation.titleL).padRight(5f).right();
        add(locationOrientation.valueT).expandX().fillX();
        add().row();
    }

    public void init() {
        locationPosition = new Vector3VisTable(false, true, true, new VisLabel("Position: "));
        locationOrientation = new FloatVisTable(true, new VisLabel("Orientation: "));
    }

    public Location3DVisTable setLocation(Location<Vector3> location) {
        this.location = location;
        if (location != null) {
            locationPosition.setVector3(location.getPosition());
            locationOrientation.setFloat(location.getOrientation()).setSetter(location::setOrientation);
        } else {
            locationPosition.setVector3(null);
            locationOrientation.setFloat(0f).clearSetter();
        }
        return this;
    }

    @Override
    public void update(float delta) {
        locationPosition.update();
        locationOrientation.setFloat(location != null ? location.getOrientation() : 0f);
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}