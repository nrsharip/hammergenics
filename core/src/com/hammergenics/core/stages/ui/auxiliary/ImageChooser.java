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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import static com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum.IMAGE_FILES;

public class ImageChooser extends VisWindow {
    public HGEngine engine;
    public ModelEditStage stage;

    public VisTree<HGTreeVisTableNode, VisLabel> imageTree;
    public VisScrollPane imageTreeScrollPane;
    public VisTextButton chooseFileTB;

    public ImageChooser(HGEngine engine, ModelEditStage stage) {
        super("Choose Image");

        this.engine = engine;
        this.stage = stage;

        setResizable(true);
        closeOnEscape();
        addCloseButton();
        setMovable(true);

        imageTree = new VisTree<>();
        HGTreeVisTableNode imagesNode = new HGTreeVisTableNode(new HGTreeVisTableNode.HGTreeVisTable("Images"));
        imageTree.add(imagesNode);
        imageTree.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                pack();
                centerWindow();
            }
        });
        stage.projManagerTable.fillTreeNodesWithAssets(null, imagesNode);
        imageTree.expandAll();

        imageTreeScrollPane = new VisScrollPane(imageTree);

        chooseFileTB = new VisTextButton("Load File...");
        chooseFileTB.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // https://github.com/kotcrab/vis-ui/wiki/File-chooser#typefilters
                // FileTypeFilter allows user to filter file chooser list by given set of extension. If type filter is set
                // for chooser then below file path select box with possible extensions is displayed. If user switches filter
                // rule then only extensions allowed in that rule will be displayed (directories are also displayed of course)
                FileTypeFilter typeFilter = new FileTypeFilter(false); //allow "All Types" mode where all files are shown
                typeFilter.addRule(IMAGE_FILES.getDescription(), IMAGE_FILES.getExtensions());
                stage.fileChooser.setFileTypeFilter(typeFilter);

                // https://github.com/kotcrab/vis-ui/wiki/File-chooser#selection-mode
                // The following selection modes are available: FILES, DIRECTORIES, FILES_AND_DIRECTORIES.
                // Please note that if dialog is in DIRECTORIES mode files still will be displayed, if user tries
                // to select file, error message will be showed. Default selection mode is SelectionMode.FILES.
                stage.fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

                // https://github.com/kotcrab/vis-ui/wiki/File-chooser#multiple-selection
                // Chooser allow to select multiple files. It is disabled by default, to enable it call:
                stage.fileChooser.setMultiSelectionEnabled(false);

                stage.fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> fileHandles) {
                        Gdx.app.debug("filechooser", "\n" + fileHandles.toString("\n"));

                        if (engine.assetManager.getQueuedAssets() > 0) { return; } // another load is in progress
                        engine.loadQueue.clear();

                        stage.loadProgressBar.setValue(0f);
                        stage.addActor(stage.loadProgressWindow.fadeIn());

                        for (FileHandle fh: fileHandles) {
                            if (!fh.isDirectory()) { engine.queueAsset(fh); }
                        }
                    }
                });

                //displaying chooser with fade in animation
                stage.addActor(stage.fileChooser.fadeIn());

                return super.touchDown(event, x, y, pointer, button);
            }
        });

        add(imageTreeScrollPane).fill().expand().maxWidth(Gdx.graphics.getWidth()/2f).maxHeight(Gdx.graphics.getHeight()/2f);
        row();
        add(chooseFileTB);

        pack();
        centerWindow();

        fadeIn();
    }
}