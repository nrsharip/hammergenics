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

package com.hammergenics.screens.stages.ui.attributes;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SpotLightsAttributesTable
        extends AttributesTable<SpotLightsAttribute, BaseLightsAttributeTable<SpotLightsAttribute, SpotLight>> {

    public SpotLightsAttributesTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, SpotLightsAttribute.class);

        // START - Candidate for move to AttributesTable
        t2a.forEach((entry) -> {
            SpotLightsAttributeTable table = new SpotLightsAttributeTable(skin, container, mps);
            t2Table.put(entry.key, table);   // type to table
            a2Table.put(entry.value, table); // alias to table
        });

        resetAttributes();

        a2Table.forEach((entry) -> {
            add(new Label(entry.key + ":", skin)).right();
            add(entry.value).left();
            add().expandX();
            row();
        });
        // END - Candidate for move to AttributesTable
    }
}
