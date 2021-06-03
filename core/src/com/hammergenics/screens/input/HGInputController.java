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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.IntSet;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGInputController extends GestureDetector {
    protected ArrayMap<Integer, KeyInfo> code2keyInfo;
    protected ArrayMap<Character, KeyInfo> char2keyInfo;

    private IntSet keysPressed = new IntSet();

    public HGInputController(Array<KeyInfo> keys) {
        this(new HGGestureProcessor(), keys);
    }

    public HGInputController(GestureListener listener, Array<KeyInfo> keys) {
        super(listener);
        setKeys(keys);
    }

    public HGInputController(float halfTapSquareSize, float tapCountInterval, float longPressDuration,
                             float maxFlingDelay, GestureListener listener, Array<KeyInfo> keys) {
        super(halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, listener);
        setKeys(keys);
    }

    public HGInputController(float halfTapRectangleWidth, float halfTapRectangleHeight, float tapCountInterval,
                             float longPressDuration, float maxFlingDelay, GestureListener listener, Array<KeyInfo> keys) {
        super(halfTapRectangleWidth, halfTapRectangleHeight, tapCountInterval, longPressDuration, maxFlingDelay, listener);
        setKeys(keys);
    }

    protected static class HGGestureProcessor extends GestureDetector.GestureAdapter {
        protected float touchDownX, touchDownY, tapX, tapY, longPressX, longPressY, flingVelocityX, flingVelocityY;
        protected int touchDownPointer, touchDownButton, tapCount, tapButton, flingButton, panStopPointer, panStopButton;
        protected float panX, panY, panDeltaX, panDeltaY, panStopX, panStopY, zoomInitialDistance, zoomDistance;
        protected Vector2 pinchInitialPointer1, pinchInitialPointer2, pinchPointer1, pinchPointer2;

        protected HGGestureProcessor() {
            touchDownX = touchDownY = tapX = tapY = longPressX = longPressY = flingVelocityX = flingVelocityY = -1f;
            touchDownPointer = touchDownButton = tapCount = tapButton = flingButton = panStopPointer = panStopButton = -1;
            panX = panY = panDeltaX = panDeltaY = panStopX = panStopY = zoomInitialDistance = zoomDistance = -1f;
            pinchInitialPointer1 = Vector2.Zero.cpy();
            pinchInitialPointer2 = Vector2.Zero.cpy();
            pinchPointer1 = Vector2.Zero.cpy();
            pinchPointer2 = Vector2.Zero.cpy();
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f pointer: %5d button: %5d", x, y, pointer, button));
            touchDownX = x; touchDownY = y; touchDownPointer = pointer; touchDownButton = button;
            return super.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f pointer: %5d button: %5d", x, y, count, button));
            tapX = x; tapY = y; tapCount = count; tapButton = button;
            // touchUp fired - invalidating the touchDown values:
            touchDownX = touchDownY = -1f; touchDownPointer = touchDownButton = -1;
            return super.tap(x, y, count, button);
        }

        @Override
        public boolean longPress(float x, float y) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f", x, y));
            longPressX = x; longPressY = y;
            return super.longPress(x, y);
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            Gdx.app.debug(getTag(), String.format("velocityX: %5.3f velocityY: %5.3f button: %5d", velocityX, velocityY, button));
            flingVelocityX = velocityX; flingVelocityY = velocityY; flingButton = button;
            return super.fling(velocityX, velocityY, button);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f dX: %5.3f dY: %5.3f", x, y, deltaX, deltaY));
            panX = x; panY = y; panDeltaX = deltaX; panDeltaY = deltaY;
            return super.pan(x, y, deltaX, deltaY);
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            Gdx.app.debug(getTag(), String.format("x: %5.3f y: %5.3f pointer: %5d button: %5d", x, y, pointer, button));
            panStopX = x; panStopY = y; panStopPointer = pointer; panStopButton = button;
            // touchUp fired - invalidating the touchDown values:
            touchDownX = touchDownY = -1f; touchDownPointer = touchDownButton = -1;
            return super.panStop(x, y, pointer, button);
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            Gdx.app.debug(getTag(), String.format("init: %5.3f dist: %5.3f", initialDistance, distance));
            zoomInitialDistance = initialDistance; zoomDistance = distance;
            return super.zoom(initialDistance, distance);
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            Gdx.app.debug(getTag(), String.format("initialPointer1: %s initialPointer2: %s pointer1: %s pointer2: %s",
                    initialPointer1.toString(), initialPointer2.toString(), pointer1.toString(), pointer2.toString()));
            pinchInitialPointer1.set(initialPointer1); pinchInitialPointer2.set(initialPointer2);
            pinchPointer1.set(pointer1); pinchPointer2.set(pointer2);
            return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }

        @Override
        public void pinchStop() {
            Gdx.app.debug(getTag(), "");
            super.pinchStop();
        }

        protected static String getTag() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            return stackTrace[4].getMethodName() + "->" + stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        Gdx.app.debug(getTag(), String.format("keycode: %5d", keycode));
        keysPressed.add(keycode);
        processKeysDown();
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        Gdx.app.debug(getTag(), String.format("keycode: %5d", keycode));
        keysPressed.remove(keycode);

        if (code2keyInfo == null) { return super.keyUp(keycode); }
        KeyInfo keyInfo = code2keyInfo.get(keycode);
        if (keyInfo != null && keyInfo.keyListener != null) {
            code2keyInfo.get(keycode).keyListener.onKeyUp(keycode);
        }

        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        Gdx.app.debug(getTag(), String.format("character: %c", character));

        if (char2keyInfo == null) { return super.keyTyped(character); }
        KeyInfo keyInfo = char2keyInfo.get(character);
        if (keyInfo != null && keyInfo.keyListener != null) {
            char2keyInfo.get(character).keyListener.onKeyTyped(character);
        }

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

    public static class KeyInfo {
        public int code;
        public char character;
        public String description;
        public KeyListener keyListener;

        public KeyInfo(int code) { this(code, new KeyAdapter()); }

        public KeyInfo(int code, KeyListener keyListener) { this(code, Character.MIN_VALUE, "", keyListener); }

        public KeyInfo(int code, char character, String description, KeyListener keyListener) {
            this.code = code;
            this.character = character;
            this.description = description;
            this.keyListener = keyListener;
        }
    }

    public static class KeyAdapter implements KeyListener{
        @Override
        public void onKeyDown(int keycode) { Gdx.app.debug(getTag(), String.format("keycode: %5d", keycode)); }
        @Override
        public void onKeyUp(int keycode) { Gdx.app.debug(getTag(), String.format("keycode: %5d", keycode)); }
        @Override
        public void onKeyTyped(char character) { Gdx.app.debug(getTag(), String.format("character: %c", character)); }

        private static String getTag() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            return stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
        }
    }

    public interface KeyListener {
        void onKeyDown(int keycode);
        void onKeyUp(int keycode);
        void onKeyTyped(char character);
    }

    private String getTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        return stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
    }

    protected void processKeysDown() {
        if (keysPressed == null || code2keyInfo == null) { return; }
        Gdx.app.debug(getTag(), String.format("keysPressed: %s", keysPressed.toString()));

        IntSet.IntSetIterator iterator = keysPressed.iterator();
        while (iterator.hasNext) {
            int code = iterator.next();
            KeyInfo keyInfo = code2keyInfo.get(code);

            if (keyInfo != null && keyInfo.keyListener != null) {
                code2keyInfo.get(code).keyListener.onKeyDown(code);
            }
        }
    }

    public void setKeys(Array<KeyInfo> keys) {
        if (keys == null) { return; }

        code2keyInfo = new ArrayMap<>(Integer.class, KeyInfo.class);
        char2keyInfo = new ArrayMap<>(Character.class, KeyInfo.class);
        keys.forEach(key -> {
            code2keyInfo.put(key.code, key);
            char2keyInfo.put(key.character, key);
        });
    }
}