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

package com.hammergenics.screens.stages.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.hammergenics.HGEngine;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.stages.ModelEditStage;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class MapGenerationTable extends HGTable {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;
    public DebugModelInstance dbgModelInstance;

    public TextButton genNoiseTextButton = null;
    public TextButton genCellTextButton = null;
    public TextButton genDungTextButton = null;

    public Texture textureNoise;
    public Texture textureCellular;
    public Texture textureDungeon;

    public MapGenerationTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(stage.skin);
        this.modelES = modelES;
        this.eng = modelES.eng;
        this.stage = stage;

        init();

        add(genNoiseTextButton).center().fillX();
        row();
        add(genCellTextButton).center().fillX();
        row();
        add(genDungTextButton).center().fillX();
        row();
    }

    public void init() {
        genNoiseTextButton = new TextButton("gen noise grid", stage.skin);
        stage.unpressButton(genNoiseTextButton);
        genNoiseTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateNoise();
                imageNoise();
                // see Image (Texture texture) for example on how to convert Texture to Image
                stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureNoise)));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        genCellTextButton = new TextButton("gen cellular grid", stage.skin);
        stage.unpressButton(genCellTextButton);
        genCellTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateCellular();
                imageCellular();
                // see Image (Texture texture) for example on how to convert Texture to Image
                stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureCellular)));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        genDungTextButton = new TextButton("gen dungeon grid", stage.skin);
        stage.unpressButton(genDungTextButton);
        genDungTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateDungeon();
                imageDungeon();
                // see Image (Texture texture) for example on how to convert Texture to Image
                stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(textureDungeon)));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });
    }

    // see: https://github.com/czyzby/noise4j
    public void imageNoise() {
        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

        final Color color = new Color();
        for (int x = 0; x < eng.gridNoise.getWidth(); x++) {
            for (int y = 0; y < eng.gridNoise.getHeight(); y++) {
                final float cell = eng.gridNoise.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }
        textureNoise = new Texture(map);
        map.dispose();
    }

    // see: https://github.com/czyzby/noise4j
    public void imageCellular() {
        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
        final Color color = new Color();
        for (int x = 0; x < eng.gridCellular.getWidth(); x++) {
            for (int y = 0; y < eng.gridCellular.getHeight(); y++) {
                final float cell = eng.gridCellular.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }

        textureCellular = new Texture(map);
        map.dispose();
    }

    // see: https://github.com/czyzby/noise4j
    public void imageDungeon() {
        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);

        final Color color = new Color();
        for (int x = 0; x < eng.gridDungeon.getWidth(); x++) {
            for (int y = 0; y < eng.gridDungeon.getHeight(); y++) {
                final float cell = 1f - eng.gridDungeon.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }

        textureDungeon = new Texture(map);
        map.dispose();
    }

    public void resetActors() {
        stage.infoTCell.clearActor();
        stage.infoBCell.clearActor();
        stage.editCell.clearActor();

        stage.infoBCell.setActor(stage.textureImage);
        stage.editCell.setActor(this);
    }
}
