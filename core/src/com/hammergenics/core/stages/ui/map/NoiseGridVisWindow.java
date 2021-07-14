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

package com.hammergenics.core.stages.ui.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.map.HGGrid;
import com.hammergenics.map.TerrainChunk;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

import java.util.Arrays;

public class NoiseGridVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public VisTextButton genNoiseTextButton;
    public Texture textureNoise;
    public Array<NoiseStageTable> noiseStageTables = new Array<>(true, 16, NoiseStageTable.class);
    public VisTextButton roundStepNoiseTextButton;
    public VisCheckBox previewNoiseGrid;
    // TODO: preview image should be more general option on the stage level
    public VisCheckBox previewNoiseImage;
    public VisTextField noiseYScaleTF;
    public VisTextField noiseStepTF;
    public float noiseYScale = 20f;
    public float noiseStep = 0.05f;

    public NoiseGridVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Noise Grid");

        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        noiseStageTables.addAll(
                new NoiseStageTable(stage, 16, 0.8f),
                new NoiseStageTable(stage, 8, 0.1f),
                new NoiseStageTable(stage, 4, 0.1f),
                new NoiseStageTable(stage, 1, 0.05f)
        );

        genNoiseTextButton = new VisTextButton("gen noise grid");
        stage.unpressButton(genNoiseTextButton);
        genNoiseTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Array<HGGrid.NoiseStageInfo> stages = new Array<>(true, 16, HGGrid.NoiseStageInfo.class);
                for (NoiseStageTable nst: noiseStageTables) {
                    if (nst.enabledCB.isChecked()) {
                        if (!nst.applySeedCB.isChecked()) { nst.stageInfo.seed = -1; }
                        stages.add(nst.stageInfo);
                    }
                }
                if (stages.size == 0) { return super.touchDown(event, x, y, pointer, button); }

                eng.generateNoise(noiseYScale, stages);

                for (NoiseStageTable nst: noiseStageTables) {
                    nst.noiseGridSeedTF.getColor().set(Color.WHITE);
                    nst.noiseGridSeedTF.setText(Integer.toString(nst.stageInfo.seed));
                }

                Array<HGGrid> grids = Arrays.stream(eng.chunks.toArray())
                        .map(TerrainChunk::getGridNoise).collect(Array::new, Array::add, Array::addAll);
                textureNoise = stage.mapGenerationTable.imageGrid("Noise Grid Preview", grids);
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        roundStepNoiseTextButton = new VisTextButton("round: step");
        stage.unpressButton(roundStepNoiseTextButton);
        roundStepNoiseTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.roundNoiseToStep(noiseStep);

                Array<HGGrid> grids = Arrays.stream(eng.chunks.toArray())
                        .map(TerrainChunk::getGridNoise).collect(Array::new, Array::add, Array::addAll);
                textureNoise = stage.mapGenerationTable.imageGrid("Noise Grid Preview - round step: " + noiseStep, grids);
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        noiseYScaleTF = new VisTextField(Float.toString(noiseYScale));
        noiseYScaleTF.setTextFieldListener((textField, c) -> {
            try {
                float value = Float.parseFloat(textField.getText());
                if (value <= 0) { textField.getColor().set(Color.PINK); return; }
                noiseYScale = value;
                textField.getColor().set(Color.WHITE);
            } catch (NumberFormatException e) {
                textField.getColor().set(Color.PINK);
            }
        });

        noiseStepTF = new VisTextField(Float.toString(noiseStep));
        noiseStepTF.setTextFieldListener((textField, c) -> {
            try {
                float value = Float.parseFloat(textField.getText());
                if (value >= 1 || value <= 0) { textField.getColor().set(Color.PINK); return; }
                noiseStep = value;
                textField.getColor().set(Color.WHITE);
            } catch (NumberFormatException e) {
                textField.getColor().set(Color.PINK);
            }
        });

        previewNoiseGrid = new VisCheckBox("preview noise grid");
        previewNoiseGrid.setChecked(true);

        // TODO: preview image should be more general option on the stage level
        previewNoiseImage = new VisCheckBox("preview noise image");
        previewNoiseImage.setChecked(true);
        previewNoiseImage.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (previewNoiseImage.isChecked()) {
                    stage.imagePreviewCell.setActor(stage.textureImage);
                } else {
                    stage.imagePreviewCell.clearActor();
                }
            }
        });

        for (NoiseStageTable nst: noiseStageTables) {
            add(nst).center().expandX().fillX();
            row().pad(0.5f);
        }

        VisTable noiseGridTable = new VisTable();
        noiseGridTable.add(new VisLabel("yScale:")).right().padRight(5f);
        noiseGridTable.add(noiseYScaleTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(genNoiseTextButton).center().expandX().fillX().padRight(5f);

        noiseGridTable.add(new VisLabel("step:")).right().padRight(5f);
        noiseGridTable.add(noiseStepTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(roundStepNoiseTextButton).center().expandX().fillX();

        add(noiseGridTable).center().expandX().fillX();
        row();

        VisTable previewCheckBoxes = new VisTable();
        previewCheckBoxes.add(previewNoiseGrid).center().expandX().fillX();
        previewCheckBoxes.add(previewNoiseImage).center().expandX().fillX();

        add(previewCheckBoxes).center().expandX().fillX();
        row();
    }


    public static class NoiseStageTable extends VisTable {
        public ModelEditStage stage;

        public HGGrid.NoiseStageInfo stageInfo = new HGGrid.NoiseStageInfo();

        public VisCheckBox enabledCB;
        public VisCheckBox applySeedCB;
        public VisTextField radiusTF;
        public VisTextField modifierTF;

        public VisTextField noiseGridSeedTF;

        public NoiseStageTable(ModelEditStage stage, int radius, float modifier) {
            this.stage = stage;
            this.stageInfo.radius = radius;
            this.stageInfo.modifier = modifier;

            radiusTF = new VisTextField(Integer.toString(radius));
            radiusTF.setTextFieldListener((textField, c) -> {
                try {
                    int value = Integer.parseInt(textField.getText());
                    if (value < 0) { textField.getColor().set(Color.PINK); return; }
                    stageInfo.radius = value;
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            });
            modifierTF = new VisTextField(Float.toString(modifier));
            modifierTF.setTextFieldListener((textField, c) -> {
                try {
                    float value = Float.parseFloat(textField.getText());
                    if (value > 1 || value < 0) { textField.getColor().set(Color.PINK); return; }
                    stageInfo.modifier = value;
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            });

            enabledCB = new VisCheckBox("enabled");
            enabledCB.setChecked(true);

            applySeedCB = new VisCheckBox("apply seed");
            applySeedCB.setChecked(false);

            noiseGridSeedTF = new VisTextField(Integer.toString(stageInfo.seed));
            noiseGridSeedTF.setTextFieldListener((textField, c) -> {
                try {
                    int value = Integer.parseInt(textField.getText());
                    if (value <= 0) { textField.getColor().set(Color.PINK); return; }
                    stageInfo.seed = value;
                    applySeedCB.setChecked(true);
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    applySeedCB.setChecked(false);
                    textField.getColor().set(Color.PINK);
                }
            });

            add(enabledCB).padRight(5f);
            add(new VisLabel("radius:")).right().padRight(5f);
            add(radiusTF).width(40).maxWidth(40).padRight(5f);
            add(new VisLabel("modifier:")).right().padRight(5f);
            add(modifierTF).width(40).maxWidth(40).padRight(5f);
            add(new VisLabel("seed:")).right().padRight(5f);
            add(noiseGridSeedTF).width(200).maxWidth(200).padRight(5f);
            add(applySeedCB).padRight(5f);
        }
    }
}