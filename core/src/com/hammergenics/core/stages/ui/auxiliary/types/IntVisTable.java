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

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class IntVisTable extends VisTable {
    public int value;
    public VisLabel titleL;
    public VisTextField octTF;
    public VisTextField decTF;
    public VisTextField hexTF;
    public VisTable valueT;

    public IntVisTable(boolean title) {
        this(0, title, null);
    }
    public IntVisTable(boolean title, VisLabel titleL) {
        this(0, title, titleL);
    }

    public IntVisTable() {
        this(0, false, null);
    }
    public IntVisTable(int value) {
        this(value, false, null);
    }
    public IntVisTable(int value, boolean title) {
        this(value, title, null);
    }
    public IntVisTable(int value, boolean title, VisLabel titleL) {
        this.value = value;
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Float: "); }

        octTF = new VisTextField(Integer.toOctalString(this.value));
        decTF = new VisTextField(Integer.toString(this.value));
        hexTF = new VisTextField(Integer.toHexString(this.value));

        valueT = new VisTable();
        VisTable tmp = new VisTable();
        tmp.add(new VisLabel("hex: ")).right(); tmp.add(hexTF).width(90).maxWidth(90).pad(0.5f);
        tmp.add(new VisLabel("dec: ")).right(); tmp.add(decTF).width(90).maxWidth(90).pad(0.5f);
        tmp.add(new VisLabel("oct: ")).right(); tmp.add(octTF).width(90).maxWidth(90).pad(0.5f);
        valueT.add(tmp);

        if (title) { add(this.titleL).padRight(5f).right(); };
        add(valueT).expandX().fillX();
        row();
    }

    public void setInt(int value) {
        this.value = value;
        octTF.setText(Integer.toOctalString(value));
        decTF.setText(Integer.toString(value));
        hexTF.setText(Integer.toHexString(value));
    }
}