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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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

    // direction: x, y, z
    protected TextField xTF = null;
    protected TextField yTF = null;
    protected TextField zTF = null;
    protected TextButton setTB = null;

    private TextField.TextFieldListener xyzTextFieldListener;
    private InputListener setInputListener;

    public DirectionalLightsAttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, DirectionalLightsAttribute.class, DirectionalLight.class);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        xTF = new TextField("0", skin); xTF.setName(ACTOR_X);
        yTF = new TextField("0", skin); yTF.setName(ACTOR_Y);
        zTF = new TextField("-1", skin); zTF.setName(ACTOR_Z);
        setTB = new TextButton("SET", skin);

        xTF.setTextFieldListener(xyzTextFieldListener);
        yTF.setTextFieldListener(xyzTextFieldListener);
        zTF.setTextFieldListener(xyzTextFieldListener);
        setTB.addListener(setInputListener);

        Table line = new Table();
        line.add(new Label("dir x:", skin)).right();
        line.add(xTF).width(100).maxWidth(100);
        line.add(new Label("dir y:", skin)).right();
        line.add(yTF).width(100).maxWidth(100);
        line.add(new Label("dir z:", skin)).right();
        line.add(zTF).width(100).maxWidth(100);
        line.add(setTB).padLeft(10f);
        line.add().expandX();
        add(line).fillX();
    }

    private void createListeners() {
        xyzTextFieldListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    Float.parseFloat(textField.getText());
                    textField.getColor().set(Color.WHITE);
                    if (xTF.getColor().equals(Color.WHITE)
                            && yTF.getColor().equals(Color.WHITE)
                            && zTF.getColor().equals(Color.WHITE)) {
                        setTB.getColor().set(COLOR_UNPRESSED);
                        setTB.getLabel().getColor().set(COLOR_UNPRESSED);
                    }
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                    setTB.getColor().set(COLOR_DISABLED);
                    setTB.getLabel().getColor().set(COLOR_DISABLED);
                }
            }
        };
        setInputListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (container != null && currentType != 0) {
                    DirectionalLightsAttribute attr = null;
                    attr = container.get(DirectionalLightsAttribute.class, currentType);

                    if (setTB.getColor().equals(COLOR_DISABLED)) {
                        return super.touchDown(event, x, y, pointer, button);
                    }

                    if (attr != null && index >= 0 && index < attr.lights.size) {
                        attr.lights.get(index).direction.set(
                                Float.parseFloat(xTF.getText()),
                                Float.parseFloat(yTF.getText()),
                                Float.parseFloat(zTF.getText())
                        );
                    }

                    if (attr != null && listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
                }

                return super.touchDown(event, x, y, pointer, button);
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
        //Gdx.app.debug(getClass().getSimpleName(), "lights size: " + lights.size);
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
        Vector3 dir;

        if (lights != null && lights.size != 0) {
            DirectionalLight pl = lights.get(lights.size - 1);
            dir = pl.direction.cpy().rotate(-90f, 0, 1, 0).nor();
        } else {
            dir = new Vector3(0f, -0.5f, -1f);
        }

        return new DirectionalLight().set(Color.WHITE, dir);
    }
}
