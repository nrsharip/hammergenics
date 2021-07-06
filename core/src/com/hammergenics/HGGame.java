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

package com.hammergenics;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.I18NBundle;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.utils.HGUtils;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTextButton;

import java.nio.charset.Charset;
import java.util.Locale;

import static com.hammergenics.HGEngine.filterModels;
import static com.hammergenics.HGGame.I18NBundlesEnum.ENGLISH;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGame extends Game {
    public HGEngine engine;
    public ModelBatch modelBatch;
    public ModelCache modelCache;

    /**
     *
     */
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getDensity() = " + Gdx.graphics.getDensity());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getWidth()   = " + Gdx.graphics.getWidth());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.graphics.getHeight()  = " + Gdx.graphics.getHeight());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.getExternalStoragePath() = " + Gdx.files.getExternalStoragePath());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.getLocalStoragePath()    = " + Gdx.files.getLocalStoragePath());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.isExternalStorageAvailable() = " + Gdx.files.isExternalStorageAvailable());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.isLocalStorageAvailable()    = " + Gdx.files.isLocalStorageAvailable());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.classpath(\"\") = " + Gdx.files.classpath("").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.internal(\"\")  = " + Gdx.files.internal("").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.external(\"\")  = " + Gdx.files.external("").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.absolute(\"\")  = " + Gdx.files.absolute("").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.local(\"\")     = " + Gdx.files.local("").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.classpath(\"./\") = " + Gdx.files.classpath("./").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.internal(\"./\")  = " + Gdx.files.internal("./").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.external(\"./\")  = " + Gdx.files.external("./").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.absolute(\"./\")  = " + Gdx.files.absolute("./").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.local(\"./\")     = " + Gdx.files.local("./").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.classpath(\"/\") = " + Gdx.files.classpath("/").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.internal(\"/\")  = " + Gdx.files.internal("/").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.external(\"/\")  = " + Gdx.files.external("/").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.absolute(\"/\")  = " + Gdx.files.absolute("/").file().getAbsoluteFile());
        Gdx.app.debug(getClass().getSimpleName(),"Gdx.files.local(\"/\")     = " + Gdx.files.local("/").file().getAbsoluteFile());
        System.getProperties().forEach((k, v) -> Gdx.app.debug(getClass().getSimpleName(),k + " = " + v));
        Gdx.app.debug(getClass().getSimpleName(), "Charset.defaultCharset().displayName() = " + Charset.defaultCharset().displayName());
        Gdx.app.debug(getClass().getSimpleName(), "Charset.defaultCharset().name()        = " + Charset.defaultCharset().name());

        engine = new HGEngine(this);
        // the map should be ordered: see resetFolderSelectBoxItems
        engine.folder2models = new ArrayMap<>(true, 16, FileHandle.class, Array.class);
        HGUtils.traversFileHandle(Gdx.files.internal("root"), filterModels, engine.folder2models); // syncup: asset manager

        // https://github.com/libgdx/libgdx/wiki/ModelBatch#default-shader
        // The behavior of DefaultShader class is configurable by supplying
        // a DefaultShader.Config instance to the DefaultShaderProvider.

        DefaultShader.Config config = new DefaultShader.Config();
//        config.vertexShader;         // String = null - The uber vertex shader to use, null to use the default vertex shader.
//        config.fragmentShader;       // String = null - The uber fragment shader to use, null to use the default fragment shader.
//        config.numDirectionalLights; // int = 2 The number of directional lights to use
//        config.numPointLights;       // int = 5 The number of point lights to use
//        config.numSpotLights;        // int = 0 The number of spot lights to use
//        config.numBones;             // int = 12 The number of bones to use
//        config.ignoreUnimplemented;  // boolean = true
//        config.defaultCullFace;      // int = -1 Set to 0 to disable culling, -1 to inherit from {@link DefaultShader#defaultCullFace}
//        config.defaultDepthFunc;     // int = -1 Set to 0 to disable depth test, -1 to inherit from {@link DefaultShader#defaultDepthFunc}

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // You'd typically create a ModelBatch in the create() method.
        modelBatch = new ModelBatch(new DefaultShaderProvider(config));

        // see: https://github.com/libgdx/libgdx/wiki/ModelCache#using-modelcache
        modelCache = new ModelCache();

        // https://github.com/kotcrab/vis-ui#usage
        VisUI.load();
        VisUI.getSkin().get(Label.LabelStyle.class).fontColor = Color.BLACK.cpy();
        VisUI.getSkin().get(VisCheckBox.VisCheckBoxStyle.class).fontColor = Color.BLACK.cpy();
        VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class).fontColor = Color.WHITE.cpy();

        ENGLISH.apply(engine.assetManager);

        this.setScreen(new ModelEditScreen(this, engine, modelBatch, modelCache));
    }

    // see java.util.LocaleISOData.java : isoLanguageTable
    // see java.util.LocaleISOData.java : isoCountryTable
    // see java.util.Locale.java : getInstance -> LOCALECACHE.get(key)
    // see java.util.Locale.java : static public final Locale ENGLISH
    // see java.util.Locale.java : static public final Locale FRENCH
    // see java.util.Locale.java : static public final Locale GERMAN
    //                             ...
    public static Locale localeEn = Locale.ENGLISH;
    public static Locale localeRu = new Locale("ru");

    public enum I18NBundlesEnum {
        ENGLISH(localeEn, "english"),
        RUSSIAN(localeRu, "русский");

        public static I18NBundlesEnum applied = null;

        public final Locale locale;
        public final String lang;

        public I18NBundle visUIButtonBarBundle;   private static final String path1 = "root/bundles/visui/ButtonBar";
        public I18NBundle visUIColorPickerBundle; private static final String path2 = "root/bundles/visui/ColorPicker";
        public I18NBundle visUICommonBundle;      private static final String path3 = "root/bundles/visui/Common";
        public I18NBundle visUIDialogsBundle;     private static final String path4 = "root/bundles/visui/Dialogs";
        public I18NBundle visUIFileChooserBundle; private static final String path5 = "root/bundles/visui/FileChooser";
        public I18NBundle modelEditStageBundle;   private static final String path6 = "root/bundles/stage/ModelEditStage";

        I18NBundlesEnum(Locale locale, String lang) { this.locale = locale; this.lang = lang; }

        private void acquire(AssetManager am) {
            //Gdx.app.debug("locale1", "" + Locale.getDefault());

            // see https://github.com/libgdx/libgdx/wiki/Internationalization-and-Localization#creating-a-bundle
            // see I18NBundleLoader.loadAsync():
            // if (parameter == null) { locale = Locale.getDefault(); ... } else { locale = parameter.locale == null ? Locale.getDefault() : parameter.locale; ... }
            I18NBundleLoader.I18NBundleParameter parameter = new I18NBundleLoader.I18NBundleParameter(Locale.getDefault());

            if (am.contains(path1)) { am.unload(path1); } am.load(path1, I18NBundle.class);
            if (am.contains(path2)) { am.unload(path2); } am.load(path2, I18NBundle.class);
            if (am.contains(path3)) { am.unload(path3); } am.load(path3, I18NBundle.class);
            if (am.contains(path4)) { am.unload(path4); } am.load(path4, I18NBundle.class);
            if (am.contains(path5)) { am.unload(path5); } am.load(path5, I18NBundle.class);
            if (am.contains(path6)) { am.unload(path6); } am.load(path6, I18NBundle.class);
            am.finishLoading();

            visUIButtonBarBundle = am.get(path1, I18NBundle.class);
            visUIColorPickerBundle = am.get(path2, I18NBundle.class);
            visUICommonBundle = am.get(path3, I18NBundle.class);
            visUIDialogsBundle = am.get(path4, I18NBundle.class);
            visUIFileChooserBundle = am.get(path5, I18NBundle.class);
            modelEditStageBundle = am.get(path6, I18NBundle.class);

            // see https://github.com/libgdx/libgdx/wiki/Internationalization-and-Localization
            // there's also an option of createBundle(fileHandle, locale) to have specific locale applied, if not - the default is applied
            //I18NBundle visUIButtonBarBundleEn = I18NBundle.createBundle(Gdx.files.internal("root/bundles/visui/ButtonBar"));
            //I18NBundle visUIColorPickerBarBundleEn = I18NBundle.createBundle(Gdx.files.internal("root/bundles/visui/ColorPicker"));
            //I18NBundle visUICommonBarBundleEn = I18NBundle.createBundle(Gdx.files.internal("root/bundles/visui/Common"));
            //I18NBundle visUIDialogsBarBundleEn = I18NBundle.createBundle(Gdx.files.internal("root/bundles/visui/Dialogs"));
            //I18NBundle visUIFileChooserBarBundleEn = I18NBundle.createBundle(Gdx.files.internal("root/bundles/visui/FileChooser"));
        }

        public void apply(AssetManager am) {
            Locale.setDefault(locale);
            Locales.setLocale(locale);

            acquire(am);

            //Gdx.app.debug("locale2", "" + visUIButtonBarBundle.getLocale());
            //Gdx.app.debug("locale2", "" + visUIColorPickerBundle.getLocale());

            // see https://github.com/kotcrab/vis-ui/wiki/VisUI-I18N
            Locales.setButtonBarBundle(visUIButtonBarBundle);
            Locales.setColorPickerBundle(visUIColorPickerBundle);
            Locales.setCommonBundle(visUICommonBundle);
            Locales.setDialogsBundle(visUIDialogsBundle);
            Locales.setFileChooserBundle(visUIFileChooserBundle);

            applied = this;
        };

        @Override public String toString() { return lang; }
    }

    /**
     *
     */
    @Override
    public void render() {
        super.render(); // important (see implementation)
    }

    /**
     *
     */
    @Override
    public void dispose() {
        // https://github.com/kotcrab/vis-ui#usage
        VisUI.dispose();

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // Because it contains native resources (like the shaders it uses),
        // you'll need to call the dispose() method when no longer needed.
        modelBatch.dispose();

        // https://github.com/libgdx/libgdx/wiki/ModelCache#using-modelcache
        // ModelCache owns several native resources.
        // Therefore you should dispose() the cache when no longer needed.
        modelCache.dispose();

        HGEngine.gridHgModel.dispose();
        HGEngine.lightsHgModel.dispose();
        HGEngine.boxHgModel.dispose();
        HGEngine.sphereHgModel.dispose();
        if (engine != null) { engine.dispose(); }

        // Note that the dispose() method of the GameScreen class is not called automatically, see the Screen API.
        // It is your responsibility to take care of that.
        // You can call this method from the dispose() method of the Game class,
        // if the GameScreen class passes a reference to itself to the Game class.
        getScreen().dispose();
    }
}