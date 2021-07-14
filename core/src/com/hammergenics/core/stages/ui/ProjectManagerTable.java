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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.HGTexture;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.HGModel;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.auxiliary.HGTreeVisTableNode;
import com.hammergenics.core.stages.ui.auxiliary.HGTreeVisTableNode.HGTreeVisTable;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ProjectManagerTable extends ManagerTable {
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

    public final ArrayMap<String, HGTreeVisTableNode> extension2treeNode = new ArrayMap<>(String.class, HGTreeVisTableNode.class);

    public ActorGestureListener nonSelectListener;
    public ActorGestureListener selectListener;

    public ProjectManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);
    }

    @Override
    protected void init() {
        // https://github.com/kotcrab/vis-ui/blob/master/ui/src/test/java/com/kotcrab/vis/ui/test/manual/TestTree.java#L75
        projectTree = new VisTree<>();
        projectTree.add(assetsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Assets")));
        projectTree.add(modelInstancesTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Model Instances")));
        projectTree.add(envTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Environment")));

        assetsTreeNode.add(assetsModelsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Models", Color.CYAN)));
        assetsTreeNode.add(assetsImagesTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Images", Color.CHARTREUSE)));
        assetsTreeNode.add(assetsSoundsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Sounds", Color.GOLD)));
        assetsTreeNode.add(assetsFontsTreeNode = new HGTreeVisTableNode(new HGTreeVisTable("Fonts", Color.CORAL)));

        projectTree.expandAll();
        projectTreeScrollPane = new VisScrollPane(projectTree);

        nonSelectListener = new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                Gdx.app.debug("project", "GLOBAL non-select");
                handleNonSelect(event, x, y, count, button);
                super.tap(event, x, y, count, button);
            }
        };
        selectListener = new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if (count == 1) { // single click
                    Gdx.app.debug("project", "GLOBAL select: tap 1");
                    tap1(event, x, y, count, button);
                } else if ( count == 2 ) { // double click
                    Gdx.app.debug("project", "GLOBAL select: tap 2");
                    tap2(event, x, y, count, button);
                }
                super.tap(event, x, y, count, button);
            }
        };

        applyListeners();
    }

    public void tap1(InputEvent event, float x, float y, int count, int button) { }
    public void tap2(InputEvent event, float x, float y, int count, int button) { }
    public void handleNonSelect(InputEvent event, float x, float y, int count, int button) { }

    public void applyListeners() {
        applyListeners(assetsTreeNode);
        applyListeners(modelInstancesTreeNode);
        applyListeners(envTreeNode);

        applyListeners(fh2imagesTreeNode,
                new ActorGestureListener() {
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button) {
                        Gdx.app.debug("project", "IMAGE non-select");
                        handleNonSelect(event, x, y, count, button);
                        super.tap(event, x, y, count, button);
                    }
                }, new ActorGestureListener() {
                    @Override
                    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        stage.showPreviewImage(((HGTreeVisTableNode.HGTreeVisTable)event.getTarget().getParent()).fileHandle);
                        super.touchDown(event, x, y, pointer, button);
                    }
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button) {
                        if (count == 1) { // single click
                            Gdx.app.debug("project", "IMAGE select: tap 1");
                            tap1(event, x, y, count, button);
                        } else if ( count == 2 ) { // double click
                            Gdx.app.debug("project", "IMAGE select: tap 2");
                            tap2(event, x, y, count, button);
                        }
                        super.tap(event, x, y, count, button);
                    }
                });
    }

    public void applyListeners(HGTreeVisTableNode rootNode) {
        rootNode.getActor().clearListeners();
        rootNode.getActor().addListener(nonSelectListener);

        for (HGTreeVisTableNode node1 : rootNode.getChildren()) {      // "Models", "Images", "Sounds"...
            node1.getActor().clearListeners();
            node1.getActor().addListener(nonSelectListener);
            for (HGTreeVisTableNode node2 : node1.getChildren()) {     // parent folders
                node2.getActor().clearListeners();
                node2.getActor().addListener(nonSelectListener);
                for (HGTreeVisTableNode node3 : node2.getChildren()) { // actual asset
                    node3.getActor().clearListeners();
                    node3.getActor().addListener(selectListener);
                }
            }
        }
    }

    public void applyListeners(final ArrayMap<FileHandle, HGTreeVisTableNode> map,
                               ActorGestureListener nonSelectListener,
                               ActorGestureListener selectListener) {
        if (map == null) { return; }
        for (ObjectMap.Entry<FileHandle, HGTreeVisTableNode> entry: map) {
            FileHandle parent = entry.key;
            HGTreeVisTableNode tn = entry.value;

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
        for (ObjectMap.Entry<FileHandle, HGTexture> entry: eng.hgTextures) {
            HGTreeVisTableNode node;
            assetsImagesTreeNode.add(node = new HGTreeVisTableNode(new HGTreeVisTable(entry.key.nameWithoutExtension())));
            node.add(new HGTreeVisTableNode(new HGTreeVisTable(entry.key.path())));
        }
    }

    public void fillTreeNodesWithAssets(HGTreeVisTableNode rootModelsTreeNode, HGTreeVisTableNode rootImagesTreeNode) {
        final ArrayMap<FileHandle, HGTreeVisTableNode> tmpModels = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);
        final ArrayMap<FileHandle, HGTreeVisTableNode> tmpImages = new ArrayMap<>(FileHandle.class, HGTreeVisTableNode.class);

        if (rootModelsTreeNode != null) {
            for (ObjectMap.Entry<FileHandle, HGModel> entry: eng.hgModels) {
                addAssetTreeNode(entry.key, rootModelsTreeNode, rootImagesTreeNode, tmpModels, tmpImages);
            }
        }

        if (rootImagesTreeNode != null) {
            for (ObjectMap.Entry<FileHandle, HGTexture> entry: eng.hgTextures) {
                addAssetTreeNode(entry.key, rootModelsTreeNode, rootImagesTreeNode, tmpModels, tmpImages);
            }
        }
    }

    public void addAssetTreeNode(FileHandle fileHandle) {
        addAssetTreeNode(fileHandle, assetsModelsTreeNode, assetsImagesTreeNode, fh2modelsTreeNode, fh2imagesTreeNode);
    }

    public void addAssetTreeNode(FileHandle fileHandle,
                                 HGTreeVisTableNode rootModelsTreeNode,
                                 HGTreeVisTableNode rootImagesTreeNode,
                                 final ArrayMap<FileHandle, HGTreeVisTableNode> mapModels,
                                 final ArrayMap<FileHandle, HGTreeVisTableNode> mapImages) {
        if (fileHandle == null || fileHandle.isDirectory()) { return; }

        Class<?> assetClass = HGEngine.getAssetClass(fileHandle);

        boolean parentPresent;
        HGTreeVisTableNode treeNode;
        // adding the parent folder node first (if not existed before)
        if (assetClass.equals(Model.class) && rootModelsTreeNode != null) {
            parentPresent = mapModels.containsKey(fileHandle.parent());
            treeNode = getAssetParentFolderTreeNode(fileHandle, assetClass, mapModels);
            if (!parentPresent) { addModelAssetParentFolderTreeNode(fileHandle, treeNode, rootModelsTreeNode); }
        } else if (assetClass.equals(Texture.class) && rootImagesTreeNode != null) {
            parentPresent = mapImages.containsKey(fileHandle.parent());
            treeNode = getAssetParentFolderTreeNode(fileHandle, assetClass, mapImages);
            if (!parentPresent) { addImageAssetParentFolderTreeNode(fileHandle, treeNode, rootImagesTreeNode); }
        } else {
            return;
        }

        applyCommonPathToParentTreeNodes(mapModels);
        applyCommonPathToParentTreeNodes(mapImages);

        // then adding the child node - the asset itself
        if (assetClass == Model.class) {
            addModelAssetTreeNode(fileHandle, treeNode);
        } else if (assetClass == Texture.class) {
            addImageAssetTreeNode(fileHandle, treeNode);
        }
    }

    public HGTreeVisTableNode getAssetParentFolderTreeNode(FileHandle fileHandle, Class<?> assetClass,
                                                           final ArrayMap<FileHandle, HGTreeVisTableNode> map) {
        FileHandle parent = fileHandle.parent();
        String parentAbsPath = parent.file().getAbsolutePath();

        HGTreeVisTableNode treeNode = map.get(parent);
        if (treeNode == null) {
            Color clr = Color.WHITE;
            if (assetClass.equals(Model.class)) { clr = Color.CYAN; }
            else if (assetClass.equals(Texture.class)) { clr = Color.CHARTREUSE; }

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
        VisTextButton createMisTB = new VisTextButton("create instances");
        createMisTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Array<FileHandle> fhs;
                // by this time fh2treeNode.get(parent) should return a real node
                fhs = Arrays.stream(fh2modelsTreeNode.get(parent).getChildren().toArray()) // Array<HGTreeVisTableNode> -> HGTreeVisTableNode[]
                        .map(Tree.Node::getActor)                                    // HGTreeVisTableNode -> HGTreeVisTable
                        .map(HGTreeVisTable::getFileHandle)                          // HGTreeVisTable -> FileHandle
                        .collect(Array::new, Array::add, Array::addAll);             // -> Array<FileHandle>
                stage.addModelInstances(fhs);
                stage.afterCurrentModelInstanceChanged();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        VisTextButton unloadTB = new VisTextButton("unload");
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

        VisTextButton createMisTB = new VisTextButton("create instance");
        createMisTB.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                stage.addModelInstance(fileHandle);
                stage.afterCurrentModelInstanceChanged();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        VisTextButton unloadTB = new VisTextButton("unload");
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

    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);
    }

    @Override
    public void resetActors() {
        super.resetActors();

        VisTable table = new VisTable();
        VisWindow window = new VisWindow("Project");
        window.setResizable(false);
        window.addCloseButton();
        window.setMovable(false);

        window.add(projectTreeScrollPane).expand().fill().padRight(5f).minWidth(Gdx.graphics.getWidth()/8f);

        VisImageButton closeTB = null;
        for (Actor actor: window.getTitleTable().getChildren()) {
            if (actor instanceof VisImageButton) { closeTB = (VisImageButton) actor; break; }
        }
        if (closeTB != null) {
            closeTB.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    stage.unpressButton(stage.projTextButton, true);
                    stage.resetTables();
                }
            });
        }

        table.add(window).expand().fillY().left();
        stage.leftPaneCell.setActor(table);
    }

    @Override
    public void applyLocale() {
        super.applyLocale();
    }
}
