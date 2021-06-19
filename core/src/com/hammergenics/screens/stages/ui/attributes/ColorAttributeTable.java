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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.screens.ModelEditScreen;

import static com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
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
    private TextField rTF = null;
    private TextField gTF = null;
    private TextField bTF = null;
    private TextField aTF = null;
    private SelectBox<String> colorSB = null;

    private TextFieldListener paramTextFieldListener;
    private ChangeListener colorSelectBoxListener;

    private Color color = new Color().set(Color.GRAY);

    public ColorAttributeTable(Skin skin, Attributes container, ModelEditScreen modelES) {
        super(skin, container, modelES, ColorAttribute.class);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        rTF = new TextField("150", skin); rTF.setName(ACTOR_R);
        gTF = new TextField("150", skin); gTF.setName(ACTOR_G);
        bTF = new TextField("150", skin); bTF.setName(ACTOR_B);
        aTF = new TextField("150", skin); aTF.setName(ACTOR_A);

        rTF.setTextFieldListener(paramTextFieldListener);
        gTF.setTextFieldListener(paramTextFieldListener);
        bTF.setTextFieldListener(paramTextFieldListener);
        aTF.setTextFieldListener(paramTextFieldListener);

        //Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "Color: \n" + itemsColor.toString("\n"));

        // Select Box: Color
        colorSB = new SelectBox<>(skin);
        colorSB.clearItems();
        if (color_s2c != null && color_s2c.size > 0) {
            colorSB.setItems(color_s2c.keys().toArray());
        }
        //[switchModelInstance] before clear
        //[textureSelectBox.changed] -1
        //[textureSelectBox.changed] null
        //[switchModelInstance] after clear/before set
        //[textureSelectBox.changed] 0
        //[textureSelectBox.changed] No Texture
        //[switchModelInstance] after set
        colorSB.addListener(colorSelectBoxListener);

        add(enabledCheckBox).expandX().left().padLeft(5f).padRight(5f);
        add(new Label("r:", skin)).right();
        add(rTF).width(40).maxWidth(40);
        add(new Label("g:", skin)).right();
        add(gTF).width(40).maxWidth(40);
        add(new Label("b:", skin)).right();
        add(bTF).width(40).maxWidth(40);
        add(new Label("a:", skin)).right();
        add(aTF).width(40).maxWidth(40);
        add(new Label("color:", skin)).right();
        add(colorSB).fillX();
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

    private void createListeners() {
        paramTextFieldListener = new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (value > 255 || value < 0) {
                        textField.getColor().set(Color.PINK);
                        return;
                    }
                    value = (value / 255); // since originally we translated from [0:1] to [0:255]

//                    Gdx.app.debug("enabledCheckBox", textField.getName() + " = " + value
//                            + " type = 0x" + Long.toHexString(currentType) + " alias = " + currentTypeAlias);

                    if (container != null && currentType != 0) {
                        ColorAttribute attr = null;
                        attr = container.get(ColorAttribute.class, currentType);

                        switch (textField.getName()) {
                            case ACTOR_R:
                                if (attr != null) { attr.color.r = value; }
                                color.r = value;
                                break;
                            case ACTOR_G:
                                if (attr != null) { attr.color.g = value; }
                                color.g = value;
                                break;
                            case ACTOR_B:
                                if (attr != null) { attr.color.b = value; }
                                color.b = value;
                                break;
                            case ACTOR_A:
                                if (attr != null) { attr.color.a = value; }
                                color.a = value;
                                break;
                        }

                        if (attr != null && listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
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
                    color.set(color_s2c.get(colorSB.getSelected()));

                    // This most likely works with ChangeListener not TextFieldListener
//                    rTF.setProgrammaticChangeEvents(true); // to have the events fired on programmatic setText()
//                    gTF.setProgrammaticChangeEvents(true); // to have the events fired on programmatic setText()
//                    bTF.setProgrammaticChangeEvents(true); // to have the events fired on programmatic setText()
//                    aTF.setProgrammaticChangeEvents(true); // to have the events fired on programmatic setText()

                    if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                    if (rTF != null) { rTF.setText(String.valueOf((int)(color.r * 255))); } // extending the range from [0:1] to [0:255]
                    if (gTF != null) { gTF.setText(String.valueOf((int)(color.g * 255))); } // extending the range from [0:1] to [0:255]
                    if (bTF != null) { bTF.setText(String.valueOf((int)(color.b * 255))); } // extending the range from [0:1] to [0:255]
                    if (aTF != null) { aTF.setText(String.valueOf((int)(color.a * 255))); } // extending the range from [0:1] to [0:255]

                    ColorAttribute attr = null;
                    attr = container.get(ColorAttribute.class, currentType);
                    if (attr != null) { attr.color.set(color); }

                    if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
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
        if (rTF != null) { rTF.setText(String.valueOf((int)(Color.GRAY.r * 255))); } // extending the range from [0:1] to [0:255]
        if (gTF != null) { gTF.setText(String.valueOf((int)(Color.GRAY.g * 255))); } // extending the range from [0:1] to [0:255]
        if (bTF != null) { bTF.setText(String.valueOf((int)(Color.GRAY.b * 255))); } // extending the range from [0:1] to [0:255]
        if (aTF != null) { aTF.setText(String.valueOf((int)(Color.GRAY.a * 255))); } // extending the range from [0:1] to [0:255]
    }

    @Override
    protected void fetchWidgetsFromAttribute(ColorAttribute attr) {
        color.set(attr.color);
        if (enabledCheckBox != null) { enabledCheckBox.setChecked(true); }
        if (rTF != null) { rTF.setText(String.valueOf((int)(attr.color.r * 255))); } // extending the range from [0:1] to [0:255]
        if (gTF != null) { gTF.setText(String.valueOf((int)(attr.color.g * 255))); } // extending the range from [0:1] to [0:255]
        if (bTF != null) { bTF.setText(String.valueOf((int)(attr.color.b * 255))); } // extending the range from [0:1] to [0:255]
        if (aTF != null) { aTF.setText(String.valueOf((int)(attr.color.a * 255))); } // extending the range from [0:1] to [0:255]
    }

    @Override
    protected void postRemoveAttr() {

    }
}
