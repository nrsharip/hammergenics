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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.screens.ModelPreviewScreen;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class DirectionalLightsAttributeTable extends AttributeTable<DirectionalLightsAttribute> {
    private static final Color COLOR_DISABLED = Color.GRAY;
    private static final Color COLOR_PRESSED = Color.RED;
    private static final Color COLOR_UNPRESSED = Color.WHITE;

    public TextButton plsTextButton = null;
    public TextButton mnsTextButton = null;
    public Array<TextButton> indexedTB = null;
    private Array<DirectionalLight> lights = new Array<>(DirectionalLight.class);
    private Table indexedTBTable = new Table();

    public DirectionalLightsAttributeTable(Skin skin, Attributes container, ModelPreviewScreen mps) {
        super(skin, container, mps, DirectionalLightsAttribute.class);

        plsTextButton = new TextButton("+", skin);
        plsTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (plsTextButton.getColor().equals(COLOR_DISABLED) || lights == null || indexedTB == null) { return; }

                if (!enabledCheckBox.isChecked()) { enabledCheckBox.setChecked(true); }

                addButton();

                lights.add(new DirectionalLight().set(Color.WHITE, -0.7f, -0.5f, -0.3f));

                mnsTextButton.getColor().set(COLOR_UNPRESSED);
                mnsTextButton.getLabel().getColor().set(COLOR_UNPRESSED);

                if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
            }
        });

        mnsTextButton = new TextButton("-", skin);
        mnsTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (mnsTextButton.getColor().equals(COLOR_DISABLED) || lights == null || indexedTB == null) { return; }

                indexedTBTable.getCell(indexedTB.get(indexedTB.size - 1)).clearActor().reset();
                indexedTBTable.removeActor(indexedTB.get(indexedTB.size - 1));
                indexedTB.get(indexedTB.size - 1).remove();
                indexedTB.removeIndex(indexedTB.size - 1);

                lights.removeIndex(lights.size - 1);

                if (indexedTB.size == 0 || lights.size == 0) { // these should be equal
                    mnsTextButton.getColor().set(COLOR_DISABLED);
                    mnsTextButton.getLabel().getColor().set(COLOR_DISABLED);
                }

                if (listener != null) { listener.onAttributeChange(currentType, currentTypeAlias); }
            }
        });

        Table line1 = new Table();
        Table line2 = new Table();

        line1.add(enabledCheckBox);
        line1.add(new Label("lights:", skin)).right();
        line1.add(mnsTextButton).width(20f).maxWidth(20f);
        line1.add(indexedTBTable);
        line1.add(plsTextButton).width(20f).maxWidth(20f);
        line1.add().expandX();

//        line2.add(new Label("src:", skin)).right();
//        line2.add(srcFuncSB);
//        line2.row();
//        line2.add(new Label("dst:", skin)).right();
//        line2.add(dstFuncSB);

        add(line1).fillX();
        row();
        add(line2).fillX();

    }

    private void addButton() {
        TextButton button = new TextButton(String.valueOf(indexedTB.size + 1), this.uiSkin);

        button.setName(String.valueOf(indexedTB.size));

        indexedTB.add(button);

        indexedTBTable.add(button).width(20f).maxWidth(20f);
    }

    @Override
    protected boolean preCreateAttr() {
        return true;
    }

    @Override
    protected void fetchWidgetsFromAttribute(DirectionalLightsAttribute attr) {
        this.lights = attr.lights;

        resetWidgetsToDefaults();

        Gdx.app.debug(getClass().getSimpleName(), "lights size: " + lights.size);
    }

    @Override
    protected void postRemoveAttr() {
        indexedTBTable.reset();
        mnsTextButton.getColor().set(COLOR_DISABLED);
        mnsTextButton.getLabel().getColor().set(COLOR_DISABLED);
    }

    @Override
    protected void resetWidgetsToDefaults() {
        indexedTB = new Array<>(TextButton.class);
        indexedTBTable.reset();

        if (lights == null || lights.size == 0) { // lights shouldn't be null
            mnsTextButton.getColor().set(COLOR_DISABLED);
            mnsTextButton.getLabel().getColor().set(COLOR_DISABLED);
        } else {
            mnsTextButton.getColor().set(COLOR_UNPRESSED);
            mnsTextButton.getLabel().getColor().set(COLOR_UNPRESSED);
        }

        this.lights.forEach(light -> {
            addButton();
        });
    }

    @Override
    protected DirectionalLightsAttribute createAttribute(String alias) {
        // this is a bit hacky since the way you add lights attribute is environment.add(...)
        DirectionalLightsAttribute lightsAttribute = new DirectionalLightsAttribute();
        lightsAttribute.lights.addAll(this.lights);
        return lightsAttribute;
    }
}
