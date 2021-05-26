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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class DirectionalLightsAttributeTable extends BaseLightsAttributeTable<DirectionalLightsAttribute, DirectionalLight> {
    public static final String ACTOR_X = "x_TextField";
    public static final String ACTOR_Y = "y_TextField";
    public static final String ACTOR_Z = "z_TextField";

    // this is an internal counter to switch the position of newly created light
    private short pos = 0;

    // x, y, z: direction
    protected TextField xTF = null;
    protected TextField yTF = null;
    protected TextField zTF = null;

    private TextField.TextFieldListener xyzTextFieldListener;

    public DirectionalLightsAttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, DirectionalLightsAttribute.class, DirectionalLight.class);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        xTF = new TextField("0", skin); xTF.setName(ACTOR_X);
        yTF = new TextField("0", skin); yTF.setName(ACTOR_Y);
        zTF = new TextField("-1", skin); zTF.setName(ACTOR_Z);

        xTF.setTextFieldListener(xyzTextFieldListener);
        yTF.setTextFieldListener(xyzTextFieldListener);
        zTF.setTextFieldListener(xyzTextFieldListener);

        Table line = new Table();
        line.add(new Label("x:", skin)).right();
        line.add(xTF).width(100).maxWidth(100);
        line.add(new Label("y:", skin)).right();
        line.add(yTF).width(100).maxWidth(100);
        line.add(new Label("z:", skin)).right();
        line.add(zTF).width(100).maxWidth(100);
        line.add().expandX();
        add(line).fillX();
    }

    private void createListeners() {
        xyzTextFieldListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

//                    if (value > 1 || value < 0) {
//                        textField.getColor().set(Color.PINK);
//                        return;
//                    }

                    if (container != null && currentType != 0) {
                        DirectionalLightsAttribute attr = null;
                        attr = container.get(DirectionalLightsAttribute.class, currentType);

                        if (attr != null && index >= 0) {
                            switch (textField.getName()) {
                                case ACTOR_X: if (index < attr.lights.size) { attr.lights.get(index).direction.x = value; } break;
                                case ACTOR_Y: if (index < attr.lights.size) { attr.lights.get(index).direction.y = value; } break;
                                case ACTOR_Z: if (index < attr.lights.size) { attr.lights.get(index).direction.z = value; } break;
                            }
                        }

                        if (attr != null && listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
                    }
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            }
        };
    }

    @Override
    protected void fetchWidgetsFromAttribute(DirectionalLightsAttribute attr) {
        this.lights = attr.lights;

        resetWidgetsToDefaults();

        if (index >= 0 && index < attr.lights.size) {
            color.set(attr.lights.get(index).color);
            if (rTF != null) { rTF.setText(String.valueOf((int)(attr.lights.get(index).color.r * 255))); } // extending the range from [0:1] to [0:255]
            if (gTF != null) { gTF.setText(String.valueOf((int)(attr.lights.get(index).color.g * 255))); } // extending the range from [0:1] to [0:255]
            if (bTF != null) { bTF.setText(String.valueOf((int)(attr.lights.get(index).color.b * 255))); } // extending the range from [0:1] to [0:255]
            if (aTF != null) { aTF.setText(String.valueOf((int)(attr.lights.get(index).color.a * 255))); } // extending the range from [0:1] to [0:255]
            // additional from DirectionalLight:
            if (xTF != null) { xTF.setText(String.valueOf(attr.lights.get(index).direction.x)); }
            if (yTF != null) { yTF.setText(String.valueOf(attr.lights.get(index).direction.y)); }
            if (zTF != null) { zTF.setText(String.valueOf(attr.lights.get(index).direction.z)); }
        }
        Gdx.app.debug(getClass().getSimpleName(), "lights size: " + lights.size);
    }

    @Override
    protected DirectionalLightsAttribute createAttribute(String alias) {
        DirectionalLightsAttribute lightsAttribute = new DirectionalLightsAttribute();
        lightsAttribute.lights.addAll(this.lights);
        return lightsAttribute;
    }

    @Override
    protected void postButtonAdd() {
        if (index >= 0 && index < lights.size) {
            // additional from DirectionalLight:
            if (xTF != null) { xTF.setText(String.valueOf(lights.get(index).direction.x)); }
            if (yTF != null) { yTF.setText(String.valueOf(lights.get(index).direction.y)); }
            if (zTF != null) { zTF.setText(String.valueOf(lights.get(index).direction.z)); }
        }
    }

    @Override
    protected void postButtonRemove() {
        pos--;
        if (indexedTB.size == 0) { pos = 0; } // resetting
    }

    @Override
    protected void resetWidgetsToDefaults() {
        // additional from DirectionalLight:
        if (xTF != null) { xTF.setText(String.valueOf(0f)); }
        if (yTF != null) { yTF.setText(String.valueOf(0f)); }
        if (zTF != null) { zTF.setText(String.valueOf(-1f)); }

        super.resetWidgetsToDefaults();
    }

    @Override
    protected DirectionalLight createLight() {
        // pos % 4     x   z
        //    0     :  0  -1
        //    1     :  1   0
        //    2     :  0   1
        //    3     : -1   0
        float x = pos % 2 == 0 ? 0f : (pos % 4) > 2 ? -1f : 1f;
        float z = (pos + 3) % 2 == 0 ? 0f : ((pos + 3) % 4) > 2 ? -1f : 1f;
        pos++;
        return new DirectionalLight().set(Color.WHITE, x, -0.5f, z);
    }
}
