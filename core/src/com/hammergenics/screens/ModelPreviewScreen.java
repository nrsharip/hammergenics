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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hammergenics.HGGame;
import com.hammergenics.stages.ModelPreviewStage;
import com.hammergenics.ui.attributes.BaseAttributeTable;
import com.hammergenics.ui.attributes.BlendingAttributesTable;
import com.hammergenics.ui.attributes.ColorAttributesTable;
import com.hammergenics.ui.attributes.TextureAttributesTable;
import com.hammergenics.util.LibgdxUtils;

import java.util.Arrays;

import static com.badlogic.gdx.Input.Buttons;
import static com.badlogic.gdx.Input.Keys;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage;

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
    private CameraInputController cameraInputController;
    // TODO: IMPORTANT see also FirstPersonCameraController
    private Environment environment;
    public Array<Model> models = new Array<>();
    private Array<Texture> textures = new Array<>();

    private Model gridModel = null;
    private ModelInstance gridXZModelInstance = null;
    private ModelInstance gridYModelInstance = null;

    // 2D Stage - this is where all the widgets (buttons, checkboxes, labels etc.) are located
    public ModelPreviewStage stage;

    // Current ModelInstance Related:
    public ModelInstance modelInstance = null;
    public AnimationController animationController = null;
    public AnimationController.AnimationDesc animationDesc = null;
    public int animationIndex = 0;

    private float clockFPS;

    /**
     * @param game
     */
    public ModelPreviewScreen(HGGame game) {
        this.game = game;
        // Game Class objects:
        // AssetManager
        // ModelBatch
        assetManager = game.assetManager;
        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // ModelBatch is a relatively heavy weight object, because of the shaders it might create.
        modelBatch = game.modelBatch;

        // Getting Assets
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#getting-assets
        // Model.class
        assetManager.getAll(Model.class, models);
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "models loaded: " + models.size);
        if (models.size == 0) {
            Gdx.app.error(Thread.currentThread().getStackTrace()[1].getMethodName(), "No models available");
            Gdx.app.exit(); // On iOS this should be avoided in production as it breaks Apples guidelines
            return;
        }
        // Texture.class
        assetManager.getAll(Texture.class, textures);
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
                "textures loaded: " + textures.size + "\n" + textures.toString("\n"));

        // Camera related
        perspectiveCamera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraInputController = new CameraInputController(perspectiveCamera);

        // 2D Stage
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#stage-setup
        stage = new ModelPreviewStage(new ScreenViewport(), this);
        stage.setup2DStageStyling();
        stage.setup2DStageWidgets();
        stage.setup2DStageLayout();
        setup3DEnvironment();

        stage.envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(environment,"", ""));

        int i = 0;
        while (modelInstance == null && i < models.size) {
            String filename = assetManager.getAssetFileName(models.get(i++));
            switchModelInstance(filename, null, -1);
            if (modelInstance != null) {
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "model selected: " + filename);
            }
        }

        testRenderRelated();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        // order of addProcessor matter
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(cameraInputController);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Uncomment to get gen_* files with fields contents:
//        LibGDXUtil.getFieldsContents(modelInstance, 4, true);
//        LibGDXUtil.getFieldsContents(modelBatch, 4, true);
//        LibGDXUtil.getFieldsContents(model, 4, true);
//        LibGDXUtil.getFieldsContents(environment, 4, true);
//        LibGDXUtil.getFieldsContents(Gdx.gl, 4, true);
    }

    /**
     * @param delta
     */
    @Override
    public void render(float delta) {
        cameraInputController.update();

        if(animationController != null) {
            animationController.update(delta);
//            if (animationDesc.loopCount == 0) {
//                // do something if the animation is over
//            }
        }
        //camera.update(); // done with cameraInputController.update();

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // call glClear etc.
        // GL20 - Interface wrapping all the methods of OpenGL ES 2.0
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
        if (modelInstance != null && environment != null) {
            modelBatch.render(modelInstance, environment);
        }
        if (gridXZModelInstance != null && stage.gridXZCheckBox.isChecked()) {
            modelBatch.render(gridXZModelInstance);
        }
        if (gridYModelInstance != null && stage.gridYCheckBox.isChecked()) {
            modelBatch.render(gridYModelInstance);
        }

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The actual rendering is performed at the call to end();.
        // If you want to force rendering in between, then you can use the modelBatch.flush(); method
        modelBatch.end();

        checkFPS(delta);

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
        if (cameraInputController != null) {
            cameraInputController.camera.viewportWidth = width;
            cameraInputController.camera.viewportHeight = height;
            // camera should be updated explicitly since CameraInputController updates camera only on:
            // rotateRightPressed || rotateLeftPressed || forwardPressed || backwardPressed
            cameraInputController.camera.update();
            cameraInputController.update();
        }
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }

        // FIXME: putting this attributes code here for now to trigger on resize.
