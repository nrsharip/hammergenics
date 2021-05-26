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

package com.hammergenics.ui.attributes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SpotLightsAttributeTable extends BaseLightsAttributeTable<SpotLightsAttribute, SpotLight> {

    public SpotLightsAttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, SpotLightsAttribute.class, SpotLight.class);
    }

    @Override
    protected void fetchWidgetsFromAttribute(SpotLightsAttribute attr) {
        this.lights = attr.lights;

        resetWidgetsToDefaults();

        if (index >= 0 && index < attr.lights.size) {
            color.set(attr.lights.get(index).color);
            if (rTF != null) { rTF.setText(String.valueOf((int)(attr.lights.get(index).color.r * 255))); } // extending the range from [0:1] to [0:255]
            if (gTF != null) { gTF.setText(String.valueOf((int)(attr.lights.get(index).color.g * 255))); } // extending the range from [0:1] to [0:255]
            if (bTF != null) { bTF.setText(String.valueOf((int)(attr.lights.get(index).color.b * 255))); } // extending the range from [0:1] to [0:255]
            if (aTF != null) { aTF.setText(String.valueOf((int)(attr.lights.get(index).color.a * 255))); } // extending the range from [0:1] to [0:255]
        }
        Gdx.app.debug(getClass().getSimpleName(), "lights size: " + lights.size);
    }

    @Override
    protected SpotLightsAttribute createAttribute(String alias) {
        SpotLightsAttribute lightsAttribute = new SpotLightsAttribute();
        lightsAttribute.lights.addAll(this.lights);
        return lightsAttribute;
    }

    @Override
    protected void postButtonAdd() {

    }

    @Override
    protected void postButtonRemove() {

    }

    @Override
    protected void resetWidgetsToDefaults() {
        // additional from SpotLight:

        super.resetWidgetsToDefaults();
    }

    @Override
    protected SpotLight createLight() {
        return new SpotLight();
    }
}
