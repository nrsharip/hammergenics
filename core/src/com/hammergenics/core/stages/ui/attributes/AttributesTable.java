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
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.i18n.BundleText;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;

import java.lang.reflect.Field;
import java.util.Arrays;

import static com.hammergenics.core.stages.ui.attributes.AttributesTable.LabelsTextEnum.*;

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

    final VisWindow window;

    public AttributesTable(String title, Attributes container, ModelEditScreen modelES, Class<T> aClass) {
        this(title, true, container, modelES, aClass);
    }
    /**
     * @param container
     */
    public AttributesTable(String title, boolean addLabel, Attributes container, ModelEditScreen modelES, Class<T> aClass) {
        super(container, modelES, aClass, null);

        window = new VisWindow(title);

        if (this instanceof Blending) {
            WINDOW_TITLE_BLENDING.seize(window.getTitleLabel());
        } else if (this instanceof Color) {
            WINDOW_TITLE_COLOR.seize(window.getTitleLabel());
        } else if (this instanceof DirectionalLights) {
            WINDOW_TITLE_DIRECTIONAL_LIGHTS.seize(window.getTitleLabel());
        } else if (this instanceof PointLights) {
            WINDOW_TITLE_POINT_LIGHTS.seize(window.getTitleLabel());
        } else if (this instanceof SpotLights) {
            WINDOW_TITLE_SPOT_LIGHTS.seize(window.getTitleLabel());
        } else if (this instanceof Texture) {
            WINDOW_TITLE_TEXTURE.seize(window.getTitleLabel());
        }

        window.add(this).expand().fill();

        t2a = new ArrayMap<>();
        a2t = new ArrayMap<>();

        traverse();

        t2a.forEach((entry) -> {
            Q table = createTable(container, modelES, window, entry.key, entry.value);
            t2Table.put(entry.key, table);   // type to table
            a2Table.put(entry.value, table); // alias to table
        });

        resetAttributes();

        a2Table.forEach((entry) -> {
            if (addLabel) {
                VisLabel label = new VisLabel(entry.key + ":");
                add(label).right();

                switch (entry.key) {
                    case ColorAttribute.DiffuseAlias: ATTR_COLOR_DIFFUSE.seize(label); break;
                    case ColorAttribute.SpecularAlias: ATTR_COLOR_SPECULAR.seize(label); break;
                    case ColorAttribute.AmbientAlias: ATTR_COLOR_AMBIENT.seize(label); break;
                    case ColorAttribute.EmissiveAlias: ATTR_COLOR_EMISSIVE.seize(label); break;
                    case ColorAttribute.ReflectionAlias: ATTR_COLOR_REFLECTION.seize(label); break;
                    case ColorAttribute.AmbientLightAlias: ATTR_COLOR_AMBIENT_LIGHT.seize(label); break;
                    case ColorAttribute.FogAlias: ATTR_COLOR_FOG.seize(label); break;
                }
            }
            add(entry.value).expand().fill().right();
            row().pad(0.5f);
        });
    }

    protected abstract Q createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias);

    protected void applyLocale(HGGame.I18NBundlesEnum language) {
        LabelsTextEnum.setLanguage(language);

        if (a2Table != null) { a2Table.forEach((entry) -> entry.value.applyLocale(language)); }
    }

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
            super("Blending Attributes", false, container, modelES, BlendingAttribute.class);
        }

        @Override
        protected BlendingAttributeTable createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
            return new BlendingAttributeTable(container, modelES, window, type, alias);
        }
    }

    public static class Color extends AttributesTable<ColorAttribute, ColorAttributeTable> {
        /**
         * @param container
         * @param modelES
         */
        public Color(Attributes container, ModelEditScreen modelES) {
            super("Color Attributes", container, modelES, ColorAttribute.class);
        }

        @Override
        protected ColorAttributeTable createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
            return new ColorAttributeTable(container, modelES, window, type, alias);
        }
    }

    public static class DirectionalLights
            extends AttributesTable<DirectionalLightsAttribute, BaseLightsAttributeTable<DirectionalLightsAttribute, DirectionalLight>> {

        public DirectionalLights(Attributes container, ModelEditScreen modelES) {
            super("Directional Lights Attributes", false, container, modelES, DirectionalLightsAttribute.class);
        }

        @Override
        protected BaseLightsAttributeTable<DirectionalLightsAttribute, DirectionalLight> createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
            return new DirectionalLightsAttributeTable(container, modelES, window, type, alias);
        }
    }

    public static class PointLights
            extends AttributesTable<PointLightsAttribute, BaseLightsAttributeTable<PointLightsAttribute, PointLight>> {

        public PointLights(Attributes container, ModelEditScreen modelES) {
            super("Point Lights Attributes", false, container, modelES, PointLightsAttribute.class);
        }

        @Override
        protected BaseLightsAttributeTable<PointLightsAttribute, PointLight> createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
            return new PointLightsAttributeTable(container, modelES, window, type, alias);
        }
    }

    public static class SpotLights
            extends AttributesTable<SpotLightsAttribute, BaseLightsAttributeTable<SpotLightsAttribute, SpotLight>> {

        public SpotLights(Attributes container, ModelEditScreen modelES) {
            super("Spot Lights Attributes", false, container, modelES, SpotLightsAttribute.class);
        }

        @Override
        protected BaseLightsAttributeTable<SpotLightsAttribute, SpotLight> createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
            return new SpotLightsAttributeTable(container, modelES, window, type, alias);
        }
    }

    public static class Texture extends AttributesTable<TextureAttribute, TextureAttributeTable> {
        /**
         * @param container
         */
        public Texture(Attributes container, ModelEditScreen modelES) {
            super("Texture Attributes", false, container, modelES, TextureAttribute.class);
        }

        @Override
        protected TextureAttributeTable createTable(Attributes container, ModelEditScreen modelES, VisWindow window, Long type, String alias) {
            return new TextureAttributeTable(container, modelES, window, type, alias);
        }
    }

    public enum LabelsTextEnum implements BundleText {
        WINDOW_TITLE_BLENDING("window.title.blending"),
        WINDOW_TITLE_COLOR("window.title.color"),
        WINDOW_TITLE_DIRECTIONAL_LIGHTS("window.title.lights.directional"),
        WINDOW_TITLE_POINT_LIGHTS("window.title.lights.point"),
        WINDOW_TITLE_SPOT_LIGHTS("window.title.lights.spot"),
        WINDOW_TITLE_TEXTURE("window.title.texture"),

        ATTR_COLOR_DIFFUSE("attribute.color.label.diffuse"),
        ATTR_COLOR_SPECULAR("attribute.color.label.specular"),
        ATTR_COLOR_AMBIENT("attribute.color.label.ambient"),
        ATTR_COLOR_EMISSIVE("attribute.color.label.emissive"),
        ATTR_COLOR_REFLECTION("attribute.color.label.reflection"),
        ATTR_COLOR_AMBIENT_LIGHT("attribute.color.label.ambientlight"),
        ATTR_COLOR_FOG("attribute.color.label.fog"),

        ATTR_TEXTURE_DIFFUSE("attribute.texture.label.diffuse"),
        ATTR_TEXTURE_SPECULAR("attribute.texture.label.specular"),
        ATTR_TEXTURE_BUMP("attribute.texture.label.bump"),
        ATTR_TEXTURE_NORMAL("attribute.texture.label.normal"),
        ATTR_TEXTURE_AMBIENT("attribute.texture.label.ambient"),
        ATTR_TEXTURE_EMISSIVE("attribute.texture.label.emissive"),
        ATTR_TEXTURE_REFLECTION("attribute.texture.label.reflection");

        private final String property;
        // TODO: IMPORTANT: This array will keeps the references to all buttons.
        //                  Need to make sure the references are removed when no longer needed
        private Array<Label> instances = new Array<>(Label.class);
        private static HGGame.I18NBundlesEnum language;

        LabelsTextEnum(String property) { this.property = property; }

        public static void setLanguage(HGGame.I18NBundlesEnum lang) {
            language = lang;

            for (LabelsTextEnum label: LabelsTextEnum.values()) {
                for (Label instance: label.instances) { if (instance != null) { instance.setText(label.get()); } }
            }
        }

        public Label seize(Label label) {
            this.instances.add(label);
            label.setText(get());
            return label;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.attributesManagerBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.attributesManagerBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.attributesManagerBundle.format(property, arguments) : "ERR"; }
    }
}
