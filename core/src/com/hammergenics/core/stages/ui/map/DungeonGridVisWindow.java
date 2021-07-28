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

package com.hammergenics.core.stages.ui.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisWindow;
import com.hammergenics.map.HGGrid;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class DungeonGridVisWindow extends ContextAwareVisWindow {
    public VisTextButton genDungeonTextButton;
    public Texture textureDungeon;

    public DungeonGridVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Dungeon Grid", modelES, stage);

        genDungeonTextButton = new VisTextButton("gen dungeon grid");
        stage.unpressButton(genDungeonTextButton);
        genDungeonTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                eng.generateDungeon();
                textureDungeon = stage.mapGenerationTable.imageGrid("Dungeon Grid Preview", new Array<>(new HGGrid[]{eng.gridDungeon}));
                return super.touchDown(event, x, y, pointer, button); // false
                // If true is returned, this listener will have touch focus, so it will receive all
                // touchDragged and touchUp events, even those not over this actor, until touchUp is received.
                // Also when true is returned, the event is handled
            }
        });

        add(genDungeonTextButton).center().expandX().fillX();
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}