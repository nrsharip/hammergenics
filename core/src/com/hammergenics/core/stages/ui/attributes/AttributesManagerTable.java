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

package com.hammergenics.core.stages.ui.attributes;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AttributesManagerTable extends VisTable {
    public Attributes container;
    protected BaseAttributeTable.EventListener listener = null;
    protected ModelEditScreen modelES;

    public AttributesTable.Color colorAttrTable;
    public AttributesTable.Texture textureAttrTable;
    public AttributesTable.Blending blendingAttrTable;
    public AttributesTable.DirectionalLights dlAttrTable;
    public AttributesTable.PointLights plAttrTable;
    public AttributesTable.SpotLights slAttrTable;

    public AttributesManagerTable(Attributes container, ModelEditScreen modelES) {
        this.container = container;
        this.modelES = modelES;

        textureAttrTable = new AttributesTable.Texture(container, modelES);
        colorAttrTable = new AttributesTable.Color(container, modelES);
        blendingAttrTable = new AttributesTable.Blending(container, modelES);
        dlAttrTable = new AttributesTable.DirectionalLights(container, modelES);
        plAttrTable = new AttributesTable.PointLights(container, modelES);
        slAttrTable = new AttributesTable.SpotLights(container, modelES);

        textureAttrTable.resetAttributes();
        colorAttrTable.resetAttributes();
        blendingAttrTable.resetAttributes();
        dlAttrTable.resetAttributes();
        plAttrTable.resetAttributes();
        slAttrTable.resetAttributes();

        add(colorAttrTable.window).expand().top().left();
        add(textureAttrTable.window).expand().top().right();

        VisTable lights = new VisTable();
        lights.add(dlAttrTable.window);
        lights.add(plAttrTable.window);
        lights.add(slAttrTable.window);

        row();
        add(lights).expand().bottom().left();
        add(blendingAttrTable.window).expand().bottom().right();
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

    public void applyLocale() {

    }
}
