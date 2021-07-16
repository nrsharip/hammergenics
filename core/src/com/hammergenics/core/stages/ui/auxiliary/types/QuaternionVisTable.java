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

import com.badlogic.gdx.math.Quaternion;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

public class QuaternionVisTable extends VisTable {
    public Quaternion quaternion;

    public VisLabel titleL;
    public VisLabel xL;
    public VisLabel yL;
    public VisLabel zL;
    public VisLabel wL;
    public VisTable labelsT;
    public VisTable valueT;

    public VisTextField xTF;
    public VisTextField yTF;
    public VisTextField zTF;
    public VisTextField wTF;

    public QuaternionVisTable(boolean vertical) {
        this(null, vertical, false, false, null, null, null, null, null);
    }
    public QuaternionVisTable(boolean vertical, boolean title) {
        this(null, vertical, title, false, null, null, null, null, null);
    }
    public QuaternionVisTable(boolean vertical, boolean title, boolean labels) {
        this(null, vertical, title, labels, null, null, null, null, null);
    }
    public QuaternionVisTable(boolean vertical, boolean title, boolean labels, VisLabel titleL) {
        this(null, vertical, title, labels, titleL, null, null, null, null);
    }

    public QuaternionVisTable() {
        this(null, false, false, false, null, null, null, null, null);
    }
    public QuaternionVisTable(Quaternion quaternion) {
        this(quaternion, false, false, false, null, null, null, null, null);
    }
    public QuaternionVisTable(Quaternion quaternion, boolean vertical) {
        this(quaternion, vertical, false, false, null, null, null, null, null);
    }
    public QuaternionVisTable(Quaternion quaternion, boolean vertical, boolean title, boolean labels) {
        this(quaternion, vertical, title, labels, null, null, null, null, null);
    }
    public QuaternionVisTable(Quaternion quaternion, boolean vertical, boolean title, boolean labels, VisLabel titleL) {
        this(quaternion, vertical, title, labels, titleL, null, null, null, null);
    }
    public QuaternionVisTable(Quaternion quaternion, boolean vertical, boolean title, boolean labels, VisLabel titleL, VisLabel xL, VisLabel yL, VisLabel zL, VisLabel wL) {
        if (quaternion != null) { this.quaternion = quaternion; } else { this.quaternion = new Quaternion(); }
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Quaternion: "); }
        if (xL != null) { this.xL = xL; } else { this.xL = new VisLabel("x"); }
        if (yL != null) { this.yL = yL; } else { this.yL = new VisLabel("y"); }
        if (zL != null) { this.zL = zL; } else { this.zL = new VisLabel("z"); }
        if (wL != null) { this.wL = wL; } else { this.wL = new VisLabel("w"); }

        xTF = new VisTextField(Float.toString(this.quaternion.x));
        yTF = new VisTextField(Float.toString(this.quaternion.y));
        zTF = new VisTextField(Float.toString(this.quaternion.z));
        wTF = new VisTextField(Float.toString(this.quaternion.w));

        if (!vertical) {
            if (labels) {
                labelsT = new VisTable();
                labelsT.add(this.xL).maxWidth(90).expandX().center();
                labelsT.add(this.yL).maxWidth(90).expandX().center();
                labelsT.add(this.zL).maxWidth(90).expandX().center();
                labelsT.add(this.wL).maxWidth(90).expandX().center();

                if (title) { add().right(); }
                add(labelsT).expandX().fillX();
                row();
            }

            valueT = new VisTable();
            valueT.add(xTF).width(90).maxWidth(90).expandX().center().pad(0.5f);
            valueT.add(yTF).width(90).maxWidth(90).expandX().center().pad(0.5f);
            valueT.add(zTF).width(90).maxWidth(90).expandX().center().pad(0.5f);
            valueT.add(wTF).width(90).maxWidth(90).expandX().center().pad(0.5f);

            if (title) { add(this.titleL).padRight(5f).right(); };
            add(valueT).expandX().fillX();
            row();
        }
    }

    public void setQuaternion(Quaternion quaternion) {
        this.quaternion = quaternion;
        update();
    }

    public void update() {
        if (quaternion != null) {
            xTF.setText(Float.toString(quaternion.x));
            yTF.setText(Float.toString(quaternion.y));
            zTF.setText(Float.toString(quaternion.z));
            wTF.setText(Float.toString(quaternion.w));
        } else {
            xTF.setText("");
            yTF.setText("");
            zTF.setText("");
            wTF.setText("");
        }
    }
}