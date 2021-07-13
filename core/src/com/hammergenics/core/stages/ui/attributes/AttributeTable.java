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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class AttributeTable<T extends Attribute> extends BaseAttributeTable<T> {
    protected long currentType = 0;
    protected String currentTypeAlias = null;

    protected VisCheckBox enabledCheckBox = null;

    protected ChangeListener checkBoxListener;

    public AttributeTable(Attributes container, ModelEditScreen modelES, Class<T> aClass, VisWindow window, Long type, String alias) {
        super(container, modelES, aClass, window);

        currentType = type;
        currentTypeAlias = alias;

        enabledCheckBox = new VisCheckBox("enabled");

        checkBoxListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (container != null && currentType != 0) {
                    T attr = container.get(attributeClass, currentType);
                    if (enabledCheckBox.isChecked() && attr != null) { // attribute already exists, sync up

                        fetchWidgetsFromAttribute(attr); // syncing one more time

                    } else if (enabledCheckBox.isChecked()) { // adding the attribute

                        if (!preCreateAttr()) { return; }

                        attr = createAttribute(currentTypeAlias);

                        if (attr == null) {
                            Gdx.app.error("enabledCheckBox", "ERROR: attribute is not created"
                                    + " (attribute: type = 0x" + Long.toHexString(currentType) + " alias = " + currentTypeAlias + ")");
                            return;
                        }

                        fetchWidgetsFromAttribute(attr);

                        container.set(attr);

                        Gdx.app.debug("enabledCheckBox", "Setting the attribute: type = 0x"
                                + Long.toHexString(currentType) + " alias = " + currentTypeAlias);

                        if (listener != null) { listener.onAttributeEnabled(container, currentType, currentTypeAlias); }
                    } else { // removing the attribute
                        if (container.get(currentType) != null) {
                            container.remove(currentType);

                            postRemoveAttr();

                            Gdx.app.debug("enabledCheckBox", "Clearing the attribute: type = 0x"
                                    + Long.toHexString(currentType) + " alias = " + currentTypeAlias);

                            if (listener != null) { listener.onAttributeDisabled(container, currentType, currentTypeAlias); }
                        } else {
                            Gdx.app.error("enabledCheckBox", "ERROR: we shouldn't be here: type = 0x"
                                    + Long.toHexString(currentType) + " alias = " + currentTypeAlias);
                        }
                    }
                }
            }
        };
        enabledCheckBox.addListener(checkBoxListener);
    }

    protected void fetchWidgetsFromContainer(long type, String alias) {
        if (container != null) {
            currentType = type;
            currentTypeAlias = alias;

            T attr = container.get(attributeClass, type);
            if (attr != null) {
                if (enabledCheckBox != null) {
                    enabledCheckBox.setProgrammaticChangeEvents(false);
                    enabledCheckBox.setChecked(true);
                    enabledCheckBox.setProgrammaticChangeEvents(true);
                }
                fetchWidgetsFromAttribute(attr);
            } else {
                resetWidgetsToDefaults();
            }
        }
    }

    @Override
    public void setListener(EventListener listener) { this.listener = listener; }

    protected abstract boolean preCreateAttr();

    protected abstract void fetchWidgetsFromAttribute(T attr);

    protected abstract void postRemoveAttr();

    protected abstract void resetWidgetsToDefaults();

    protected abstract T createAttribute(String alias);

}
