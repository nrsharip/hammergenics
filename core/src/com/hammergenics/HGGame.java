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
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.hammergenics.config.Config;
import com.hammergenics.config.Conventions;
import com.hammergenics.screens.LoadScreen;
import com.hammergenics.util.LibgdxUtils;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGame extends Game {
    public final AssetManager assetManager = new AssetManager();
    public ModelBatch modelBatch;

    /**
     *
     */
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        assetManager.getLogger().setLevel(Logger.DEBUG);

        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getDensity() = " + Gdx.graphics.getDensity());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getWidth()   = " + Gdx.graphics.getWidth());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getHeight()  = " + Gdx.graphics.getHeight());

        // see public BitmapFont ()
        // Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png")

        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#loading-a-ttf-using-the-assethandler
        // Adding TTF loader
        FileHandleResolver fileHandleResolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(fileHandleResolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(fileHandleResolver));

        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#loading-a-ttf-using-the-assethandler
        FreetypeFontLoader.FreeTypeFontLoaderParameter param = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        param.fontFileName = Config.ASSET_FILE_NAME_FONT;
        param.fontParameters.size = 16;
        assetManager.load(Config.ASSET_FILE_NAME_FONT, BitmapFont.class, param);

        // FIXME: Seems like TTF is breaking :html project
//[ERROR] Line 55: No source code is available for type com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator; did you forget to inherit a required module?
//[ERROR] Line 55: No source code is available for type com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader; did you forget to inherit a required module?
//[ERROR] Line 56: No source code is available for type com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader; did you forget to inherit a required module?
//[ERROR] Line 59: No source code is available for type com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter; did you forget to inherit a required module?

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

        FileHandle rootFileHandle = Gdx.files.local(Conventions.modelsRootDirectory);
        Array<FileHandle> fileHandleList = LibgdxUtils.traversFileHandle(rootFileHandle,
                file -> file.isDirectory()
//                      || file.getName().toLowerCase().endsWith(".3ds")  // converted to G3DB with fbx-conv
                        || file.getName().toLowerCase().endsWith(".obj")  // wavefront
//                      || file.getName().toLowerCase().endsWith(".gltf") // see for support: https://github.com/mgsx-dev/gdx-gltf
                        || file.getName().toLowerCase().endsWith(".tga")  // textures in TGA
//                      || file.getName().toLowerCase().endsWith(".g3dj") // json // disabling for now...
                        || file.getName().toLowerCase().endsWith(".g3db") // binary
                        || file.getName().toLowerCase().endsWith(".png")  // textures in PNG
                        || file.getName().toLowerCase().endsWith(".bmp")  // textures in BMP
        );

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
//              case "g3dj":
                    assetManager.load(fileHandle.path(), Model.class, null);
                    break;
                case "tga":
                case "png":
                case "bmp":
                    assetManager.load(fileHandle.path(), Texture.class, textureParameter);
                    break;
                case "XXX": // for testing purposes
                    assetManager.load(fileHandle.path(), ParticleEffect.class, null);
                    break;
                default:
                    Gdx.app.error(getClass().getSimpleName(),
                            "Unexpected file extension: " + fileHandle.extension());
            }
        });

        // https://github.com/libgdx/libgdx/wiki/ModelBatch#default-shader
        // The behavior of DefaultShader class is configurable by supplying
        // a DefaultShader.Config instance to the DefaultShaderProvider.

        DefaultShader.Config config = new DefaultShader.Config();
//        config.vertexShader;         // String = null - The uber vertex shader to use, null to use the default vertex shader.
//        config.fragmentShader;       // String = null - The uber fragment shader to use, null to use the default fragment shader.
//        config.numDirectionalLights; // int = 2 The number of directional lights to use
//        config.numPointLights;       // int = 5 The number of point lights to use
//        config.numSpotLights;        // int = 0 The number of spot lights to use
//        config.numBones;             // int = 12 The number of bones to use
//        config.ignoreUnimplemented;  // boolean = true
//        config.defaultCullFace;      // int = -1 Set to 0 to disable culling, -1 to inherit from {@link DefaultShader#defaultCullFace}
//        config.defaultDepthFunc;     // int = -1 Set to 0 to disable depth test, -1 to inherit from {@link DefaultShader#defaultDepthFunc}

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // You'd typically create a ModelBatch in the create() method.
        modelBatch = new ModelBatch(new DefaultShaderProvider(config));
        //modelBatch = new ModelBatch();

        this.setScreen(new LoadScreen(this));
    }

    /**
     *
     */
    @Override
    public void render() {
        super.render(); // important (see implementation)
    }

    /**
     *
     */
    @Override
    public void dispose() {
        assetManager.dispose();

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // Because it contains native resources (like the shaders it uses),
        // you'll need to call the dispose() method when no longer needed.
        modelBatch.dispose();

        // Note that the dispose() method of the GameScreen class is not called automatically, see the Screen API.
        // It is your responsibility to take care of that.
        // You can call this method from the dispose() method of the Game class,
        // if the GameScreen class passes a reference to itself to the Game class.
        getScreen().dispose();
    }
}
