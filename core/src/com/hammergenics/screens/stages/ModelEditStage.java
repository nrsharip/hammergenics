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

package com.hammergenics.screens.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hammergenics.HGGame;
import com.hammergenics.config.Config;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.stages.ui.AggregatedAttributesManagerTable;
import com.hammergenics.screens.stages.ui.AnimationsManagerTable;
import com.hammergenics.screens.stages.ui.MapGenerationTable;
import com.hammergenics.screens.stages.ui.attributes.AttributesManagerTable;
import com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable;
import com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType;
import com.hammergenics.utils.HGUtils;

import static com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_CHANGED;
import static com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_DISABLED;
import static com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_ENABLED;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditStage extends Stage {
    public static final Color COLOR_DISABLED = Color.GRAY;
    public static final Color COLOR_PRESSED = Color.RED;
    public static final Color COLOR_UNPRESSED = Color.LIGHT_GRAY;

    public final HGGame game;
    public final ModelEditScreen modelES;

    // 2D Stage Styling:
    public Skin skin;
    public BitmapFont labelBitmapFont;
    public Label.LabelStyle labelStyle;

    // 2D Stage Layout:
    public Table rootTable;
    public Cell<?> infoTCell = null;
    public Cell<?> infoBCell = null;
    public Cell<?> editCell = null;

    public AttributesManagerTable envAttrTable;
    public AggregatedAttributesManagerTable aggrAttrTable;
    public AnimationsManagerTable animationsManagerTable;
    public MapGenerationTable mapGenerationTable;

    // 2D Stage Widgets:
    public Label miLabel;  // Model Instance Info
    public Label envLabel; // Environment Info
    public Label fpsLabel; // FPS Info
    public Image textureImage;
    public CheckBox debugStageCheckBox;
    public CheckBox gridXZCheckBox;
    public CheckBox gridYCheckBox;
    public CheckBox dynamicsCheckBox;
    public CheckBox rbCheckBox;
    public CheckBox groundCheckBox;
    public CheckBox lightsCheckBox;
    public CheckBox origScaleCheckBox;
    public CheckBox bbCheckBox;
    public CheckBox nodesCheckBox;
    public CheckBox bonesCheckBox;
    public CheckBox invertCheckBox;
    public CheckBox meshPartsCheckBox;
    public SelectBox<FileHandle> folderSelectBox;
    public SelectBox<FileHandle> modelSelectBox;
    public SelectBox<String> nodeSelectBox;
    public TextButton attrTextButton = null;
    public TextButton animTextButton = null;
    public TextButton mapTextButton = null;
    public TextButton clearModelsTextButton = null;
    public TextButton deleteCurrModelTextButton = null;
    public TextButton saveCurrModelTextButton = null;

    public BaseAttributeTable.EventListener eventListener;

    public ModelEditStage(Viewport viewport, HGGame game, ModelEditScreen modelES) {
        super(viewport);
        this.game = game;
        this.modelES = modelES;

        setup2DStageStyling();
        setup2DStageWidgets();
        setup2DStageLayout();

        aggrAttrTable = new AggregatedAttributesManagerTable(modelES, this);
        animationsManagerTable = new AnimationsManagerTable(modelES, this);
        mapGenerationTable = new MapGenerationTable(modelES, this);
    }

    /**
     *
     */
    public void setup2DStageWidgets() {
        // WIDGETS for 2D Stage (https://github.com/libgdx/libgdx/wiki/Scene2d.ui#widgets)
        // IMAGES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#image
        // https://libgdx.info/basic_image/
        textureImage = new Image();
        textureImage.setPosition(0f, 0f);
        textureImage.setScaling(Scaling.fit);
        textureImage.setAlign(Align.bottomLeft);

        // LABELS:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label
        fpsLabel = new Label("", labelStyle);
        miLabel = new Label("", labelStyle);
        envLabel = new Label("", labelStyle);

        // SELECT BOXES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox

        // Select Box: Models
        folderSelectBox = new SelectBox<>(skin);
        resetFolderSelectBoxItems(game.engine.folder2models);
        folderSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (folderSelectBox.getSelectedIndex() == 0) { return; } // syncup: folder select

                modelES.eng.queueAssets(folderSelectBox.getSelected());
            }
        });

        modelSelectBox = new SelectBox<>(skin);
        modelSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {  // syncup: model select
                if (modelSelectBox.getSelectedIndex() == 0) { return; } // 'Select Model' item
                if (modelSelectBox.getSelectedIndex() == 1) {           // 'ALL' item
                    addModelInstances(game.engine.folder2models.get(folderSelectBox.getSelected()));
                    afterCurrentModelInstanceChanged();
                    Gdx.app.debug(modelSelectBox.getClass().getSimpleName(), "model selected: ALL");
                } else {
                    addModelInstance(modelSelectBox.getSelected());
                    afterCurrentModelInstanceChanged();
                    Gdx.app.debug(modelSelectBox.getClass().getSimpleName(), "model selected: " + modelSelectBox.getSelected());
                }
            }
        });

        // Select Box: Nodes
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        nodeSelectBox = new SelectBox<>(skin);
        nodeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI == null) { return; }
                if (nodeSelectBox.getSelectedIndex() == 0) { // 'all' selected
                    addModelInstance(modelSelectBox.getSelected());
                    afterCurrentModelInstanceChanged();
                } else {
                    if (!addModelInstance(modelES.eng.currMI.nodeid2model.get(nodeSelectBox.getSelected()))) { // -1 since there's 'all' item
                        nodeSelectBox.getColor().set(Color.PINK);
                    } else {
                        afterCurrentModelInstanceChanged();
                    }
                }
            }
        });

        // CHECK BOXES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#checkbox
        debugStageCheckBox = new CheckBox("debug stage", skin);
        debugStageCheckBox.setChecked(false);
        debugStageCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                // https://github.com/libgdx/libgdx/wiki/Table#debugging
                // turn on all debug lines (table, cell, and widget)
                //rootTable.setDebug(debugLayoutCheckBox.isChecked());
                setDebugAll(debugStageCheckBox.isChecked());
            }
        });

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#checkbox
        gridXZCheckBox = new CheckBox("XZ", skin);
        gridXZCheckBox.setChecked(true);

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#checkbox
        gridYCheckBox = new CheckBox("Y", skin);
        gridYCheckBox.setChecked(true);

        dynamicsCheckBox = new CheckBox("dynamics", skin);
        dynamicsCheckBox.setChecked(false);
        dynamicsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (!dynamicsCheckBox.isChecked()) modelES.eng.arrangeInSpiral(origScaleCheckBox.isChecked());
            }
        });

        rbCheckBox = new CheckBox("rigid body", skin);
        rbCheckBox.setChecked(false);

        groundCheckBox = new CheckBox("ground", skin);
        groundCheckBox.setChecked(false);

        lightsCheckBox = new CheckBox("lights", skin);
        lightsCheckBox.setChecked(true);

        origScaleCheckBox = new CheckBox("orig scale", skin);
        origScaleCheckBox.setChecked(false);
        origScaleCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) { modelES.eng.arrangeInSpiral(origScaleCheckBox.isChecked()); }
        });

        bbCheckBox = new CheckBox("BB", skin);
        bbCheckBox.setChecked(false);
        // TODO: fix BB checkbox
        //bbCheckBox.addListener(new ChangeListener() {
        //    @Override
        //    public void changed (ChangeEvent event, Actor actor) { modelES.eng.resetBBModelInstances(); }
        //});

        nodesCheckBox = new CheckBox("nodes", skin);
        nodesCheckBox.setChecked(false);

        bonesCheckBox = new CheckBox("bones (", skin);
        bonesCheckBox.setChecked(false);

        invertCheckBox = new CheckBox("invert)", skin);
        invertCheckBox.setChecked(false);

        meshPartsCheckBox = new CheckBox("mesh parts", skin);
        meshPartsCheckBox.setChecked(false);

        // TEXT BUTTONS:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textbutton
        attrTextButton = new TextButton("ATTR", skin);
        unpressButton(attrTextButton);
        attrTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editCell.clearActor();
                if (!isPressed(attrTextButton)) {
                    unpressAllButtons();
                    pressButton(attrTextButton);
                } else {
                    unpressButton(attrTextButton);
                }
                resetTables();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        animTextButton = new TextButton("ANIM", skin);
        unpressButton(animTextButton);
        animTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editCell.clearActor();
                if (!isPressed(animTextButton)) {
                    unpressAllButtons();
                    pressButton(animTextButton);
                } else {
                    unpressButton(animTextButton);
                }
                resetTables();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        mapTextButton = new TextButton("MAP", skin);
        unpressButton(mapTextButton);
        mapTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editCell.clearActor();
                if (!isPressed(mapTextButton)) {
                    unpressAllButtons();
                    pressButton(mapTextButton);
                } else {
                    unpressButton(mapTextButton);
                }
                resetTables();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        clearModelsTextButton = new TextButton("clear all", skin);
        unpressButton(clearModelsTextButton);
        clearModelsTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.eng.clearModelInstances();
                reset();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        deleteCurrModelTextButton = new TextButton("delete", skin);
        unpressButton(deleteCurrModelTextButton);
        deleteCurrModelTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.eng.removeDbgModelInstance(modelES.eng.currMI);
                if (modelES.eng.physMIs.size > 0) { modelES.eng.currMI = modelES.eng.physMIs.get(0); }
                else { modelES.eng.currMI = null; }
                reset();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        saveCurrModelTextButton = new TextButton("save", skin);
        unpressButton(saveCurrModelTextButton);
        saveCurrModelTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.eng.saveHgModelInstance(modelES.eng.currMI);
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        // temporarily placing it here:
        eventListener = new BaseAttributeTable.EventListener() {
            @Override
            public void onAttributeEnabled(Attributes container, long type, String alias) {
                handleAttributeUpdate(ATTR_ENABLED, container, type, alias);
            }

            @Override
            public void onAttributeDisabled(Attributes container, long type, String alias) {
                handleAttributeUpdate(ATTR_DISABLED, container, type, alias);
            }

            @Override
            public void onAttributeChange(Attributes container, long type, String alias) {
                handleAttributeUpdate(ATTR_CHANGED, container, type, alias);
            }
        };
    }

    public void updateModelSelectBox() {
        if (modelSelectBox == null || folderSelectBox.getSelectedIndex() == 0) { return; }

        // by this time the models should be loaded
        Array<FileHandle> fhs = game.engine.folder2models.get(folderSelectBox.getSelected());
        Array<FileHandle> out = new Array<>(FileHandle.class);
        for (FileHandle fh: fhs) { if (modelES.eng.hgModels.get(fh).hasMeshes()) { out.add(fh); } }

        FileHandle array1[] = out.toArray(FileHandle.class);
        FileHandle array2[] = new FileHandle[array1.length + 2];
        System.arraycopy(array1, 0, array2, 2, array1.length);
        array2[0] = Gdx.files.local("Select Model"); // syncup: model select
        array2[1] = Gdx.files.local("ALL");

        modelSelectBox.getSelection().setProgrammaticChangeEvents(false); // even though the listeners are defined later
        modelSelectBox.clearItems();
        modelSelectBox.setItems(array2);
        modelSelectBox.getSelection().setProgrammaticChangeEvents(true);
    }

    public void addModelInstances(Array<FileHandle> modelFHs) {
        if (modelFHs == null) { return; }
        modelFHs.forEach(fileHandle -> addModelInstance(fileHandle));
        if (modelES.eng.physMIs.size > 0) { modelES.eng.currMI = modelES.eng.physMIs.get(0); }
    }

    public boolean addModelInstance(FileHandle assetFL) {
        boolean created = modelES.eng.addModelInstance(assetFL);
        return created;
    }

    public boolean addModelInstance(Model model) {
        boolean created = modelES.eng.addModelInstance(model);
        return created;
    }

    public boolean addModelInstance(HGModel hgModel) {
        boolean created = modelES.eng.addModelInstance(hgModel);
        if (created) { aggrAttrTable.setDbgModelInstance(modelES.eng.currMI); }
        return created;
    }

    /**
     * Keeping the attribute change event in one place.
     * @param container
     * @param type
     * @param alias
     */
    private void handleAttributeUpdate(EventType eType, Attributes container, long type, String alias) {
        if (modelES.eng.currMI != null) {
            miLabel.setText(HGUtils.getModelInstanceInfo(modelES.eng.currMI));
        } else {
            miLabel.setText("");
        }

        envLabel.setText("Environment:\n" + HGUtils.extractAttributes(modelES.environment,"", ""));

        if ((type & (DirectionalLightsAttribute.Type | PointLightsAttribute.Type)) != 0) {
            Vector3 center = Vector3.Zero.cpy();
            if (modelES.eng.currMI != null) { modelES.eng.currMI.getBB().getCenter(center); }
            modelES.eng.resetLightsModelInstances(center, modelES.environment);
        }
        //Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "onAttributeDisabled: 0x" + Long.toHexString(type) + " alias: " + alias);
    }

    public void afterCurrentModelInstanceChanged() {
        modelES.reset();
        reset();
    }

    public void resetFolderSelectBoxItems(ArrayMap<FileHandle, Array<FileHandle>> f2m) {
        folderSelectBox.getSelection().setProgrammaticChangeEvents(false); // even though the listeners are defined later
        folderSelectBox.clearItems();

        if (f2m.size > 0) {
            // making sure the map doesn't have folders with the same set of models, e.g.
            // ./          -> (model1, model2)
            // ./tmp1      -> (model1, model2)
            // ./tmp1/tmp2 -> (model1, model2)
            // with the only models:
            // ./tmp1/tmp2/model1
            // ./tmp1/tmp2/model2
            // Assuming the map is ordered and arranged in such a way that
            // the children folders located precisely after their parent folder
            ArrayMap<FileHandle, Array<FileHandle>> copy = new ArrayMap<>(f2m);
            Array<FileHandle>[] values = copy.values;
            Array<FileHandle> value = values[copy.size - 1];
            for (int i = copy.size - 2; i >= 0; i--) {
                if (values[i].equals(value)) { copy.removeIndex(i); }
                else { value = values[i]; }
            }

            FileHandle array1[] = copy.keys().toArray().toArray(FileHandle.class);
            FileHandle array2[] = new FileHandle[array1.length + 1];
            System.arraycopy(array1, 0, array2, 1, array1.length);
            array2[0] = Gdx.files.local("Choose Folder..."); // syncup: folder select
            folderSelectBox.setItems(array2);
        } else {
            folderSelectBox.setItems(Gdx.files.local("No Models Available"));
        }

        folderSelectBox.getSelection().setProgrammaticChangeEvents(true);
    }

    /**
     *
     */
    public void setup2DStageStyling() {
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#loading-a-ttf-using-the-assethandler
        labelBitmapFont = modelES.eng.assetManager.get(Config.ASSET_FILE_NAME_FONT, BitmapFont.class);
        labelBitmapFont.getData().setScale(0.5f);
        labelStyle = new Label.LabelStyle(labelBitmapFont, Color.BLACK);
        // SKIN for 2D Stage Widgets
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#skin
        // Skin files from the libGDX tests can be used as a starting point.
        // https://github.com/libgdx/libgdx/tree/master/tests/gdx-tests-android/assets/data
        // You will need: uiskin.png, uiskin.atlas, uiskin.json, and default.fnt.
        // This enables you to quickly get started using scene2d.ui and replace the skin assets later.
        // https://github.com/libgdx/libgdx/wiki/Texture-packer#textureatlas
        //TextureAtlas atlas;
        //atlas = new TextureAtlas(Gdx.files.internal("skins/libgdx/uiskin.atlas"));
        // https://github.com/libgdx/libgdx/wiki/Skin#resources
        // https://github.com/libgdx/libgdx/wiki/Skin#skin-json
        skin = new Skin(Gdx.files.internal(Config.ASSET_FILE_NAME_SKIN));
        //skin.addRegions(atlas);
    }

    /**
     *
     */
    public void setup2DStageLayout() {
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#layout-widgets
        // Table, Container, Stack, ScrollPane, SplitPane, Tree, VerticalGroup, HorizontalGroup

        // ROOT TABLE:
        // https://github.com/libgdx/libgdx/wiki/Table#quickstart
        rootTable = new Table();
        // https://github.com/libgdx/libgdx/wiki/Table#root-table
        rootTable.setFillParent(true);
        // https://github.com/libgdx/libgdx/wiki/Table#debugging
        rootTable.setDebug(false);

        // https://github.com/libgdx/libgdx/wiki/Table#adding-cells
        Table upperPanel = new Table();
        upperPanel.add(new Label("Folder: ", skin)).right();
        upperPanel.add(folderSelectBox).padLeft(5f).left();
        upperPanel.add(new Label("Model: ", skin)).right();
        upperPanel.add(modelSelectBox).padLeft(5f).left();
        upperPanel.add(new Label("Node: ", skin)).padLeft(5f).right();
        upperPanel.add(nodeSelectBox).padLeft(5f).left();
        upperPanel.add().expandX();

        rootTable.add();
        rootTable.add(upperPanel).colspan(2).expandX().left();

        rootTable.row();

        Table leftPanel = new Table();
        leftPanel.add(attrTextButton).fillX();
        leftPanel.row();
        leftPanel.add(animTextButton).fillX();
        leftPanel.row();
        leftPanel.add(mapTextButton).fillX();
        leftPanel.row();

        rootTable.add(leftPanel).padTop(10f).top().left();

        Table infoTable = new Table();
        infoTCell = infoTable.add().expand().top().left();
        infoTable.row();
        infoBCell = infoTable.add().expand().bottom().left().maxHeight(Gdx.graphics.getHeight());

        rootTable.add(infoTable).expand().fillY().left().pad(10f);
        editCell = rootTable.add().expand().right().top().pad(10f);

        rootTable.row();

        Table lowerPanel = new Table();
        lowerPanel.add(fpsLabel).minWidth(70f).pad(3f);
        lowerPanel.add(debugStageCheckBox).pad(3f);
        lowerPanel.add(gridXZCheckBox).pad(3f);
        lowerPanel.add(gridYCheckBox).pad(3f);
        lowerPanel.add(dynamicsCheckBox).pad(3f);
        lowerPanel.add(rbCheckBox).pad(3f);
        lowerPanel.add(groundCheckBox).pad(3f);
        lowerPanel.add(lightsCheckBox).pad(3f);
        lowerPanel.add(origScaleCheckBox).pad(3f);
        // TODO: fix BB checkbox
        //lowerPanel.add(bbCheckBox).pad(3f);
        lowerPanel.add(nodesCheckBox).pad(3f);
        lowerPanel.add(bonesCheckBox);
        lowerPanel.add(invertCheckBox).pad(3f);
        lowerPanel.add(meshPartsCheckBox).pad(3f);
        lowerPanel.add(saveCurrModelTextButton).pad(3f);
        lowerPanel.add(deleteCurrModelTextButton).pad(3f);
        lowerPanel.add(clearModelsTextButton).pad(3f);
        lowerPanel.add().expandX();

        rootTable.add(lowerPanel).colspan(3).expandX().left();

        addActor(rootTable);
    }

    public void unpressAllButtons() {
        unpressButton(attrTextButton);
        unpressButton(animTextButton);
        unpressButton(mapTextButton);
    }
    public boolean isAnyButtonPressed() {
        return isPressed(attrTextButton) || isPressed(animTextButton) || isPressed(mapTextButton);
    }
    public void unpressButton(TextButton btn) { btn.getColor().set(COLOR_UNPRESSED); }
    public void pressButton(TextButton btn) { btn.getColor().set(COLOR_PRESSED); }
    public void disableButton(TextButton btn) {
        btn.getColor().set(COLOR_DISABLED); btn.getLabel().getColor().set(COLOR_DISABLED);
    }
    public void enableButton(TextButton btn) {
        btn.getColor().set(COLOR_UNPRESSED); btn.getLabel().getColor().set(Color.WHITE);
    }
    public boolean isPressed(TextButton btn) { return btn.getColor().equals(COLOR_PRESSED); }
    public boolean isDisabled(TextButton btn) { return btn.getColor().equals(COLOR_DISABLED); }

    public void reset() {
        textureImage.setDrawable(null);

        // Select Box: Nodes
        // making sure no events fired during the nodeSelectBox reset
        nodeSelectBox.getSelection().setProgrammaticChangeEvents(false);
        nodeSelectBox.clearItems();
        if (modelES.eng.currMI != null) {
            String array1[] = modelES.eng.currMI.nodeid2model.keys().toArray().toArray();
            String array2[] = new String[array1.length + 1];
            System.arraycopy(array1, 0, array2, 1, array1.length);
            array2[0] = "All";
            nodeSelectBox.setItems(array2);
        }
        nodeSelectBox.getSelection().setProgrammaticChangeEvents(true);
        nodeSelectBox.getColor().set(Color.WHITE);

        resetTables();
    }

    public void resetTables() {
        if (modelES == null) { return; }

        if (isPressed(attrTextButton)) {
            aggrAttrTable.setDbgModelInstance(modelES.eng.currMI);
            aggrAttrTable.resetActors();
        }

        if (isPressed(animTextButton)) {
            animationsManagerTable.setDbgModelInstance(modelES.eng.currMI);
            animationsManagerTable.resetActors();
        }

        if (isPressed(mapTextButton)) {
            //mapGenerationTable.setDbgModelInstance(modelES.eng.currMI);
            mapGenerationTable.resetActors();
        }

        if (!isAnyButtonPressed()) {
            infoTCell.clearActor();
            infoBCell.clearActor();
            editCell.clearActor();
            textureImage.setDrawable(null);
        }
    }
}
