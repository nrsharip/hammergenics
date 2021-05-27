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
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
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
public class PointLightsAttributeTable extends BaseLightsAttributeTable<PointLightsAttribute, PointLight> {
    public static final String ACTOR_X = "x_TextField";
    public static final String ACTOR_Y = "y_TextField";
    public static final String ACTOR_Z = "z_TextField";
    public static final String ACTOR_I = "i_TextField";

    // position: x, y, z
    protected TextField xTF = null;
    protected TextField yTF = null;
    protected TextField zTF = null;
    // intensity
    protected TextField iTF = null;

    private TextField.TextFieldListener xyziTextFieldListener;

    // this is an internal counter to switch the position of newly created light
    private short pos = 0;

    public PointLightsAttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, PointLightsAttribute.class, PointLight.class);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        xTF = new TextField("400", skin); xTF.setName(ACTOR_X);
        yTF = new TextField("400", skin); yTF.setName(ACTOR_Y);
        zTF = new TextField("400", skin); zTF.setName(ACTOR_Z);
        iTF = new TextField("100000", skin); iTF.setName(ACTOR_I);

        xTF.setTextFieldListener(xyziTextFieldListener);
        yTF.setTextFieldListener(xyziTextFieldListener);
        zTF.setTextFieldListener(xyziTextFieldListener);
        iTF.setTextFieldListener(xyziTextFieldListener);

        Table line = new Table();
        line.add(new Label("x:", skin)).right();
        line.add(xTF).width(100).maxWidth(100);
        line.add(new Label("y:", skin)).right();
        line.add(yTF).width(100).maxWidth(100);
        line.add(new Label("z:", skin)).right();
        line.add(zTF).width(100).maxWidth(100);
        line.add(new Label("intensity:", skin)).right();
        line.add(iTF).width(100).maxWidth(100);
        line.add().expandX();
        add(line).fillX();
    }

    private void createListeners() {
        xyziTextFieldListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

//                    if (value > 1 || value < 0) {
//                        textField.getColor().set(Color.PINK);
//                        return;
//                    }

                    if (container != null && currentType != 0) {
                        PointLightsAttribute attr = null;
                        attr = container.get(PointLightsAttribute.class, currentType);

                        if (attr != null && index >= 0) {
                            switch (textField.getName()) {
                                case ACTOR_X: if (index < attr.lights.size) { attr.lights.get(index).position.x = value; } break;
                                case ACTOR_Y: if (index < attr.lights.size) { attr.lights.get(index).position.y = value; } break;
                                case ACTOR_Z: if (index < attr.lights.size) { attr.lights.get(index).position.z = value; } break;
                                case ACTOR_I: if (index < attr.lights.size) { attr.lights.get(index).intensity = value; } break;
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
    protected void fetchWidgetsFromAttribute(PointLightsAttribute attr) {
        this.lights = attr.lights;

        resetWidgetsToDefaults();

        if (index >= 0 && index < attr.lights.size) {
            color.set(attr.lights.get(index).color);
            if (rTF != null) { rTF.setText(String.valueOf((int)(attr.lights.get(index).color.r * 255))); } // extending the range from [0:1] to [0:255]
            if (gTF != null) { gTF.setText(String.valueOf((int)(attr.lights.get(index).color.g * 255))); } // extending the range from [0:1] to [0:255]
            if (bTF != null) { bTF.setText(String.valueOf((int)(attr.lights.get(index).color.b * 255))); } // extending the range from [0:1] to [0:255]
            if (aTF != null) { aTF.setText(String.valueOf((int)(attr.lights.get(index).color.a * 255))); } // extending the range from [0:1] to [0:255]
            // additional from PointLight:
            if (xTF != null) { xTF.setText(String.valueOf(attr.lights.get(index).position.x)); }
            if (yTF != null) { yTF.setText(String.valueOf(attr.lights.get(index).position.y)); }
            if (zTF != null) { zTF.setText(String.valueOf(attr.lights.get(index).position.z)); }
            if (iTF != null) { iTF.setText(String.valueOf(attr.lights.get(index).intensity)); }
        }
        Gdx.app.debug(getClass().getSimpleName(), "lights size: " + lights.size);
    }

    @Override
    protected PointLightsAttribute createAttribute(String alias) {
        PointLightsAttribute lightsAttribute = new PointLightsAttribute();
        lightsAttribute.lights.addAll(this.lights);
        return lightsAttribute;
    }

    @Override
    protected void postButtonAdd() {
        if (index >= 0 && index < lights.size) {
            // additional from PointLight:
            if (xTF != null) { xTF.setText(String.valueOf(lights.get(index).position.x)); }
            if (yTF != null) { yTF.setText(String.valueOf(lights.get(index).position.y)); }
            if (zTF != null) { zTF.setText(String.valueOf(lights.get(index).position.z)); }
            if (iTF != null) { iTF.setText(String.valueOf(lights.get(index).intensity)); }
        }
    }

    @Override
    protected void postButtonRemove() {
        pos--;
        if (indexedTB.size == 0) { pos = 0; } // resetting
    }

    @Override
    protected void resetWidgetsToDefaults() {
        // additional from PointLight:
        if (xTF != null) { xTF.setText(String.valueOf(400f)); }
        if (yTF != null) { yTF.setText(String.valueOf(400f)); }
        if (zTF != null) { zTF.setText(String.valueOf(400f)); }
        if (iTF != null) { iTF.setText(String.valueOf(100000f)); }
        super.resetWidgetsToDefaults();
    }

    @Override
    protected PointLight createLight() {
        // 8 positions:
        // pos % 4   pos % 8     x   z
        //    0         0     :  0  -1
        //    1         1     :  1  -1
        //    2         2     :  1   0
        //    3         3     :  1   1
        //    0         4     :  0   1
        //    1         5     : -1   1
        //    2         6     : -1   0
        //    3         7     : -1  -1

        float x = pos % 4 == 0 ? 0f : (pos % 8) > 3 ? -1f : 1f;
        float z = pos % 4 == 2 ? 0f : ((pos + 2) % 8) < 4 ? -1f : 1f;
        pos++;
        // scaling:
        x *= 400f;
        z *= 400f;

        return new PointLight().set(Color.WHITE, x, 400f, z, 100000f);
    }
}
