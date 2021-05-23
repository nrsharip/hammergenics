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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.ModelPreviewScreen;
import com.hammergenics.util.LibgdxUtils;

import java.util.Arrays;

import static com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TextureTable extends AttributeTable {
    private CheckBox enabledCheckBox = null;
    private TextField textureOffsetU = null;
    private TextField textureOffsetV = null;
    private TextField textureScaleU = null;
    private TextField textureScaleV = null;
    private SelectBox<String> textureMinFilter = null;
    private SelectBox<String> textureMagFilter = null;
    private SelectBox<String> textureUWrap = null;
    private SelectBox<String> textureVWrap = null;
    private SelectBox<String> textureSelectBox = null;

    private Array<String> itemsTextureFilter;
    private Array<String> itemsTextureWrap;

    private TextFieldListener paramTextFieldListener;
    private ChangeListener paramSelectBoxListener;
    private ChangeListener textureSelectBoxListener;
    private ChangeListener checkBoxListener;

    // this is a small hack to allow the use of the top panel texture selectbox
    public Texture texture;

    public TextureTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps);

        createListeners();

        enabledCheckBox = new CheckBox("enabled", skin);
        enabledCheckBox.addListener(checkBoxListener);

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        textureOffsetU = new TextField("0", skin); textureOffsetU.setName("textureOffsetU");
        textureOffsetV = new TextField("0", skin); textureOffsetV.setName("textureOffsetV");
        textureScaleU = new TextField("1", skin); textureScaleU.setName("textureScaleU");
        textureScaleV = new TextField("1", skin); textureScaleV.setName("textureScaleV");

        textureOffsetU.setTextFieldListener(paramTextFieldListener);
        textureOffsetV.setTextFieldListener(paramTextFieldListener);
        textureScaleU.setTextFieldListener(paramTextFieldListener);
        textureScaleV.setTextFieldListener(paramTextFieldListener);

        itemsTextureFilter = Arrays.stream(Texture.TextureFilter.values()).map(String::valueOf)
                .collect(Array::new, Array::add, Array::addAll);
        itemsTextureWrap = Arrays.stream(Texture.TextureWrap.values()).map(String::valueOf)
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

        // Select Box: Textures
        textureSelectBox = new SelectBox<>(skin);
        Array<String> itemsTexture = new Array<>();
        itemsTexture.add("No Texture Selected");

        // All PNG files in the same directory and direct subdirecories the asset is located
        FileHandle assetFileHandle = Gdx.files.local(mps.stage.modelSelectBox.getSelected());
        Array<FileHandle> textureFileHandleArray = texturesLookUp(assetFileHandle);

        if (textureFileHandleArray.size > 0) {
            itemsTexture.addAll(textureFileHandleArray.toString(";").split(";"));
        }

        //[switchModelInstance] before clear
        //[textureSelectBox.changed] -1
        //[textureSelectBox.changed] null
        //[switchModelInstance] after clear/before set
        //[textureSelectBox.changed] 0
        //[textureSelectBox.changed] No Texture
        //[switchModelInstance] after set

        if (textureSelectBox != null) {
            textureSelectBox.clearItems();
            textureSelectBox.setItems(itemsTexture);
        }

        textureMinFilter.addListener(paramSelectBoxListener);
        textureMagFilter.addListener(paramSelectBoxListener);
        textureUWrap.addListener(paramSelectBoxListener);
        textureVWrap.addListener(paramSelectBoxListener);
        textureSelectBox.addListener(textureSelectBoxListener);

        add(new Label("offsetU:", skin)).right();
        add(textureOffsetU).width(40).maxWidth(40);
        add(new Label("minFilter:", skin)).right();
        add(textureMinFilter).fillX();
        add(enabledCheckBox).expandX().left().padLeft(5f);
        row();

        add(new Label("offsetV:", skin)).right();
        add(textureOffsetV).width(40).maxWidth(40);
        add(new Label("magFilter:", skin)).right();
        add(textureMagFilter).fillX();
        add().expandX();
        row();

        add(new Label("scaleU:", skin)).right();
        add(textureScaleU).width(40).maxWidth(40);
        add(new Label("uWrap:", skin)).right();
        add(textureUWrap).fillX();
        add().expandX();
        row();

        add(new Label("scaleV:", skin)).right();
        add(textureScaleV).width(40).maxWidth(40);
        add(new Label("vWrap:", skin)).right();
        add(textureVWrap).fillX();
        add().expandX();
        row();

        add(textureSelectBox).colspan(5).fillX();
    }

    private Array<FileHandle> texturesLookUp (FileHandle assetFileHandle) {
        Array<FileHandle> textureFileHandleArray = LibgdxUtils.traversFileHandle(assetFileHandle.parent(),
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
                        || file.getName().toLowerCase().endsWith("tga")  // textures in TGA
                        || file.getName().toLowerCase().endsWith("bmp")  // textures in BMP
        );

        // TODO: Add unified convention like "textures | skins" to specify all folders at once
        // All PNG files in the "textures" directory and subdirectories (if any) on asset's path
        textureFileHandleArray = LibgdxUtils.traversFileHandle(
                // starting at parent() since we already traversed current folder/subfolders above
                LibgdxUtils.fileOnPath(assetFileHandle.parent(), "textures"),
                textureFileHandleArray,
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
                        || file.getName().toLowerCase().endsWith("tga")  // textures in TGA
                        || file.getName().toLowerCase().endsWith("bmp")  // textures in BMP
        );
        // All PNG files in the "skins" directory and subdirectories (if any) on asset's path
        textureFileHandleArray = LibgdxUtils.traversFileHandle(
                // starting at parent() since we already traversed current folder/subfolders above
                LibgdxUtils.fileOnPath(assetFileHandle.parent(), "skins"),
                textureFileHandleArray,
                file -> file.isDirectory()
                        || file.getName().toLowerCase().endsWith("png")  // textures in PNG
                        || file.getName().toLowerCase().endsWith("tga")  // textures in TGA
                        || file.getName().toLowerCase().endsWith("bmp")  // textures in BMP
        );
        return textureFileHandleArray;
    }

    // long Diffuse
    // long Specular
    // long Bump
    // long Normal
    // long Ambient
    // long Emissive
    // long Reflection
    private TextureAttribute createTextureAttribute() {
        if (texture == null) {
            return null;
        }
        switch (currentTypeAlias) {
            case TextureAttribute.DiffuseAlias: return TextureAttribute.createDiffuse(texture);
            case TextureAttribute.SpecularAlias: return TextureAttribute.createSpecular(texture);
            case TextureAttribute.BumpAlias: return TextureAttribute.createBump(texture);
            case TextureAttribute.NormalAlias: return TextureAttribute.createNormal(texture);
            case TextureAttribute.AmbientAlias: return TextureAttribute.createAmbient(texture);
            case TextureAttribute.EmissiveAlias: return TextureAttribute.createEmissive(texture);
            case TextureAttribute.ReflectionAlias: return TextureAttribute.createReflection(texture);
        }
        return null;
    }

    private void createListeners() {
        paramTextFieldListener = new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float value = Float.parseFloat(textField.getText());

                    if (container != null && currentType != 0) {
                        //Material mtl = modelInstance.materials.get(0);
                        TextureAttribute attr = null;
                        // public final static long Diffuse
                        // public final static long Specular
                        // public final static long Bump
                        // public final static long Normal
                        // public final static long Ambient
                        // public final static long Emissive
                        // public final static long Reflection
                        attr = container.get(TextureAttribute.class, currentType);
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
                        if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
                    }
                    textField.getColor().set(Color.WHITE);
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                    textField.getColor().set(Color.PINK);
                }
            }
        };

        paramSelectBoxListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (container != null && currentType != 0) {
                    //Material mtl = modelInstance.materials.get(0);
                    TextureAttribute attr = null;
                    // public final static long Diffuse
                    // public final static long Specular
                    // public final static long Bump
                    // public final static long Normal
                    // public final static long Ambient
                    // public final static long Emissive
                    // public final static long Reflection
                    attr = container.get(TextureAttribute.class, currentType);
                    if (attr != null) {
                        switch (actor.getName()) {
                            case "textureMinFilter":
                                attr.textureDescription.minFilter = Texture.TextureFilter.valueOf(textureMinFilter.getSelected());
                                break;
                            case "textureMagFilter":
                                attr.textureDescription.magFilter = Texture.TextureFilter.valueOf(textureMagFilter.getSelected());
                                break;
                            case "textureUWrap":
                                attr.textureDescription.uWrap = Texture.TextureWrap.valueOf(textureUWrap.getSelected());
                                break;
                            case "textureVWrap":
                                attr.textureDescription.uWrap = Texture.TextureWrap.valueOf(textureVWrap.getSelected());
                                break;
                        }
                    }
                    if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
                }
            }
        };

        textureSelectBoxListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (container != null && currentType != 0) {
                    enabledCheckBox.setChecked(false);              // relying on 'enabled' check box event processing
                    if (textureSelectBox.getSelectedIndex() != 0) { // == 0 - 'No Texture Selected'
                        enabledCheckBox.setChecked(true);           // relying on 'enabled' check box event processing
                    }
                    if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
                }
            }
        };

        checkBoxListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (textureSelectBox.getSelectedIndex() == 0) {
                    texture = null;
                    enabledCheckBox.setChecked(false); // no texture attribute gets enabled without the texture selected first
                    Gdx.app.debug("enabledCheckBox", "No texture selected: type = 0x"
                            + Long.toHexString(currentType) + " alias = " + currentTypeAlias);
                } else {
                    texture = mps.assetManager.get(textureSelectBox.getSelected(), Texture.class);
                    if (texture == null) {
                        Gdx.app.debug("enabledCheckBox", "Texture is not loaded from: " + textureSelectBox.getSelected()
                                + " (attribute: type = 0x" + Long.toHexString(currentType) + " alias = " + currentTypeAlias + ")");
                        enabledCheckBox.setChecked(false); // no texture attribute gets enabled without the texture selected first
                    }
                }

                if (container != null) {
                    if (enabledCheckBox.isChecked()) { // adding the attribute
                        // TODO: make sure this is propagated
                        //Texture texture = assetManager.get(textureSelectBox.getSelected(), Texture.class);
                        TextureAttribute attr = createTextureAttribute();

                        if (attr == null) {
                            Gdx.app.error("enabledCheckBox", "ERROR: attribute is not created"
                                    + " (attribute: type = 0x" + Long.toHexString(currentType) + " alias = " + currentTypeAlias + ")");
                            return;
                        }

                        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
                        attr.offsetU = 0;
                        attr.offsetV = 0;
                        attr.scaleU = 1;
                        attr.scaleV = 1;
                        attr.textureDescription.minFilter = texture.getMinFilter();
                        attr.textureDescription.magFilter = texture.getMagFilter();
                        attr.textureDescription.uWrap = texture.getUWrap();
                        attr.textureDescription.vWrap = texture.getVWrap();
                        container.set(attr);

                        mps.stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
                        Gdx.app.debug("enabledCheckBox", "Setting the attribute: type = 0x"
                                + Long.toHexString(currentType) + " alias = " + currentTypeAlias);

                        if (listener != null) { listener.onAttributeEnabled(currentType, currentTypeAlias); }
                    } else { // removing the attribute
                        if (container.get(currentType) != null) {
                            container.remove(currentType);

                            mps.stage.textureImage.setDrawable(null);
                            Gdx.app.debug("enabledCheckBox", "Clearing the attribute: type = 0x"
                                    + Long.toHexString(currentType) + " alias = " + currentTypeAlias);

                            if (listener != null) { listener.onAttributeDisabled(currentType, currentTypeAlias); }
                        } else {
                            Gdx.app.error("enabledCheckBox", "ERROR: we shouldn't be here: type = 0x"
                                + Long.toHexString(currentType) + " alias = " + currentTypeAlias);
                        }
                    }
                }
            }
        };
    }

    public void resetAttribute(long type, String alias) {
        if (container != null) {
            //Material mtl = modelInstance.materials.get(0);
            TextureAttribute attr = null;

            currentType = type;
            currentTypeAlias = alias;

            attr = container.get(TextureAttribute.class, type);
            if (attr != null) {
                if (enabledCheckBox != null) { enabledCheckBox.setChecked(true); }
                if (textureOffsetU != null) { textureOffsetU.setText(String.valueOf(attr.offsetU)); }
                if (textureOffsetV != null) { textureOffsetV.setText(String.valueOf(attr.offsetV)); }
                if (textureScaleU != null) { textureScaleU.setText(String.valueOf(attr.scaleU)); }
                if (textureScaleV != null) { textureScaleV.setText(String.valueOf(attr.scaleV)); }
                if (textureMinFilter != null) { textureMinFilter.setSelected(attr.textureDescription.minFilter.name()); }
                if (textureMagFilter != null) { textureMagFilter.setSelected(attr.textureDescription.magFilter.name()); }
                if (textureUWrap != null) { textureUWrap.setSelected(attr.textureDescription.uWrap.name()); }
                if (textureVWrap != null) { textureVWrap.setSelected(attr.textureDescription.vWrap.name()); }
                if (textureSelectBox != null && attr.textureDescription.texture.getTextureData() instanceof FileTextureData) {
                    textureSelectBox.setSelected(attr.textureDescription.texture.toString());
                } else {
                    textureSelectBox.setSelectedIndex(0);
                }
            } else {
                resetToDefaults();
            }
        }
    }

    private void resetToDefaults() {
        if (enabledCheckBox != null) { enabledCheckBox.setChecked(false); }
        if (textureOffsetU != null) { textureOffsetU.setText("0"); }
        if (textureOffsetV != null) { textureOffsetV.setText("0"); }
        if (textureScaleU != null) { textureScaleU.setText("1"); }
        if (textureScaleV != null) { textureScaleV.setText("1"); }
        if (textureMinFilter != null) { textureMinFilter.setSelected(itemsTextureFilter.get(0)); }
        if (textureMagFilter != null) { textureMagFilter.setSelected(itemsTextureFilter.get(0)); }
        if (textureUWrap != null) { textureUWrap.setSelected(itemsTextureWrap.get(0)); }
        if (textureVWrap != null) { textureVWrap.setSelected(itemsTextureWrap.get(0)); }
        if (textureSelectBox != null) { textureSelectBox.setSelectedIndex(0); }
    }

    @Override
    public void setListener(Event listener) {
        this.listener = listener;
    }
}
