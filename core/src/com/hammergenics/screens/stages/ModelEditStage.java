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
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
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
import com.hammergenics.screens.stages.ui.AttributesManagerTable;
import com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable;
import com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType;
import com.hammergenics.utils.LibgdxUtils;

import static com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_CHANGED;
import static com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_DISABLED;
import static com.hammergenics.screens.stages.ui.attributes.BaseAttributeTable.EventType.ATTR_ENABLED;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditStage extends Stage {
    public static final Color COLOR_PRESSED = Color.RED;
    public static final Color COLOR_UNPRESSED = Color.GRAY;

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

    // 2D Stage Widgets:
    public Label miLabel;  // Model Instance Info
    public Label envLabel; // Environment Info
    public Label fpsLabel; // FPS Info
    public Image textureImage;
    public CheckBox debugStageCheckBox;
    public CheckBox gridXZCheckBox;
    public CheckBox gridYCheckBox;
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
    public SelectBox<String> animationSelectBox = null;
    public TextButton attrTextButton = null;
    public TextButton animTextButton = null;
    public TextButton clearModelsTextButton = null;

    // TODO: ANIMATIONS RELATED: to be moved to a separate class
    public CheckBox animLoopCheckBox;
    public Slider keyFrameSlider = null;

    public BaseAttributeTable.EventListener eventListener;
    
    public ModelEditStage(Viewport viewport, HGGame game, ModelEditScreen modelES) {
        super(viewport);
        this.game = game;
        this.modelES = modelES;

        setup2DStageStyling();
        setup2DStageWidgets();
        setup2DStageLayout();

        aggrAttrTable = new AggregatedAttributesManagerTable(modelES, this);
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
                if (modelSelectBox == null || folderSelectBox.getSelectedIndex() == 0) { return; } // syncup: folder select

                FileHandle array1[] = game.engine.folder2models.get(folderSelectBox.getSelected()).toArray(FileHandle.class);
                FileHandle array2[] = new FileHandle[array1.length + 2];
                System.arraycopy(array1, 0, array2, 2, array1.length);
                array2[0] = Gdx.files.local("Select Model"); // syncup: model select
                array2[1] = Gdx.files.local("ALL");

                modelSelectBox.getSelection().setProgrammaticChangeEvents(false); // even though the listeners are defined later
                modelSelectBox.clearItems();
                modelSelectBox.setItems(array2);
                modelSelectBox.getSelection().setProgrammaticChangeEvents(true);

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

        // Select Box: Animations
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        animationSelectBox = new SelectBox<>(skin);
        animationSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int index = animationSelectBox.getSelectedIndex() - 1; // -1 since we have "No Animation" item
                if (modelES.eng.currMI.animationController == null) { return; }

                if (index < 0) {
                    modelES.eng.currMI.animationDesc = null;
                    modelES.eng.currMI.animationController.setAnimation(null);
                    return;
                }

                Animation anim = modelES.eng.currMI.getAnimation(animationSelectBox.getSelected());
                if (animLoopCheckBox.isChecked()) {
                    modelES.eng.currMI.animationDesc = modelES.eng.currMI.animationController.setAnimation(anim.id, -1);
                }
                Gdx.app.debug(animationSelectBox.getClass().getSimpleName(), "animation selected: " + anim.id);
                keyFrameSlider.setValue(0f);
                keyFrameSlider.setRange(0f, anim.duration);
                keyFrameSlider.setStepSize(anim.duration/1000f);
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
                    if (!aggrAttrTable.isAnyButtonPressed()) { aggrAttrTable.pressEnv(); }
                    else if (isPressed(aggrAttrTable.envTextButton)) {
                        infoTCell.setActor(envLabel);
                    } else if (isPressed(aggrAttrTable.mtlTextButton)) {
                        infoTCell.setActor(miLabel);
                        infoBCell.setActor(textureImage);
                    }
                    editCell.setActor(aggrAttrTable);
                } else {
                    unpressButton(attrTextButton);
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

        animTextButton = new TextButton("ANIM", skin);
        unpressButton(animTextButton);

        clearModelsTextButton = new TextButton("clear", skin);
        unpressButton(clearModelsTextButton);
        clearModelsTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.eng.clearModelInstances();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        // TODO: ANIMATIONS RELATED: to be moved to a separate class
        animLoopCheckBox = new CheckBox("loop", skin);
        animLoopCheckBox.setChecked(true);
        animLoopCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI.animationController == null) { return; }
                if (animationSelectBox.getSelectedIndex() - 1 < 0) { // -1 since we have "No Animation" item
                    // "No Animation" item selected
                    modelES.eng.currMI.animationDesc = null;
                    modelES.eng.currMI.animationController.setAnimation(null);
                    return;
                }

                if (animLoopCheckBox.isChecked()) {
                    Animation anim = modelES.eng.currMI.getAnimation(animationSelectBox.getSelected());
                    modelES.eng.currMI.animationDesc = modelES.eng.currMI.animationController.setAnimation(anim.id, -1);
                } else {
                    modelES.eng.currMI.animationDesc = null;
                    modelES.eng.currMI.animationController.setAnimation(null);
                }
            }
        });

        // SLIDERS:
        keyFrameSlider = new Slider(0f, 10f, 0.1f, false, skin);
        keyFrameSlider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (animationSelectBox.getSelectedIndex() != 0) {
                    float keytime = keyFrameSlider.getValue();

                    // turning off the animation loop (assuming the change event is fired on the checkbox)
                    animLoopCheckBox.setChecked(false);

                    Animation anim = modelES.eng.currMI.getAnimation(animationSelectBox.getSelected());
                    Gdx.app.debug("ChangeListener", ""
                            + " anim id: " + anim.id + " value: " + keytime);

                    for (NodeAnimation nodeAnim:anim.nodeAnimations) {
                        // setting to false so calculateLocalTransform() would return the base values
                        nodeAnim.node.isAnimated = false;
                        nodeAnim.node.calculateLocalTransform();
                        // Getting the default base values for the node (prior any animations applied)
                        Vector3 tmpTrans = nodeAnim.node.localTransform.getTranslation(new Vector3());
                        Quaternion tmpRot = nodeAnim.node.localTransform.getRotation(new Quaternion());
                        Vector3 tmpScale = nodeAnim.node.localTransform.getScale(new Vector3());

                        // the translation keyframes if any (might be null), sorted by time ascending
                        if (nodeAnim.translation != null) {
                            for (NodeKeyframe<Vector3> nTrans:nodeAnim.translation) {
                                if (nTrans.keytime <= keytime) { tmpTrans.set(nTrans.value); }
                                else { break; }}}
                        // the rotation keyframes if any (might be null), sorted by time ascending
                        if (nodeAnim.rotation != null) {
                            for (NodeKeyframe<Quaternion> nRot:nodeAnim.rotation) {
                                if (nRot.keytime <= keytime) { tmpRot.set(nRot.value); }
                                else { break; }}}
                        // the scaling keyframes if any (might be null), sorted by time ascending
                        if (nodeAnim.scaling != null) {
                            for (NodeKeyframe<Vector3> nScale:nodeAnim.scaling) {
                                if (nScale.keytime <= keytime) { tmpScale.set(nScale.value); }
                                else { break; }}}
                        Gdx.app.debug("", ""
                                + " node.id: " + nodeAnim.node.id + " node.parts.size: " + nodeAnim.node.parts.size
                                + " trans: " + tmpTrans + " rot: " + tmpRot + " scale: " + tmpScale
                        );
                        // setting isAnimated to true so the localTransform isn't reset to the base values.
                        // the real check happens in node.calculateLocalTransform()
                        nodeAnim.node.isAnimated = true;

                        // setting the local transform to the values from key frames (if not, the default used)
                        nodeAnim.node.localTransform.set(tmpTrans, tmpRot, tmpScale);
                    }

                    // see ModelInstance.calculateTransforms:
                    // calculate both local and global transforms for each node and subnodes recursively.
                    // IMPORTANT to have isAnimated set to true so local transform is not reset
                    // seemingly bones transforms is based on the updated global transforms
                    modelES.eng.currMI.calculateTransforms();
                }
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

    public void addModelInstances(Array<FileHandle> modelFHs) {
        if (modelFHs == null) { return; }
        modelFHs.forEach(fileHandle -> addModelInstance(fileHandle));
        if (modelES.eng.dbgMIs.size > 0) { modelES.eng.currMI = modelES.eng.dbgMIs.get(0); }
    }

    public boolean addModelInstance(FileHandle assetFL) {
        boolean created = modelES.eng.addModelInstance(assetFL);
        if (created) { aggrAttrTable.setDbgModelInstance(modelES.eng.currMI); }
        return created;
    }

    public boolean addModelInstance(Model model) {
        boolean created = modelES.eng.addModelInstance(model);
        if (created) { aggrAttrTable.setDbgModelInstance(modelES.eng.currMI); }
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
        miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelES.eng.currMI));
        envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(modelES.environment,"", ""));

        if ((type & (DirectionalLightsAttribute.Type | PointLightsAttribute.Type)) != 0) {
            modelES.eng.resetLightsModelInstances(modelES.eng.currMI.getBB().getCenter(Vector3.Zero.cpy()), modelES.environment);
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
        upperPanel.add(new Label("Animation: ", skin)).padLeft(5f).right();
        // TODO: ANIMATIONS RELATED: to be moved to a separate class
        upperPanel.add(animationSelectBox).padLeft(5f).left();
        upperPanel.add(animLoopCheckBox).padLeft(5f).left();
        upperPanel.add(keyFrameSlider).padLeft(5f).left();
        upperPanel.add().expandX();

        rootTable.add();
        rootTable.add(upperPanel).colspan(2).expandX().left();

        rootTable.row();

        Table leftPanel = new Table();
        leftPanel.add(attrTextButton).fillX();
        leftPanel.row();
        leftPanel.add(animTextButton).fillX();
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
        lowerPanel.add(lightsCheckBox).pad(3f);
        lowerPanel.add(origScaleCheckBox).pad(3f);
        // TODO: fix BB checkbox
        //lowerPanel.add(bbCheckBox).pad(3f);
        lowerPanel.add(nodesCheckBox).pad(3f);
        lowerPanel.add(bonesCheckBox);
        lowerPanel.add(invertCheckBox).pad(3f);
        lowerPanel.add(meshPartsCheckBox).pad(3f);
        lowerPanel.add(clearModelsTextButton).pad(3f);
        lowerPanel.add().expandX();

        rootTable.add(lowerPanel).colspan(3).expandX().left();

        addActor(rootTable);
    }

    public void unpressAllButtons() { unpressButton(attrTextButton); unpressButton(animTextButton); }
    public void unpressButton(TextButton btn) { btn.getColor().set(COLOR_UNPRESSED); }
    public void pressButton(TextButton btn) { btn.getColor().set(COLOR_PRESSED); }
    public boolean isPressed(TextButton btn) { return btn.getColor().equals(COLOR_PRESSED); }

    public void reset() {
        if (modelES == null) { return; }

        textureImage.setDrawable(null);

        if (modelES.eng.currMI != null) {
            aggrAttrTable.setDbgModelInstance(modelES.eng.currMI);
            if (isPressed(attrTextButton)) {
                editCell.clearActor();
                editCell.setActor(aggrAttrTable);
            }

            // making sure no events fired during the nodeSelectBox reset
            nodeSelectBox.getSelection().setProgrammaticChangeEvents(false);
            nodeSelectBox.clearItems();

            String array1[] = modelES.eng.currMI.nodeid2model.keys().toArray().toArray();
            String array2[] = new String[array1.length + 1];
            System.arraycopy(array1, 0, array2, 1, array1.length);
            array2[0] = "All";

            nodeSelectBox.setItems(array2);
            nodeSelectBox.getSelection().setProgrammaticChangeEvents(true);
        }

        nodeSelectBox.getColor().set(Color.WHITE);

        if (modelES.eng.currMI == null) { return; }

        // Select Box: Animations
        Array<String> itemsAnimation = new Array<>();
        itemsAnimation.add("No Animation");
        modelES.eng.currMI.animations.forEach(a -> itemsAnimation.add(a.id));
        animationSelectBox.getSelection().setProgrammaticChangeEvents(false);
        animationSelectBox.clearItems();
        animationSelectBox.setItems(itemsAnimation);
        animationSelectBox.getSelection().setProgrammaticChangeEvents(true);
        miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelES.eng.currMI));
    }
}
