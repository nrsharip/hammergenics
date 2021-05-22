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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

import static com.badlogic.gdx.graphics.Texture.TextureFilter;
import static com.badlogic.gdx.graphics.Texture.TextureWrap;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TextureAttributesTable extends Table {
    public Attributes container;
    // Texture Attribute related
    // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
//            public final static long Diffuse
//            public final static long Specular
//            public final static long Bump
//            public final static long Normal
//            public final static long Ambient
//            public final static long Emissive
//            public final static long Reflection
    // TODO: this is to be changed when implemented with reflection
    private TextField textureOffsetU = null;
    private TextField textureOffsetV = null;
    private TextField textureScaleU = null;
    private TextField textureScaleV = null;
    private SelectBox<String> textureMinFilter = null;
    private SelectBox<String> textureMagFilter = null;
    private SelectBox<String> textureUWrap = null;
    private SelectBox<String> textureVWrap = null;

    /**
     * @param skin
     * @param container
     */
    public TextureAttributesTable(Skin skin, Attributes container) {
        super(null); // setting no skin for the underlying table...
        this.container = container;
        // TODO: this whole thing should be redesigned with the use of reflection
        // Texture Attribute related:
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        textureOffsetU = new TextField("0", skin); textureOffsetU.setName("textureOffsetU");
        textureOffsetV = new TextField("0", skin); textureOffsetV.setName("textureOffsetV");
        textureScaleU = new TextField("1", skin); textureScaleU.setName("textureScaleU");
        textureScaleV = new TextField("1", skin); textureScaleV.setName("textureScaleV");

        TextField.TextFieldListener textFieldListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (container != null) {
                        //Material mtl = modelInstance.materials.get(0);
                        TextureAttribute attr = null;
                        // public final static long Diffuse
                        // public final static long Specular
                        // public final static long Bump
                        // public final static long Normal
                        // public final static long Ambient
                        // public final static long Emissive
                        // public final static long Reflection
                        attr = container.get(TextureAttribute.class, TextureAttribute.Diffuse);
                        if (attr != null) {
                            switch (textField.getName()) {
                                case "textureOffsetU":
                                    attr.offsetU = value;
                                    break;
                                case "textureOffsetV":
                                    attr.offsetV = value;
                                    break;
                                case "textureScaleU":
                                    attr.scaleU = value;
                                    break;
                                case "textureScaleV":
                                    attr.scaleV = value;
                                    break;
                            }
                        }
                        //miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
                    }
                    // seems like all text fields share the same Style object thus the color of all fields is changed
                    textField.getStyle().fontColor = Color.WHITE;
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                    // seems like all text fields share the same Style object thus the color of all fields is changed
                    textField.getStyle().fontColor = Color.RED;
                }
            }
        };

        textureOffsetU.setTextFieldListener(textFieldListener);
        textureOffsetV.setTextFieldListener(textFieldListener);
        textureScaleU.setTextFieldListener(textFieldListener);
        textureScaleV.setTextFieldListener(textFieldListener);

        Array<String> itemsTextureFilter = Arrays.stream(TextureFilter.values()).map(String::valueOf)
                .collect(Array::new, Array::add, Array::addAll);
        Array<String> itemsTextureWrap = Arrays.stream(TextureWrap.values()).map(String::valueOf)
                .collect(Array::new, Array::add, Array::addAll);

