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
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
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

    public CheckBox animLoopCheckBox;
    public Slider keyFrameSlider = null;

    public AnimationsManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.stage = stage;

        init();

        add(new Label("Animation: ", stage.skin)).padLeft(5f).right();
        add(animationSelectBox).padLeft(5f).left();
        add(animLoopCheckBox).padLeft(5f).left();
        row();
        add(keyFrameSlider).padLeft(5f).left().colspan(3).fillX();
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

                if (animationSelectBox.getSelectedIndex() - 1 < 0) { // -1 since we have "No Animation" item
                    mi.currKeyTime = 0f;
                    mi.selectedAnimation = null;
                    mi.removeAnimations();
                    setKeyFrameSlider(0f, 1f, 10f, 0f);
                    return;
                }

                Animation anim = mi.getAnimation(animationSelectBox.getSelected());
                AnimationInfo info = mi.anim2info.get(anim);
                mi.selectedAnimation = anim;

                Gdx.app.debug(animationSelectBox.getClass().getSimpleName(), "animation selected: " + anim.id);
                setKeyFrameSlider(0f, anim.duration, info.minStep, 0f);

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
                        modelES.eng.currMI.removeAnimations();
                        modelES.eng.currMI.animApplyKeyTime(keyFrameSlider.getValue());
                    }
                } else {
                    // no animation selected
                    modelES.eng.currMI.removeAnimations();
                }
            }
        });

        // SLIDERS:
        keyFrameSlider = new Slider(0f, 1f, 10f, false, stage.skin);
        keyFrameSlider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (animationSelectBox.getSelectedIndex() != 0) {
                    // turning off the animation loop (assuming the change event is fired on the checkbox)
                    animLoopCheckBox.setChecked(false);
                    modelES.eng.currMI.animApplyKeyTime(keyFrameSlider.getValue());
                }
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

    public void setDbgModelInstance(DebugModelInstance mi) {
        this.dbgModelInstance = mi;
        // Select Box: Animations
        animationSelectBox.getSelection().setProgrammaticChangeEvents(false);
        animationSelectBox.clearItems();
        if (mi != null && mi.animations != null) {
            Array<String> itemsAnimation = new Array<>();
            itemsAnimation.add("No Animation");
            mi.animations.forEach(a -> itemsAnimation.add(a.id));
            animationSelectBox.setItems(itemsAnimation);
            if (mi.selectedAnimation != null) {
                animationSelectBox.setSelected(mi.selectedAnimation.id);
                setKeyFrameSlider(0f, mi.selectedAnimation.duration,
                        mi.anim2info.get(mi.selectedAnimation).minStep, mi.currKeyTime);
            } else {
                setKeyFrameSlider(0f, 1f, 10f, 0f);
            }

            // this is to make sure the change events are fired
            boolean isChecked = mi.animLoop;
            animLoopCheckBox.setChecked(!isChecked);
            animLoopCheckBox.setChecked(isChecked);
        }
        animationSelectBox.getSelection().setProgrammaticChangeEvents(true);
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.editCell.setActor(this);
    }
}
