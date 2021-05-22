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

package com.hammergenics.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class AttributesTable extends Table {
    public Class<? extends Attribute> aClass;
    public Attributes container;
    public Skin uiSkin;

    /**
     * @param skin
     * @param container
     */
    public AttributesTable(Skin skin, Attributes container, Class<? extends Attribute> aClass) {
        super(null); // setting no skin for the underlying table...
        this.aClass = aClass;
        this.container = container;
        this.uiSkin = skin;
    }

    /**
     *
     */
    public void traverse() {
        Field[] attrTypesFields = Arrays.stream(aClass.getFields())       // getting all accessible public fields
                .filter(field -> field.getType().equals(Long.TYPE))       // taking only fields of type 'long'
                .filter(field -> Modifier.isFinal(field.getModifiers()))  // taking only final fields
                .filter(field -> Modifier.isStatic(field.getModifiers())) // taking only static fields
                .toArray(Field[]::new);                                   // retrieving the array

        if (attrTypesFields.length == 0) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR: no type fields found in: " + aClass.getName());
            return;
        }

        for (Field field: attrTypesFields) {
            try {
                long type = field.getLong(null); // null is allowed for static fields...

                if (Attribute.getAttributeAlias(type) == null) {
                    Gdx.app.debug(getClass().getSimpleName(),
                            "WARNING: field value is not a registered Attribute Type: "
                                    + aClass.getSimpleName() + "." + field.getName() + " = 0x" + Long.toHexString(type));
                }

                Gdx.app.debug(getClass().getSimpleName(),
                        aClass.getSimpleName() + "." + field.getName() + ": 0x" + Long.toHexString(type)
                                + " (alias: " + Attribute.getAttributeAlias(type) + ")");
            } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
                Gdx.app.error(getClass().getSimpleName(),
                        "EXCEPTION while reading the field contents of the class: " + aClass.getName() + "\n" +
                                Arrays.stream(e.getStackTrace())
                                        .map(element -> String.valueOf(element) + "\n")
                                        .reduce("", String::concat));
            }
        }
    }

    /**
     *
     */
    public abstract void resetAttributes();
}
