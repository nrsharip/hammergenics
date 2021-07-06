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

package com.hammergenics.core.stages.ui;

import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AIManagerTable extends VisTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;

    public VisCheckBox steerCheckBox;

    public VisTextField lvxTF = null; // linear velocity
    public VisTextField lvyTF = null; // linear velocity
    public VisTextField lvzTF = null; // linear velocity
    public VisTextField avTF = null; // angular velocity
    public VisTextField brTF = null; // bounding radius
    public VisCheckBox taggedCB = null; // tagged

    public VisTextField zlstTF = null; // zero linear speed threshold
    public VisTextField mlsTF = null; // the maximum linear speed
    public VisTextField mlaTF = null; // the maximum linear acceleration
    public VisTextField masTF = null; // the maximum angular speed
    public VisTextField maaTF = null; // the maximum angular acceleration

    public VisTextField pxTF = null; // position
    public VisTextField pyTF = null; // position
    public VisTextField pzTF = null; // position
    public VisTextField oTF = null; // orientation

    public AIManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        this.modelES = modelES;
        this.stage = stage;

        init();

        add(steerCheckBox).right(); add().expandX().fillX(); row();

        VisTable lvLblTable1 = new VisTable();
        lvLblTable1.add(new VisLabel("x")).expandX().center();
        lvLblTable1.add(new VisLabel("y")).expandX().center();
        lvLblTable1.add(new VisLabel("z")).expandX().center();

        add().right(); add(lvLblTable1).expandX().fillX(); row();

        VisTable lvTable = new VisTable();
        lvTable.add(lvxTF).width(120).maxWidth(120).left();
        lvTable.add(lvyTF).width(120).maxWidth(120).left();
        lvTable.add(lvzTF).width(120).maxWidth(120).left();

        add(new VisLabel("linear velocity:")).right(); add(lvTable).left(); row();
        add(new VisLabel("angular velocity:")).right(); add(avTF).width(120).maxWidth(120).left(); row();
        add(new VisLabel("bounding radius:")).right(); add(brTF).width(120).maxWidth(120).left(); row();
        add().expandX().fillX().right(); add(taggedCB).width(120).maxWidth(120).left(); row();

        add(new VisLabel("zero linear speed threshold:")).right(); add(zlstTF).width(120).maxWidth(120).left(); row();
        add(new VisLabel("maximum linear speed:")).right(); add(mlsTF).width(120).maxWidth(120).left(); row();
        add(new VisLabel("maximum linear acceleration:")).right(); add(mlaTF).width(120).maxWidth(120).left(); row();
        add(new VisLabel("maximum angular speed:")).right(); add(masTF).width(120).maxWidth(120).left(); row();
        add(new VisLabel("maximum angular acceleration:")).right(); add(maaTF).width(120).maxWidth(120).left(); row();

        VisTable lvLblTable2 = new VisTable();
        lvLblTable2.add(new VisLabel("x")).expandX().center();
        lvLblTable2.add(new VisLabel("y")).expandX().center();
        lvLblTable2.add(new VisLabel("z")).expandX().center();

        add().right(); add(lvLblTable2).expandX().fillX(); row();

        VisTable pTable = new VisTable();
        pTable.add(pxTF).width(120).maxWidth(120).left();
        pTable.add(pyTF).width(120).maxWidth(120).left();
        pTable.add(pzTF).width(120).maxWidth(120).left();

        add(new VisLabel("position:")).right(); add(pTable).left(); row();
        add(new VisLabel("orientation:")).right(); add(oTF).width(120).maxWidth(120).left(); row();
    }

    private void init() {
        steerCheckBox = new VisCheckBox("enable steering");
        steerCheckBox.setChecked(false);

        lvxTF = new VisTextField(""); // linear velocity
        lvyTF = new VisTextField(""); // linear velocity
        lvzTF = new VisTextField(""); // linear velocity

        avTF = new VisTextField(""); // angular velocity
        brTF = new VisTextField(""); // bounding radius
        taggedCB = new VisCheckBox("tagged"); // tagged

        zlstTF = new VisTextField(""); // zero linear speed threshold
        mlsTF = new VisTextField(""); // the maximum linear speed
        mlaTF = new VisTextField(""); // the maximum linear acceleration
        masTF = new VisTextField(""); // the maximum angular speed
        maaTF = new VisTextField(""); // the maximum angular acceleration

        pxTF = new VisTextField(""); // position
        pyTF = new VisTextField(""); // position
        pzTF = new VisTextField(""); // position
        oTF = new VisTextField(""); // orientation
    }

    public void updateSteerable() {
        EditableModelInstance mi = dbgModelInstance;
        if (mi != null) {
            lvxTF.setText(Float.toString(mi.linearVelocity.x));
            lvyTF.setText(Float.toString(mi.linearVelocity.y));
            lvzTF.setText(Float.toString(mi.linearVelocity.z));
            avTF.setText(Float.toString(mi.angularVelocity)); brTF.setText(Float.toString(mi.boundingRadius));
            taggedCB.setChecked(mi.tagged);
            zlstTF.setText(Float.toString(mi.zeroLinearSpeedThreshold));
            mlsTF.setText(Float.toString(mi.maxLinearSpeed)); mlaTF.setText(Float.toString(mi.maxLinearAcceleration));
            masTF.setText(Float.toString(mi.maxAngularSpeed)); maaTF.setText(Float.toString(mi.maxAngularAcceleration));
            pxTF.setText(Float.toString(mi.position.x));
            pyTF.setText(Float.toString(mi.position.y));
            pzTF.setText(Float.toString(mi.position.z));
            oTF.setText(Float.toString(mi.orientation));
        } else {
            lvxTF.setText(""); lvyTF.setText(""); lvzTF.setText("");
            avTF.setText(""); brTF.setText("");
            taggedCB.setChecked(false);
            zlstTF.setText(""); mlsTF.setText(""); mlaTF.setText(""); masTF.setText(""); maaTF.setText("");
            pxTF.setText(""); pyTF.setText(""); pzTF.setText(""); oTF.setText("");
        }
    }

    public void setDbgModelInstance(EditableModelInstance mi) {
        this.dbgModelInstance = mi;

        updateSteerable();
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.editCell.setActor(this);
    }

    public void applyLocale() {

    }
}