//        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                "TextureFilter: \n" + itemsTextureFilter.toString("\n"));
//        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                "TextureWrap: \n" + itemsTextureWrap.toString("\n"));

        textureMinFilter = new SelectBox<>(skin);
        textureMinFilter.setName("textureMinFilter");
        textureMinFilter.clearItems();
        textureMinFilter.setItems(itemsTextureFilter);

        textureMagFilter = new SelectBox<>(skin);
        textureMagFilter.setName("textureMagFilter");
        textureMagFilter.clearItems();
        textureMagFilter.setItems(itemsTextureFilter);

        textureUWrap = new SelectBox<>(skin);
        textureUWrap.setName("textureUWrap");
        textureUWrap.clearItems();
        textureUWrap.setItems(itemsTextureWrap);

        textureVWrap = new SelectBox<>(skin);
        textureVWrap.setName("textureVWrap");
        textureVWrap.clearItems();
        textureVWrap.setItems(itemsTextureWrap);

        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (container != null) {
                    //Material mtl = modelInstance.materials.get(0);
                    TextureAttribute attr = null;
                    // public final static long Diffuse
                    // public final static long Specular
                    // public final static long Bump
                    // public final static long Normal
                    // public final static long Ambient
                    // public final static long Emissive
                    // public final static long Reflection
                    attr = container.get(TextureAttribute.class, TextureAttribute.Diffuse);
                    if (attr != null) {
                        switch (actor.getName()) {
                            case "textureMinFilter":
                                attr.textureDescription.minFilter = TextureFilter.valueOf(textureMinFilter.getSelected());
                                break;
                            case "textureMagFilter":
                                attr.textureDescription.magFilter = TextureFilter.valueOf(textureMagFilter.getSelected());
                                break;
                            case "textureUWrap":
                                attr.textureDescription.uWrap = TextureWrap.valueOf(textureUWrap.getSelected());
                                break;
                            case "textureVWrap":
                                attr.textureDescription.uWrap = TextureWrap.valueOf(textureVWrap.getSelected());
                                break;
                        }
                    }
                    //miLabel.setText(LibgdxUtils.getModelInstanceInfo(modelInstance));
                }
            }
        };
        textureMinFilter.addListener(changeListener);
        textureMagFilter.addListener(changeListener);
        textureUWrap.addListener(changeListener);
        textureVWrap.addListener(changeListener);

        add(new Label("offsetU:", skin)).right();
        add(textureOffsetU).width(40).maxWidth(40);
        add(new Label("minFilter:", skin)).right();
        add(textureMinFilter).fillX();
        row();

        add(new Label("offsetV:", skin)).right();
        add(textureOffsetV).width(40).maxWidth(40);
        add(new Label("magFilter:", skin)).right();
        add(textureMagFilter).fillX();
        row();

        add(new Label("scaleU:", skin)).right();
        add(textureScaleU).width(40).maxWidth(40);
        add(new Label("uWrap:", skin)).right();
        add(textureUWrap).fillX();
        row();

        add(new Label("scaleV:", skin)).right();
        add(textureScaleV).width(40).maxWidth(40);
        add(new Label("vWrap:", skin)).right();
        add(textureVWrap).fillX();
        row();

    }

    /**
     *
     */
    public void resetAttributes() {
        if (container != null) {
            //Material mtl = modelInstance.materials.get(0);
            TextureAttribute attr = null;
//            public final static long Diffuse
//            public final static long Specular
//            public final static long Bump
//            public final static long Normal
//            public final static long Ambient
//            public final static long Emissive
//            public final static long Reflection
            attr = container.get(TextureAttribute.class, TextureAttribute.Diffuse);
            if (attr != null) {
                if (textureOffsetU != null) { textureOffsetU.setText(String.valueOf(attr.offsetU)); }
                if (textureOffsetV != null) { textureOffsetV.setText(String.valueOf(attr.offsetV)); }
                if (textureScaleU != null) { textureScaleU.setText(String.valueOf(attr.scaleU)); }
                if (textureScaleV != null) { textureScaleV.setText(String.valueOf(attr.scaleV)); }
                if (textureMinFilter != null) { textureMinFilter.setSelected(attr.textureDescription.minFilter.name()); }
                if (textureMagFilter != null) { textureMagFilter.setSelected(attr.textureDescription.magFilter.name()); }
                if (textureUWrap != null) { textureUWrap.setSelected(attr.textureDescription.uWrap.name()); }
                if (textureVWrap != null) { textureVWrap.setSelected(attr.textureDescription.vWrap.name()); }
            }
        }
    }
}
