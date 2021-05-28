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

package com.hammergenics.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hammergenics.config.Config;
import com.hammergenics.screens.ModelPreviewScreen;
import com.hammergenics.ui.AttributesManagerTable;
import com.hammergenics.ui.attributes.BaseAttributeTable;
import com.hammergenics.util.LibgdxUtils;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelPreviewStage extends Stage {
    public static final Color COLOR_PRESSED = Color.RED;
    public static final Color COLOR_UNPRESSED = Color.GRAY;

    public ModelPreviewScreen modelPS;

    // 2D Stage Styling:
    public Skin skin;
    public BitmapFont labelBitmapFont;
    public Label.LabelStyle labelStyle;

    // 2D Stage Layout:
    public Table rootTable;
    public Cell<?> infoTCell = null;
    public Cell<?> infoBCell = null;
    public Cell<?> editCell = null;

    public AttributesManagerTable mtlAttrTable;
    public AttributesManagerTable envAttrTable;

    // 2D Stage Widgets:
    public Label miLabel;  // Model Instance Info
    public Label envLabel; // Environment Info
    public Label fpsLabel; // FPS Info
    public Image textureImage;
    public CheckBox debugStageCheckBox;
    public CheckBox gridXZCheckBox;
    public CheckBox gridYCheckBox;
    public CheckBox lightsCheckBox;
    public SelectBox<String> modelSelectBox;
    public SelectBox<String> nodeSelectBox;
    public SelectBox<String> animationSelectBox = null;
    public TextButton mtlTextButton = null;
    public TextButton envTextButton = null;
    public TextButton camTextButton = null;

    public BaseAttributeTable.EventListener eventListener;
    
    public ModelPreviewStage(Viewport viewport, ModelPreviewScreen modelPS) {
        super(viewport);
        this.modelPS = modelPS;
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
        modelSelectBox = new SelectBox<>(skin);

        // Select Box: Models
        Array<String> itemsModel = new Array<>();
        for (Model model: modelPS.models) {
            if (model.materials.size == 0 && model.meshes.size == 0 && model.meshParts.size == 0) {
                continue;
            }

            itemsModel.add(modelPS.assetManager.getAssetFileName(model));
        }

        String noModelsAvailable = "No models available";
        if (itemsModel.size == 0) { itemsModel.add(noModelsAvailable); }

        modelSelectBox.clearItems();
        modelSelectBox.setItems(itemsModel);
        modelSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelSelectBox.getSelected().equals(noModelsAvailable)) {
                    return;
                }
                modelPS.switchModelInstance(modelSelectBox.getSelected(), null, -1);
                Gdx.app.debug(modelSelectBox.getClass().getSimpleName(),
                        "model selected: " + modelSelectBox.getSelected());
            }
        });

        // Select Box: Nodes
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        nodeSelectBox = new SelectBox<>(skin);
        nodeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelPS.currMI == null) {
                    return; // we're in the init phase...
                }
                if (nodeSelectBox.getSelectedIndex() == 0) { // 'all' selected
                    modelPS.switchModelInstance(modelSelectBox.getSelected(), null, -1);
                } else {
                    modelPS.switchModelInstance(modelSelectBox.getSelected(),
                            nodeSelectBox.getSelected(), nodeSelectBox.getSelectedIndex() - 1); // -1 since there's 'all' item
                }
            }
        });


        // Select Box: Animations
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        animationSelectBox = new SelectBox<>(skin);
        animationSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                modelPS.currMI.animationIndex = animationSelectBox.getSelectedIndex() - 1; // -1 since we have "No Animation" item
                if (modelPS.currMI.animationController == null) { return; }

                if (modelPS.currMI.animationIndex < 0) {
                    modelPS.currMI.animationController.setAnimation(null);
                    return;
                }

                modelPS.currMI.animationDesc = modelPS.currMI.animationController.setAnimation(modelPS.currMI.animations.get(modelPS.currMI.animationIndex).id, -1);
                Gdx.app.debug(animationSelectBox.getClass().getSimpleName(),
                        "animation selected: " + modelPS.currMI.animations.get(modelPS.currMI.animationIndex).id);
                // Uncomment to get gen_* files with fields contents:
                //LibGDXUtil.getFieldsContents(animationDesc, 3,  "", true);
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

        lightsCheckBox = new CheckBox("lights (ENV)", skin);
        lightsCheckBox.setChecked(true);

        // TEXT BUTTONS:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textbutton
        mtlTextButton = new TextButton("MTL", skin);
        mtlTextButton.getColor().set(COLOR_UNPRESSED);
        mtlTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                infoTCell.clearActor();
                infoBCell.clearActor();
                editCell.clearActor();
                if (mtlTextButton.getColor().equals(COLOR_UNPRESSED)) {
                    // clearing all buttons first
                    mtlTextButton.getColor().set(COLOR_UNPRESSED);
                    envTextButton.getColor().set(COLOR_UNPRESSED);
                    camTextButton.getColor().set(COLOR_UNPRESSED);

                    // setting MTL specific actors
                    mtlTextButton.getColor().set(COLOR_PRESSED);
                    infoTCell.setActor(miLabel);
                    infoBCell.setActor(textureImage);
                    editCell.setActor(mtlAttrTable);
                } else if (mtlTextButton.getColor().equals(COLOR_PRESSED)) {
                    mtlTextButton.getColor().set(COLOR_UNPRESSED);
                    infoTCell.clearActor();
                    infoBCell.clearActor();
                    editCell.clearActor();
                }

                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        envTextButton = new TextButton("ENV", skin);
        envTextButton.getColor().set(COLOR_UNPRESSED);
        envTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                infoTCell.clearActor();
                infoBCell.clearActor();
                editCell.clearActor();
                if (envTextButton.getColor().equals(COLOR_UNPRESSED)) {
                    // clearing all buttons first
                    mtlTextButton.getColor().set(COLOR_UNPRESSED);
                    envTextButton.getColor().set(COLOR_UNPRESSED);
                    camTextButton.getColor().set(COLOR_UNPRESSED);

                    // setting ENV specific actors
                    envTextButton.getColor().set(COLOR_PRESSED);
                    infoTCell.setActor(envLabel);
                    editCell.setActor(envAttrTable);
                } else if (envTextButton.getColor().equals(COLOR_PRESSED)) {
                    envTextButton.getColor().set(COLOR_UNPRESSED);
                    infoTCell.clearActor();
                    infoBCell.clearActor();
                    editCell.clearActor();
                }

                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        camTextButton = new TextButton("CAM", skin);
        camTextButton.getColor().set(COLOR_UNPRESSED);


        // temporarily placing it here:
        eventListener = new BaseAttributeTable.EventListener() {
            @Override
            public void onAttributeEnabled(long type, String alias) {
                miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelPS.currMI));
                envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelPS.environment,"", ""));

                if (modelPS.currMI != null && (type & (DirectionalLightsAttribute.Type | PointLightsAttribute.Type)) != 0) {
                    modelPS.resetLightsModel(modelPS.currMI.maxD, modelPS.currMI.center);
                }
