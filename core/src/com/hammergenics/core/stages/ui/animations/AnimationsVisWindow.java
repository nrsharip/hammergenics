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

package com.hammergenics.core.stages.ui.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.model.AnimationInfo;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisWindow;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationsVisWindow extends ContextAwareVisWindow {
    public VisSelectBox<String> animationSelectBox;
    public VisCheckBox animLoopCheckBox;
    public VisTextButton createAnimTextButton;
    public VisTextButton deleteAnimTextButton;
    public VisTextButton chooseAnimModelTextButton;

    public AnimationEditVisTable animationEditVisTable;

    public AnimationsVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Animations", modelES, stage);

        init();

        VisTable topPanelTable = new VisTable();
        topPanelTable.add(new VisLabel("Animation: ")).padLeft(5f).right();
        topPanelTable.add(animationSelectBox).padLeft(5f).left();
        topPanelTable.add(animLoopCheckBox).padLeft(5f).left();
        topPanelTable.add(deleteAnimTextButton).padLeft(5f).left();
        topPanelTable.add(createAnimTextButton).padLeft(5f).left();
        topPanelTable.add(chooseAnimModelTextButton).padLeft(5f).left();

        add(topPanelTable).row();
        add(new Separator("menu")).expandX().fillX().pad(5f).row();
        add(animationEditVisTable).row();
    }

    public void init() {
        // Select Box: Animations
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        animationSelectBox = new VisSelectBox<>();
        animationSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelES.eng.getCurrMI().animationController == null) { return; }

                EditableModelInstance mi = modelES.eng.getCurrMI();

                animationEditVisTable.animIdTextField.getColor().set(Color.WHITE);
                if (animationSelectBox.getSelectedIndex() - 1 < 0) { // -1 since we have "No Animation" item
                    mi.currKeyTime = 0f;
                    mi.selectedAnimation = null;
                    mi.undoAnimations();
                    animationEditVisTable.setKeyFrameSlider(0f, 1f, 10f, 0f);
                    //updateActors();
                    animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
                    return;
                }

                Animation anim = mi.getAnimation(animationSelectBox.getSelected());
                AnimationInfo info = mi.anim2info.get(anim);
                mi.selectedAnimation = anim;

                Gdx.app.debug(animationSelectBox.getClass().getSimpleName(), "animation selected: " + anim.id);
                animationEditVisTable.setKeyFrameSlider(0f, anim.duration, info.minStep, 0f);
                animationEditVisTable.animIdTextField.setText(anim.id);

                // this is to make sure the change events are fired
                boolean isChecked = mi.animLoop;
                animLoopCheckBox.setChecked(!isChecked);
                animLoopCheckBox.setChecked(isChecked);
                animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
            }
        });

        animLoopCheckBox = new VisCheckBox("Loop");
        animLoopCheckBox.setChecked(true);
        animLoopCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (modelES.eng.getCurrMI() == null) { return; }
                boolean checked = modelES.eng.getCurrMI().animLoop = animLoopCheckBox.isChecked();

                if (modelES.eng.getCurrMI().selectedAnimation != null) {
                    if (checked) {
                        // animation selected, loop checked
                        Animation anim = modelES.eng.getCurrMI().selectedAnimation;
                        modelES.eng.getCurrMI().animationDesc = modelES.eng.getCurrMI().animationController.setAnimation(anim.id, -1);
                    } else {
                        // animation selected, loop not checked
                        modelES.eng.getCurrMI().undoAnimations();
                        modelES.eng.getCurrMI().animApplyKeyTime(animationEditVisTable.keyFrameSlider.getValue());
                    }
                } else {
                    // no animation selected
                    modelES.eng.getCurrMI().undoAnimations();
                }
                animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
            }
        });

        deleteAnimTextButton = new VisTextButton("Delete");
        stage.unpressButton(deleteAnimTextButton);
        deleteAnimTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (dbgModelInstance != null) {
                    dbgModelInstance.deleteAnimation(dbgModelInstance.selectedAnimation);
                    stage.animationsManagerTable.animationsVisWindow.setAnimSelectBox(dbgModelInstance);
                    animationEditVisTable.setAnimationOfModelInstance(dbgModelInstance);
                    //updateActors();
                    animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
                }
                animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        createAnimTextButton = new VisTextButton("New");
        stage.unpressButton(createAnimTextButton);
        createAnimTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (dbgModelInstance != null) {
                    dbgModelInstance.selectedAnimation = dbgModelInstance.createAnimation();
                    setAnimSelectBox(dbgModelInstance);
                    animationEditVisTable.setAnimationOfModelInstance(dbgModelInstance);
                    animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
                }
                animationEditVisTable.keyTimeTextField.setText(String.format("%.5f", animationEditVisTable.keyFrameSlider.getValue()));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        chooseAnimModelTextButton = new VisTextButton("Copy from model");
        chooseAnimModelTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.stage.modelChooser.updateAssetsTree();
                modelES.stage.modelChooser.setListener(new ConfirmDialogListener<FileHandle>() {
                    @Override
                    public void result(FileHandle result) {
                        //Gdx.app.debug("anim","" + " result: " + result);
                        if (result.exists()) {
                            //Gdx.app.debug("anim","exists");
                            eng.copyExternalAnimationsV2(result, dbgModelInstance);

                            setAnimSelectBox(dbgModelInstance);
                        }
                    }
                });
                modelES.stage.addActor(modelES.stage.modelChooser.fadeIn());
                return super.touchDown(event, x, y, pointer, button);
            }
        });


        animationEditVisTable = new AnimationEditVisTable(modelES, stage);
    }

    public void setAnimSelectBox(EditableModelInstance mi) {
        // Select Box: Animations
        animationSelectBox.getSelection().setProgrammaticChangeEvents(false);
        animationSelectBox.clearItems();
        if (mi != null && mi.animations != null) {
            Array<String> itemsAnimation = new Array<>();
            itemsAnimation.add("No Animation");
            mi.getAnimIds(itemsAnimation);
            animationSelectBox.setItems(itemsAnimation);

            if (mi.selectedAnimation != null) { animationSelectBox.setSelected(mi.selectedAnimation.id); }
        }
        animationSelectBox.getSelection().setProgrammaticChangeEvents(true);
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        animationEditVisTable.setDbgModelInstances(mis);

        setAnimSelectBox(dbgModelInstance);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void applyLocale() { }
}