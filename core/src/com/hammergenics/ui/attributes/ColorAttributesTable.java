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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ColorAttributesTable extends AttributesTable<ColorAttribute> {
    /**
     * attribute type to color attribute table map
     */
    private ArrayMap<Long, ColorAttributeTable> t2Table;
    /**
     * attribute alias to color attribute table map
     */
    private ArrayMap<String, ColorAttributeTable> a2Table;

    /**
     * @param skin
     * @param container
     * @param mps
     */
    public ColorAttributesTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, ColorAttribute.class);

        t2Table = new ArrayMap<>();
        a2Table = new ArrayMap<>();

        t2a.forEach((entry) -> {
            ColorAttributeTable table = new ColorAttributeTable(skin, container, mps);
            table.resetAttribute(entry.key, entry.value);
            t2Table.put(entry.key, table);   // type to table
            a2Table.put(entry.value, table); // alias to table
        });

        a2Table.forEach((entry) -> {
            add(new Label(entry.key + ":", skin)).right();
            add(entry.value).left();
            add().expandX();
            row();
        });
    }

    /**
     *
     */
    @Override
    public void resetAttributes() {
        t2a.forEach((entry) -> {
            t2Table.get(entry.key).resetAttribute(entry.key, entry.value);
        });
    }

    /**
     * @param listener
     */
    @Override
    public void setListener(EventListener listener) {
        a2Table.forEach((entry) -> entry.value.setListener(listener));
    }
}
