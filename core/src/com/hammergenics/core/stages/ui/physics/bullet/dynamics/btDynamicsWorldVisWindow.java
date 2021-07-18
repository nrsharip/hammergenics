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

package com.hammergenics.core.stages.ui.physics.bullet.dynamics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btMLCPSolver;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.physics.bullet.dynamics.btConstraintSolversEnum;
import com.hammergenics.physics.bullet.dynamics.btDynamicsWorldTypesEnum;
import com.hammergenics.physics.bullet.dynamics.btMLCPSolversEnum;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

import static com.hammergenics.physics.bullet.dynamics.btConstraintSolversEnum.BT_MLCP_SOLVER;
import static com.hammergenics.utils.HGUtils.btDbgModes;

public class btDynamicsWorldVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public btDynamicsWorld dw;

    public VisLabel dwTypeLabel;

    public VisCheckBox dynamicsCheckBox;
    public VisCheckBox rbCheckBox;
    public VisCheckBox groundCheckBox;

    public VisSelectBox<String> btDebugModeSelectBox;
    public VisSelectBox<btConstraintSolversEnum> constraintSolverSelectBox;
    public VisSelectBox<btMLCPSolversEnum> mlcpAlgorithmSelectBox;

    public VisTextField dwGravityXTF; // dw gravity
    public VisTextField dwGravityYTF; // dw gravity
    public VisTextField dwGravityZTF; // dw gravity

    public Vector3 dwGravity = new Vector3();
    
    public btDynamicsWorldVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Dynamics World");

        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        this.dw = btDynamicsWorldTypesEnum.selected.dynamicsWorld;

        init();

        btDebugModeSelectBox.getSelection().setProgrammaticChangeEvents(false);
        btDebugModeSelectBox.setSelectedIndex(btDbgModes.indexOfValue(dw.getDebugDrawer().getDebugMode(), false));
        btDebugModeSelectBox.getSelection().setProgrammaticChangeEvents(true);

        constraintSolverSelectBox.getSelection().setProgrammaticChangeEvents(false);
        constraintSolverSelectBox.setSelected(btConstraintSolversEnum.findByType(dw.getConstraintSolver().getSolverType()));
        constraintSolverSelectBox.getSelection().setProgrammaticChangeEvents(true);

        add(dwTypeLabel).colspan(2).center().expandX();
        row();

        VisTable row01 = new VisTable();
        row01.add(dynamicsCheckBox).expandX();
        row01.add(rbCheckBox).expandX();
        row01.add(groundCheckBox).expandX();
        add(row01).colspan(2).center().expandX().fillX();
        row();

        add(new VisLabel("bullet debug mode:")).padRight(5f).right();
        add(btDebugModeSelectBox).expandX().fillX().center();
        row();

        add(new VisLabel("constraint solver:")).padRight(5f).right();
        add(constraintSolverSelectBox).expandX().fillX().center();
        row();

        add(new VisLabel("algorithm (for MLCP only):")).padRight(5f).right();
        add(mlcpAlgorithmSelectBox).expandX().fillX().center();
        row();

        VisTable lblTable1 = new VisTable();
        lblTable1.add(new VisLabel("x")).expandX().center();
        lblTable1.add(new VisLabel("y")).expandX().center();
        lblTable1.add(new VisLabel("z")).expandX().center();

        add().right();
        add(lblTable1).expandX().fillX();
        row();

        VisTable dwGravityTable = new VisTable();
        dwGravityTable.add(dwGravityXTF).width(120).maxWidth(120).left();
        dwGravityTable.add(dwGravityYTF).width(120).maxWidth(120).left();
        dwGravityTable.add(dwGravityZTF).width(120).maxWidth(120).left();
        add(new VisLabel("gravity:")).padRight(5f).right();
        add(dwGravityTable).left();
        row();

    }

    public void init() {
        dwTypeLabel = new VisLabel("");

        dynamicsCheckBox = new VisCheckBox("enable dynamics");
        dynamicsCheckBox.setChecked(false);
        dynamicsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (!dynamicsCheckBox.isChecked()) modelES.eng.arrangeInSpiral(stage.origScaleCheckBox.isChecked());
            }
        });

        rbCheckBox = new VisCheckBox("rigid body");
        rbCheckBox.setChecked(false);

        groundCheckBox = new VisCheckBox("ground");
        groundCheckBox.setChecked(false);

        btDebugModeSelectBox = new VisSelectBox<>();
        btDebugModeSelectBox.clearItems();
        btDebugModeSelectBox.setItems(btDbgModes.keys().toArray());
        btDebugModeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // btIDebugDraw.h
                // enum DebugDrawModes
                // {
                //     DBG_NoDebug=0,
                //     DBG_DrawWireframe = 1,
                //     DBG_DrawAabb=2,
                //     DBG_DrawFeaturesText=4,
                //     DBG_DrawContactPoints=8,
                //     DBG_NoDeactivation=16,
                //     DBG_NoHelpText = 32,
                //     DBG_DrawText=64,
                //     DBG_ProfileTimings = 128,
                //     DBG_EnableSatComparison = 256,
                //     DBG_DisableBulletLCP = 512,
                //     DBG_EnableCCD = 1024,
                //     DBG_DrawConstraints = (1 << 11),
                //     DBG_DrawConstraintLimits = (1 << 12),
                //     DBG_FastWireframe = (1<<13),
                //     DBG_DrawNormals = (1<<14),
                //     DBG_DrawFrames = (1<<15),
                //     DBG_MAX_DEBUG_DRAW_MODE
                // };
                dw.getDebugDrawer().setDebugMode(btDbgModes.get(btDebugModeSelectBox.getSelected()));
            }
        });

        constraintSolverSelectBox = new VisSelectBox<>();
        constraintSolverSelectBox.clearItems();
        constraintSolverSelectBox.setItems(btConstraintSolversEnum.values());
        constraintSolverSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // see getConstraintSolver():
                //    long cPtr = DynamicsJNI.btDynamicsWorld_getConstraintSolver(swigCPtr, this);
                //    return (cPtr == 0) ? null : new btConstraintSolver(cPtr, false);
                // Gdx.app.debug("test", dw.getConstraintSolver().className);
                dw.setConstraintSolver(constraintSolverSelectBox.getSelected().getInstance());
            }
        });

        mlcpAlgorithmSelectBox = new VisSelectBox<>();
        mlcpAlgorithmSelectBox.clearItems();
        mlcpAlgorithmSelectBox.setItems(btMLCPSolversEnum.values());
        mlcpAlgorithmSelectBox.setSelected(btMLCPSolversEnum.current());
        mlcpAlgorithmSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ((btMLCPSolver)BT_MLCP_SOLVER.getInstance()).setMLCPSolver(mlcpAlgorithmSelectBox.getSelected().apply());
            }
        });

        dwGravityXTF = new VisTextField(""); // dw gravity
        dwGravityYTF = new VisTextField(""); // dw gravity
        dwGravityZTF = new VisTextField(""); // dw gravity
    }

    public void updateDynamicsWorld() {
        String dwTypeName = btDynamicsWorldTypesEnum.findByType(dw.getWorldType()).toString();
        dwTypeLabel.setText("Dynamics World Type: " + dwTypeName);

        dwGravity = dw.getGravity();
        dwGravityXTF.setText(Float.toString(dwGravity.x));
        dwGravityYTF.setText(Float.toString(dwGravity.y));
        dwGravityZTF.setText(Float.toString(dwGravity.z));
    }
}