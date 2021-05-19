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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hammergenics.HGGame;
import com.hammergenics.config.Config;
import com.hammergenics.util.LibgdxUtils;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelPreviewScreen extends ScreenAdapter {
    private final HGGame game;
    private final AssetManager assetManager;
    private final ModelBatch modelBatch;

    private PerspectiveCamera perspectiveCamera;
    private CameraInputController cameraInputController;
    private Environment environment;
    private Array<Model> models = new Array<>();
    private Array<Texture> textures = new Array<>();

    // 2D Stage - this is where all widgets (buttons, checkboxes, labels etc.) are located
    private Stage stage;

    // 2D Stage Styling:
    private Skin skin;
    private BitmapFont labelBitmapFont;
    private Label.LabelStyle labelStyle;

    // 2D Stage Widgets:
    private Table rootTable;
    private Label miLabel;  // Model Instance Info
    private Label envLabel; // Environment Info
    private Label fpsLabel; // FPS Info
    private Image textureImage;
    private CheckBox debugStageCheckBox;
    private SelectBox<String> modelSelectBox;
    private SelectBox<String> animationSelectBox = null;
    private SelectBox<String> textureSelectBox = null;

    // Current ModelInstance Related:
    private ModelInstance modelInstance;
    private AnimationController animationController = null;
    private AnimationController.AnimationDesc animationDesc = null;
    private int animationIndex = 0;

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
        // When possible you should try to reuse it.
        modelBatch = game.modelBatch;

        // Getting Assets
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#getting-assets

        // Model.class
        assetManager.getAll(Model.class, models);
        Gdx.app.debug(getClass().getSimpleName(),"models loaded: " + models.size);
        if (models.size == 0) {
            Gdx.app.error(getClass().getSimpleName(), "No models available");
            Gdx.app.exit(); // On iOS this should be avoided in production as it breaks Apples guidelines
            return;
        }
        // Texture.class
        assetManager.getAll(Texture.class, textures);
        Gdx.app.debug(getClass().getSimpleName(),"textures loaded: " + textures.size);

        // Camera related
        perspectiveCamera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraInputController = new CameraInputController(perspectiveCamera);

        // 2D Stage
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#stage-setup
        stage = new Stage(new ScreenViewport());

        setup2DStageStyling();
        setup2DStageWidgets();
        setup2DStageLayout();
        setup3DEnvironment();

        switchModelInstance(assetManager.getAssetFileName(models.get(0)));

        envLabel.setText("Environment:\n" + LibgdxUtils.extractAttributes(environment,"", ""));

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
        // glClear takes a single argument that is the bitwise OR
        // of several values indicating which buffer is to be cleared. The values are as follows:
        // GL_COLOR_BUFFER_BIT   - Indicates the buffers currently enabled for color writing.
        // (https://stackoverflow.com/questions/34164309/gl-color-buffer-bit-regenerating-which-memory)
        // GL_DEPTH_BUFFER_BIT   - Indicates the depth buffer.
        // GL_ACCUM_BUFFER_BIT   - Indicates the accumulation buffer.
        // GL_STENCIL_BUFFER_BIT - Indicates the stencil buffer.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The Camera you supply is hold by reference, meaning that it must not be changed in between
        // the begin and end calls. If you need to switch camera in between the begin and end calls,
        // then you can call the modelBatch.setCamera(camera);, which will flush() the batch if needed.
        modelBatch.begin(perspectiveCamera);

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The call to modelBatch.render(...) is only valid in between
        // the call to modelBatch.begin(camera) and modelBatch.end()

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

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // The actual rendering is performed at the call to end();.
        // If you want to force rendering in between, then you can use the modelBatch.flush(); method
        modelBatch.end();

        checkFPS(delta);

        stage.act(delta);
//        stage.getBatch().begin();
//        stage.getBatch().draw(textures.get(0), 0f, 0f);
//        stage.getBatch().end();
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
//        Gdx.app.debug(getClass().getSimpleName(),
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
    }

    /**
     * @param assetName
     */
    private void switchModelInstance(String assetName) {
        // TODO: add checks for null perspectiveCamera, cameraInputController, and the size of models

        modelInstance = null;
        if (assetName.toLowerCase().contains("animations")) { return; } // we got animations only model

        // see: ModelBuilder() - https://libgdx.badlogicgames.com/ci/nightlies/dist/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html
        modelInstance = new ModelInstance(assetManager.get(assetName, Model.class));
        // see other useful constructors:
        // new ModelInstance (model, final String nodeId, boolean mergeTransform...
        // new ModelInstance (model, final String... rootNodeIds
        // new ModelInstance (model, final Array<String> rootNodeIds...
        // new ModelInstance (model, float x, float y, float z ---> x,y,z - position
        // new ModelInstance (model, Matrix4 transform
        // new ModelInstance (ModelInstance copyFrom...

        // see also other useful methods:
//        modelInstance.calculateBoundingBox(out); - Calculate the bounding box of this model instance.
//                                                   !!! This is a potential slow operation, it is advised to cache the result.
//        modelInstance.calculateTransforms();     - Calculates the local and world transform of all Node instances in this model, recursively.
//                                                   This method can be used to recalculate all transforms if any of
//                                                   the Node's local properties (translation, rotation, scale) was modified.
//        modelInstance.extendBoundingBox(out);    - Extends the bounding box with the bounds of this model instance.
//                                                   !!! This is a potential slow operation, it is advised to cache
//        modelInstance.copy();
//        modelInstance.copyAnimation...
//        modelInstance.getNode (final String id)                                         ---> getNode(id, true)
//        modelInstance.getNode (final String id, boolean recursive)                      ---> getNode(id, recursive, false)
//        modelInstance.getNode (final String id, boolean recursive, boolean ignoreCase)  ---> Node.getNode(nodes, id, recursive, ignoreCase)

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

        // This is the place where the attributes are coming from (see Model.java):
        // see new Model (modelData, textureProvider) -> load (...) -> loadMaterials (...) ->
        //     -> convertMaterial(ModelMaterial mtl, TextureProvider textureProvider):
        // Material result = new Material();
        // if (mtl.ambient != null) result.set(new ColorAttribute(ColorAttribute.Ambient, mtl.ambient));
        // if (mtl.diffuse != null) result.set(new ColorAttribute(ColorAttribute.Diffuse, mtl.diffuse));
        // ...
        // if (mtl.textures != null) {
        //   for (ModelTexture tex : mtl.textures) {
        //     Texture texture = textureProvider.load(tex.fileName);
        //
        //     TextureDescriptor descriptor = new TextureDescriptor(texture) ---> ... ---> set(...)
        //          this.texture = texture;
        //          this.minFilter = <null>;
        //          this.magFilter = <null>;
        //          this.uWrap = <null>;
        //          this.vWrap = <null>;
        //     descriptor.minFilter = texture.getMinFilter();
        //     descriptor.magFilter = texture.getMagFilter();
        //     descriptor.uWrap = texture.getUWrap();
        //     descriptor.vWrap = texture.getVWrap();
        //     float offsetU = (tex.uvTranslation == null) ? 0f : tex.uvTranslation.x;
        //     float offsetV = (tex.uvTranslation == null) ? 0f : tex.uvTranslation.y;
        //     float scaleU = (tex.uvScaling == null) ? 1f : tex.uvScaling.x;
        //     float scaleV = (tex.uvScaling == null) ? 1f : tex.uvScaling.y;
        //
        //     switch (tex.usage) {
        //     case ModelTexture.USAGE_DIFFUSE:
        //          result.set(new TextureAttribute(TextureAttribute.Diffuse, descriptor, offsetU, offsetV, scaleU, scaleV));
        //          break;
        //     ...
        //   }
        // }
        // return result;

        Array<String> itemsTexture = new Array<>();
        itemsTexture.add("No Texture");

        textureImage.setDrawable(null);
        if (modelInstance.materials != null && modelInstance.materials.size > 0) {
//            modelInstance.materials.get(0).get(ColorAttribute.class, ColorAttribute.Diffuse).color.set(Color.DARK_GRAY);
//            modelInstance.materials.get(0).get(ColorAttribute.class, ColorAttribute.Specular).color.set(Color.DARK_GRAY);
//            modelInstance.materials.get(0).get(ColorAttribute.class, ColorAttribute.Ambient).color.set(Color.DARK_GRAY);
//            modelInstance.materials.get(0).get(ColorAttribute.class, ColorAttribute.Emissive).color.set(Color.DARK_GRAY);
//            modelInstance.materials.get(0).get(FloatAttribute.class, FloatAttribute.Shininess).value /= 10;

            modelInstance.materials.get(0).remove(ColorAttribute.Diffuse);     // ! darkens the model
//            modelInstance.materials.get(0).remove(ColorAttribute.Specular);  // min enabled
//            modelInstance.materials.get(0).remove(ColorAttribute.Ambient);   // min enabled
            modelInstance.materials.get(0).remove(ColorAttribute.Emissive);    // ! adds glowing effect
//            modelInstance.materials.get(0).remove(FloatAttribute.Shininess); // min enabled

            TextureAttribute textureDiffuse = modelInstance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse);
            if (textureDiffuse != null) {
                // scaling seems to zoom in/out the texture image towards the mesh
//                modelInstance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse).scaleU = 10;
//                modelInstance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse).scaleV = 10;
//                modelInstance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse).offsetU = 100;
//                modelInstance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse).offsetV = 100;

                // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#image
                // https://libgdx.info/basic_image/
                // see image constructors starting Image (Texture texture) for Texture to Drawable translation:
                //Gdx.app.debug(getClass().getSimpleName(), "Setting Texture Image for: " + textureDiffuse.textureDescription.texture);
                textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureDiffuse.textureDescription.texture)));
                itemsTexture.add(textureDiffuse.textureDescription.texture.toString());
            }
        }

        // Select Box: Textures
        FileHandle assetFileHandle = Gdx.files.local(assetName);
        // All PNG files in the same directory and direct subdirecories the asset is located
        Array<FileHandle> textureFileHandleArray;
        textureFileHandleArray = LibgdxUtils.traversFileHandle(assetFileHandle.parent(),
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
        );

        // TODO: Add unified convention like "textures | skins" to specify all folders at once
        // All PNG files in the "textures" directory and subdirectories (if any) on asset's path
        textureFileHandleArray = LibgdxUtils.traversFileHandle(
                LibgdxUtils.fileOnPath(assetName, "textures"),
                textureFileHandleArray,
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
        );
        // All PNG files in the "skins" directory and subdirectories (if any) on asset's path
        textureFileHandleArray = LibgdxUtils.traversFileHandle(
                LibgdxUtils.fileOnPath(assetName, "skins"),
                textureFileHandleArray,
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
        );

        if (textureFileHandleArray.size > 0) {
            Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "adding textures: \n"
                    + textureFileHandleArray.toString("\n"));
            itemsTexture.addAll(textureFileHandleArray.toString(";").split(";"));
        }

        textureSelectBox.clearItems();
        textureSelectBox.setItems(itemsTexture);

        copyExternalAnimations(assetName);

        // FIXME: calculateBoundingBox is a slow operation - BoundingBox object should be cached
        Vector3 dimensions = modelInstance.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3());
        float D = Math.max(Math.max(dimensions.x,dimensions.y),dimensions.z);

        updateCamera(D);
        updateCameraInputController(D);

        animationController = null;
        if(modelInstance.animations.size > 0) {
            animationController = new AnimationController(modelInstance);
            // AnimationController extends BaseAnimationController

            // AnimationDesc current               = null
            // AnimationDesc queued                = null
            //         float queuedTransitionTime  = 0.0
            // AnimationDesc previous              = null
            //         float transitionCurrentTime = 0.0
            //         float transitionTargetTime  = 0.0
            //       boolean inAction              = false
            //       boolean paused                = false
            //       boolean allowSameAnimation    = false
            // ModelInstance target                         // BaseAnimationController

            // Uncomment to get gen_* files with fields contents:
            //LibGDXUtil.getFieldsContents(animationController, 2,  "", true);
        }

        // Select Box: Animations
        Array<String> itemsAnimation = new Array<>();
        itemsAnimation.add("No Animation");
        modelInstance.animations.forEach(a -> itemsAnimation.add(a.id));
        animationSelectBox.clearItems();
        animationSelectBox.setItems(itemsAnimation);

        miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
        // Uncomment to get gen_* files with fields contents:
        //LibGDXUtil.getFieldsContents(perspectiveCamera, 2, "", true);
        //LibGDXUtil.getFieldsContents(cameraInputController, 2,  "", true);
    }

    /**
     * @param assetName
     */
    private void copyExternalAnimations(String assetName) {
        if (assetManager == null || modelInstance == null) { return; }

        FileHandle animationsFolder = LibgdxUtils.fileOnPath(assetName, "animations");
        if (animationsFolder != null && animationsFolder.isDirectory()) {
            // final since it goes to lambda closure
            final Array<String> animationsPresent = new Array<>();
            // populating with animations already present
            for (Animation animation : modelInstance.animations) { animationsPresent.add(animation.id); }

            for (Model m: models) {
                String filename = assetManager.getAssetFileName(m);
                if (filename.startsWith(animationsFolder.toString())) {
                    //modelInstance.copyAnimations(m.animations);
                    m.animations.forEach(animation -> {
                        //Gdx.app.debug(getClass().getSimpleName(), "animation: " + animation.id);
                        // this is to make sure that we don't add the same animation multiple times
                        // from different animation models
                        if (!animationsPresent.contains(animation.id, false)) {
                            Gdx.app.debug(getClass().getSimpleName(), "adding animation: " + animation.id);
                            modelInstance.copyAnimation(animation);
                            animationsPresent.add(animation.id);
                        }
                    });
                }
            }
        }
    }

    /**
     * @param D
     */
    private void updateCamera(float D) {
        // IDEA finds 2 classes extending Camera:
        // 1. OrthographicCamera
        // 2. PerspectiveCamera

        // Camera Articles:
        // https://github.com/libgdx/libgdx/wiki/Orthographic-camera
        // https://stackoverflow.com/questions/54198655/how-camera-works-in-libgdx-and-together-with-viewport
        // https://gamefromscratch.com/libgdx-tutorial-7-camera-basics/
        // https://gamefromscratch.com/libgdx-tutorial-part-16-cameras/
        // TODO: need to visually debug this as well as switching between Orthographic and Perspective cameras (?)
        perspectiveCamera.fieldOfView = 70f;         // PerspectiveCamera: float fieldOfView
                                                     //                    the field of view of the height, in degrees
        perspectiveCamera.position.set(D, D, D);     // Camera: Vector3 position
        perspectiveCamera.lookAt(0,0,0);             // Camera: Vector3 direction
                                                     //         camera.up and camera.direction must
                                                     //         ALWAYS be orthonormal vectors
        //perspectiveCamera.up;                      // Camera: Vector3 up
        //perspectiveCamera.projection;              // Camera: Matrix4 projection
        //perspectiveCamera.view;                    // Camera: Matrix4 view
        //perspectiveCamera.combined;                // Camera: Matrix4 combined
        //perspectiveCamera.invProjectionView;       // Camera: Matrix4 invProjectionView
        perspectiveCamera.near = Math.min(1f, D/10); // Camera: float near
        perspectiveCamera.far = 5*D;                 // Camera: float far
        //perspectiveCamera.viewportWidth;           // Camera: float viewportWidth
        //perspectiveCamera.viewportHeight;          // Camera: float viewportHeight
        //perspectiveCamera.frustum;                 // Camera: Frustum frustum
                                                     //         A truncated rectangular pyramid.
                                                     //         Used to define the viewable region and
                                                     //         its projection onto the screen
        //perspectiveCamera.frustum.planes;          // Frustum: Plane[] planes
                                                     //          the six clipping planes:
                                                     //          near, far, left, right, top, bottom
        //perspectiveCamera.frustum.planePoints;     // Frustum: Vector3[] planePoints
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
    private void updateCameraInputController(float D) {
        // Uncomment to get gen_* files with fields contents:
        //LibGDXUtil.getFieldsContents(cameraInputController, 1,  "", true);

        // Setting most of cameraInputController to defaults, except for scrollFactor and translateUnits.
        // These are calculated based on the model's dimensions
        cameraInputController.rotateButton = Input.Buttons.LEFT;     //     int rotateButton    = 0
        cameraInputController.rotateAngle = 360f;                    //   float rotateAngle     = 360.0
        cameraInputController.translateButton = Input.Buttons.RIGHT; //     int translateButton = 1
        cameraInputController.translateUnits = D/2;                  //   float translateUnits  = 188.23 // "right button speed"
        cameraInputController.forwardButton = Input.Buttons.MIDDLE;  //     int forwardButton   = 2
        cameraInputController.activateKey = 0;                       //     int activateKey     = 0
        cameraInputController.alwaysScroll = true;                   // boolean alwaysScroll    = true
        cameraInputController.scrollFactor = -0.2f;                  //   float scrollFactor    = -0.2 // "zoom speed"
        cameraInputController.pinchZoomFactor = 10f;                 //   float pinchZoomFactor = 10.0
        cameraInputController.autoUpdate = true;                     // boolean autoUpdate      = true
        cameraInputController.target.set(0, 0, 0);                   // Vector3 target          = (0.0,0.0,0.0)
        cameraInputController.translateTarget = true;                // boolean translateTarget = true
        cameraInputController.forwardTarget = true;                  // boolean forwardTarget   = true
        cameraInputController.scrollTarget = false;                  // boolean scrollTarget    = false
        cameraInputController.forwardKey = Input.Keys.W;             //     int forwardKey      = 51
        cameraInputController.backwardKey = Input.Keys.S;            //     int backwardKey     = 47
        cameraInputController.rotateRightKey = Input.Keys.A;         //     int rotateRightKey  = 29
        cameraInputController.rotateLeftKey = Input.Keys.D;          //     int rotateLeftKey   = 32
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
            fpsLabel.setText("FPS: " + fps);
            //Gdx.app.debug("clockFPS", "time elapsed: " + clockFPS + " seconds passed. FPS = " + fps);
            clockFPS = 0; // reset your variable to 0
        }
    }

    /**
     *
     */
    public void setup2DStageWidgets() {
        // WIDGETS for 2D Stage (https://github.com/libgdx/libgdx/wiki/Scene2d.ui#widgets)
        // IMAGES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#image
        // https://libgdx.info/basic_image/
        textureImage = new Image();
        textureImage.setPosition(0f, 0f);
        textureImage.setScaling(Scaling.fit);
        textureImage.setAlign(Align.left);

        // LABELS:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#label
        fpsLabel = new Label("", labelStyle);
        miLabel = new Label("", labelStyle);
        envLabel = new Label("", labelStyle);

        // SELECT BOXES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#selectbox
        modelSelectBox = new SelectBox<>(skin);
        //String[] items = (String[]) Arrays.stream(models.items).map(assetManager::getAssetFileName).toArray();
        // Exception in thread "LWJGL Application" java.lang.ClassCastException:
        // class [Ljava.lang.Object; cannot be cast to class [Lcom.badlogic.gdx.graphics.g3d.Model;
        // ([Ljava.lang.Object; is in module java.base of loader 'bootstrap';
        // [Lcom.badlogic.gdx.graphics.g3d.Model; is in unnamed module of loader 'app')

        // Select Box: Models
        Array<String> itemsModel = new Array<>();
        for (Model m: models) {
            String filename = assetManager.getAssetFileName(m);
            if (!filename.toLowerCase().contains("animations")) {
                itemsModel.add(filename);
            }
        }

        String noModelsAvailable = "No models available";
        if (itemsModel.size == 0) { itemsModel.add(noModelsAvailable); }

        modelSelectBox.clearItems();
        modelSelectBox.setItems(itemsModel);
        modelSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (modelSelectBox.getSelected().equals(noModelsAvailable)) {
                    return;
                }
                switchModelInstance(modelSelectBox.getSelected());
                Gdx.app.debug(modelSelectBox.getClass().getSimpleName(),
                        "model selected: " + modelSelectBox.getSelected());
            }
        });

        // Select Box: Animations
        animationSelectBox = new SelectBox<>(skin);
        animationSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                animationIndex = animationSelectBox.getSelectedIndex() - 1; // -1 since we have "No Animation" item
                if (animationController == null) { return; }

                if (animationIndex < 0) {
                    animationController.setAnimation(null);
                    return;
                }

                animationDesc = animationController.setAnimation(modelInstance.animations.get(animationIndex).id, -1);
                Gdx.app.debug(animationSelectBox.getClass().getSimpleName(),
                        "animation selected: " + modelInstance.animations.get(animationIndex).id);
                // Uncomment to get gen_* files with fields contents:
                //LibGDXUtil.getFieldsContents(animationDesc, 3,  "", true);

                // (the actual numbers here are made up to show what it looks like):
                // AnimationListener listener  = null
                //             float speed     = 1.0
                //             float time      = 0.0
                //             float offset    = 0.0
                //             float duration  = 2.784
                //               int loopCount = -1
                //         Animation animation = com.badlogic.gdx.graphics.g3d.model.Animation
                //              String id             = test
                //               float duration       = 2.784
                //               Array nodeAnimations =
                //                     int size = 2
                //                     0: NodeAnimation com.badlogic.gdx.graphics.g3d.model.NodeAnimation
                //                         Node node        = com.badlogic.gdx.graphics.g3d.model.Node
                //                        Array<NodeKeyframe<Vector3>> translation =
                //                              0: NodeKeyframe com.badlogic.gdx.graphics.g3d.model.NodeKeyframe
                //                                    float keytime = 0.0
                //                                   Object value   = <Vector3>
                //                             ...
                //                             14: NodeKeyframe com.badlogic.gdx.graphics.g3d.model.NodeKeyframe
                //                                    float keytime = 2.784
                //                                   Object value   = <Vector3>
                //                        Array scaling     =
                //                              0: NodeKeyframe com.badlogic.gdx.graphics.g3d.model.NodeKeyframe
                //                                    float keytime = 0.0
                //                                   Object value   = <Vector3>
                //                             ...
                //                             14: NodeKeyframe com.badlogic.gdx.graphics.g3d.model.NodeKeyframe
                //                                    float keytime = 2.784
                //                                   Object value   = <Vector3>
                //                        Array rotation    =
                //                              0: NodeKeyframe com.badlogic.gdx.graphics.g3d.model.NodeKeyframe
                //                                    float keytime = 0.0
                //                                   Object value   = <Quaternion>
                //                             ...
                //                             14: NodeKeyframe com.badlogic.gdx.graphics.g3d.model.NodeKeyframe
                //                                    float keytime = 2.784
                //                                   Object value   = <Quaternion>
                //                     1: NodeAnimation com.badlogic.gdx.graphics.g3d.model.NodeAnimation
                //                            ...

            }
        });

        // Select Box: Textures
        textureSelectBox = new SelectBox<>(skin);
        textureSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                textureImage.setDrawable(null);

                if (modelInstance == null
                        || modelInstance.materials == null
                        || modelInstance.materials.size == 0) {
                    return;
                }

                modelInstance.materials.get(0).remove(TextureAttribute.Diffuse);

                if (textureSelectBox.getSelectedIndex() == 0 || textureSelectBox.getSelected() == null) {
                    //Gdx.app.debug("textureSelectBox.changed", String.valueOf(textureSelectBox.getSelectedIndex()));
                    //Gdx.app.debug("textureSelectBox.changed", textureSelectBox.getSelected());
                    return;
                }

                Texture texture = assetManager.get(textureSelectBox.getSelected(), Texture.class);
                TextureAttribute attr = TextureAttribute.createDiffuse(texture);
                attr.textureDescription.minFilter = texture.getMinFilter();
                attr.textureDescription.magFilter = texture.getMagFilter();
                attr.textureDescription.uWrap = texture.getUWrap();
                attr.textureDescription.vWrap = texture.getVWrap();
                modelInstance.materials.get(0).set(attr);

                // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#image
                // https://libgdx.info/basic_image/
                // see image constructors starting Image (Texture texture) for Texture to Drawable translation:
                textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
                Gdx.app.debug(textureSelectBox.getClass().getSimpleName(),
                        "texture selected: " + texture);
            }
        });

        // CHECK BOXES:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#checkbox
        debugStageCheckBox = new CheckBox("debug stage", skin);
        debugStageCheckBox.setChecked(false);
        debugStageCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                // https://github.com/libgdx/libgdx/wiki/Table#debugging
                // turn on all debug lines (table, cell, and widget)
                //rootTable.setDebug(debugLayoutCheckBox.isChecked());
                stage.setDebugAll(debugStageCheckBox.isChecked());
            }
        });
    }

    /**
     *
     */
    public void setup2DStageStyling() {
        // https://github.com/libgdx/libgdx/wiki/Managing-your-assets#loading-a-ttf-using-the-assethandler
        labelBitmapFont = assetManager.get(Config.ASSET_FILE_NAME_FONT, BitmapFont.class);
        labelStyle = new Label.LabelStyle(labelBitmapFont, Color.BLACK);
        // SKIN for 2D Stage Widgets
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#skin
        // Skin files from the libGDX tests can be used as a starting point.
        // https://github.com/libgdx/libgdx/tree/master/tests/gdx-tests-android/assets/data
        // You will need: uiskin.png, uiskin.atlas, uiskin.json, and default.fnt.
        // This enables you to quickly get started using scene2d.ui and replace the skin assets later.
        // https://github.com/libgdx/libgdx/wiki/Texture-packer#textureatlas
        //TextureAtlas atlas;
        //atlas = new TextureAtlas(Gdx.files.internal("skins/libgdx/uiskin.atlas"));
        // https://github.com/libgdx/libgdx/wiki/Skin#resources
        // https://github.com/libgdx/libgdx/wiki/Skin#skin-json
        skin = new Skin(Gdx.files.internal(Config.ASSET_FILE_NAME_SKIN));
        //skin.addRegions(atlas);
    }

    /**
     *
     */
    public void setup2DStageLayout() {
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#layout-widgets
        // Table, Container, Stack, ScrollPane, SplitPane, Tree, VerticalGroup, HorizontalGroup

        // https://github.com/libgdx/libgdx/wiki/Table#quickstart
        // The table sizes and positions its children, so setting the width of the text fields
        // to 100 is done on the table cell, not on the text fields themselves.
        rootTable = new Table();
        // https://github.com/libgdx/libgdx/wiki/Table#root-table
        // When doing UI layout, a UI widget does not set its own size.
        // Instead, it provides a minimum, preferred, and maximum size.
        // In libgdx the setFillParent method can be used to easily size the root table to the stage
        // (but should generally only be used on the root table):
        rootTable.setFillParent(true);
        // https://github.com/libgdx/libgdx/wiki/Table#debugging
        // turn on all debug lines (table, cell, and widget)
        rootTable.setDebug(false);

        // https://github.com/libgdx/libgdx/wiki/Table#adding-cells
        Table upperPanel = new Table();
        upperPanel.add(new Label("Models: ", skin));
        upperPanel.add(modelSelectBox);
        upperPanel.add(new Label("Textures: ", skin));
        upperPanel.add(textureSelectBox);
        upperPanel.add(new Label("Animations: ", skin));
        upperPanel.add(animationSelectBox);
        upperPanel.add().expandX();
        rootTable.add(upperPanel).colspan(2).expandX().left();

        rootTable.row();

        rootTable.add(miLabel).expand().left();
        rootTable.add(envLabel).expand().right();

        rootTable.row();

        //https://github.com/libgdx/libgdx/wiki/Scene2d.ui#image
        //https://libgdx.info/basic_image/
        rootTable.add(textureImage).expand().left();
        rootTable.add().expand().right();

//        rootTable.row();
//        rootTable.add(new Label("TEST TEST TEST", skin)).expand().left();
//        rootTable.add(new Label("TEST TEST TEST", skin)).expand().right();

        rootTable.row();

        Table lowerPanel = new Table();
        lowerPanel.add(fpsLabel).minWidth(90f);
        lowerPanel.add(debugStageCheckBox).minWidth(90f);
        lowerPanel.add().expandX();
        rootTable.add(lowerPanel).colspan(2).expandX().left();

        stage.addActor(rootTable);
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

        // Getting the Renderable (for Node 0, NodePart 0)
        Renderable renderable = modelInstance.getRenderable(new Renderable());

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
        Gdx.app.debug(getClass().getSimpleName(),"TextureBinder used: "
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