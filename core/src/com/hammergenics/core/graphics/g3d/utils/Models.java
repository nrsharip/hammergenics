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

package com.hammergenics.core.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.hammergenics.map.HGGrid;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class Models {
    // see: https://libgdx.badlogicgames.com/ci/nightlies/dist/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html
    // see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
    // see also com.badlogic.gdx.graphics.g3d.utils.shapebuilders
    public static Model createGridModel(int units) {
        ModelBuilder mb;
        MeshPartBuilder mpb;

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

    public static Model createGridModel(HGGrid grid, int primType, float scale, Color clr) {
        ModelBuilder mb;
        MeshPartBuilder mpb;

        mb = new ModelBuilder();
        mb.begin();

        mb.node().id = "grid";
        mpb = mb.part("grid", primType, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(clr)));

        createGridMeshPart(grid, mpb, scale);

        return mb.end();
    }

    public static void createGridMeshPart(HGGrid grid, MeshPartBuilder mpb, float scale) {
        int width = grid.getWidth();
        int height = grid.getHeight();

        if (mpb.getPrimitiveType() == GL20.GL_LINES) {
            Vector3 p1 = new Vector3();
            Vector3 p2 = new Vector3();
            // drawing lines along X - axis
            for (int z = 0; z < height; z++) {
                for (int x = 0; x < width - 1; x++) {
                    p1.set(x    , grid.get(x    , z), z);
                    p2.set(x + 1, grid.get(x + 1, z), z);
                    //Gdx.app.debug("grid", "" + " x: " + x + " z: " + z + " y1: " + y1 + " y2: " + y2);
                    mpb.line(p1, p2);
                }
            }

            // drawing lines along Z - axis
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < height - 1; z++) {
                    p1.set(x, grid.get(x, z    ), z    );
                    p2.set(x, grid.get(x, z + 1), z + 1);
                    mpb.line(p1, p2);
                }
            }
        } else if (mpb.getPrimitiveType() == GL20.GL_TRIANGLES) {
            //com.badlogic.gdx.utils.GdxRuntimeException: Mesh must be indexed and triangulated
            //        at com.badlogic.gdx.physics.bullet.collision.btIndexedMesh.set(btIndexedMesh.java:165)
            //        at com.badlogic.gdx.physics.bullet.collision.btIndexedMesh.<init>(btIndexedMesh.java:126)
            //        at com.badlogic.gdx.physics.bullet.collision.btIndexedMesh.obtain(btIndexedMesh.java:84)
            //        at com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray.addMeshPart(btTriangleIndexVertexArray.java:136)
            //        at com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray.addMeshParts(btTriangleIndexVertexArray.java:156)
            //        at com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray.<init>(btTriangleIndexVertexArray.java:119)
            //        at com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray.obtain(btTriangleIndexVertexArray.java:103)
            //        at com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape.obtain(btBvhTriangleMeshShape.java:83)
            //btBvhTriangleMeshShape.obtain(hgModel.obj.meshParts);
            Vector3 p00 = new Vector3();
            Vector3 p01 = new Vector3();
            Vector3 p10 = new Vector3();
            Vector3 p11 = new Vector3();
            for (int z = 1; z < height; z++) {
                for (int x = 1; x < width; x++) {
                    p00.set(x - 1, grid.get(x - 1, z - 1), z - 1).scl(scale, grid.yScale * scale, scale);
                    p01.set(x - 1, grid.get(x - 1, z    ), z    ).scl(scale, grid.yScale * scale, scale);
                    p10.set(x    , grid.get(x    , z - 1), z - 1).scl(scale, grid.yScale * scale, scale);
                    p11.set(x    , grid.get(x    , z    ), z    ).scl(scale, grid.yScale * scale, scale);
                    mpb.triangle(p00, p01, p11);
                    mpb.triangle(p00, p11, p10);
                }
            }
        }
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

    public static Model createBoundingBoxModel() {
        // see: ModelBuilder()
        // https://libgdx.badlogicgames.com/ci/nightlies/dist/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html
        ModelBuilder mb = new ModelBuilder();
        MeshPartBuilder mpb;

        mb.begin();

        mb.node().id = "box";
        // MeshPart "box", see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
        mpb = mb.part("box", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material("box"));

        // Requires GL_POINTS, GL_LINES or GL_TRIANGLES
        BoxShapeBuilder.build(mpb, 1f, 1f, 1f); // a unit box

        for (int i = 0; i < 8; i++) { // BB corners
            String id = String.format("corner%3s", Integer.toBinaryString(i)).replace(' ', '0');
            mb.node().id = id;
            // MeshPart "cornerBBB", see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
            mpb = mb.part(id, GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                    new Material(id));

            SphereShapeBuilder.build(mpb, 1, 1, 1, 100, 100); // a unit sphere
        }

        // see also com.badlogic.gdx.graphics.g3d.utils.shapebuilders:
        //  ArrowShapeBuilder
        //  BaseShapeBuilder
        //  BoxShapeBuilder
        //  CapsuleShapeBuilder
        //  ConeShapeBuilder
        //  CylinderShapeBuilder
        //  EllipseShapeBuilder
        //  FrustumShapeBuilder
        //  PatchShapeBuilder
        //  RenderableShapeBuilder
        //  SphereShapeBuilder
        return mb.end();
    }

    public static Model createTestBox(int primType) {
        ModelBuilder mb = new ModelBuilder();
        MeshPartBuilder mpb;

        mb.begin();

        mb.node().id = "box";
        // MeshPart "box", see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
        mpb = mb.part("box", primType, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material("box"));

        // see https://www.khronos.org/opengl/wiki/Vertex_Specification
        // see https://www.khronos.org/opengl/wiki/Vertex_Rendering
        // see https://www.khronos.org/opengl/wiki/Primitive

        // Requires GL_POINTS, GL_LINES or GL_TRIANGLES
        BoxShapeBuilder.build(mpb, 1f, 1f, 1f); // a unit box

        return mb.end();
    }

    public static Model createTestSphere(int primType, int divUV) {
        ModelBuilder mb = new ModelBuilder();
        MeshPartBuilder mpb;

        mb.begin();

        mb.node().id = "sphere";
        // MeshPart "sphere", see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
        mpb = mb.part("sphere", primType, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material("sphere"));

        // see https://www.khronos.org/opengl/wiki/Vertex_Specification
        // see https://www.khronos.org/opengl/wiki/Vertex_Rendering
        // see https://www.khronos.org/opengl/wiki/Primitive

        SphereShapeBuilder.build(mpb, 1, 1, 1, divUV, divUV); // a unit box

        return mb.end();
    }
}