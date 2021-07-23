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

import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.ArrayAsTreeVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class RadiusProximityVisTable extends ContextAwareVisTable {
    public RadiusProximity<Vector3> radiusProximity;
    public ArrayAsTreeVisTable agentsVisTable;
    public FloatVisTable radiusProximityRadius;

    public VisLabel titleL;
    public VisLabel agentsL;

    public RadiusProximityVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        this(new VisLabel("Proximity"), new VisLabel("Agents: "), modelES, stage);
    }
    public RadiusProximityVisTable(VisLabel titleL, VisLabel agentsL, ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        this.titleL = titleL != null ? titleL : new VisLabel("Target");
        this.agentsL = agentsL != null ? agentsL : new VisLabel("Agents: ");

        init();

        add(this.titleL).center();
        add(agentsVisTable.titleL).padRight(5f).right();
        add(agentsVisTable.valueT).expandX().fillX().maxHeight(100f);
        add().row();

        add().center().pad(2f);
        add(radiusProximityRadius.titleL).padRight(5f).right();
        add(radiusProximityRadius.valueT).expandX().fillX();
        add().row();
    }

    public void init() {
        agentsVisTable = new ArrayAsTreeVisTable(null, true, agentsL);
        radiusProximityRadius = new FloatVisTable(true, new VisLabel("Radius: "));
    }

    public RadiusProximityVisTable setProximity(RadiusProximity<Vector3> radiusProximity) {
        this.radiusProximity = radiusProximity;
        if (radiusProximity != null) {
            radiusProximityRadius.setFloat(radiusProximity.getRadius()).setSetter(radiusProximity::setRadius);
            agentsVisTable.setArray(radiusProximity.getAgents());
        } else {
            radiusProximityRadius.setFloat(0f).clearSetter();
        }
        return this;
    }

    @Override
    public void update(float delta) {
        radiusProximityRadius.setFloat(radiusProximity != null ? radiusProximity.getRadius() : 0f);
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);
    }

    @Override
    public void applyLocale() { }
}