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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.input.HGInputController;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ScreenInputController extends HGInputController {
    public Camera camera;

    public ScreenInputController(Camera camera) {
        this(camera, null);

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

    protected ScreenInputController(Camera camera, Array<KeyInfo> keys) {
        super(keys);
        this.camera = camera;
    }

    private String getTag() {
        return ScreenInputController.class.getSimpleName() + "."
                + Thread.currentThread().getStackTrace()[2].getMethodName();
    }
}