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

package com.hammergenics.core.stages.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.model.AnimationInfo;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationsManagerTable extends VisTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public EditableModelInstance dbgModelInstance;

    public VisLabel animInfoLabel;
    public VisSelectBox<String> animationSelectBox = null;
    public VisCheckBox animLoopCheckBox = null;
    public VisSlider keyFrameSlider = null;
    public VisTextField animIdTextField = null;
    public VisTextField keyTimeTextField = null;
    public VisTextButton createAnimTextButton = null;
    public VisTextButton deleteAnimTextButton = null;
    public VisTextButton plsKeyFrameTextButton = null;
    public VisTextButton mnsKeyFrameTextButton = null;

    public AnimationsManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        this.modelES = modelES;
        this.stage = stage;

        init();

        add(new VisLabel("Animation: ")).padLeft(5f).right();
        add(animationSelectBox).padLeft(5f).left();
        add(animLoopCheckBox).padLeft(5f).left();
        add(deleteAnimTextButton).padLeft(5f).left();
        add(createAnimTextButton).padLeft(5f).left();

        row();
        add(animIdTextField).center().colspan(5).fillX();

        row();
        VisTable kfTable = new VisTable();
        kfTable.add(mnsKeyFrameTextButton).center();
        kfTable.add(plsKeyFrameTextButton).center();
        kfTable.add(keyFrameSlider).pad(5f).center().expandX().fillX();
        kfTable.add(keyTimeTextField).width(80).maxWidth(80);
        add(kfTable).left().colspan(5).fillX();
    }

    private void init() {

        animInfoLabel = new VisLabel("");

        // Select Box: Animations
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        animationSelectBox = new VisSelectBox<>();
        animationSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI.animationController == null) { return; }

                EditableModelInstance mi = modelES.eng.currMI;

                animIdTextField.getColor().set(Color.WHITE);
                if (animationSelectBox.getSelectedIndex() - 1 < 0) { // -1 since we have "No Animation" item
                    mi.currKeyTime = 0f;
                    mi.selectedAnimation = null;
                    mi.undoAnimations();
                    setKeyFrameSlider(0f, 1f, 10f, 0f);
                    updateActors();
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
                updateActors();
            }
        });

        animLoopCheckBox = new VisCheckBox("loop");
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
                updateActors();
            }
        });

        // SLIDERS:
        keyFrameSlider = new VisSlider(0f, 1f, 10f, false);
        keyFrameSlider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (modelES.eng.currMI.selectedAnimation != null) {
                    // turning off the animation loop (assuming the change event is fired on the checkbox)
                    animLoopCheckBox.setChecked(false);
                    modelES.eng.currMI.animApplyKeyTime(keyFrameSlider.getValue());
                    updateActors();
                }
            }
        });

        animIdTextField = new VisTextField("");
        animIdTextField.setTextFieldListener(new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                EditableModelInstance mi = modelES.eng.currMI;
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
                updateActors();
            }
        });

        keyTimeTextField = new VisTextField("");
        keyTimeTextField.setTextFieldListener(new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (value < 0) { textField.getColor().set(Color.PINK); return; }

                    dbgModelInstance.currKeyTime = value;

                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    textField.getColor().set(Color.PINK);
                }
            }
        });

        deleteAnimTextButton = new VisTextButton("delete");
        stage.unpressButton(deleteAnimTextButton);
        deleteAnimTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (dbgModelInstance != null) {
                    dbgModelInstance.deleteAnimation(dbgModelInstance.selectedAnimation);
                    setDbgModelInstance(dbgModelInstance);
                }
                updateActors();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        createAnimTextButton = new VisTextButton("new");
        stage.unpressButton(createAnimTextButton);
        createAnimTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (dbgModelInstance != null) {
                    dbgModelInstance.selectedAnimation = dbgModelInstance.createAnimation();
                    setDbgModelInstance(dbgModelInstance);
                }
                updateActors();
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        plsKeyFrameTextButton = new VisTextButton("+");
        stage.unpressButton(plsKeyFrameTextButton);
        plsKeyFrameTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        mnsKeyFrameTextButton = new VisTextButton("-");
        stage.unpressButton(mnsKeyFrameTextButton);
        mnsKeyFrameTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

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

    private void setAnimSelectBox(EditableModelInstance mi) {
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

    private void updateActors() {
        keyTimeTextField.setText(String.format("%.5f", keyFrameSlider.getValue()));
        if (dbgModelInstance != null && dbgModelInstance.selectedAnimation != null) {
            Animation anim = dbgModelInstance.selectedAnimation;
            AnimationInfo info = dbgModelInstance.anim2info.get(anim);
            StringBuilder sb = new StringBuilder();
            sb.append("Animation: ").append(anim.id);

            sb.append("\n");
            sb.append("\n");
            sb.append("keyFrameSlider: ");
            sb.append("range: [")
                    .append(keyFrameSlider.getMinValue()).append(", ")
                    .append(keyFrameSlider.getMaxValue()).append("], ");
            sb.append("step: ").append(String.format("%.8f ", keyFrameSlider.getStepSize())).append(", ");
            sb.append("value: ").append(keyFrameSlider.getValue());

            sb.append("\n");
            sb.append("\n");
            sb.append("NodeAnimation: ").append("\n");
            for (ObjectMap.Entry<NodeAnimation, FloatArray> entry: info.nAnim2keyTimes) {

                sb.append("keytimes: [");
                for (float kt: entry.value.toArray()) { sb.append(String.format("%.3f ", kt)); }
                sb.append("] ");
                sb.append("(node:").append(entry.key.node.id).append(")");
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("OVERALL\nkeytimes: [");
            for (float kt: info.keyTimes.toArray()) { sb.append(String.format("%.3f ", kt)); }
            sb.append("]");

            animInfoLabel.setText(sb.toString());
        } else {
            animInfoLabel.setText("");
        }
    }

    public void setAnimation(EditableModelInstance mi) {
        Animation anim = null;
        if (mi != null) { anim = mi.selectedAnimation; }
        if (mi != null && anim != null) {
            AnimationInfo info = mi.anim2info.get(anim);
            if (info != null) { setKeyFrameSlider(0f, anim.duration, info.minStep, mi.currKeyTime); }
            animIdTextField.setText(anim.id);
            keyTimeTextField.setText(String.format("%.5f", keyFrameSlider.getValue()));
            // this is to make sure the change events are fired
            boolean isChecked = mi.animLoop;
            animLoopCheckBox.setChecked(!isChecked);
            animLoopCheckBox.setChecked(isChecked);
        } else {
            setKeyFrameSlider(0f, 1f, 10f, 0f);
            animIdTextField.setText("");
            keyTimeTextField.setText("");
        }
    }

    public void setDbgModelInstance(EditableModelInstance mi) {
        this.dbgModelInstance = mi;

        setAnimSelectBox(mi);
        setAnimation(mi);
        updateActors();
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.infoTCell.setActor(animInfoLabel);
        stage.editCell.setActor(this);
    }

    public void applyLocale() {

    }
}
