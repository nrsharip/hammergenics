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

import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

public class Vector3VisTable extends VisTable {
    public Vector3 vector3;

    public VisLabel titleL;
    public VisLabel xL;
    public VisLabel yL;
    public VisLabel zL;
    public VisTable labelsT;
    public VisTable valueT;

    public VisTextField xTF;
    public VisTextField yTF;
    public VisTextField zTF;

    public Vector3VisTable(boolean vertical) {
        this(null, vertical, false, false, null, null, null, null);
    }
    public Vector3VisTable(boolean vertical, boolean title) {
        this(null, vertical, title, false, null, null, null, null);
    }
    public Vector3VisTable(boolean vertical, boolean title, boolean labels) {
        this(null, vertical, title, labels, null, null, null, null);
    }
    public Vector3VisTable(boolean vertical, boolean title, boolean labels, VisLabel titleL) {
        this(null, vertical, title, labels, titleL, null, null, null);
    }

    public Vector3VisTable() {
        this(null, false, false, false, null, null, null, null);
    }
    public Vector3VisTable(Vector3 vector3) {
        this(vector3, false, false, false, null, null, null, null);
    }
    public Vector3VisTable(Vector3 vector3, boolean vertical) {
        this(vector3, vertical, false, false, null, null, null, null);
    }
    public Vector3VisTable(Vector3 vector3, boolean vertical, boolean title, boolean labels) {
        this(vector3, vertical, title, labels, null, null, null, null);
    }
    public Vector3VisTable(Vector3 vector3, boolean vertical, boolean title, boolean labels, VisLabel titleL) {
        this(vector3, vertical, title, labels, titleL, null, null, null);
    }
    public Vector3VisTable(Vector3 vector3, boolean vertical, boolean title, boolean labels, VisLabel titleL, VisLabel xL, VisLabel yL, VisLabel zL) {
        if (vector3 != null) { this.vector3 = vector3; } else { this.vector3 = new Vector3(); }
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Vector3: "); }
        if (xL != null) { this.xL = xL; } else { this.xL = new VisLabel("x"); }
        if (yL != null) { this.yL = yL; } else { this.yL = new VisLabel("y"); }
        if (zL != null) { this.zL = zL; } else { this.zL = new VisLabel("z"); }

        xTF = new VisTextField(Float.toString(this.vector3.x));
        yTF = new VisTextField(Float.toString(this.vector3.y));
        zTF = new VisTextField(Float.toString(this.vector3.z));

        if (!vertical) {
            if (labels) {
                labelsT = new VisTable();
                labelsT.add(this.xL).maxWidth(120).expandX().center();
                labelsT.add(this.yL).maxWidth(120).expandX().center();
                labelsT.add(this.zL).maxWidth(120).expandX().center();

                if (title) { add().right(); }
                add(labelsT).expandX().fillX();
                row();
            }

            valueT = new VisTable();
            valueT.add(xTF).width(120).maxWidth(120).expandX().center().pad(0.5f);
            valueT.add(yTF).width(120).maxWidth(120).expandX().center().pad(0.5f);
            valueT.add(zTF).width(120).maxWidth(120).expandX().center().pad(0.5f);

            if (title) { add(this.titleL).padRight(5f).right(); };
            add(valueT).expandX().fillX();
            row();
        }
    }

    public void setVector3(Vector3 vector3) {
        this.vector3 = vector3;
        update();
    }

    public void update() {
        if (vector3 != null) {
            xTF.setText(Float.toString(vector3.x));
            yTF.setText(Float.toString(vector3.y));
            zTF.setText(Float.toString(vector3.z));
        } else {
            xTF.setText("");
            yTF.setText("");
            zTF.setText("");
        }
    }
}