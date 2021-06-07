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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.screens.graphics.g3d.HGModel;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.utils.LibgdxUtils;

import java.util.Arrays;

import static com.hammergenics.screens.graphics.g3d.utils.Models.createGridModel;
import static com.hammergenics.screens.graphics.g3d.utils.Models.createLightsModel;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGEngine implements Disposable {
    public final HGGame game;
    public final AssetManager assetManager;

    public Array<HGModel> hgModels = new Array<>();
    public Array<Texture> textures = new Array<>();
    // Auxiliary models:
    public Model gridModel = null;
    public ModelInstance gridXZModelInstance = null; // XZ plane: lines (yellow)
    public ModelInstance gridYModelInstance = null;  // Y axis: vertical lines (red)
    public ModelInstance gridOModelInstance = null;  // origin: sphere (red)
    public Model lightsModel = null;
    public Array<ModelInstance> dlArrayModelInstance = null; // directional lights
    public Array<ModelInstance> plArrayModelInstance = null; // point lights
    public Array<ModelInstance> bbArrayModelInstance = null; // bounding boxes

    // ModelInstance Related:
    public Array<HGModelInstance> hgMIs = new Array<>(HGModelInstance.class);
    public float unitSize = 0f;
    public float overallSize = 0f;
    public HGModelInstance currMI = null;
    public Vector2 currCell = Vector2.Zero.cpy();
    public HGModelInstance hoveredOverMI = null;
    public ArrayMap<Attributes, ColorAttribute> hoveredOverMIAttributes = null;

    public HGEngine(HGGame game) {
        this.game = game;
        this.assetManager = game.assetManager;

        // Creating the Aux Models beforehand:
        gridModel = createGridModel();
        lightsModel = createLightsModel();
    }

    @Override
    public void dispose() {
        if (gridModel != null) { gridModel.dispose(); }
        if (lightsModel != null) { lightsModel.dispose(); }
        for (HGModelInstance mi:hgMIs) { mi.dispose(); }
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

        if (hgModels.size == 0) {
            Gdx.app.error(Thread.currentThread().getStackTrace()[1].getMethodName(), "No models available");
            Gdx.app.exit(); // On iOS this should be avoided in production as it breaks Apples guidelines
            return;
        }

        assetManager.getAll(Texture.class, textures);
        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "textures loaded: " + textures.size);
    }

    public void addModelInstances(Array<FileHandle> modelFHs) {
        modelFHs.forEach(fileHandle -> addModelInstance(fileHandle, null, -1));

        if (hgMIs.size > 0) { currMI = hgMIs.get(0); }
    }

    /**
     * @param assetFL
     */
    public boolean addModelInstance(FileHandle assetFL, String nodeId, int nodeIndex) {
        HGModel hgModel = new HGModel(assetManager.get(assetFL.path(), Model.class), assetFL);
        if (!hgModel.hasMaterials() && !hgModel.hasMeshes() && !hgModel.hasMeshParts()) {
            if (hgModel.hasAnimations()) {
                // we got animations only model
                Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(), "animations only model: " + assetFL);
            }
            return false;
        }

        if (nodeId == null) {
            currMI = new HGModelInstance(hgModel, assetFL);
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
            currMI = new HGModelInstance(hgModel, assetFL, nodeId);
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
        copyExternalAnimations(assetFL);

        currMI.animationController = null;
        if (currMI.animations.size > 0) { currMI.animationController = new AnimationController(currMI); }

        return true;
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

    public void resetLightsModelInstances(Vector3 center, Environment environment) {
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
}