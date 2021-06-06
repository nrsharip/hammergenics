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


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class Models {
    // see: https://libgdx.badlogicgames.com/ci/nightlies/dist/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html
    // see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
    // see also com.badlogic.gdx.graphics.g3d.utils.shapebuilders
    public static Model createGridModel() {
        ModelBuilder mb;
        MeshPartBuilder mpb;
        int units = 30;

        mb = new ModelBuilder();
        mb.begin();

        mb.node().id = "XZ";
        mpb = mb.part("XZ", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
        for (float pos = -units; pos < units + 1; pos += 1 ) {
            // Requires GL_LINES primitive type.
            mpb.line(-units, 0,     pos, units, 0,    pos); // along X-axis
            mpb.line(    pos, 0, -units,    pos, 0, units); // along Z-axis
        }

        mb.node().id = "Y"; // adding node Y
        mpb = mb.part("Y", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)));
        for (float x = -units; x < units + 1; x += units) {
            for (float z = -units; z < units + 1; z += units) {
                // see implementation: Add a line. Requires GL_LINES primitive type.
                mpb.line(x, -10, z, x, 10, z); // along Y-axis
                // Exception in thread "LWJGL Application" com.badlogic.gdx.utils.GdxRuntimeException: Too many vertices used
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.vertex(MeshBuilder.java:547)
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.vertex(MeshBuilder.java:590)
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.line(MeshBuilder.java:657)
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.line(MeshBuilder.java:667)
                //        at com.hammergenics.screens.ModelPreviewScreen.createGridModel(ModelPreviewScreen.java:605)
            }
        }

        mb.node().id = "origin"; // adding node Y
        mpb = mb.part("origin", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)));
        SphereShapeBuilder.build(mpb, 0.25f, 0.25f, 0.25f, 100, 100);
        return mb.end();
    }

    public static Model createLightsModel() {
        ModelBuilder mb = new ModelBuilder();
        MeshPartBuilder mpb;

        mb.begin();

        mb.node().id = "directional";
        mpb = mb.part("directional", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material("base"));
        ArrowShapeBuilder.build(mpb, 0, 0, 0, 1, 0, 0, 0.2f, 0.5f, 100); // unit arrow

        mb.node().id = "point";
        mpb = mb.part("point", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material("base"));
        SphereShapeBuilder.build(mpb, 1, 1, 1, 100, 100); // unit sphere

        return mb.end();
    }
}