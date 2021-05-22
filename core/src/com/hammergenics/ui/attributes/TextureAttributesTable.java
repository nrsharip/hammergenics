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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TextureAttributesTable extends AttributesTable {
    // Texture Attribute related
    // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
//            public final static long Diffuse
//            public final static long Specular
//            public final static long Bump
//            public final static long Normal
//            public final static long Ambient
//            public final static long Emissive
//            public final static long Reflection

    private TextureTable textureTable;

    /**
     * @param skin
     * @param container
     */
    public TextureAttributesTable(Skin skin, Attributes container) {
        super(skin, container, TextureAttribute.class);

        add(textureTable = new TextureTable(skin, container));
    }

    /**
     *
     */
    public void resetAttributes() {
        textureTable.resetAttribute();
    }
}
