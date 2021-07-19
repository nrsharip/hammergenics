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

package com.hammergenics.core.stages.ui.physics.bullet.dynamics;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisWindow;
import com.hammergenics.core.stages.ui.physics.bullet.collision.btCollisionObjectVisTable;
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
public class btRigidBodyVisWindow extends ContextAwareVisWindow {
    public VisTable mainTabbedPaneTable;
    public btRigidBodyVisTable rbTable;
    public btCollisionObjectVisTable coTable;

    public btRigidBodyVisWindow(ModelEditScreen modelES, ModelEditStage stage) {
        super("Rigid Body", modelES, stage);

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

        VisScrollPane rbScrollPane = new VisScrollPane(rbTable);
        VisTable rbScrollPaneTable = new VisTable();
        rbScrollPaneTable.add(rbScrollPane);

        VisScrollPane coScrollPane = new VisScrollPane(coTable);
        VisTable coScrollPaneTable = new VisTable();
        coScrollPaneTable.add(coScrollPane);

        tabbedPane.add(new rbTab("Collision Object", coScrollPaneTable));
        tabbedPane.add(new rbTab("Rigid Body", rbScrollPaneTable));

        add(tabbedPane.getTable()).expandX().fillX();
        row();
        add(mainTabbedPaneTable).expandX().center();
    }

    public void init() {
        rbTable = new btRigidBodyVisTable(modelES, stage);
        coTable = new btCollisionObjectVisTable(modelES, stage);
    }

    @Override
    public void setDbgModelInstance(EditableModelInstance mi) {
        super.setDbgModelInstance(mi);
        rbTable.setDbgModelInstance(mi);
        coTable.setDbgModelInstance(mi);
    }

    public static class rbTab extends Tab {
        String title;
        VisTable contentTable;

        public rbTab(String title, VisTable contentTable) {
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