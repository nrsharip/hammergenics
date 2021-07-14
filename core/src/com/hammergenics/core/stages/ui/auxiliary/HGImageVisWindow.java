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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

public class HGImageVisWindow extends VisWindow {
    public HGImageVisTable table;
    public Cell<HGImageVisTable> cell;

    public HGImageVisWindow() { this(true); }

    public HGImageVisWindow(boolean close) {
        super("");
        getTitleLabel().setText("Image Preview");
        setResizable(true);
        setResizeBorder(16);
        if (close) { addCloseButton(); }
        setMovable(false);
        cell = add(table = new HGImageVisTable()).expand().fill();
    }

    public static class HGImageVisTable extends VisTable {
        public final Image image = new Image();
        public final Cell<Image> cell;

        public HGImageVisTable() {
            image.setPosition(0f, 0f);
            image.setScaling(Scaling.fit);
            image.setAlign(Align.center);

            cell = add(image).expand().fill();
        }

        public void setImage(Texture texture) {
            image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        }
        public void clearImage() { image.setDrawable(null); }
    }

    public VisWindow showImageWindow(Texture texture) {
        table.clearImage();
        table.setImage(texture);

        int width = texture.getWidth();
        int height = texture.getHeight();
        float scaleX = (Gdx.graphics.getWidth()/1.5f) / width;
        float scaleY = (Gdx.graphics.getHeight()/1.5f) / height;
        float scale = Math.min(scaleX, scaleY);

        setMovable(true);
        setWidth(scale * texture.getWidth());
        setHeight(scale * texture.getHeight());

        centerWindow();
        return fadeIn();
    }

    public void hideImageWindow() {
        table.clearImage();
        pack();
        fadeOut();
    }
}