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

import com.badlogic.gdx.math.Matrix3;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import static com.badlogic.gdx.math.Matrix3.*;

public class Matrix3VisTable extends VisTable {
    public Matrix3 matrix3;

    public VisLabel titleL;
    public VisTable valueT;

    public VisTextField M00TF, M01TF, M02TF;
    public VisTextField M10TF, M11TF, M12TF;
    public VisTextField M20TF, M21TF, M22TF;

    public Matrix3VisTable(boolean title) {
        this(null, title, null);
    }
    public Matrix3VisTable(boolean title, VisLabel titleL) {
        this(null, title, titleL);
    }

    public Matrix3VisTable() {
        this(null, false, null);
    }
    public Matrix3VisTable(Matrix3 matrix3) {
        this(matrix3, false, null);
    }
    public Matrix3VisTable(Matrix3 matrix3, boolean title) {
        this(matrix3, title, null);
    }
    public Matrix3VisTable(Matrix3 matrix3, boolean title, VisLabel titleL) {
        if (matrix3 != null) { this.matrix3 = matrix3; } else { this.matrix3 = new Matrix3(); }
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Matrix3: "); }

        M00TF = new VisTextField(Float.toString(this.matrix3.val[M00]));
        M01TF = new VisTextField(Float.toString(this.matrix3.val[M01]));
        M02TF = new VisTextField(Float.toString(this.matrix3.val[M02]));

        M10TF = new VisTextField(Float.toString(this.matrix3.val[M10]));
        M11TF = new VisTextField(Float.toString(this.matrix3.val[M11]));
        M12TF = new VisTextField(Float.toString(this.matrix3.val[M12]));

        M20TF = new VisTextField(Float.toString(this.matrix3.val[M20]));
        M21TF = new VisTextField(Float.toString(this.matrix3.val[M21]));
        M22TF = new VisTextField(Float.toString(this.matrix3.val[M22]));

        valueT = new VisTable();
        valueT.add(M00TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.add(M01TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.add(M02TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.row();
        valueT.add(M10TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.add(M11TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.add(M12TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.row();
        valueT.add(M20TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.add(M21TF).width(120).maxWidth(120).expandX().center().pad(0.5f);
        valueT.add(M22TF).width(120).maxWidth(120).expandX().center().pad(0.5f);

        if (title) { add(this.titleL).padRight(5f).right(); };
        add(valueT).expandX().fillX();
        row();
    }

    public void setMatrix3(Matrix3 matrix3) {
        this.matrix3 = matrix3;
        update();
    }

    public void update() {
        if (matrix3 != null) {
            M00TF.setText(Float.toString(this.matrix3.val[M00]));
            M01TF.setText(Float.toString(this.matrix3.val[M01]));
            M02TF.setText(Float.toString(this.matrix3.val[M02]));

            M10TF.setText(Float.toString(this.matrix3.val[M10]));
            M11TF.setText(Float.toString(this.matrix3.val[M11]));
            M12TF.setText(Float.toString(this.matrix3.val[M12]));

            M20TF.setText(Float.toString(this.matrix3.val[M20]));
            M21TF.setText(Float.toString(this.matrix3.val[M21]));
            M22TF.setText(Float.toString(this.matrix3.val[M22]));
        } else {
            M00TF.setText(""); M01TF.setText(""); M02TF.setText("");
            M10TF.setText(""); M11TF.setText(""); M12TF.setText("");
            M20TF.setText(""); M21TF.setText(""); M22TF.setText("");
        }
    }
}