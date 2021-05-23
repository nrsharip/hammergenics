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

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hammergenics.screens.ModelPreviewScreen;
import com.hammergenics.ui.AbstractTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class AbstractAttributeTable extends AbstractTable {
    protected EventListener listener = null;
    protected ModelPreviewScreen mps;

    public AbstractAttributeTable(Skin skin, ModelPreviewScreen mps) {
        super(skin);
        this.mps = mps;
    }

    public abstract void setListener(EventListener listener);

    public interface EventListener {
        void onAttributeEnabled(long type, String alias);
        void onAttributeDisabled(long type, String alias);
        void onAttributeChange(long type, String alias);
    }
}
