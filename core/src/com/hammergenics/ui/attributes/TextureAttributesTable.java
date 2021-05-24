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

package com.hammergenics.ui.attributes;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TextureAttributesTable extends AttributesTable<TextureAttribute, TextureAttributeTable> {
    // Texture Attribute related
    // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
    private SelectBox<String> textureAttributeSelectBox;
    private TextureAttributeTable currentTextureAttributeTable;

    /**
     * @param skin
     * @param container
     */
    public TextureAttributesTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, TextureAttribute.class);

        textureAttributeSelectBox = new SelectBox<String>(uiSkin);
        textureAttributeSelectBox.clearItems();
        textureAttributeSelectBox.setItems(t2a.values().toArray());
        textureAttributeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String alias = textureAttributeSelectBox.getSelected();
                currentTextureAttributeTable.fetchWidgetsFromContainer(a2t.get(alias), alias);
            }
        });

        add(new Label("Texture Type: ", skin)).right();
        add(textureAttributeSelectBox).left();
        add().expandX();
        row();
        add(currentTextureAttributeTable = new TextureAttributeTable(skin, container, mps)).colspan(3).fillX();
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