//        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                "Attribute Aliases registered so far:\n"
//                        + LibGDXUtil.getRegisteredAttributeAliases().toString("\n"));
    }

    /**
     *
     */
    @Override
    public void dispose() {
        super.dispose();
        if (stage != null) {
            stage.dispose();
        }
        if (gridModel != null) {
            gridModel.dispose();
        }
    }

    /**
     * @param assetName
     */
    public void switchModelInstance(String assetName, String nodeId, int nodeIndex) {
        // TODO: add checks for null perspectiveCamera, cameraInputController, and the size of models

        Model model = assetManager.get(assetName, Model.class);

        modelInstance = null;

        if (model.materials.size == 0 && model.meshes.size == 0 && model.meshParts.size == 0) {
            if (model.animations.size > 0) {
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "animations only model: " + assetName);
            }
            return; // we got animations only model
        }

        if (model.nodes.size != 0 && nodeId == null) { // switching the whole asset
            stage.nodeSelectBox.clearItems();

            String array1[] = Arrays.stream(model.nodes.toArray(Node.class)).map(n->n.id).toArray(String[]::new);
            String array2[] = new String[array1.length + 1];
            System.arraycopy(array1, 0, array2, 1, array1.length);
            array2[0] = "All";

            stage.nodeSelectBox.setItems(array2);
        }

        // see: ModelBuilder() - https://libgdx.badlogicgames.com/ci/nightlies/dist/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html
        if (nodeId == null) {
            modelInstance = new ModelInstance(model);
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

            for (NodePart part:model.nodes.get(nodeIndex).parts) {
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
                    modelInstance = new ModelInstance(model);
                    stage.nodeSelectBox.getColor().set(Color.PINK);
                    break;
                }
            }

            if (modelInstance == null) {
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),"nodeId: " + nodeId + " nodeIndex: " + nodeIndex);
                //modelInstance = new ModelInstance(model);
                modelInstance = new ModelInstance(model, nodeId);
                stage.nodeSelectBox.getColor().set(Color.WHITE);
                // for some reasons getting this exception in case nodeId == null:
                // (should be done like (String[])null maybe...)
                // Exception in thread "LWJGL Application" java.lang.NullPointerException
                //        at com.badlogic.gdx.graphics.g3d.ModelInstance.copyNodes(ModelInstance.java:232)
                //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:155)
                //        at com.badlogic.gdx.graphics.g3d.ModelInstance.<init>(ModelInstance.java:145)
            }
        }

        // *********************************
        // **** ModelInstance.transform ****
        // *********************************
