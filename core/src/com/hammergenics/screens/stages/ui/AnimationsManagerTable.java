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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.graphics.g3d.model.AnimationInfo;
import com.hammergenics.screens.stages.ModelEditStage;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationsManagerTable extends HGTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public DebugModelInstance dbgModelInstance;

    public SelectBox<String> animationSelectBox = null;
    public CheckBox animLoopCheckBox = null;
    public Slider keyFrameSlider = null;
    public TextField animIdTextField = null;
    public TextButton deleteAnimTextButton = null;

    public AnimationsManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.stage = stage;

        init();

        add(new Label("Animation: ", stage.skin)).padLeft(5f).right();
        add(animationSelectBox).padLeft(5f).left();
        add(animLoopCheckBox).padLeft(5f).left();
        add(deleteAnimTextButton).padLeft(5f).left();
        row();
        add(keyFrameSlider).padLeft(5f).left().colspan(3).fillX();
        row();
        add(animIdTextField).padLeft(5f).left().colspan(3).fillX();
    }

    private void init() {
        // Select Box: Animations
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        animationSelectBox = new SelectBox<>(stage.skin);
        animationSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI.animationController == null) { return; }

                DebugModelInstance mi = modelES.eng.currMI;

                animIdTextField.getColor().set(Color.WHITE);
                if (animationSelectBox.getSelectedIndex() - 1 < 0) { // -1 since we have "No Animation" item
                    mi.currKeyTime = 0f;
                    mi.selectedAnimation = null;
                    mi.undoAnimations();
                    setKeyFrameSlider(0f, 1f, 10f, 0f);
                    animIdTextField.setText("");
                    return;
                }

                Animation anim = mi.getAnimation(animationSelectBox.getSelected());
                AnimationInfo info = mi.anim2info.get(anim);
                mi.selectedAnimation = anim;

                Gdx.app.debug(animationSelectBox.getClass().getSimpleName(), "animation selected: " + anim.id);
                setKeyFrameSlider(0f, anim.duration, info.minStep, 0f);
                animIdTextField.setText(anim.id);

                // this is to make sure the change events are fired
                boolean isChecked = mi.animLoop;
                animLoopCheckBox.setChecked(!isChecked);
                animLoopCheckBox.setChecked(isChecked);
            }
        });

        animLoopCheckBox = new CheckBox("loop", stage.skin);
        animLoopCheckBox.setChecked(true);
        animLoopCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI == null) { return; }
                boolean checked = modelES.eng.currMI.animLoop = animLoopCheckBox.isChecked();

                if (modelES.eng.currMI.selectedAnimation != null) {
                    if (checked) {
                        // animation selected, loop checked
                        Animation anim = modelES.eng.currMI.selectedAnimation;
                        modelES.eng.currMI.animationDesc = modelES.eng.currMI.animationController.setAnimation(anim.id, -1);
                    } else {
                        // animation selected, loop not checked
                        modelES.eng.currMI.undoAnimations();
                        modelES.eng.currMI.animApplyKeyTime(keyFrameSlider.getValue());
                    }
                } else {
                    // no animation selected
                    modelES.eng.currMI.undoAnimations();
                }
            }
        });

        // SLIDERS:
        keyFrameSlider = new Slider(0f, 1f, 10f, false, stage.skin);
        keyFrameSlider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI.selectedAnimation != null) {
                    // turning off the animation loop (assuming the change event is fired on the checkbox)
                    animLoopCheckBox.setChecked(false);
                    modelES.eng.currMI.animApplyKeyTime(keyFrameSlider.getValue());
                }
            }
        });

        animIdTextField = new TextField("", stage.skin);
        animIdTextField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                DebugModelInstance mi = modelES.eng.currMI;
                if (mi == null || mi.selectedAnimation == null) { return; }
                Animation anim = mi.selectedAnimation;

                String updAnimId = textField.getText();
                textField.getColor().set(Color.WHITE);
                if (updAnimId.equals(anim.id)) { return; }

                Array<String> animIds = mi.getAnimIds(new Array<>());

                if (updAnimId.equals("") || animIds.contains(updAnimId, false)) {
                    textField.getColor().set(Color.RED);
                    return;
                }

                anim.id = updAnimId;

                setAnimSelectBox(mi);
            }
        });

        deleteAnimTextButton = new TextButton("delete", stage.skin);
        stage.unpressButton(deleteAnimTextButton);
        deleteAnimTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (dbgModelInstance != null) {
                    dbgModelInstance.deleteAnimation(dbgModelInstance.selectedAnimation);
                    setDbgModelInstance(dbgModelInstance);
                }
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });
    }

    private void setKeyFrameSlider(float min, float max, float step, float value) {
        keyFrameSlider.setProgrammaticChangeEvents(false);
        keyFrameSlider.setRange(min, max);
        keyFrameSlider.setStepSize(step);
        keyFrameSlider.setValue(value);
        keyFrameSlider.setProgrammaticChangeEvents(true);
    }

    private void setAnimSelectBox(DebugModelInstance mi) {
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

    public void setAnimation(DebugModelInstance mi, Animation anim) {
        if (mi != null && anim != null) {
            AnimationInfo info = mi.anim2info.get(anim);
            if (info != null) { setKeyFrameSlider(0f, anim.duration, info.minStep, mi.currKeyTime); }
            animIdTextField.setText(anim.id);

            // this is to make sure the change events are fired
            boolean isChecked = mi.animLoop;
            animLoopCheckBox.setChecked(!isChecked);
            animLoopCheckBox.setChecked(isChecked);
        } else {
            setKeyFrameSlider(0f, 1f, 10f, 0f);
            animIdTextField.setText("");
        }
    }

    public void setDbgModelInstance(DebugModelInstance mi) {
        this.dbgModelInstance = mi;

        setAnimSelectBox(mi);
        if (mi != null) { setAnimation(mi, mi.selectedAnimation); }
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.editCell.setActor(this);
    }
}
