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

package com.hammergenics.core.stages.ui.auxiliary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.widget.ButtonBar;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import static com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum.ALL_FILES;

public abstract class AssetChooser extends VisWindow {
    public HGEngine engine;
    public ModelEditStage stage;

    // chooser part
    public VisTree<HGTreeVisTableNode, VisLabel> assetsTree;
    public VisScrollPane assetsTreeScrollPane;
    public VisTextButton okTextButton;
    public VisTextButton cancelTextButton;
    public VisTextButton chooseFileTB;
    public Cell<VisScrollPane> scrollPaneCell;
    public ButtonBar btnBar;

    public ConfirmDialogListener<FileHandle> listener;

    public ActorGestureListener nonSelectListener;
    public ActorGestureListener selectListener;

    public AssetChooser(HGEngine engine, ModelEditStage stage) {
        super("Choose Asset");

        this.engine = engine;
        this.stage = stage;

        initChooser();

        VisTable chooserTable = new VisTable();
        scrollPaneCell = chooserTable.add(assetsTreeScrollPane).fill().expand();
        chooserTable.row();
        chooserTable.add(btnBar.createTable()).expandX().fillX();

        add(chooserTable).expand().fill();

        pack();
        centerWindow();
    }

    public void tap1(InputEvent event, float x, float y, int count, int button) { }
    public void tap2(InputEvent event, float x, float y, int count, int button) { }
    public void handleNonSelect(InputEvent event, float x, float y, int count, int button) { }

    public void updateAssetsTree() {
        assetsTree.clearChildren();

        HGTreeVisTableNode assetsNode = getAssetsNode();
        assetsNode.setExpanded(true);
        assetsNode.getActor().addListener(nonSelectListener);
        assetsTree.add(assetsNode);

        for (HGTreeVisTableNode node1: assetsNode.getChildren()) {
            node1.getActor().addListener(nonSelectListener);
            for (HGTreeVisTableNode node2: node1.getChildren()) {
                node2.getActor().addListener(selectListener);
            }
        }

        assetsNode.expandTo();
        if (scrollPaneCell != null) {
            scrollPaneCell
                    .minWidth(Gdx.graphics.getWidth()/5f).minHeight(Gdx.graphics.getHeight()/3f)
                    .maxWidth(Gdx.graphics.getWidth()/2f).maxHeight(Gdx.graphics.getHeight()/3f);
        }
        pack();
        centerWindow();
    }

    public abstract HGTreeVisTableNode getAssetsNode();
    public TypeFilterRulesEnum getTypeFilterRule() { return ALL_FILES; }

    public void initChooser() {
        setResizable(true);
        closeOnEscape();
        addCloseButton();
        setMovable(true);
        setResizeBorder(16);

        assetsTree = new VisTree<>();
        assetsTree.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                pack();
            }
        });
        assetsTreeScrollPane = new VisScrollPane(assetsTree);

        nonSelectListener = new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                okTextButton.setDisabled(true);
                handleNonSelect(event, x, y, count, button);
                super.tap(event, x, y, count, button);
                pack();
            }
        };
        selectListener = new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                okTextButton.setDisabled(false);
                if (count == 1) { // single click
                    Gdx.app.debug("asset chooser", "select: tap 1");
                    tap1(event, x, y, count, button);
                } else if ( count == 2 ) { // double click
                    Gdx.app.debug("asset chooser", "select: tap 2");
                    tap2(event, x, y, count, button);
                    if (listener != null) { listener.result(assetsTree.getSelectedNode().getActor().fileHandle); }
                }
                super.tap(event, x, y, count, button);
                pack();
            }
        };

        btnBar = new ButtonBar();
        ChangeListener okBtnListener = new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                if (listener != null) { listener.result(assetsTree.getSelectedNode().getActor().fileHandle); }
                clearListener();
                fadeOut();
            }
        };
        ChangeListener cancelBtnListener = new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                clearListener();
                fadeOut();
            }
        };
        ChangeListener fileBtnListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                // https://github.com/kotcrab/vis-ui/wiki/File-chooser#typefilters
                // FileTypeFilter allows user to filter file chooser list by given set of extension. If type filter is set
                // for chooser then below file path select box with possible extensions is displayed. If user switches filter
                // rule then only extensions allowed in that rule will be displayed (directories are also displayed of course)
                FileTypeFilter typeFilter = new FileTypeFilter(false); //allow "All Types" mode where all files are shown
                typeFilter.addRule(getTypeFilterRule().getDescription(), getTypeFilterRule().getExtensions());
                stage.fileChooser.setFileTypeFilter(typeFilter);

                // https://github.com/kotcrab/vis-ui/wiki/File-chooser#selection-mode
                // The following selection modes are available: FILES, DIRECTORIES, FILES_AND_DIRECTORIES.
                // Please note that if dialog is in DIRECTORIES mode files still will be displayed, if user tries
                // to select file, error message will be showed. Default selection mode is SelectionMode.FILES.
                stage.fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

                // https://github.com/kotcrab/vis-ui/wiki/File-chooser#multiple-selection
                // Chooser allow to select multiple files. It is disabled by default, to enable it call:
                stage.fileChooser.setMultiSelectionEnabled(true);

                // this event fires on file actually selected in file chooser
                stage.fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> fileHandles) {
                        Gdx.app.debug("filechooser", "\n" + fileHandles.toString("\n"));

                        if (engine.assetManager.getQueuedAssets() > 0) { return; } // another load is in progress
                        engine.loadQueue.clear();
                        engine.clearLoadListeners();
                        engine.addLoadListener(new HGEngine.LoadListener.LoadAdapter() {
                            @Override
                            public void update(boolean result) {
                                // waiting until the asset manager finishes the load
                                if (result) {
                                    // IMPORTANT: updateAssetsTree is performed by the root listener of the
                                    //            load progress bar of the stage (see stage.prepProgressBarForLoad())
                                }
                                super.update(result);
                            }
                        });

                        stage.prepProgressBarForLoad();

                        for (FileHandle fh: fileHandles) {
                            // TODO: FILES_AND_DIRECTORIES mode is disabled.
                            // queueAssets(...) currently uses ALL_FILES.getFileFilter(), so all files are being loaded
                            // with no regards to stage.fileChooser.getActiveFileTypeFilterRule()
                            if (fh.isDirectory()) { engine.queueAssets(fh, stage.fileChooser.getActiveFileTypeFilterRule()); }
                            else { engine.queueAsset(fh); }
                        }
                    }
                });

                //displaying chooser with fade in animation
                stage.addActor(stage.fileChooser.fadeIn());
            }
        };

        okTextButton = new VisTextButton("OK");
        cancelTextButton = new VisTextButton("Cancel");
        chooseFileTB = new VisTextButton("Choose File(s)...");
        btnBar.setButton(ButtonBar.ButtonType.LEFT, chooseFileTB, fileBtnListener);
        btnBar.setButton(ButtonBar.ButtonType.OK, okTextButton, okBtnListener);
        btnBar.setButton(ButtonBar.ButtonType.CANCEL, cancelTextButton, cancelBtnListener);

        VisImageButton closeTB = null;
        for (Actor actor: getTitleTable().getChildren()) {
            if (actor instanceof VisImageButton) { closeTB = (VisImageButton) actor; break; }
        }
        if (closeTB != null) {
            closeTB.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    clearListener();
                }
            });
        }
    }

    public void setListener(ConfirmDialogListener<FileHandle> listener) { this.listener = listener; }
    public void clearListener() { this.listener = null; }
}