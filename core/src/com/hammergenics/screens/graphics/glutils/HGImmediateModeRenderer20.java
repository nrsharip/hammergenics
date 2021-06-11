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

package com.hammergenics.screens.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGImmediateModeRenderer20 extends ImmediateModeRenderer20 {
    // Primitive Type:
    // GL_POINTS = 0x0000;
    // GL_LINES = 0x0001;
    // GL_LINE_LOOP = 0x0002;
    // GL_LINE_STRIP = 0x0003;
    // GL_TRIANGLES = 0x0004;
    // GL_TRIANGLE_STRIP = 0x0005;
    // GL_TRIANGLE_FAN = 0x0006;
    public int primitiveType = -1;

    public HGImmediateModeRenderer20(boolean hasNormals, boolean hasColors, int numTexCoords) {
        super(hasNormals, hasColors, numTexCoords);
    }

    @Override
    public void begin(Matrix4 projModelView, int primitiveType) {
        this.primitiveType = primitiveType;
        super.begin(projModelView, primitiveType);
    }

    @Override
    public void end() {
        this.primitiveType = -1;
        super.end();
    }

    public void line(Vector3 p1, Vector3 p2, Color c1, Color c2) {
        switch (primitiveType) {
            case GL_LINES:
                color(c1); vertex(p1.x, p1.y, p1.z); color(c2); vertex(p2.x, p2.y, p2.z);
                break;
            default:
                Gdx.app.error(getClass().getSimpleName(), "line: UNSUPPORTED primitive type");
                break;
        }
    }
}
