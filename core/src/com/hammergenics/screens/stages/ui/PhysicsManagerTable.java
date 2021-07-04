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


import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btMLCPSolver;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.HGEngine;
import com.hammergenics.HGEngine.btConstraintSolversEnum;
import com.hammergenics.HGEngine.btMLCPSolversEnum;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.EditableModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;

import static com.hammergenics.HGEngine.btConstraintSolversEnum.BT_MLCP_SOLVER;
import static com.hammergenics.utils.HGUtils.btDbgModes;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicsManagerTable extends HGTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;
    public btDynamicsWorld dw;
    public EditableModelInstance dbgModelInstance;

    public CheckBox dynamicsCheckBox;
    public CheckBox rbCheckBox;
    public CheckBox groundCheckBox;

    public SelectBox<String> btDebugModeSelectBox = null;
    public SelectBox<btConstraintSolversEnum> constraintSolverSelectBox = null;
    public SelectBox<btMLCPSolversEnum> mlcpAlgorithmSelectBox = null;

    public PhysicsManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.eng = modelES.eng;
        this.dw = modelES.eng.dynamicsWorld;
        this.stage = stage;

        init();

        Table row01 = new Table();
        row01.add(dynamicsCheckBox).expandX();
        row01.add(rbCheckBox).expandX();
        row01.add(groundCheckBox).expandX();
        add(row01).colspan(2).center().expandX().fillX().row();

        add(new Label("bullet debug mode:", stage.skin)).padRight(5f).right();
        add(btDebugModeSelectBox).expandX().fillX().center();
        row();

        add(new Label("constraint solver:", stage.skin)).padRight(5f).right();
        add(constraintSolverSelectBox).expandX().fillX().center();
        row();

        add(new Label("algorithm (for MLCP only):", stage.skin)).padRight(5f).right();
        add(mlcpAlgorithmSelectBox).expandX().fillX().center();
        row();
    }

    private void init() {
        dynamicsCheckBox = new CheckBox("enable dynamics", stage.skin);
        dynamicsCheckBox.setChecked(false);
        dynamicsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (!dynamicsCheckBox.isChecked()) modelES.eng.arrangeInSpiral(stage.origScaleCheckBox.isChecked());
            }
        });

        rbCheckBox = new CheckBox("rigid body", stage.skin);
        rbCheckBox.setChecked(false);

        groundCheckBox = new CheckBox("ground", stage.skin);
        groundCheckBox.setChecked(false);

        btDebugModeSelectBox = new SelectBox<>(stage.skin);
        btDebugModeSelectBox.clearItems();
        btDebugModeSelectBox.setItems(btDbgModes.keys().toArray());
        btDebugModeSelectBox.setSelectedIndex(btDbgModes.indexOfValue(dw.getDebugDrawer().getDebugMode(), false));
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

        constraintSolverSelectBox = new SelectBox<>(stage.skin);
        constraintSolverSelectBox.clearItems();
        constraintSolverSelectBox.setItems(btConstraintSolversEnum.values());
        constraintSolverSelectBox.setSelected(btConstraintSolversEnum.findByType(dw.getConstraintSolver().getSolverType()));
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

        mlcpAlgorithmSelectBox = new SelectBox<>(stage.skin);
        mlcpAlgorithmSelectBox.clearItems();
        mlcpAlgorithmSelectBox.setItems(btMLCPSolversEnum.values());
        mlcpAlgorithmSelectBox.setSelected(btMLCPSolversEnum.current());
        mlcpAlgorithmSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ((btMLCPSolver)BT_MLCP_SOLVER.getInstance()).setMLCPSolver(mlcpAlgorithmSelectBox.getSelected().apply());
            }
        });
    }

    public void setDbgModelInstance(EditableModelInstance mi) {
        this.dbgModelInstance = mi;
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.editCell.setActor(this);
    }
}
