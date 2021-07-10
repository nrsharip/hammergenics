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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TextureAttributesTable extends AttributesTable<TextureAttribute, TextureAttributeTable> {
    // Texture Attribute related
    // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
    private VisSelectBox<String> textureAttributeSelectBox;
    private TextureAttributeTable currentTextureAttributeTable;

    /**
     * @param container
     */
    public TextureAttributesTable(Attributes container, ModelEditScreen modelES) {
        super(container, modelES, TextureAttribute.class);

        textureAttributeSelectBox = new VisSelectBox<String>();
        textureAttributeSelectBox.clearItems();
        textureAttributeSelectBox.setItems(t2a.values().toArray());
        textureAttributeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String alias = textureAttributeSelectBox.getSelected();
                currentTextureAttributeTable.fetchWidgetsFromContainer(a2t.get(alias), alias);
            }
        });

        add(new VisLabel("Texture Type: ", Color.BLACK)).right();
        add(textureAttributeSelectBox).left();
        add().expandX();
        row();
        add(currentTextureAttributeTable = new TextureAttributeTable(container, modelES)).colspan(3).fillX();
    }

    /**
     *
     */
    @Override
    public void resetAttributes() {
        String alias = textureAttributeSelectBox.getSelected();
        currentTextureAttributeTable.fetchWidgetsFromContainer(a2t.get(alias), alias);
    }

    /**
     * @param listener
     */
    @Override
    public void setListener(EventListener listener) {
        this.listener = listener;
        currentTextureAttributeTable.setListener(listener);
    }
}
