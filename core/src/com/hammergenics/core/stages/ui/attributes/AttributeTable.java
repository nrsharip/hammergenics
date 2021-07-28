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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.i18n.BundleText;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisWindow;

import static com.hammergenics.core.stages.ui.attributes.AttributeTable.TextButtonsTextEnum.*;

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

        enabledCheckBox = new VisCheckBox("enabled"); CHECKBOX_ENABLED.seize(enabledCheckBox);

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

    protected void applyLocale(HGGame.I18NBundlesEnum language) {
        TextButtonsTextEnum.setLanguage(language);
    }

    public enum TextButtonsTextEnum implements BundleText {
        SELECT_COLOR("attribute.color.textButton.select"),
        CHOOSE_IMAGE("attribute.texture.textButton.image"),

        CHECKBOX_ENABLED("checkBox.textButton.enabled"),
        CHECKBOX_BLENDED("attribute.blending.checkBox.textButton.blended"),
        CHECKBOX_MATCH_SRC_WITH_DST("attribute.blending.checkBox.textButton.match");

        private final String property;
        // TODO: IMPORTANT: This array will keeps the references to all buttons.
        //                  Need to make sure the references are removed when no longer needed
        private final Array<TextButton> instances = new Array<>(TextButton.class);
        private static HGGame.I18NBundlesEnum language;

        TextButtonsTextEnum(String property) { this.property = property; }

        public static void setLanguage(HGGame.I18NBundlesEnum lang) {
            language = lang;

            for (TextButtonsTextEnum tbs: TextButtonsTextEnum.values()) {
                for (TextButton tb: tbs.instances) { if (tb != null) { tb.setText(tbs.get()); } }
            }
        }

        public TextButton seize(TextButton btn) {
            this.instances.add(btn);
            btn.setText(get());
            return btn;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.attributesManagerBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.attributesManagerBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.attributesManagerBundle.format(property, arguments) : "ERR"; }
    }
}
