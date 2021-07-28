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

package com.hammergenics.core.stages.ui.ai.pfa;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
import com.hammergenics.ai.pfa.HGGraphNode;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.ContextAwareVisTable;
import com.hammergenics.core.stages.ui.auxiliary.types.Vector3VisTable;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PathFindingVisTable extends ContextAwareVisTable {
    public VisCheckBox previewGraphNodesGrid;
    public VisCheckBox previewGraphNodesConnectionsGrid;
    public VisCheckBox previewPathSegments;

    public Vector3VisTable fromVisTable;
    public Vector3VisTable toVisTable;

    public PathFindingVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        init();

        VisTable previewCheckBoxes = new VisTable();
        previewCheckBoxes.add(previewGraphNodesGrid).center().expandX().fillX();
        previewCheckBoxes.add(previewGraphNodesConnectionsGrid).center().expandX().fillX();
        previewCheckBoxes.add(previewPathSegments).center().expandX().fillX();
        add(previewCheckBoxes).center().expandX().fillX().colspan(2).row();

        add(fromVisTable.titleL).right();
        add(fromVisTable.valueT).row();
        add(toVisTable.titleL).right();
        add(toVisTable.valueT).row();
    }

    public void init() {
        previewGraphNodesGrid = new VisCheckBox("Preview Graph Nodes");
        previewGraphNodesGrid.setChecked(false);

        previewGraphNodesConnectionsGrid = new VisCheckBox("Preview Graph Nodes Connections");
        previewGraphNodesConnectionsGrid.setChecked(false);

        previewPathSegments = new VisCheckBox("Preview Path Segments");
        previewPathSegments.setChecked(false);

        fromVisTable = new Vector3VisTable(false, true, true, new VisLabel("Move From: "));
        toVisTable  = new Vector3VisTable(false, true, true, new VisLabel("Move To: "));
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);

        if (dbgModelInstance != null) {
            EditableModelInstance pmi = dbgModelInstance;
            EditableModelInstance smi = getSecondaryModelInstance();
            if (smi != null) {
                GraphPath<Connection<HGGraphNode>> outPath = new DefaultGraphPath<>();
                if (eng.pfaGraph.searchConnectionPath(pmi.getPosition(), smi.getPosition(), outPath)) {
                    dbgModelInstance.setOutPath(outPath);
                    dbgModelInstance.steeringEnabled = true;

                    fromVisTable.setVector3(dbgModelInstance.followPath.getStartPoint());
                    toVisTable.setVector3(dbgModelInstance.followPath.getEndPoint());
                } else {
                    dbgModelInstance.setOutPath(null);

                    fromVisTable.setVector3(dbgModelInstance.getPosition());
                    toVisTable.setVector3(null);
                }
            } else {
                dbgModelInstance.setOutPath(null);

                fromVisTable.setVector3(dbgModelInstance.getPosition());
                toVisTable.setVector3(null);
            }
        } else {
            fromVisTable.setVector3(null);
            toVisTable.setVector3(null);
        }
        update(0f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) { }
}