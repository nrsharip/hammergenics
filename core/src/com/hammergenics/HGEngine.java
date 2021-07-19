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
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
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
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.UBJsonWriter;
import com.hammergenics.core.graphics.HGTexture;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.HGModel;
import com.hammergenics.core.graphics.g3d.HGModelInstance;
import com.hammergenics.core.graphics.g3d.PhysicalModelInstance.ShapesEnum;
import com.hammergenics.core.graphics.g3d.saver.G3dModelSaver;
import com.hammergenics.core.utils.AttributesMap;
import com.hammergenics.map.HGGrid;
import com.hammergenics.map.HGGrid.NoiseStageInfo;
import com.hammergenics.map.TerrainChunk;
import com.hammergenics.map.TerrainPartsEnum;
import com.hammergenics.physics.bullet.dynamics.btDynamicsWorldTypesEnum;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.loaders.shared.SceneAssetLoaderParameters;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.hammergenics.core.graphics.g3d.utils.Models.createGridModel;
import static com.hammergenics.core.graphics.g3d.utils.Models.createLightsModel;
import static com.hammergenics.core.graphics.g3d.utils.Models.createTestBox;
import static com.hammergenics.core.graphics.g3d.utils.Models.createTestSphere;
import static com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum.ALL_FILES;
import static com.hammergenics.physics.bullet.dynamics.btDynamicsWorldTypesEnum.BT_SOFT_RIGID_DYNAMICS_WORLD;
import static com.hammergenics.physics.bullet.dynamics.btDynamicsWorldTypesEnum.FLAG_ALL;
import static com.hammergenics.physics.bullet.dynamics.btDynamicsWorldTypesEnum.FLAG_GROUND;
import static com.hammergenics.physics.bullet.dynamics.btDynamicsWorldTypesEnum.FLAG_OBJECT;

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

    public final HGGame game;
    public final HGAssetManager assetManager = new HGAssetManager();
    public boolean assetsLoaded = true;
    public G3dModelSaver g3dSaver = new G3dModelSaver();

    public final Array<FileHandle> loadQueue = new Array<>(true, 16, FileHandle.class);
    public FileHandle loaded;
    public FileHandle failed;

    public Array<LoadListener> loadListeners = new Array<>(true, 16, LoadListener.class);
    public void addLoadListener(LoadListener listener) { loadListeners.add(listener); }
    public void removeLoadListener(LoadListener listener) { loadListeners.removeValue(listener, false); }
    public void clearLoadListeners() { loadListeners.clear(); }
    public class HGAssetManager extends AssetManager {
        @Override public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
            //Gdx.app.debug("asset manager", "load: " + " fileName: " + fileName + " type: " + type.getSimpleName());
            loadQueue.add(getFileHandleResolver().resolve(fileName));
            super.load(fileName, type, parameter);
            loadListeners.forEach(loadListener -> loadListener.load(fileName, type, parameter));
        }
        @Override public synchronized boolean update() {
            //Gdx.app.debug("asset manager", "update");
            loaded = null;
            failed = null;
            assetsLoaded = super.update();
            loadListeners.forEach(loadListener -> loadListener.update(assetsLoaded));
            return assetsLoaded;
        }
        @Override protected <T> void addAsset(String fileName, Class<T> type, T asset) {
            loaded = getFileHandleResolver().resolve(fileName);
            //Gdx.app.debug("asset manager", "add: "
            //        + " fileName: " + fileName + " loaded: " + loaded + " type: " + type.getSimpleName());
            loadQueue.removeValue(loaded, false);
            super.addAsset(fileName, type, asset);
            HGEngine.this.addAsset(loaded);
            loadListeners.forEach(loadListener -> loadListener.addAsset(fileName, type, asset));
        }
        @Override protected void taskFailed(AssetDescriptor assetDesc, RuntimeException ex) {
            failed = getFileHandleResolver().resolve(assetDesc.fileName);
            //Gdx.app.debug("asset manager", "failed: "
            //        + " fileName: " + assetDesc.fileName + " failed: " + failed);
            loadQueue.removeValue(failed, false);
            super.taskFailed(assetDesc, ex);
            loadListeners.forEach(loadListener -> loadListener.taskFailed(assetDesc, ex));
        }
    }

    public interface LoadListener {
        <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter);
        void update(boolean result);
        <T> void addAsset(String fileName, Class<T> type, T asset);
        void taskFailed(AssetDescriptor assetDesc, RuntimeException ex);

        class LoadAdapter implements LoadListener {
            @Override public <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) { }
            @Override public void update(boolean result) { }
            @Override public <T> void addAsset(String fileName, Class<T> type, T asset) { }
            @Override public void taskFailed(AssetDescriptor assetDesc, RuntimeException ex) { }
        }
    }

    public ArrayMap<FileHandle, HGModel> hgModels = new ArrayMap<>(FileHandle.class, HGModel.class);
    public ArrayMap<FileHandle, HGTexture> hgTextures = new ArrayMap<>(FileHandle.class, HGTexture.class);

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
    public final Array<EditableModelInstance> selectedMIs = new Array<>(EditableModelInstance.class);
    public Vector2 currCell = Vector2.Zero.cpy();

    public EditableModelInstance getCurrMI() {
        if (selectedMIs.size != 1) { return null; }
        return selectedMIs.first();
    }

    public void setCurrMI(EditableModelInstance mi) {
        selectedMIs.clear();
        if (mi != null) { selectedMIs.add(mi); }
    }

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
        assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());

        initBullet();
        btDynamicsWorldTypesEnum.resetAllBtDynamicsWorlds(1f);
        btDynamicsWorldTypesEnum.setSelected(BT_SOFT_RIGID_DYNAMICS_WORLD);

        // Chunks should be populated after bullet is initialized (TerrainChunk has physical models)
        chunks = new Array<>(new TerrainChunk[]{
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER - MAP_SIZE, MAP_CENTER - MAP_SIZE),
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER - MAP_SIZE, MAP_CENTER           ),
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER           , MAP_CENTER - MAP_SIZE),
                new TerrainChunk(MAP_SIZE + 1, MAP_CENTER           , MAP_CENTER           )
        });

        // see public BitmapFont ()
        // Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png")

        gridOHgModelInstance = new HGModelInstance(gridHgModel, "origin");
    }

    @Override
    public void dispose() {
        for (EditableModelInstance mi: editableMIs) { mi.dispose(); }

        btDynamicsWorldTypesEnum.disposeAll();
        assetManager.dispose();
    }

    // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#loading-the-correct-dll
    // Set this to the path of the lib to use it on desktop instead of the default lib.
    private final static String customDesktopLib = "E:\\...\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";
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
        // Bullet Github: https://github.com/bulletphysics/bullet3/blob/master/src/LinearMath/btScalar.h#L28
        // #define BT_BULLET_VERSION 317
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#debugging

        //contactListener = new HGContactListener(this);
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
            tc.resetNoiseModelInstances(scale);

            tc.trnNoiseLinesHgModelInstance(0f, -mid * tc.gridNoise.yScale * scale, 0f);
            tc.trnNoiseTrianglesPhysModelInstance(0f, -mid * tc.gridNoise.yScale * scale, 0f);

            tc.noiseTrianglesPhysModelInstance.addRigidBodyToDynamicsWorld(FLAG_GROUND, FLAG_ALL);

            tc.applyTerrainParts(scale);
            tc.trnTerrain(0f, -mid * tc.gridNoise.yScale * scale, 0f);
        }
    }

    public void resetDynamicsWorld(float scale) {
        btDynamicsWorldTypesEnum.resetAllBtDynamicsWorlds(scale);
    }

    public void queueAssets(FileHandle rootFileHandle, FileTypeFilter.Rule rule) {
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

        Array<FileHandle> fileHandleList = HGUtils.traversFileHandle(rootFileHandle, ALL_FILES.getFileFilter()); // syncup: asset manager

        Arrays.stream(fileHandleList.toArray()).forEach(this::queueAsset); //.filter(rule::accept)
    }

    // See TextureLoader loadAsync() and loadSync() methods for use of this parameter
    private final TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
    private final SceneAssetLoaderParameters sceneAssetLoaderParameters = new SceneAssetLoaderParameters();

    public void queueAsset(FileHandle fileHandle) {
        if (fileHandle == null) { return; }
        assetsLoaded = false;

        Class<?> assetClass = getAssetClass(fileHandle);
        if (assetClass.equals(SceneAsset.class)) {
            // See GLBAssetLoader (or GLTFAssetLoader) loadAsync() and loadSync() methods for use of this parameter
            sceneAssetLoaderParameters.withData = true;
            assetManager.load(fileHandle.path(), SceneAsset.class, sceneAssetLoaderParameters);
        } else if (assetClass.equals(Texture.class)) {
            // See TextureLoader loadAsync() and loadSync() methods for use of this parameter
            // ATTENTION: 'gdx-1.10.0.jar' and 'gdx-backend-gwt-1.10.0.jar' both have
            //            com.badlogic.gdx.assets.loaders.TextureLoader inside (seemingly with different code)
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
            assetManager.load(fileHandle.path(), Texture.class, textureParameter);
        } else {
            assetManager.load(fileHandle.path(), assetClass, null);
        }
    }

    public static Class<?> getAssetClass(FileHandle fileHandle) {
        if (fileHandle == null) { return null; }
        switch (fileHandle.extension().toLowerCase()) {
//              case "3ds":  // converted to G3DB with fbx-conv
            case "obj":
            case "g3db":
            case "g3dj":
                return Model.class;
            case "gltf":
            case "glb":
                return SceneAsset.class;
            case "tga":
            case "png":
            case "bmp":
                return Texture.class;
            case "fnt":
                return BitmapFont.class;
            case "XXX": // for testing purposes
                return ParticleEffect.class;
            default:
                Gdx.app.error("engine", "Unexpected file extension: " + fileHandle.extension());
        }
        return null;
    }

    public <T> T getAsset(FileHandle fileHandle, Class<T> type) {
        FileHandle tmp = new FileHandle(fileHandle.file().getAbsolutePath());
        return assetManager.get(tmp.path(), type);
    }

    public void addAsset(FileHandle fileHandle) {
        if (fileHandle == null) { return; }
        Class<?> assetClass = getAssetClass(fileHandle);
        if (assetClass == null) { return; }
        //Gdx.app.debug("engine", "add asset:" + " fileHandle: " + fileHandle + " assetClass: " + assetClass.getSimpleName());
        if (assetClass.equals(Model.class)) {
            //Gdx.app.debug("engine", " add asset: " + " Model ");
            Model model = assetManager.get(fileHandle.path(), Model.class);
            this.hgModels.put(fileHandle, new HGModel(model, fileHandle));
        } else if (assetClass.equals(SceneAsset.class)) {
            //Gdx.app.debug("engine", " add asset: " + " SceneAsset ");
            SceneAsset sceneAsset = assetManager.get(fileHandle.path(), SceneAsset.class);
            this.hgModels.put(fileHandle, new HGModel(sceneAsset.scene.model, fileHandle));
        } else if (assetClass.equals(Texture.class)) {
            //Gdx.app.debug("engine", " add asset: " + " Texture ");
            Texture texture = assetManager.get(fileHandle.path(), Texture.class);
            this.hgTextures.put(fileHandle, new HGTexture(texture, fileHandle));
        }
    }

    public boolean updateLoad() {
        if (!assetsLoaded) { assetManager.update(); }
        return assetsLoaded;
    }

    public void getAllAssets() {
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#getting-assets
        Array<Model> models = new Array<>(Model.class);
        assetManager.getAll(Model.class, models);
        this.hgModels = Arrays.stream(models.toArray())
                .map(model -> {
                    String fn = assetManager.getAssetFileName(model);
                    FileHandle fh = assetManager.getFileHandleResolver().resolve(fn);
                    return new HGModel(model, fh); })                                     // Array<Model> -> Array<HGModel>
                .collect(() -> new ArrayMap<>(FileHandle.class, HGModel.class),
                        (accum, model) -> accum.put(model.afh, model), ArrayMap::putAll); // retrieving the ArrayMap<FileHandle, HGModel>
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "models loaded: " + hgModels.size);

        Array<Texture> textures = new Array<>(Texture.class);
        assetManager.getAll(Texture.class, textures);

        this.hgTextures = Arrays.stream(textures.toArray())
                .map(texture -> {
                    String fn = assetManager.getAssetFileName(texture);
                    FileHandle fh = assetManager.getFileHandleResolver().resolve(fn);
                    return new HGTexture(texture, fh); })                                       // Array<Texture> -> Array<HGTexture>
                .collect(() -> new ArrayMap<>(FileHandle.class, HGTexture.class),
                        (accum, texture) -> accum.put(texture.afh, texture), ArrayMap::putAll); // retrieving the ArrayMap<FileHandle, HGTexture>

        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "textures loaded: " + textures.size);
    }

    public HGModel getHgModelFromFileHandle(FileHandle assetFL) {
        if (assetFL == null) { return null; }
        if (!assetManager.contains(assetFL.path())) { return null; }

        Class<?> assetClass = getAssetClass(assetFL);
        HGModel hgModel = null;
        if (assetClass.equals(Model.class)) {
            hgModel = new HGModel(assetManager.get(assetFL.path(), Model.class), assetFL);
        } else if (assetClass.equals(SceneAsset.class)) {
            SceneAsset sceneAsset = assetManager.get(assetFL.path(), SceneAsset.class);
            hgModel = new HGModel(sceneAsset.scene.model, assetFL);
        }
        return hgModel;
    }

    public boolean addModelInstance(FileHandle assetFL) {
        return addModelInstance(assetFL, null, -1);
    }

    public boolean addModelInstance(FileHandle assetFL, String nodeId, int nodeIndex) {
        return addModelInstance(getHgModelFromFileHandle(assetFL), nodeId, nodeIndex);
    }

    public boolean addModelInstance(HGModel hgModel) {
        return addModelInstance(hgModel, null, -1);
    }

    public boolean addModelInstance(HGModel hgModel, String nodeId, int nodeIndex) {
        if (hgModel == null) { return false; }
        if (!hgModel.hasMaterials() && !hgModel.hasMeshes() && !hgModel.hasMeshParts()) {
            if (hgModel.hasAnimations()) {
                // we got animations only model
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "animations only model: " + hgModel.afh);
            }
            return false;
        }

        if (nodeId == null) {
            setCurrMI(new EditableModelInstance(hgModel, hgModel.afh, 10f, ShapesEnum.BOX));
        } else {
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

            setCurrMI(new EditableModelInstance(hgModel, hgModel.afh, 10f, ShapesEnum.BOX, nodeId));
            // for some reasons getting this exception in case nodeId == null:
            // (should be done like (String[])null maybe...)
            // Exception in thread "LWJGL Application" java.lang.NullPointerException
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.copyNodes(ModelInstance.java:232)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:155)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
        }

        editableMIs.add(getCurrMI());
        getCurrMI().addRigidBodyToDynamicsWorld(FLAG_OBJECT, FLAG_ALL);

        // ********************
        // **** ANIMATIONS ****
        // ********************
        copyExternalAnimations(hgModel.afh);

        getCurrMI().checkAnimations();

        return true;
    }

    /**
     * @param assetFL
     */
    private void copyExternalAnimations(FileHandle assetFL) {
        if (assetManager == null || getCurrMI() == null || assetFL == null) { return; }

        FileHandle animationsFolder = HGUtils.fileOnPath(assetFL, "animations");
        if (animationsFolder != null && animationsFolder.isDirectory()) {
            // final since it goes to lambda closure
            final Array<String> animationsPresent = new Array<>();
            // populating with animations already present
            for (Animation animation : getCurrMI().animations) { animationsPresent.add(animation.id); }

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
                        getCurrMI().copyAnimation(animation);
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

    public void resetBBModelInstances() {
        if (bbArrayHgModelInstance != null) { bbArrayHgModelInstance.clear(); } else { return; }

        for (EditableModelInstance mi: editableMIs) {
            if (mi.equals(getCurrMI())) { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.GREEN)); }
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
        if (getCurrMI() != null) {
            for (Material material:getCurrMI().materials) {
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
        editableMIs.removeValue(mi, true);
        mi.dispose();
    }

    public void clearModelInstances() {
        editableMIs.forEach(mi -> { mi.dispose(); });
        editableMIs.clear();
        // no need to dispose - will be done in HGModelInstance on dispose()
        //auxMIs.forEach(HGModelInstance::dispose);
        auxMIs.clear();
        selectedMIs.clear();
    }

    public void update(final float delta, boolean dynamics) {
        editableMIs.forEach(hgMI -> {
            if(hgMI.animationController != null && !hgMI.animationController.paused) {
                hgMI.animationController.update(delta);
//                if (animationDesc.loopCount == 0) {
//                    // do something if the animation is over
//                }
            }
        });

        // https://github.com/libgdx/gdx-ai/wiki/Initializing-and-Using-gdxAI
        // Timepiece is the AI clock which gives you the current time and the last delta time i.e.,
        // the time span between the current frame and the last frame in seconds. This is the only service
        // provider that does not depend on the environment, whether libgdx or not. It is needed because some
        // parts of gdx-ai (like for instance MessageDispatcher, Jump steering behavior and Wait task) have
        // a notion of spent time and we want to support game pause. It's developer's responsibility to update
        // the timepiece on each game loop. When the game is paused you simply don't update the timepiece.

        // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#wander
        // Note that this behavior internally calls the GdxAI.getTimepiece().getTime() method to get
        // the current AI time and make the wanderRate FPS independent. This means that
        // * if you forget to update the timepiece the wander orientation won't change.
        // * the timepiece should be always updated before this steering behavior runs.
        GdxAI.getTimepiece().update(delta);
        editableMIs.forEach(mi -> mi.update(delta));

        if (dynamics) {
            // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
            // The discrete dynamics world uses a fixed time step.
            // This basically means that it will always use the same delta value to perform calculations.
            // This fixed delta value is supplied as the third argument of stepSimulation.
            // If the actual delta value (the first argument) is greater than the desired fixed delta value,
            // then the calculation will be done multiple times.
            // The maximum number of times that this will be done (the maximum number of sub-steps) is specified
            // by the second argument.
            btDynamicsWorldTypesEnum.selected.dynamicsWorld.stepSimulation(delta, 5, 1f/60f);
        }
    }
}