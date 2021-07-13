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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.map.TerrainPartsEnum;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_INN;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_OUT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_FLAT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE;

public class TerrainVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public VisCheckBox previewTerrain;
    public VisTextButton applyTerrainTextButton;
    public VisTextButton clearTerrainTextButton;
    public ArrayMap<TerrainPartsEnum, VisSelectBox<FileHandle>> trrnSelectBoxes =
            new ArrayMap<>(true, 16, TerrainPartsEnum.class, VisSelectBox.class);

    public TerrainVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Terrain");

        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        trrnSelectBoxes.put(TRRN_FLAT, new VisSelectBox<>());
        trrnSelectBoxes.put(TRRN_SIDE, new VisSelectBox<>());
        trrnSelectBoxes.put(TRRN_CORN_INN, new VisSelectBox<>());
        trrnSelectBoxes.put(TRRN_CORN_OUT, new VisSelectBox<>());

        trrnSelectBoxes.get(TRRN_FLAT).setName(TRRN_FLAT.name());
        trrnSelectBoxes.get(TRRN_SIDE).setName(TRRN_SIDE.name());
        trrnSelectBoxes.get(TRRN_CORN_INN).setName(TRRN_CORN_INN.name());
        trrnSelectBoxes.get(TRRN_CORN_OUT).setName(TRRN_CORN_OUT.name());

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

        previewTerrain = new VisCheckBox("preview terrain");
        previewTerrain.setChecked(true);

        VisTable trrnPartTable = new VisTable();
        for (ObjectMap.Entry<TerrainPartsEnum, VisSelectBox<FileHandle>> entry: trrnSelectBoxes) {
            TerrainPartsEnum part = entry.key;
            VisSelectBox<FileHandle> sb = entry.value;

            trrnPartTable.add(new VisLabel(part.description + ":")).right();
            trrnPartTable.add(sb).center().expandX().fillX();
            trrnPartTable.row().pad(0.5f);
        }
        add(trrnPartTable).center().expandX().fillX();
        row();

        VisTable trrnTable = new VisTable();
        trrnTable.add(applyTerrainTextButton).pad(0.5f).center().expandX().fillX();
        trrnTable.add(previewTerrain).center().pad(0.5f).expandX().fillX();
        trrnTable.add(clearTerrainTextButton).pad(0.5f).center().expandX().fillX();
        add(trrnTable).center().expandX().fillX();
        row();
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
}