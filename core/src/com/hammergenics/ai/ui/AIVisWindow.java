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

package com.hammergenics.ai.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;

public class AIVisWindow extends VisWindow {
    public ModelEditScreen modelES;
    public ModelEditStage stage;
    public HGEngine eng;

    public VisTable mainTabbedPaneTable;
    public SteeringVisTable steeringTable;

    public AIVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("AI Algorithms");

        this.modelES = modelES;
        this.stage = stage;
        this.eng = modelES.eng;

        init();

        mainTabbedPaneTable = new VisTable();
        TabbedPane.TabbedPaneStyle style = VisUI.getSkin().get("default", TabbedPane.TabbedPaneStyle.class);
        TabbedPane tabbedPane = new TabbedPane(style);
        tabbedPane.addListener(new TabbedPaneAdapter() {
            @Override
            public void switchedTab (Tab tab) {
                mainTabbedPaneTable.clearChildren();
                mainTabbedPaneTable.add(tab.getContentTable()).expand().fill();
            }
        });

        VisScrollPane steeringScrollPane = new VisScrollPane(steeringTable);
        VisTable steeringScrollPaneTable = new VisTable();
        steeringScrollPaneTable.add(steeringScrollPane);

        tabbedPane.add(new aiTab("Steering", steeringScrollPaneTable));

        add(tabbedPane.getTable()).expandX().fillX();
        row();
        add(mainTabbedPaneTable).expandX().center();
    }

    public void init() {
        steeringTable = new SteeringVisTable(modelES, stage);
    }

    public void update(EditableModelInstance mi) {
        steeringTable.updateSteerable(mi);
    }

    public static class aiTab extends Tab {
        String title;
        VisTable contentTable;

        public aiTab(String title, VisTable contentTable) {
            super(false, false);
            this.title = title;
            this.contentTable = contentTable;
        }

        @Override public String getTabTitle() { return title; }
        @Override public Table getContentTable() { return contentTable; }
    }
}