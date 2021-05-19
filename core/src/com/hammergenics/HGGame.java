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
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
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

        FileHandle rootFileHandle = Gdx.files.local(Conventions.modelsRootDirectory);
        Array<FileHandle> fileHandleList = LibgdxUtils.traversFileHandle(rootFileHandle,
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("obj")
//                        || file.getName().toLowerCase().endsWith(".g3dj") // json // disabling for now...
                        || file.getName().toLowerCase().endsWith("g3db") // binary
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
        );
        fileHandleList.forEach(fileHandle -> {
            switch (fileHandle.extension().toLowerCase()) {
                case "obj":
                case "g3db":
                case "g3dj":
                    assetManager.load(fileHandle.path(), Model.class, null);
                    break;
                case "png":
                    assetManager.load(fileHandle.path(), Texture.class, null);
                    break;
                default:
                    Gdx.app.error(getClass().getSimpleName(),
                            "Unexpected file extension: " + fileHandle.extension());
            }
        });

        // https://github.com/libgdx/libgdx/wiki/ModelBatch#default-shader
        // The behavior of DefaultShader class is configurable by supplying
        // a DefaultShader.Config instance to the DefaultShaderProvider.

// Default Values from gen_1.10.0_Config_...
//  String vertexShader         = null // The GPU shader (the vertex and fragment shader) to be used is also configurable using this config.
//  String fragmentShader       = null // Because this shader can be used for various combinations of attributes, it typically is a so-called ubershader.
//     int numDirectionalLights = 2
//     int numPointLights       = 5
//     int numSpotLights        = 0
//     int numBones             = 12
// boolean ignoreUnimplemented  = true
//     int defaultCullFace      = -1
//     int defaultDepthFunc     = -1

        // DefaultShader.Config config = new DefaultShader.Config();
        // config.numDirectionalLights = 1;
        // config.numPointLights = 0;
        // config.numBones = 16;

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // You'd typically create a ModelBatch in the create() method.
        //modelBatch = new ModelBatch(new DefaultShaderProvider(config));
        modelBatch = new ModelBatch();

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
