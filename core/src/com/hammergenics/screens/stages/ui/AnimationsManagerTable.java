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
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
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

        animLoopCheckBox = new CheckBox("loop", stage.skin);
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
        keyFrameSlider = new Slider(0f, 10f, 0.1f, false, stage.skin);
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
                        Quaternion tmpRot = nodeAnim.node.localTransform.getRotation(new Quaternion(), true);
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
    }

    public void setDbgModelInstance(DebugModelInstance dbgModelInstance) {
        this.dbgModelInstance = dbgModelInstance;
        // Select Box: Animations
        animationSelectBox.getSelection().setProgrammaticChangeEvents(false);
        animationSelectBox.clearItems();
        if (dbgModelInstance != null && dbgModelInstance.animations != null) {
            Array<String> itemsAnimation = new Array<>();
            itemsAnimation.add("No Animation");
            dbgModelInstance.animations.forEach(a -> itemsAnimation.add(a.id));
            animationSelectBox.setItems(itemsAnimation);
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
