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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class BaseAttributeTable<T extends Attribute> extends VisTable {
    public Attributes container;
    public Class<T> attributeClass;
    protected EventListener listener = null;
    protected ModelEditScreen modelES;
    protected VisWindow window;

    public BaseAttributeTable(Attributes container, ModelEditScreen modelES, Class<T> aClass, VisWindow window) {
        this.modelES = modelES;
        this.attributeClass = aClass;
        this.container = container;
        this.window = window;

        if (HGGame.I18NBundlesEnum.applied != null) {
            applyLocale(HGGame.I18NBundlesEnum.applied);
        } else {
            Gdx.app.error(getClass().getSimpleName(), "ERROR: locales are not configured");
            Gdx.app.exit();
        }
    }

    public abstract void setListener(EventListener listener);

    public interface EventListener {
        void onAttributeEnabled(Attributes container, long type, String alias);
        void onAttributeDisabled(Attributes container, long type, String alias);
        void onAttributeChange(Attributes container, long type, String alias);
    }
    public enum EventType {
        ATTR_ENABLED, ATTR_DISABLED, ATTR_CHANGED
    }
    protected abstract void applyLocale(HGGame.I18NBundlesEnum language);
}
