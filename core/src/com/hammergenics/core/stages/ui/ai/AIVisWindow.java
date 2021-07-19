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

package com.hammergenics.core.stages.ui.ai;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisWindow;
import com.hammergenics.core.stages.ui.ai.steer.SteeringVisTable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AIVisWindow extends ContextAwareVisWindow {
    public VisTable mainTabbedPaneTable;
    public SteeringVisTable steeringTable;

    public AIVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("AI Algorithms", modelES, stage);

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

    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);
        steeringTable.setDbgModelInstance(mi);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        steeringTable.update(delta);
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

    @Override
    public void applyLocale() { }
}