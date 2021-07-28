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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.model.AnimationInfo;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationEditVisTable extends ContextAwareVisTable {
    public VisSlider keyFrameSlider;
    public VisTextField animIdTextField;
    public VisTextField keyTimeTextField;
    public VisTextButton plsKeyFrameTextButton;
    public VisTextButton mnsKeyFrameTextButton;
    public VisLabel keyTimeSizeLabel;

    public VisTextButton chooseSoundTB;
    public VisLabel soundLabel;

    public AnimationEditVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        init();

        add(animIdTextField).center().fillX();
        row();

        VisTable kfTable = new VisTable();
        kfTable.add(mnsKeyFrameTextButton).center();
        kfTable.add(plsKeyFrameTextButton).center();
        kfTable.add(keyFrameSlider).pad(5f).center().expandX().fillX();
        kfTable.add(keyTimeTextField).pad(5f).width(80).maxWidth(80);
        kfTable.add(keyTimeSizeLabel).width(80).maxWidth(80);
        add(kfTable).left().fillX().row();

        VisTable chooseSoundTable = new VisTable();
        chooseSoundTable.add(new VisLabel("Sound: ")).padRight(5f).right();
        chooseSoundTable.add(chooseSoundTB).padRight(5f).fillX();
        chooseSoundTable.add(soundLabel).padRight(5f).expandX().fillX().left();
        chooseSoundTable.row().pad(0.5f);

        add(chooseSoundTable).left().fillX().row();
    }

    public void init() {
        // SLIDERS:
        keyFrameSlider = new VisSlider(0f, 1f, 10f, false);
        keyFrameSlider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (dbgModelInstance.selectedAnimation != null) {
                    // turning off the animation loop (assuming the change event is fired on the checkbox)
                    stage.animationsManagerTable.animationsVisWindow.animLoopCheckBox.setChecked(false);
                    dbgModelInstance.animApplyKeyTime(keyFrameSlider.getValue());
                    //updateActors();
                    keyTimeTextField.setText(String.format("%.5f", keyFrameSlider.getValue()));

                    AnimationInfo info = dbgModelInstance.anim2info.get(dbgModelInstance.selectedAnimation);
                    FileHandle soundFileHandle = info.keyTimes2sounds.get(info.floorKeyTime(keyFrameSlider.getValue()));
                    soundLabel.setText(soundFileHandle != null ? soundFileHandle.name() : "No sound selected");
                }
            }
        });

        animIdTextField = new VisTextField("");
        animIdTextField.setTextFieldListener(new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                EditableModelInstance mi = modelES.eng.getCurrMI();
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

                stage.animationsManagerTable.animationsVisWindow.setAnimSelectBox(mi);
                //updateActors();
                keyTimeTextField.setText(String.format("%.5f", keyFrameSlider.getValue()));
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

        keyTimeSizeLabel = new VisLabel();

        soundLabel = new VisLabel();

        chooseSoundTB = new VisTextButton("select");
        chooseSoundTB.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                modelES.stage.soundChooser.updateAssetsTree();
                modelES.stage.soundChooser.setListener(new ConfirmDialogListener<FileHandle>() {
                    @Override
                    public void result(FileHandle result) {
                        //Gdx.app.debug("terrain","" + " tp: " + part.description + " filehandle: " + fileHandle);
                        if (result.exists()) {
                            //Gdx.app.debug("terrain","exists");
                            if (dbgModelInstance.selectedAnimation != null) {
                                AnimationInfo info = dbgModelInstance.anim2info.get(dbgModelInstance.selectedAnimation);
                                info.keyTimes2sounds.put(info.floorKeyTime(keyFrameSlider.getValue()), result);
                                soundLabel.setText(result.name());
                            }
                        }
                    }
                });
                modelES.stage.addActor(modelES.stage.soundChooser.fadeIn());
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        setAnimationOfModelInstance(dbgModelInstance);
    }

    public void setAnimationOfModelInstance(EditableModelInstance mi) {
        if (mi != null && mi.selectedAnimation != null) {
            Animation anim = mi.selectedAnimation;
            AnimationInfo info = mi.anim2info.get(anim);
            if (info != null) {
                setKeyFrameSlider(0f, anim.duration, info.minStep, mi.currKeyTime);
                keyTimeSizeLabel.setText(Integer.toString(info.keyTimes.size));
            }
            animIdTextField.setText(anim.id);
            keyTimeTextField.setText(String.format("%.5f", keyFrameSlider.getValue()));
            // this is to make sure the change events are fired
            boolean isChecked = mi.animLoop;
            stage.animationsManagerTable.animationsVisWindow.animLoopCheckBox.setChecked(!isChecked);
            stage.animationsManagerTable.animationsVisWindow.animLoopCheckBox.setChecked(isChecked);
        } else {
            setKeyFrameSlider(0f, 1f, 10f, 0f);
            animIdTextField.setText("");
            keyTimeTextField.setText("");
        }
    }

    public void setKeyFrameSlider(float min, float max, float step, float value) {
        keyFrameSlider.setProgrammaticChangeEvents(false);
        keyFrameSlider.setRange(min, max);
        keyFrameSlider.setStepSize(step);
        keyFrameSlider.setValue(value);
        keyFrameSlider.setProgrammaticChangeEvents(true);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}