//        modelInstance.transform.setToTranslation(float x, float y, float z) - changes the position
//        modelInstance.transform.setToTranslation(Vector3 position) - changes the position
//        modelInstance.transform.setToScaling();
//        see Matrix4 for all operations available...
        modelInstance.transform.setToTranslation(0, 0, 0);

        // ********************
        // **** ATTRIBUTES ****
        // ********************
        stage.textureImage.setDrawable(null);

        if (modelInstance.materials != null && modelInstance.materials.size > 0) {
            BaseAttributeTable.EventListener eventListener = new BaseAttributeTable.EventListener() {
                @Override
                public void onAttributeEnabled(long type, String alias) {
                    stage.miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
                }

                @Override
                public void onAttributeDisabled(long type, String alias) {
                    stage.miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
                }

                @Override
                public void onAttributeChange(long type, String alias) {
                    stage.miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
                }
            };

            stage.textureAttrTable = new TextureAttributesTable(stage.skin, modelInstance.materials.get(0), this);
            stage.colorAttrTable = new ColorAttributesTable(stage.skin, modelInstance.materials.get(0), this);
            stage.blendingAttrTable = new BlendingAttributesTable(stage.skin, modelInstance.materials.get(0), this);

            stage.textureAttrTable.setListener(eventListener);
            stage.colorAttrTable.setListener(eventListener);
            stage.blendingAttrTable.setListener(eventListener);

            stage.textureAttrTable.resetAttributes();
            stage.colorAttrTable.resetAttributes();
            stage.blendingAttrTable.resetAttributes();

            stage.attrTable.clear();
            stage.attrTable.add(stage.colorAttrTable).padTop(20f).top().left().fillX();
            stage.attrTable.row();
            stage.attrTable.add(stage.textureAttrTable).padTop(20f).top().left().fillX();
            stage.attrTable.row();
            stage.attrTable.add(stage.blendingAttrTable).padTop(20f).top().left().fillX();
            stage.attrTable.row();
            stage.attrTable.add().expandY();
        }

        copyExternalAnimations(assetName);

        // FIXME: calculateBoundingBox is a slow operation - BoundingBox object should be cached
        BoundingBox bb = new BoundingBox();
        modelInstance.calculateBoundingBox(bb);
        Vector3 dimensions = bb.getDimensions(new Vector3());
        Vector3 center = bb.getCenter(new Vector3());
        float D = Math.max(Math.max(dimensions.x,dimensions.y),dimensions.z);

        createGridModel(D);
        resetCamera(D, center);
        resetCameraInputController(D, center);

        animationController = null;
        if(modelInstance.animations.size > 0) {
            animationController = new AnimationController(modelInstance);

            // Uncomment to get gen_* files with fields contents:
            //LibGDXUtil.getFieldsContents(animationController, 2,  "", true);
        }

        // Select Box: Animations
        Array<String> itemsAnimation = new Array<>();
        itemsAnimation.add("No Animation");
        modelInstance.animations.forEach(a -> itemsAnimation.add(a.id));
        stage.animationSelectBox.clearItems();
        stage.animationSelectBox.setItems(itemsAnimation);

        stage.miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
        // Uncomment to get gen_* files with fields contents:
        //LibGDXUtil.getFieldsContents(perspectiveCamera, 2, "", true);
        //LibGDXUtil.getFieldsContents(cameraInputController, 2,  "", true);
    }

    /**
     * @param assetName
     */
    private void copyExternalAnimations(String assetName) {
        if (assetManager == null || modelInstance == null) { return; }

        FileHandle animationsFolder = LibgdxUtils.fileOnPath(Gdx.files.local(assetName), "animations");
        if (animationsFolder != null && animationsFolder.isDirectory()) {
            // final since it goes to lambda closure
            final Array<String> animationsPresent = new Array<>();
            // populating with animations already present
            for (Animation animation : modelInstance.animations) { animationsPresent.add(animation.id); }

            for (Model m: models) {
                String filename = assetManager.getAssetFileName(m);
                if (filename.startsWith(animationsFolder.toString())) {
//                    Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                            LibgdxUtils.getFieldsContents(m, 0));

                    if (m.materials.size != 0) {
                        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
                                "WARNING: animation only model has materials (" + m.materials.size+ "): " + filename);
                    }
                    if (m.meshes.size != 0) {
                        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
                                "WARNING: animation only model has meshes (" + m.meshes.size+ "): " + filename);
                    }
                    if (m.meshParts.size != 0) {
                        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
                                "WARNING: animation only model has meshParts (" + m.meshParts.size+ "): " + filename);
                    }

                    //modelInstance.copyAnimations(m.animations);
                    m.animations.forEach(animation -> {
                        //Gdx.app.debug(Thread.currentThread().getStackTrace()[3].getMethodName(),
                        // "animation: " + animation.id);
                        // this is to make sure that we don't add the same animation multiple times
                        // from different animation models
                        if (!animationsPresent.contains(animation.id, false)) {
                            Gdx.app.debug(Thread.currentThread().getStackTrace()[3].getMethodName(),
                                    "adding animation: " + animation.id);
                            modelInstance.copyAnimation(animation);
                            animationsPresent.add(animation.id);
                        }
                    });
                }
            }
        }
    }

    /**
     * @return
     */
    private void createGridModel(float D) {
        // IMPORTANT
        if (gridModel != null) {
            gridModel.dispose();
            gridModel = null;
            gridXZModelInstance = null;
            gridYModelInstance = null;
        }

        //int step = (int) Math.pow (10, (int) (Math.log10(D))) ; // 10 ^ (number of digits in D - 1)
        float step = D / 5;
        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),"grid step: " + step + " for max dimension: " + D);

        // see: ModelBuilder()
        // https://libgdx.badlogicgames.com/ci/nightlies/dist/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html
        ModelBuilder mb = new ModelBuilder();
        MeshPartBuilder mpb;

        mb.begin();

        mb.node().id = "XZ"; // adding node XZ
        // MeshPart "XZ"
        // see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
        mpb = mb.part("XZ", GL20.GL_LINES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
        for (float pos = -1000 * step; pos < 1000 * step; pos += step ) {
            // see implementation: Add a line. Requires GL_LINES primitive type.
            mpb.line(-1000 * step, 0,          pos, 1000 * step, 0,         pos); // along X-axis
            mpb.line(         pos, 0, -1000 * step,         pos, 0, 1000 * step); // along Z-axis
        }

        mb.node().id = "Y"; // adding node Y
        // MeshPart "Y"
        // see for primitive types: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glBegin.xml
        mpb = mb.part("Y", GL20.GL_LINES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)));
        for (float x = -1000 * step; x < 1000 * step; x += 20 * step ) {
            for (float z = -1000 * step; z < 1000 * step; z += 20 * step ) {
                // see implementation: Add a line. Requires GL_LINES primitive type.
                mpb.line(x, -10 * step, z, x, 10 * step, z); // along Y-axis
                // Exception in thread "LWJGL Application" com.badlogic.gdx.utils.GdxRuntimeException: Too many vertices used
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.vertex(MeshBuilder.java:547)
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.vertex(MeshBuilder.java:590)
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.line(MeshBuilder.java:657)
                //        at com.badlogic.gdx.graphics.g3d.utils.MeshBuilder.line(MeshBuilder.java:667)
                //        at com.hammergenics.screens.ModelPreviewScreen.createGridModel(ModelPreviewScreen.java:605)
            }
        }

        // see also com.badlogic.gdx.graphics.g3d.utils.shapebuilders:
        //  ArrowShapeBuilder
        //  BaseShapeBuilder
        //  BoxShapeBuilder
        //  CapsuleShapeBuilder
        //  ConeShapeBuilder
        //  CylinderShapeBuilder
        //  EllipseShapeBuilder
        //  FrustumShapeBuilder
        //  PatchShapeBuilder
        //  RenderableShapeBuilder
        //  SphereShapeBuilder
        gridModel = mb.end();
        gridXZModelInstance = new ModelInstance(gridModel, "XZ");
        gridYModelInstance = new ModelInstance(gridModel, "Y");
