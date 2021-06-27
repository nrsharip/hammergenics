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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.HGEngine.NoiseStageInfo;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class MapGenerationTable extends HGTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;
    public DebugModelInstance dbgModelInstance;

    public TextButton genNoiseTextButton = null;
    public TextButton genCellTextButton = null;
    public TextButton genDungTextButton = null;

    public Texture textureNoise;
    public Texture textureCellular;
    public Texture textureDungeon;

    public Array<NoiseStageTable> noiseStageTables = new Array<>(true, 16, NoiseStageTable.class);
    public float noiseYScale = 20f;
    public TextField noiseYScaleTF;

    public MapGenerationTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.eng = modelES.eng;
        this.stage = stage;

        init();

        for (NoiseStageTable nst: noiseStageTables) {
            add(nst).center().expandX().fillX();
            row();
        }

        Table genNoiseGridTable = new Table();
        genNoiseGridTable.add(new Label("yScale:", stage.skin)).right();
        genNoiseGridTable.add(noiseYScaleTF).width(60).maxWidth(60).padRight(5f);
        genNoiseGridTable.add(genNoiseTextButton).center().expandX().fillX();

        add(genNoiseGridTable).center().expandX().fillX();
        row();

        Table genGridTable = new Table();
        genGridTable.add(genCellTextButton).center().expandX().fillX();
        genGridTable.add(genDungTextButton).center().expandX().fillX();

        add(genGridTable).center().expandX().fillX();
        row();
    }

    public static class NoiseStageTable extends HGTable {
        public ModelEditStage stage;

        public NoiseStageInfo stageInfo = new NoiseStageInfo();

        public CheckBox enabledCB;
        public TextField radiusTF;
        public TextField modifierTF;

        public TextField noiseGridSeedTF;

        public NoiseStageTable(ModelEditStage stage, int radius, float modifier) {
            super(stage.skin);
            this.stage = stage;
            this.stageInfo.radius = radius;
            this.stageInfo.modifier = modifier;

            radiusTF = new TextField(Integer.toString(radius), stage.skin);
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
            modifierTF = new TextField(Float.toString(modifier), stage.skin);
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

            enabledCB = new CheckBox("enabled", stage.skin);
            enabledCB.setChecked(true);

            noiseGridSeedTF = new TextField(Integer.toString(stageInfo.seed), stage.skin);
            noiseGridSeedTF.setDisabled(true);

            add(enabledCB).padRight(5f);
            add(new Label("radius:", stage.skin)).right();
            add(radiusTF).width(40).maxWidth(40).padRight(5f);
            add(new Label("modifier:", stage.skin)).right();
            add(modifierTF).width(40).maxWidth(40).padRight(5f);
            add(new Label("seed:", stage.skin)).right();
            add(noiseGridSeedTF).width(200).maxWidth(200).padRight(5f);
        }
    }

    public void init() {
        noiseStageTables.addAll(
                new NoiseStageTable(stage, 32, 0.6f),
                new NoiseStageTable(stage, 16, 0.2f),
                new NoiseStageTable(stage, 8, 0.1f),
                new NoiseStageTable(stage, 4, 0.1f),
                new NoiseStageTable(stage, 1, 0.05f)
        );

        genNoiseTextButton = new TextButton("gen noise grid", stage.skin);
        stage.unpressButton(genNoiseTextButton);
        genNoiseTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Array<NoiseStageInfo> stages = new Array<>(true, 16, NoiseStageInfo.class);
                for (NoiseStageTable nst: noiseStageTables) {
                    if (nst.enabledCB.isChecked()) { stages.add(nst.stageInfo); }
                }
                if (stages.size == 0) { return super.touchDown(event, x, y, pointer, button); }

                eng.generateNoise(noiseYScale, stages);

                for (NoiseStageTable nst: noiseStageTables) {
                    nst.noiseGridSeedTF.setText(Integer.toString(nst.stageInfo.seed));
                }

                imageNoise();
                // see Image (Texture texture) for example on how to convert Texture to Image
                stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureNoise)));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        genCellTextButton = new TextButton("gen cellular grid", stage.skin);
        stage.unpressButton(genCellTextButton);
        genCellTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateCellular();
                imageCellular();
                // see Image (Texture texture) for example on how to convert Texture to Image
                stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureCellular)));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        genDungTextButton = new TextButton("gen dungeon grid", stage.skin);
        stage.unpressButton(genDungTextButton);
        genDungTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateDungeon();
                imageDungeon();
                // see Image (Texture texture) for example on how to convert Texture to Image
                stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureDungeon)));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        noiseYScaleTF = new TextField(Float.toString(noiseYScale), stage.skin);
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
    }

    // see: https://github.com/czyzby/noise4j
    public void imageNoise() {
        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

        final Color color = new Color();
        for (int x = 0; x < eng.gridNoise.getWidth(); x++) {
            for (int y = 0; y < eng.gridNoise.getHeight(); y++) {
                final float cell = eng.gridNoise.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }
        textureNoise = new Texture(map);
        map.dispose();
    }

    // see: https://github.com/czyzby/noise4j
    public void imageCellular() {
        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
        final Color color = new Color();
        for (int x = 0; x < eng.gridCellular.getWidth(); x++) {
            for (int y = 0; y < eng.gridCellular.getHeight(); y++) {
                final float cell = eng.gridCellular.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }

        textureCellular = new Texture(map);
        map.dispose();
    }

    // see: https://github.com/czyzby/noise4j
    public void imageDungeon() {
        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

        final Color color = new Color();
        for (int x = 0; x < eng.gridDungeon.getWidth(); x++) {
            for (int y = 0; y < eng.gridDungeon.getHeight(); y++) {
                final float cell = 1f - eng.gridDungeon.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }

        textureDungeon = new Texture(map);
        map.dispose();
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.infoBCell.setActor(stage.textureImage);
        stage.editCell.setActor(this);
    }
}
