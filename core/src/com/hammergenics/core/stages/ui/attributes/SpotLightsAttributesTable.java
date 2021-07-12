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

package com.hammergenics.core.stages.ui.attributes;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.hammergenics.core.ModelEditScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SpotLightsAttributesTable
        extends AttributesTable<SpotLightsAttribute, BaseLightsAttributeTable<SpotLightsAttribute, SpotLight>> {

    public SpotLightsAttributesTable(Attributes container, ModelEditScreen modelES) {
        super(container, modelES, SpotLightsAttribute.class);
    }

    @Override
    protected BaseLightsAttributeTable<SpotLightsAttribute, SpotLight> createTable(Attributes container, ModelEditScreen modelES) {
        return new SpotLightsAttributeTable(container, modelES);
    }
}
