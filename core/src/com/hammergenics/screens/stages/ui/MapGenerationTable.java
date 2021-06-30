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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.HGEngine;
import com.hammergenics.map.HGGrid;
import com.hammergenics.map.HGGrid.NoiseStageInfo;
import com.hammergenics.map.TerrainPartsEnum;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;

import static com.hammergenics.map.TerrainPartsEnum.TRRN_FLAT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE_CORN_INN;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE_CORN_OUT;

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
    public TextButton roundDigitsNoiseTextButton = null;
    public TextButton roundStepNoiseTextButton = null;

    public CheckBox previewNoiseGrid = null;
    // TODO: preview image should be more general option on the stage level
    public CheckBox previewNoiseImage = null;
    public CheckBox previewTerrain = null;

    public TextField noiseYScaleTF;
    public TextField noiseDigitsTF;
    public TextField noiseStepTF;

    public float noiseYScale = 20f;
    public int noiseDigits = 5;
    public float noiseStep = 0.05f;

    public TextButton applyTerrainTextButton = null;
    public TextButton clearTerrainTextButton = null;
    public ArrayMap<TerrainPartsEnum, SelectBox<FileHandle>> trrnSelectBoxes =
            new ArrayMap<>(true, 16, TerrainPartsEnum.class, SelectBox.class);

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

        Table noiseGridTable = new Table();
        noiseGridTable.add(new Label("yScale:", stage.skin)).right();
        noiseGridTable.add(noiseYScaleTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(genNoiseTextButton).center().expandX().fillX();

        noiseGridTable.add(new Label("step:", stage.skin)).right();
        noiseGridTable.add(noiseStepTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(roundStepNoiseTextButton).center().expandX().fillX();

        noiseGridTable.add(new Label("digits:", stage.skin)).right();
        noiseGridTable.add(noiseDigitsTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(roundDigitsNoiseTextButton).center().expandX().fillX();

        add(noiseGridTable).center().expandX().fillX();
        row();

        Table previewCheckBoxes = new Table();
        previewCheckBoxes.add(previewNoiseGrid).center().expandX().fillX();
        previewCheckBoxes.add(previewNoiseImage).center().expandX().fillX();

        add(previewCheckBoxes).center().expandX().fillX();
        row();

        Table genGridTable = new Table();
        genGridTable.add(genCellTextButton).center().expandX().fillX();
        genGridTable.add(genDungTextButton).center().expandX().fillX();

        add(genGridTable).center().expandX().fillX();
        row();

        Table trrnPartTable = new Table();
        for (ObjectMap.Entry<TerrainPartsEnum, SelectBox<FileHandle>> entry: trrnSelectBoxes) {
            TerrainPartsEnum part = entry.key;
            SelectBox<FileHandle> sb = entry.value;

            trrnPartTable.add(new Label(part.description + ":", stage.skin)).right();
            trrnPartTable.add(sb).center().expandX().fillX();
            trrnPartTable.row();
        }
        add(trrnPartTable).center().expandX().fillX();
        row();

        Table trrnTable = new Table();
        trrnTable.add(applyTerrainTextButton).center().expandX().fillX();
        trrnTable.add(previewTerrain).center().expandX().fillX();
        trrnTable.add(clearTerrainTextButton).center().expandX().fillX();
        add(trrnTable).center().expandX().fillX();
        row();
    }

    public static class NoiseStageTable extends HGTable {
        public ModelEditStage stage;

        public NoiseStageInfo stageInfo = new NoiseStageInfo();

        public CheckBox enabledCB;
        public CheckBox applySeedCB;
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

            applySeedCB = new CheckBox("apply seed", stage.skin);
            applySeedCB.setChecked(false);

            noiseGridSeedTF = new TextField(Integer.toString(stageInfo.seed), stage.skin);
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
            add(new Label("radius:", stage.skin)).right();
            add(radiusTF).width(40).maxWidth(40).padRight(5f);
            add(new Label("modifier:", stage.skin)).right();
            add(modifierTF).width(40).maxWidth(40).padRight(5f);
            add(new Label("seed:", stage.skin)).right();
            add(noiseGridSeedTF).width(200).maxWidth(200).padRight(5f);
            add(applySeedCB).padRight(5f);
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

                textureNoise = imageGrid(eng.gridNoise00);
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        roundDigitsNoiseTextButton = new TextButton("round: digits", stage.skin);
        stage.unpressButton(roundDigitsNoiseTextButton);
        roundDigitsNoiseTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.roundNoiseToDigits(noiseDigits);

                textureNoise = imageGrid(eng.gridNoise00);
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        roundStepNoiseTextButton = new TextButton("round: step", stage.skin);
        stage.unpressButton(roundStepNoiseTextButton);
        roundStepNoiseTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.roundNoiseToStep(noiseStep);

                textureNoise = imageGrid(eng.gridNoise00);
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
                textureCellular = imageGrid(eng.gridCellular);
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
                textureDungeon = imageGrid(eng.gridDungeon);
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        applyTerrainTextButton = new TextButton("apply terrain parts", stage.skin);
        stage.unpressButton(applyTerrainTextButton);
        applyTerrainTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ArrayMap<TerrainPartsEnum, FileHandle> tp2fh =
                        new ArrayMap<>(true, 16, TerrainPartsEnum.class, FileHandle.class);
                for (ObjectMap.Entry<TerrainPartsEnum, SelectBox<FileHandle>> entry: trrnSelectBoxes) {
                    TerrainPartsEnum part = entry.key;
                    SelectBox<FileHandle> sb = entry.value;

                    if (sb.getItems().size == 0 || sb.getSelectedIndex() == 0) { continue; }
                    tp2fh.put(part, sb.getSelected());
                }

                eng.applyTerrainParts(tp2fh);

                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        clearTerrainTextButton = new TextButton("clear terrain", stage.skin);
        stage.unpressButton(clearTerrainTextButton);
        clearTerrainTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.clearTerrain();

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

        noiseDigitsTF = new TextField(Integer.toString(noiseDigits), stage.skin);
        noiseDigitsTF.setTextFieldListener((textField, c) -> {
            try {
                int value = Integer.parseInt(textField.getText());
                if (value <= 0) { textField.getColor().set(Color.PINK); return; }
                noiseDigits = value;
                textField.getColor().set(Color.WHITE);
            } catch (NumberFormatException e) {
                textField.getColor().set(Color.PINK);
            }
        });

        noiseStepTF = new TextField(Float.toString(noiseStep), stage.skin);
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

        previewNoiseGrid = new CheckBox("preview noise grid", stage.skin);
        previewNoiseGrid.setChecked(true);

        // TODO: preview image should be more general option on the stage level
        previewNoiseImage = new CheckBox("preview noise image", stage.skin);
        previewNoiseImage.setChecked(true);
        previewNoiseImage.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (previewNoiseImage.isChecked()) {
                    stage.infoBCell.setActor(stage.textureImage);
                } else {
                    stage.infoBCell.clearActor();
                }
            }
        });

        previewTerrain = new CheckBox("preview terrain", stage.skin);
        previewTerrain.setChecked(true);

        trrnSelectBoxes.put(TRRN_FLAT, new SelectBox<>(stage.skin));
        trrnSelectBoxes.put(TRRN_SIDE, new SelectBox<>(stage.skin));
        trrnSelectBoxes.put(TRRN_SIDE_CORN_INN, new SelectBox<>(stage.skin));
        trrnSelectBoxes.put(TRRN_SIDE_CORN_OUT, new SelectBox<>(stage.skin));

        trrnSelectBoxes.get(TRRN_FLAT).setName(TRRN_FLAT.name());
        trrnSelectBoxes.get(TRRN_SIDE).setName(TRRN_SIDE.name());
        trrnSelectBoxes.get(TRRN_SIDE_CORN_INN).setName(TRRN_SIDE_CORN_INN.name());
        trrnSelectBoxes.get(TRRN_SIDE_CORN_OUT).setName(TRRN_SIDE_CORN_OUT.name());
    }

    // see: https://github.com/czyzby/noise4j
    public Texture imageGrid(HGGrid grid) {
        Pixmap map = new Pixmap(grid.getWidth(), grid.getHeight(), Pixmap.Format.RGBA8888);

        final Color color = new Color();
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                final float cell = grid.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }

        if (grid.getWidth() == grid.getHeight() && grid.getWidth() < 512) {
            Pixmap other = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

            // see: https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/Pixmap.html
            // Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch
            // the source image to the specified target rectangle. Use setFilter(Filter) to specify the type
            // of filtering to be used (nearest neighbour or bilinear).
            other.drawPixmap(map,
                    0, 0, grid.getWidth(), grid.getHeight(),
                    0, 0, 512, 512);
            map.dispose();
            map = other;
        }

        Texture texture = new Texture(map);
        map.dispose();

        // see Image (Texture texture) for example on how to convert Texture to Image
        stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));

        return texture;
    }

    public void updateTrrnSelectBoxes() {
        if (stage.folderSelectBox.getSelectedIndex() == 0) {
            for (ObjectMap.Entry<TerrainPartsEnum, SelectBox<FileHandle>> entry: trrnSelectBoxes) {
                SelectBox<FileHandle> sb = entry.value;

                sb.getSelection().setProgrammaticChangeEvents(false);
                sb.clearItems();
                sb.getSelection().setProgrammaticChangeEvents(true);
            }
            return;
        }

        // by this time the models should be loaded
        Array<FileHandle> fhs = eng.folder2models.get(stage.folderSelectBox.getSelected());
        Array<FileHandle> out = new Array<>(FileHandle.class);
        for (FileHandle fh: fhs) { if (modelES.eng.hgModels.get(fh).hasMeshes()) { out.add(fh); } }

        FileHandle array1[] = out.toArray(FileHandle.class);
        FileHandle array2[] = new FileHandle[array1.length + 1];
        System.arraycopy(array1, 0, array2, 1, array1.length);
        array2[0] = Gdx.files.local("Select Model");

        for (ObjectMap.Entry<TerrainPartsEnum, SelectBox<FileHandle>> entry: trrnSelectBoxes) {
            TerrainPartsEnum part = entry.key;
            SelectBox<FileHandle> sb = entry.value;

            sb.getSelection().setProgrammaticChangeEvents(false);
            sb.clearItems();
            sb.setItems(array2);
            sb.getSelection().setProgrammaticChangeEvents(true);
        }
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.infoBCell.setActor(stage.textureImage);
        stage.editCell.setActor(this);
    }
}
