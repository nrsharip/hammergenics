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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

import static com.hammergenics.utils.HGUtils.gl20_i2s;
import static com.hammergenics.utils.HGUtils.gl20_s2i;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class BlendingAttributeTable extends AttributeTable<BlendingAttribute> {
    private static final String ACTOR_SRC = "sourceFunction";
    private static final String ACTOR_DST = "destFunction";

    private VisTextField opacityTF = null;
    private VisSelectBox<String> srcFuncSB = null;
    private VisSelectBox<String> dstFuncSB = null;
    private VisCheckBox blendedCB = null;
    private VisCheckBox mapSrc2DstCB = null;

    // boolean blended - Whether this material should be considered blended (default: true). This is used for sorting (back to front instead of front to back).
    // sourceFunction  - Specifies how the (incoming) red, green, blue, and alpha source blending factors are computed (default: GL_SRC_ALPHA)
    // destFunction    - Specifies how the (existing) red, green, blue, and alpha destination blending factors are computed (default: GL_ONE_MINUS_SRC_ALPHA)
    // float opacity   - The opacity used as source alpha value, ranging from 0 (fully transparent) to 1 (fully opaque) (default: 1)

    private ArrayMap<String, String> src2dst;
    private Array<String> itemsSB;

    private VisTextField.TextFieldListener opacityTFListener = null;
    private ChangeListener selectBoxListener = null;
    private ChangeListener blendedCBListener = null;

    public BlendingAttributeTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
        super(container, modelES, BlendingAttribute.class, window, type, alias);

        createListeners();

        setSrc2dst(new ArrayMap<>(String.class, String.class));
        setItemsSB(new Array<>(String.class));
        //Gdx.app.debug(getClass().getSimpleName(), "Retrieved function list: \n" + itemsSB.toString("\n"));

        opacityTF = new VisTextField("1");

        blendedCB = new VisCheckBox("blended");
        blendedCB.setChecked(true);

        mapSrc2DstCB = new VisCheckBox("match src with dst");
        mapSrc2DstCB.setChecked(true);

        srcFuncSB = new VisSelectBox<>();
        srcFuncSB.setName(ACTOR_SRC);

        dstFuncSB = new VisSelectBox<>();
        dstFuncSB.setName(ACTOR_DST);

        srcFuncSB.clearItems();
        dstFuncSB.clearItems();

        srcFuncSB.setItems(itemsSB.toArray(String.class));
        dstFuncSB.setItems(itemsSB.toArray(String.class));

        opacityTF.setTextFieldListener(opacityTFListener);
        blendedCB.addListener(blendedCBListener);
        srcFuncSB.addListener(selectBoxListener);
        dstFuncSB.addListener(selectBoxListener);

        VisTable line1 = new VisTable();
        VisTable line2 = new VisTable();

        line1.add(enabledCheckBox);
        line1.add(new VisLabel("opacity:")).right();
        line1.add(opacityTF).width(80).maxWidth(80).padRight(5f);
        line1.add(blendedCB).padRight(5f);
        line1.add(mapSrc2DstCB).padRight(5f);
        line1.add().expandX();

        line2.add(new VisLabel("src:")).right();
        line2.add(srcFuncSB);
        line2.add(new VisLabel("dst:")).right();
        line2.add(dstFuncSB);

        add(line1).fillX();
        row();
        add(line2).fillX();
    }

    private void createListeners() {
        blendedCBListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (container != null && currentType != 0) {
                    BlendingAttribute attr = container.get(BlendingAttribute.class, currentType);
                    if (attr != null) {
                        attr.blended = blendedCB.isChecked();
                    }
                }
            }
        };

        opacityTFListener = new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (value > 1 || value < 0) {
                        textField.getColor().set(Color.PINK);
                        return;
                    }

                    if (container != null && currentType != 0) {
                        BlendingAttribute attr = null;
                        attr = container.get(BlendingAttribute.class, currentType);

                        if (attr != null) { attr.opacity = value; }

                        if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
                    }
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            }
        };

        selectBoxListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (container != null && currentType != 0) {

                    BlendingAttribute attr = container.get(BlendingAttribute.class, currentType);
                    if (attr != null) {
                        switch (actor.getName()) {
                            case ACTOR_SRC:
                                attr.sourceFunction = gl20_s2i.get(srcFuncSB.getSelected());
                                if (mapSrc2DstCB.isChecked() && src2dst.containsKey(srcFuncSB.getSelected())) {
                                    String mapped = src2dst.get(srcFuncSB.getSelected());

                                    dstFuncSB.getSelection().setProgrammaticChangeEvents(false);
                                    dstFuncSB.setSelected(mapped);
                                    dstFuncSB.getSelection().setProgrammaticChangeEvents(true);

                                    attr.destFunction = gl20_s2i.get(mapped);
                                }
                                break;
                            case ACTOR_DST:
                                attr.destFunction = gl20_s2i.get(dstFuncSB.getSelected());
                                if (mapSrc2DstCB.isChecked() && src2dst.containsKey(dstFuncSB.getSelected())) {
                                    String mapped = src2dst.get(dstFuncSB.getSelected());

                                    srcFuncSB.getSelection().setProgrammaticChangeEvents(false);
                                    srcFuncSB.setSelected(mapped);
                                    srcFuncSB.getSelection().setProgrammaticChangeEvents(true);

                                    attr.sourceFunction = gl20_s2i.get(mapped);
                                }
                                break;
                        }
                    }

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
        if (opacityTF != null) { opacityTF.setText(String.valueOf(1f)); }
        if (srcFuncSB != null) { srcFuncSB.setSelected(gl20_i2s.get(GL20.GL_SRC_ALPHA));}
        if (dstFuncSB != null) { dstFuncSB.setSelected(gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_ALPHA));}
        if (blendedCB != null) { blendedCB.setChecked(true); }
    }

    @Override
    protected void fetchWidgetsFromAttribute(BlendingAttribute attr) {
        if (opacityTF != null) { opacityTF.setText(String.valueOf(attr.opacity)); }
        if (srcFuncSB != null) { srcFuncSB.setSelected(attr.sourceFunction == 0 ?
                "GL_ZERO" : attr.sourceFunction == 1 ? "GL_ONE" : gl20_i2s.get(attr.sourceFunction));}
        if (dstFuncSB != null) { dstFuncSB.setSelected(attr.destFunction == 0 ?
                "GL_ZERO" : attr.destFunction == 1 ? "GL_ONE" : gl20_i2s.get(attr.destFunction));}
        if (blendedCB != null) { blendedCB.setChecked(attr.blended); }
    }

    @Override
    protected void postRemoveAttr() {

    }

    @Override
    protected BlendingAttribute createAttribute(String alias) {
        return new BlendingAttribute(); // using no-arg constructor
    }

    private void setSrc2dst(ArrayMap<String, String> map) {
        // https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glBlendFunc.xhtml
        // Color Components:
        // * first source  (Rs0,Gs0,Bs0,As0)
        // * second source (Rs1,Gs1,Bs1,As1)
        // * destination   ( Rd, Gd, Bd, Ad)
        // * The color specified by glBlendColor is referred to as (Rc,Gc,Bc,Ac)
        //
        //   NOTE: The values are in range 0 and (kR,kG,kB,kA): kc = 2^mc − 1, where
        //         (mR,mG,mB,mA) is the number of red, green, blue, and alpha bitplanes (https://en.wikipedia.org/wiki/Bit_plane)
        //
        // Scale Factors:
        // * source      (sR,sG,sB,sA)
        // * destination (dR,dG,dB,dA)
        //
        //   NOTE: scale factors have range [0,1]
        //         in the table below denoted as (fR,fG,fB,fA) for either source or destination factors

        // Parameter:                   (fR,fG,fB,fA):
        // GL_ZERO                                  (     0,      0,      0,      0)
        // GL_ONE                                   (     1,      1,      1,      1)
        // GL_SRC_COLOR                             (Rs0/kR, Gs0/kG, Bs0/kB, As0/kA)
        // GL_ONE_MINUS_SRC_COLOR       (1,1,1,1) − (Rs0/kR, Gs0/kG, Bs0/kB, As0/kA)
        // GL_DST_COLOR                             ( Rd/kR,  Gd/kG,  Bd/kB,  Ad/kA)
        // GL_ONE_MINUS_DST_COLOR       (1,1,1,1) − ( Rd/kR,  Gd/kG,  Bd/kB,  Ad/kA)
        // GL_SRC_ALPHA                             (As0/kA, As0/kA, As0/kA, As0/kA)
        // GL_ONE_MINUS_SRC_ALPHA       (1,1,1,1) − (As0/kA, As0/kA, As0/kA, As0/kA)
        // GL_DST_ALPHA                             ( Ad/kA,  Ad/kA,  Ad/kA,  Ad/kA)
        // GL_ONE_MINUS_DST_ALPHA       (1,1,1,1) − ( Ad/kA,  Ad/kA,  Ad/kA,  Ad/kA)
        // GL_CONSTANT_COLOR                        (    Rc,     Gc,     Bc,     Ac)
        // GL_ONE_MINUS_CONSTANT_COLOR  (1,1,1,1) − (    Rc,     Gc,     Bc,     Ac)
        // GL_CONSTANT_ALPHA                        (    Ac,     Ac,     Ac,     Ac)
        // GL_ONE_MINUS_CONSTANT_ALPHA  (1,1,1,1) − (    Ac,     Ac,     Ac,     Ac)
        // GL_SRC_ALPHA_SATURATE                    (     i,      i,      i,      1)
        // GL_SRC1_COLOR                            (Rs1/kR, Gs1/kG, Bs1/kB, As1/kA)
        // GL_ONE_MINUS_SRC1_COLOR      (1,1,1,1) − (Rs1/kR, Gs1/kG, Bs1/kB, As1/kA)
        // GL_SRC1_ALPHA                            (As1/kA, As1/kA, As1/kA, As1/kA)
        // GL_ONE_MINUS_SRC1_ALPHA      (1,1,1,1) − (As1/kA, As1/kA, As1/kA, As1/kA)

        map.put("GL_ZERO", "GL_ONE");//map.put(gl20_i2s.get(GL20.GL_ZERO), gl20_i2s.get(GL20.GL_ONE)); // GL_ZERO (duplicates: GL_FALSE, GL_NO_ERROR, GL_NONE)
        map.put("GL_ONE", "GL_ZERO");//map.put(gl20_i2s.get(GL20.GL_ONE), gl20_i2s.get(GL20.GL_ZERO)); // GL_ONE (duplicates: GL_ES_VERSION_2_0, GL_TRUE)
        map.put(gl20_i2s.get(GL20.GL_SRC_COLOR), gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_COLOR));           // GL_SRC_COLOR
        map.put(gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_COLOR), gl20_i2s.get(GL20.GL_SRC_COLOR));           // GL_ONE_MINUS_SRC_COLOR
        map.put(gl20_i2s.get(GL20.GL_DST_COLOR), gl20_i2s.get(GL20.GL_ONE_MINUS_DST_COLOR));           // GL_DST_COLOR
        map.put(gl20_i2s.get(GL20.GL_ONE_MINUS_DST_COLOR), gl20_i2s.get(GL20.GL_DST_COLOR));           // GL_ONE_MINUS_DST_COLOR
        map.put(gl20_i2s.get(GL20.GL_SRC_ALPHA), gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_ALPHA));           // GL_SRC_ALPHA
        map.put(gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_ALPHA), gl20_i2s.get(GL20.GL_SRC_ALPHA));           // GL_ONE_MINUS_SRC_ALPHA
        map.put(gl20_i2s.get(GL20.GL_DST_ALPHA), gl20_i2s.get(GL20.GL_ONE_MINUS_DST_ALPHA));           // GL_DST_ALPHA
        map.put(gl20_i2s.get(GL20.GL_ONE_MINUS_DST_ALPHA), gl20_i2s.get(GL20.GL_DST_ALPHA));           // GL_ONE_MINUS_DST_ALPHA
        map.put(gl20_i2s.get(GL20.GL_CONSTANT_COLOR), gl20_i2s.get(GL20.GL_ONE_MINUS_CONSTANT_COLOR)); // GL_CONSTANT_COLOR
        map.put(gl20_i2s.get(GL20.GL_ONE_MINUS_CONSTANT_COLOR), gl20_i2s.get(GL20.GL_CONSTANT_COLOR)); // GL_ONE_MINUS_CONSTANT_COLOR
        map.put(gl20_i2s.get(GL20.GL_CONSTANT_ALPHA), gl20_i2s.get(GL20.GL_ONE_MINUS_CONSTANT_ALPHA)); // GL_CONSTANT_ALPHA
        map.put(gl20_i2s.get(GL20.GL_ONE_MINUS_CONSTANT_ALPHA), gl20_i2s.get(GL20.GL_CONSTANT_ALPHA)); // GL_ONE_MINUS_CONSTANT_ALPHA
        //map.put(gl20_i2s.get(GL20.), gl20_i2s.get(GL20.));

        src2dst = map;
    }

    private void setItemsSB(Array<String> items) {
        String[] array = new String[] {
                // all available src/dst constants in GL20.java
                "GL_ZERO", //gl20_i2s.get(GL20.GL_ZERO), // - duplicates found (duplicates: GL_FALSE, GL_NO_ERROR, GL_NONE)
                "GL_ONE",  //gl20_i2s.get(GL20.GL_ONE),  // - duplicates found (duplicates: GL_ES_VERSION_2_0, GL_TRUE)
                gl20_i2s.get(GL20.GL_SRC_COLOR),
                gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_COLOR),
                gl20_i2s.get(GL20.GL_DST_COLOR),
                gl20_i2s.get(GL20.GL_ONE_MINUS_DST_COLOR),
                gl20_i2s.get(GL20.GL_SRC_ALPHA),
                gl20_i2s.get(GL20.GL_ONE_MINUS_SRC_ALPHA),
                gl20_i2s.get(GL20.GL_DST_ALPHA),
                gl20_i2s.get(GL20.GL_ONE_MINUS_DST_ALPHA),
                gl20_i2s.get(GL20.GL_CONSTANT_COLOR),
                gl20_i2s.get(GL20.GL_ONE_MINUS_CONSTANT_COLOR),
                gl20_i2s.get(GL20.GL_CONSTANT_ALPHA),
                gl20_i2s.get(GL20.GL_ONE_MINUS_CONSTANT_ALPHA),
                gl20_i2s.get(GL20.GL_SRC_ALPHA_SATURATE)
        };
        items.addAll(array, 0, array.length);

        itemsSB = items;
    }
}