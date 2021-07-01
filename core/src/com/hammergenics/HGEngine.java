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

package com.hammergenics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.UBJsonWriter;
import com.hammergenics.config.Config;
import com.hammergenics.map.HGGrid;
import com.hammergenics.map.HGGrid.NoiseStageInfo;
import com.hammergenics.map.TerrainPartsEnum;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.screens.graphics.g3d.PhysicalModelInstance;
import com.hammergenics.screens.graphics.g3d.saver.G3dModelSaver;
import com.hammergenics.screens.physics.bullet.collision.HGContactListener;
import com.hammergenics.screens.utils.AttributesMap;
import com.hammergenics.utils.HGUtils;

import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_INN;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_CORN_OUT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_FLAT;
import static com.hammergenics.map.TerrainPartsEnum.TRRN_SIDE;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createGridModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createLightsModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createTestBox;
import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGEngine implements Disposable {
    public ArrayMap<FileHandle, Array<FileHandle>> folder2models;
    public static final FileFilter filterBitmapFonts = file -> file.isDirectory()
            || file.getName().toLowerCase().endsWith(".fnt"); // BitmapFont
    public static final FileFilter filterTextures = file -> file.isDirectory()
            || file.getName().toLowerCase().endsWith(".bmp")  // textures in BMP
            || file.getName().toLowerCase().endsWith(".png")  // textures in PNG
            || file.getName().toLowerCase().endsWith(".tga"); // textures in TGA
    public static final FileFilter filterModels = file -> file.isDirectory()
//          || file.getName().toLowerCase().endsWith(".3ds")  // converted to G3DB with fbx-conv
            || file.getName().toLowerCase().endsWith(".g3db") // binary
            || file.getName().toLowerCase().endsWith(".g3dj") // json
//          || file.getName().toLowerCase().endsWith(".gltf") // see for support: https://github.com/mgsx-dev/gdx-gltf
            || file.getName().toLowerCase().endsWith(".obj"); // wavefront
    public static final FileFilter filterAll = file ->
            filterBitmapFonts.accept(file) | filterTextures.accept(file) | filterModels.accept(file);

    public final HGGame game;
    public final AssetManager assetManager = new AssetManager();
    public boolean assetsLoaded = true;
    public G3dModelSaver g3dSaver = new G3dModelSaver();

    public ArrayMap<FileHandle, HGModel> hgModels = new ArrayMap<>(FileHandle.class, HGModel.class);
    public Array<Texture> textures = new Array<>();
    // Auxiliary models:
    public HGModel gridHgModel = null;
    public HGModel lightsHgModel = null;
    public HGModel boxHgModel = null;
    public HGModelInstance gridXZHgModelInstance = null; // XZ plane: lines (yellow)
    public HGModelInstance gridYHgModelInstance = null;  // Y axis: vertical lines (red)
    public HGModelInstance gridOHgModelInstance = null;  // origin: sphere (red)
    public PhysicalModelInstance groundPhysModelInstance = null;  // origin: sphere (red)
    public Array<HGModelInstance> dlArrayHgModelInstance = new Array<>(ModelInstance.class); // directional lights
    public Array<HGModelInstance> plArrayHgModelInstance = new Array<>(ModelInstance.class); // point lights
    public Array<HGModelInstance> bbArrayHgModelInstance = new Array<>(ModelInstance.class); // bounding boxes
    // the general container for any auxiliary model instances
    public Array<HGModelInstance> auxMIs = new Array<>(HGModelInstance.class);

    // ModelInstance Related:
    public Array<PhysicalModelInstance> physMIs = new Array<>(PhysicalModelInstance.class);
    public float unitSize = 0f;
    public float overallSize = 0f;
    public PhysicalModelInstance currMI = null;
    public Vector2 currCell = Vector2.Zero.cpy();

    // Physics related:
    // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // https://pybullet.org/Bullet/phpBB3/viewtopic.php?t=5449#p19521
    // it’s generally better to use bits which aren’t used for anything else.
    public final static short FLAG_GROUND = 1<<8;
    public final static short FLAG_OBJECT = 1<<9;
    public final static short FLAG_ALL = -1;

    public final ArrayMap<PhysicalModelInstance, btRigidBody> mi2rb = new ArrayMap<>(PhysicalModelInstance.class, btRigidBody.class);
    public final ArrayMap<btRigidBody, PhysicalModelInstance> rb2mi = new ArrayMap<>(btRigidBody.class, PhysicalModelInstance.class);
    public final ArrayMap<Integer, btRigidBody> hc2rb = new ArrayMap<>(Integer.class, btRigidBody.class);
    public final ArrayMap<btRigidBody, Integer> rb2hc = new ArrayMap<>(btRigidBody.class, Integer.class);

    // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Because it’s not possible in Java to use global callback methods, the wrapper adds the ContactListener class
    // to take care of that. This is also the reason that we don’t have to inform bullet to use our ContactListener,
    // the wrapper takes care of that when you construct the ContactListener.
    public ContactListener contactListener;
    public btDynamicsWorld dynamicsWorld;
    // simply said: constraints can be used to attach objects to each other
    public btConstraintSolver constraintSolver;

    // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Ideally we’d first check if the two objects are near each other, for example using a bounding box or bounding sphere.
    // And only if they are near each other, we’d use the more accurate specialized collision algorithm.

    // The first phase, where we find collision objects that are near each other, is called the broad phase.
    // It’s therefore crucial that the broad phase is highly optimized. Bullet does this by caching the collision information,
    // so it doesn’t have to recalculate it every time. There are several implementations you can choose from,
    // but in practice this is done in the form a tree. I’ll not go into detail about this, but if you want to know more
    // about it, you can search for “axis aligned bounding box tree” or in short “AABB tree”.
    public btBroadphaseInterface broadPhase;
    // The second phase, where a more accurate specialized collision algorithm is used, is called the near phase.

    // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Before we can start the actual collision detection we need a few helper classes.
    public btCollisionConfiguration collisionConfig;
    public btDispatcher dispatcher;

    // Map generation related:
    // Integer.MAX_VALUE = 0x7fffffff = 2,147,483,647
    // Integer.MAX_VALUE / 512 = 4,194,303
    public final static int MAP_CENTER = Integer.MAX_VALUE / 512;
    public final static int MAP_SIZE = 64; // amount of cells on one side of the grid

    // taking size + 1 to have the actual [SIZE x SIZE] cells grid
    // which will take [SIZE + 1 x SIZE + 1] vertex grid to define
    public HGGrid gridNoise00 = new HGGrid(MAP_SIZE + 1, MAP_CENTER - MAP_SIZE, MAP_CENTER - MAP_SIZE);
    public HGGrid gridNoise01 = new HGGrid(MAP_SIZE + 1, MAP_CENTER - MAP_SIZE, MAP_CENTER           );
    public HGGrid gridNoise10 = new HGGrid(MAP_SIZE + 1, MAP_CENTER           , MAP_CENTER - MAP_SIZE);
    public HGGrid gridNoise11 = new HGGrid(MAP_SIZE + 1, MAP_CENTER           , MAP_CENTER           );
    public HGGrid gridCellular = new HGGrid(512);
    public HGGrid gridDungeon = new HGGrid(512); // This algorithm likes odd-sized maps, although it works either way.

    Array<NoiseStageInfo> noiseStages = new Array<>(true, 16, NoiseStageInfo.class);
    public float mid;
    public float yScale = 1f;
    public float step = -1f;

    // Terrain:
    public Array<HGModelInstance> terrain = new Array<>(true, 16, HGModelInstance.class);

    // Noise Grid related:
    public HGModel noiseHgModel00 = null;
    public HGModel noiseHgModel01 = null;
    public HGModel noiseHgModel10 = null;
    public HGModel noiseHgModel11 = null;
    public PhysicalModelInstance noisePhysModelInstance00 = null;
    public PhysicalModelInstance noisePhysModelInstance01 = null;
    public PhysicalModelInstance noisePhysModelInstance10 = null;
    public PhysicalModelInstance noisePhysModelInstance11 = null;

    public HGEngine(HGGame game) {
        this.game = game;
        assetManager.getLogger().setLevel(Logger.DEBUG);

        initBullet();

        // see public BitmapFont ()
        // Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png")

        assetManager.load(Config.ASSET_FILE_NAME_FONT, BitmapFont.class, null);
        assetManager.finishLoading();
        // Creating the Aux Models beforehand:
        gridHgModel = new HGModel(createGridModel());
        lightsHgModel = new HGModel(createLightsModel());
        boxHgModel = new HGModel(createTestBox(GL20.GL_TRIANGLES));

        gridXZHgModelInstance = new HGModelInstance(gridHgModel, "XZ");
        gridYHgModelInstance = new HGModelInstance(gridHgModel, "Y");
        gridOHgModelInstance = new HGModelInstance(gridHgModel, "origin");
        groundPhysModelInstance = new PhysicalModelInstance(boxHgModel, 0f, "box");
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        if (gridHgModel != null) { gridHgModel.dispose(); }
        if (lightsHgModel != null) { lightsHgModel.dispose(); }
        for (PhysicalModelInstance mi: physMIs) {
            if (mi.rigidBody != null) { dynamicsWorld.removeRigidBody(mi.rigidBody); }
            mi.dispose();
        }

        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // Every time you construct a bullet class in java, the wrapper will also construct the same class
        // in the native (C++) library. But while in java the garbage collector takes care of memory management and will
        // free an object when you don’t use it anymore, in C++ you’re responsible for freeing the memory yourself.
        // You’re probably already familiar with this cconcept, because the same goes for a texture, model, model batch, shader etc.
        // Because of this, you have to manually dispose the object when you no longer need it.
        dynamicsWorld.dispose();
        constraintSolver.dispose();
        contactListener.dispose();
        broadPhase.dispose();
        collisionConfig.dispose();
        dispatcher.dispose();
    }


    public void initBullet() {
        Bullet.init();

        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);

        // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // For the broad phase I’ve chosen the btDbvtBroadphase implementation,
        // which is a Dynamic Bounding Volume Tree implementation.
        // In most scenario’s this implementation should suffice.
        broadPhase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadPhase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(Vector3.Y.cpy().scl(-10f));
        contactListener = new HGContactListener(this);
    }

    public void generateNoise(float yScale, Array<NoiseStageInfo> stages) {
        if (stages.size == 0) { return; }

        this.yScale = yScale;
        this.step = -1f;

        noiseStages.clear();
        noiseStages.addAll(stages);

        gridNoise00.generateNoise(yScale, stages);
        gridNoise01.generateNoise(yScale, stages);
        gridNoise10.generateNoise(yScale, stages);
        gridNoise11.generateNoise(yScale, stages);

        resetNoiseModelInstance();
    }

    public void roundNoiseToDigits(int digits) {
        gridNoise00.roundToDigits(digits);
        gridNoise01.roundToDigits(digits);
        gridNoise10.roundToDigits(digits);
        gridNoise11.roundToDigits(digits);

        resetNoiseModelInstance();
    }

    public void roundNoiseToStep(float step) {
        this.step = step;
        gridNoise00.roundToStep(step);
        gridNoise01.roundToStep(step);
        gridNoise10.roundToStep(step);
        gridNoise11.roundToStep(step);

        resetNoiseModelInstance();
    }

    public void generateCellular() { gridCellular.generateCellular(); }

    public void generateDungeon() { gridDungeon.generateDungeon(); }

    public void resetNoiseModelInstance() {
        if (noiseHgModel00 != null) { noiseHgModel00.dispose(); }
        if (noiseHgModel01 != null) { noiseHgModel01.dispose(); }
        if (noiseHgModel10 != null) { noiseHgModel10.dispose(); }
        if (noiseHgModel11 != null) { noiseHgModel11.dispose(); }
        if (noisePhysModelInstance00 != null) { noisePhysModelInstance00.dispose(); }
        if (noisePhysModelInstance01 != null) { noisePhysModelInstance01.dispose(); }
        if (noisePhysModelInstance10 != null) { noisePhysModelInstance10.dispose(); }
        if (noisePhysModelInstance11 != null) { noisePhysModelInstance11.dispose(); }
        noiseHgModel00 = new HGModel(createGridModel(gridNoise00));
        noiseHgModel01 = new HGModel(createGridModel(gridNoise01));
        noiseHgModel10 = new HGModel(createGridModel(gridNoise10));
        noiseHgModel11 = new HGModel(createGridModel(gridNoise11));
        noisePhysModelInstance00 = new PhysicalModelInstance(noiseHgModel00, 0f, "grid");
        noisePhysModelInstance01 = new PhysicalModelInstance(noiseHgModel01, 0f, "grid");
        noisePhysModelInstance10 = new PhysicalModelInstance(noiseHgModel10, 0f, "grid");
        noisePhysModelInstance11 = new PhysicalModelInstance(noiseHgModel11, 0f, "grid");

        mid = (float) Arrays.stream(new double[] {
                gridNoise00.mid, gridNoise01.mid, gridNoise10.mid, gridNoise11.mid
        }).average().orElse(0f);

        noisePhysModelInstance00.transform.setToTranslation(
                gridNoise00.x0 - MAP_CENTER, -mid * yScale, gridNoise00.z0 - MAP_CENTER);
        noisePhysModelInstance01.transform.setToTranslation(
                gridNoise01.x0 - MAP_CENTER, -mid * yScale, gridNoise01.z0 - MAP_CENTER);
        noisePhysModelInstance10.transform.setToTranslation(
                gridNoise10.x0 - MAP_CENTER, -mid * yScale, gridNoise10.z0 - MAP_CENTER);
        noisePhysModelInstance11.transform.setToTranslation(
                gridNoise11.x0 - MAP_CENTER, -mid * yScale, gridNoise11.z0 - MAP_CENTER);
    }

    public void applyTerrainParts(final ArrayMap<TerrainPartsEnum, FileHandle> tp2fh) {
        if (step < 0) { return; }
        terrain.clear();

        TerrainPartsEnum.clearAll();
        for (TerrainPartsEnum tp: TerrainPartsEnum.values()) {
            tp.processFileHandle(assetManager, tp2fh.get(tp));
        }

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

        int floatScale = new BigDecimal(Float.toString(gridNoise00.step)).scale();
        for (int x = 1; x < gridNoise00.getWidth(); x++) {
            for (int z = 1; z < gridNoise00.getHeight(); z++) {
                ys.clear();
                ys.addAll(
                        gridNoise00.get(x - 1, z - 1),
                        gridNoise00.get(x - 1,     z),
                        gridNoise00.get(    x, z - 1),
                        gridNoise00.get(    x,     z)
                );

                points.get(0b00).set(x - 1, ys.get(0b00), z - 1);
                points.get(0b01).set(x - 1, ys.get(0b01), z    );
                points.get(0b10).set(x    , ys.get(0b10), z - 1);
                points.get(0b11).set(x    , ys.get(0b11), z    );

                index2plane.get(0b000110).set(points.get(0b00), points.get(0b01), points.get(0b10));

                // making sure the plane's normal points into the Y axis direction
                if (Vector3.Y.dot(index2plane.get(0b000110).normal) < 0) {
                    index2plane.get(0b000110).normal.set(Vector3.Y);
                    index2plane.get(0b000110).d *= -1f;
                }

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
                        translation.sub(0, mid, 0).scl(1f, yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1/2f));
                        translation.add(gridNoise00.x0 - MAP_CENTER, 0, gridNoise00.z0 - MAP_CENTER);
                        tmp.transform.setToTranslation(translation);
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

                        float factor = yScale * gridNoise00.step/tmp.dims.y;

                        rotation.idt();
                        switch (TRRN_SIDE.leadingCornerI2d ^ index) {
                            case 0b01: rotation.setEulerAngles(90, 0, 0); break;
                            case 0b10: rotation.setEulerAngles(-90, 0, 0); break;
                            case 0b11: rotation.setEulerAngles(180, 0, 0); break;
                        }

                        translation.set(points.get(0b11));
                        translation.y = points.get(index).y + gridNoise00.step;
                        translation.sub(0, mid, 0).scl(1f, yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1, factor, 1).scl(1/2f));
                        translation.add(gridNoise00.x0 - MAP_CENTER, 0, gridNoise00.z0 - MAP_CENTER);
                        scaling.set(1, factor, 1f);
                        tmp.transform.setToTranslationAndScaling(translation, scaling);
                        tmp.transform.rotate(rotation);
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

                    PlaneSide side = index2plane.get(ip).testPoint(points.get(ippoint));
                    // now as we found the plane (points triple) parallel to XZ and the protrusive point
                    // we have the following options:
                    // 1. The protrusive point is below P - TRRN_CORN_INN
                    if (TRRN_CORN_INN.ready && side.equals(PlaneSide.Back)) {
                        HGModelInstance tmp = new HGModelInstance(TRRN_CORN_INN.model);

                        float factor = yScale * gridNoise00.step/tmp.dims.y;

                        rotation.idt();
                        switch (TRRN_CORN_INN.leadingCornerI2d ^ ippoint) {
                            case 0b01: rotation.setEulerAngles(-90, 0, 0); break;
                            case 0b10: rotation.setEulerAngles(90, 0, 0); break;
                            case 0b11: rotation.setEulerAngles(180, 0, 0); break;
                        }

                        translation.set(points.get(0b11));
                        translation.y = points.get(ippoint).y + gridNoise00.step;
                        translation.sub(0, mid, 0).scl(1f, yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1, factor, 1).scl(1/2f));
                        translation.add(gridNoise00.x0 - MAP_CENTER, 0, gridNoise00.z0 - MAP_CENTER);
                        scaling.set(1, factor, 1f);
                        tmp.transform.setToTranslationAndScaling(translation, scaling);
                        tmp.transform.rotate(rotation);
                        terrain.add(tmp);
                        continue;
                    }
                    // 2. The protrusive point is above P - TRRN_CORN_OUT
                    if (TRRN_CORN_OUT.ready && side.equals(PlaneSide.Front)) {
                        HGModelInstance tmp = new HGModelInstance(TRRN_CORN_OUT.model);

                        float factor = yScale * gridNoise00.step/tmp.dims.y;

                        rotation.idt();
                        switch (TRRN_CORN_OUT.leadingCornerI2d ^ ippoint) {
                            case 0b01: rotation.setEulerAngles(-90, 0, 0); break;
                            case 0b10: rotation.setEulerAngles(90, 0, 0); break;
                            case 0b11: rotation.setEulerAngles(180, 0, 0); break;
                        }

                        translation.set(points.get(0b11));
                        translation.y = points.get(ippoint).y;
                        translation.sub(0, mid, 0).scl(1f, yScale, 1f);
                        translation.sub(tmp.dims.cpy().scl(1, factor, 1).scl(1/2f));
                        translation.add(gridNoise00.x0 - MAP_CENTER, 0, gridNoise00.z0 - MAP_CENTER);
                        scaling.set(1, factor, 1f);
                        tmp.transform.setToTranslationAndScaling(translation, scaling);
                        tmp.transform.rotate(rotation);
                        terrain.add(tmp);
                        continue;
                    }
                }
            }
        }
    }

    public void clearTerrain() { terrain.clear(); }

    public void queueAssets(FileHandle rootFileHandle) {
        assetsLoaded = false;
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#adding-assets-to-the-queue
        // https://github.com/libgdx/fbx-conv
        // fbx-conv.exe -f -v .fbx .g3db

        // Loading Process (where Model.class instance is created):
        //   I. AssetManager.load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter)
        //      1. AssetLoader loader = getLoader(type, fileName);                           // used just for check if (loader == null) here
        //         a. ObjectMap<String, AssetLoader> loaders = this.loaders.get(type);       // this.loaders - ObjectMap<Class, ObjectMap<String, AssetLoader>>
        //                                                                                   //                map:asset type -> (map:<extension> -> AssetLoader)
        //                                                                                   //                AssetManager.setLoader(...)
        //                                                                                   //                   <extension>: loaders.put(suffix == null ? "" : suffix, loader);
        //         b. for (Entry<String, AssetLoader> entry : loaders.entries()) {
        //                if (entry.key.length() > length && fileName.endsWith(entry.key)) { // !!! fileName.endsWith(entry.key) entry.key = <extension> - might be ""
        //                    result = entry.value;
        //                    length = entry.key.length();
        //                }
        //            }
        //      2. AssetDescriptor assetDesc = new AssetDescriptor(fileName, type, parameter);
        //      3. loadQueue.add(assetDesc);
        //
        //  ATTENTION: 'gdx-1.10.0.jar' and 'gdx-backend-gwt-1.10.0.jar' both have (same packaged) AsyncExecutor and AssetLoadingTask inside
        //  II. AssetManager.update()
        //      1. nextTask()
        //         a. AssetDescriptor assetDesc = loadQueue.removeIndex(0)
        //         b. addTask(assetDesc)
        //             i. AssetLoader loader = getLoader(assetDesc.type, assetDesc.fileName)
        //            ii. tasks.add(new AssetLoadingTask(this, assetDesc, loader, executor)) // extends AsyncTask -> call()
        //      2. return updateTask() -> (AssetLoadingTask)task.update()
        //         if (loader instanceof SynchronousAssetLoader)
        //            handleSyncLoader();
        //         else
        //            handleAsyncLoader(); // (AssetLoadingTask)
        //            a. if (!dependenciesLoaded) {
        //                                                         // TODO: revisit this
        //                  if (depsFuture == null)                // AsyncResult<Void> depsFuture
        //                     depsFuture = executor.submit(this); // -> (AssetLoadingTask)task.call()
        //                  else if (depsFuture.isDone()) {        // AsyncResult<Void> depsFuture.isDone() -> Future.isDone() (see java.util.concurrent)
        //                     try { depsFuture.get(); } catch (Exception e) { throw new GdxRuntimeException("Couldn't load dependencies of asset: " + assetDesc.fileName, e); }
        //                     dependenciesLoaded = true;
        //                     if (asyncDone) asset = asyncLoader.loadSync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
        //                  }
        //               } else if (loadFuture == null && !asyncDone)
        //                  loadFuture = executor.submit(this);
        //               else if (asyncDone)
        //                  asset = asyncLoader.loadSync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
        //               else if (loadFuture.isDone()) {
        //                  try { loadFuture.get(); } catch (Exception e) { throw new GdxRuntimeException("Couldn't load asset: " + assetDesc.fileName, e); }
        //                  asset = asyncLoader.loadSync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
        //               }
        //
        // Model.class (g3db example) LOADER: // see AssetManager: setLoader(Model.class, ".g3db", new G3dModelLoader(new UBJsonReader(), resolver))
        // 1. G3dModelLoader.loadAsync() -> ModelLoader.loadAsync (AssetManager manager, String fileName, FileHandle file, P parameters) { /* EMPTY */ }
        // 2. G3dModelLoader.loadSync() -> ModelLoader.loadSync (AssetManager manager, String fileName, FileHandle file, P parameters)
        //    a. !!! final Model result = new Model(data, new TextureProvider.AssetTextureProvider(manager));
        //       (see TextureProvider.java):
        //       - FileTextureProvider.load() : return new Texture(Gdx.files.internal(fileName), useMipMaps)
        //       - AssetTextureProvider.load(): return assetManager.get(fileName, Texture.class)
        //       (see Model.java where the textureProvider being used)
        //       Model convertMaterial (ModelMaterial mtl, TextureProvider textureProvider)
        //
        // Texture.class LOADER: // see AssetManager: setLoader(Texture.class, new TextureLoader(resolver))
        // 1. TextureLoader.loadAsync(): uses TextureLoader.TextureParameter
        //    a. TODO: info.data = TextureData.Factory.loadFromFile(file, format, genMipMaps);
        // 2. TextureLoader.loadSync(): uses TextureLoader.TextureParameter
        //    if (parameter != null) {
        //        texture.setFilter(parameter.minFilter, parameter.magFilter);
        //        texture.setWrap(parameter.wrapU, parameter.wrapV);
        //    }

        Array<FileHandle> fileHandleList = HGUtils.traversFileHandle(rootFileHandle, filterAll); // syncup: asset manager

        // See TextureLoader loadAsync() and loadSync() methods for use of this parameter
        // ATTENTION: 'gdx-1.10.0.jar' and 'gdx-backend-gwt-1.10.0.jar' both have
        //            com.badlogic.gdx.assets.loaders.TextureLoader inside (seemingly with different code)
        final TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
        // textureParameter.format;                                // default = null  (the format of the final Texture. Uses the source images format if null)
        // textureParameter.genMipMaps;                            // default = false (whether to generate mipmaps)
        // textureParameter.texture;                               // default = null  (The texture to put the TextureData in, optional)
        // textureParameter.textureData;                           // default = null  (TextureData for textures created on the fly, optional.
        //                                                         //                 When set, all format and genMipMaps are ignored)
        // Using TextureProvider.FileTextureProvider defaults:
        textureParameter.minFilter = Texture.TextureFilter.Linear; // default = TextureFilter.Nearest
        textureParameter.magFilter = Texture.TextureFilter.Linear; // default = TextureFilter.Nearest
        textureParameter.wrapU = Texture.TextureWrap.Repeat;       // default = TextureWrap.ClampToEdge
        textureParameter.wrapV = Texture.TextureWrap.Repeat;       // default = TextureWrap.ClampToEdge

        fileHandleList.forEach(fileHandle -> {
            switch (fileHandle.extension().toLowerCase()) {
//              case "3ds":  // converted to G3DB with fbx-conv
                case "obj":
//              case "gltf": // see for support: https://github.com/mgsx-dev/gdx-gltf
                case "g3db":
                case "g3dj":
                    assetManager.load(fileHandle.path(), Model.class, null);
                    break;
                case "tga":
                case "png":
                case "bmp":
                    assetManager.load(fileHandle.path(), Texture.class, textureParameter);
                    break;
                case "fnt":
                    assetManager.load(fileHandle.path(), BitmapFont.class, null);
                    break;
                case "XXX": // for testing purposes
                    assetManager.load(fileHandle.path(), ParticleEffect.class, null);
                    break;
                default:
                    Gdx.app.error(getClass().getSimpleName(),
                            "Unexpected file extension: " + fileHandle.extension());
            }
        });
    }

    public void getAssets() {
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#getting-assets
        Array<Model> models = new Array<>(Model.class);
        assetManager.getAll(Model.class, models);
        hgModels = Arrays.stream(models.toArray())
                .map(model -> {
                    String fn = assetManager.getAssetFileName(model);
                    FileHandle fh = assetManager.getFileHandleResolver().resolve(fn);
                    return new HGModel(model, fh); })                                     // Array<Model> -> Array<HGModel>
                .collect(() -> new ArrayMap<>(FileHandle.class, HGModel.class),
                        (accum, model) -> accum.put(model.afh, model), ArrayMap::putAll); // retrieving the ArrayMap<FileHandle, HGModel>
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "models loaded: " + hgModels.size);

        assetManager.getAll(Texture.class, textures);
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "textures loaded: " + textures.size);
    }

    public boolean addModelInstance(FileHandle assetFL) { return addModelInstance(assetFL, null, -1); }

    public boolean addModelInstance(FileHandle assetFL, String nodeId, int nodeIndex) {
        if (!assetManager.contains(assetFL.path())) { return false; }
        HGModel hgModel = new HGModel(assetManager.get(assetFL.path(), Model.class), assetFL);
        return addModelInstance(hgModel, nodeId, nodeIndex);
    }

    public boolean addModelInstance(Model model) {
        return addModelInstance(model, null, -1);
    }

    public boolean addModelInstance(Model model, String nodeId, int nodeIndex) {
        return addModelInstance(new HGModel(model), nodeId, nodeIndex);
    }

    public boolean addModelInstance(HGModel hgModel) { return addModelInstance(hgModel, null, -1); }

    public boolean addModelInstance(HGModel hgModel, String nodeId, int nodeIndex) {
        if (!hgModel.hasMaterials() && !hgModel.hasMeshes() && !hgModel.hasMeshParts()) {
            if (hgModel.hasAnimations()) {
                // we got animations only model
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "animations only model: " + hgModel.afh);
            }
            return false;
        }

        if (nodeId == null) {
            currMI = new PhysicalModelInstance(hgModel, hgModel.afh, 10f);
        } else {
            // TODO: maybe it's good to add a Tree for Node traversal
            // https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Tree.html
//            Array<String> nodeTree = new Array<>();
//            for (Node node:model.nodes) {
//                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                        "node '" + node.id + "': \n" + LibgdxUtils.traverseNode(node, nodeTree, " ").toString("\n"));
//                nodeTree.clear();
//            }

            for (NodePart part:hgModel.obj.nodes.get(nodeIndex).parts) {
                // see model.nodePartBones
                // ModelInstance.copyNodes(model.nodes, rootNodeIds); - fills in the bones...
                if (part.invBoneBindTransforms != null) {
                    // see Node.calculateBoneTransforms. It fails with:
                    // Caused by: java.lang.NullPointerException
                    //        at com.badlogic.gdx.graphics.g3d.model.Node.calculateBoneTransforms(Node.java:94)
                    //        at com.badlogic.gdx.graphics.g3d.ModelInstance.calculateTransforms(ModelInstance.java:406)
                    //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:157)
                    //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
                    // (this line: part.bones[i].set(part.invBoneBindTransforms.keys[i].globalTransform).mul(part.invBoneBindTransforms.values[i]);)
                    //
                    // part.invBoneBindTransforms.keys[i] == null, giving the exception
                    // model.nodes.get(nodeIndex).parts.get(<any>).invBoneBindTransforms actually contains both
                    // the keys(Nodes (head, leg, etc...)) and values (Matrix4's)...
                    // so the keys are getting invalidated (set to null) in ModelInstance.invalidate (Node node)
                    // because the nodes they refer to located in other root nodes (not the selected one)

                    return false;
                }
            }
            Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),"nodeId: " + nodeId + " nodeIndex: " + nodeIndex);
            currMI = new PhysicalModelInstance(hgModel, hgModel.afh, 10f, nodeId);
            // for some reasons getting this exception in case nodeId == null:
            // (should be done like (String[])null maybe...)
            // Exception in thread "LWJGL Application" java.lang.NullPointerException
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.copyNodes(ModelInstance.java:232)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:155)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
        }

        physMIs.add(currMI);
        addRigidBody(currMI, FLAG_OBJECT, FLAG_ALL);

        // ********************
        // **** ANIMATIONS ****
        // ********************
        copyExternalAnimations(hgModel.afh);

        currMI.checkAnimations();

        return true;
    }

    /**
     * @param assetFL
     */
    private void copyExternalAnimations(FileHandle assetFL) {
        if (assetManager == null || currMI == null || assetFL == null) { return; }

        FileHandle animationsFolder = HGUtils.fileOnPath(assetFL, "animations");
        if (animationsFolder != null && animationsFolder.isDirectory()) {
            // final since it goes to lambda closure
            final Array<String> animationsPresent = new Array<>();
            // populating with animations already present
            for (Animation animation : currMI.animations) { animationsPresent.add(animation.id); }

            for (int i = 0; i < hgModels.size; i++) {  // using for loop instead of for-each to avoid nested iterators exception:
                HGModel hgm = hgModels.getValueAt(i);  // GdxRuntimeException: #iterator() cannot be used nested.
                String filename = hgm.afh.path();      // thrown by Array$ArrayIterator...
                if (filename.startsWith(animationsFolder.toString())) {
                    if (hgm.hasMaterials()) {
                        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
                                "WARNING: animation only model has materials (" + hgm.obj.materials.size+ "): " + filename);
                    }
                    if (hgm.hasMeshes()) {
                        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
                                "WARNING: animation only model has meshes (" + hgm.obj.meshes.size+ "): " + filename);
                    }
                    if (hgm.hasMeshParts()) {
                        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
                                "WARNING: animation only model has meshParts (" + hgm.obj.meshParts.size+ "): " + filename);
                    }

                    //modelInstance.copyAnimations(m.animations);
                    hgm.obj.animations.forEach(animation -> {
                        //Gdx.app.debug(Thread.currentThread().getStackTrace()[3].getMethodName(), "animation: " + animation.id);
                        // this is to make sure that we don't add the same animation multiple times from different animation models
                        //if (animationsPresent.contains(animation.id, false)) {
                        //    animation.id = hgm.afh.name() + ":" + animation.id;
                        //}
                        animation.id = hgm.afh.name() + ":" + animation.id;
                        Gdx.app.debug(Thread.currentThread().getStackTrace()[3].getMethodName(), "adding animation: " + animation.id);
                        currMI.copyAnimation(animation);
                        animationsPresent.add(animation.id);
                    });
                }
            }
        }
    }

    public Vector2 arrangeInSpiral(boolean keepOriginalScale) {
        Vector2 cell = Vector2.Zero.cpy();
        unitSize = 0f;
        for(PhysicalModelInstance mi: physMIs) { if (mi.maxD > unitSize) { unitSize = mi.maxD; } }
        for(PhysicalModelInstance mi: physMIs) {
            mi.transform.idt(); // first cancel any previous transform
            float factor = 1f;
            // Scale: if the dimension of the current instance is less than maximum dimension of all instances scale it
            if (!keepOriginalScale && mi.maxD < unitSize) { factor = unitSize/mi.maxD; }

            Vector3 position;
            // Position:
            // NOTE: Assuming that the model is centered to origin (see HGModel.centerToOrigin())
            //       Otherwise additional adjustments need to be done
            // 1. Move the instance to the current base position ([cell.x, 0, cell.y] vector sub scaled center vector)
            // 2. Add half of the scaled height to the current position so bounding box's bottom matches XZ plane
            position = new Vector3(cell.x * 1.1f * unitSize, 0f, cell.y * 1.1f * unitSize)
                    .add(0, factor * mi.getBB().getHeight()/2, 0);
            mi.moveAndScaleTo(position, Vector3.Zero.cpy().add(factor));
            //Gdx.app.debug("spiral", "transform:\n" + mi.transform);
            resetRigidBody(mi, FLAG_OBJECT, FLAG_ALL);

            // spiral loop around (0, 0, 0)
            HGUtils.spiralGetNext(cell);
        }

        overallSize = Math.max(Math.abs(cell.x), Math.abs(cell.y)) * unitSize;
        overallSize = overallSize == 0 ? unitSize : overallSize;
        currCell = cell;

        resetBBModelInstances();

        return cell;
    }

    /**
     * @return
     */
    public void resetGridModelInstances() {
        if (gridHgModel == null || gridXZHgModelInstance == null || gridYHgModelInstance == null
                || gridOHgModelInstance == null) { return; }

        gridXZHgModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        gridYHgModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        gridOHgModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(unitSize/4f));

        float height = unitSize/10f;
        groundPhysModelInstance.transform.setToTranslationAndScaling(
                Vector3.Y.cpy().scl(-height/2f), new Vector3(15*overallSize, height, 15*overallSize));
        resetRigidBody(groundPhysModelInstance, FLAG_GROUND, FLAG_ALL);

        if (noisePhysModelInstance00 != null) {
            noisePhysModelInstance00.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        }
    }

    public void addRigidBody(PhysicalModelInstance mi, int group, int mask) {
        mi2rb.put(mi, mi.rigidBody);
        rb2mi.put(mi.rigidBody, mi);
        hc2rb.put(mi.rbHashCode, mi.rigidBody);
        rb2hc.put(mi.rigidBody, mi.rbHashCode);
        dynamicsWorld.addRigidBody(mi.rigidBody, group, mask);
    }

    public void removeRigidBody(PhysicalModelInstance mi) {
        mi2rb.removeKey(mi);
        rb2mi.removeKey(mi.rigidBody);
        hc2rb.removeKey(mi.rbHashCode);
        rb2hc.removeKey(mi.rigidBody);
        dynamicsWorld.removeRigidBody(mi.rigidBody);
    }

    public void resetRigidBody(PhysicalModelInstance mi, int group, int mask) {
        if (mi.rigidBody != null) { removeRigidBody(mi); }
        mi.createRigidBody();
        addRigidBody(mi, group, mask);
    }


    public void resetBBModelInstances() {
        if (bbArrayHgModelInstance != null) { bbArrayHgModelInstance.clear(); } else { return; }

        for (DebugModelInstance mi: physMIs) {
            if (mi.equals(currMI)) { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.GREEN)); }
            else { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.BLACK)); }
        }
    }

    public void resetLightsModelInstances(Vector3 center, Environment environment) {
        if (dlArrayHgModelInstance != null) { dlArrayHgModelInstance.clear(); } else { return; }
        if (plArrayHgModelInstance != null) { plArrayHgModelInstance.clear(); } else { return; }

        Vector3 envPosition;
        if (physMIs != null && physMIs.size > 0) {
            envPosition = physMIs.get(0).getBB().getCenter(new Vector3());
        } else {
            envPosition = Vector3.Zero.cpy();
        }

        // Environment Lights
        DirectionalLightsAttribute dlAttribute = environment.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
        if (dlAttribute != null) {
            for (DirectionalLight light:dlAttribute.lights) {
                dlArrayHgModelInstance.add(createDLModelInstance(light, envPosition, overallSize));
            }
        }
        PointLightsAttribute plAttribute = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
        if (plAttribute != null) {
            for (PointLight light:plAttribute.lights) {
                plArrayHgModelInstance.add(createPLModelInstance(light, envPosition, overallSize));
            }
        }

        // Current Model Instance's Material Lights
        if (currMI != null) {
            for (Material material:currMI.materials) {
                dlAttribute = material.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
                if (dlAttribute != null) {
                    dlAttribute.lights.forEach(light -> dlArrayHgModelInstance.add(createDLModelInstance(light, center, unitSize)));
                }
                plAttribute = material.get(PointLightsAttribute.class, PointLightsAttribute.Type);
                if (plAttribute != null) {
                    plAttribute.lights.forEach(light -> plArrayHgModelInstance.add(createPLModelInstance(light, center, unitSize)));
                }
            }
        }
    }

    private HGModelInstance createDLModelInstance(DirectionalLight dl, Vector3 passThrough, float distance) {
        HGModelInstance mi = new HGModelInstance(lightsHgModel, "directional");
        // from the center moving backwards to the direction of light
        mi.transform.setToTranslationAndScaling(
                passThrough.cpy().sub(dl.direction.cpy().nor().scl(distance)), Vector3.Zero.cpy().add(distance/10));
        // rotating the arrow from X vector (1,0,0) to the direction vector
        mi.transform.rotate(Vector3.X, dl.direction.cpy().nor());
        mi.getMaterial("base", true).set(
                ColorAttribute.createDiffuse(dl.color), ColorAttribute.createEmissive(dl.color)
        );
        return mi;
    }

    private HGModelInstance createPLModelInstance(PointLight pl, Vector3 directTo, float distance) {
        // This a directional light added to the sphere itself to create
        // a perception of glowing reflecting point lights' intensity
        DirectionalLightsAttribute dlAttribute = new DirectionalLightsAttribute();
        Array<DirectionalLight> dLights = new Array<>(DirectionalLight.class);
        Vector3 dir = pl.position.cpy().sub(directTo).nor();
        float ref = (distance < 50f ? 10.10947f : 151.0947f) * distance - 90f; // TODO: temporal solution, revisit
        ref = ref <= 0 ? 1f : ref;                                             // TODO: temporal solution, revisit
        float fraction = pl.intensity / (2 * ref); // syncup: pl
        dLights.addAll(new DirectionalLight().set(new Color(Color.BLACK).add(fraction, fraction, fraction, 0f), dir));
        dlAttribute.lights.addAll(dLights);
        // directional light part over

        HGModelInstance mi = new HGModelInstance(lightsHgModel, "point");
        mi.transform.setToTranslationAndScaling(pl.position, Vector3.Zero.cpy().add(distance/10));
        mi.getMaterial("base", true).set(
                dlAttribute, ColorAttribute.createDiffuse(pl.color), ColorAttribute.createEmissive(pl.color)
        );
        return mi;
    }

    /**
     * Checks if the ray collides with any of the model instances' Bounding Boxes.<br>
     * If it does adds such model instance to the out array allocated beforehand.
     * @param ray
     * @param modelInstances
     * @param out
     * @return An array of model instances sorted by the distance from camera position (ascending order)
     */
    public <T extends HGModelInstance> Array<T> rayMICollision(Ray ray, Array<T> modelInstances, final Array<T> out) {
        ArrayMap<BoundingBox, T> bb2mi = new ArrayMap<>();
        Array<BoundingBox> bbs = Arrays.stream(modelInstances.toArray())
                .map(HGModelInstance::getBB)
                .collect(() -> new Array<>(BoundingBox.class), Array::add, Array::addAll);

        for (int i = 0; i < modelInstances.size; i++) { bb2mi.put(bbs.get(i), modelInstances.get(i)); }

        Array<BoundingBox> outBB = rayBBCollision(ray, bbs, new Array<>(true, 16, BoundingBox.class));

        Arrays.stream(outBB.toArray()).map(bb2mi::get).collect(() -> out, Array::add, Array::addAll);

        return out;
    }

    public Array<BoundingBox> rayBBCollision(Ray ray, Array<BoundingBox> bbs, Array<BoundingBox> out) {
        // TODO: revisit this later when the Bullet Collision Physics is added

        for (BoundingBox bb:bbs) {
            if (Intersector.intersectRayBoundsFast(ray, bb)) { out.add(bb); }
        }
        Sort.instance().sort(out, (bb1, bb2) -> {
            float len1 = ray.origin.cpy().sub(bb1.getCenter(new Vector3())).len();
            float len2 = ray.origin.cpy().sub(bb2.getCenter(new Vector3())).len();

            return Float.compare(len1, len2);
        });
        return out;
    }

    public void saveAttributes(HGModelInstance hgmi, final AttributesMap storage) {
        if (hgmi != null) {
            hgmi.materials.forEach(attributes -> {
                Array<Attribute> out = new Array<>(Attribute.class);
                attributes.get(out, attributes.getMask());
                storage.put(attributes, out);
            });
        }
    }

    public void restoreAttributes(HGModelInstance hgmi, final AttributesMap storage) {
        if (hgmi != null && storage != null) {
            hgmi.materials.forEach(attributes -> {
                attributes.clear();
                attributes.set(storage.get(attributes));
            });
        }
    }

    public void saveHgModelInstance(HGModelInstance mi) {
        if (mi == null) { return; }

        String filename = "test";
        if (mi.afh != null) { filename = mi.afh.name().replace("." + mi.afh.extension(), ""); }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        FileHandle g3dj = Gdx.files.local("root/test/" + filename + "." + fmt.format(now) + ".g3dj");
        FileHandle g3db = Gdx.files.local("root/test/" + filename + "." + fmt.format(now) + ".g3db");

        g3dSaver.saveG3dj(g3dj, mi);
        JsonValue jv = new JsonReader().parse(g3dj);
        try {
            new UBJsonWriter(g3db.write(false, 8192)).value(jv).close();
        } catch (IOException e) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR writing to file: " + e.getMessage());
        }
    }

    public void removeDbgModelInstance(PhysicalModelInstance mi) {
        if (mi == null) { return; }
        if (mi.rigidBody != null) { dynamicsWorld.removeRigidBody(mi.rigidBody); }
        physMIs.removeValue(mi, true);
        mi.dispose();
    }

    public void clearModelInstances() {
        physMIs.forEach(mi -> {
            if (mi.rigidBody != null) { dynamicsWorld.removeRigidBody(mi.rigidBody); }
            mi.dispose();
        });
        physMIs.clear();
        // no need to dispose - will be done in HGModelInstance on dispose()
        //auxMIs.forEach(HGModelInstance::dispose);
        auxMIs.clear();
        currMI = null;
    }
}