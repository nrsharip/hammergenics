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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.hammergenics.HGEngine;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum;

import static com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum.IMAGE_FILES;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ImageChooser extends AssetChooser {
    // image preview part
    public HGImageVisWindow imagePreviewWindow;
    public Cell<HGImageVisWindow> imagePreviewCell;

    public ImageChooser(HGEngine engine, ModelEditStage stage) {
        super(engine, stage);

        getTitleLabel().setText("Choose Image");

        imagePreviewWindow = new HGImageVisWindow(false);
        imagePreviewWindow.table.clearImage();
        imagePreviewCell = add(imagePreviewWindow);
        hideImagePreview();

        pack();
        centerWindow();
    }

    public void showImagePreview(FileHandle fileHandle) {
        imagePreviewWindow.table.setImage(engine.getAsset(fileHandle, Texture.class));
        imagePreviewCell.expand().setActor(imagePreviewWindow);
        imagePreviewCell.minWidth(Gdx.graphics.getWidth()/5f).minHeight(Gdx.graphics.getHeight()/3f)
                        .maxWidth(Gdx.graphics.getWidth()/5f).maxHeight(Gdx.graphics.getHeight()/3f);
        pack();
    }

    public void hideImagePreview() {
        imagePreviewWindow.table.clearImage();
        imagePreviewCell.expand(false, false).clearActor();
        imagePreviewCell.minWidth(0f).minHeight(0f);

        pack();
    }

    @Override
    public HGTreeVisTableNode getAssetsNode()  {
        HGTreeVisTableNode assetsNode = new HGTreeVisTableNode(new HGTreeVisTableNode.HGTreeVisTable("Images"));
        if (stage.projManagerTable != null) {
            stage.projManagerTable.fillTreeNodesWithAssets(null, assetsNode, null);
        }
        return assetsNode;
    }

    @Override
    public TypeFilterRulesEnum getTypeFilterRule() { return IMAGE_FILES; }

    @Override
    public void tap1(InputEvent event, float x, float y, int count, int button) {
        showImagePreview(((HGTreeVisTableNode.HGTreeVisTable)event.getTarget().getParent()).fileHandle);
        super.tap1(event, x, y, count, button);
    }

    @Override
    public void handleNonSelect(InputEvent event, float x, float y, int count, int button) {
        hideImagePreview();
        super.handleNonSelect(event, x, y, count, button);
    }
}