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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

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
    protected VisTextField xTF = null;
    protected VisTextField yTF = null;
    protected VisTextField zTF = null;
    protected VisTextButton setTB = null;

    private VisTextField.TextFieldListener xyzTextFieldListener;
    private InputListener setInputListener;

    public DirectionalLightsAttributeTable(Attributes container, ModelEditScreen modelES) {
        super(container, modelES, DirectionalLightsAttribute.class, DirectionalLight.class);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        xTF = new VisTextField("0"); xTF.setName(ACTOR_X);
        yTF = new VisTextField("0"); yTF.setName(ACTOR_Y);
        zTF = new VisTextField("-1"); zTF.setName(ACTOR_Z);
        setTB = new VisTextButton("SET");

        xTF.setTextFieldListener(xyzTextFieldListener);
        yTF.setTextFieldListener(xyzTextFieldListener);
        zTF.setTextFieldListener(xyzTextFieldListener);
        setTB.addListener(setInputListener);

        VisTable line = new VisTable();
        line.add(new VisLabel("dir x:", Color.BLACK)).right();
        line.add(xTF).width(100).maxWidth(100);
        line.add(new VisLabel("dir y:", Color.BLACK)).right();
        line.add(yTF).width(100).maxWidth(100);
        line.add(new VisLabel("dir z:", Color.BLACK)).right();
        line.add(zTF).width(100).maxWidth(100);
        line.add(setTB).padLeft(10f);
        line.add().expandX();
        add(line).fillX();
    }

    private void createListeners() {
        xyzTextFieldListener = new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                try {
                    Float.parseFloat(textField.getText());
                    textField.getColor().set(Color.WHITE);
                    if (xTF.getColor().equals(Color.WHITE)
                            && yTF.getColor().equals(Color.WHITE)
                            && zTF.getColor().equals(Color.WHITE)) {
                        modelES.stage.enableButton(setTB);
                    }
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                    modelES.stage.disableButton(setTB);
                }
            }
        };
        setInputListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (container != null && currentType != 0) {
                    DirectionalLightsAttribute attr = null;
                    attr = container.get(DirectionalLightsAttribute.class, currentType);

                    if (modelES.stage.isDisabled(setTB)) {
                        return super.touchDown(event, x, y, pointer, button);
                    }

                    if (attr != null && index >= 0 && index < attr.lights.size) {
                        attr.lights.get(index).direction.set(
                                Float.parseFloat(xTF.getText()),
                                Float.parseFloat(yTF.getText()),
                                Float.parseFloat(zTF.getText())
                        );
                    }

                    if (attr != null && listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
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
            DirectionalLight dl = lights.get(lights.size - 1);
            dir = dl.direction.cpy().rotate(Vector3.Y.cpy(), -90f).nor();
        } else {
            dir = new Vector3(-1f, -0.5f, -1f);
        }

        return new DirectionalLight().set(Color.LIGHT_GRAY, dir);
    }
}
