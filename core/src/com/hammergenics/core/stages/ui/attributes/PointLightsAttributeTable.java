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
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

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
    protected VisTextField xTF = null;
    protected VisTextField yTF = null;
    protected VisTextField zTF = null;
    // intensity
    protected VisTextField iTF = null;

    private VisTextField.TextFieldListener xyziTextFieldListener;

    public PointLightsAttributeTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
        super(container, modelES, PointLightsAttribute.class, PointLight.class, window, type, alias);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        xTF = new VisTextField("100"); xTF.setName(ACTOR_X);
        yTF = new VisTextField("100"); yTF.setName(ACTOR_Y);
        zTF = new VisTextField("100"); zTF.setName(ACTOR_Z);
        iTF = new VisTextField("5000"); iTF.setName(ACTOR_I);

        xTF.setTextFieldListener(xyziTextFieldListener);
        yTF.setTextFieldListener(xyziTextFieldListener);
        zTF.setTextFieldListener(xyziTextFieldListener);
        iTF.setTextFieldListener(xyziTextFieldListener);

        VisTable line = new VisTable();
        cell11.setActor(new VisLabel("x:")).right();
        cell12.setActor(xTF).width(100).maxWidth(100);
        cell21.setActor(new VisLabel("y:")).right();
        cell22.setActor(yTF).width(100).maxWidth(100);
        cell31.setActor(new VisLabel("z:")).right();
        cell32.setActor(zTF).width(100).maxWidth(100);
        cell41.setActor(new VisLabel("intensity:")).right();
        cell42.setActor(iTF).width(100).maxWidth(100);
    }

    private void createListeners() {
        xyziTextFieldListener = new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
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

                        if (attr != null && listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
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
        //Gdx.app.debug(getClass().getSimpleName(), "lights size: " + lights.size);
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
    }

    @Override
    protected void resetWidgetsToDefaults() {
        // additional from PointLight:
        if (xTF != null) { xTF.setText(String.valueOf(100f)); }
        if (yTF != null) { yTF.setText(String.valueOf(100f)); }
        if (zTF != null) { zTF.setText(String.valueOf(100f)); }
        if (iTF != null) { iTF.setText(String.valueOf(5000f)); }
        super.resetWidgetsToDefaults();
    }

    @Override
    protected PointLight createLight() {
        Vector3 position = Vector3.Zero.cpy();
        float intensity;

        if (lights != null && lights.size != 0) {
            PointLight pl = lights.get(lights.size - 1);

            if (container instanceof Environment) {
                position.set(pl.position.cpy().rotate(Vector3.Y.cpy(), -45f));
            } else if (container instanceof Material) {
                position.set(pl.position.cpy().sub(modelES.eng.getCurrMI().getBB().getCenter(new Vector3())));
                position.rotate(Vector3.Y.cpy(), -45f);
                position.add(modelES.eng.getCurrMI().getBB().getCenter(new Vector3()));
            } else { return null; } // so the IDE is not complaining

            intensity = pl.intensity;
        } else {
            float size;
            if (container instanceof Environment) {
                size = modelES.eng.overallSize;
                if (modelES.eng.editableMIs.size > 0) {
                    position.set(modelES.eng.editableMIs.get(0).getBB().getCenter(new Vector3()).cpy());
                }
            } else if (container instanceof Material) {
                size = modelES.eng.unitSize;
                if (modelES.eng.getCurrMI() != null) {
                    position = modelES.eng.getCurrMI().getBB().getCenter(new Vector3()).cpy();
                }
            } else { return null; } // so the IDE is not complaining

            position.add(-size/2, size/2, size/2);
            // seems that intensity should grow exponentially(?) over the distance, the table is:
            //  unitSize: 1.7   17    191    376    522
            // intensity:   1  100  28708  56470  78397
            intensity = (size < 50f ? 10.10947f : 151.0947f) * size - 90f; // TODO: temporal solution, revisit
            intensity = intensity <= 0 ? 1f : intensity;                                 // TODO: temporal solution, revisit
            // syncup: pl
        }
        return new PointLight().set(Color.WHITE, position, intensity);
    }
}
