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

package com.hammergenics.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hammergenics.HGEngine;
import com.hammergenics.HGGame;
import com.hammergenics.map.TerrainChunk;
import com.hammergenics.screens.graphics.g3d.EditableModelInstance;
import com.hammergenics.screens.graphics.g3d.utils.ModelEditInputController;
import com.hammergenics.screens.graphics.glutils.HGImmediateModeRenderer20;
import com.hammergenics.screens.stages.ModelEditStage;
import com.hammergenics.screens.stages.ui.attributes.AttributesManagerTable;
import com.hammergenics.utils.HGUtils;

import static com.hammergenics.HGEngine.filterModels;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditScreen extends ScreenAdapter {
    public final HGGame game;
    private final ModelBatch modelBatch;
    private final ModelCache modelCache;

    public final PerspectiveCamera perspectiveCamera;
    private final ModelEditInputController meic;
    public Environment environment;

    public HGEngine eng;

    public HGImmediateModeRenderer20 immediateModeRenderer;

    // 2D Stage - this is where all the widgets (buttons, checkboxes, labels etc.) are located
    public ModelEditStage stage;

    private float clock1s;

    /**
     * @param game
     */
    public ModelEditScreen(HGGame game, HGEngine engine, ModelBatch mb, ModelCache mc) {
        this.game = game;
        this.eng = engine;
        this.modelBatch = mb; // https://github.com/libgdx/libgdx/wiki/ModelBatch
        this.modelCache = mc; // https://github.com/libgdx/libgdx/wiki/ModelBatch

        immediateModeRenderer = new HGImmediateModeRenderer20(10*Short.MAX_VALUE, false, true, 0);

        // Camera related
        perspectiveCamera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        meic = new ModelEditInputController(this, perspectiveCamera);
        // Environment related
        environment = new Environment();
        resetEnvironment();

        // 2D Stage - https://github.com/libgdx/libgdx/wiki/Scene2d.ui#stage-setup
        stage = new ModelEditStage(new ScreenViewport(), game, this);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        // order of addProcessor matter
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(meic);
        Gdx.input.setInputProcessor(inputMultiplexer);

        stage.addModelInstance(eng.sphereHgModel);
        stage.afterCurrentModelInstanceChanged();
    }

    /**
     * @param delta
     */
    @Override
    public void render(float delta) {
        if (!eng.assetsLoaded && eng.assetManager.update()) {
            eng.getAssets();
            eng.assetsLoaded = true;
            stage.updateModelSelectBox();
            stage.mapGenerationTable.updateTrrnSelectBoxes();
        }

        meic.update(delta);

        eng.editableMIs.forEach(hgMI -> {
            if(hgMI.animationController != null) {
                hgMI.animationController.update(delta);
//                if (animationDesc.loopCount == 0) {
//                    // do something if the animation is over
//                }
            }
        });

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // https://encycolorpedia.com/96b0bc
        Gdx.gl.glClearColor(150 / 255f, 176 / 255f, 188 / 255f, 1f);
        // https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glClear.xml
        // (https://stackoverflow.com/questions/34164309/gl-color-buffer-bit-regenerating-which-memory)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (stage.aiManagerTable.steerCheckBox.isChecked()) {
            eng.editableMIs.forEach(mi -> mi.update(delta));
            if (stage.aiManagerTable.dbgModelInstance != null) {
                stage.aiManagerTable.updateSteerable();
            }
        }

        if (stage.physManagerTable.dynamicsCheckBox.isChecked()) {
            // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
            // The discrete dynamics world uses a fixed time step.
            // This basically means that it will always use the same delta value to perform calculations.
            // This fixed delta value is supplied as the third argument of stepSimulation.
            // If the actual delta value (the first argument) is greater than the desired fixed delta value,
            // then the calculation will be done multiple times.
            // The maximum number of times that this will be done (the maximum number of sub-steps) is specified
            // by the second argument.
            eng.dynamicsWorld.stepSimulation(delta, 5, 1f/60f);
            eng.editableMIs.forEach(EditableModelInstance::syncWithRBTransform);
        }

        // https://github.com/libgdx/libgdx/wiki/ModelCache#using-modelcache
        modelCache.begin();
        if (stage.isPressed(stage.mapTextButton) && eng.chunks.size > 0) {
            if (stage.mapGenerationTable.previewTerrain.isChecked()) {
                for (TerrainChunk tc: eng.chunks) { modelCache.add(tc.terrain); }
            }
        }
        modelCache.end();

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The Camera you supply is hold by reference, meaning that it must not be changed in between
        // the begin and end calls. If you need to switch camera in between the begin and end calls,
        // then you can call the modelBatch.setCamera(camera);, which will flush() the batch if needed.
        modelBatch.begin(perspectiveCamera);

        // https://github.com/libgdx/libgdx/wiki/ModelBatch#what-are-render-calls
        // To specify a render call, libGDX contains the Renderable (code) class, which contains almost everything
        // (except for the camera) required to perform a single render call.
        // it contains:
        // * how (the shader) and
        // * where (the transformation) to render
        // * the shape (the mesh part) in which
        // * context (the environment and material).

        // for future reference:
        // * Enable caching as soon as multiple instances are rendered: https://github.com/libgdx/libgdx/wiki/ModelCache
        if (eng.editableMIs.size > 0 && environment != null) { modelBatch.render(eng.editableMIs, environment); }
        if (eng.auxMIs.size > 0) { modelBatch.render(eng.auxMIs); }

        modelBatch.render(modelCache, environment);

        if (stage.mapGenerationTable.previewNoiseGrid.isChecked()) {
            for (TerrainChunk tc: eng.chunks) {
                if (tc.noiseLinesHGModelInstance != null) { modelBatch.render(tc.noiseLinesHGModelInstance); }
            }
        }
        if (stage.gridOriginCheckBox.isChecked()) { modelBatch.render(eng.gridOHgModelInstance); }
        if (stage.gridYCheckBox.isChecked()) {
            for (TerrainChunk tc: eng.chunks) {
                if (tc.yLinesHGModelInstance != null) { modelBatch.render(tc.yLinesHGModelInstance); }
            }
        }
        if (stage.physManagerTable.groundCheckBox.isChecked()) {
            for (TerrainChunk tc: eng.chunks) {
                if (tc.noiseTrianglesPhysModelInstance != null) { modelBatch.render(tc.noiseTrianglesPhysModelInstance, environment); }
            }
        }
        if (eng.dlArrayHgModelInstance != null && stage.lightsCheckBox.isChecked()) { modelBatch.render(eng.dlArrayHgModelInstance, environment); }
        if (eng.plArrayHgModelInstance != null && stage.lightsCheckBox.isChecked()) { modelBatch.render(eng.plArrayHgModelInstance, environment); }
        if (eng.bbArrayHgModelInstance != null && stage.bbCheckBox.isChecked()) { modelBatch.render(eng.bbArrayHgModelInstance, environment); }

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The actual rendering is performed at the call to end();.
        // If you want to force rendering in between, then you can use the modelBatch.flush(); method
        modelBatch.end();

        immediateModeRenderer.begin(perspectiveCamera.combined, GL20.GL_LINES);
        if (stage.showSelectionScaleCheckBox.isChecked()) {
            if (eng.currMI != null) { eng.currMI.addSelectionBoxToRenderer(immediateModeRenderer, Color.RED); }
            eng.selectedMIs.forEach(mi -> mi.addSelectionBoxToRenderer(immediateModeRenderer, Color.FOREST));
        }
        if (stage.nodesCheckBox.isChecked()) { eng.editableMIs.forEach(hgMI -> hgMI.addNodesToRenderer(immediateModeRenderer)); }
        if (stage.meshPartsCheckBox.isChecked()) { eng.editableMIs.forEach(hgMI -> hgMI.addMeshPartsToRenderer(immediateModeRenderer)); }
        if (stage.bonesCheckBox.isChecked()) { eng.editableMIs.forEach(hgMI ->
                hgMI.addBonesToRenderer(immediateModeRenderer, stage.invertCheckBox.isChecked())); }
        if (stage.physManagerTable.rbCheckBox.isChecked()) { eng.editableMIs.forEach(mi -> mi.addRBShapeToRenderer(immediateModeRenderer)); }
        if (stage.verticesCheckBox.isChecked()) { eng.editableMIs.forEach(mi -> mi.addVerticesToRenderer(immediateModeRenderer)); }
        if (stage.closestCheckBox.isChecked()) { eng.editableMIs.forEach(mi -> mi.addClosestVerticesToRenderer(immediateModeRenderer)); }
        immediateModeRenderer.end();

        checkTimerEvents(delta);

        stage.act(delta);
        stage.draw();
    }

    /**
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (meic != null) {
            meic.camera.viewportWidth = width;
            meic.camera.viewportHeight = height;
            meic.update(-1f);
        }
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    /**
     *
     */
    @Override
    public void dispose() {
        super.dispose();
        if (stage != null) { stage.dispose(); }
    }

    /**
     * @param delta
     */
    private void checkTimerEvents(float delta) {
        clock1s += delta;  // add the time since the last frame
        if (clock1s > 1) { // every second
            // updating FPS
            int fps = Gdx.graphics.getFramesPerSecond();
            stage.fpsLabel.setText("FPS: " + fps);

            // check if there're changes made in the root directory
            // the map should be ordered: see resetFolderSelectBoxItems
            ArrayMap<FileHandle, Array<FileHandle>> f2m = new ArrayMap<>(true, 16, FileHandle.class, Array.class);
            HGUtils.traversFileHandle(Gdx.files.internal("root"), filterModels, f2m); // syncup: asset manager

            if (!eng.folder2models.equals(f2m)) {
                eng.folder2models = f2m;
                stage.resetFolderSelectBoxItems(f2m);
            }

            clock1s = 0;
        }
    }

    public void reset() {
        eng.arrangeInSpiral(stage.origScaleCheckBox.isChecked());

        if (eng.unitSize == 0f) { eng.unitSize = 1f; eng.overallSize = 5f; }

        Vector3 center = eng.currMI != null ? eng.currMI.getBB().getCenter(Vector3.Zero.cpy()) : Vector3.Zero.cpy();

        resetCamera(eng.overallSize, center.cpy());
        resetScreenInputController(eng.unitSize, eng.overallSize, center.cpy());

        if (!environment.has(DirectionalLightsAttribute.Type)
                && !environment.has(PointLightsAttribute.Type)
                && !environment.has(SpotLightsAttribute.Type)) {
            addInitialEnvLights(); // if no lights defined adds 1 directional light to the environment
        }

        eng.resetGridModelInstances();
        eng.resetLightsModelInstances(center.cpy(), environment);
        eng.resetChunks(eng.unitSize);

        stage.envAttrTable = new AttributesManagerTable(stage.skin, environment, this);
        stage.envAttrTable.setListener(stage.eventListener);
    }

    /**
     * @param distance
     */
    private void resetCamera(float distance, Vector3 lookAtVector) {
        // IDEA finds 2 classes extending Camera:
        // 1. OrthographicCamera
        // 2. PerspectiveCamera

        // Camera Articles:
        // https://github.com/libgdx/libgdx/wiki/Orthographic-camera
        // https://stackoverflow.com/questions/54198655/how-camera-works-in-libgdx-and-together-with-viewport
        // https://gamefromscratch.com/libgdx-tutorial-7-camera-basics/
        // https://gamefromscratch.com/libgdx-tutorial-part-16-cameras/
        // TODO: need to visually debug this as well as switching between Orthographic and Perspective cameras (?)
        perspectiveCamera.fieldOfView = 70f;                              // PerspectiveCamera: float fieldOfView
                                                                          //                    the field of view of the height, in degrees
        perspectiveCamera.position.set(lookAtVector.cpy().add(distance)); // Camera: Vector3 position
        perspectiveCamera.direction.set(0, 0, -1);                        // Camera: Vector3 direction
        perspectiveCamera.up.set(0, 1, 0);                                // Camera: Vector3 up
        perspectiveCamera.lookAt(lookAtVector);                           //   camera.up and camera.direction must
                                                                          //   ALWAYS be orthonormal vectors
        //perspectiveCamera.projection;                                   // Camera: Matrix4 projection
        //perspectiveCamera.view;                                         // Camera: Matrix4 view
        //perspectiveCamera.combined;                                     // Camera: Matrix4 combined
        //perspectiveCamera.invProjectionView;                            // Camera: Matrix4 invProjectionView
        perspectiveCamera.near = Math.min(1f, distance/10);               // Camera: float near
        perspectiveCamera.far = 1000*distance;                            // Camera: float far
        //perspectiveCamera.viewportWidth;                                // Camera: float viewportWidth
        //perspectiveCamera.viewportHeight;                               // Camera: float viewportHeight
        //perspectiveCamera.frustum;                                      // Camera: Frustum frustum
                                                                          //         A truncated rectangular pyramid.
                                                                          //         Used to define the viewable region and
                                                                          //         its projection onto the screen
        //perspectiveCamera.frustum.planes;                               // Frustum: Plane[] planes
                                                                          //          the six clipping planes:
                                                                          //          near, far, left, right, top, bottom
        //perspectiveCamera.frustum.planePoints;                          // Frustum: Vector3[] planePoints
                                                                          //          eight points making up the near and far clipping "rectangles".
                                                                          //          order is counter clockwise, starting at bottom left
        // See also:
        // *   frustum culling : https://en.wikipedia.org/wiki/Hidden-surface_determination#Viewing-frustum_culling
        // * back-face culling : https://en.wikipedia.org/wiki/Back-face_culling
        perspectiveCamera.update();
    }

    /**
     * @param overallSize
     */
    private void resetScreenInputController(float unitSize, float overallSize, Vector3 rotateAroundVector) {
        meic.unitDistance = unitSize;
        meic.overallDistance = overallSize;
        meic.rotateAround.set(rotateAroundVector);
        meic.update(-1f);
    }

    /**
     *
     */
    private void resetEnvironment() {
        // https://github.com/libgdx/libgdx/wiki/Material-and-environment
        // In practice, when rendering, you are specifying what (the shape) to render and how (the material) to render.
        // * The shape is specified using the Mesh (or more commonly the MeshPart),
        //   which defines the vertices attributes for the shader.
        // * The material is most commonly used to specify the uniform values for the shader.

        // Uniforms can be grouped into
        // * model specific (e.g. the texture applied or whether or not to use blending) and
        // * environmental uniforms (e.g. the lights being applied or an environment cubemap).
        // Likewise the 3D api allows you to specify a material and environment.

        // https://github.com/libgdx/libgdx/wiki/Material-and-environment#environment
        // An Environment contains the uniform values specific for a location.
        // For example, the lights are part of the Environment.
        // Simple applications might use only Environment, while more complex applications might use multiple
        // environments depending on the location of a ModelInstance.
        // TODO: keep for now
        // !!! A ModelInstance (or Renderable) can only contain one Environment though.

        environment.clear();
        // Some attribute classes are dedicated to a single type value (bit).
        // Others can be used for multiple type values (bits), in which case you must specify the type on construction.
        environment.set(ColorAttribute.createAmbient(Color.GRAY));           // min enabled
//      environment.set(ColorAttribute.createDiffuse(Color.GRAY));           // ! darkens the model
        environment.set(ColorAttribute.createSpecular(Color.GRAY));          // min enabled
        environment.set(ColorAttribute.createReflection(Color.GRAY));        // min enabled
//      environment.set(ColorAttribute.createEmissive(Color.GRAY));          // ! adds glowing effect  // JdxLib v1.10.0
        environment.set(ColorAttribute.createAmbientLight(Color.GRAY));      // min enabled            // JdxLib v1.10.0
        environment.set(ColorAttribute.createFog(Color.GRAY));               // min enabled            // JdxLib v1.10.0

        if (environment.has(DirectionalLightsAttribute.Type)) { environment.remove(DirectionalLightsAttribute.Type); }
        if (environment.has(PointLightsAttribute.Type)) { environment.remove(PointLightsAttribute.Type); }
        if (environment.has(SpotLightsAttribute.Type)) { environment.remove(SpotLightsAttribute.Type); }
    }

    private void addInitialEnvLights() {
        // ADDING LIGHTS TO THE ENVIRONMENT
        // https://github.com/libgdx/libgdx/wiki/Material-and-environment#lights
        // you can attach a light to either an environment or a material.
        // Adding a light to an environment can still be done using the environment.add(light) method.
        // However, you can also use the
        // - DirectionalLightsAttribute
        // - PointLightsAttribute
        // - SpotLightsAttribute
        // attributes.
        // Each of these attributes has an array which you can use to attach one or more lights to it.
        // TODO: keep for now
        // !!! If you add a light to the PointLightsAttribute of the environment
        // and then add another light to the PointLightsAttribute of the material,
        // then the DefaultShader will ignore the point light(s) added to the environment.
        // !!! Lights are always used by reference.
        // IMPORTANT NOTE ON SHADER CONFIG: https://github.com/libgdx/libgdx/wiki/ModelBatch#default-shader

        // TODO: keep for now
        // Lights should be sorted by importance. Usually this means that lights should be sorted on distance.
        // !!! The DefaultShader for example by default (configurable) only uses the first five point lights for shader lighting.
        // Any remaining lights will be added to an ambient cubemap which is much less accurate.

        // adding a single directional light
        environment.add(new DirectionalLight().set(Color.LIGHT_GRAY, -1f, -0.5f, -1f));
        // TODO: revisit the commented code below (Point Light addition).
        //       Issue: when more objects are added the grid gets rescaled and point lights are no longer visible.
        // adding a single point light
        //Vector3 plPosition;
        //if (eng.dbgMIs != null && eng.dbgMIs.size > 0) {
        //    plPosition = eng.dbgMIs.get(0).getBB().getCenter(new Vector3());
        //} else {
        //    plPosition = Vector3.Zero.cpy();
        //}
        //plPosition.add(-eng.overallSize/2, eng.overallSize/2, eng.overallSize/2);
        // seems that intensity should grow exponentially(?) over the distance, the table is:
        //  unitSize: 1.7   17    191    376    522
        // intensity:   1  100  28708  56470  78397
        //float intensity = (eng.overallSize < 50f ? 10.10947f : 151.0947f) * eng.overallSize - 90f; // TODO: temporal solution, revisit
        //intensity = intensity <= 0 ? 1f : intensity;                                               // TODO: temporal solution, revisit
        //environment.add(new PointLight().set(Color.WHITE, plPosition, intensity < 0 ? 0.5f : intensity)); // syncup: pl
    }
}