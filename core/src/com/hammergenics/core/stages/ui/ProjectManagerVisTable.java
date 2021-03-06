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

package com.hammergenics.core.stages.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.HGEngine;
import com.hammergenics.HGGame;
import com.hammergenics.core.HGAsset;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.HGModel;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.auxiliary.HGTreeVisTableNode;
import com.hammergenics.core.stages.ui.auxiliary.HGTreeVisTableNode.HGTreeVisTable;
import com.kotcrab.vis.ui.i18n.BundleText;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;

import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.hammergenics.core.stages.ui.ProjectManagerVisTable.LabelsTextEnum.*;
import static com.hammergenics.core.stages.ui.ProjectManagerVisTable.TextButtonsTextEnum.*;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ProjectManagerVisTable extends ManagerVisTable {
    public VisTree<HGTreeVisTableNode, VisLabel> projectTree;
    public VisScrollPane projectTreeScrollPane;

    public HGTreeVisTableNode assetsTreeNode;
    public HGTreeVisTableNode assetsModelsTreeNode;
    public HGTreeVisTableNode assetsImagesTreeNode;
    public HGTreeVisTableNode assetsSoundsTreeNode;
    public HGTreeVisTableNode assetsFontsTreeNode;
    public HGTreeVisTableNode modelInstancesTreeNode;
    public HGTreeVisTableNode envTreeNode;

    public FileHandle commonPath = null;
    public final ArrayMap<HGTreeVisTableNode, FileHandle> treeNode2fh = new ArrayMap<>(HGTreeVisTableNode.class, FileHandle.class);
    public final ArrayMap<FileHandle, HGTreeVisTableNode> fh2modelsTreeNode = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);
    public final ArrayMap<FileHandle, HGTreeVisTableNode> fh2imagesTreeNode = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);
    public final ArrayMap<FileHandle, HGTreeVisTableNode> fh2soundsTreeNode = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);

    public final ArrayMap<String, HGTreeVisTableNode> extension2treeNode = new ArrayMap<>(String.class, HGTreeVisTableNode.class);

    public ActorGestureListener globalNonSelectListener;
    public ActorGestureListener globalSelectListener;

    public ProjectManagerVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        applyListeners();
    }

    @Override
    protected void init() {
        // https://github.com/kotcrab/vis-ui/blob/master/ui/src/test/java/com/kotcrab/vis/ui/test/manual/TestTree.java#L75
        projectTree = new VisTree<>();
        projectTree.add(assetsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Assets")));
        TREE_NODE_ASSETS.seize(assetsTreeNode.getActor().label);
        projectTree.add(modelInstancesTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Model Instances")));
        TREE_NODE_MODEL_INSTANCES.seize(modelInstancesTreeNode.getActor().label);
        projectTree.add(envTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Environment")));
        TREE_NODE_ENVIRONMENT.seize(envTreeNode.getActor().label);

        //                   Blueish        Greenish        Yellowish      Reddish        Purplish
        //          Assets: Color.CYAN;  Color.CHARTREUSE; Color.GOLD;   Color.CORAL;   Color.PINK;
        // Model Instances: Color.SKY;   Color.GREEN;      Color.YELLOW; Color.SCARLET; Color.MAGENTA;
        //     Environment: Color.BLUE;  Color.LIME;       Color.ORANGE; Color.RED;     Color.PURPLE;
        //         Scripts: Color.ROYAL;                   Color.TAN;    Color.SALMON;  Color.VIOLET;
        //            ... : the unused from the above

        assetsTreeNode.add(assetsModelsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Models", Color.CYAN)));
        TREE_NODE_ASSETS_MODELS.seize(assetsModelsTreeNode.getActor().label);
        assetsTreeNode.add(assetsImagesTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Images", Color.CHARTREUSE)));
        TREE_NODE_ASSETS_IMAGES.seize(assetsImagesTreeNode.getActor().label);
        assetsTreeNode.add(assetsSoundsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Sounds", Color.GOLD)));
        TREE_NODE_ASSETS_SOUNDS.seize(assetsSoundsTreeNode.getActor().label);
        assetsTreeNode.add(assetsFontsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Fonts", Color.CORAL)));
        TREE_NODE_ASSETS_FONTS.seize(assetsFontsTreeNode.getActor().label);

        projectTree.expandAll();
        projectTreeScrollPane = new VisScrollPane(projectTree);

        globalNonSelectListener = new ActorGestureListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("project", "GLOBAL non-select: touchDown");
                handleNonSelectTouchDown(event, x, y, pointer, button);
                super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                Gdx.app.debug("project", "GLOBAL non-select: tap");
                handleNonSelectTap(event, x, y, count, button);
                super.tap(event, x, y, count, button);
            }
        };
        globalSelectListener = new ActorGestureListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("project", "GLOBAL select: touchDown");
                handleSelectTouchDown(event, x, y, pointer, button);
                super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if (count == 1) { // single click
                    Gdx.app.debug("project", "GLOBAL select: tap 1");
                    handleSelectTap1(event, x, y, count, button);
                } else if ( count == 2 ) { // double click
                    Gdx.app.debug("project", "GLOBAL select: tap 2");
                    handleSelectTap2(event, x, y, count, button);
                }
                super.tap(event, x, y, count, button);
            }
        };
    }

    public void handleSelectTap1(InputEvent event, float x, float y, int count, int button) { }
    public void handleSelectTap2(InputEvent event, float x, float y, int count, int button) { }
    public void handleSelectTouchDown(InputEvent event, float x, float y, int pointer, int button) { }
    public void handleNonSelectTouchDown(InputEvent event, float x, float y, int pointer, int button) { }
    public void handleNonSelectTap(InputEvent event, float x, float y, int count, int button) { }

    public void applyListeners() {
        applyAssetsListeners(assetsTreeNode);
        applyModelInstancesListeners(modelInstancesTreeNode);
        applyEnvironmentListeners(envTreeNode);

        applyListeners(fh2imagesTreeNode.values().toArray(),
                new ActorGestureListener() {
                    @Override
                    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        Gdx.app.debug("project", "IMAGE non-select: touchDown");
                        super.touchDown(event, x, y, pointer, button);
                    }
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button) {
                        Gdx.app.debug("project", "IMAGE non-select: tap");
                        super.tap(event, x, y, count, button);
                    }
                }, new ActorGestureListener() {
                    @Override
                    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        Gdx.app.debug("project", "IMAGE select: touchDown");
                        stage.hidePreviewImage();
                        stage.showPreviewImage(((HGTreeVisTableNode.HGTreeVisTable)event.getTarget().getParent()).fileHandle);
                        super.touchDown(event, x, y, pointer, button);
                    }
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button) {
                        if (count == 1) { // single click
                            Gdx.app.debug("project", "IMAGE select: tap 1");
                        } else if ( count == 2 ) { // double click
                            Gdx.app.debug("project", "IMAGE select: tap 2");
                        }
                        super.tap(event, x, y, count, button);
                    }
                });
    }

    public void applyAssetsListeners(HGTreeVisTableNode rootNode) {
        rootNode.getActor().clearListeners();
        rootNode.getActor().addListener(globalNonSelectListener);

        for (HGTreeVisTableNode node1 : rootNode.getChildren()) {      // "Models", "Images", "Sounds"...
            node1.getActor().clearListeners();
            node1.getActor().addListener(globalNonSelectListener);
            for (HGTreeVisTableNode node2 : node1.getChildren()) {     // parent folders
                node2.getActor().clearListeners();
                node2.getActor().addListener(globalNonSelectListener);
                for (HGTreeVisTableNode node3 : node2.getChildren()) { // actual asset
                    node3.getActor().clearListeners();
                    node3.getActor().addListener(globalSelectListener);
                }
            }
        }
    }

    public void applyModelInstancesListeners(HGTreeVisTableNode rootNode) {
        rootNode.getActor().clearListeners();
        rootNode.getActor().addListener(globalNonSelectListener);

        for (HGTreeVisTableNode node1 : rootNode.getChildren()) {      // "Models", "Images", "Sounds"...
            node1.getActor().clearListeners();
            node1.getActor().addListener(globalSelectListener);
        }
    }

    public void applyEnvironmentListeners(HGTreeVisTableNode rootNode) {
        rootNode.getActor().clearListeners();
        rootNode.getActor().addListener(globalNonSelectListener);
    }

    public void applyListeners(final Array<HGTreeVisTableNode> parentTreeNodes,
                               ActorGestureListener nonSelectListener,
                               ActorGestureListener selectListener) {
        if (parentTreeNodes == null) { return; }
        for (HGTreeVisTableNode tn: parentTreeNodes) {
            // the listeners should already be cleared
            //tn.getActor().clearListeners();
            tn.getActor().addListener(nonSelectListener);

            for (HGTreeVisTableNode node1: tn.getChildren()) { // actual asset
                // the listeners should already be cleared
                //node1.getActor().clearListeners();
                node1.getActor().addListener(selectListener);
            }
        }
    }

    public void updateAssetsTree() {
        assetsModelsTreeNode.clearChildren();
        for (ObjectMap.Entry<FileHandle, HGModel> entry: eng.hgModels) {
            HGTreeVisTableNode node;
            assetsModelsTreeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(entry.key.nameWithoutExtension())));
            node.add(new HGTreeVisTableNode(new HGTreeVisTable(entry.key.path())));
        }

        assetsImagesTreeNode.clearChildren();
        for (ObjectMap.Entry<FileHandle, HGAsset<Texture>> entry: eng.hgTextures) {
            HGTreeVisTableNode node;
            assetsImagesTreeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(entry.key.nameWithoutExtension())));
            node.add(new HGTreeVisTableNode(new HGTreeVisTable(entry.key.path())));
        }
    }

    public void fillTreeNodesWithAssets(HGTreeVisTableNode rootModelsTreeNode,
                                        HGTreeVisTableNode rootImagesTreeNode,
                                        HGTreeVisTableNode rootSoundsTreeNode) {
        final ArrayMap<FileHandle, HGTreeVisTableNode> tmpModels = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);
        final ArrayMap<FileHandle, HGTreeVisTableNode> tmpImages = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);
        final ArrayMap<FileHandle, HGTreeVisTableNode> tmpSounds = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);

        if (rootModelsTreeNode != null) {
            for (ObjectMap.Entry<FileHandle, HGModel> entry: eng.hgModels) {
                addAssetTreeNode(entry.key, rootModelsTreeNode, rootImagesTreeNode, rootSoundsTreeNode, tmpModels, tmpImages, tmpSounds);
            }
        }

        if (rootImagesTreeNode != null) {
            for (ObjectMap.Entry<FileHandle, HGAsset<Texture>> entry: eng.hgTextures) {
                addAssetTreeNode(entry.key, rootModelsTreeNode, rootImagesTreeNode, rootSoundsTreeNode, tmpModels, tmpImages, tmpSounds);
            }
        }

        if (rootSoundsTreeNode != null) {
            for (ObjectMap.Entry<FileHandle, HGAsset<Sound>> entry: eng.hgSounds) {
                addAssetTreeNode(entry.key, rootModelsTreeNode, rootImagesTreeNode, rootSoundsTreeNode, tmpModels, tmpImages, tmpSounds);
            }
        }
    }

    public void addAssetTreeNode(FileHandle fileHandle) {
        addAssetTreeNode(fileHandle,
                assetsModelsTreeNode,
                assetsImagesTreeNode,
                assetsSoundsTreeNode,
                fh2modelsTreeNode,
                fh2imagesTreeNode,
                fh2soundsTreeNode);
    }

    public void addAssetTreeNode(FileHandle fileHandle,
                                 HGTreeVisTableNode rootModelsTreeNode,
                                 HGTreeVisTableNode rootImagesTreeNode,
                                 HGTreeVisTableNode rootSoundsTreeNode,
                                 final ArrayMap<FileHandle, HGTreeVisTableNode> mapModels,
                                 final ArrayMap<FileHandle, HGTreeVisTableNode> mapImages,
                                 final ArrayMap<FileHandle, HGTreeVisTableNode> mapSounds) {
        if (fileHandle == null || fileHandle.isDirectory()) { return; }

        Class<?> assetClass = HGEngine.getAssetClass(fileHandle);

        boolean parentPresent;
        HGTreeVisTableNode parentTreeNode;
        // adding the parent folder node first (if not existed before)
        if ((assetClass.equals(Model.class) || assetClass.equals(SceneAsset.class)) && rootModelsTreeNode != null) {
            parentPresent = mapModels.containsKey(fileHandle.parent());
            parentTreeNode = getAssetParentFolderTreeNode(fileHandle, assetClass, mapModels);
            if (!parentPresent) { addModelAssetParentFolderTreeNode(fileHandle, parentTreeNode, rootModelsTreeNode); }
        } else if (assetClass.equals(Texture.class) && rootImagesTreeNode != null) {
            parentPresent = mapImages.containsKey(fileHandle.parent());
            parentTreeNode = getAssetParentFolderTreeNode(fileHandle, assetClass, mapImages);
            if (!parentPresent) { addImageAssetParentFolderTreeNode(fileHandle, parentTreeNode, rootImagesTreeNode); }
        } else if ((assetClass.equals(Sound.class) || assetClass.equals(Music.class)) && rootSoundsTreeNode != null) {
            parentPresent = mapSounds.containsKey(fileHandle.parent());
            parentTreeNode = getAssetParentFolderTreeNode(fileHandle, assetClass, mapSounds);
            if (!parentPresent) { addSoundAssetParentFolderTreeNode(fileHandle, parentTreeNode, rootSoundsTreeNode); }
        } else {
            return;
        }

        applyCommonPathToParentTreeNodes(mapModels);
        applyCommonPathToParentTreeNodes(mapImages);
        applyCommonPathToParentTreeNodes(mapSounds);

        // then adding the child node - the asset itself
        if (assetClass.equals(Model.class) || assetClass.equals(SceneAsset.class)) {
            addModelAssetTreeNode(fileHandle, parentTreeNode);
        } else if (assetClass.equals(Texture.class)) {
            addImageAssetTreeNode(fileHandle, parentTreeNode);
        } else if (assetClass.equals(Sound.class) || assetClass.equals(Music.class)) {
            addSoundAssetTreeNode(fileHandle, parentTreeNode);
        }
    }

    public HGTreeVisTableNode getAssetParentFolderTreeNode(FileHandle fileHandle, Class<?> assetClass,
                                                           final ArrayMap<FileHandle, HGTreeVisTableNode> map) {
        FileHandle parent = fileHandle.parent();
        String parentAbsPath = parent.file().getAbsolutePath();

        HGTreeVisTableNode treeNode = map.get(parent);
        if (treeNode == null) {
            Color clr = Color.WHITE;
            if (assetClass.equals(Model.class) || assetClass.equals(SceneAsset.class)) { clr = Color.CYAN; }
            else if (assetClass.equals(Texture.class)) { clr = Color.CHARTREUSE; }
            else if (assetClass.equals(Sound.class) || assetClass.equals(Music.class)) { clr = Color.GOLD; }

            treeNode = new HGTreeVisTableNode(new HGTreeVisTable(parentAbsPath, clr, parent));
            map.put(parent, treeNode);
        }

        if (commonPath == null) { commonPath = parent; }

        // checking the actual common folder's absolute path
        if (!parentAbsPath.startsWith(commonPath.file().getAbsolutePath())) {
            recalculateCommonPath(parent);
        }
        return treeNode;
    }

    public void applyCommonPathToParentTreeNodes(final ArrayMap<FileHandle, HGTreeVisTableNode> map) {
        // taking the common path's parent folder's absolute path
        // to have 1 level more to avoid the empty tree node label
        String commonAbsPath = commonPath.parent().file().getAbsolutePath();
        for (ObjectMap.Entry<FileHandle, HGTreeVisTableNode> entry: map) {
            FileHandle parent = entry.key;
            String parentAbsPath = parent.file().getAbsolutePath();
            HGTreeVisTableNode tn = entry.value;
            //Gdx.app.debug("project", "" + " common.fh: " + commonPath);
            //Gdx.app.debug("project", "" + " common: " + commonPath.file().getAbsolutePath());
            //Gdx.app.debug("project", "" + " common.parent: " + commonAbsPath);
            //Gdx.app.debug("project", "" + " parent: " + parentAbsPath);
            tn.getActor().label.setText(parentAbsPath.replace(commonAbsPath, ""));
        }
    }

    public void recalculateCommonPath(FileHandle parent) {
        // commonPath = /a/b/c/d/e
        //     parent = /a/b/c/f/g/h
        String abs1 = commonPath.file().getAbsolutePath();
        String abs2 = parent.file().getAbsolutePath();
        //Gdx.app.debug("project", "" + " abs1: " + abs1);
        //Gdx.app.debug("project", "" + " abs2: " + abs2);
        Array<String> folders1 = new Array<>(abs1.split(Pattern.quote(File.separator)));
        Array<String> folders2 = new Array<>(abs2.split(Pattern.quote(File.separator)));
        //Gdx.app.debug("project", "" + " folders1: " + folders1.toString("|"));
        //Gdx.app.debug("project", "" + " folders2: " + folders2.toString("|"));
        int i = 0;
        while((i < folders1.size) && (i < folders2.size) && folders1.get(i).equals(folders2.get(i++)));
        // i-1 should be taken to get the actual common array (one extra i++ is done at the end of the loop)
        commonPath = new FileHandle(String.join(File.separator, Arrays.copyOfRange(folders1.toArray(), 0, i-1)));
        //Gdx.app.debug("project", "" + " commonPath: " + commonPath);
    }

    public void addModelAssetParentFolderTreeNode(final FileHandle fileHandle, HGTreeVisTableNode treeNode,
                                                  HGTreeVisTableNode rootTreeNode) {
        FileHandle parent = fileHandle.parent();
        VisTextButton createMisTB = new VisTextButton("Create Instances");
        TREE_NODE_MODELS_PARENT_CREATE_INSTANCES.seize(createMisTB);
        createMisTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Array<FileHandle> fhs;
                // by this time fh2treeNode.get(parent) should return a real node
                fhs = Arrays.stream(fh2modelsTreeNode.get(parent).getChildren().toArray()) // Array<HGTreeVisTableNode> -> HGTreeVisTableNode[]
                        .map(Tree.Node::getActor)                                          // HGTreeVisTableNode -> HGTreeVisTable
                        .map(HGTreeVisTable::getFileHandle)                                // HGTreeVisTable -> FileHandle
                        .collect(Array::new, Array::add, Array::addAll);                   // -> Array<FileHandle>
                stage.addModelInstances(fhs);
                stage.afterCurrentModelInstanceChanged();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        VisTextButton unloadTB = new VisTextButton("Unload");
        TREE_NODE_MODELS_PARENT_UNLOAD.seize(unloadTB);
        unloadTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        treeNode.getActor().setCell1(createMisTB).setCell2(unloadTB);
        rootTreeNode.add(treeNode);
    }

    public void addModelAssetTreeNode(final FileHandle fileHandle, HGTreeVisTableNode treeNode) {
        HGTreeVisTableNode node;

        VisTextButton createMisTB = new VisTextButton("Create Instance");
        TREE_NODE_MODELS_CREATE_INSTANCES.seize(createMisTB);
        createMisTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                stage.addModelInstance(fileHandle);
                stage.afterCurrentModelInstanceChanged();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        VisTextButton unloadTB = new VisTextButton("Unload");
        TREE_NODE_MODELS_UNLOAD.seize(unloadTB);
        unloadTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        treeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.name(), Color.CYAN, fileHandle)
                .setCell1(createMisTB).setCell2(unloadTB)));
        //node.add(new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.file().getAbsolutePath())));
    }

    public void addImageAssetParentFolderTreeNode(final FileHandle fileHandle, HGTreeVisTableNode treeNode,
                                                  HGTreeVisTableNode rootTreeNode) {
        rootTreeNode.add(treeNode);
    }

    public void addImageAssetTreeNode(FileHandle fileHandle, HGTreeVisTableNode treeNode) {
        HGTreeVisTableNode node;
        treeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.name(), Color.CHARTREUSE, fileHandle)));
        //node.add(new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.file().getAbsolutePath())));
    }

    public void addSoundAssetParentFolderTreeNode(final FileHandle fileHandle, HGTreeVisTableNode treeNode,
                                                  HGTreeVisTableNode rootTreeNode) {
        rootTreeNode.add(treeNode);
    }

    public void addSoundAssetTreeNode(FileHandle fileHandle, HGTreeVisTableNode treeNode) {
        HGTreeVisTableNode node;

        VisTextButton playTB = new VisTextButton("Play");
        TREE_NODE_SOUNDS_PLAY.seize(playTB);
        playTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Object asset = eng.getAsset(fileHandle, HGEngine.getAssetClass(fileHandle));
                if (asset instanceof Sound) {
                    // https://github.com/libgdx/libgdx/wiki/Sound-effects
                    ((Sound)asset).play(1f);
                } else if (asset instanceof Music) {
                    // https://github.com/libgdx/libgdx/wiki/Streaming-music
                    ((Music)asset).play();
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        treeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.name(), Color.GOLD, fileHandle)
                .setCell1(playTB)));
        //node.add(new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.file().getAbsolutePath())));
    }

    public void addModelInstanceTreeNode(EditableModelInstance mi) {
        addModelInstanceTreeNode(mi, modelInstancesTreeNode);
    }
    public void addModelInstanceTreeNode(EditableModelInstance mi, HGTreeVisTableNode treeNode) {
        HGTreeVisTableNode node;

        String nodeName = mi.toString() + " @" + mi.hashCode();
        treeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(nodeName, Color.SKY, mi.hgModel.afh)));

        //node.add(new HGTreeVisTableNode(new HGTreeVisTable(fileHandle.file().getAbsolutePath())));
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);
    }

    @Override
    public void resetActors() {
        super.resetActors();

        VisTable table = new VisTable();
        VisWindow window = new VisWindow("Project"); WINDOW_TITLE_PROJECT.seize(window.getTitleLabel());
        window.setResizable(false);
        window.addCloseButton();
        window.setMovable(false);

        window.add(projectTreeScrollPane).expand().fill().padRight(5f).minWidth(Gdx.graphics.getWidth()/6f);

        VisImageButton closeTB = null;
        for (Actor actor: window.getTitleTable().getChildren()) {
            if (actor instanceof VisImageButton) { closeTB = (VisImageButton) actor; break; }
        }
        if (closeTB != null) {
            closeTB.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    stage.unpressButton(stage.projTextButton);
                    stage.resetTables();
                }
            });
        }

        table.add(window).expand().fillY().left();
        stage.leftPaneCell.setActor(table);
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) {
        TextButtonsTextEnum.setLanguage(language);
        LabelsTextEnum.setLanguage(language);
    }

    public enum TextButtonsTextEnum implements BundleText {
        TREE_NODE_MODELS_PARENT_CREATE_INSTANCES("tree.node.textButton.assets.models.parent.createinstances"),
        TREE_NODE_MODELS_PARENT_UNLOAD("tree.node.textButton.assets.models.parent.unload"),
        TREE_NODE_MODELS_CREATE_INSTANCES("tree.node.textButton.assets.models.createinstance"),
        TREE_NODE_MODELS_UNLOAD("tree.node.textButton.assets.models.unload"),

        TREE_NODE_SOUNDS_PLAY("tree.node.textButton.assets.sounds.play");

        private final String property;
        // TODO: IMPORTANT: This array will keeps the references to all buttons.
        //                  Need to make sure the references are removed when no longer needed
        //                  (e.g. after the model is unloaded and tree node removed)
        private final Array<TextButton> instances = new Array<>(TextButton.class);
        private static HGGame.I18NBundlesEnum language;

        TextButtonsTextEnum(String property) { this.property = property; }

        public static void setLanguage(HGGame.I18NBundlesEnum lang) {
            language = lang;

            for (TextButtonsTextEnum tbs: TextButtonsTextEnum.values()) {
                for (TextButton tb: tbs.instances) { if (tb != null) { tb.setText(tbs.get()); } }
            }
        }

        public TextButton seize(TextButton btn) {
            this.instances.add(btn);
            btn.setText(get());
            return btn;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.projectManagerBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.projectManagerBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.projectManagerBundle.format(property, arguments) : "ERR"; }
    }

    public enum LabelsTextEnum implements BundleText {
        WINDOW_TITLE_PROJECT("window.title.project"),
        TREE_NODE_ASSETS("tree.node.label.assets"),
        TREE_NODE_ASSETS_MODELS("tree.node.label.assets.models"),
        TREE_NODE_ASSETS_IMAGES("tree.node.label.assets.images"),
        TREE_NODE_ASSETS_SOUNDS("tree.node.label.assets.sounds"),
        TREE_NODE_ASSETS_FONTS("tree.node.label.assets.fonts"),

        TREE_NODE_MODEL_INSTANCES("tree.node.label.modelinstances"),
        TREE_NODE_ENVIRONMENT("tree.node.label.environment");

        private final String property;
        private Label instance = null;
        private static HGGame.I18NBundlesEnum language;

        LabelsTextEnum(String property) { this.property = property; }

        public static void setLanguage(HGGame.I18NBundlesEnum lang) {
            language = lang;

            for (LabelsTextEnum label: LabelsTextEnum.values()) {
                if (label.instance != null) { label.instance.setText(label.get()); }
            }
        }

        public Label seize(Label label) {
            this.instance = label;
            label.setText(get());
            return label;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.projectManagerBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.projectManagerBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.projectManagerBundle.format(property, arguments) : "ERR"; }
    }
}
