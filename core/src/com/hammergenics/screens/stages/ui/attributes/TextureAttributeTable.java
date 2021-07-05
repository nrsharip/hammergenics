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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.util.Arrays;

import static com.hammergenics.HGEngine.filterTextures;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class TextureAttributeTable extends AttributeTable<TextureAttribute> {
    private static final String ACTOR_OFFSETU = "textureOffsetU";
    private static final String ACTOR_OFFSETV = "textureOffsetV";
    private static final String ACTOR_SCALEU = "textureScaleU";
    private static final String ACTOR_SCALEV = "textureScaleV";
    private static final String ACTOR_MINFILTER = "textureMinFilter";
    private static final String ACTOR_MAGFILTER = "textureMagFilter";
    private static final String ACTOR_UWRAP = "textureUWrap";
    private static final String ACTOR_VWRAP = "textureVWrap";

    private VisTextField offsetUTF = null;
    private VisTextField offsetVTF = null;
    private VisTextField scaleUTF = null;
    private VisTextField scaleVTF = null;
    private VisSelectBox<String> minFilterSB = null;
    private VisSelectBox<String> magFilterSB = null;
    private VisSelectBox<String> uWrapSB = null;
    private VisSelectBox<String> vWrapSB = null;
    // TODO: one more parameter is currently missing: uvIndex (see TextureAttribute)
    private VisSelectBox<String> textureSelectBox = null;

    private Array<String> itemsTextureFilter;
    private Array<String> itemsTextureWrap;

    private VisTextField.TextFieldListener paramTextFieldListener;
    private ChangeListener paramSelectBoxListener;
    private ChangeListener textureSelectBoxListener;

    private Texture texture;

    public TextureAttributeTable(Attributes container, ModelEditScreen modelES) {
        super(container, modelES, TextureAttribute.class);

        createListeners();

        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        offsetUTF = new VisTextField("0"); offsetUTF.setName(ACTOR_OFFSETU);
        offsetVTF = new VisTextField("0"); offsetVTF.setName(ACTOR_OFFSETV);
        scaleUTF = new VisTextField("1"); scaleUTF.setName(ACTOR_SCALEU);
        scaleVTF = new VisTextField("1"); scaleVTF.setName(ACTOR_SCALEV);

        offsetUTF.setTextFieldListener(paramTextFieldListener);
        offsetVTF.setTextFieldListener(paramTextFieldListener);
        scaleUTF.setTextFieldListener(paramTextFieldListener);
        scaleVTF.setTextFieldListener(paramTextFieldListener);

        itemsTextureFilter = Arrays.stream(Texture.TextureFilter.values()).map(String::valueOf)
                .collect(Array::new, Array::add, Array::addAll);
        itemsTextureWrap = Arrays.stream(Texture.TextureWrap.values()).map(String::valueOf)
                .collect(Array::new, Array::add, Array::addAll);

//        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                "TextureFilter: \n" + itemsTextureFilter.toString("\n"));
//        Gdx.app.debug(Thread.currentThread().getStackTrace()[1].getMethodName(),
//                "TextureWrap: \n" + itemsTextureWrap.toString("\n"));

        minFilterSB = new VisSelectBox<>();
        minFilterSB.setName(ACTOR_MINFILTER);
        minFilterSB.clearItems();

        magFilterSB = new VisSelectBox<>();
        magFilterSB.setName(ACTOR_MAGFILTER);
        magFilterSB.clearItems();

        if (itemsTextureFilter != null && itemsTextureFilter.size > 0) {
            minFilterSB.setItems(itemsTextureFilter);
            magFilterSB.setItems(itemsTextureFilter);
        }

        uWrapSB = new VisSelectBox<>();
        uWrapSB.setName(ACTOR_UWRAP);
        uWrapSB.clearItems();

        vWrapSB = new VisSelectBox<>();
        vWrapSB.setName(ACTOR_VWRAP);
        vWrapSB.clearItems();

        if (itemsTextureWrap != null && itemsTextureWrap.size > 0) {
            uWrapSB.setItems(itemsTextureWrap);
            vWrapSB.setItems(itemsTextureWrap);
        }

        // Select Box: Textures
        textureSelectBox = new VisSelectBox<>();
        Array<String> itemsTexture = new Array<>();
        itemsTexture.add("No Texture Selected");

        // All PNG files in the same directory and direct subdirecories the asset is located
        FileHandle assetFileHandle = modelES.stage.modelSelectBox.getSelected();
        Array<FileHandle> textureFileHandleArray = texturesLookUp(assetFileHandle);

        if (textureFileHandleArray != null && textureFileHandleArray.size > 0) {
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

        minFilterSB.addListener(paramSelectBoxListener);
        magFilterSB.addListener(paramSelectBoxListener);
        uWrapSB.addListener(paramSelectBoxListener);
        vWrapSB.addListener(paramSelectBoxListener);
        textureSelectBox.addListener(textureSelectBoxListener);

        add(enabledCheckBox);
        add(new VisLabel("offsetU:")).right();
        add(offsetUTF).width(40).maxWidth(40);
        add(new VisLabel("minFilter:")).right();
        add(minFilterSB).fillX();
        row();

        add();
        add(new VisLabel("offsetV:")).right();
        add(offsetVTF).width(40).maxWidth(40);
        add(new VisLabel("magFilter:")).right();
        add(magFilterSB).fillX();
        row();

        add();
        add(new VisLabel("scaleU:")).right();
        add(scaleUTF).width(40).maxWidth(40);
        add(new VisLabel("uWrap:")).right();
        add(uWrapSB).fillX();
        row();

        add();
        add(new VisLabel("scaleV:")).right();
        add(scaleVTF).width(40).maxWidth(40);
        add(new VisLabel("vWrap:")).right();
        add(vWrapSB).fillX();
        row();

        add(textureSelectBox).colspan(5).fillX();
    }

    private Array<FileHandle> texturesLookUp (FileHandle assetFileHandle) {
        if (assetFileHandle == null) { return null; }
        Array<FileHandle> textureFileHandleArray = HGUtils.traversFileHandle(assetFileHandle.parent(), filterTextures);

        // TODO: Add unified convention like "textures | skins" to specify all folders at once
        // All texture files in the "textures" directory and subdirectories (if any) on asset's path
        textureFileHandleArray = HGUtils.traversFileHandle(
                // starting at parent() since we already traversed current folder/subfolders above
                HGUtils.fileOnPath(assetFileHandle.parent(), "textures"),
                filterTextures,
                textureFileHandleArray
        );
        // All texture files in the "skins" directory and subdirectories (if any) on asset's path
        textureFileHandleArray = HGUtils.traversFileHandle(
                // starting at parent() since we already traversed current folder/subfolders above
                HGUtils.fileOnPath(assetFileHandle.parent(), "skins"),
                filterTextures,
                textureFileHandleArray
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
    protected TextureAttribute createAttribute(String alias) {
        if (texture == null) {
            return null;
        }
        TextureAttribute out = null;
        switch (alias) {
            case TextureAttribute.DiffuseAlias: out = TextureAttribute.createDiffuse(texture); break;
            case TextureAttribute.SpecularAlias: out = TextureAttribute.createSpecular(texture); break;
            case TextureAttribute.BumpAlias: out = TextureAttribute.createBump(texture); break;
            case TextureAttribute.NormalAlias: out = TextureAttribute.createNormal(texture); break;
            case TextureAttribute.AmbientAlias: out = TextureAttribute.createAmbient(texture); break;
            case TextureAttribute.EmissiveAlias: out = TextureAttribute.createEmissive(texture); break;
            case TextureAttribute.ReflectionAlias: out = TextureAttribute.createReflection(texture); break;
        }
        if (out != null) {
            // no-arg constructor "public TextureDescriptor ()" doesn't fill these out
            out.textureDescription.minFilter = texture.getMinFilter();
            out.textureDescription.magFilter = texture.getMagFilter();
            out.textureDescription.uWrap = texture.getUWrap();
            out.textureDescription.vWrap = texture.getVWrap();
        }

        return out;
    }

    private void createListeners() {
        paramTextFieldListener = new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
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
                                case ACTOR_OFFSETU:
                                    attr.offsetU = value;
                                    break;
                                case ACTOR_OFFSETV:
                                    attr.offsetV = value;
                                    break;
                                case ACTOR_SCALEU:
                                    attr.scaleU = value;
                                    break;
                                case ACTOR_SCALEV:
                                    attr.scaleV = value;
                                    break;
                            }
                        }
                        if (attr != null && listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
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
                            case ACTOR_MINFILTER:
                                attr.textureDescription.minFilter = Texture.TextureFilter.valueOf(minFilterSB.getSelected());
                                break;
                            case ACTOR_MAGFILTER:
                                attr.textureDescription.magFilter = Texture.TextureFilter.valueOf(magFilterSB.getSelected());
                                break;
                            case ACTOR_UWRAP:
                                attr.textureDescription.uWrap = Texture.TextureWrap.valueOf(uWrapSB.getSelected());
                                break;
                            case ACTOR_VWRAP:
                                attr.textureDescription.vWrap = Texture.TextureWrap.valueOf(vWrapSB.getSelected());
                                break;
                        }
                    }
                    if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
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
                    if (listener != null) { listener.onAttributeChange(container, currentType, currentTypeAlias); }
                }
            }
        };
    }

    @Override
    protected boolean preCreateAttr() {
        if (textureSelectBox == null) { return false; }
        if (textureSelectBox.getSelectedIndex() == 0) {
            texture = null;
            enabledCheckBox.setProgrammaticChangeEvents(false); // making sure no events fired during setChecked()
            enabledCheckBox.setChecked(false);                  // no texture attribute gets enabled without the texture selected first
            enabledCheckBox.setProgrammaticChangeEvents(true);  // enabling events back
            Gdx.app.debug("enabledCheckBox", "No texture selected: type = 0x"
                    + Long.toHexString(currentType) + " alias = " + currentTypeAlias);
            return false;
        } else {
            if (!modelES.eng.assetManager.contains(textureSelectBox.getSelected())) {
                Gdx.app.debug("enabledCheckBox", "Texture is not loaded from: " + textureSelectBox.getSelected()
                        + " (attribute: type = 0x" + Long.toHexString(currentType) + " alias = " + currentTypeAlias + ")");
                enabledCheckBox.setProgrammaticChangeEvents(false); // making sure no events fired during setChecked()
                enabledCheckBox.setChecked(false);                  // no texture attribute gets enabled without the texture selected first
                enabledCheckBox.setProgrammaticChangeEvents(true);  // enabling events back
                return false;
            }
            texture = modelES.eng.assetManager.get(textureSelectBox.getSelected(), Texture.class);
        }
        return true;
    }

    @Override
    protected void postRemoveAttr() {
        modelES.stage.textureImage.setDrawable(null);
    }

    @Override
    protected void fetchWidgetsFromAttribute(TextureAttribute attr) {
        // https://github.com/libgdx/libgdx/wiki/Scene2d.ui#textfield
        if (offsetUTF != null) { offsetUTF.setText(String.valueOf(attr.offsetU)); }
        if (offsetVTF != null) { offsetVTF.setText(String.valueOf(attr.offsetV)); }
        if (scaleUTF != null) { scaleUTF.setText(String.valueOf(attr.scaleU)); }
        if (scaleVTF != null) { scaleVTF.setText(String.valueOf(attr.scaleV)); }
        if (minFilterSB != null) { minFilterSB.setSelected(attr.textureDescription.minFilter.name()); }
        if (magFilterSB != null) { magFilterSB.setSelected(attr.textureDescription.magFilter.name()); }
        if (uWrapSB != null) { uWrapSB.setSelected(attr.textureDescription.uWrap.name()); }
        if (vWrapSB != null) { vWrapSB.setSelected(attr.textureDescription.vWrap.name()); }
        // TODO: one more parameter is currently missing: uvIndex (see TextureAttribute)

        if (textureSelectBox != null) {
            textureSelectBox.getSelection().setProgrammaticChangeEvents(false); // disabling events
            if (attr.textureDescription.texture.getTextureData() instanceof FileTextureData) {
                textureSelectBox.setSelected(attr.textureDescription.texture.toString());
            } else {
                textureSelectBox.setSelectedIndex(0);
            }
            textureSelectBox.getSelection().setProgrammaticChangeEvents(true);  // enabling events
        }

        preCreateAttr(); // to setup a texture
        if (texture != null) {
            // see Image (Texture texture) for example on how to convert Texture to Image
            modelES.stage.textureImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        }
    }

    protected void resetWidgetsToDefaults() {
        if (enabledCheckBox != null) { enabledCheckBox.setChecked(false); }
        if (offsetUTF != null) { offsetUTF.setText("0"); }
        if (offsetVTF != null) { offsetVTF.setText("0"); }
        if (scaleUTF != null) { scaleUTF.setText("1"); }
        if (scaleVTF != null) { scaleVTF.setText("1"); }
        if (minFilterSB != null) { minFilterSB.setSelected(itemsTextureFilter.get(0)); }
        if (magFilterSB != null) { magFilterSB.setSelected(itemsTextureFilter.get(0)); }
        if (uWrapSB != null) { uWrapSB.setSelected(itemsTextureWrap.get(0)); }
        if (vWrapSB != null) { vWrapSB.setSelected(itemsTextureWrap.get(0)); }
        // TODO: one more parameter is currently missing: uvIndex (see TextureAttribute)
        if (textureSelectBox != null) { textureSelectBox.setSelectedIndex(0); }
    }
}
