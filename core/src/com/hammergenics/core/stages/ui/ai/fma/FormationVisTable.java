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

package com.hammergenics.core.stages.ui.ai.fma;

import com.badlogic.gdx.ai.fma.FormationMember;
import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.ai.fma.FormationPatterns3DEnum;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.ai.steer.utils.Location3DVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.ArrayAsTreeVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.FloatVisTable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;

import static com.hammergenics.ai.fma.FormationPatterns3DEnum.DEFENSIVE_CIRCLE;
import static com.hammergenics.ai.fma.FormationPatterns3DEnum.OFFENSIVE_CIRCLE;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class FormationVisTable extends ContextAwareVisTable {
    public VisSelectBox<FormationPatterns3DEnum> formationPatternSB;
    public Location3DVisTable formationAnchor;
    public ArrayAsTreeVisTable formationMembers;
    public FloatVisTable formationPatternRadiusVisTable;

    public FormationVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        init();

        add(new VisLabel("Formation Patterns: "));
        add(formationPatternSB).expandX().fillX().row();

        add(formationAnchor).colspan(2).row();

        add(formationMembers.titleL).padRight(5f).right();
        add(formationMembers.valueT).expandX().fillX().maxHeight(100f).row();

        add(formationPatternRadiusVisTable.titleL).padRight(5f).right();
        add(formationPatternRadiusVisTable.valueT).left().row();
    }

    public void init() {
        formationPatternSB = new VisSelectBox<>();

        FormationPatterns3DEnum currentPattern = FormationPatterns3DEnum.getByInstance(eng.formation.getPattern());
        formationPatternSB.getSelection().setProgrammaticChangeEvents(false);
        formationPatternSB.clearItems();
        formationPatternSB.setItems(FormationPatterns3DEnum.values());
        formationPatternSB.setSelected(currentPattern);
        formationPatternSB.getSelection().setProgrammaticChangeEvents(true);

        formationPatternSB.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                changePattern();
            }
        });

        formationAnchor = new Location3DVisTable(new VisLabel("Anchor"), modelES, stage);
        formationAnchor.setLocation(null);

        formationMembers = new ArrayAsTreeVisTable(null, true, new VisLabel("Members: "));
        formationMembers.setArray(null);

        // doesn't make sense to update(delta) this value at this point
        formationPatternRadiusVisTable = new FloatVisTable(true, new VisLabel("Radius: "));
        formationPatternRadiusVisTable.setFloat(currentPattern.memberRadius).setSetter(value -> changePattern());
    }

    public void changePattern() {
        FormationPattern<Vector3> pattern = null;
        switch (formationPatternSB.getSelected()) {
            case OFFENSIVE_CIRCLE: pattern = OFFENSIVE_CIRCLE.getInstance(formationPatternRadiusVisTable.value); break;
            case DEFENSIVE_CIRCLE: pattern = DEFENSIVE_CIRCLE.getInstance(formationPatternRadiusVisTable.value); break;
        }
        if (pattern == null) { return; }
        eng.formation.changePattern(pattern);
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);
        if (dbgModelInstance != null) {
            eng.setFormationAnchor(dbgModelInstance);
            if (dbgModelInstances.size > 1) {
                Array<EditableModelInstance> members = getNonPrimaryModelInstances();
                eng.setFormationMembers(members);
            }
        }
        formationAnchor.setLocation(eng.formation.getAnchorPoint());
        formationMembers.setArray(eng.getFormationMembers(new Array<>(true, 16, FormationMember.class)));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        formationAnchor.update(delta);
    }

    @Override
    public void applyLocale() { }
}