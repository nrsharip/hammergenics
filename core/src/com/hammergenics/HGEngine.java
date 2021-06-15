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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Sort;
import com.hammergenics.config.Config;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.screens.utils.AttributesMap;
import com.hammergenics.utils.LibgdxUtils;

import java.io.FileFilter;
import java.util.Arrays;

import static com.hammergenics.screens.graphics.g3d.utils.Models.createGridModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createLightsModel;

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

    public Array<HGModel> hgModels = new Array<>();
    public Array<Texture> textures = new Array<>();
    // Auxiliary models:
    public HGModel gridHgModel = null;
    public HGModel lightsHgModel = null;
    public HGModelInstance gridXZHgModelInstance = null; // XZ plane: lines (yellow)
    public HGModelInstance gridYHgModelInstance = null;  // Y axis: vertical lines (red)
    public HGModelInstance gridOHgModelInstance = null;  // origin: sphere (red)
    public Array<HGModelInstance> dlArrayHgModelInstance = null; // directional lights
    public Array<HGModelInstance> plArrayHgModelInstance = null; // point lights
    public Array<HGModelInstance> bbArrayHgModelInstance = null; // bounding boxes
    // the general container for any auxiliary model instances
    public Array<HGModelInstance> auxMIs = new Array<>(HGModelInstance.class);

    // ModelInstance Related:
    public Array<HGModelInstance> hgMIs = new Array<>(HGModelInstance.class);
    public float unitSize = 0f;
    public float overallSize = 0f;
    public HGModelInstance currMI = null;
    public Vector2 currCell = Vector2.Zero.cpy();
    // main Model Instances
    public HGModelInstance hoveredOverMI = null;
    public AttributesMap hoveredOverMIAttributes = null;
    public HGModelInstance draggedMI = null;
    // bounding box, corners
    public HGModelInstance hoveredOverBBMI = null;
    public Array<HGModelInstance> hoveredOverCornerMIs = null;
    public HGModelInstance hoveredOverCorner = null;
    public AttributesMap hoveredOverCornerAttributes = null;

    public HGEngine(HGGame game) {
        this.game = game;
        assetManager.getLogger().setLevel(Logger.DEBUG);

        // see public BitmapFont ()
        // Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png")

        assetManager.load(Config.ASSET_FILE_NAME_FONT, BitmapFont.class, null);
        assetManager.finishLoading();
        // Creating the Aux Models beforehand:
        gridHgModel = new HGModel(createGridModel());
        lightsHgModel = new HGModel(createLightsModel());
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        if (gridHgModel != null) { gridHgModel.dispose(); }
        if (lightsHgModel != null) { lightsHgModel.dispose(); }
        for (HGModelInstance mi:hgMIs) { mi.dispose(); }

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

        Array<FileHandle> fileHandleList = LibgdxUtils.traversFileHandle(rootFileHandle, filterAll); // syncup: asset manager

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
                    return new HGModel(model, fh); })                                  // Array<Model> -> Array<HGModel>
                .collect(() -> new Array<>(HGModel.class), Array::add, Array::addAll); // retrieving the Array<HGModel>
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "models loaded: " + hgModels.size);

        assetManager.getAll(Texture.class, textures);
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "textures loaded: " + textures.size);
    }

    public void addModelInstances(Array<FileHandle> modelFHs) {
        if (modelFHs == null) { return; }

        modelFHs.forEach(fileHandle -> addModelInstance(fileHandle, null, -1));

        if (hgMIs.size > 0) { currMI = hgMIs.get(0); }
    }

    public boolean addModelInstance(FileHandle assetFL, String nodeId, int nodeIndex) {
        HGModel hgModel = new HGModel(assetManager.get(assetFL.path(), Model.class), assetFL);
        return addModelInstance(hgModel, nodeId, nodeIndex);
    }

    public boolean addModelInstance(Model model) {
        return addModelInstance(model, null, -1);
    }

    public boolean addModelInstance(Model model, String nodeId, int nodeIndex) {
        return addModelInstance(new HGModel(model), nodeId, nodeIndex);
    }

    public boolean addModelInstance(HGModel hgModel, String nodeId, int nodeIndex) {
        if (!hgModel.hasMaterials() && !hgModel.hasMeshes() && !hgModel.hasMeshParts()) {
            if (hgModel.hasAnimations()) {
                // we got animations only model
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "animations only model: " + hgModel.afh);
            }
            return false;
        }

        if (nodeId == null) {
            currMI = new HGModelInstance(hgModel, hgModel.afh);
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
            currMI = new HGModelInstance(hgModel, hgModel.afh, nodeId);
            // for some reasons getting this exception in case nodeId == null:
            // (should be done like (String[])null maybe...)
            // Exception in thread "LWJGL Application" java.lang.NullPointerException
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.copyNodes(ModelInstance.java:232)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:155)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
        }

        currMI.setAttributes(new BlendingAttribute());
        hgMIs.add(currMI);

        // ********************
        // **** ANIMATIONS ****
        // ********************
        copyExternalAnimations(hgModel.afh);

        currMI.animationController = null;
        if (currMI.animations.size > 0) { currMI.animationController = new AnimationController(currMI); }

        return true;
    }

    /**
     * @param assetFL
     */
    private void copyExternalAnimations(FileHandle assetFL) {
        if (assetManager == null || currMI == null || assetFL == null) { return; }

        FileHandle animationsFolder = LibgdxUtils.fileOnPath(assetFL, "animations");
        if (animationsFolder != null && animationsFolder.isDirectory()) {
            // final since it goes to lambda closure
            final Array<String> animationsPresent = new Array<>();
            // populating with animations already present
            for (Animation animation : currMI.animations) { animationsPresent.add(animation.id); }

            for (int i = 0; i < hgModels.size; i++) {  // using for loop instead of for-each to avoid nested iterators exception:
                HGModel hgm = hgModels.get(i);         // GdxRuntimeException: #iterator() cannot be used nested.
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
                        if (!animationsPresent.contains(animation.id, false)) {
                            Gdx.app.debug(Thread.currentThread().getStackTrace()[3].getMethodName(),
                                    "adding animation: " + animation.id);
                            currMI.copyAnimation(animation);
                            animationsPresent.add(animation.id);
                        }
                    });
                }
            }
        }
    }

    public Vector2 arrangeInSpiral(boolean keepOriginalScale) {
        Vector2 cell = Vector2.Zero.cpy();
        unitSize = 0f;
        for(HGModelInstance hgMI: hgMIs) { if (hgMI.maxD > unitSize) { unitSize = hgMI.maxD; } }
        for(HGModelInstance hgMI: hgMIs) {
            hgMI.transform.idt(); // first cancel any previous transform
            float factor = 1f;
            // Scale: if the dimension of the current instance is less than maximum dimension of all instances scale it
            if (!keepOriginalScale && hgMI.maxD < unitSize) { factor = unitSize/hgMI.maxD; }

            Vector3 center = hgMI.getBB().getCenter(new Vector3());
            Vector3 position;
            // Position:
            // 1. Move the instance (scaled center) to the current base position ([cell.x, 0, cell.y] vector sub scaled center vector)
            // 2. Add half of the scaled height to the current position so bounding box's bottom matches XZ plane
            position = new Vector3(cell.x * 1.1f * unitSize, 0f, cell.y * 1.1f * unitSize)
                    .sub(center.cpy().scl(factor))
                    .add(0, factor * hgMI.getBB().getHeight()/2, 0);
            hgMI.moveAndScaleTo(position, Vector3.Zero.cpy().add(factor));
            // spiral loop around (0, 0, 0)
            LibgdxUtils.spiralGetNext(cell);
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
        if (gridHgModel == null) { return; }

        gridXZHgModelInstance = new HGModelInstance(gridHgModel, "XZ");
        gridYHgModelInstance = new HGModelInstance(gridHgModel, "Y");
        gridOHgModelInstance = new HGModelInstance(gridHgModel, "origin");

        gridXZHgModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        gridYHgModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        gridOHgModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(unitSize/4f));
    }

    public void resetBBModelInstances() {
        if (bbArrayHgModelInstance != null) {
            bbArrayHgModelInstance.clear();
            bbArrayHgModelInstance = null;
        }
        bbArrayHgModelInstance = new Array<>(ModelInstance.class);

        for (HGModelInstance mi:hgMIs) {
            if (mi.equals(currMI)) { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.GREEN)); }
            else { bbArrayHgModelInstance.add(mi.getBBHgModelInstance(Color.BLACK)); }
        }
    }

    public void resetLightsModelInstances(Vector3 center, Environment environment) {
        if (dlArrayHgModelInstance != null) { dlArrayHgModelInstance.clear(); dlArrayHgModelInstance = null; }
        if (plArrayHgModelInstance != null) { plArrayHgModelInstance.clear(); plArrayHgModelInstance = null; }
        dlArrayHgModelInstance = new Array<>(ModelInstance.class);
        plArrayHgModelInstance = new Array<>(ModelInstance.class);

        Vector3 envPosition;
        if (hgMIs != null && hgMIs.size > 0) {
            envPosition = hgMIs.get(0).getBB().getCenter(new Vector3());
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
    public Array<HGModelInstance> rayMICollision(Ray ray, Array<HGModelInstance> modelInstances, Array<HGModelInstance> out) {
        // TODO: revisit this later when the Bullet Collision Physics is added

        for (HGModelInstance mi:modelInstances) {
            if (Intersector.intersectRayBoundsFast(ray, mi.getBB())) { out.add(mi); }
        }
        Sort.instance().sort(out, (mi1, mi2) -> {
            Vector3 bbc1 = mi1.getBB().getCenter(new Vector3());
            Vector3 bbc2 = mi2.getBB().getCenter(new Vector3());

            float len1 = ray.origin.cpy().sub(bbc1).len();
            float len2 = ray.origin.cpy().sub(bbc2).len();

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

    public void clearModelInstances() {
        hgMIs.forEach(HGModelInstance::dispose);
        hgMIs.clear();
        // no need to dispose - will be done in HGModelInstance on dispose()
        //auxMIs.forEach(HGModelInstance::dispose);
        auxMIs.clear();
    }
}