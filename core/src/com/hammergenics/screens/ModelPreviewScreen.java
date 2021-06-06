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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hammergenics.HGGame;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.screens.graphics.g3d.utils.ModelEditorInputController;
import com.hammergenics.screens.stages.ModelPreviewStage;
import com.hammergenics.utils.LibgdxUtils;

import java.util.Arrays;

import static com.hammergenics.screens.graphics.g3d.utils.Models.createGridModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createLightsModel;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelPreviewScreen extends ScreenAdapter {
    public final HGGame game;
    public final AssetManager assetManager;
    private final ModelBatch modelBatch;

    private PerspectiveCamera perspectiveCamera;
    private ModelEditorInputController modelEditorInputController;
    public Environment environment;
    public Array<HGModel> hgModels = new Array<>();
    private Array<Texture> textures = new Array<>();

    // Auxiliary models:
    private Model gridModel = null;
    private ModelInstance gridXZModelInstance = null;
    private ModelInstance gridYModelInstance = null;
    private ModelInstance gridOModelInstance = null;
    private Model lightsModel = null;
    private Array<ModelInstance> dlArrayModelInstance = null; // directional lights
    private Array<ModelInstance> plArrayModelInstance = null; // point lights
    private Array<ModelInstance> bbArrayModelInstance = null; // bounding boxes

    // 2D Stage - this is where all the widgets (buttons, checkboxes, labels etc.) are located
    public ModelPreviewStage stage;

    // ModelInstance Related:
    public Array<HGModelInstance> hgMIs = new Array<>(HGModelInstance.class);
    public float unitSize = 0f;
    public float overallSize = 0f;
    public HGModelInstance currMI = null;
    public Vector2 currCell = Vector2.Zero.cpy();
    public HGModelInstance hoveredOverMI = null;
    public ArrayMap<Attributes, ColorAttribute> hoveredOverMIAttributes = null;

    private float clockFPS;

    /**
     * @param game
     */
    public ModelPreviewScreen(HGGame game) {
        this.game = game;
        assetManager = game.assetManager;
        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        modelBatch = game.modelBatch;
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
        if (hgModels.size == 0) {
            Gdx.app.error(Thread.currentThread().getStackTrace()[1].getMethodName(), "No models available");
            Gdx.app.exit(); // On iOS this should be avoided in production as it breaks Apples guidelines
            return;
        }

        assetManager.getAll(Texture.class, textures);
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "textures loaded: " + textures.size);

        // Creating the Aux Models beforehand:
        gridModel = createGridModel();
        lightsModel = createLightsModel();

        // Camera related
        perspectiveCamera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        modelEditorInputController = new ModelEditorInputController(this, perspectiveCamera);
        // Environment related
        environment = new Environment();

        // 2D Stage - https://github.com/libgdx/libgdx/wiki/Scene2d.ui#stage-setup
        stage = new ModelPreviewStage(new ScreenViewport(), game, this);

        testRenderRelated();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        // order of addProcessor matter
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(modelEditorInputController);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * @param delta
     */
    @Override
    public void render(float delta) {
        modelEditorInputController.update(delta);

        hgMIs.forEach(hgMI -> {
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
        // * there's a render(...) that could take an Iterable of ModelInstance's, e.g.
        //   modelBatch.render((Array<ModelInstance>) array, environment);
        // * Enable caching as soon as multiple instances are rendered: https://github.com/libgdx/libgdx/wiki/ModelCache
        if (hgMIs.size > 0 && environment != null) { modelBatch.render(hgMIs, environment); }
        if (gridXZModelInstance != null && stage.gridXZCheckBox.isChecked()) {
            modelBatch.render(gridXZModelInstance);
            modelBatch.render(gridOModelInstance);
        }
        if (gridYModelInstance != null && stage.gridYCheckBox.isChecked()) { modelBatch.render(gridYModelInstance); }
        if (dlArrayModelInstance != null && stage.lightsCheckBox.isChecked()) { modelBatch.render(dlArrayModelInstance, environment); }
        if (plArrayModelInstance != null && stage.lightsCheckBox.isChecked()) { modelBatch.render(plArrayModelInstance, environment); }
        if (bbArrayModelInstance != null && stage.bbCheckBox.isChecked()) { modelBatch.render(bbArrayModelInstance, environment); }

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The actual rendering is performed at the call to end();.
        // If you want to force rendering in between, then you can use the modelBatch.flush(); method
        modelBatch.end();

        updateFPS(delta);

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
        if (modelEditorInputController != null) {
            modelEditorInputController.camera.viewportWidth = width;
            modelEditorInputController.camera.viewportHeight = height;
            modelEditorInputController.update(-1f);
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
        if (gridModel != null) { gridModel.dispose(); }
        if (lightsModel != null) { lightsModel.dispose(); }
        for (HGModelInstance mi:hgMIs) { mi.bbModel.dispose(); }
    }

    public void addModelInstances(Array<FileHandle> modelFHs) {
        modelFHs.forEach(fileHandle -> addModelInstance(fileHandle, null, -1, false));

        if (hgMIs.size > 0) {
            currMI = hgMIs.get(0);
            reset();
            stage.reset();
        }
    }

    /**
     * @param assetFL
     */
    public void addModelInstance(FileHandle assetFL, String nodeId, int nodeIndex, boolean resetScreen) {
        HGModel hgModel = new HGModel(assetManager.get(assetFL.path(), Model.class), assetFL);
        if (!hgModel.hasMaterials() && !hgModel.hasMeshes() && !hgModel.hasMeshParts()) {
            if (hgModel.hasAnimations()) {
                // we got animations only model
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "animations only model: " + assetFL);
            }
            return;
        }

        if (nodeId == null) {
            currMI = new HGModelInstance(hgModel, assetFL);
            stage.nodeSelectBox.getColor().set(Color.WHITE);
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
                    stage.nodeSelectBox.getColor().set(Color.PINK);
                    return;
                }
            }
            Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),"nodeId: " + nodeId + " nodeIndex: " + nodeIndex);
            currMI = new HGModelInstance(hgModel, assetFL, nodeId);
            // for some reasons getting this exception in case nodeId == null:
            // (should be done like (String[])null maybe...)
            // Exception in thread "LWJGL Application" java.lang.NullPointerException
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.copyNodes(ModelInstance.java:232)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:155)
            //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
            stage.nodeSelectBox.getColor().set(Color.WHITE);
        }

        currMI.setAttributes(new BlendingAttribute());
        hgMIs.add(currMI);

        if (resetScreen) {
            reset();
            stage.reset();
        }

        // ********************
        // **** ANIMATIONS ****
        // ********************
        copyExternalAnimations(assetFL);

        currMI.animationController = null;
        if(currMI.animations.size > 0) { currMI.animationController = new AnimationController(currMI); }

        // Select Box: Animations
        Array<String> itemsAnimation = new Array<>();
        itemsAnimation.add("No Animation");
        currMI.animations.forEach(a -> itemsAnimation.add(a.id));
        stage.animationSelectBox.clearItems();
        stage.animationSelectBox.setItems(itemsAnimation);

        stage.miLabel.setText(LibgdxUtils.getModelInstanceInfo(currMI));
    }

    /**
     * @param assetFL
     */
    private void copyExternalAnimations(FileHandle assetFL) {
        if (assetManager == null || currMI == null) { return; }

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

    public Vector2 arrangeInSpiral() {
        Vector2 cell = Vector2.Zero.cpy();
        unitSize = 0f;
        for(HGModelInstance hgMI: hgMIs) { if (hgMI.maxD > unitSize) { unitSize = hgMI.maxD; } }
        for(HGModelInstance hgMI: hgMIs) {
            hgMI.transform.idt(); // first cancel any previous transform
            float factor = 1f;
            if (!stage.origScaleCheckBox.isChecked() && hgMI.maxD < unitSize) {
                // Scale: if the dimension of the current instance is less than maximum dimension of all instances scale it
                factor = unitSize/hgMI.maxD;
            }
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

    private void reset() {
        arrangeInSpiral();

        Vector3 center = currMI.getBB().getCenter(Vector3.Zero.cpy());

        resetCamera(overallSize, center.cpy());
        resetScreenInputController(unitSize, overallSize, center.cpy());
        resetEnvironment();    // clears all lights
        addInitialEnvLights(); // adds 1 directional and 1 point light to the environment

        resetGridModelInstances();
        resetLightsModelInstances(center.cpy());
    }

    /**
     * @return
     */
    private void resetGridModelInstances() {
        if (gridModel == null) { return; }

        gridXZModelInstance = new ModelInstance(gridModel, "XZ");
        gridYModelInstance = new ModelInstance(gridModel, "Y");
        gridOModelInstance = new ModelInstance(gridModel, "origin");

        gridXZModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        gridYModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(overallSize/4f));
        gridOModelInstance.transform.setToScaling(Vector3.Zero.cpy().add(unitSize/4f));
    }

    public void resetBBModelInstances() {
        if (bbArrayModelInstance != null) {
            bbArrayModelInstance.clear();
            bbArrayModelInstance = null;
        }
        bbArrayModelInstance = new Array<>(ModelInstance.class);

        for (HGModelInstance mi:hgMIs) {
            if (mi.equals(currMI)) { bbArrayModelInstance.add(mi.getBBModelInstance(Color.GREEN, Color.RED)); }
            else { bbArrayModelInstance.add(mi.getBBModelInstance(Color.BLACK, Color.RED)); }
        }
    }

    public void resetLightsModelInstances(Vector3 center) {
        if (dlArrayModelInstance != null) { dlArrayModelInstance.clear(); dlArrayModelInstance = null; }
        if (plArrayModelInstance != null) { plArrayModelInstance.clear(); plArrayModelInstance = null; }
        dlArrayModelInstance = new Array<>(ModelInstance.class);
        plArrayModelInstance = new Array<>(ModelInstance.class);

        // Environment Lights
        DirectionalLightsAttribute dlAttribute = environment.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
        if (dlAttribute != null) {
            for (DirectionalLight light:dlAttribute.lights) {
                dlArrayModelInstance.add(createDLModelInstance(light, hgMIs.get(0).getBB().getCenter(new Vector3()), overallSize));
            }
        }
        PointLightsAttribute plAttribute = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
        if (plAttribute != null) {
            for (PointLight light:plAttribute.lights) {
                plArrayModelInstance.add(createPLModelInstance(light, hgMIs.get(0).getBB().getCenter(new Vector3()), overallSize));
            }
        }

        // Current Model Instance's Material Lights
        if (currMI != null) {
            for (Material material:currMI.materials) {
                dlAttribute = material.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
                if (dlAttribute != null) {
                    dlAttribute.lights.forEach(light -> dlArrayModelInstance.add(createDLModelInstance(light, center, unitSize)));
                }
                plAttribute = material.get(PointLightsAttribute.class, PointLightsAttribute.Type);
                if (plAttribute != null) {
                    plAttribute.lights.forEach(light -> plArrayModelInstance.add(createPLModelInstance(light, center, unitSize)));
                }
            }
        }
    }

    private ModelInstance createDLModelInstance(DirectionalLight dl, Vector3 passThrough, float distance) {
        ModelInstance mi = new ModelInstance(lightsModel, "directional");
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

    private ModelInstance createPLModelInstance(PointLight pl, Vector3 directTo, float distance) {
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

        ModelInstance mi = new ModelInstance(lightsModel, "point");
        mi.transform.setToTranslationAndScaling(pl.position, Vector3.Zero.cpy().add(distance/10));
        mi.getMaterial("base", true).set(
                dlAttribute, ColorAttribute.createDiffuse(pl.color), ColorAttribute.createEmissive(pl.color)
        );
        return mi;
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
        modelEditorInputController.unitDistance = unitSize;
        modelEditorInputController.overallDistance = overallSize;
        modelEditorInputController.rotateAround.set(rotateAroundVector);
        modelEditorInputController.update(-1f);
    }

    /**
     * @param delta
     */
    private void updateFPS(float delta) {
        clockFPS += delta; // add the time since the last frame
        if (clockFPS > 1) { // every second
            int fps = Gdx.graphics.getFramesPerSecond();
            stage.fpsLabel.setText("FPS: " + fps);
            //Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
            // "time elapsed: " + clockFPS + " seconds passed. FPS = " + fps);
            clockFPS = 0; // reset your variable to 0
        }
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
        Vector3 plPosition = hgMIs.get(0).getBB().getCenter(new Vector3());
        plPosition.add(-overallSize/2, overallSize/2, overallSize/2);
        // seems that intensity should grow exponentially(?) over the distance, the table is:
        //  unitSize: 1.7   17    191    376    522
        // intensity:   1  100  28708  56470  78397
        float intensity = (overallSize < 50f ? 10.10947f : 151.0947f) * overallSize - 90f; // TODO: temporal solution, revisit
        intensity = intensity <= 0 ? 1f : intensity;                                       // TODO: temporal solution, revisit
        environment.add(new PointLight().set(Color.WHITE, plPosition, intensity < 0 ? 0.5f : intensity)); // syncup: pl
    }

    public void checkMouseMoved(int screenX, int screenY) {
        Ray ray = perspectiveCamera.getPickRay(screenX, screenY);

        Array<HGModelInstance> out = rayMICollision(ray, hgMIs, new Array<>(HGModelInstance.class));

        if (out.size > 0 && !out.get(0).equals(hoveredOverMI)) {
            restoreAttributes();
            hoveredOverMI = out.get(0);
            persistAttributes();
            hoveredOverMI.setAttributes(ColorAttribute.createEmissive(Color.DARK_GRAY.cpy()));
        } else if (out.size == 0) {
            restoreAttributes();
            hoveredOverMI = null;
            if (hoveredOverMIAttributes != null) { hoveredOverMIAttributes.clear(); }
            hoveredOverMIAttributes = null;
        }
    }

    private void persistAttributes() {
        if (hoveredOverMI != null) {
            hoveredOverMIAttributes = new ArrayMap<>(Attributes.class, ColorAttribute.class);
            hoveredOverMI.materials.forEach(attributes -> {
                ColorAttribute attr = attributes.get(ColorAttribute.class, ColorAttribute.Emissive);
                if (attr != null) { attr = (ColorAttribute) attr.copy(); }
                hoveredOverMIAttributes.put(attributes, attr);
            });
        }
    }

    private void restoreAttributes() {
        if (hoveredOverMI != null && hoveredOverMIAttributes != null) {
            hoveredOverMI.materials.forEach(attributes -> {
                ColorAttribute attr = hoveredOverMIAttributes.get(attributes);
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "attr: " + attr);
                if (attr != null) { attributes.set(attr); } else { attributes.remove(ColorAttribute.Emissive); }
            });
        }
    }

    public void checkTap(float x, float y, int count, int button) {
        Ray ray = perspectiveCamera.getPickRay(x, y);
        switch (button) {
            case Input.Buttons.LEFT:
                Array<HGModelInstance> out = rayMICollision(ray, hgMIs, new Array<>(HGModelInstance.class));
                out.forEach(mi -> Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "object intersected: " + mi.afh + " @" + mi.hashCode()));
                break;
            case Input.Buttons.MIDDLE:
                break;
            case Input.Buttons.RIGHT:
                break;
        }
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
        final Vector3 camPos = perspectiveCamera.position.cpy();

        for (HGModelInstance mi:modelInstances) {
            Vector3 bbCenter = mi.getBB().getCenter(new Vector3());
            if (Intersector.intersectRayBoundsFast(ray, mi.getBB())) { out.add(mi); }
        }
        Sort.instance().sort(out, (mi1, mi2) -> {
            Vector3 bbc1 = mi1.getBB().getCenter(new Vector3());
            Vector3 bbc2 = mi2.getBB().getCenter(new Vector3());

            if (camPos.cpy().sub(bbc1).len() < camPos.cpy().sub(bbc2).len()) { return -1; }
            if (camPos.cpy().sub(bbc1).len() > camPos.cpy().sub(bbc2).len()) { return 1; }
            return 0;
        });
        return out;
    }

    /**
     *
     */
    public void testRenderRelated() {
        if (currMI == null) { return; }

        // Regarding Pool<T>:
        // see example:
        // AnimationController.animationPool = new Pool<AnimationDesc>() {
        //        @Override
        //        protected AnimationDesc newObject () {
        //            return new AnimationDesc();
        //        }
        // };

        // ********************
        // **** RENDERABLE ****
        // ********************
        // https://github.com/libgdx/libgdx/wiki/ModelBatch#what-are-render-calls

// Renderable init (see ModelBatch.RenderablePool.obtain()):
//     renderable.environment = null;
//     renderable.material = null;
//     renderable.meshPart.set("", null, 0, 0, 0);
//     renderable.shader = null;
//     renderable.userData = null;
// Also see Renderable.java for non pooled newly instantiated objects
//     Matrix4 renderable.bones[]
//     Matrix4 renderable.worldTransform = new Matrix4()

// For ModelInstance: getRenderables returns 1 Renderable per 1 NodePart if nodePart.enabled
// ModelInstance.getRenderable (final Renderable out, final Node node, final NodePart nodePart)
//     renderable.material = material;     ---> from nodePart.material
//     renderable.meshPart.set(meshPart);  ---> from nodePart.material
//     renderable.bones = bones;           ---> from nodePart.material
//        renderable.worldTransform.set(transform).mul(node.globalTransform) ---> if ModelInstance.transform != null and nodePart.bones == 0
//     renderable.worldTransform.set(transform);                          ---> if ModelInstance.transform != null and nodePart.bones != 0
//     renderable.worldTransform.idt(); (identity matrix)                 ---> if ModelInstance.transform == null
//     renderable.userData = userData;     ---> from ModelInstance.userData

// Next, based on the render() method used the following happens:
// In ModelBatch.render (final RenderableProvider renderableProvider, final Environment environment)
//     renderable.environment = environment;
//     renderable.shader = shaderProvider.getShader(renderable);
// In ModelBatch.render (final RenderableProvider renderableProvider, final Environment environment, final Shader shader)
//     renderable.environment = environment;
//     renderable.shader = shader;                               // note this double assignment
//     renderable.shader = shaderProvider.getShader(renderable); // note this double assignment
// ... (see ModelBatch.java for the complete list of render()'s)

        if (currMI.materials == null || currMI.materials.size == 0) { return; }
        // Getting this exception in case there's no material defined.
        // Exception in thread "LWJGL Application" java.lang.IndexOutOfBoundsException: index can't be >= size: 0 >= 0
        //        at com.badlogic.gdx.utils.Array.get(Array.java:155)
        //        at com.badlogic.gdx.graphics.g3d.ModelInstance.getRenderable(ModelInstance.java:366)
        //        at com.badlogic.gdx.graphics.g3d.ModelInstance.getRenderable(ModelInstance.java:361)
        //        at com.hammergenics.screens.ModelPreviewScreen.testRenderRelated(ModelPreviewScreen.java:728)

        // Getting the Renderable
        Renderable renderable = null;
        for (Node node: currMI.nodes) {
            if (node.parts.size == 0) continue;
            renderable = currMI.getRenderable(new Renderable(), node);
            break;
        }
        if (renderable == null) { return; }

        // *****************
        // **** SHADERS ****
        // *****************
        // https://github.com/libgdx/libgdx/wiki/ModelBatch#what-is-a-shader
        // Trying to get Shader from the ModelBatch ShaderProvider
        // IDEA finds 3 classes implementing ShaderProvider:
        // 1. BaseShaderProvider    (abstract)
        // 2. DefaultShaderProvider (by extending BaseShaderProvider) // !!! see DefaultShader.Config config;
        // 3. DepthShaderProvider   (by extending BaseShaderProvider) // !!! see DepthShader.Config config;
        // TODO: add an instanceof check
        DefaultShaderProvider defaultShaderProvider = (DefaultShaderProvider) modelBatch.getShaderProvider();
        DefaultShader defaultShader = (DefaultShader) defaultShaderProvider.getShader(renderable);
        // 1. BaseShaderProvider.getShader(Renderable renderable):
        //    return renderable.shader if renderable.shader.canRender(renderable) <-- double assignment above explained
        //    OR
        //    return BaseShaderProvider.shaders.forEach.shader.canRender(renderable)
        //    OR
        // 2. DefaultShaderProvider.createShader(): new DefaultShader(renderable, DefaultShader.Config config) -->
        // 3. --> DefaultShader (final Renderable renderable, final Config config, final ShaderProgram shaderProgram)
        //    a. getDefaultVertexShader():     defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString()
        //    b. getDefaultFragmentShader(): defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.fragment.glsl").readString()
        // 4. if !shader.canRender(renderable) --> GdxRuntimeException("unable to provide a shader for this renderable");
        // 5. shader.init() --> see DefaultShader.init()

        // LIST OF DefaultShader UNIFORMS:
        // https://github.com/libgdx/libgdx/blob/1.7.0/gdx/src/com/badlogic/gdx/graphics/g3d/shaders/DefaultShader.java#L81-L120

        // keeping this code for now to ease the jumps to the class sources in IDEA:
        if (defaultShaderProvider instanceof DefaultShaderProvider) {}
        if (defaultShaderProvider instanceof BaseShaderProvider) {}
        if (defaultShader instanceof DefaultShader) {}
        if (defaultShader instanceof BaseShader) {}

        // Uncomment to get gen_* files with fields contents:
        //LibGDXUtil.getFieldsContents(defaultShaderProvider.config, 1, "", true);
        //LibGDXUtil.getFieldsContents(defaultShader, 1,  "", true);

        // don't forget to dispose the Shader:
        // DefaultShader.dispose():
        //    program.dispose();
        // BaseShader.dispose():
        //    program = null;
        //    uniforms.clear();
        //    validators.clear();
        //    setters.clear();
        //    localUniforms.clear();
        //    globalUniforms.clear();
        //    locations = null;
        //defaultShader.dispose();
        // causes java.lang.NullPointerException on final ModelBatch.dispose()
        // seems like ModelBatch takes care of disposing this defaultShader

        // ***************************
        // **** RENDERABLE SORTER ****
        // ***************************
        // https://github.com/libgdx/libgdx/wiki/ModelBatch#sorting-render-calls
        // By default ModelBatch will use the DefaultRenderableSorter (code) to sort the render calls.
        // This implementation will cause that opaque objects are rendered first from front to back,
        // after which transparent objects are rendered from back to front. To decide whether an object is transparent or not,
        // the default implementation checks the BlendingAttribute#blended value.
        // https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/RenderableSorter.html
        // https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/DefaultRenderableSorter.html

        // IDEA finds 2 class implementing RenderableSorter:
        // 1. DefaultRenderableSorter
        // 2. ModelCache.Sorter (see https://github.com/libgdx/libgdx/wiki/ModelCache)
        modelBatch.getRenderableSorter();

        // ************************
        // **** RENDER CONTEXT ****
        // ************************
        // https://github.com/libgdx/libgdx/wiki/ModelBatch#managing-the-render-context
        // You can call the modelBatch.ownsRenderContext method to check whether the ModelBatch owns and manages the RenderContext.
        // 1 concrete class: RenderContext
        modelBatch.getRenderContext();
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),"TextureBinder used: "
                + modelBatch.getRenderContext().textureBinder.getClass().getSimpleName());

        // ************************
        // **** TEXTURE BINDER ****
        // ************************
        // https://github.com/libgdx/libgdx/wiki/ModelBatch#texturebinder
        // TextureBinder (code) is an interface used to keep track of texture binds, as well as texture context
        // (e.g. the minification/magnification filters). By default the DefaultTextureBinder (code) is used.
        // https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/TextureBinder.html
        // https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/DefaultTextureBinder.html
        // IDEA finds 1 class implementing TextureBinder: DefaultTextureBinder
        if (modelBatch.getRenderContext().textureBinder instanceof DefaultTextureBinder) {
            DefaultTextureBinder dtb = (DefaultTextureBinder) modelBatch.getRenderContext().textureBinder;
            // Two methods:
            // ROUNDROBIN = 0;
            // LRU = 1;
        }
    }
}