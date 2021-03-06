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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;

import static com.hammergenics.core.stages.ui.attributes.AttributeTable.TextButtonsTextEnum.SELECT_COLOR;
import static com.hammergenics.core.stages.ui.attributes.ColorAttributeTable.*;
import static com.hammergenics.HGUtils.color_s2c;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class BaseLightsAttributeTable<T extends Attribute, L extends BaseLight<L>> extends AttributeTable<T> {
    private static final String ACTOR_BUTTON_PREFIX = "button_";

    protected Class<L> lightClass;
    protected Color color = new Color().set(Color.GRAY);
    protected int index = -1;

    // r, g, b, a
    protected VisTextField rTF = null;
    protected VisTextField gTF = null;
    protected VisTextField bTF = null;
    protected VisTextField aTF = null;
    // color select box
    protected VisSelectBox<String> colorSB = null;
    protected VisTextButton selectColorTB = null;
    private ColorPickerListener colorPickerListener;

    // + and - buttons
    protected VisTextButton plsTextButton = null;
    protected VisTextButton mnsTextButton = null;
    // table to contain the indexed buttons (1, 2, 3 etc.)
    protected VisTable indexedTBTable = new VisTable();

    public Cell<?> cell11, cell12, cell21, cell22;
    public Cell<?> cell31, cell32, cell41, cell42;

    private VisTextField.TextFieldListener rgbaTextFieldListener;
    private ChangeListener colorSelectBoxListener;

    public Array<VisTextButton> indexedTB = null;
    protected Array<L> lights;

    protected abstract L createLight();

    public BaseLightsAttributeTable(Attributes container, ModelEditScreen modelES, Class<T> aClass,
                                    Class<L> lightClass, VisWindow window, Long type, String alias) {
        super(container, modelES, aClass, window, type, alias);
        this.lightClass = lightClass;
        lights = new Array<>(lightClass);

        plsTextButton = new VisTextButton("+");
        modelES.stage.unpressButton(plsTextButton);
        plsTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelES.stage.isDisabled(plsTextButton) || lights == null || indexedTB == null) { return; }

                if (!enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }

                addButton();

                L light = createLight();

                lights.add(light);

                indexedTB.get(indexedTB.size - 1).setChecked(true); // "pressing" the button added

                modelES.stage.enableButton(mnsTextButton);

                if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
            }
        });

        mnsTextButton = new VisTextButton("-");
        mnsTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelES.stage.isDisabled(mnsTextButton) || lights == null || indexedTB == null) { return; }

                if (indexedTB.size > 2 && modelES.stage.isPressed(indexedTB.get(indexedTB.size - 1))) {
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
                    modelES.stage.disableButton(mnsTextButton);
                }

                postButtonRemove();

                if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
            }
        });

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        rTF =  new VisTextField("150"); rTF.setName(ACTOR_R);
        gTF =  new VisTextField("150"); gTF.setName(ACTOR_G);
        bTF =  new VisTextField("150"); bTF.setName(ACTOR_B);
        aTF =  new VisTextField("150"); aTF.setName(ACTOR_A);

        rTF.setTextFieldListener(rgbaTextFieldListener);
        gTF.setTextFieldListener(rgbaTextFieldListener);
        bTF.setTextFieldListener(rgbaTextFieldListener);
        aTF.setTextFieldListener(rgbaTextFieldListener);

        // Select Box: Color
        colorSB = new VisSelectBox<>();
        colorSB.clearItems();
        if (color_s2c != null && color_s2c.size > 0) { colorSB.setItems(color_s2c.keys().toArray()); }
        colorSB.addListener(colorSelectBoxListener);

        selectColorTB = new VisTextButton("select"); SELECT_COLOR.seize(selectColorTB);
        selectColorTB.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                modelES.stage.colorPicker.setListener(colorPickerListener);
                modelES.stage.colorPicker.getPicker().setColor(color);
                //displaying picker with fade in animation
                getStage().addActor(modelES.stage.colorPicker.fadeIn());
            }
        });

        window.getTitleTable().add(enabledCheckBox).padLeft(10f);

        // Standard Color Layout:
        VisTable line1 = new VisTable();
        VisTable line2 = new VisTable();
        VisTable line3 = new VisTable();

        line1.add(new VisLabel("lights:")).padRight(5f).right();
        line1.add(mnsTextButton).width(20f).maxWidth(20f).padRight(1f);
        line1.add(plsTextButton).width(20f).maxWidth(20f);
        line1.add(indexedTBTable).expandX().left();

        line2.add(new VisLabel("r:")).padRight(5f).right();
        line2.add(rTF).width(40).maxWidth(40);
        cell11 = line2.add();
        cell12 = line2.add();

        line2.row();
        line2.add(new VisLabel("g:")).padRight(5f).right();
        line2.add(gTF).width(40).maxWidth(40);
        cell21 = line2.add();
        cell22 = line2.add();

        line2.row();
        line2.add(new VisLabel("b:")).padRight(5f).right();
        line2.add(bTF).width(40).maxWidth(40);
        cell31 = line2.add();
        cell32 = line2.add();

        line2.row();
        line2.add(new VisLabel("a:")).padRight(5f).right();
        line2.add(aTF).width(40).maxWidth(40);
        cell41 = line2.add();
        cell42 = line2.add();

        line3.add(new VisLabel("color:")).colspan(2).padRight(5f).right();
        line3.add(selectColorTB).fillX();
        line3.add(colorSB).fillX();

        add(line1).expandX().fillX().left();
        row();
        add(line2).expandX().fillX().center();
        row();
        add(line3).expandX().fillX().center();
    }

    private void createListeners() {
        rgbaTextFieldListener =  new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
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
                    setAttributeColor(color_s2c.get(colorSB.getSelected()));
                }
            }
        };

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
    }

    public void setAttributeColor(Color color) {
        this.color.set(color);

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

        if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
    }

    protected abstract void postButtonAdd();
    protected abstract void postButtonRemove();

    private void addButton() {
        VisTextButton button = new VisTextButton(String.valueOf(indexedTB.size + 1));
        button.setName(ACTOR_BUTTON_PREFIX + String.valueOf(indexedTB.size));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                indexedTB.forEach(btn -> modelES.stage.unpressButton(btn));
                index = Integer.parseInt(actor.getName().replace(ACTOR_BUTTON_PREFIX,""));
                modelES.stage.pressButton(indexedTB.get(index));

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
            modelES.stage.pressButton(indexedTB.get(index));
        }
    }

    @Override
    protected boolean preCreateAttr() {
        return true;
    }

    @Override
    protected void postRemoveAttr() {
        indexedTBTable.reset();
        modelES.stage.disableButton(mnsTextButton);
    }

    @Override
    protected void resetWidgetsToDefaults() {
        indexedTB = new Array<>(VisTextButton.class);
        indexedTBTable.reset();
        index = -1;

        if (lights == null || lights.size == 0) { // lights shouldn't be null
            modelES.stage.disableButton(mnsTextButton);
        } else {
            modelES.stage.enableButton(mnsTextButton);
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
            modelES.stage.pressButton(indexedTB.get(index));
        }
    }
}
