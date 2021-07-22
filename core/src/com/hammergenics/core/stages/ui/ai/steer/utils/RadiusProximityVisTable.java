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

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.HGModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.auxiliary.HGTreeVisTableNode;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class RadiusProximityVisTable extends ContextAwareVisTable {
    public RadiusProximity<Vector3> radiusProximity;
    public VisTree<HGTreeVisTableNode, VisLabel> arrayTree;
    public VisScrollPane arrayTreeScrollPane;
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
        add(this.agentsL).padRight(5f).right();
        add(arrayTreeScrollPane).expandX().fillX().maxHeight(100f);
        add().row();

        add().center().pad(2f);
        add(radiusProximityRadius.titleL).padRight(5f).right();
        add(radiusProximityRadius.valueT).expandX().fillX();
        add().row();
    }

    public void init() {
        arrayTree = new VisTree<>();
        arrayTreeScrollPane = new VisScrollPane(arrayTree);
        radiusProximityRadius = new FloatVisTable(true, new VisLabel("Radius: "));
    }

    public RadiusProximityVisTable setProximity(RadiusProximity<Vector3> radiusProximity) {
        this.radiusProximity = radiusProximity;
        arrayTree.clearChildren();
        if (radiusProximity != null) {
            radiusProximityRadius.setFloat(radiusProximity.getRadius()).setSetter(radiusProximity::setRadius);
            for (Steerable<Vector3> agent: radiusProximity.getAgents()) {
                if (agent instanceof HGModelInstance) {
                    HGModelInstance mi = (HGModelInstance)agent;
                    String name = mi.hgModel.afh != null ? mi.hgModel.afh.nameWithoutExtension() : mi.nodes.get(0).id;
                    arrayTree.add(new HGTreeVisTableNode(new HGTreeVisTableNode.HGTreeVisTable(name)));
                }
            }
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