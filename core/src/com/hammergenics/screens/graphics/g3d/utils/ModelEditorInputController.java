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
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditorInputController extends SpectatorInputController {
    public ModelPreviewScreen screen;

    public ModelEditorInputController(ModelPreviewScreen screen, Camera camera) {
        this(screen, camera, new ModelEditorGestureProcessor(), new KeyProcessor(), new MouseProcessor());
    }

    public ModelEditorInputController(ModelPreviewScreen screen, Camera camera, ModelEditorGestureProcessor megp,
                                      KeyProcessor kl, MouseProcessor ml) {
        super(camera, megp, kl, ml);
        this.screen = screen;
        megp.meic = this;
        kl.meic = this;
        ml.meic = this;
    }

    public static class ModelEditorGestureProcessor extends SpectatorGestureProcessor {
        public ModelEditorInputController meic;

        @Override
        public boolean tap(float x, float y, int count, int button) {
            if (meic.screen != null) { meic.screen.checkTap(x, y, count, button); }
            return super.tap(x, y, count, button);
        }
    }

    public static class KeyProcessor extends KeyAdapter {
        public ModelEditorInputController meic;
    }

    public static class MouseProcessor extends MouseAdapter {
        public ModelEditorInputController meic;

        @Override
        public boolean onMouseMoved(int screenX, int screenY) {
            if (meic.screen != null) { meic.screen.checkMouseMoved(screenX, screenY); }

            return super.onMouseMoved(screenX, screenY);
        }
    }
}