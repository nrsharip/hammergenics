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

package com.hammergenics.core.stages.ui.file;

import com.badlogic.gdx.utils.Array;

import java.io.FileFilter;

public enum TypeFilterRulesEnum {
    HG_FILES("Save files", file -> file.isDirectory()
            || file.getName().toLowerCase().endsWith(".hg"), "hg"),
    MODEL_FILES("Model files", file -> file.isDirectory()
            //|| file.getName().toLowerCase().endsWith(".3ds") // converted to G3DB with fbx-conv
            || file.getName().toLowerCase().endsWith(".g3db")  // binary
            || file.getName().toLowerCase().endsWith(".g3dj")  // json
            || file.getName().toLowerCase().endsWith(".glb")   //
            || file.getName().toLowerCase().endsWith(".gltf")  //
            || file.getName().toLowerCase().endsWith(".obj"), "obj", "gltf", "glb", "g3dj", "g3db"),
    IMAGE_FILES("Image files", file -> file.isDirectory()
            || file.getName().toLowerCase().endsWith(".bmp")  // textures in BMP
            || file.getName().toLowerCase().endsWith(".png")  // textures in PNG
            || file.getName().toLowerCase().endsWith(".tga"), "bmp", "png", "tga"),
    FONT_FILES("Font files", file -> file.isDirectory()
            || file.getName().toLowerCase().endsWith(".fnt"), "fnt"),
    SOUND_FILES("Sound files", file -> file.isDirectory()
            // see: https://github.com/libgdx/libgdx/wiki/Sound-effects
            // Sound effects can be stored in various formats. libGDX supports MP3, OGG and WAV files.
            // RoboVM (iOS) currently does not support OGG files.
            || file.getName().toLowerCase().endsWith(".mp3")
            || file.getName().toLowerCase().endsWith(".ogg")
            || file.getName().toLowerCase().endsWith(".wav"), "mp3", "ogg", "wav"),
    ALL_FILES("All files", file -> false) {
        @Override
        public String getDescription() {
            if (extensions.size == 0) {
                for (TypeFilterRulesEnum tfr: TypeFilterRulesEnum.values()) {
                    extensions.addAll(tfr.extensions);
                }
            }
            return super.getDescription();
        }

        @Override
        public FileFilter getFileFilter() {
            // OR combination of file filters of all type filters
            return file -> {
                for (TypeFilterRulesEnum tfr: TypeFilterRulesEnum.values()) {
                    if (tfr.fileFilter.accept(file)) { return true; }
                }
                return false;
            };
        }
    };

    private final static StringBuilder sb = new StringBuilder();
    public final String description;
    public final FileFilter fileFilter;
    public final Array<String> extensions = new Array<>(true, 16, String.class);

    TypeFilterRulesEnum(String description, FileFilter fileFilter, String... extensions) {
        this.description = description;
        this.fileFilter = fileFilter;
        this.extensions.addAll(extensions);
    }

    public String getDescription() {
        sb.setLength(0);
        sb.append(description).append(" (*.").append(extensions.toString(", *.")).append(")");
        return sb.toString();
    }

    public FileFilter getFileFilter() { return fileFilter; }

    public String[] getExtensions() { return extensions.toArray(); }
}