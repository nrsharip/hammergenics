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

package com.hammergenics.screens.stages.ui.attributes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.screens.ModelPreviewScreen;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class AttributesTable<T extends Attribute, Q extends AttributeTable<T>> extends BaseAttributeTable<T> {
    /**
     * attribute type to color attribute table map
     */
    protected ArrayMap<Long, Q> t2Table = new ArrayMap<>();
    /**
     * attribute alias to color attribute table map
     */
    protected ArrayMap<String, Q> a2Table = new ArrayMap<>();

    /**
     * type to alias map
     */
    public ArrayMap<Long, String> t2a;
    /**
     * alias to type map
     */
    public ArrayMap<String, Long> a2t;

    /**
     * @param skin
     * @param container
     */
    public AttributesTable(Skin skin, Attributes container, ModelPreviewScreen mps, Class<T> aClass) {
        super(skin, container, mps, aClass);

        t2a = new ArrayMap<>();
        a2t = new ArrayMap<>();
        traverse();
    }

    /**
     *
     */
    private void traverse() {
        Field[] attrTypesFields = Arrays.stream(attributeClass.getFields()) // getting all accessible public fields
                .filter(field -> field.getType().equals(Long.TYPE))         // taking only fields of type 'long'
                .filter(field -> Modifier.isFinal(field.getModifiers()))    // taking only final fields
                .filter(field -> Modifier.isStatic(field.getModifiers()))   // taking only static fields
                .toArray(Field[]::new);                                     // retrieving the array

        if (attrTypesFields.length == 0) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR: no type fields found in: " + attributeClass.getName());
            return;
        }

        for (Field field: attrTypesFields) {
            try {
                long type = field.getLong(null); // null is allowed for static fields...
                String alias = Attribute.getAttributeAlias(type);

                if (alias == null) {
                    Gdx.app.debug(getClass().getSimpleName(),
                            "WARNING: field value is not a registered Attribute Type: "
                                    + attributeClass.getSimpleName() + "." + field.getName() + " = 0x" + Long.toHexString(type));
                    continue;
                }

                t2a.put(type, alias);
                a2t.put(alias, type);

//                Gdx.app.debug(getClass().getSimpleName(),
//                        attributeClass.getSimpleName() + "." + field.getName() + ": 0x" + Long.toHexString(type)
//                                + " (alias: " + Attribute.getAttributeAlias(type) + ")");
            } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
                Gdx.app.error(getClass().getSimpleName(),
                        "EXCEPTION while reading the field contents of the class: " + attributeClass.getName() + "\n" +
                                Arrays.stream(e.getStackTrace())
                                        .map(element -> String.valueOf(element) + "\n")
                                        .reduce("", String::concat));
            }
        }
    }

    /**
     *
     */
    public void resetAttributes() {
        t2a.forEach((entry) -> t2Table.get(entry.key).fetchWidgetsFromContainer(entry.key, entry.value));
    }

    @Override
    public void setListener(EventListener listener) {
        this.listener = listener;
        t2Table.forEach((entry) -> entry.value.setListener(listener));
    }
}
