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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.map.CellularGridVisWindow;
import com.hammergenics.core.stages.ui.map.DungeonGridVisWindow;
import com.hammergenics.core.stages.ui.map.NoiseGridVisWindow;
import com.hammergenics.core.stages.ui.map.TerrainVisWindow;
import com.hammergenics.map.HGGrid;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class MapGenerationTable extends ManagerTable {
    public NoiseGridVisWindow noiseGridVisWindow;
    public CellularGridVisWindow cellularGridVisWindow;
    public DungeonGridVisWindow dungeonGridVisWindow;
    public TerrainVisWindow terrainVisWindow;

    public MapGenerationTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        noiseGridVisWindow = new NoiseGridVisWindow(modelES, stage);
        cellularGridVisWindow = new CellularGridVisWindow(modelES, stage);
        dungeonGridVisWindow = new DungeonGridVisWindow(modelES, stage);
        terrainVisWindow = new TerrainVisWindow(modelES, stage);

        VisTable windows = new VisTable();

        windows.add(noiseGridVisWindow).expandX().fillX();
        windows.row();
        windows.add(cellularGridVisWindow).expandX().fillX();
        windows.row();
        windows.add(dungeonGridVisWindow).expandX().fillX();
        windows.row();
        windows.add(terrainVisWindow).expandX().fillX();
        windows.row();

        add(windows).expand().top().right();
    }

    @Override
    protected void init() {

    }

    // see: https://github.com/czyzby/noise4j
    public static Texture imageGrid(Array<HGGrid> grids) {

        Rectangle combined = HGGrid.getCombinedBounds(grids, new Rectangle());
        Gdx.app.debug("image", ""
                + " w: " + (int)combined.getWidth()
                + " h: " + (int)combined.getHeight());
        Pixmap map = new Pixmap((int)combined.getWidth(), (int)combined.getHeight(), Pixmap.Format.RGBA8888);

        final Color color = new Color();
        int x0, y0;
        for (HGGrid grid: grids) {
            x0 = grid.getX0();
            y0 = grid.getZ0();
            Gdx.app.debug("image", "x0: " + x0 + " y0: " + y0);
            for (int x = 0; x < grid.getWidth(); x++) {
                for (int y = 0; y < grid.getHeight(); y++) {
                    final float cell = grid.get(x, y);
                    color.set(cell, cell, cell, 1f);
                    map.drawPixel(x + x0 - (int)combined.x, y + y0 - (int)combined.y, Color.rgba8888(color));
                }
            }
        }

        if ((int)combined.getWidth() == (int)combined.getHeight() && (int)combined.getWidth() < 512) {
            Pixmap other = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

            // see: https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/Pixmap.html
            // Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch
            // the source image to the specified target rectangle. Use setFilter(Filter) to specify the type
            // of filtering to be used (nearest neighbour or bilinear).
            other.drawPixmap(map,
                    0, 0, (int)combined.getWidth(), (int)combined.getHeight(),
                    0, 0, 512, 512);
            map.dispose();
            map = other;
        }

        Texture texture = new Texture(map);
        map.dispose();

        // see Image (Texture texture) for example on how to convert Texture to Image
        //stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));

        return texture;
    }

    @Override
    public void resetActors() {
        super.resetActors();

        stage.infoBCell.setActor(stage.textureImage);
    }

    public void applyLocale() {

    }
}
