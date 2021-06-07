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

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.stages.ui.attributes.*;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AttributesManagerTable extends HGTable {
    public Attributes container;
    protected BaseAttributeTable.EventListener listener = null;
    protected ModelEditScreen modelES;

    public ColorAttributesTable colorAttrTable;
    public TextureAttributesTable textureAttrTable;
    public BlendingAttributesTable blendingAttrTable;
    public DirectionalLightsAttributesTable dlAttrTable;
    public PointLightsAttributesTable plAttrTable;
    public SpotLightsAttributesTable slAttrTable;

    public AttributesManagerTable(Skin skin, Attributes container, ModelEditScreen modelES) {
        super(skin);
        this.container = container;
        this.modelES = modelES;

        textureAttrTable = new TextureAttributesTable(skin, container, modelES);
        colorAttrTable = new ColorAttributesTable(skin, container, modelES);
        blendingAttrTable = new BlendingAttributesTable(skin, container, modelES);
        dlAttrTable = new DirectionalLightsAttributesTable(skin, container, modelES);
        plAttrTable = new PointLightsAttributesTable(skin, container, modelES);
        slAttrTable = new SpotLightsAttributesTable(skin, container, modelES);

        textureAttrTable.resetAttributes();
        colorAttrTable.resetAttributes();
        blendingAttrTable.resetAttributes();
        dlAttrTable.resetAttributes();
        plAttrTable.resetAttributes();
        slAttrTable.resetAttributes();

        clear();
        add(colorAttrTable).padTop(10f).top().left().fillX();
        row();
        add(textureAttrTable).padTop(10f).top().left().fillX();
        row();
        add(blendingAttrTable).padTop(10f).top().left().fillX();
        row();
        add(dlAttrTable).padTop(10f).top().left().fillX();
        row();
        add(plAttrTable).padTop(10f).top().left().fillX();
        row();
        add(slAttrTable).padTop(10f).top().left().fillX();
        row();
        add().expandY();
    }

    public void setListener(BaseAttributeTable.EventListener listener) {
        this.listener = listener;
        textureAttrTable.setListener(listener);
        colorAttrTable.setListener(listener);
        blendingAttrTable.setListener(listener);
        dlAttrTable.setListener(listener);
        plAttrTable.setListener(listener);
        slAttrTable.setListener(listener);
    }
}
