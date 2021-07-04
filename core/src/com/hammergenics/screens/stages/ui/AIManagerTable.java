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

package com.hammergenics.screens.stages.ui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.EditableModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AIManagerTable extends HGTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;

    public CheckBox steerCheckBox;

    public TextField lvxTF = null; // linear velocity
    public TextField lvyTF = null; // linear velocity
    public TextField lvzTF = null; // linear velocity
    public TextField avTF = null; // angular velocity
    public TextField brTF = null; // bounding radius
    public CheckBox taggedCB = null; // tagged

    public TextField zlstTF = null; // zero linear speed threshold
    public TextField mlsTF = null; // the maximum linear speed
    public TextField mlaTF = null; // the maximum linear acceleration
    public TextField masTF = null; // the maximum angular speed
    public TextField maaTF = null; // the maximum angular acceleration

    public TextField pxTF = null; // position
    public TextField pyTF = null; // position
    public TextField pzTF = null; // position
    public TextField oTF = null; // orientation

    public AIManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.stage = stage;

        init();

        add(steerCheckBox).right(); add().expandX().fillX(); row();

        Table lvLblTable1 = new Table();
        lvLblTable1.add(new Label("x", stage.skin)).expandX().center();
        lvLblTable1.add(new Label("y", stage.skin)).expandX().center();
        lvLblTable1.add(new Label("z", stage.skin)).expandX().center();

        add().right(); add(lvLblTable1).expandX().fillX(); row();

        Table lvTable = new Table();
        lvTable.add(lvxTF).width(120).maxWidth(120).left();
        lvTable.add(lvyTF).width(120).maxWidth(120).left();
        lvTable.add(lvzTF).width(120).maxWidth(120).left();

        add(new Label("linear velocity:", stage.skin)).right(); add(lvTable).left(); row();
        add(new Label("angular velocity:", stage.skin)).right(); add(avTF).width(120).maxWidth(120).left(); row();
        add(new Label("bounding radius:", stage.skin)).right(); add(brTF).width(120).maxWidth(120).left(); row();
        add().expandX().fillX().right(); add(taggedCB).width(120).maxWidth(120).left(); row();

        add(new Label("zero linear speed threshold:", stage.skin)).right(); add(zlstTF).width(120).maxWidth(120).left(); row();
        add(new Label("maximum linear speed:", stage.skin)).right(); add(mlsTF).width(120).maxWidth(120).left(); row();
        add(new Label("maximum linear acceleration:", stage.skin)).right(); add(mlaTF).width(120).maxWidth(120).left(); row();
        add(new Label("maximum angular speed:", stage.skin)).right(); add(masTF).width(120).maxWidth(120).left(); row();
        add(new Label("maximum angular acceleration:", stage.skin)).right(); add(maaTF).width(120).maxWidth(120).left(); row();

        Table lvLblTable2 = new Table();
        lvLblTable2.add(new Label("x", stage.skin)).expandX().center();
        lvLblTable2.add(new Label("y", stage.skin)).expandX().center();
        lvLblTable2.add(new Label("z", stage.skin)).expandX().center();

        add().right(); add(lvLblTable2).expandX().fillX(); row();

        Table pTable = new Table();
        pTable.add(pxTF).width(120).maxWidth(120).left();
        pTable.add(pyTF).width(120).maxWidth(120).left();
        pTable.add(pzTF).width(120).maxWidth(120).left();

        add(new Label("position:", stage.skin)).right(); add(pTable).left(); row();
        add(new Label("orientation:", stage.skin)).right(); add(oTF).width(120).maxWidth(120).left(); row();
    }

    private void init() {
        steerCheckBox = new CheckBox("enable steering", stage.skin);
        steerCheckBox.setChecked(false);

        lvxTF = new TextField("", stage.skin); // linear velocity
        lvyTF = new TextField("", stage.skin); // linear velocity
        lvzTF = new TextField("", stage.skin); // linear velocity

        avTF = new TextField("", stage.skin); // angular velocity
        brTF = new TextField("", stage.skin); // bounding radius
        taggedCB = new CheckBox("tagged", stage.skin); // tagged

        zlstTF = new TextField("", stage.skin); // zero linear speed threshold
        mlsTF = new TextField("", stage.skin); // the maximum linear speed
        mlaTF = new TextField("", stage.skin); // the maximum linear acceleration
        masTF = new TextField("", stage.skin); // the maximum angular speed
        maaTF = new TextField("", stage.skin); // the maximum angular acceleration

        pxTF = new TextField("", stage.skin); // position
        pyTF = new TextField("", stage.skin); // position
        pzTF = new TextField("", stage.skin); // position
        oTF = new TextField("", stage.skin); // orientation
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
}
