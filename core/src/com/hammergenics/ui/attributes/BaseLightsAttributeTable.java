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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.ModelPreviewScreen;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static com.hammergenics.ui.attributes.ColorAttributeTable.*;
import static com.hammergenics.util.LibgdxUtils.color_s2c;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class BaseLightsAttributeTable<T extends Attribute, L extends BaseLight<L>> extends AttributeTable<T> {
    private static final String ACTOR_BUTTON_PREFIX = "button_";
    private static final Color COLOR_DISABLED = Color.GRAY;
    private static final Color COLOR_PRESSED = Color.RED;
    private static final Color COLOR_UNPRESSED = Color.WHITE;

    protected Class<L> lightClass;
    protected Color color = new Color().set(Color.GRAY);
    protected int index = -1;

    // r, g, b, a
    protected TextField rTF = null;
    protected TextField gTF = null;
    protected TextField bTF = null;
    protected TextField aTF = null;
    // color select box
    protected SelectBox<String> colorSB = null;
    // + and - buttons
    protected TextButton plsTextButton = null;
    protected TextButton mnsTextButton = null;
    // table to contain the indexed buttons (1, 2, 3 etc.)
    protected Table indexedTBTable = new Table();

    private TextField.TextFieldListener rgbaTextFieldListener;
    private ChangeListener colorSelectBoxListener;

    public Array<TextButton> indexedTB = null;
    protected Array<L> lights;

    protected abstract L createLight();

    public BaseLightsAttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps, Class<T> aClass, Class<L> lightClass) {
        super(skin, container, mps, aClass);
        this.lightClass = lightClass;
        lights = new Array<>(lightClass);

        plsTextButton = new TextButton("+", skin);
        plsTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (plsTextButton.getColor().equals(COLOR_DISABLED) || lights == null || indexedTB == null) { return; }

                if (!enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }

                addButton();

                L light = createLight();

                lights.add(light);

                indexedTB.get(indexedTB.size - 1).setChecked(true); // "pressing" the button added

                mnsTextButton.getColor().set(COLOR_UNPRESSED);
                mnsTextButton.getLabel().getColor().set(COLOR_UNPRESSED);

                if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
            }
        });

        mnsTextButton = new TextButton("-", skin);
        mnsTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (mnsTextButton.getColor().equals(COLOR_DISABLED) || lights == null || indexedTB == null) { return; }

                if (indexedTB.size > 2 && indexedTB.get(indexedTB.size - 1).getColor().equals(COLOR_PRESSED)) {
                    // trick to trigger the change event on the button before the one being removed ("press" button)
                    indexedTB.get(indexedTB.size - 2).setChecked(!indexedTB.get(indexedTB.size - 2).isChecked());
                } else if (indexedTB.size == 2) {
                    // trick to trigger the change event on button 0 ("press" 0 button)
                    indexedTB.get(0).setChecked(!indexedTB.get(0).isChecked());
                }

                indexedTBTable.getCell(indexedTB.get(indexedTB.size - 1)).clearActor().reset();
                indexedTBTable.removeActor(indexedTB.get(indexedTB.size - 1));
                indexedTB.get(indexedTB.size - 1).remove();
                indexedTB.removeIndex(indexedTB.size - 1);

                lights.removeIndex(lights.size - 1);

                if (indexedTB.size == 0 || lights.size == 0) { // these should be equal
                    mnsTextButton.getColor().set(COLOR_DISABLED);
                    mnsTextButton.getLabel().getColor().set(COLOR_DISABLED);
                }

                postButtonRemove();

                if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
            }
        });

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        rTF = new TextField("150", skin); rTF.setName(ACTOR_R);
        gTF = new TextField("150", skin); gTF.setName(ACTOR_G);
        bTF = new TextField("150", skin); bTF.setName(ACTOR_B);
        aTF = new TextField("150", skin); aTF.setName(ACTOR_A);

        rTF.setTextFieldListener(rgbaTextFieldListener);
        gTF.setTextFieldListener(rgbaTextFieldListener);
        bTF.setTextFieldListener(rgbaTextFieldListener);
        aTF.setTextFieldListener(rgbaTextFieldListener);

        // Select Box: Color
        colorSB = new SelectBox<>(skin);
        colorSB.clearItems();
        if (color_s2c != null && color_s2c.size > 0) { colorSB.setItems(color_s2c.keys().toArray()); }
        colorSB.addListener(colorSelectBoxListener);

        // Standard Color Layout:
        Table line1 = new Table();
        Table line2 = new Table();

        line1.add(enabledCheckBox);
        line1.add(new Label("lights:", skin)).right();
        line1.add(mnsTextButton).width(20f).maxWidth(20f);
        line1.add(indexedTBTable);
        line1.add(plsTextButton).width(20f).maxWidth(20f);
        line1.add().expandX();

        line2.add(new Label("r:", skin)).right();
        line2.add(rTF).width(40).maxWidth(40);
        line2.add(new Label("g:", skin)).right();
        line2.add(gTF).width(40).maxWidth(40);
        line2.add(new Label("b:", skin)).right();
        line2.add(bTF).width(40).maxWidth(40);
        line2.add(new Label("a:", skin)).right();
        line2.add(aTF).width(40).maxWidth(40);
        line2.add(new Label("color:", skin)).right();
        line2.add(colorSB).fillX();
        line2.add().expandX();

        add(line1).fillX();
        row();
        add(line2).fillX();
        row();
    }

    private void createListeners() {
        rgbaTextFieldListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (value > 255 || value < 0) {
                        textField.getColor().set(Color.PINK);
                        return;
                    }
                    value = (value / 255); // since originally we translated from [0:1] to [0:255]

                    if (container != null && currentType != 0) {
                        T attr = null;
                        attr = container.get(attributeClass, currentType);

                        if (attr != null && index >= 0) {
                            if (attr instanceof DirectionalLightsAttribute) {
                                DirectionalLightsAttribute attrTyped = (DirectionalLightsAttribute)attr;
                                switch (textField.getName()) {
                                    case ACTOR_R: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.r = value; } break;
                                    case ACTOR_G: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.g = value; } break;
                                    case ACTOR_B: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.b = value; } break;
                                    case ACTOR_A: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.a = value; } break;
                                }
                            }
                            if (attr instanceof PointLightsAttribute) {
                                PointLightsAttribute attrTyped = (PointLightsAttribute)attr;
                                switch (textField.getName()) {
                                    case ACTOR_R: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.r = value; } break;
                                    case ACTOR_G: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.g = value; } break;
                                    case ACTOR_B: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.b = value; } break;
                                    case ACTOR_A: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.a = value; } break;
                                }
                            }
                            if (attr instanceof SpotLightsAttribute) {
                                SpotLightsAttribute attrTyped = (SpotLightsAttribute)attr;
                                switch (textField.getName()) {
                                    case ACTOR_R: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.r = value; } break;
                                    case ACTOR_G: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.g = value; } break;
                                    case ACTOR_B: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.b = value; } break;
                                    case ACTOR_A: if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.a = value; } break;
                                }
                            }
                        }

                        switch (textField.getName()) {
                            case ACTOR_R: color.r = value; break;
                            case ACTOR_G: color.g = value; break;
                            case ACTOR_B: color.b = value; break;
                            case ACTOR_A: color.a = value; break;
                        }

                        if (attr != null && listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
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

                    //if (enabledCheckBox != null && !enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }
                    if (rTF != null) { rTF.setText(String.valueOf((int)(color.r * 255))); } // extending the range from [0:1] to [0:255]
                    if (gTF != null) { gTF.setText(String.valueOf((int)(color.g * 255))); } // extending the range from [0:1] to [0:255]
                    if (bTF != null) { bTF.setText(String.valueOf((int)(color.b * 255))); } // extending the range from [0:1] to [0:255]
                    if (aTF != null) { aTF.setText(String.valueOf((int)(color.a * 255))); } // extending the range from [0:1] to [0:255]

                    T attr = null;
                    attr = container.get(attributeClass, currentType);

                    if (attr != null && index >= 0) {
                        if (attr instanceof DirectionalLightsAttribute) {
                            DirectionalLightsAttribute attrTyped = (DirectionalLightsAttribute)attr;
                            if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.set(color); }
                        }
                        if (attr instanceof PointLightsAttribute) {
                            PointLightsAttribute attrTyped = (PointLightsAttribute)attr;
                            if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.set(color); }
                        }
                        if (attr instanceof SpotLightsAttribute) {
                            SpotLightsAttribute attrTyped = (SpotLightsAttribute)attr;
                            if (index < attrTyped.lights.size) { attrTyped.lights.get(index).color.set(color); }
                        }
                    }

                    if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
                }
            }
        };
    }

    protected abstract void postButtonAdd();
    protected abstract void postButtonRemove();

    private void addButton() {
        TextButton button = new TextButton(String.valueOf(indexedTB.size + 1), this.uiSkin);
        button.setName(ACTOR_BUTTON_PREFIX + String.valueOf(indexedTB.size));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                indexedTB.forEach(btn -> btn.getColor().set(COLOR_UNPRESSED));
                index = Integer.parseInt(actor.getName().replace(ACTOR_BUTTON_PREFIX,""));
                indexedTB.get(index).getColor().set(COLOR_PRESSED);

                if (index >= 0 && index < lights.size) {
                    color.set(lights.get(index).color);
                    if (rTF != null) { rTF.setText(String.valueOf((int)(lights.get(index).color.r * 255))); } // extending the range from [0:1] to [0:255]
                    if (gTF != null) { gTF.setText(String.valueOf((int)(lights.get(index).color.g * 255))); } // extending the range from [0:1] to [0:255]
                    if (bTF != null) { bTF.setText(String.valueOf((int)(lights.get(index).color.b * 255))); } // extending the range from [0:1] to [0:255]
                    if (aTF != null) { aTF.setText(String.valueOf((int)(lights.get(index).color.a * 255))); } // extending the range from [0:1] to [0:255]
                }

                postButtonAdd();
            }
        });

        indexedTB.add(button);

        indexedTBTable.add(button).width(20f).maxWidth(20f);

        if (indexedTB.size == 1) { // the first button added
            index = 0;
            indexedTB.get(index).getColor().set(COLOR_PRESSED);
        }
    }

    @Override
    protected boolean preCreateAttr() {
        return true;
    }

    @Override
    protected void postRemoveAttr() {
        indexedTBTable.reset();
        mnsTextButton.getColor().set(COLOR_DISABLED);
        mnsTextButton.getLabel().getColor().set(COLOR_DISABLED);
    }

    @Override
    protected void resetWidgetsToDefaults() {
        indexedTB = new Array<>(TextButton.class);
        indexedTBTable.reset();
        index = -1;

        if (lights == null || lights.size == 0) { // lights shouldn't be null
            mnsTextButton.getColor().set(COLOR_DISABLED);
            mnsTextButton.getLabel().getColor().set(COLOR_DISABLED);
        } else {
            mnsTextButton.getColor().set(COLOR_UNPRESSED);
            mnsTextButton.getLabel().getColor().set(COLOR_UNPRESSED);
        }

        if (rTF != null) { rTF.setText(String.valueOf((int)(Color.GRAY.r * 255))); } // extending the range from [0:1] to [0:255]
        if (gTF != null) { gTF.setText(String.valueOf((int)(Color.GRAY.g * 255))); } // extending the range from [0:1] to [0:255]
        if (bTF != null) { bTF.setText(String.valueOf((int)(Color.GRAY.b * 255))); } // extending the range from [0:1] to [0:255]
        if (aTF != null) { aTF.setText(String.valueOf((int)(Color.GRAY.a * 255))); } // extending the range from [0:1] to [0:255]

        this.lights.forEach(light -> {
            addButton();
        });

        if (indexedTB.size > 0) {
            index = 0;
            indexedTB.get(index).getColor().set(COLOR_PRESSED);
        }
    }
}
