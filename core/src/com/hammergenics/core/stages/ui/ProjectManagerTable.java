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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.Array;
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
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTree;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ProjectManagerTable extends ManagerTable {
    public VisTree<HGVisTableTreeNode, VisLabel> projectTree;
    public VisScrollPane projectTreeScrollPane;

    public HGVisTableTreeNode assetsTreeNode;
    public HGVisTableTreeNode assetsModelsTreeNode;
    public HGVisTableTreeNode assetsImagesTreeNode;
    public HGVisTableTreeNode assetsSoundsTreeNode;
    public HGVisTableTreeNode assetsFontsTreeNode;
    public HGVisTableTreeNode modelInstancesTreeNode;
    public HGVisTableTreeNode envTreeNode;

    public FileHandle commonPath = null;
    public final ArrayMap<HGVisTableTreeNode, FileHandle> treeNode2fh = new ArrayMap<>(HGVisTableTreeNode.class, FileHandle.class);
    public final ArrayMap<FileHandle, HGVisTableTreeNode> fh2treeNode = new ArrayMap<>(FileHandle.class, HGVisTableTreeNode.class);

    public final ArrayMap<String, HGVisTableTreeNode> extension2treeNode = new ArrayMap<>(String.class, HGVisTableTreeNode.class);

    public ProjectManagerTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);
    }

    @Override
    protected void init() {
        // https://github.com/kotcrab/vis-ui/blob/master/ui/src/test/java/com/kotcrab/vis/ui/test/manual/TestTree.java#L75
        projectTree = new VisTree<>();
        projectTree.add(assetsTreeNode = new HGVisTableTreeNode(new HGVisTable("Assets", Color.BLACK)));
        projectTree.add(modelInstancesTreeNode = new HGVisTableTreeNode(new HGVisTable("Model Instances", Color.BLACK)));
        projectTree.add(envTreeNode = new HGVisTableTreeNode(new HGVisTable("Environment", Color.BLACK)));

        assetsTreeNode.add(assetsModelsTreeNode = new HGVisTableTreeNode(new HGVisTable("Models", Color.BLACK)));
        assetsTreeNode.add(assetsImagesTreeNode = new HGVisTableTreeNode(new HGVisTable("Images", Color.BLACK)));
        assetsTreeNode.add(assetsSoundsTreeNode = new HGVisTableTreeNode(new HGVisTable("Sounds", Color.BLACK)));
        assetsTreeNode.add(assetsFontsTreeNode = new HGVisTableTreeNode(new HGVisTable("Fonts", Color.BLACK)));

        projectTreeScrollPane = new VisScrollPane(projectTree);
    }

    private static class HGVisTableTreeNode extends Tree.Node<HGVisTableTreeNode, Integer, HGVisTable> {
        public HGVisTableTreeNode(HGVisTable actor) { super(actor); }
    }

    private static class HGVisTable extends VisTable {
        public Cell<?> cell1, cell2, cell3, cell4;
        public VisLabel label;
        public FileHandle fileHandle;

        public HGVisTable(CharSequence text, Color textColor) { this(text, textColor, null); }

        public HGVisTable(CharSequence text, Color textColor, FileHandle fileHandle) {
            this.label = new VisLabel(text, textColor);
            this.fileHandle = fileHandle;

            cell1 = add().padRight(2f); cell2 = add().padRight(2f);
            cell3 = add().padRight(2f); cell4 = add().padRight(2f);
            add(this.label);
        }

        public FileHandle getFileHandle() { return fileHandle; }
        public HGVisTable setCell1(Actor actor) { this.cell1.setActor(actor); return this; }
        public HGVisTable setCell2(Actor actor) { this.cell2.setActor(actor); return this; }
        public HGVisTable setCell3(Actor actor) { this.cell3.setActor(actor); return this; }
        public HGVisTable setCell4(Actor actor) { this.cell4.setActor(actor); return this; }
    }

    public void updateAssetsTree() {
        assetsModelsTreeNode.clearChildren();
        for (ObjectMap.Entry<FileHandle, HGModel> entry: eng.hgModels) {
            HGVisTableTreeNode node;
            assetsModelsTreeNode.add(node = new HGVisTableTreeNode(new HGVisTable(entry.key.nameWithoutExtension(), Color.BLACK)));
            node.add(new HGVisTableTreeNode(new HGVisTable(entry.key.path(), Color.BLACK)));
        }

        assetsImagesTreeNode.clearChildren();
        for (ObjectMap.Entry<FileHandle, HGTexture> entry: eng.hgTextures) {
            HGVisTableTreeNode node;
            assetsImagesTreeNode.add(node = new HGVisTableTreeNode(new HGVisTable(entry.key.nameWithoutExtension(), Color.BLACK)));
            node.add(new HGVisTableTreeNode(new HGVisTable(entry.key.path(), Color.BLACK)));
        }
    }

    public void addAssetTreeNode(FileHandle fileHandle) {
        if (fileHandle == null || fileHandle.isDirectory()) { return; }

        Class<?> assetClass = eng.getAssetClass(fileHandle);
        FileHandle parent = fileHandle.parent();

        HGVisTableTreeNode treeNode = fh2treeNode.get(parent);
        if (treeNode == null) {
            treeNode = new HGVisTableTreeNode(new HGVisTable(parent.file().getAbsolutePath(), Color.BLACK, parent));
            if (assetClass.equals(Model.class)) {
                VisTextButton createMisTB = new VisTextButton("create instances");
                createMisTB.addListener(new InputListener(){
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        Array<FileHandle> fhs;
                        // by this time fh2treeNode.get(parent) should return a real node
                        fhs = Arrays.stream(fh2treeNode.get(parent).getChildren().toArray()) // Array<HGVisTableTreeNode> -> HGVisTableTreeNode[]
                                .map(Tree.Node::getActor)                                    // HGVisTableTreeNode -> HGVisTable
                                .map(HGVisTable::getFileHandle)                              // HGVisTable -> FileHandle
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
                assetsModelsTreeNode.add(treeNode);
            } else if (assetClass.equals(Texture.class)) {
                assetsImagesTreeNode.add(treeNode);
            } else {
                return;
            }

            fh2treeNode.put(parent, treeNode);
            treeNode2fh.put(treeNode, parent);
        }

        if (commonPath == null) { commonPath = parent; }

        if (!parent.file().getAbsolutePath().startsWith(commonPath.file().getAbsolutePath())) {
            // commonPath = /a/b/c/d/e
            //     parent = /a/b/c/f/g/h

            String abs1 = commonPath.file().getAbsolutePath();
            String abs2 = fileHandle.parent().file().getAbsolutePath();
            //Gdx.app.debug("project", "" + " abs1: " + abs1);
            //Gdx.app.debug("project", "" + " abs2: " + abs2);
            Array<String> folders1 = new Array<>(abs1.split(Pattern.quote(File.separator)));
            Array<String> folders2 = new Array<>(abs2.split(Pattern.quote(File.separator)));
            //Gdx.app.debug("project", "" + " folders1: " + folders1.toString("|"));
            //Gdx.app.debug("project", "" + " folders2: " + folders2.toString("|"));

            int i = 0;
            while((i < folders1.size) && (i < folders2.size) && folders1.get(i).equals(folders2.get(i++)));
            commonPath = new FileHandle(String.join(File.separator, Arrays.copyOfRange(folders1.toArray(), 0, i-1)));
            //Gdx.app.debug("project", "" + " commonPath: " + commonPath);
        }

        for (ObjectMap.Entry<FileHandle, HGVisTableTreeNode> entry: fh2treeNode) {
            FileHandle fh = entry.key;
            HGVisTableTreeNode tn = entry.value;

            tn.getActor().label.setText(fh.file().getAbsolutePath().replace(commonPath.file().getAbsolutePath(), ""));
        }

        if (assetClass == Model.class) {
            addModelAssetTreeNode(fileHandle, treeNode);
        } else if (assetClass == Texture.class) {
            addImageAssetTreeNode(fileHandle, treeNode);
        }
    }

    public void addModelAssetTreeNode(final FileHandle fileHandle, HGVisTableTreeNode treeNode) {
        HGVisTableTreeNode node;

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
        treeNode.add(node = new HGVisTableTreeNode(new HGVisTable(fileHandle.name(), Color.BLACK, fileHandle)
                .setCell1(createMisTB).setCell2(unloadTB)));
        node.add(new HGVisTableTreeNode(new HGVisTable(fileHandle.file().getAbsolutePath(), Color.BLACK)));
    }

    public void addImageAssetTreeNode(FileHandle fileHandle, HGVisTableTreeNode treeNode) {
        HGVisTableTreeNode node;
        treeNode.add(node = new HGVisTableTreeNode(new HGVisTable(fileHandle.name(), Color.BLACK, fileHandle)));
        node.add(new HGVisTableTreeNode(new HGVisTable(fileHandle.file().getAbsolutePath(), Color.BLACK)));
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
