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

package com.hammergenics.core.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hammergenics.HGGame;
import com.hammergenics.HGGame.I18NBundlesEnum;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.HGModel;
import com.hammergenics.core.stages.ui.AIManagerTable;
import com.hammergenics.core.stages.ui.AggregatedAttributesManagerTable;
import com.hammergenics.core.stages.ui.AnimationsManagerTable;
import com.hammergenics.core.stages.ui.MapGenerationTable;
import com.hammergenics.core.stages.ui.PhysicsManagerTable;
import com.hammergenics.core.stages.ui.attributes.AttributesManagerTable;
import com.hammergenics.core.stages.ui.attributes.BaseAttributeTable;
import com.hammergenics.core.stages.ui.attributes.BaseAttributeTable.EventType;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.i18n.BundleText;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.color.ColorPicker;

import static com.hammergenics.core.stages.ModelEditStage.MenuItemsTextEnum.*;
import static com.hammergenics.core.stages.ModelEditStage.TextButtonsTextEnum.*;
import static com.hammergenics.core.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_CHANGED;
import static com.hammergenics.core.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_DISABLED;
import static com.hammergenics.core.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_ENABLED;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditStage extends Stage {
    // see VisUI.SkinScale:
    // X1("com/kotcrab/vis/ui/skin/x1/uiskin.json", "default")
    // X2("com/kotcrab/vis/ui/skin/x2/uiskin.json", "x2");
    public final TextButtonStyle tbStyleDefault = VisUI.getSkin().get("default", TextButtonStyle.class);
    public final TextButtonStyle tbStyleBlue = VisUI.getSkin().get("blue", TextButtonStyle.class);
    public final LabelStyle lStyleFontWhite = new LabelStyle(VisUI.getSkin().get("default", LabelStyle.class));

    public final HGGame game;
    public final ModelEditScreen modelES;

    // 2D Stage Layout:
    public VisTable rootTable;
    public MenuBar menuBar;
    public Cell<?> infoTCell = null;
    public Cell<?> infoBCell = null;
    public Cell<?> editCell = null;

    public AttributesManagerTable envAttrTable;
    public AggregatedAttributesManagerTable aggrAttrTable;
    public AnimationsManagerTable animationsManagerTable;
    public MapGenerationTable mapGenerationTable;
    public AIManagerTable aiManagerTable;
    public PhysicsManagerTable physManagerTable;

    // 2D Stage Widgets:
    public ColorPicker colorPicker;

    public VisLabel miLabel;  // Model Instance Info
    public VisLabel envLabel; // Environment Info
    public VisLabel fpsLabel; // FPS Info
    public Image textureImage;
    public VisCheckBox debugStageCheckBox;
    public VisCheckBox gridOriginCheckBox;
    public VisCheckBox gridYCheckBox;
    public VisCheckBox lightsCheckBox;
    public VisCheckBox showSelectionCheckBox;
    public VisCheckBox origScaleCheckBox;
    public VisCheckBox bbCheckBox;
    public VisCheckBox nodesCheckBox;
    public VisCheckBox bonesCheckBox;
    public VisCheckBox invertBonesCheckBox;
    public VisCheckBox meshPartsCheckBox;
    public VisCheckBox verticesCheckBox;
    public VisCheckBox closestCheckBox;
    public VisSelectBox<FileHandle> folderSelectBox;
    public VisSelectBox<FileHandle> modelSelectBox;
    public VisSelectBox<String> nodeSelectBox;
    public VisTextButton attrTextButton = null;
    public VisTextButton animTextButton = null;
    public VisTextButton mapTextButton = null;
    public VisTextButton aiTextButton = null;
    public VisTextButton physTextButton = null;
    public VisTextButton clearModelsTextButton = null;
    public VisTextButton deleteCurrModelTextButton = null;
    public VisTextButton saveCurrModelTextButton = null;

    public BaseAttributeTable.EventListener eventListener;

    public ModelEditStage(Viewport viewport, HGGame game, ModelEditScreen modelES) {
        super(viewport);
        this.game = game;
        this.modelES = modelES;

        lStyleFontWhite.fontColor = Color.WHITE.cpy();

        initColorPicker();
        initMenuBar();

        setup2DStageWidgets();
        setup2DStageLayout();

        if (I18NBundlesEnum.applied != null) {
            applyLocale(I18NBundlesEnum.applied);
        } else {
            Gdx.app.error(getClass().getSimpleName(), "ERROR: locales are not configured");
            Gdx.app.exit();
        }

        aggrAttrTable = new AggregatedAttributesManagerTable(modelES, this);
        animationsManagerTable = new AnimationsManagerTable(modelES, this);
        mapGenerationTable = new MapGenerationTable(modelES, this);
        aiManagerTable = new AIManagerTable(modelES, this);
        physManagerTable = new PhysicsManagerTable(modelES, this);
    }

    @Override
    public void dispose() {
        // see: https://github.com/kotcrab/vis-ui/wiki/Color-Picker
        // Color picker is a heavy widget and should be reused whenever possible, picker unlike other
        // VisUI widgets must be disposed (by calling picker.dispose()) when no longer needed.
        //picker creation
        colorPicker.dispose();
        super.dispose();
    }

    public void initColorPicker() {
        if (colorPicker != null) { colorPicker.dispose(); }

        // see: https://github.com/kotcrab/vis-ui/wiki/Color-Picker
        // Color picker is a heavy widget and should be reused whenever possible, picker unlike other
        // VisUI widgets must be disposed (by calling picker.dispose()) when no longer needed.
        //picker creation
        colorPicker = new ColorPicker();

        // making label font color white
        if (colorPicker.getPicker().getCells().size == 2) {
            // should only be two actors - VisTable mainTable, extendedTable
            Actor actor;
            VisTable table;
            VisLabel label;
            // 1.
            // see ColorPicker.picker -> ExtendedColorPicker.createUI -> BasicColorPicker.createUI -> BasicColorPicker.createHexTable()
            // see ColorPicker.picker -> ExtendedColorPicker.createUI -> BasicColorPicker.createUI -> rebuildMainTable()
            // see ColorPicker.picker -> ExtendedColorPicker.createUI -> BasicColorPicker.createUI -> add(mainTable)
            actor = colorPicker.getPicker().getCells().first().getActor();
            if (actor instanceof VisTable) {
                table = (VisTable) actor;
                //Gdx.app.debug("picker", "" + " r: " + table.getRows() + " c: " + table.getColumns() + " table:\n" + table);
                for (Cell<?> cell1: table.getCells()) {
                    if (cell1.getActor() instanceof VisTable) {
                        for (Cell<?> cell2: ((VisTable)cell1.getActor()).getCells()) {
                            if (cell2.getActor() instanceof VisLabel) {
                                ((VisLabel)cell2.getActor()).setStyle(lStyleFontWhite);
                            }
                        }
                    }
                }
            }

            // 2.
            // see ColorPicker.picker -> ExtendedColorPicker.createUI -> add(extendedTable)
            // Cell Indices: hBar:0, sBar: 1, vBar: 2, rBar: 4, gBar: 5, bBar: 6, aBar: 8
            // see ColorChannelWidget::new - add(new VisLabel(label)) - Label index is 0
            actor = colorPicker.getPicker().getCells().peek().getActor();
            if (actor instanceof VisTable) {
                table = (VisTable) actor;
                //Gdx.app.debug("picker", "" + " r: " + table.getRows() + " c: " + table.getColumns() + " table:\n" + table);
                for (Cell<?> cell1: table.getCells()) {
                    if (cell1.getActor() instanceof VisTable) {
                        for (Cell<?> cell2: ((VisTable)cell1.getActor()).getCells()) {
                            if (cell2.getActor() instanceof VisLabel) {
                                ((VisLabel)cell2.getActor()).setStyle(lStyleFontWhite);
                            }
                        }
                    }
                }
            }
        }
    }

    // see: https://github.com/kotcrab/vis-ui/blob/master/ui/src/test/java/com/kotcrab/vis/ui/test/manual/TestLauncher.java
    public void initMenuBar() {
        menuBar = new MenuBar();
        menuBar.setMenuListener(new MenuBar.MenuBarListener() {
            @Override public void menuOpened (Menu menu) { }
            @Override public void menuClosed (Menu menu) { }
        });

        Menu fileMenu = new Menu("File"); MENU_BAR_FILE.seize(fileMenu.openButton);
        Menu editMenu = new Menu("Edit"); MENU_BAR_EDIT.seize(editMenu.openButton);
        //Menu windowMenu = new Menu("Window"); MENU_BAR_WINDOW.seize(windowMenu.openButton);
        Menu helpMenu = new Menu("Help"); MENU_BAR_HELP.seize(helpMenu.openButton);

        MenuItem newMenuItem = new MenuItem("New").setShortcut("Ctrl + N"); MENU_ITEM_NEW.seize(newMenuItem);
        MenuItem openMenuItem = new MenuItem("Open").setShortcut("Ctrl + O"); MENU_ITEM_OPEN.seize(openMenuItem);
        MenuItem addToProjectMenuItem = new MenuItem("Add to Project"); MENU_ITEM_ADD_TO_PROJECT.seize(addToProjectMenuItem);
        PopupMenu addToProjectPopupMenu = new PopupMenu();
        MenuItem addFolderMenuItem = new MenuItem("Folder", new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {}
        });
        MENU_ITEM_ADD_FOLDER.seize(addFolderMenuItem);
        MenuItem addFileMenuItem = new MenuItem("File", new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {}
        });
        MENU_ITEM_ADD_FILE.seize(addFileMenuItem);
        addToProjectPopupMenu.addItem(addFolderMenuItem);
        addToProjectPopupMenu.addItem(addFileMenuItem);
        addToProjectMenuItem.setSubMenu(addToProjectPopupMenu);

        MenuItem saveMenuItem = new MenuItem("Save").setShortcut("Ctrl + S"); MENU_ITEM_SAVE.seize(saveMenuItem);
        saveMenuItem.setDisabled(true);
        MenuItem settingsMenuItem = new MenuItem("Settings...").setShortcut("Ctrl + Alt + S"); MENU_ITEM_SETTINGS.seize(settingsMenuItem);
        MenuItem exitMenuItem = new MenuItem("Exit").setShortcut("Alt + F4"); MENU_ITEM_EXIT.seize(exitMenuItem);

        fileMenu.addItem(newMenuItem);
        fileMenu.addItem(openMenuItem);
        fileMenu.addItem(addToProjectMenuItem);
        fileMenu.addSeparator();
        fileMenu.addItem(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.addItem(settingsMenuItem);
        fileMenu.addSeparator();
        fileMenu.addItem(exitMenuItem);

        MenuItem undoMenuItem = new MenuItem("Undo").setShortcut("Ctrl + Z"); MENU_ITEM_UNDO.seize(undoMenuItem);
        MenuItem redoMenuItem = new MenuItem("Redo").setShortcut("Ctrl + Shift + Z"); MENU_ITEM_REDO.seize(redoMenuItem);
        MenuItem cutMenuItem = new MenuItem("Cut").setShortcut("Ctrl + X"); MENU_ITEM_CUT.seize(cutMenuItem);
        MenuItem copyMenuItem = new MenuItem("Copy").setShortcut("Ctrl + C"); MENU_ITEM_COPY.seize(copyMenuItem);
        MenuItem pasteMenuItem = new MenuItem("Paste").setShortcut("Ctrl + V"); MENU_ITEM_PASTE.seize(pasteMenuItem);
        MenuItem deleteMenuItem = new MenuItem("Delete").setShortcut("Delete"); MENU_ITEM_DELETE.seize(deleteMenuItem);
        MenuItem deleteAllMenuItem = new MenuItem("Delete All").setShortcut("Shift + Delete"); MENU_ITEM_DELETE_ALL.seize(deleteAllMenuItem);
        MenuItem selectAllMenuItem = new MenuItem("Select All").setShortcut("Ctrl + A"); MENU_ITEM_SELECT_ALL.seize(selectAllMenuItem);

        editMenu.addItem(undoMenuItem);
        editMenu.addItem(redoMenuItem);
        editMenu.addSeparator();
        editMenu.addItem(cutMenuItem);
        editMenu.addItem(copyMenuItem);
        editMenu.addItem(pasteMenuItem);
        editMenu.addItem(deleteMenuItem);
        editMenu.addItem(deleteAllMenuItem);
        editMenu.addSeparator();
        editMenu.addItem(selectAllMenuItem);

        final Stage stage = this;
        MenuItem aboutMenuItem = new MenuItem("about", new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                Dialogs.showOKDialog(stage, "about", "Project Info\nRelease Info\nBuild Info\nCopyright notice");
            }
        });
        MENU_ITEM_ABOUT.seize(aboutMenuItem);
        helpMenu.addItem(aboutMenuItem);

        menuBar.addMenu(fileMenu);
        menuBar.addMenu(editMenu);
        //menuBar.addMenu(windowMenu);
        menuBar.addMenu(helpMenu);

        VisSelectBox<I18NBundlesEnum> languages = new VisSelectBox<I18NBundlesEnum>();
        languages.setItems(I18NBundlesEnum.values());
        if (I18NBundlesEnum.applied != null) {
            languages.setSelected(I18NBundlesEnum.applied);
        }
        languages.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                languages.getSelected().apply(modelES.eng.assetManager);
                // see https://github.com/kotcrab/vis-ui/wiki/VisUI-I18N
                // Changing bundle will not affect already exiting dialogs and widgets.
                applyLocale(languages.getSelected());
            }
        });

        menuBar.getTable().add(languages).expandX().right();
    }

    public void applyLocale(I18NBundlesEnum language) {
        initColorPicker();

        if (envAttrTable != null) { envAttrTable.applyLocale(); }
        if (aggrAttrTable != null) { aggrAttrTable.applyLocale(); }
        if (animationsManagerTable != null) { animationsManagerTable.applyLocale(); }
        if (mapGenerationTable != null) { mapGenerationTable.applyLocale(); }
        if (aiManagerTable != null) { aiManagerTable.applyLocale(); }
        if (physManagerTable != null) { physManagerTable.applyLocale(); }

        TextButtonsTextEnum.setLanguage(language);
        MenuItemsTextEnum.setLanguage(language);
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
        textureImage.setScaling(Scaling.fill);
        textureImage.setAlign(Align.bottomLeft);

        // LABELS:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label
        fpsLabel = new VisLabel("");
        miLabel = new VisLabel("");
        envLabel = new VisLabel("");

        // SELECT BOXES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox

        // Select Box: Models
        folderSelectBox = new VisSelectBox<>();
        resetFolderSelectBoxItems(game.engine.folder2models);
        folderSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (folderSelectBox.getSelectedIndex() == 0) { return; } // syncup: folder select

                modelES.eng.queueAssets(folderSelectBox.getSelected());
            }
        });

        modelSelectBox = new VisSelectBox<>();
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
        nodeSelectBox = new VisSelectBox<>();
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
        debugStageCheckBox = (VisCheckBox)CHECKBOX_DEBUG_STAGE.seize(new VisCheckBox("debug stage"));
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
        gridOriginCheckBox = (VisCheckBox)CHECKBOX_GRID_ORIGIN.seize(new VisCheckBox("origin"));
        gridOriginCheckBox.setChecked(true);

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#checkbox
        gridYCheckBox = (VisCheckBox)CHECKBOX_GRID_Y.seize(new VisCheckBox("Y"));
        gridYCheckBox.setChecked(true);

        lightsCheckBox = (VisCheckBox)CHECKBOX_LIGHTS.seize(new VisCheckBox("lights"));
        lightsCheckBox.setChecked(true);

        showSelectionCheckBox = (VisCheckBox)CHECKBOX_SHOW_SELECTION.seize(new VisCheckBox("selection"));
        showSelectionCheckBox.setChecked(false);

        origScaleCheckBox = (VisCheckBox)CHECKBOX_ORIG_SCALE.seize(new VisCheckBox("orig scale"));
        origScaleCheckBox.setChecked(false);
        origScaleCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) { modelES.eng.arrangeInSpiral(origScaleCheckBox.isChecked()); }
        });

        bbCheckBox = (VisCheckBox)CHECKBOX_BB.seize(new VisCheckBox("BB"));
        bbCheckBox.setChecked(false);
        // TODO: fix BB checkbox
        //bbCheckBox.addListener(new ChangeListener() {
        //    @Override
        //    public void changed (ChangeEvent event, Actor actor) { modelES.eng.resetBBModelInstances(); }
        //});

        nodesCheckBox = (VisCheckBox)CHECKBOX_NODES.seize(new VisCheckBox("nodes"));
        nodesCheckBox.setChecked(false);

        bonesCheckBox = (VisCheckBox)CHECKBOX_BONES.seize(new VisCheckBox("bones ("));
        bonesCheckBox.setChecked(false);

        invertBonesCheckBox = (VisCheckBox)CHECKBOX_INVERT_BONES.seize(new VisCheckBox("invert)"));
        invertBonesCheckBox.setChecked(false);

        meshPartsCheckBox = (VisCheckBox)CHECKBOX_MESH_PARTS.seize(new VisCheckBox("mesh parts"));
        meshPartsCheckBox.setChecked(false);

        verticesCheckBox = (VisCheckBox)CHECKBOX_VERTICES.seize(new VisCheckBox("vertices ("));
        verticesCheckBox.setChecked(false);

        closestCheckBox = (VisCheckBox)CHECKBOX_CLOSEST.seize(new VisCheckBox("closest to corners)"));
        closestCheckBox.setChecked(false);

        // TEXT BUTTONS:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textbutton
        attrTextButton = (VisTextButton)ATTRIBUTES.seize(new VisTextButton("ATTR"));
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

        animTextButton = (VisTextButton)ANIMATIONS.seize(new VisTextButton("ANIM"));
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

        mapTextButton = (VisTextButton)MAP.seize(new VisTextButton("MAP"));
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

        aiTextButton = (VisTextButton)AI.seize(new VisTextButton("AI"));
        unpressButton(aiTextButton);
        aiTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editCell.clearActor();
                if (!isPressed(aiTextButton)) {
                    unpressAllButtons();
                    pressButton(aiTextButton);
                } else {
                    unpressButton(aiTextButton);
                }
                resetTables();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        physTextButton = (VisTextButton)PHYSICS.seize(new VisTextButton("PHYS"));
        unpressButton(physTextButton);
        physTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editCell.clearActor();
                if (!isPressed(physTextButton)) {
                    unpressAllButtons();
                    pressButton(physTextButton);
                } else {
                    unpressButton(physTextButton);
                }
                resetTables();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        clearModelsTextButton = (VisTextButton)CLEAR_MODELS.seize(new VisTextButton("clear all"));
        unpressButton(clearModelsTextButton);
        clearModelsTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.eng.clearModelInstances();
                reset();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        deleteCurrModelTextButton = (VisTextButton)DELETE_CURRENT_MODEL.seize(new VisTextButton("delete"));
        unpressButton(deleteCurrModelTextButton);
        deleteCurrModelTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.eng.removeEditableModelInstance(modelES.eng.currMI);
                if (modelES.eng.editableMIs.size > 0) { modelES.eng.currMI = modelES.eng.editableMIs.get(0); }
                else { modelES.eng.currMI = null; }
                reset();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        saveCurrModelTextButton = (VisTextButton)SAVE_CURRENT_MODEL.seize(new VisTextButton("save"));
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
        if (modelES.eng.editableMIs.size > 0) { modelES.eng.currMI = modelES.eng.editableMIs.get(0); }
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
    public void setup2DStageLayout() {
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#layout-widgets
        // Table, Container, Stack, ScrollPane, SplitPane, Tree, VerticalGroup, HorizontalGroup

        // ROOT TABLE:
        // https://github.com/libgdx/libgdx/wiki/Table#quickstart
        rootTable = new VisTable();
        // https://github.com/libgdx/libgdx/wiki/Table#root-table
        rootTable.setFillParent(true);
        // https://github.com/libgdx/libgdx/wiki/Table#debugging
        rootTable.setDebug(false);

        rootTable.add(menuBar.getTable()).colspan(3).expandX().fillX().row();

        // https://github.com/libgdx/libgdx/wiki/Table#adding-cells
        VisTable upperPanel = new VisTable();
        upperPanel.add(new VisLabel("Folder: ")).right();
        upperPanel.add(folderSelectBox).padLeft(5f).left();
        upperPanel.add(new VisLabel("Model: ")).right();
        upperPanel.add(modelSelectBox).padLeft(5f).left();
        upperPanel.add(new VisLabel("Node: ")).padLeft(5f).right();
        upperPanel.add(nodeSelectBox).padLeft(5f).left();
        upperPanel.add().expandX();

        rootTable.add();
        rootTable.add(upperPanel).colspan(2).expandX().left();

        rootTable.row();

        VisTable leftPanel = new VisTable();
        leftPanel.add(attrTextButton).pad(1f).padLeft(0f).fillX();
        leftPanel.row();
        leftPanel.add(animTextButton).pad(1f).padLeft(0f).fillX();
        leftPanel.row();
        leftPanel.add(mapTextButton).pad(1f).padLeft(0f).fillX();
        leftPanel.row();
        leftPanel.add(physTextButton).pad(1f).padLeft(0f).fillX();
        leftPanel.row();
        leftPanel.add(aiTextButton).pad(1f).padLeft(0f).fillX();
        leftPanel.row();

        rootTable.add(leftPanel).padTop(10f).top().left();

        VisTable infoTable = new VisTable();
        infoTCell = infoTable.add().expand().top().left();
        infoTable.row();
        infoBCell = infoTable.add().expand().bottom().left().maxWidth(512).maxHeight(512);

        rootTable.add(infoTable).expand().fillY().left().pad(10f);
        editCell = rootTable.add().expand().right().top().pad(10f);

        rootTable.row();

        VisTable lowerPanel = new VisTable();
        lowerPanel.add(fpsLabel).minWidth(70f).pad(3f);
        lowerPanel.add(debugStageCheckBox).pad(3f);
        lowerPanel.add(gridOriginCheckBox).pad(3f);
        lowerPanel.add(gridYCheckBox).pad(3f);
        lowerPanel.add(lightsCheckBox).pad(3f);
        lowerPanel.add(showSelectionCheckBox).pad(3f);
        lowerPanel.add(origScaleCheckBox).pad(3f);
        // TODO: fix BB checkbox
        //lowerPanel.add(bbCheckBox).pad(3f);
        lowerPanel.add(nodesCheckBox).pad(3f);
        lowerPanel.add(bonesCheckBox);
        lowerPanel.add(invertBonesCheckBox).pad(3f);
        lowerPanel.add(meshPartsCheckBox).pad(3f);
        lowerPanel.add(verticesCheckBox);
        lowerPanel.add(closestCheckBox).pad(3f);
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
        unpressButton(aiTextButton);
        unpressButton(physTextButton);
    }
    public boolean isAnyButtonPressed() {
        return isPressed(attrTextButton) || isPressed(animTextButton) || isPressed(mapTextButton)
                || isPressed(aiTextButton) || isPressed(physTextButton);
    }
    public void unpressButton(VisTextButton btn) { btn.setStyle(tbStyleDefault); }
    public void pressButton(VisTextButton btn) { btn.setStyle(tbStyleBlue); }
    public void disableButton(VisTextButton btn) { btn.setDisabled(true); }
    public void enableButton(VisTextButton btn) { btn.setDisabled(false); }
    public boolean isPressed(VisTextButton btn) { return btn.getStyle().equals(tbStyleBlue); }
    public boolean isDisabled(VisTextButton btn) { return btn.isDisabled(); }

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

        if (isPressed(aiTextButton)) {
            aiManagerTable.setDbgModelInstance(modelES.eng.currMI);
            aiManagerTable.resetActors();
        }

        if (isPressed(physTextButton)) {
            physManagerTable.setDbgModelInstance(modelES.eng.currMI);
            physManagerTable.resetActors();
        }

        if (!isAnyButtonPressed()) {
            infoTCell.clearActor();
            infoBCell.clearActor();
            editCell.clearActor();
            textureImage.setDrawable(null);
        }
    }

    public enum TextButtonsTextEnum implements BundleText {
        ATTRIBUTES("textButton.attributes"),
        ANIMATIONS("textButton.animations"),
        MAP("textButton.map"),
        AI("textButton.ai"),
        PHYSICS("textButton.physics"),
        CLEAR_MODELS("textButton.clearAll"),
        DELETE_CURRENT_MODEL("textButton.deleteCurrentModel"),
        SAVE_CURRENT_MODEL("textButton.saveCurrentModel"),

        MENU_BAR_FILE("menuBar.textButton.file"),
        MENU_BAR_EDIT("menuBar.textButton.edit"),
        MENU_BAR_WINDOW("menuBar.textButton.window"),
        MENU_BAR_HELP("menuBar.textButton.help"),

        CHECKBOX_DEBUG_STAGE("checkBox.textButton.debugStage"),
        CHECKBOX_GRID_ORIGIN("checkBox.textButton.gridOrigin"),
        CHECKBOX_GRID_Y("checkBox.textButton.gridY"),
        CHECKBOX_LIGHTS("checkBox.textButton.lights"),
        CHECKBOX_SHOW_SELECTION("checkBox.textButton.showSelection"),
        CHECKBOX_ORIG_SCALE("checkBox.textButton.origScale"),
        CHECKBOX_BB("checkBox.textButton.bb"),
        CHECKBOX_NODES("checkBox.textButton.nodes"),
        CHECKBOX_BONES("checkBox.textButton.bones"),
        CHECKBOX_INVERT_BONES("checkBox.textButton.invertBones"),
        CHECKBOX_MESH_PARTS("checkBox.textButton.meshParts"),
        CHECKBOX_VERTICES("checkBox.textButton.vertices"),
        CHECKBOX_CLOSEST("checkBox.textButton.closest");

        private final String property;
        private TextButton instance = null;
        private static I18NBundlesEnum language;

        TextButtonsTextEnum(String property) { this.property = property; }

        public static void setLanguage(I18NBundlesEnum lang) {
            language = lang;

            for (TextButtonsTextEnum tb: TextButtonsTextEnum.values()) {
                if (tb.instance != null) { tb.instance.setText(tb.get()); }
            }
        }

        public TextButton seize(TextButton btn) {
            this.instance = btn;
            btn.setText(get());
            return btn;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.modelEditStageBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.modelEditStageBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.modelEditStageBundle.format(property, arguments) : "ERR"; }
    }

    public enum MenuItemsTextEnum implements BundleText {
        MENU_ITEM_NEW("menuItem.label.new", Keys.CONTROL_LEFT, Keys.N),
        MENU_ITEM_OPEN("menuItem.label.open", Keys.CONTROL_LEFT, Keys.O),
        MENU_ITEM_ADD_TO_PROJECT("menuItem.label.addToProject"),
        MENU_ITEM_ADD_FOLDER("menuItem.label.addFolderToProject"),
        MENU_ITEM_ADD_FILE("menuItem.label.addFileToProject"),
        MENU_ITEM_SAVE("menuItem.label.save", Keys.CONTROL_LEFT, Keys.S),
        MENU_ITEM_SETTINGS("menuItem.label.settings", Keys.CONTROL_LEFT, Keys.ALT_LEFT, Keys.S),
        MENU_ITEM_EXIT("menuItem.label.exit", Keys.ALT_LEFT, Keys.F4),

        MENU_ITEM_UNDO("menuItem.label.undo", Keys.CONTROL_LEFT, Keys.Z),
        MENU_ITEM_REDO("menuItem.label.redo", Keys.CONTROL_LEFT, Keys.SHIFT_LEFT, Keys.Z),
        MENU_ITEM_CUT("menuItem.label.cut", Keys.CONTROL_LEFT, Keys.X),
        MENU_ITEM_COPY("menuItem.label.copy", Keys.CONTROL_LEFT, Keys.C),
        MENU_ITEM_PASTE("menuItem.label.paste", Keys.CONTROL_LEFT, Keys.V),
        MENU_ITEM_DELETE("menuItem.label.delete", Keys.DEL),
        MENU_ITEM_DELETE_ALL("menuItem.label.deleteAll", Keys.SHIFT_LEFT, Keys.DEL),
        MENU_ITEM_SELECT_ALL("menuItem.label.selectAll", Keys.CONTROL_LEFT, Keys.A),

        MENU_ITEM_ABOUT("menuItem.label.about");

        private final String property;
        private final int[] keycodes;
        private MenuItem instance = null;
        private static I18NBundlesEnum language;

        MenuItemsTextEnum(String property, int... keycodes) {
            this.property = property;
            this.keycodes = keycodes;
        }

        public static void setLanguage(I18NBundlesEnum lang) {
            language = lang;

            for (MenuItemsTextEnum menuItem: MenuItemsTextEnum.values()) {
                if (menuItem.instance != null) {
                    menuItem.instance.setText(menuItem.get());
                    // resetting shortcut is mostly done to trigger packContainerMenu() -> containerMenu.pack()
                    // and have found no obvious way to do that explicitly. If container is not repacked on long i18n
                    // strings label width might exceed the container's width causing undesired visual glitches
                    menuItem.instance.setShortcut(menuItem.keycodes);
                }
            }
        }

        public MenuItem seize(MenuItem label) {
            this.instance = label;
            label.setText(get());
            return label;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.modelEditStageBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.modelEditStageBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.modelEditStageBundle.format(property, arguments) : "ERR"; }
    }
}
