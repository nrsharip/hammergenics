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

package com.hammergenics.core.stages.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGGame;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.stages.ModelEditStage;
import com.hammergenics.HGUtils;
import com.kotcrab.vis.ui.i18n.BundleText;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import static com.hammergenics.core.stages.ui.AggregatedAttributesManagerVisTable.TextButtonsTextEnum.ENVIRONMENT;
import static com.hammergenics.core.stages.ui.AggregatedAttributesManagerVisTable.TextButtonsTextEnum.MATERIAL;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AggregatedAttributesManagerVisTable extends ManagerVisTable {
    public Cell<?> attrTableCell;
    
    public VisTextButton mtlTextButton;
    public VisTextButton envTextButton;
    public VisSelectBox<String> mtlSelectBox;

    public AggregatedAttributesManagerVisTable(ModelEditScreen modelES, ModelEditStage stage) {
        super(modelES, stage);

        VisTable topPanel = new VisTable();
        topPanel.add(envTextButton).pad(1f).padRight(5f);
        topPanel.add(mtlTextButton).pad(1f).padRight(5f);
        topPanel.add(mtlSelectBox).padBottom(1f);
        topPanel.add().expandX();
        add(topPanel).expandX().fillX().right();
        row();
        attrTableCell = add().expand().fill().left();
    }

    @Override
    protected void init() {
        mtlTextButton = new VisTextButton("Material"); MATERIAL.seize(mtlTextButton);
        stage.unpressButton(mtlTextButton);
        mtlTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressMtl();
                return super.touchDown(event, x, y, pointer, button); // false
            }
        });

        envTextButton = new VisTextButton("Environment"); ENVIRONMENT.seize(envTextButton);
        stage.unpressButton(envTextButton);
        envTextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressEnv();
                return super.touchDown(event, x, y, pointer, button); // false
            }
        });

        mtlSelectBox = new VisSelectBox<>();
        mtlSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetActors();
            }
        });
    }

    @Override
    public void setDbgModelInstances(Array<EditableModelInstance> mis) {
        super.setDbgModelInstances(mis);
        mtlSelectBox.getSelection().setProgrammaticChangeEvents(false);
        mtlSelectBox.clearItems();
        if (dbgModelInstance != null) { mtlSelectBox.setItems(dbgModelInstance.mtlIds); }
        mtlSelectBox.getSelection().setProgrammaticChangeEvents(true);

        resetActors();
    }

    public void unpressAllButtons() { stage.unpressButton(mtlTextButton); stage.unpressButton(envTextButton); }
    public boolean isAnyButtonPressed() { return stage.isPressed(mtlTextButton) || stage.isPressed(envTextButton); }

    public void pressEnv() {
        if (!stage.isPressed(envTextButton)) {
            unpressAllButtons();
            stage.pressButton(envTextButton);
            resetActors();
        }
    }

    public void pressMtl() {
        if (!stage.isPressed(mtlTextButton)) {
            unpressAllButtons();
            stage.pressButton(mtlTextButton);
            resetActors();
        }
    }

    @Override
    public void resetActors() {
        super.resetActors();

        stage.editCell.expand().fill().right();

        if (!isAnyButtonPressed()) { pressEnv(); }

        if (stage.isPressed(envTextButton) && modelES.environment != null) {
            attrTableCell.setActor(stage.envAttrTable);

            stage.envLabel.setText("Environment:\n" + HGUtils.extractAttributes(modelES.environment,"", ""));

            //stage.infoTCell.setActor(stage.envLabel);
        }

        if (stage.isPressed(mtlTextButton) && dbgModelInstance != null) {
            dbgModelInstance.createMtlAttributeTable(mtlSelectBox.getSelected(), stage.eventListener, modelES);
            attrTableCell.setActor(dbgModelInstance.mtlid2atable.get(mtlSelectBox.getSelected()));

            stage.miLabel.setText(HGUtils.getModelInstanceInfo(modelES.eng.getCurrMI()));

            //stage.infoTCell.setActor(stage.miLabel);
            //stage.infoBCell.setActor(stage.textureImage);
        } else if (stage.isPressed(mtlTextButton)) {
            pressEnv();
        }
    }

    @Override
    public void applyLocale(HGGame.I18NBundlesEnum language) {
        TextButtonsTextEnum.setLanguage(language);
    }

    public enum TextButtonsTextEnum implements BundleText {
        ENVIRONMENT("textButton.environment"),
        MATERIAL("textButton.material");

        private final String property;
        private TextButton instance = null;
        private static HGGame.I18NBundlesEnum language;

        TextButtonsTextEnum(String property) { this.property = property; }

        public static void setLanguage(HGGame.I18NBundlesEnum lang) {
            language = lang;

            for (TextButtonsTextEnum tb: TextButtonsTextEnum.values()) {
                if (tb.instance != null) { tb.instance.setText(tb.get()); }
            }
        }

        public TextButton seize(TextButton btn) {
            this.instance = btn;
            btn.setText(get());
            return btn;
        }

        @Override public String getName() { return property; }
        @Override public String get() { return language != null ? language.attributesManagerBundle.get(property) : "ERR"; }
        @Override public String format() { return language != null ? language.attributesManagerBundle.format(property) : "ERR"; }
        @Override public String format(Object... arguments) { return language != null ? language.attributesManagerBundle.format(property, arguments) : "ERR"; }
    }
}
