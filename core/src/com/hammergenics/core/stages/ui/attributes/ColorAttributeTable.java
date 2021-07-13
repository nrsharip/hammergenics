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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;

import static com.hammergenics.utils.HGUtils.color_c2s;
import static com.hammergenics.utils.HGUtils.color_s2c;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ColorAttributeTable extends AttributeTable<ColorAttribute> {
    public static final String ACTOR_R = "r_TextField";
    public static final String ACTOR_G = "g_TextField";
    public static final String ACTOR_B = "b_TextField";
    public static final String ACTOR_A = "a_TextField";

    // r, g, b, a;
    private VisTextField rTF = null;
    private VisTextField gTF = null;
    private VisTextField bTF = null;
    private VisTextField aTF = null;
    private VisSelectBox<String> colorSB = null;
    private VisTextButton selectColorTB = null;

    private VisTextField.TextFieldListener paramTextFieldListener;
    private ChangeListener colorSelectBoxListener;
    private ColorPickerListener colorPickerListener;

    private Color color = new Color(Color.GRAY);

    public ColorAttributeTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
        super(container, modelES, ColorAttribute.class, window, type, alias);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        rTF = new VisTextField("150"); rTF.setName(ACTOR_R);
        gTF = new VisTextField("150"); gTF.setName(ACTOR_G);
        bTF = new VisTextField("150"); bTF.setName(ACTOR_B);
        aTF = new VisTextField("150"); aTF.setName(ACTOR_A);

        rTF.setTextFieldListener(paramTextFieldListener);
        gTF.setTextFieldListener(paramTextFieldListener);
        bTF.setTextFieldListener(paramTextFieldListener);
        aTF.setTextFieldListener(paramTextFieldListener);

        //Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "Color: \n" + itemsColor.toString("\n"));

        // Select Box: Color
        colorSB = new VisSelectBox<>();
        colorSB.clearItems();
        if (color_s2c != null && color_s2c.size > 0) {
            String array1[] = color_s2c.keys().toArray().toArray();
            String array2[] = new String[array1.length + 1];
            System.arraycopy(array1, 0, array2, 1, array1.length);
            array2[0] = "custom";

            colorSB.setItems(array2);
        }
        //[switchModelInstance] before clear
        //[textureSelectBox.changed] -1
        //[textureSelectBox.changed] null
        //[switchModelInstance] after clear/before set
        //[textureSelectBox.changed] 0
        //[textureSelectBox.changed] No Texture
        //[switchModelInstance] after set
        colorSB.addListener(colorSelectBoxListener);

        selectColorTB = new VisTextButton("select");
        selectColorTB.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                modelES.stage.colorPicker.setListener(colorPickerListener);
                modelES.stage.colorPicker.getPicker().setColor(color);
                //displaying picker with fade in animation
                getStage().addActor(modelES.stage.colorPicker.fadeIn());
            }
        });

        add(enabledCheckBox).expandX().left().padLeft(5f).padRight(5f);
        add(new VisLabel("r:")).right();
        add(rTF).pad(1f).width(40).maxWidth(40);
        add(new VisLabel("g:")).right();
        add(gTF).pad(1f).width(40).maxWidth(40);
        add(new VisLabel("b:")).right();
        add(bTF).pad(1f).width(40).maxWidth(40);
        add(new VisLabel("a:")).right();
        add(aTF).pad(1f).width(40).maxWidth(40);
        add(new VisLabel("color:")).right();
        add(colorSB).pad(1f).fillX();
        add(selectColorTB).fillX();
        add().expandX();
    }

    @Override
    protected ColorAttribute createAttribute(String alias) {
        switch (alias) {
            case ColorAttribute.DiffuseAlias: return ColorAttribute.createDiffuse (color);
            case ColorAttribute.SpecularAlias: return ColorAttribute.createSpecular (color);
            case ColorAttribute.AmbientAlias: return ColorAttribute.createAmbient (color);
            case ColorAttribute.EmissiveAlias: return ColorAttribute.createEmissive (color);
            case ColorAttribute.ReflectionAlias: return ColorAttribute.createReflection (color);
            case ColorAttribute.AmbientLightAlias: return ColorAttribute.createAmbientLight (color);
            case ColorAttribute.FogAlias: return ColorAttribute.createFog (color);
        }
        return null;
    }

    public void setAttributeColor(Color color) {
        if (container != null && currentType != 0) {
            ColorAttribute attr = container.get(ColorAttribute.class, currentType);
            if (attr != null) {
                attr.color.set(color);
                if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
            }
        }
        fetchFromColor(color);
    }

    private void createListeners() {
        colorPickerListener = new ColorPickerListener() {
            @Override public void canceled(Color oldColor) {
                if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                setAttributeColor(oldColor);
            }
            @Override public void changed(Color newColor) {
                if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                setAttributeColor(newColor);
            }
            @Override public void reset(Color previousColor, Color newColor) {
                if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                setAttributeColor(newColor);
            }
            @Override public void finished(Color newColor) {
                if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                setAttributeColor(newColor);
            }
        };

        paramTextFieldListener = new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (value > 255 || value < 0) { textField.getColor().set(Color.PINK); return; }
                    value = (value / 255); // since originally we translated from [0:1] to [0:255]

//                    Gdx.app.debug("enabledCheckBox", textField.getName() + " = " + value
//                            + " type = 0x" + Long.toHexString(currentType) + " alias = " + currentTypeAlias);

                    if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                    if (container != null && currentType != 0) {
                        Color tmp = new Color(color);
                        switch (textField.getName()) {
                            case ACTOR_R: tmp.r = value; break;
                            case ACTOR_G: tmp.g = value; break;
                            case ACTOR_B: tmp.b = value; break;
                            case ACTOR_A: tmp.a = value; break;
                        }
                        setAttributeColor(tmp);
                    }
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            }
        };

        colorSelectBoxListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (container != null && currentType != 0) {
                    if (colorSB.getSelectedIndex() == 0) { return; }

                    color.set(color_s2c.get(colorSB.getSelected()));

                    if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }

                    setAttributeColor(color);
                }
            }
        };
    }

    @Override
    protected boolean preCreateAttr() {
        return true;
    }

    @Override
    protected void resetWidgetsToDefaults() {
        if (enabledCheckBox != null) { enabledCheckBox.setChecked(false); }
        fetchFromColor(Color.GRAY);
    }

    @Override
    protected void fetchWidgetsFromAttribute(ColorAttribute attr) {
        if (enabledCheckBox != null) { enabledCheckBox.setChecked(true); }
        fetchFromColor(attr.color);
    }

    protected void fetchFromColor(Color clr) {
        color.set(clr);
        if (rTF != null) { rTF.setText(String.valueOf((int)(clr.r * 255))); } // extending the range from [0:1] to [0:255]
        if (gTF != null) { gTF.setText(String.valueOf((int)(clr.g * 255))); } // extending the range from [0:1] to [0:255]
        if (bTF != null) { bTF.setText(String.valueOf((int)(clr.b * 255))); } // extending the range from [0:1] to [0:255]
        if (aTF != null) { aTF.setText(String.valueOf((int)(clr.a * 255))); } // extending the range from [0:1] to [0:255]
        if (colorSB != null) {
            colorSB.getSelection().setProgrammaticChangeEvents(false);
            String colorName = color_c2s.get(clr);
            if (colorName != null) { colorSB.setSelected(colorName); } else { colorSB.setSelectedIndex(0); }
            colorSB.getSelection().setProgrammaticChangeEvents(true);
        }
    }

    @Override
    protected void postRemoveAttr() {

    }
}
