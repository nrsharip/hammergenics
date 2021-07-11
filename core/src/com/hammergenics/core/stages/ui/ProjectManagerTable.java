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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.HGTexture;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.HGModel;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ProjectManagerTable extends ManagerTable {
    public VisTree<HGTreeNode, VisLabel> projectTree;
    public VisScrollPane projectTreeScrollPane;

    public HGTreeNode assetsTreeNode;
    public HGTreeNode assetsModelsTreeNode;
    public HGTreeNode assetsImagesTreeNode;
    public HGTreeNode assetsSoundsTreeNode;
    public HGTreeNode assetsFontsTreeNode;
    public HGTreeNode modelInstancesTreeNode;
    public HGTreeNode envTreeNode;

    public final ArrayMap<String, HGTreeNode> extension2treeNode = new ArrayMap<>(String.class, HGTreeNode.class);

    public ProjectManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);
    }

    @Override
    protected void init() {
        // https://github.com/kotcrab/vis-ui/blob/master/ui/src/test/java/com/kotcrab/vis/ui/test/manual/TestTree.java#L75
        projectTree = new VisTree<>();
        projectTree.add(assetsTreeNode = new HGTreeNode(new VisLabel("Assets", Color.BLACK)));
        projectTree.add(modelInstancesTreeNode = new HGTreeNode(new VisLabel("Model Instances", Color.BLACK)));
        projectTree.add(envTreeNode = new HGTreeNode(new VisLabel("Environment", Color.BLACK)));

        assetsTreeNode.add(assetsModelsTreeNode = new HGTreeNode(new VisLabel("Models", Color.BLACK)));
        assetsTreeNode.add(assetsImagesTreeNode = new HGTreeNode(new VisLabel("Images", Color.BLACK)));
        assetsTreeNode.add(assetsSoundsTreeNode = new HGTreeNode(new VisLabel("Sounds", Color.BLACK)));
        assetsTreeNode.add(assetsFontsTreeNode = new HGTreeNode(new VisLabel("Fonts", Color.BLACK)));

        projectTreeScrollPane = new VisScrollPane(projectTree);
    }

    public static class HGTreeNode extends Tree.Node<HGTreeNode, Integer, VisLabel> {
        public HGTreeNode(VisLabel actor) {
            super(actor);
        }
    }

    public void updateAssetsTree() {
        assetsModelsTreeNode.clearChildren();
        for (ObjectMap.Entry<FileHandle, HGModel> entry: eng.hgModels) {
            HGTreeNode node;
            assetsModelsTreeNode.add(node = new HGTreeNode(new VisLabel(entry.key.nameWithoutExtension(), Color.BLACK)));
            node.add(new HGTreeNode(new VisLabel(entry.key.path(), Color.BLACK)));
        }

        assetsImagesTreeNode.clearChildren();
        for (ObjectMap.Entry<FileHandle, HGTexture> entry: eng.hgTextures) {
            HGTreeNode node;
            assetsImagesTreeNode.add(node = new HGTreeNode(new VisLabel(entry.key.nameWithoutExtension(), Color.BLACK)));
            node.add(new HGTreeNode(new VisLabel(entry.key.path(), Color.BLACK)));
        }
    }

    public void addAssetTreeNode(FileHandle fileHandle) {
        Class<?> assetClass = eng.getAssetClass(fileHandle);
        String extension = fileHandle.extension().toUpperCase();
        HGTreeNode treeNode = extension2treeNode.get(extension, null);
        //Gdx.app.debug("project", "add asset" + " filehandle: " + fileHandle + " extension: " + extension + " treeNode: " + treeNode);
        if (treeNode == null) {
            treeNode = new HGTreeNode(new VisLabel(extension, Color.BLACK));
            if (assetClass.equals(Model.class)) { assetsModelsTreeNode.add(treeNode); }
            else if (assetClass.equals(Texture.class)) { assetsImagesTreeNode.add(treeNode); }
            else { return; }
            extension2treeNode.put(extension, treeNode);
        }

        if (assetClass == Model.class) {
            addModelAssetTreeNode(fileHandle, treeNode);
        } else if (assetClass == Texture.class) {
            addImageAssetTreeNode(fileHandle, treeNode);
        }
    }

    public void addModelAssetTreeNode(FileHandle fileHandle, HGTreeNode treeNode) {
        HGTreeNode node;
        treeNode.add(node = new HGTreeNode(new VisLabel(fileHandle.nameWithoutExtension(), Color.BLACK)));
        node.add(new HGTreeNode(new VisLabel(fileHandle.file().getAbsolutePath(), Color.BLACK)));
    }

    public void addImageAssetTreeNode(FileHandle fileHandle, HGTreeNode treeNode) {
        HGTreeNode node;
        treeNode.add(node = new HGTreeNode(new VisLabel(fileHandle.nameWithoutExtension(), Color.BLACK)));
        node.add(new HGTreeNode(new VisLabel(fileHandle.file().getAbsolutePath(), Color.BLACK)));
    }

    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);
    }

    @Override
    public void resetActors() {
        super.resetActors();

        VisTable table = new VisTable();
        table.add(projectTreeScrollPane).expand().fill();
        table.add().expand().fill();
        stage.infoTCell.setActor(table);
    }

    @Override
    public void applyLocale() {
        super.applyLocale();
    }
}
