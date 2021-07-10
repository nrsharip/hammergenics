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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.HGEngine;
import com.hammergenics.map.HGGrid;
import com.hammergenics.map.HGGrid.NoiseStageInfo;
import com.hammergenics.map.TerrainChunk;
import com.hammergenics.map.TerrainPartsEnum;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.util.Arrays;

import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_INN;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_OUT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_FLAT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class MapGenerationTable extends ManagerTable {
    public VisTextButton genNoiseTextButton;
    public VisTextButton genCellTextButton;
    public VisTextButton genDungTextButton;

    public Texture textureNoise;
    public Texture textureCellular;
    public Texture textureDungeon;

    public Array<NoiseStageTable> noiseStageTables = new Array<>(true, 16, NoiseStageTable.class);
    public VisTextButton roundStepNoiseTextButton;

    public VisCheckBox previewNoiseGrid;
    // TODO: preview image should be more general option on the stage level
    public VisCheckBox previewNoiseImage;
    public VisCheckBox previewTerrain;

    public VisTextField noiseYScaleTF;
    public VisTextField noiseStepTF;

    public float noiseYScale = 20f;
    public float noiseStep = 0.05f;

    public VisTextButton applyTerrainTextButton;
    public VisTextButton clearTerrainTextButton;
    public ArrayMap<TerrainPartsEnum, VisSelectBox<FileHandle>> trrnSelectBoxes =
            new ArrayMap<>(true, 16, TerrainPartsEnum.class, VisSelectBox.class);

    public MapGenerationTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        noiseStageTables.addAll(
                new NoiseStageTable(stage, 16, 0.8f),
                new NoiseStageTable(stage, 8, 0.1f),
                new NoiseStageTable(stage, 4, 0.1f),
                new NoiseStageTable(stage, 1, 0.05f)
        );

        trrnSelectBoxes.put(TRRN_FLAT, new VisSelectBox<>());
        trrnSelectBoxes.put(TRRN_SIDE, new VisSelectBox<>());
        trrnSelectBoxes.put(TRRN_CORN_INN, new VisSelectBox<>());
        trrnSelectBoxes.put(TRRN_CORN_OUT, new VisSelectBox<>());

        trrnSelectBoxes.get(TRRN_FLAT).setName(TRRN_FLAT.name());
        trrnSelectBoxes.get(TRRN_SIDE).setName(TRRN_SIDE.name());
        trrnSelectBoxes.get(TRRN_CORN_INN).setName(TRRN_CORN_INN.name());
        trrnSelectBoxes.get(TRRN_CORN_OUT).setName(TRRN_CORN_OUT.name());

        for (NoiseStageTable nst: noiseStageTables) {
            add(nst).center().expandX().fillX();
            row();
        }

        VisTable noiseGridTable = new VisTable();
        noiseGridTable.add(new VisLabel("yScale:", Color.BLACK)).right();
        noiseGridTable.add(noiseYScaleTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(genNoiseTextButton).center().expandX().fillX();

        noiseGridTable.add(new VisLabel("step:", Color.BLACK)).right();
        noiseGridTable.add(noiseStepTF).width(60).maxWidth(60).padRight(5f);
        noiseGridTable.add(roundStepNoiseTextButton).center().expandX().fillX();

        add(noiseGridTable).center().expandX().fillX();
        row();

        VisTable previewCheckBoxes = new VisTable();
        previewCheckBoxes.add(previewNoiseGrid).center().expandX().fillX();
        previewCheckBoxes.add(previewNoiseImage).center().expandX().fillX();

        add(previewCheckBoxes).center().expandX().fillX();
        row();

        VisTable genGridTable = new VisTable();
        genGridTable.add(genCellTextButton).center().expandX().fillX();
        genGridTable.add(genDungTextButton).center().expandX().fillX();

        add(genGridTable).center().expandX().fillX();
        row();

        VisTable trrnPartTable = new VisTable();
        for (ObjectMap.Entry<TerrainPartsEnum, VisSelectBox<FileHandle>> entry: trrnSelectBoxes) {
            TerrainPartsEnum part = entry.key;
            VisSelectBox<FileHandle> sb = entry.value;

            trrnPartTable.add(new VisLabel(part.description + ":", Color.BLACK)).right();
            trrnPartTable.add(sb).center().expandX().fillX();
            trrnPartTable.row();
        }
        add(trrnPartTable).center().expandX().fillX();
        row();

        VisTable trrnTable = new VisTable();
        trrnTable.add(applyTerrainTextButton).center().expandX().fillX();
        trrnTable.add(previewTerrain).center().expandX().fillX();
        trrnTable.add(clearTerrainTextButton).center().expandX().fillX();
        add(trrnTable).center().expandX().fillX();
        row();
    }

    public static class NoiseStageTable extends VisTable {
        public ModelEditStage stage;

        public NoiseStageInfo stageInfo = new NoiseStageInfo();

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
            add(new VisLabel("radius:", Color.BLACK)).right();
            add(radiusTF).width(40).maxWidth(40).padRight(5f);
            add(new VisLabel("modifier:", Color.BLACK)).right();
            add(modifierTF).width(40).maxWidth(40).padRight(5f);
            add(new VisLabel("seed:", Color.BLACK)).right();
            add(noiseGridSeedTF).width(200).maxWidth(200).padRight(5f);
            add(applySeedCB).padRight(5f);
        }
    }

    @Override
    protected void init() {
        genNoiseTextButton = new VisTextButton("gen noise grid");
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

                Array<HGGrid> grids = Arrays.stream(eng.chunks.toArray())
                        .map(TerrainChunk::getGridNoise).collect(Array::new, Array::add, Array::addAll);
                textureNoise = imageGrid(grids);
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
                textureNoise = imageGrid(grids);
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        genCellTextButton = new VisTextButton("gen cellular grid");
        stage.unpressButton(genCellTextButton);
        genCellTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateCellular();
                textureCellular = imageGrid(new Array<>(new HGGrid[]{eng.gridCellular}));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        genDungTextButton = new VisTextButton("gen dungeon grid");
        stage.unpressButton(genDungTextButton);
        genDungTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateDungeon();
                textureDungeon = imageGrid(new Array<>(new HGGrid[]{eng.gridDungeon}));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        applyTerrainTextButton = new VisTextButton("apply terrain parts");
        stage.unpressButton(applyTerrainTextButton);
        applyTerrainTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ArrayMap<TerrainPartsEnum, FileHandle> tp2fh =
                        new ArrayMap<>(true, 16, TerrainPartsEnum.class, FileHandle.class);
                for (ObjectMap.Entry<TerrainPartsEnum, VisSelectBox<FileHandle>> entry: trrnSelectBoxes) {
                    TerrainPartsEnum part = entry.key;
                    VisSelectBox<FileHandle> sb = entry.value;

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

        clearTerrainTextButton = new VisTextButton("clear terrain");
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
                    stage.infoBCell.setActor(stage.textureImage);
                } else {
                    stage.infoBCell.clearActor();
                }
            }
        });

        previewTerrain = new VisCheckBox("preview terrain");
        previewTerrain.setChecked(true);
    }

    // see: https://github.com/czyzby/noise4j
    public Texture imageGrid(Array<HGGrid> grids) {

        Rectangle combined = HGGrid.getCombinedBounds(grids, new Rectangle());
        Gdx.app.debug("image", ""
                + " w: " + (int)combined.getWidth()
                + " h: " + (int)combined.getHeight());
        Pixmap map = new Pixmap((int)combined.getWidth(), (int)combined.getHeight(), Pixmap.Format.RGBA8888);

        final Color color = new Color();
        int x0, y0;
        for (HGGrid grid: grids) {
            x0 = grid.getX0();
            y0 = grid.getZ0();
            Gdx.app.debug("image", "x0: " + x0 + " y0: " + y0);
            for (int x = 0; x < grid.getWidth(); x++) {
                for (int y = 0; y < grid.getHeight(); y++) {
                    final float cell = grid.get(x, y);
                    color.set(cell, cell, cell, 1f);
                    map.drawPixel(x + x0 - (int)combined.x, y + y0 - (int)combined.y, Color.rgba8888(color));
                }
            }
        }

        if ((int)combined.getWidth() == (int)combined.getHeight() && (int)combined.getWidth() < 512) {
            Pixmap other = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

            // see: https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/Pixmap.html
            // Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch
            // the source image to the specified target rectangle. Use setFilter(Filter) to specify the type
            // of filtering to be used (nearest neighbour or bilinear).
            other.drawPixmap(map,
                    0, 0, (int)combined.getWidth(), (int)combined.getHeight(),
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
            for (ObjectMap.Entry<TerrainPartsEnum, VisSelectBox<FileHandle>> entry: trrnSelectBoxes) {
                VisSelectBox<FileHandle> sb = entry.value;

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

        for (ObjectMap.Entry<TerrainPartsEnum, VisSelectBox<FileHandle>> entry: trrnSelectBoxes) {
            TerrainPartsEnum part = entry.key;
            VisSelectBox<FileHandle> sb = entry.value;

            sb.getSelection().setProgrammaticChangeEvents(false);
            sb.clearItems();
            sb.setItems(array2);
            sb.getSelection().setProgrammaticChangeEvents(true);
        }
    }

    @Override
    public void resetActors() {
        super.resetActors();

        stage.infoBCell.setActor(stage.textureImage);
    }

    public void applyLocale() {

    }
}