//                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "onAttributeEnabled: 0x" + Long.toHexString(type) + " alias: " + alias);
            }

            @Override
            public void onAttributeDisabled(long type, String alias) {
                miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelPS.currMI));
                envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelPS.environment,"", ""));

                if (modelPS.currMI != null && (type & (DirectionalLightsAttribute.Type | PointLightsAttribute.Type)) != 0) {
                    modelPS.resetLightsModel(modelPS.currMI.maxD, modelPS.currMI.center);
                }
//                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "onAttributeDisabled: 0x" + Long.toHexString(type) + " alias: " + alias);
            }

            @Override
            public void onAttributeChange(long type, String alias) {
                miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelPS.currMI));
                envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelPS.environment,"", ""));

                if (modelPS.currMI != null && (type & (DirectionalLightsAttribute.Type | PointLightsAttribute.Type)) != 0) {
                    modelPS.resetLightsModel(modelPS.currMI.maxD, modelPS.currMI.center);
                }
//                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "onAttributeChange: 0x" + Long.toHexString(type) + " alias: " + alias);
            }
        };
    }

    /**
     *
     */
    public void setup2DStageStyling() {
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#loading-a-ttf-using-the-assethandler
        labelBitmapFont = modelPS.assetManager.get(Config.ASSET_FILE_NAME_FONT, BitmapFont.class);
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

        // Attributes related:
        //mtlAttrTable = new Table();
        //envAttrTable = new Table();

        // ROOT TABLE:
        // https://github.com/libgdx/libgdx/wiki/Table#quickstart
        rootTable = new Table();
        // https://github.com/libgdx/libgdx/wiki/Table#root-table
        rootTable.setFillParent(true);
        // https://github.com/libgdx/libgdx/wiki/Table#debugging
        rootTable.setDebug(false);

        // https://github.com/libgdx/libgdx/wiki/Table#adding-cells
        Table upperPanel = new Table();
        upperPanel.add(new Label("Models: ", skin)).right();
        upperPanel.add(modelSelectBox).padLeft(5f).left();
        upperPanel.add(new Label("Nodes: ", skin)).padLeft(5f).right();
        upperPanel.add(nodeSelectBox).padLeft(5f).left();
        upperPanel.add(new Label("Animations: ", skin)).padLeft(5f).right();
        upperPanel.add(animationSelectBox).padLeft(5f).left();
        upperPanel.add().expandX();

        rootTable.add();
        rootTable.add(upperPanel).colspan(2).expandX().left();

        rootTable.row();

        Table leftPanel = new Table();
        leftPanel.add(mtlTextButton).fillX();
        leftPanel.row();
        leftPanel.add(envTextButton).fillX();
        leftPanel.row();
        leftPanel.add(camTextButton).fillX();
        leftPanel.row();

        rootTable.add(leftPanel).padTop(10f).top().left();

        Table infoTable = new Table();
        infoTCell = infoTable.add().expand().top().left();
        infoTable.row();
        infoBCell = infoTable.add().expand().bottom().left();

        rootTable.add(infoTable).expand().fillY().left().pad(10f);
        editCell = rootTable.add().expand().right().top().pad(10f);

        rootTable.row();

        Table lowerPanel = new Table();
        lowerPanel.add(fpsLabel).minWidth(70f).pad(3f);
        lowerPanel.add(debugStageCheckBox).pad(3f);
        lowerPanel.add(gridXZCheckBox).pad(3f);
        lowerPanel.add(gridYCheckBox).pad(3f);
        lowerPanel.add(lightsCheckBox).pad(3f);
        lowerPanel.add().expandX();

        rootTable.add(lowerPanel).colspan(3).expandX().left();

        addActor(rootTable);
    }

    public void resetPages() {
        // **************************
        // **** ATTRIBUTES 2D UI ****
        // **************************
        envAttrTable = new AttributesManagerTable(skin, modelPS.environment, modelPS);
        envAttrTable.setListener(eventListener);
        envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelPS.environment,"", ""));
        // TODO: this should be decoupled from screen eventually
        if (envTextButton.getColor().equals(COLOR_PRESSED)) {
            editCell.clearActor();
            editCell.setActor(envAttrTable);
        }

        textureImage.setDrawable(null);
        if (modelPS.currMI.materials != null && modelPS.currMI.materials.size > 0) {
            mtlAttrTable = new AttributesManagerTable(skin, modelPS.currMI.materials.get(0), modelPS);
            mtlAttrTable.setListener(eventListener);
            // Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "" );

            // TODO: this should be decoupled from screen eventually
            if (mtlTextButton.getColor().equals(COLOR_PRESSED)) {
                editCell.clearActor();
                editCell.setActor(mtlAttrTable);
            }
        }
    }
}
