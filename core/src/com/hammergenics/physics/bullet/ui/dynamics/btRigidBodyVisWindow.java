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

package com.hammergenics.physics.bullet.ui.dynamics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.physics.bullet.dynamics.btRigidBodyProxy;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

public class btRigidBodyVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public btRigidBody rb;
    public btRigidBodyProxy rbp;

    public VisTextField rbCompXTF; // Center Of Mass Position
    public VisTextField rbCompYTF; // Center Of Mass Position
    public VisTextField rbCompZTF; // Center Of Mass Position

    public btRigidBodyVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Rigid Body");

        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        init();

        VisTable lblTable2 = new VisTable();
        lblTable2.add(new VisLabel("x")).expandX().center();
        lblTable2.add(new VisLabel("y")).expandX().center();
        lblTable2.add(new VisLabel("z")).expandX().center();

        add().right();
        add(lblTable2).expandX().fillX();
        row();

        VisTable rbCompTable = new VisTable();
        rbCompTable.add(rbCompXTF).width(120).maxWidth(120).left();
        rbCompTable.add(rbCompYTF).width(120).maxWidth(120).left();
        rbCompTable.add(rbCompZTF).width(120).maxWidth(120).left();
        add(new VisLabel("center of mass position:")).padRight(5f).right();
        add(rbCompTable).expandX().fillX();
        row();
    }

    public void init() {
        rbCompXTF = new VisTextField(""); // Center Of Mass Position
        rbCompYTF = new VisTextField(""); // Center Of Mass Position
        rbCompZTF = new VisTextField(""); // Center Of Mass Position
    }

    public void updateRigidBody(EditableModelInstance mi) {
        rb = null;
        rbp = null;
        if (mi != null && mi.rigidBody != null) {
            rb = mi.rigidBody;
            if (mi.rigidBodyProxy == null) {
                mi.rigidBodyProxy = new btRigidBodyProxy(mi.rigidBody);
            }
            rbp = mi.rigidBodyProxy;
            rbp.setInstance(rb); // reassuring that rigid body is legitimate
            rbp.update();

            rbCompXTF.setText(Float.toString(rbp.rbCenterOfMassPosition.x));
            rbCompYTF.setText(Float.toString(rbp.rbCenterOfMassPosition.y));
            rbCompZTF.setText(Float.toString(rbp.rbCenterOfMassPosition.z));
        } else {
            rbCompXTF.setText("");
            rbCompYTF.setText("");
            rbCompZTF.setText("");
        }
    }
}