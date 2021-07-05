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

import com.badlogic.gdx.Application;
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
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolverType;
import com.badlogic.gdx.physics.bullet.dynamics.btDantzigSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btLemkeSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btMLCPSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btMLCPSolverInterface;
import com.badlogic.gdx.physics.bullet.dynamics.btMultiBodyConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btNNCGConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btSolveProjectedGaussSeidel;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.UBJsonWriter;
import com.hammergenics.config.Config;
import com.hammergenics.map.HGGrid;
import com.hammergenics.map.HGGrid.NoiseStageInfo;
import com.hammergenics.map.TerrainChunk;
import com.hammergenics.map.TerrainPartsEnum;
import com.hammergenics.screens.graphics.g3d.EditableModelInstance;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.screens.graphics.g3d.PhysicalModelInstance;
import com.hammergenics.screens.graphics.g3d.PhysicalModelInstance.ShapesEnum;
import com.hammergenics.screens.graphics.g3d.saver.G3dModelSaver;
import com.hammergenics.screens.physics.bullet.collision.HGContactListener;
import com.hammergenics.screens.utils.AttributesMap;
import com.hammergenics.utils.HGUtils;

import java.io.FileFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.hammergenics.HGEngine.btConstraintSolversEnum.BT_MLCP_SOLVER;
import static com.hammergenics.HGEngine.btConstraintSolversEnum.BT_MULTIBODY_SOLVER;
import static com.hammergenics.HGEngine.btConstraintSolversEnum.BT_NNCG_SOLVER;
import static com.hammergenics.HGEngine.btConstraintSolversEnum.BT_SEQUENTIAL_IMPULSE_SOLVER;
import static com.hammergenics.HGEngine.btMLCPSolversEnum.BT_DANTZIG;
import static com.hammergenics.HGEngine.btMLCPSolversEnum.BT_GAUSS_SEIDEL;
import static com.hammergenics.HGEngine.btMLCPSolversEnum.BT_LEMKE;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createGridModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createLightsModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createTestBox;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createTestSphere;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGEngine implements Disposable {
    // Map generation related:
    // Integer.MAX_VALUE = 0x7fffffff = 2,147,483,647
    // Integer.MAX_VALUE / 512 = 4,194,303
    public final static int MAP_CENTER = Integer.MAX_VALUE / 512;
    public final static int MAP_SIZE = 64; // amount of cells on one side of the grid

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

    // Creating the Aux Models beforehand:
    public static final HGModel gridHgModel = new HGModel(createGridModel(MAP_SIZE/2));
    public static final HGModel lightsHgModel = new HGModel(createLightsModel());
    public static final HGModel boxHgModel = new HGModel(createTestBox(GL20.GL_TRIANGLES));
    public static final HGModel sphereHgModel = new HGModel(createTestSphere(GL20.GL_TRIANGLES, 40));

    public HGModelInstance gridOHgModelInstance = null;  // origin: sphere (red)
    public Array<HGModelInstance> dlArrayHgModelInstance = new Array<>(ModelInstance.class); // directional lights
    public Array<HGModelInstance> plArrayHgModelInstance = new Array<>(ModelInstance.class); // point lights
    public Array<HGModelInstance> bbArrayHgModelInstance = new Array<>(ModelInstance.class); // bounding boxes
    // the general container for any auxiliary model instances
    public Array<HGModelInstance> auxMIs = new Array<>(HGModelInstance.class);

    // ModelInstance Related:
    public final Array<EditableModelInstance> editableMIs = new Array<>(EditableModelInstance.class);
    public float unitSize = 0f;
    public float overallSize = 0f;
    public EditableModelInstance currMI = null;
    public final Array<EditableModelInstance> selectedMIs = new Array<>(EditableModelInstance.class);
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

    // CONSTRAINT SOLVERS:
    // https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
    // simply said: constraints can be used to attach objects to each other
    //
    // "Exploring MLCP solvers and Featherstone" by Erwin Coumans:
    // http://goo.gl/84N71q (https://www.gdcvault.com/play/1020076/Physics-for-Game-Programmers-Exploring)
    // "Normal and Friction Stabilization Techniques for Interactive Rigid Body Constraint-based
    // Contact Force Computations" by Morten Silcowitz, Sarah Niebe, Kenny Erleben
    // https://diglib.eg.org/bitstream/handle/10.2312/PE.vriphys.vriphys10.089-095/089-095.pdf
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/ConstraintSolver/btConstraintSolver.h#L32
    // enum btConstraintSolverType
    // {
    //     BT_SEQUENTIAL_IMPULSE_SOLVER = 1,
    //     BT_MLCP_SOLVER = 2,
    //     BT_NNCG_SOLVER = 4,
    //     BT_MULTIBODY_SOLVER = 8,
    //     BT_BLOCK_SOLVER = 16,
    // };
    public enum btConstraintSolversEnum implements Disposable {
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h
        BT_SEQUENTIAL_IMPULSE_SOLVER(btConstraintSolverType.BT_SEQUENTIAL_IMPULSE_SOLVER, "Sequential Impulse", "SI"),
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btMLCPSolver.h
        BT_MLCP_SOLVER(btConstraintSolverType.BT_MLCP_SOLVER, "Mixed Linear Complementarity Problem", "MLCP"),
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/ConstraintSolver/btNNCGConstraintSolver.h
        BT_NNCG_SOLVER(btConstraintSolverType.BT_NNCG_SOLVER, "Nonlinear Nonsmooth Conjugate Gradient", "NNCG"),
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/Featherstone/btMultiBodyConstraintSolver.h
        BT_MULTIBODY_SOLVER(8, "Multi-Body", "MB");
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/Dynamics/btDiscreteDynamicsWorldMt.h#L24
        // TODO: look into multi-threaded solvers' pooling

        public final int type;
        public final String fullName;
        public final String abbreviation;
        public btConstraintSolver instance = null;

        btConstraintSolversEnum(int type, String fullName, String abbreviation) {
            this.type = type;
            this.fullName = fullName;
            this.abbreviation = abbreviation;
        }

        public btConstraintSolver getInstance() { return instance; }
        public void setInstance(btConstraintSolver instance) {
            if (this.instance == null) { this.instance = instance; }
        }

        public static btConstraintSolversEnum findByType(int type) {
            for (btConstraintSolversEnum cs: btConstraintSolversEnum.values()) {
                if (cs.type == type) { return cs; }
            }
            Gdx.app.error("bullet", "ERROR: undefined constraint solver type " + type);
            return null;
        }

        @Override
        public String toString() { return toString(false); }
        public String toString(boolean abbr) { return abbr ? abbreviation : fullName; }

        @Override
        public void dispose() { if (instance != null) { instance.dispose(); } }
    }

    public enum btMLCPSolversEnum implements Disposable {
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btDantzigLCP.cpp
        BT_DANTZIG("Dantzig"),
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btLemkeAlgorithm.h
        BT_LEMKE("Lemke"),
        // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btSolveProjectedGaussSeidel.h#L22
        // This solver is mainly for debug/learning purposes: it is functionally equivalent to the
        // btSequentialImpulseConstraintSolver solver, but much slower (it builds the full LCP matrix)
        BT_GAUSS_SEIDEL("Gauss–Seidel");

        private boolean isApplied = false;
        public final String fullName;
        public btMLCPSolverInterface instance = null;

        btMLCPSolversEnum(String fullName) { this.fullName = fullName; }

        public btMLCPSolverInterface apply() {
            unsetAll();
            isApplied = true;
            return instance;
        }
        public void setInstance(btMLCPSolverInterface instance) {
            if (this.instance == null) { this.instance = instance; }
        }

        @Override public String toString() { return fullName; }
        @Override public void dispose() { if (instance != null) { instance.dispose(); } }

        private static void unsetAll() {
            for (btMLCPSolversEnum solver: btMLCPSolversEnum.values()) { solver.isApplied = false; }
        }

        public static btMLCPSolversEnum current() {
            for (btMLCPSolversEnum solver: btMLCPSolversEnum.values()) {
                if (solver.isApplied) { return solver; }
            }
            return null;
        }
    }

    // BROAD PHASE:
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
    // taking size + 1 to have the actual [SIZE x SIZE] cells grid
    // which will take [SIZE + 1 x SIZE + 1] vertex grid to define
    public final Array<TerrainChunk> chunks;
    public HGGrid gridCellular = new HGGrid(512);
    public HGGrid gridDungeon = new HGGrid(512); // This algorithm likes odd-sized maps, although it works either way.

    Array<NoiseStageInfo> noiseStages = new Array<>(true, 16, NoiseStageInfo.class);
    public float mid;
    public float yScale = 1f;
    public float step = -1f;

    public HGEngine(HGGame game) {
        this.game = game;
        assetManager.getLogger().setLevel(Logger.DEBUG);

        initBullet();

        BT_DANTZIG.setInstance(new btDantzigSolver());
        BT_LEMKE.setInstance(new btLemkeSolver());
        BT_GAUSS_SEIDEL.setInstance(new btSolveProjectedGaussSeidel());

        BT_SEQUENTIAL_IMPULSE_SOLVER.setInstance(new btSequentialImpulseConstraintSolver());
        BT_MLCP_SOLVER.setInstance(new btMLCPSolver(BT_DANTZIG.apply()));
        BT_NNCG_SOLVER.setInstance(new btNNCGConstraintSolver());
        BT_MULTIBODY_SOLVER.setInstance(new btMultiBodyConstraintSolver());

        resetDynamicsWorld(1f);

        // Chunks should be populated after bullet is initialized (TerrainChunk has physical models)
        chunks = new Array<>(new TerrainChunk[]{
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER - MAP_SIZE, MAP_CENTER - MAP_SIZE),
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER - MAP_SIZE, MAP_CENTER           ),
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER           , MAP_CENTER - MAP_SIZE),
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER           , MAP_CENTER           )
        });

        // see public BitmapFont ()
        // Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png")

        assetManager.load(Config.ASSET_FILE_NAME_FONT, BitmapFont.class, null);
        assetManager.finishLoading();

        gridOHgModelInstance = new HGModelInstance(gridHgModel, "origin");
    }

    @Override
    public void dispose() {
        for (PhysicalModelInstance mi: mi2rb.keys()) { removeRigidBody(mi); }

        for (EditableModelInstance mi: editableMIs) { mi.dispose(); }

        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // Every time you construct a bullet class in java, the wrapper will also construct the same class
        // in the native (C++) library. But while in java the garbage collector takes care of memory management and will
        // free an object when you don’t use it anymore, in C++ you’re responsible for freeing the memory yourself.
        // You’re probably already familiar with this cconcept, because the same goes for a texture, model, model batch, shader etc.
        // Because of this, you have to manually dispose the object when you no longer need it.
        dynamicsWorld.dispose();

        BT_SEQUENTIAL_IMPULSE_SOLVER.dispose();
        BT_MLCP_SOLVER.dispose();
        BT_NNCG_SOLVER.dispose();
        BT_MULTIBODY_SOLVER.dispose();

        BT_DANTZIG.dispose();
        BT_LEMKE.dispose();
        BT_GAUSS_SEIDEL.dispose();

        contactListener.dispose();
        broadPhase.dispose();
        collisionConfig.dispose();
        dispatcher.dispose();
        assetManager.dispose();
    }

    // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#loading-the-correct-dll
    // Set this to the path of the lib to use it on desktop instead of the default lib.
    private final static String customDesktopLib =
            "E:\\...\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";
    private final static boolean debugBullet = false;
    public void initBullet() {
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#getting-the-sources
        //   sources: libgdx-024282e47e9b5d8ec25373d3e1e5ddfe55122596.zip:
        //      https://github.com/libgdx/libgdx/releases/tag/gdx-parent-1.10.0
        //      https://github.com/libgdx/libgdx/tree/024282e47e9b5d8ec25373d3e1e5ddfe55122596
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#getting-the-compileride
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#building-the-debug-dll
        //
        //   ISSUE:
        //      1>...\Platforms\Win32\PlatformToolsets\v141\Toolset.targets(34,5):
        //      error MSB8036: The Windows SDK version 8.1 was not found.
        //      Install the required version of Windows SDK or change the SDK version in the project property pages or by right-clicking the solution and selecting "Retarget solution".
        //      SOLUTION: right-click VS solution -> Retarget Projects -> select the SDK
        //   ISSUE:
        //      1>------ Build started: Project: gdxBullet, Configuration: Debug x64 ------
        //      1>softbody_wrap.cpp
        //      1>...\gdx-bullet\jni\swig-src\softbody\softbody_wrap.cpp(179): fatal error C1083: Cannot open include file: 'jni.h': No such file or directory
        //      ...
        //      SOLUTION: right-click VS solution -> Properties -> Configuration: Debug, Platform: All Platforms -> C/C++ -> General -> Additional Include Directories
        //                add the following directory: <path to JDK>/include
        //   ISSUE:
        //      1>------ Build started: Project: gdxBullet, Configuration: Debug Win32 ------
        //      1>softbody_wrap.cpp
        //      1>...\include\jni.h(45): fatal error C1083: Cannot open include file: 'jni_md.h': No such file or directory
        //      ...
        //      SOLUTION: right-click VS solution -> Properties -> Configuration: Debug, Platform: All Platforms -> C/C++ -> General -> Additional Include Directories
        //                add the following directory: <path to JDK>/include/win32

        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#loading-the-correct-dll
        // Need to initialize bullet before using it.
        if (Gdx.app.getType() == Application.ApplicationType.Desktop && debugBullet) {
            System.load(customDesktopLib);
        } else {
            Bullet.init();
        }
        Gdx.app.log("bullet", "version: " + LinearMath.btGetVersion() + " debug: " + debugBullet);
        // Release (gradle: libgdx-1.10.0):
        // [Bullet] Version = 287
        // Debug (https://github.com/libgdx/libgdx/tree/024282e47e9b5d8ec25373d3e1e5ddfe55122596):
        // [Bullet] Version = 287
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#debugging

        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);

        // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // For the broad phase I’ve chosen the btDbvtBroadphase implementation,
        // which is a Dynamic Bounding Volume Tree implementation.
        // In most scenario’s this implementation should suffice.
        broadPhase = new btDbvtBroadphase();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadPhase, null, collisionConfig);
        contactListener = new HGContactListener(this);
    }

    public void generateNoise(float yScale, Array<NoiseStageInfo> stages) {
        if (stages.size == 0) { return; }

        this.yScale = yScale;
        this.step = -1f;

        noiseStages.clear();
        noiseStages.addAll(stages);

        for (TerrainChunk tc: chunks) { tc.generateNoise(yScale, stages); }

        resetChunks(unitSize);
    }

    public void roundNoiseToStep(float step) {
        this.step = step;

        for (TerrainChunk tc: chunks) { tc.roundNoiseToStep(step); }

        resetChunks(unitSize);
    }

    public void generateCellular() { gridCellular.generateCellular(); }

    public void generateDungeon() { gridDungeon.generateDungeon(); }

    public void applyTerrainParts(final ArrayMap<TerrainPartsEnum, FileHandle> tp2fh) {
        TerrainPartsEnum.clearAll();
        for (TerrainPartsEnum tp: TerrainPartsEnum.values()) {
            tp.processFileHandle(assetManager, tp2fh.get(tp));
        }

        resetChunks(unitSize);
    }

    public void clearTerrain() { for (TerrainChunk tc: chunks) { tc.clearTerrain(); } }

    public void resetChunks(float scale) {
        mid = (float) Arrays.stream(chunks.toArray())
                .map(TerrainChunk::getGridNoise)
                .mapToDouble(HGGrid::getMid)
                .average().orElse(0f);

        for (TerrainChunk tc: chunks) {
            removeRigidBody(tc.noiseTrianglesPhysModelInstance);
            tc.resetNoiseModelInstances(scale);

            tc.trnNoiseLinesHgModelInstance(0f, -mid * tc.gridNoise.yScale * scale, 0f);
            tc.trnNoiseTrianglesPhysModelInstance(0f, -mid * tc.gridNoise.yScale * scale, 0f);

            addRigidBody(tc.noiseTrianglesPhysModelInstance, FLAG_GROUND, FLAG_ALL);

            tc.applyTerrainParts(scale);
            tc.trnTerrain(0f, -mid * tc.gridNoise.yScale * scale, 0f);
        }
    }

    public void resetDynamicsWorld(float scale) {
        dynamicsWorld.setGravity(Vector3.Y.cpy().scl(-10f * scale));
        dynamicsWorld.setConstraintSolver(BT_SEQUENTIAL_IMPULSE_SOLVER.getInstance());
    }

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
            currMI = new EditableModelInstance(hgModel, hgModel.afh, 10f, ShapesEnum.BOX);
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
            currMI = new EditableModelInstance(hgModel, hgModel.afh, 10f, ShapesEnum.BOX, nodeId);
            // for some reasons getting this exception in case nodeId == null:
            // (should be done like (String[])null maybe...)
            // Exception in thread "LWJGL Application" java.lang.NullPointerException
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.copyNodes(ModelInstance.java:232)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:155)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
        }

        editableMIs.add(currMI);
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
        for(EditableModelInstance mi: editableMIs) { if (mi.maxD > unitSize) { unitSize = mi.maxD; } }
        for(EditableModelInstance mi: editableMIs) {
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
            mi.setToTranslationAndScaling(position, Vector3.Zero.cpy().add(factor));
            //Gdx.app.debug("spiral", "transform:\n" + mi.transform);

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
        if (gridOHgModelInstance == null) { return; }

        gridOHgModelInstance.setToScaling(Vector3.Zero.cpy().add(unitSize/4f));
    }

    public void addRigidBody(PhysicalModelInstance mi, int group, int mask) {
        if (mi == null || mi.rigidBody == null || btRigidBody.getCPtr(mi.rigidBody) == 0) {
            Gdx.app.error(Thread.currentThread().getStackTrace()[1].getMethodName(),
                    "ERROR: while adding the rigid body to dynamics world");
            return;
        }
        if (!mi2rb.containsKey(mi)) {
            mi2rb.put(mi, mi.rigidBody);
            rb2mi.put(mi.rigidBody, mi);
            hc2rb.put(mi.rbHashCode, mi.rigidBody);
            rb2hc.put(mi.rigidBody, mi.rbHashCode);
            dynamicsWorld.addRigidBody(mi.rigidBody, group, mask);
            //Gdx.app.debug("add rb", (mi.hgModel.afh != null ? mi.hgModel.afh.name() : mi.nodes.get(0).id)
            //        + " size: " + mi2rb.size + " num: " + dynamicsWorld.getNumCollisionObjects()
            //        + " hc: " + mi.rbHashCode + " transform:\n" + mi.rigidBody.getWorldTransform()
            //);
        }
    }

    public void removeRigidBody(PhysicalModelInstance mi) {
        if (mi2rb.containsKey(mi)) {
            mi2rb.removeKey(mi);
            rb2mi.removeKey(mi.rigidBody);
            hc2rb.removeKey(mi.rbHashCode);
            rb2hc.removeKey(mi.rigidBody);
            dynamicsWorld.removeRigidBody(mi.rigidBody);
            //Gdx.app.debug("rem rb", (mi.hgModel.afh != null ? mi.hgModel.afh.name() : mi.nodes.get(0).id)
            //        + " size: " + mi2rb.size + " num: " + dynamicsWorld.getNumCollisionObjects()
            //        + " hc: " + mi.rbHashCode + " transform:\n" + mi.rigidBody.getWorldTransform()
            //);
        }
    }

    public void resetBBModelInstances() {
        if (bbArrayHgModelInstance != null) { bbArrayHgModelInstance.clear(); } else { return; }

        for (EditableModelInstance mi: editableMIs) {
            if (mi.equals(currMI)) { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.GREEN)); }
            else { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.BLACK)); }
        }
    }

    public void resetLightsModelInstances(Vector3 center, Environment environment) {
        if (dlArrayHgModelInstance != null) { dlArrayHgModelInstance.clear(); } else { return; }
        if (plArrayHgModelInstance != null) { plArrayHgModelInstance.clear(); } else { return; }

        Vector3 envPosition;
        if (editableMIs != null && editableMIs.size > 0) {
            envPosition = editableMIs.get(0).getBB().getCenter(new Vector3());
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
        mi.setToTranslationAndScaling(
                passThrough.cpy().sub(dl.direction.cpy().nor().scl(distance)), Vector3.Zero.cpy().add(distance/10));
        // rotating the arrow from X vector (1,0,0) to the direction vector
        mi.rotate(Vector3.X, dl.direction.cpy().nor());
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
        mi.setToTranslationAndScaling(pl.position, Vector3.Zero.cpy().add(distance/10));
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

    public void removeEditableModelInstance(EditableModelInstance mi) {
        if (mi == null) { return; }
        if (mi.rigidBody != null) { removeRigidBody(mi); }
        editableMIs.removeValue(mi, true);
        mi.dispose();
    }

    public void clearModelInstances() {
        editableMIs.forEach(mi -> {
            if (mi.rigidBody != null) { removeRigidBody(mi); }
            mi.dispose();
        });
        editableMIs.clear();
        // no need to dispose - will be done in HGModelInstance on dispose()
        //auxMIs.forEach(HGModelInstance::dispose);
        auxMIs.clear();
        currMI = null;
    }
}