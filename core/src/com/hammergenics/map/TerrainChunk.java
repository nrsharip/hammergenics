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

package com.hammergenics.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Sort;
import com.hammergenics.core.graphics.g3d.HGModel;
import com.hammergenics.core.graphics.g3d.HGModelInstance;
import com.hammergenics.core.graphics.g3d.PhysicalModelInstance;
import com.hammergenics.core.graphics.g3d.PhysicalModelInstance.ShapesEnum;

import java.math.BigDecimal;
import java.util.Comparator;

import static com.hammergenics.HGEngine.gridHgModel;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_INN;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_OUT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_FLAT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE;
import static com.hammergenics.core.graphics.g3d.utils.Models.createGridModel;
import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TerrainChunk {
    public HGGrid gridNoise;
    public HGModel noiseLinesHgModel = null;
    public HGModel noiseTrianglesHgModel = null;
    public HGModelInstance noiseLinesHGModelInstance = null;
    public PhysicalModelInstance noiseTrianglesPhysModelInstance = null;
    public HGModelInstance yLinesHGModelInstance;
    public Array<HGModelInstance> terrain = new Array<>(true, 16, HGModelInstance.class);

    public TerrainChunk(int size, int x0, int z0) {
        gridNoise = new HGGrid(size, x0, z0);
        gridNoise.fill(0f);

        yLinesHGModelInstance = new HGModelInstance(gridHgModel, "Y");
        resetNoiseModelInstances(1f);
    }

    public HGGrid getGridNoise() { return gridNoise; }

    public void generateNoise(float yScale, Array<HGGrid.NoiseStageInfo> stages) {
        clearTerrain();
        gridNoise.generateNoise(yScale, stages);
    }

    public void roundNoiseToStep(float step) {
        clearTerrain();
        gridNoise.roundToStep(step);
    }

    public void discardNoise() {
        clearTerrain();
        gridNoise.discard();
    }

    public void resetNoiseModelInstances(float scale) {
        // models
        if (noiseLinesHgModel != null) { noiseLinesHgModel.dispose(); }
        if (noiseTrianglesHgModel != null) { noiseTrianglesHgModel.dispose(); }
        // instances
        if (noiseLinesHGModelInstance != null) { noiseLinesHGModelInstance.dispose(); }
        if (noiseTrianglesPhysModelInstance != null) { noiseTrianglesPhysModelInstance.dispose(); }

        noiseLinesHgModel = new HGModel(createGridModel(gridNoise, GL20.GL_LINES, scale, Color.YELLOW));
        noiseTrianglesHgModel = new HGModel(createGridModel(gridNoise, GL20.GL_TRIANGLES, scale, Color.GRAY));
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
        noiseLinesHGModelInstance = new HGModelInstance(noiseLinesHgModel, "grid");
        noiseLinesHGModelInstance.setToTranslationAndScaling(
                gridNoise.getX0() * scale, 0, gridNoise.getZ0() * scale,
                scale, gridNoise.yScale * scale, scale
        );

        noiseTrianglesPhysModelInstance = new PhysicalModelInstance(
                noiseTrianglesHgModel, 0f, ShapesEnum.MESH, "grid");
        noiseTrianglesPhysModelInstance.setToTranslationAndScaling(
                gridNoise.getX0() * scale, 0, gridNoise.getZ0() * scale,
                1f, 1f, 1f // scaling is handled in createGridModel for GL_TRIANGLES
                           // since it is meant for rigid body which has no notion of scaling
        );

        float halfCellsWidth = (gridNoise.getWidth(true))/2f;
        float halfCellsHeight = (gridNoise.getHeight(true))/2f;

        yLinesHGModelInstance.setToTranslationAndScaling(
                (gridNoise.getX0() + halfCellsWidth) * scale,
                0,
                (gridNoise.getZ0() + halfCellsHeight) * scale,
                scale, scale, scale
        );
    }

    public void applyTerrainParts(float scale) {
        if (gridNoise.step < 0) { return; }
        terrain.clear();

        Array<Float> ys = new Array<>(true, 16, Float.class);
        Array<Vector3> points = new Array<>(new Vector3[]{ new Vector3(), new Vector3(), new Vector3(), new Vector3() });

        Vector3 translation = new Vector3();
        Quaternion rotation = new Quaternion();
        Vector3 scaling = new Vector3();

        ArrayMap<Integer, Plane> index2plane = new ArrayMap<>(Integer.class, Plane.class);

        index2plane.put(0b000110, new Plane());
        index2plane.put(0b000111, new Plane());
        index2plane.put(0b001011, new Plane());
        index2plane.put(0b011011, new Plane());

        int floatScale = new BigDecimal(Float.toString(gridNoise.step)).scale();
        for (int x = 1; x < gridNoise.getWidth(); x++) {
            for (int z = 1; z < gridNoise.getHeight(); z++) {
                ys.clear();
                ys.addAll(
                        gridNoise.get(x - 1, z - 1),
                        gridNoise.get(x - 1,     z),
                        gridNoise.get(    x, z - 1),
                        gridNoise.get(    x,     z)
                );

                points.get(0b00).set(x - 1, ys.get(0b00), z - 1);
                points.get(0b01).set(x - 1, ys.get(0b01), z    );
                points.get(0b10).set(x    , ys.get(0b10), z - 1);
                points.get(0b11).set(x    , ys.get(0b11), z    );

                index2plane.get(0b000110).set(points.get(0b00), points.get(0b01), points.get(0b10));

                // Getting issues with precision thus setting it here explicitly
                float dot = index2plane.get(0b000110).normal.dot(points.get(0b11));
                dot = BigDecimal.valueOf(dot).setScale(floatScale, ROUND_HALF_UP).floatValue();
                float d = index2plane.get(0b000110).d;
                d = BigDecimal.valueOf(d).setScale(floatScale, ROUND_HALF_UP).floatValue();

                // check if all 4 points are on the same plane
                if (dot + d == 0) {
                    // all 4 points are on the same plane, possible options:
                    // 1. plane is parallel to XZ - we're dealing with the flat surface - TRRN_FLAT
                    if (TRRN_FLAT.ready && index2plane.get(0b000110).normal.isOnLine(Vector3.Y)) {
                        HGModelInstance tmp = new HGModelInstance(TRRN_FLAT.model);

                        translation.set(points.get(0b11));
                        translation.scl(1f, gridNoise.yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1/2f));
                        translation.add(gridNoise.getX0(), 0, gridNoise.getZ0());
                        scaling.set(scale, scale, scale);
                        tmp.setToTranslationAndScaling(translation.scl(scaling), scaling);
                        terrain.add(tmp);
                        continue;
                    } else if (index2plane.get(0b000110).normal.isOnLine(Vector3.Y)) {
                        continue; // to make sure that no other parts are applied for flat
                    }

                    Vector3 line1 = points.get(0b01).cpy().sub(points.get(0b00));
                    Vector3 line2 = points.get(0b10).cpy().sub(points.get(0b00));

                    // 2. plane has a tilt. The points form a rectangle - TRRN_SIDE
                    // It is sufficient to check if either of two adjacent sides of the polygon
                    // is collinear with X or Z unit vectors. If it is the polygon is a rectangle
                    if (TRRN_SIDE.ready && (
                            line1.isOnLine(Vector3.X) || line1.isOnLine(Vector3.Z) ||
                                    line2.isOnLine(Vector3.X) || line2.isOnLine(Vector3.Z))) {

                        Array<Float> sorted = new Array<>(ys);
                        Sort.instance().sort(sorted, Comparator.comparingDouble(Float::doubleValue));

                        int index = ys.indexOf(sorted.get(0), false);
                        // ATTENTION: on indexOf(...) use, since the y value most likely will match precisely
                        //            It is safer to check the next adjacent corner if it has the second greatest distance
                        //            If not - we already got the corner of maximum index
                        if (ys.get(HGModelInstance.getNext2dIndex(index)).equals(sorted.get(1))) {
                            index = HGModelInstance.getNext2dIndex(index);
                        }

                        HGModelInstance tmp = new HGModelInstance(TRRN_SIDE.model);

                        float factor = gridNoise.yScale * gridNoise.step/tmp.dims.y;

                        rotation.idt();
                        switch (TRRN_SIDE.leadingCornerI2d ^ index) {
                            case 0b01: rotation.setEulerAngles(90, 0, 0); break;
                            case 0b10: rotation.setEulerAngles(-90, 0, 0); break;
                            case 0b11: rotation.setEulerAngles(180, 0, 0); break;
                        }

                        translation.set(points.get(0b11));
                        translation.y = points.get(index).y + gridNoise.step;
                        translation.scl(1f, gridNoise.yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1, factor, 1).scl(1/2f));
                        translation.add(gridNoise.getX0(), 0, gridNoise.getZ0());
                        scaling.set(scale, factor * scale, scale);
                        tmp.setToTranslationAndScaling(translation.scl(scale), scaling);
                        tmp.rotate(rotation);
                        terrain.add(tmp);
                        continue;
                    }
                    // 3. plane has a tilt. The points form a rhombus - ?
                } else {
                    // 4 points form a triangle pyramid. One point triple should define a plane
                    // which should be parallel to XZ plane (P). Let's find P.
                    index2plane.get(0b000111).set(points.get(0b00), points.get(0b01), points.get(0b11));
                    index2plane.get(0b001011).set(points.get(0b00), points.get(0b10), points.get(0b11));
                    index2plane.get(0b011011).set(points.get(0b01), points.get(0b10), points.get(0b11));

                    // Index of P
                    int ip = -1;
                    for (ObjectMap.Entry<Integer, Plane> entry: index2plane) {
                        // checking each plane if it's normal is collinear with the Y unit vector
                        // meaning the plane is parallel to XZ. Expecting to have only one such
                        // plane so on positive match - break the loop
                        if (entry.value.normal.isOnLine(Vector3.Y)) { ip = entry.key; break; }
                    }
                    if (ip < 0) { continue; }

                    // Index of Protrusive Point
                    int ippoint = ((ip & 0b110000) >> 4) ^ ((ip & 0b1100) >> 2) ^ (ip & 0b11);

                    // making sure the P plane's normal points into the Y axis direction
                    if (Vector3.Y.dot(index2plane.get(ip).normal) < 0) {
                        index2plane.get(ip).normal.set(Vector3.Y);
                        index2plane.get(ip).d *= -1f;
                    }

                    Plane.PlaneSide side = index2plane.get(ip).testPoint(points.get(ippoint));
                    // now as we found the plane (points triple) parallel to XZ and the protrusive point
                    // we have the following options:
                    // 1. The protrusive point is below P - TRRN_CORN_INN
                    if (TRRN_CORN_INN.ready && side.equals(Plane.PlaneSide.Back)) {
                        HGModelInstance tmp = new HGModelInstance(TRRN_CORN_INN.model);

                        float factor = gridNoise.yScale * gridNoise.step/tmp.dims.y;

                        rotation.idt();
                        switch (TRRN_CORN_INN.leadingCornerI2d ^ ippoint) {
                            case 0b01: rotation.setEulerAngles(-90, 0, 0); break;
                            case 0b10: rotation.setEulerAngles(90, 0, 0); break;
                            case 0b11: rotation.setEulerAngles(180, 0, 0); break;
                        }

                        translation.set(points.get(0b11));
                        translation.y = points.get(ippoint).y + gridNoise.step;
                        translation.scl(1f, gridNoise.yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1, factor, 1).scl(1/2f));
                        translation.add(gridNoise.getX0(), 0, gridNoise.getZ0());
                        scaling.set(scale, factor * scale, scale);
                        tmp.setToTranslationAndScaling(translation.scl(scale), scaling);
                        tmp.rotate(rotation);
                        terrain.add(tmp);
                        continue;
                    }
                    // 2. The protrusive point is above P - TRRN_CORN_OUT
                    if (TRRN_CORN_OUT.ready && side.equals(Plane.PlaneSide.Front)) {
                        HGModelInstance tmp = new HGModelInstance(TRRN_CORN_OUT.model);

                        float factor = gridNoise.yScale * gridNoise.step/tmp.dims.y;

                        rotation.idt();
                        switch (TRRN_CORN_OUT.leadingCornerI2d ^ ippoint) {
                            case 0b01: rotation.setEulerAngles(-90, 0, 0); break;
                            case 0b10: rotation.setEulerAngles(90, 0, 0); break;
                            case 0b11: rotation.setEulerAngles(180, 0, 0); break;
                        }

                        translation.set(points.get(0b11));
                        translation.y = points.get(ippoint).y;
                        translation.scl(1f, gridNoise.yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1, factor, 1).scl(1/2f));
                        translation.add(gridNoise.getX0(), 0, gridNoise.getZ0());
                        scaling.set(scale, factor * scale, scale);
                        tmp.setToTranslationAndScaling(translation.scl(scale), scaling);
                        tmp.rotate(rotation);
                        terrain.add(tmp);
                        continue;
                    }
                }
            }
        }
    }

    public void clearTerrain() { terrain.clear(); }

    public void trn(float x, float y, float z) {
        trnNoiseLinesHgModelInstance(x, y, z);
        trnNoiseTrianglesPhysModelInstance(x, y, z);
        trnTerrain(x, y, z);
    }

    public void trnNoiseLinesHgModelInstance(float x, float y, float z) {
        if (noiseLinesHGModelInstance == null) { return; }

        noiseLinesHGModelInstance.trn(x, y, z);
    }

    public void trnNoiseTrianglesPhysModelInstance(float x, float y, float z) {
        if (noiseTrianglesPhysModelInstance == null) { return; }

        noiseTrianglesPhysModelInstance.trn(x, y, z);
    }

    public void trnTerrain(float x, float y, float z) {
        if (terrain.size == 0) { return; }

        for (HGModelInstance mi: terrain) { mi.trn(x, y, z); }
    }
}
