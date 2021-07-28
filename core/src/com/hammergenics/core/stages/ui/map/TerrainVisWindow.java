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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisWindow;
import com.hammergenics.map.TerrainPartsEnum;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_INN;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_OUT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_FLAT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TerrainVisWindow extends ContextAwareVisWindow {
    public VisCheckBox previewTerrain;
    public VisTextButton applyTerrainTextButton;
    public VisTextButton clearTerrainTextButton;
    public ArrayMap<TerrainPartsEnum, FileHandle> trrnPart2fileHandle =
            new ArrayMap<>(true, 16, TerrainPartsEnum.class, FileHandle.class);
    public ArrayMap<TerrainPartsEnum, VisLabel> trrnPart2label =
            new ArrayMap<>(true, 16, TerrainPartsEnum.class, VisLabel.class);
    public TerrainVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Terrain", modelES, stage);

        // NOTE: It is safe to feed null's along with the terrain parts to the engine. The actual check of null
        //       happens on the path HGEngine.applyTerrainParts -> TerrainPartsEnum.processFileHandle
        trrnPart2fileHandle.put(TRRN_FLAT, null);
        trrnPart2fileHandle.put(TRRN_SIDE, null);
        trrnPart2fileHandle.put(TRRN_CORN_INN, null);
        trrnPart2fileHandle.put(TRRN_CORN_OUT, null);

        trrnPart2label.put(TRRN_FLAT, new VisLabel("No model selected"));
        trrnPart2label.put(TRRN_SIDE, new VisLabel("No model selected"));
        trrnPart2label.put(TRRN_CORN_INN, new VisLabel("No model selected"));
        trrnPart2label.put(TRRN_CORN_OUT, new VisLabel("No model selected"));

        applyTerrainTextButton = new VisTextButton("apply terrain parts");
        stage.unpressButton(applyTerrainTextButton);
        applyTerrainTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //Gdx.app.debug("terrain","" + " map: " + trrnPart2fileHandle.toString());
                // NOTE: It is safe to feed null's along with the terrain parts to the engine. The actual check of null
                //       happens on the path HGEngine.applyTerrainParts -> TerrainPartsEnum.processFileHandle
                eng.applyTerrainParts(trrnPart2fileHandle);

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
        for (ObjectMap.Entry<TerrainPartsEnum, FileHandle> entry: trrnPart2fileHandle) {
            TerrainPartsEnum part = entry.key;

            VisTextButton chooseModelTB = new VisTextButton("select");
            chooseModelTB.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    modelES.stage.modelChooser.updateAssetsTree();
                    modelES.stage.modelChooser.setListener(new ConfirmDialogListener<FileHandle>() {
                        @Override
                        public void result(FileHandle result) {
                            FileHandle fileHandle = new FileHandle(result.file().getAbsolutePath());
                            //Gdx.app.debug("terrain","" + " tp: " + part.description + " filehandle: " + fileHandle);
                            if (fileHandle.exists()) {
                                //Gdx.app.debug("terrain","exists");
                                trrnPart2fileHandle.put(part, fileHandle);
                                trrnPart2label.get(part).setText(fileHandle.name());
                            }
                        }
                    });
                    modelES.stage.addActor(modelES.stage.modelChooser.fadeIn());
                    return super.touchDown(event, x, y, pointer, button);
                }
            });

            trrnPartTable.add(new VisLabel(part.description + ":")).padRight(5f).right();
            trrnPartTable.add(chooseModelTB).padRight(5f).fillX();
            trrnPartTable.add(trrnPart2label.get(part)).padRight(5f).expandX().fillX().left();
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

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}