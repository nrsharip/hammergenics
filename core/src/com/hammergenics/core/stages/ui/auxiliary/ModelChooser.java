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

import com.hammergenics.HGEngine;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum;

import static com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum.MODEL_FILES;

public class ModelChooser extends AssetChooser {
    public ModelChooser(HGEngine engine, ModelEditStage stage) {
        super(engine, stage);

        getTitleLabel().setText("Choose Model");
    }

    @Override
    public HGTreeVisTableNode getAssetsNode()  {
        HGTreeVisTableNode assetsNode = new HGTreeVisTableNode(new HGTreeVisTableNode.HGTreeVisTable("Models"));
        if (stage.projManagerTable != null) {
            stage.projManagerTable.fillTreeNodesWithAssets(assetsNode, null);
        }
        return assetsNode;
    }

    @Override public TypeFilterRulesEnum getTypeFilterRule() { return MODEL_FILES; }
}