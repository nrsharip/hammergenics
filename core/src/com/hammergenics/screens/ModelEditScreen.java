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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hammergenics.HGEngine;
import com.hammergenics.HGGame;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.screens.graphics.g3d.utils.ModelEditInputController;
import com.hammergenics.screens.graphics.glutils.HGImmediateModeRenderer20;
import com.hammergenics.screens.stages.ModelEditStage;
import com.hammergenics.screens.utils.AttributesMap;
import com.hammergenics.utils.LibgdxUtils;

import static com.hammergenics.HGEngine.filterModels;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createTestSphere;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditScreen extends ScreenAdapter {
    public final HGGame game;
    private final ModelBatch modelBatch;

    private PerspectiveCamera perspectiveCamera;
    private ModelEditInputController modelEditInputController;
    public Environment environment;

    public HGEngine eng;

    public HGImmediateModeRenderer20 immediateModeRenderer;

    // 2D Stage - this is where all the widgets (buttons, checkboxes, labels etc.) are located
    public ModelEditStage stage;

    private float clock1s;

    /**
     * @param game
     */
    public ModelEditScreen(HGGame game, HGEngine engine, ModelBatch mb) {
        this.game = game;
        this.eng = engine;
        this.modelBatch = mb; // https://github.com/libgdx/libgdx/wiki/ModelBatch

        // Camera related
        perspectiveCamera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        modelEditInputController = new ModelEditInputController(this, perspectiveCamera);
        // Environment related
        environment = new Environment();

        immediateModeRenderer = new HGImmediateModeRenderer20(10*Short.MAX_VALUE, false, true, 0);

        // 2D Stage - https://github.com/libgdx/libgdx/wiki/Scene2d.ui#stage-setup
        stage = new ModelEditStage(new ScreenViewport(), game, this);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        // order of addProcessor matter
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(modelEditInputController);
        Gdx.input.setInputProcessor(inputMultiplexer);

        //eng.addModelInstance(createTestBox(GL20.GL_POINTS));
        //eng.addModelInstance(createTestBox(GL20.GL_LINES));
        //eng.addModelInstance(createTestBox(GL20.GL_TRIANGLES));
        eng.addModelInstance(createTestSphere(GL20.GL_TRIANGLES, 40));
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
        }

        modelEditInputController.update(delta);

        eng.hgMIs.forEach(hgMI -> {
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
        if (eng.hgMIs.size > 0 && environment != null) { modelBatch.render(eng.hgMIs, environment); }
        if (eng.auxMIs.size > 0) { modelBatch.render(eng.auxMIs); }
        if (eng.gridXZHgModelInstance != null && stage.gridXZCheckBox.isChecked()) {
            modelBatch.render(eng.gridXZHgModelInstance);
            modelBatch.render(eng.gridOHgModelInstance);
        }
        if (eng.gridYHgModelInstance != null && stage.gridYCheckBox.isChecked()) { modelBatch.render(eng.gridYHgModelInstance); }
        if (eng.dlArrayHgModelInstance != null && stage.lightsCheckBox.isChecked()) { modelBatch.render(eng.dlArrayHgModelInstance, environment); }
        if (eng.plArrayHgModelInstance != null && stage.lightsCheckBox.isChecked()) { modelBatch.render(eng.plArrayHgModelInstance, environment); }
        if (eng.bbArrayHgModelInstance != null && stage.bbCheckBox.isChecked()) { modelBatch.render(eng.bbArrayHgModelInstance, environment); }

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The actual rendering is performed at the call to end();.
        // If you want to force rendering in between, then you can use the modelBatch.flush(); method
        modelBatch.end();

        immediateModeRenderer.begin(perspectiveCamera.combined, GL20.GL_LINES);
        if (stage.nodesCheckBox.isChecked()) { eng.hgMIs.forEach(hgMI -> hgMI.addNodesToRenderer(immediateModeRenderer)); }
        if (stage.meshPartsCheckBox.isChecked()) { eng.hgMIs.forEach(hgMI -> hgMI.addMeshPartsToRenderer(immediateModeRenderer)); }
        if (stage.bonesCheckBox.isChecked()) { eng.hgMIs.forEach(hgMI -> hgMI.addBonesToRenderer(immediateModeRenderer)); }
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
        if (modelEditInputController != null) {
            modelEditInputController.camera.viewportWidth = width;
            modelEditInputController.camera.viewportHeight = height;
            modelEditInputController.update(-1f);
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
        if (eng != null) { eng.dispose(); }
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
            LibgdxUtils.traversFileHandle(Gdx.files.internal("root"), filterModels, f2m); // syncup: asset manager

            if (!eng.folder2models.equals(f2m)) {
                eng.folder2models = f2m;
                stage.resetFolderSelectBoxItems(f2m);
            }

            clock1s = 0;
        }
    }

    public void reset() {
        eng.arrangeInSpiral(stage.origScaleCheckBox.isChecked());

        Vector3 center = eng.currMI != null ? eng.currMI.getBB().getCenter(Vector3.Zero.cpy()) : Vector3.Zero.cpy();

        resetCamera(eng.overallSize, center.cpy());
        resetScreenInputController(eng.unitSize, eng.overallSize, center.cpy());
        resetEnvironment();    // clears all lights
        addInitialEnvLights(); // adds 1 directional and 1 point light to the environment

        eng.resetGridModelInstances();
        eng.resetLightsModelInstances(center.cpy(), environment);
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
        modelEditInputController.unitDistance = unitSize;
        modelEditInputController.overallDistance = overallSize;
        modelEditInputController.rotateAround.set(rotateAroundVector);
        modelEditInputController.update(-1f);
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
        environment.set(ColorAttribute.createAmbientLight(Color.DARK_GRAY)); // min enabled            // JdxLib v1.10.0
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
        environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.5f, -1f));
        // adding a single point light
        Vector3 plPosition;
        if (eng.hgMIs != null && eng.hgMIs.size > 0) {
            plPosition = eng.hgMIs.get(0).getBB().getCenter(new Vector3());
        } else {
            plPosition = Vector3.Zero.cpy();
        }
        plPosition.add(-eng.overallSize/2, eng.overallSize/2, eng.overallSize/2);
        // seems that intensity should grow exponentially(?) over the distance, the table is:
        //  unitSize: 1.7   17    191    376    522
        // intensity:   1  100  28708  56470  78397
        float intensity = (eng.overallSize < 50f ? 10.10947f : 151.0947f) * eng.overallSize - 90f; // TODO: temporal solution, revisit
        intensity = intensity <= 0 ? 1f : intensity;                                       // TODO: temporal solution, revisit
        environment.add(new PointLight().set(Color.WHITE, plPosition, intensity < 0 ? 0.5f : intensity)); // syncup: pl
    }

    public void checkMouseMoved(int screenX, int screenY) {
        Ray ray = perspectiveCamera.getPickRay(screenX, screenY);

        Array<HGModelInstance> outCorners = null;
        if (eng.hoveredOverBBMI != null && eng.hoveredOverCornerMIs.size > 0) {
            // we're hovering over some model instance having bounding box rendered as well
            // let's check if we're hovering over a corner of that bounding box:
            outCorners = eng.rayMICollision(ray, eng.hoveredOverCornerMIs, new Array<>(HGModelInstance.class));
            //Gdx.app.debug(getClass().getSimpleName(), "corners: \n" + outCorners.toString("\n"));

            if (outCorners.size > 0 && !outCorners.get(0).equals(eng.hoveredOverCorner)) {
                // we're hovering over the new corner, need to restore the attributes of the previous corner (if any)
                eng.restoreAttributes(eng.hoveredOverCorner, eng.hoveredOverCornerAttributes);
                eng.hoveredOverCorner = outCorners.get(0); // SWITCHING THE HOVERED OVER CORNER
                eng.hoveredOverCornerAttributes = new AttributesMap();
                eng.saveAttributes(eng.hoveredOverCorner, eng.hoveredOverCornerAttributes);
                eng.hoveredOverCorner.setAttributes(new BlendingAttribute(1f));
                return; // nothing else should be done
            } else if (outCorners.size == 0) {
                // we're not hovering over any corners
                eng.restoreAttributes(eng.hoveredOverCorner, eng.hoveredOverCornerAttributes);
                eng.hoveredOverCorner = null;
                if (eng.hoveredOverCornerAttributes != null) { eng.hoveredOverCornerAttributes.clear(); }
                eng.hoveredOverCornerAttributes = null;
            } else if (outCorners.get(0).equals(eng.hoveredOverCorner)) {
                // we're hovering over the same corner, nothing else to do here
                return;
            }
        }

        Array<HGModelInstance> out = eng.rayMICollision(ray, eng.hgMIs, new Array<>(HGModelInstance.class));

        if (out.size > 0 && !out.get(0).equals(eng.hoveredOverMI)) {
            // no need to dispose the box and the corners - will be done in HGModelInstance on dispose()
            eng.auxMIs.clear();
            eng.hoveredOverMI = out.get(0); // SWITCHING THE HOVERED OVER MODEL INSTANCE

            eng.hoveredOverBBMI = eng.hoveredOverMI.getBBHgModelInstance(Color.BLACK);
            eng.auxMIs.add(eng.hoveredOverBBMI);
            eng.hoveredOverCornerMIs = eng.hoveredOverMI.getCornerHgModelInstances(Color.RED);
            eng.auxMIs.addAll(eng.hoveredOverCornerMIs);
        } else if (out.size == 0) {
            eng.hoveredOverMI = null;
            eng.hoveredOverBBMI = null;
            // no need to dispose the box and the corners - will be done in HGModelInstance on dispose()
            eng.auxMIs.clear();
        }
    }

    public void checkTap(float x, float y, int count, int button) {
        Ray ray = perspectiveCamera.getPickRay(x, y);
        switch (button) {
            case Input.Buttons.LEFT:
                Array<HGModelInstance> out = eng.rayMICollision(ray, eng.hgMIs, new Array<>(HGModelInstance.class));
                if (out != null && out.size > 0) {
                    eng.currMI = out.get(0);
                    stage.afterCurrentModelInstanceChanged();
                }
                // TODO: fix the problem of miLabel having the emissive color from hovering
                // (it is not there anymore - just miLabel didn't get updated properly)
                break;
            case Input.Buttons.MIDDLE:
                break;
            case Input.Buttons.RIGHT:
                break;
        }
    }

    public boolean checkPan(float x, float y, float deltaX, float deltaY, int touchDownButton) {
        switch (touchDownButton) {
            case Input.Buttons.LEFT:
                if (eng.hoveredOverMI != null && eng.hoveredOverCorner != null) {
                    Camera cam = perspectiveCamera;

                    Vector3 center = eng.hoveredOverMI.getBB().getCenter(new Vector3());
                    Vector3 corner = eng.hoveredOverCorner.getBB().getCenter(new Vector3());

                    Vector3 coordCenter = cam.project(center.cpy(), 0, 0, cam.viewportWidth, cam.viewportHeight);
                    Vector3 coordCorner = cam.project(corner.cpy(), 0, 0, cam.viewportWidth, cam.viewportHeight);
                    Vector3 coordDelta = new Vector3(deltaX, -deltaY, 0);
                    Vector3 coordHlfDiag = coordCorner.cpy().sub(coordCenter);
                    Vector3 coordDir = coordHlfDiag.cpy().nor();

                    int sign = coordDelta.dot(coordDir) > 0 ? 1 : -1;
                    float scale = sign * 0.02f ;

                    eng.hoveredOverMI.transform.scl(1 + scale);
                    eng.hoveredOverMI.bbHgModelInstanceReset();
                    eng.hoveredOverMI.bbCornersReset();

//                    Gdx.app.debug(getClass().getSimpleName(), ""
//                            + " coordCenter: " + coordCenter + " coordCorner: " + coordCorner
//                            + " coordHlfDiag: " + coordHlfDiag + " coordDir: " + coordDir
//                            + " sign: " + sign
//                            + " x: " + x + " y: " + y
//                            + " deltaX: " + deltaX + " deltaY: " + deltaY
//                            + " coordDir.x: " + coordDir.x + " coordDir.y: " + coordDir.y
//                            + " scale: " + scale
//                            + " cam.viewportWidth: " + cam.viewportWidth + " cam.viewportHeight: " + cam.viewportHeight
//                            + "\nprojtest100: " + cam.project(new Vector3(1,0,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest010: " + cam.project(new Vector3(0,1,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest001: " + cam.project(new Vector3(0,0,1), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest200: " + cam.project(new Vector3(2,0,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest020: " + cam.project(new Vector3(0,2,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest002: " + cam.project(new Vector3(0,0,2), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "cam.combined: \n" + cam.combined
//                    );
                    return false;
                }
            // fall-through
            case Input.Buttons.MIDDLE: // fall-through
            case Input.Buttons.RIGHT:  // fall-through
                return true;
        }
        return true;
    }
}