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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class BooleanVisTable extends VisTable {
    public boolean value;
    public VisLabel titleL;
    public VisCheckBox valueCB;
    public VisTable valueT;

    public BooleanVisTable() {
        this(false, false, null);
    }
    public BooleanVisTable(boolean value) {
        this(value, false, null);
    }
    public BooleanVisTable(boolean value, boolean title) {
        this(value, title, null);
    }
    public BooleanVisTable(boolean value, boolean title, VisLabel titleL) {
        this.value = value;
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Boolean: "); }

        valueCB = new VisCheckBox("", value);
        valueCB.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleChanged(valueCB.isChecked(), event, actor);
            }
        });

        valueT = new VisTable();
        valueT.add(valueCB).expandX().left().pad(0.5f);

        if (title) { add(this.titleL).padRight(5f).right(); };
        add(valueT).expandX().fillX();
        row();
    }

    public void setBoolean(boolean value) {
        this.value = value;
        valueCB.setChecked(value);
    }

    public void handleChanged(boolean value, ChangeListener.ChangeEvent event, Actor actor) { }
}