//        Gdx.app.log(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                "GRID model instance: " + LibgdxUtils.getModelInstanceInfo(gridModelInstance));
    }

    /**
     * @param D
     */
    private void resetCamera(float D, Vector3 c) {
        // IDEA finds 2 classes extending Camera:
        // 1. OrthographicCamera
        // 2. PerspectiveCamera

        // Camera Articles:
        // https://github.com/libgdx/libgdx/wiki/Orthographic-camera
        // https://stackoverflow.com/questions/54198655/how-camera-works-in-libgdx-and-together-with-viewport
        // https://gamefromscratch.com/libgdx-tutorial-7-camera-basics/
        // https://gamefromscratch.com/libgdx-tutorial-part-16-cameras/
        // TODO: need to visually debug this as well as switching between Orthographic and Perspective cameras (?)
        perspectiveCamera.fieldOfView = 70f;                       // PerspectiveCamera: float fieldOfView
                                                                   //                    the field of view of the height, in degrees
        perspectiveCamera.position.set(c.x + D, c.y + D, c.z + D); // Camera: Vector3 position
        perspectiveCamera.direction.set(0, 0, -1);                 // Camera: Vector3 direction
        perspectiveCamera.up.set(0, 1, 0);                         // Camera: Vector3 up
        perspectiveCamera.lookAt(c.x, c.y, c.z);                   //   camera.up and camera.direction must
                                                                   //   ALWAYS be orthonormal vectors
        //perspectiveCamera.projection;                            // Camera: Matrix4 projection
        //perspectiveCamera.view;                                  // Camera: Matrix4 view
        //perspectiveCamera.combined;                              // Camera: Matrix4 combined
        //perspectiveCamera.invProjectionView;                     // Camera: Matrix4 invProjectionView
        perspectiveCamera.near = Math.min(1f, D/10);               // Camera: float near
        perspectiveCamera.far = 10*D;                              // Camera: float far
        //perspectiveCamera.viewportWidth;                         // Camera: float viewportWidth
        //perspectiveCamera.viewportHeight;                        // Camera: float viewportHeight
        //perspectiveCamera.frustum;                               // Camera: Frustum frustum
                                                                   //         A truncated rectangular pyramid.
                                                                   //         Used to define the viewable region and
                                                                   //         its projection onto the screen
        //perspectiveCamera.frustum.planes;                        // Frustum: Plane[] planes
                                                                   //          the six clipping planes:
                                                                   //          near, far, left, right, top, bottom
        //perspectiveCamera.frustum.planePoints;                   // Frustum: Vector3[] planePoints
                                                                   //          eight points making up the near and far clipping "rectangles".
                                                                   //          order is counter clockwise, starting at bottom left
        // See also:
        // *   frustum culling : https://en.wikipedia.org/wiki/Hidden-surface_determination#Viewing-frustum_culling
        // * back-face culling : https://en.wikipedia.org/wiki/Back-face_culling
        perspectiveCamera.update();
    }

    /**
     * @param D
     */
    private void resetCameraInputController(float D, Vector3 c) {
        // Uncomment to get gen_* files with fields contents:
        //LibGDXUtil.getFieldsContents(cameraInputController, 1,  "", true);

        // Setting most of cameraInputController to defaults, except for scrollFactor and translateUnits.
        // These are calculated based on the model's dimensions
        cameraInputController.rotateButton = Buttons.LEFT;     //     int rotateButton    = 0
        cameraInputController.rotateAngle = 360f;              //   float rotateAngle     = 360.0
        cameraInputController.translateButton = Buttons.RIGHT; //     int translateButton = 1
        cameraInputController.translateUnits = 2*D;            //   float translateUnits  = 188.23 // "right button speed"
        cameraInputController.forwardButton = Buttons.MIDDLE;  //     int forwardButton   = 2
        cameraInputController.activateKey = 0;                 //     int activateKey     = 0
        cameraInputController.alwaysScroll = true;             // boolean alwaysScroll    = true
        cameraInputController.scrollFactor = -0.05f;           //   float scrollFactor    = -0.2 // "zoom speed"
        cameraInputController.pinchZoomFactor = 10f;           //   float pinchZoomFactor = 10.0
        cameraInputController.autoUpdate = true;               // boolean autoUpdate      = true
        cameraInputController.target.set(c.x, c.y, c.z);       // Vector3 target          = (0.0,0.0,0.0)
        cameraInputController.translateTarget = true;          // boolean translateTarget = true
        cameraInputController.forwardTarget = true;            // boolean forwardTarget   = true
        cameraInputController.scrollTarget = false;            // boolean scrollTarget    = false
        cameraInputController.forwardKey = Keys.W;             //     int forwardKey      = 51
        cameraInputController.backwardKey = Keys.S;            //     int backwardKey     = 47
        cameraInputController.rotateRightKey = Keys.A;         //     int rotateRightKey  = 29
        cameraInputController.rotateLeftKey = Keys.D;          //     int rotateLeftKey   = 32
        //cameraInputController.camera = ;
        cameraInputController.update();
    }

    /**
     * @param delta
     */
    private void checkFPS(float delta) {
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
    public void setup3DEnvironment() {
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

        environment = new Environment();
        // Some attribute classes are dedicated to a single type value (bit).
        // Others can be used for multiple type values (bits), in which case you must specify the type on construction.
        environment.set(ColorAttribute.createAmbient(Color.GRAY));            // min enabled
//        environment.set(ColorAttribute.createDiffuse(Color.GRAY));          // ! darkens the model
        environment.set(ColorAttribute.createSpecular(Color.GRAY));           // min enabled
        environment.set(ColorAttribute.createReflection(Color.GRAY));         // min enabled
//        environment.set(ColorAttribute.createEmissive(Color.GRAY));         // ! adds glowing effect  // JdxLib v1.10.0
        environment.set(ColorAttribute.createAmbientLight(Color.DARK_GRAY));  // min enabled            // JdxLib v1.10.0
        environment.set(ColorAttribute.createFog(Color.GRAY));                // min enabled            // JdxLib v1.10.0

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
        environment.add(new DirectionalLight().set(Color.WHITE, -0.7f, -0.5f, -0.3f));  // min enabled
        environment.add(new PointLight().set(Color.LIGHT_GRAY, 50f, 50f, 50f, 0.5f));   // min enabled
        environment.add(new SpotLight());                                               // min enabled
    }

    /**
     *
     */
    public void testRenderRelated() {
        if (modelInstance == null) { return; }

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

        if (modelInstance.materials == null || modelInstance.materials.size == 0) { return; }
        // Getting this exception in case there's no material defined.
        // Exception in thread "LWJGL Application" java.lang.IndexOutOfBoundsException: index can't be >= size: 0 >= 0
        //        at com.badlogic.gdx.utils.Array.get(Array.java:155)
        //        at com.badlogic.gdx.graphics.g3d.ModelInstance.getRenderable(ModelInstance.java:366)
        //        at com.badlogic.gdx.graphics.g3d.ModelInstance.getRenderable(ModelInstance.java:361)
        //        at com.hammergenics.screens.ModelPreviewScreen.testRenderRelated(ModelPreviewScreen.java:728)

        // Getting the Renderable
        Renderable renderable = null;
        for (Node node:modelInstance.nodes) {
            if (node.parts.size == 0) continue;
            renderable = modelInstance.getRenderable(new Renderable(), node);
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