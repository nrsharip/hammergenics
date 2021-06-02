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

package com.hammergenics.screens.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGInputController extends GestureDetector {
    private IntSet keysPressed = new IntSet();

    public HGInputController() {
        this(new HGGestureListener());
    }

    public HGInputController(GestureListener listener) {
        super(listener);
    }

    public HGInputController(float halfTapSquareSize, float tapCountInterval, float longPressDuration, float maxFlingDelay, GestureListener listener) {
        super(halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, listener);
    }

    public HGInputController(float halfTapRectangleWidth, float halfTapRectangleHeight, float tapCountInterval, float longPressDuration, float maxFlingDelay, GestureListener listener) {
        super(halfTapRectangleWidth, halfTapRectangleHeight, tapCountInterval, longPressDuration, maxFlingDelay, listener);
    }

    protected static class HGGestureListener extends GestureDetector.GestureAdapter {
        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f pointer: %5d button: %5d", x, y, pointer, button));
            return super.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f pointer: %5d button: %5d", x, y, count, button));
            return super.tap(x, y, count, button);
        }

        @Override
        public boolean longPress(float x, float y) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f", x, y));
            return super.longPress(x, y);
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            Gdx.app.debug(getTag(), String.format("velocityX: %5.3f velocityY: %5.3f button: %5d", velocityX, velocityY, button));
            return super.fling(velocityX, velocityY, button);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f dX: %5.3f dY: %5.3f", x, y, deltaX, deltaY));
            return super.pan(x, y, deltaX, deltaY);
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f pointer: %5d button: %5d", x, y, pointer, button));
            return super.panStop(x, y, pointer, button);
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            Gdx.app.debug(getTag(), String.format("init: %5.3f dist: %5.3f", initialDistance, distance));
            return super.zoom(initialDistance, distance);
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            Gdx.app.debug(getTag(), String.format("initialPointer1: %s initialPointer2: %s pointer1: %s pointer2: %s",
                    initialPointer1.toString(), initialPointer2.toString(), pointer1.toString(), pointer2.toString()));
            return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }

        @Override
        public void pinchStop() {
            Gdx.app.debug(getTag(), "");
            super.pinchStop();
        }

        private static String getTag() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            return stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        Gdx.app.debug(getTag(), String.format("keycode: %5d", keycode));
        keysPressed.add(keycode);
        processKeysPressed();
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        Gdx.app.debug(getTag(), String.format("keycode: %5d", keycode));
        keysPressed.remove(keycode);
        processKeysPressed();
        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        Gdx.app.debug(getTag(), String.format("character: %c", character));
        return super.keyTyped(character);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Gdx.app.debug(getTag(), String.format("screenX: %5d screenY: %5d", screenX, screenY));
        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Gdx.app.debug(getTag(), String.format("screenX: %5.3f screenY: %5.3f", amountX, amountY));
        return super.scrolled(amountX, amountY);
    }

    private String getTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        return stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
    }

    private void processKeysPressed() {
        if (keysPressed == null) { return; }

        Gdx.app.debug(getTag(), String.format("keysPressed: %s", keysPressed.toString()));
    }
}