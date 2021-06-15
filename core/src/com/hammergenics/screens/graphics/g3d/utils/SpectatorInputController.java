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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.hammergenics.screens.input.HGInputController;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class SpectatorInputController extends HGInputController {
    public Camera camera;
    public float unitDistance = 1f;
    public float overallDistance = 1f;
    public Vector3 rotateAround = new Vector3();

    public SpectatorInputController(Camera camera) { this(camera, new SpectatorGestureProcessor()); }

    public SpectatorInputController(Camera camera, SpectatorGestureProcessor gp) {
        super(gp);
        gp.sic = this; // this is a workaround since GestureDetector.listener isn't visible here and have no getters...
        this.camera = camera;
    }

    public static class SpectatorGestureProcessor extends HGGestureProcessor {
        public SpectatorInputController sic;

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (sic == null) return super.pan(x, y, deltaX, deltaY);

            Vector3 tmpV1 = Vector3.Zero.cpy(), tmpV2 = Vector3.Zero.cpy();
            float fracX = deltaX / Gdx.graphics.getWidth(), fracY = deltaY / Gdx.graphics.getHeight();
            switch (touchDownButton) {
                case Buttons.LEFT:
                    float distance = Math.max(sic.unitDistance, sic.overallDistance);
                    // X delta: Moving the camera position along the cross product of camera's direction and up vectors
                    sic.camera.translate(tmpV1.set(sic.camera.direction).crs(sic.camera.up).nor().scl(4 * -fracX * distance));
                    // camera's up vector XZ projection
                    tmpV2.set(sic.camera.up).y = 0;
                    // Y delta: Moving the camera position along the camera's up vector's XZ projection
                    sic.camera.translate(tmpV2.nor().scl(4 * fracY * distance));
                    // in summary, the camera moves within the [Direction x Up][Up's XZ projection] plane.
                    sic.rotateAround.add(tmpV1).add(tmpV2); // shifting the rotation point along with the camera position
                    break;
                case Buttons.MIDDLE:
                    break;
                case Buttons.RIGHT:
                    // camera Direction and camera Up vectors cross product's XZ projection
                    tmpV1.set(sic.camera.direction).crs(sic.camera.up).y = 0f;
                    // Y delta: point = rotateAround, axis = unit [Direction x Up], angle = fraction Y * -360 degrees
                    sic.camera.rotateAround(sic.rotateAround, tmpV1.nor(), fracY * -360f);
                    // X delta: point = rotateAround, axis = unit Y (0, 1, 0), angle = fraction X * -360 degrees
                    sic.camera.rotateAround(sic.rotateAround, Vector3.Y, fracX * -360f);
                    sic.camera.update();
                    break;
            }
            return super.pan(x, y, deltaX, deltaY);
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float step = amountY * unitDistance;
        // making sure we don't step beyond the rotation point.
        // assuming that camera's [direction vector] and [rotateAround vector sub camera position vector] are collinear.
        // if they are not use the dot product between the two.
        if (step + new Vector3(rotateAround).sub(camera.position).len() > 0) {
            camera.translate(new Vector3(camera.direction).scl(-step));
            overallDistance += step;
        }
        return super.scrolled(amountX, amountY);
    }

    private String getTag() {
        return getClass().getSimpleName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public void update(float delta) { camera.update(); }
}