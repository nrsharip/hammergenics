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

package com.hammergenics.screens.graphics.g3d.saver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.UBJsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader.VERSION_HI;
import static com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader.VERSION_LO;
import static com.badlogic.gdx.utils.JsonWriter.OutputType;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class G3dModelSaver {
    // see:
    // - G3dModelLoader.loadModelData
    // - G3dModelLoader.parseModel
    // - G3dModelLoader.parseMeshes(model, json);
    // - G3dModelLoader.parseMaterials(model, json, handle.parent().path());
    // - G3dModelLoader.parseNodes(model, json);
    // - G3dModelLoader.parseAnimations(model, json);

    public JsonWriter g3djWriter;
    public UBJsonWriter g3dbWriter;
    public FileHandleResolver resolver = new LocalFileHandleResolver();

    public void saveG3dj(FileHandle fh, ModelInstance mi) {
        g3djWriter = new JsonWriter(fh.writer(false, "UTF-8"));
        g3djWriter.setOutputType(OutputType.json);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();

        try {
            g3djWriter.object(); // root object
            //g3djWriter.name("raw").value(now).name("fmt").value(fmt.format(now));

            g3djWriter.name("version").array().value(VERSION_HI).value(VERSION_LO).pop();
            g3djWriter.name("id").value("");

            g3djWriter.close();
        } catch (IOException e) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR writing to file: " + e.getMessage());
        }
    }

    public void saveG3db(FileHandle fh, ModelInstance mi) {
        //g3dbWriter = new UBJsonWriter(fh.write(false, 8192));
    }
}