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
import com.badlogic.gdx.assets.loaders.I18NBundleLoader.I18NBundleParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.I18NBundle;
import com.hammergenics.core.ModelEditScreen;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.VisUI;

import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import static com.hammergenics.HGGame.I18NBundlesEnum.ENGLISH;
import static com.hammergenics.core.stages.ui.file.TypeFilterRulesEnum.MODEL_FILES;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGame extends Game {
    public HGEngine engine;
    public ModelBatch modelBatch;
    public ModelCache modelCache;
    public SpriteBatch spriteBatch;
    public GroovyShell groovyShell;
    public Binding sharedData;
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
        HGUtils.traversFileHandle(Gdx.files.internal("root"), MODEL_FILES.fileFilter, engine.folder2models); // syncup: asset manager

        // https://github.com/libgdx/libgdx/wiki/ModelBatch#default-shader
        // The behavior of DefaultShader class is configurable by supplying
        // a DefaultShader.Config instance to the DefaultShaderProvider.

        DefaultShader.Config configDefault = new DefaultShader.Config();
//        configDefault.vertexShader = null;        // The uber vertex shader to use, null to use the default vertex shader.
//        configDefault.fragmentShader = null;      // The uber fragment shader to use, null to use the default fragment shader.
//        configDefault.numDirectionalLights = 2;   // The number of directional lights to use
//        configDefault.numPointLights = 5;         // The number of point lights to use
//        configDefault.numSpotLights = 0;          // The number of spot lights to use
//        configDefault.numBones = 12;              // The number of bones to use
//        configDefault.ignoreUnimplemented = true; //
//        configDefault.defaultCullFace = -1;       // Set to 0 to disable culling, -1 to inherit from {@link DefaultShader#defaultCullFace}
//        configDefault.defaultDepthFunc = -1;      // Set to 0 to disable depth test, -1 to inherit from {@link DefaultShader#defaultDepthFunc}

        PBRShaderConfig configPBR = new PBRShaderConfig();
//        configPBR.manualSRGB = PBRShaderConfig.SRGB.ACCURATE;
//        configPBR.glslVersion = null;     // string to prepend to shaders (version), automatic if null
//        configPBR.numVertexColors = 1;    // Max vertex color layers. Default PBRShader only use 1 layer,
//                                          // custom shaders can implements more
//        configPBR.useTangentSpace = true; // Whether shaders will use tangent space.
//                                          // If true, mesh require tangent vertex attribute to work on all platforms.
//                                          // You typically set it to false when your custom shaders don't use tangents and normal matrix.

        // https://github.com/libgdx/libgdx/wiki/ModelBatch
        // You'd typically create a ModelBatch in the create() method.

        modelBatch = new ModelBatch(new DefaultShaderProvider(configDefault));

        // Enable PBR Shader. see: https://github.com/mgsx-dev/gdx-gltf
        // IMPORTANT: the model instances need to have environment provided while rendered otherwise:
        // Exception in thread "LWJGL Application" java.lang.NullPointerException
        //        [ renderable.environment.has(FogAttribute.FogEquation) ]
        //        at net.mgsx.gltf.scene3d.shaders.PBRShaderProvider.createShader(PBRShaderProvider.java:245)
        //        at com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider.getShader(BaseShaderProvider.java:34)
        //        at com.badlogic.gdx.graphics.g3d.ModelBatch.render(ModelBatch.java:238)
        //        at com.hammergenics.core.ModelEditScreen.render(ModelEditScreen.java:228)
        // modelBatch = new ModelBatch(new PBRShaderProvider(configPBR));


        // see: https://github.com/libgdx/libgdx/wiki/ModelCache#using-modelcache
        modelCache = new ModelCache();

        spriteBatch = new SpriteBatch();

        // https://github.com/kotcrab/vis-ui#usage
        VisUI.load();
        //VisUI.getSkin().get(VisCheckBox.VisCheckBoxStyle.class).fontColor = Color.BLACK.cpy();
        //VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class).fontColor = Color.WHITE.cpy();

        ENGLISH.apply(engine.assetManager);

        ModelEditScreen screen = new ModelEditScreen(this, engine, modelBatch, modelCache);

        sharedData = new Binding();

        sharedData.setProperty("game", this);
        sharedData.setProperty("engine", engine);
        sharedData.setProperty("screen", screen);
        sharedData.setProperty("stage", screen.stage);
        groovyShell = new GroovyShell(sharedData);

        this.setScreen(screen);
    }

    public String help() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nAvailable Variables:\n");

        ((Map<String, ?>)sharedData.getVariables()).forEach((k, v) -> {
            //String.format("%15s:%20s\n", k, v.getClass().getSimpleName());
            sb.append(k).append(": ").append(v.getClass().getSimpleName()).append("\n");
        });

        sb.append("\nFor additional info run 'groovy game.help(\"<variable>\")', e.g.: groovy game.help(\"engine\")\n");

        return sb.toString();
    }

    public String help(String variable) {
        StringBuilder sb = new StringBuilder();

        sb.append("Variable: ").append(variable).append("\n\n");

        for (Field field: HGUtils.scanPublicFields(sharedData.getVariable(variable).getClass())) {
            sb.append("Field: ").append(field.getName()).append("\n");
        }

        return sb.toString();
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
        RUSSIAN(localeRu, "??????????????");

        public static I18NBundlesEnum applied = null;

        public final Locale locale;
        public final String lang;

        public I18NBundle visUIButtonBarBundle;    private static final String path1 = "root/bundles/visui/ButtonBar";
        public I18NBundle visUIColorPickerBundle;  private static final String path2 = "root/bundles/visui/ColorPicker";
        public I18NBundle visUICommonBundle;       private static final String path3 = "root/bundles/visui/Common";
        public I18NBundle visUIDialogsBundle;      private static final String path4 = "root/bundles/visui/Dialogs";
        public I18NBundle visUIFileChooserBundle;  private static final String path5 = "root/bundles/visui/FileChooser";
        public I18NBundle modelEditStageBundle;    private static final String path6 = "root/bundles/stage/ModelEditStage";
        public I18NBundle projectManagerBundle;    private static final String path7 = "root/bundles/stage/ProjectManager";
        public I18NBundle attributesManagerBundle; private static final String path8 = "root/bundles/stage/AttributesManager";

        I18NBundlesEnum(Locale locale, String lang) { this.locale = locale; this.lang = lang; }

        private void acquire(AssetManager am) {
            //Gdx.app.debug("locale1", "" + Locale.getDefault());

            // see https://github.com/libgdx/libgdx/wiki/Internationalization-and-Localization#creating-a-bundle
            // see I18NBundleLoader.loadAsync():
            // if (parameter == null) { locale = Locale.getDefault(); ... } else { locale = parameter.locale == null ? Locale.getDefault() : parameter.locale; ... }
            // see I18NBundle.DEFAULT_ENCODING = "UTF-8"
            I18NBundleParameter parameter = new I18NBundleParameter(Locale.getDefault(), "UTF-8");

            if (am.contains(path1)) { am.unload(path1); } am.load(path1, I18NBundle.class, parameter);
            if (am.contains(path2)) { am.unload(path2); } am.load(path2, I18NBundle.class, parameter);
            if (am.contains(path3)) { am.unload(path3); } am.load(path3, I18NBundle.class, parameter);
            if (am.contains(path4)) { am.unload(path4); } am.load(path4, I18NBundle.class, parameter);
            if (am.contains(path5)) { am.unload(path5); } am.load(path5, I18NBundle.class, parameter);
            if (am.contains(path6)) { am.unload(path6); } am.load(path6, I18NBundle.class, parameter);
            if (am.contains(path7)) { am.unload(path7); } am.load(path7, I18NBundle.class, parameter);
            if (am.contains(path8)) { am.unload(path8); } am.load(path8, I18NBundle.class, parameter);
            am.finishLoading();

            visUIButtonBarBundle = am.get(path1, I18NBundle.class);
            visUIColorPickerBundle = am.get(path2, I18NBundle.class);
            visUICommonBundle = am.get(path3, I18NBundle.class);
            visUIDialogsBundle = am.get(path4, I18NBundle.class);
            visUIFileChooserBundle = am.get(path5, I18NBundle.class);
            modelEditStageBundle = am.get(path6, I18NBundle.class);
            projectManagerBundle = am.get(path7, I18NBundle.class);
            attributesManagerBundle = am.get(path8, I18NBundle.class);

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

        spriteBatch.dispose();

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