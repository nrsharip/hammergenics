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

package com.hammergenics.core.stages.ui.auxiliary.types;

import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.util.function.Consumer;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class FloatVisTable extends VisTable {
    public float value;
    public VisLabel titleL;
    public VisTextField valueTF;
    public VisTable valueT;
    public Consumer<Float> setter = null;

    public FloatVisTable(boolean title) {
        this(0f, title, null);
    }
    public FloatVisTable(boolean title, VisLabel titleL) {
        this(0f, title, titleL);
    }

    public FloatVisTable() {
        this(0f, false, null);
    }
    public FloatVisTable(float value) {
        this(value, false, null);
    }
    public FloatVisTable(float value, boolean title) {
        this(value, title, null);
    }
    public FloatVisTable(float value, boolean title, VisLabel titleL) {
        this.value = value;
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Float: "); }

        valueTF = new VisTextField(Float.toString(this.value));
        valueTF.setTextFieldListener(new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());
                    FloatVisTable.this.value = value;
                    handleKeyTyped(value, textField, c);
                    if (setter != null) { setter.accept(value); }
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            }
        });

        valueT = new VisTable();
        valueT.add(valueTF).width(120).maxWidth(120).expandX().left().pad(0.5f);

        if (title) { add(this.titleL).padRight(5f).right(); };
        add(valueT).expandX().fillX();
        row();
    }

    public FloatVisTable setFloat(Float value) {
        this.value = value;
        valueTF.setText(Float.toString(value));
        return this;
    }

    public void handleKeyTyped(float value, VisTextField textField, char c) { }
    public void setSetter(Consumer<Float> setter) { this.setter = setter; }
    public void clearSetter() { this.setter = null; }
}