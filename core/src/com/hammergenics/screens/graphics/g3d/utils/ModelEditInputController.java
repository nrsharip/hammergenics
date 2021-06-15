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

package com.hammergenics.screens.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Camera;
import com.hammergenics.screens.ModelEditScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditInputController extends SpectatorInputController {
    public ModelEditScreen modelES;

    public ModelEditInputController(ModelEditScreen modelES, Camera camera) {
        this(modelES, camera, new ModelEditorGestureProcessor());
    }

    public ModelEditInputController(ModelEditScreen modelES, Camera camera, ModelEditorGestureProcessor megp) {
        super(camera, megp);
        this.modelES = modelES;
        megp.meic = this;
    }

    public static class ModelEditorGestureProcessor extends SpectatorGestureProcessor {
        public ModelEditInputController meic;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            if (meic.modelES.checkTouchDown(x, y, pointer, button)) {
                return super.touchDown(x, y, pointer, button);
            } else {
                return false;
            }
            //return super.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            if (meic.modelES != null) { meic.modelES.checkTap(x, y, count, button); }
            return super.tap(x, y, count, button);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (meic.modelES != null) {
                if (meic.modelES.checkPan(x, y, deltaX, deltaY, touchDownButton, meic.overallDistance)) {
                    return super.pan(x, y, deltaX, deltaY);
                } else {
                    return false;
                }
            }
            return super.pan(x, y, deltaX, deltaY);
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            if (meic.modelES != null) {
                if (meic.modelES.checkPanStop(x, y, pointer, button)) {
                    return super.panStop(x, y, pointer, button);
                } else {
                    return false;
                }
            }
            return super.panStop(x, y, pointer, button);
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (modelES != null) { modelES.checkMouseMoved(screenX, screenY); }

        return super.mouseMoved(screenX, screenY);
    }
}