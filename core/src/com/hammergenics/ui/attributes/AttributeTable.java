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

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class AttributeTable<T extends Attribute> extends BaseAttributeTable {
    public Attributes container;
    protected long currentType = 0;
    protected String currentTypeAlias = null;

    protected CheckBox enabledCheckBox = null;

    public AttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, mps);
        this.container = container;

        enabledCheckBox = new CheckBox("enabled", skin);
    }

    /**
     *
     */
    public abstract void resetAttribute(long type, String alias);

    /**
     *
     */
    protected abstract T createAttribute(String alias);

}
