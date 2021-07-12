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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.widget.VisLabel;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Add description here
 *
 * @author nrsharip
 */
public abstract class AttributesTable<T extends Attribute, Q extends AttributeTable<T>> extends BaseAttributeTable<T> {
    /**
     * attribute type to the attribute table map
     */
    protected ArrayMap<Long, Q> t2Table = new ArrayMap<>();
    /**
     * attribute alias to the attribute table map
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
     * @param container
     */
    public AttributesTable(Attributes container, ModelEditScreen modelES, Class<T> aClass) {
        super(container, modelES, aClass);

        t2a = new ArrayMap<>();
        a2t = new ArrayMap<>();
        traverse();

        t2a.forEach((entry) -> {
            Q table = createTable(container, modelES);
            t2Table.put(entry.key, table);   // type to table
            a2Table.put(entry.value, table); // alias to table
        });

        resetAttributes();

        a2Table.forEach((entry) -> {
            add(new VisLabel(entry.key + ":", com.badlogic.gdx.graphics.Color.BLACK)).right();
            add(entry.value).left();
            add().expandX();
            row().pad(0.5f);
        });
    }

    protected abstract Q createTable(Attributes container, ModelEditScreen modelES);

    /**
     *
     */
    private void traverse() {
        Field[] attrTypesFields = HGUtils.scanPublicStaticFinalFields(attributeClass, Long.TYPE);

        if (attrTypesFields.length == 0) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR: no type fields found in: " + attributeClass.getName());
            return;
        }

        for (Field field: attrTypesFields) {
            try {
                long type = field.getLong(null); // null is allowed for static fields...
                String alias = Attribute.getAttributeAlias(type);

                if (alias == null) {
                    Gdx.app.debug(getClass().getSimpleName(), "WARNING: field value is not a registered Attribute Type: "
                            + attributeClass.getSimpleName() + "." + field.getName() + " = 0x" + Long.toHexString(type));
                    continue;
                }

                t2a.put(type, alias);
                a2t.put(alias, type);

//                Gdx.app.debug(getClass().getSimpleName(), attributeClass.getSimpleName() + "." + field.getName()
//                        + ": 0x" + Long.toHexString(type) + " (alias: " + Attribute.getAttributeAlias(type) + ")");
            } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
                Gdx.app.error(getClass().getSimpleName(), "EXCEPTION while reading the field contents of the class: "
                        + attributeClass.getName() + "\n" + Arrays.stream(e.getStackTrace())
                            .map(element -> String.valueOf(element) + "\n").reduce("", String::concat));
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

    public static class Blending extends AttributesTable<BlendingAttribute, BlendingAttributeTable> {
        public Blending(Attributes container, ModelEditScreen modelES) {
            super(container, modelES, BlendingAttribute.class);
        }

        @Override
        protected BlendingAttributeTable createTable(Attributes container, ModelEditScreen modelES) {
            return new BlendingAttributeTable(container, modelES);
        }
    }

    public static class Color extends AttributesTable<ColorAttribute, ColorAttributeTable> {
        /**
         * @param container
         * @param modelES
         */
        public Color(Attributes container, ModelEditScreen modelES) {
            super(container, modelES, ColorAttribute.class);
        }

        @Override
        protected ColorAttributeTable createTable(Attributes container, ModelEditScreen modelES) {
            return new ColorAttributeTable(container, modelES);
        }
    }

    public static class DirectionalLights
            extends AttributesTable<DirectionalLightsAttribute, BaseLightsAttributeTable<DirectionalLightsAttribute, DirectionalLight>> {

        public DirectionalLights(Attributes container, ModelEditScreen modelES) {
            super(container, modelES, DirectionalLightsAttribute.class);
        }

        @Override
        protected BaseLightsAttributeTable<DirectionalLightsAttribute, DirectionalLight> createTable(Attributes container, ModelEditScreen modelES) {
            return new DirectionalLightsAttributeTable(container, modelES);
        }
    }

    public static class PointLights
            extends AttributesTable<PointLightsAttribute, BaseLightsAttributeTable<PointLightsAttribute, PointLight>> {

        public PointLights(Attributes container, ModelEditScreen modelES) {
            super(container, modelES, PointLightsAttribute.class);
        }

        @Override
        protected BaseLightsAttributeTable<PointLightsAttribute, PointLight> createTable(Attributes container, ModelEditScreen modelES) {
            return new PointLightsAttributeTable(container, modelES);
        }
    }

    public static class SpotLights
            extends AttributesTable<SpotLightsAttribute, BaseLightsAttributeTable<SpotLightsAttribute, SpotLight>> {

        public SpotLights(Attributes container, ModelEditScreen modelES) {
            super(container, modelES, SpotLightsAttribute.class);
        }

        @Override
        protected BaseLightsAttributeTable<SpotLightsAttribute, SpotLight> createTable(Attributes container, ModelEditScreen modelES) {
            return new SpotLightsAttributeTable(container, modelES);
        }
    }

    public static class Texture extends AttributesTable<TextureAttribute, TextureAttributeTable> {
        /**
         * @param container
         */
        public Texture(Attributes container, ModelEditScreen modelES) {
            super(container, modelES, TextureAttribute.class);
        }

        @Override
        protected TextureAttributeTable createTable(Attributes container, ModelEditScreen modelES) {
            return new TextureAttributeTable(container, modelES);
        }
    }
}
