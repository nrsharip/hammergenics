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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.input.HGInputController;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ScreenInputController extends HGInputController {
    public Camera camera;
    public float unitSize = 1f;
    public Vector3 rotateAround = new Vector3();

    public ScreenInputController(Camera camera) {
        this(camera, new ScreenGestureProcessor(), null);

        Array<KeyInfo> keys = new Array<>(KeyInfo.class);
        keys.add(new KeyInfo(Keys.W, new KeyListener() {
            @Override
            public void onKeyDown(int keycode) { Gdx.app.debug(getTag(), "W pressed"); }
            @Override
            public void onKeyUp(int keycode) { Gdx.app.debug(getTag(), "W unpressed"); }
            @Override
            public void onKeyTyped(char character) { Gdx.app.debug(getTag(), "W Typed"); }
        }));
        setKeys(keys);
    }

    public ScreenInputController(Camera camera, Array<KeyInfo> keys) {
        this(camera, new ScreenGestureProcessor(), keys);
    }

    protected ScreenInputController(Camera camera, ScreenGestureProcessor gp, Array<KeyInfo> keys) {
        super(gp, keys);
        gp.sic = this; // this is a workaround since GestureDetector.listener isn't visible here and have no getters...
        this.camera = camera;
    }

    public static class ScreenGestureProcessor extends HGGestureProcessor {
        public ScreenInputController sic;

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            boolean result = super.pan(x, y, deltaX, deltaY);

            if (sic == null) return result;

            Vector3 v1 = Vector3.Zero.cpy(), v2 = Vector3.Zero.cpy();
            float fracX = deltaX / Gdx.graphics.getWidth(), fracY = deltaY / Gdx.graphics.getHeight();
            switch (touchDownButton) {
                case Buttons.LEFT:
                    // X delta: Moving the camera position along the cross product of camera's direction and up vectors
                    sic.camera.translate(v1.set(sic.camera.direction).crs(sic.camera.up).nor().scl(2 * -fracX * sic.unitSize));
                    // Y delta: Moving the camera position along the camera's up vector
                    sic.camera.translate(v2.set(sic.camera.up).scl(2 * fracY * sic.unitSize));
                    // in summary, the camera moves within the [Direction x Up][Up] plane.
                    sic.rotateAround.add(v1).add(v2); // shifting the rotation point along with the camera position
                    break;
                case Buttons.MIDDLE:
                    break;
                case Buttons.RIGHT:
                    // camera Direction and camera Up vectors cross product XZ projection
                    v1.set(sic.camera.direction).crs(sic.camera.up).y = 0f;
                    // Y delta: point = rotateAround, axis = unit [Direction x Up], angle = fraction Y * -360 degrees
                    sic.camera.rotateAround(sic.rotateAround, v1.nor(), fracY * -360f);
                    // X delta: point = rotateAround, axis = unit Y (0, 1, 0), angle = fraction X * -360 degrees
                    sic.camera.rotateAround(sic.rotateAround, Vector3.Y, fracX * -360f);
                    sic.camera.update();
                    break;
            }

            return result;
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        boolean result = super.scrolled(amountX, amountY);

        camera.translate(new Vector3(camera.direction).scl(-amountY * unitSize));

        return result;
    }

    private String getTag() {
        return getClass().getSimpleName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public void update(float delta) { camera.update(); }
}