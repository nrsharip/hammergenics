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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;

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

    public void box(Matrix4 transform, float scale, Color clr) {
        Array<Vector3> corners = new Array<>(Vector3.class);
        // min-max form a box with side of 1 * scale and a center at (0, 0, 0)
        // if scale == 1 then this will create a unit box.
        Vector3 min = new Vector3(-0.5f * scale, -0.5f * scale, -0.5f * scale);
        Vector3 max = new Vector3( 0.5f * scale,  0.5f * scale,  0.5f * scale);
        Vector3 tmp1 = Vector3.Zero.cpy();
        Vector3 tmp2 = Vector3.Zero.cpy();
        for (int i = 0; i < 8; i++) {
            // 000 - (min.x, min.y, min.z)
            // 001 - (min.x, min.y, max.z)
            // 010 - (min.x, max.y, min.z)
            // 011 - (min.x, max.y, max.z)
            // 100 - (max.x, min.y, min.z)
            // ...
            if ((i & (1 << 2)) == 0) { tmp1.x = min.x; } else { tmp1.x = max.x; }
            if ((i & (1 << 1)) == 0) { tmp1.y = min.y; } else { tmp1.y = max.y; }
            if ((i & (1 << 0)) == 0) { tmp1.z = min.z; } else { tmp1.z = max.z; }
            tmp1.mul(transform);
            corners.add(tmp1.cpy());
        }
        switch (primitiveType) {
            case GL_LINES:
                ArrayMap<Integer, Array<Integer>> cornerMap = new ArrayMap<>(Integer.class, Array.class);
                // need to connect 4 corners with 3 adjacent corners to form a box
                cornerMap.put(0b000, new Array<>(new Integer[]{0b001, 0b010, 0b100}));
                cornerMap.put(0b011, new Array<>(new Integer[]{0b001, 0b010, 0b111}));
                cornerMap.put(0b101, new Array<>(new Integer[]{0b001, 0b100, 0b111}));
                cornerMap.put(0b110, new Array<>(new Integer[]{0b010, 0b100, 0b111}));

                for (ObjectMap.Entry<Integer, Array<Integer>> entry:cornerMap) {
                    tmp1 = corners.get(entry.key);
                    for (Integer adjCornerIndex:entry.value) {
                        tmp2 = corners.get(adjCornerIndex);
                        line(tmp1, tmp2, clr, clr);
                    }
                }
                break;
            default:
                Gdx.app.error(getClass().getSimpleName(), "box: UNSUPPORTED primitive type");
                break;
        }
    }
}
