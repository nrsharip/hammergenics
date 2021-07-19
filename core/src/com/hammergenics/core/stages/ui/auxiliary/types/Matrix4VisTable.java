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

import com.badlogic.gdx.math.Matrix4;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import static com.badlogic.gdx.math.Matrix4.*;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class Matrix4VisTable extends VisTable {
    public Matrix4 matrix4;

    public VisLabel titleL;
    public VisTable valueT;

    public VisTextField M00TF, M01TF, M02TF, M03TF;
    public VisTextField M10TF, M11TF, M12TF, M13TF;
    public VisTextField M20TF, M21TF, M22TF, M23TF;
    public VisTextField M30TF, M31TF, M32TF, M33TF;

    public Matrix4VisTable(boolean title) {
        this(null, title, null);
    }
    public Matrix4VisTable(boolean title, VisLabel titleL) {
        this(null, title, titleL);
    }

    public Matrix4VisTable() {
        this(null, false, null);
    }
    public Matrix4VisTable(Matrix4 matrix4) {
        this(matrix4, false, null);
    }
    public Matrix4VisTable(Matrix4 matrix4, boolean title) {
        this(matrix4, title, null);
    }
    public Matrix4VisTable(Matrix4 matrix4, boolean title, VisLabel titleL) {
        if (matrix4 != null) { this.matrix4 = matrix4; } else { this.matrix4 = new Matrix4(); }
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Matrix4: "); }

        M00TF = new VisTextField(Float.toString(this.matrix4.val[M00]));
        M01TF = new VisTextField(Float.toString(this.matrix4.val[M01]));
        M02TF = new VisTextField(Float.toString(this.matrix4.val[M02]));
        M03TF = new VisTextField(Float.toString(this.matrix4.val[M03]));

        M10TF = new VisTextField(Float.toString(this.matrix4.val[M10]));
        M11TF = new VisTextField(Float.toString(this.matrix4.val[M11]));
        M12TF = new VisTextField(Float.toString(this.matrix4.val[M12]));
        M13TF = new VisTextField(Float.toString(this.matrix4.val[M13]));

        M20TF = new VisTextField(Float.toString(this.matrix4.val[M20]));
        M21TF = new VisTextField(Float.toString(this.matrix4.val[M21]));
        M22TF = new VisTextField(Float.toString(this.matrix4.val[M22]));
        M23TF = new VisTextField(Float.toString(this.matrix4.val[M23]));

        M30TF = new VisTextField(Float.toString(this.matrix4.val[M30]));
        M31TF = new VisTextField(Float.toString(this.matrix4.val[M31]));
        M32TF = new VisTextField(Float.toString(this.matrix4.val[M32]));
        M33TF = new VisTextField(Float.toString(this.matrix4.val[M33]));

        valueT = new VisTable();
        VisTable tmp = new VisTable();
        tmp.add(new VisLabel("00: ")).right(); tmp.add(M00TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("01: ")).right(); tmp.add(M01TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("02: ")).right(); tmp.add(M02TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("03: ")).right(); tmp.add(M03TF).width(64).maxWidth(64).pad(0.5f);
        tmp.row();
        tmp.add(new VisLabel("10: ")).right(); tmp.add(M10TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("11: ")).right(); tmp.add(M11TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("12: ")).right(); tmp.add(M12TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("13: ")).right(); tmp.add(M13TF).width(64).maxWidth(64).pad(0.5f);
        tmp.row();
        tmp.add(new VisLabel("20: ")).right(); tmp.add(M20TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("21: ")).right(); tmp.add(M21TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("22: ")).right(); tmp.add(M22TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("23: ")).right(); tmp.add(M23TF).width(64).maxWidth(64).pad(0.5f);
        tmp.row();
        tmp.add(new VisLabel("30: ")).right(); tmp.add(M30TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("31: ")).right(); tmp.add(M31TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("32: ")).right(); tmp.add(M32TF).width(64).maxWidth(64).pad(0.5f);
        tmp.add(new VisLabel("33: ")).right(); tmp.add(M33TF).width(64).maxWidth(64).pad(0.5f);
        valueT.add(tmp);

        if (title) { add(this.titleL).padRight(5f).right(); };
        add(valueT).expandX().fillX();
        row();
    }

    public void setMatrix4(Matrix4 matrix4) {
        this.matrix4 = matrix4;
        update();
    }

    public void update() {
        if (matrix4 != null) {
            M00TF.setText(Float.toString(this.matrix4.val[M00]));
            M01TF.setText(Float.toString(this.matrix4.val[M01]));
            M02TF.setText(Float.toString(this.matrix4.val[M02]));
            M03TF.setText(Float.toString(this.matrix4.val[M03]));

            M10TF.setText(Float.toString(this.matrix4.val[M10]));
            M11TF.setText(Float.toString(this.matrix4.val[M11]));
            M12TF.setText(Float.toString(this.matrix4.val[M12]));
            M13TF.setText(Float.toString(this.matrix4.val[M13]));

            M20TF.setText(Float.toString(this.matrix4.val[M20]));
            M21TF.setText(Float.toString(this.matrix4.val[M21]));
            M22TF.setText(Float.toString(this.matrix4.val[M22]));
            M23TF.setText(Float.toString(this.matrix4.val[M23]));

            M30TF.setText(Float.toString(this.matrix4.val[M30]));
            M31TF.setText(Float.toString(this.matrix4.val[M31]));
            M32TF.setText(Float.toString(this.matrix4.val[M32]));
            M33TF.setText(Float.toString(this.matrix4.val[M33]));
        } else {
            M00TF.setText(""); M01TF.setText(""); M02TF.setText(""); M03TF.setText("");
            M10TF.setText(""); M11TF.setText(""); M12TF.setText(""); M13TF.setText("");
            M20TF.setText(""); M21TF.setText(""); M22TF.setText(""); M23TF.setText("");
            M30TF.setText(""); M31TF.setText(""); M32TF.setText(""); M33TF.setText("");
        }
    }
}