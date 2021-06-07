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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.utils.LibgdxUtils;

import static com.hammergenics.HGEngine.filterModels;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGame extends Game {
    public ModelBatch modelBatch;
    public HGEngine engine;

    /**
     *
     */
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getDensity() = " + Gdx.graphics.getDensity());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getWidth()   = " + Gdx.graphics.getWidth());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getHeight()  = " + Gdx.graphics.getHeight());

        engine = new HGEngine(this);
        // the map should be ordered: see resetFolderSelectBoxItems
        engine.folder2models = new ArrayMap<>(true, 16, FileHandle.class, Array.class);
        LibgdxUtils.traversFileHandle(Gdx.files.local("./"), filterModels, engine.folder2models); // syncup: asset manager

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

        this.setScreen(new ModelEditScreen(this));
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