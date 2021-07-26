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

package com.hammergenics.core;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGAsset<T> implements Disposable {
    /**
     * texture object
     */
    public T obj;
    /**
     * asset file handle
     */
    public FileHandle afh;

    public HGAsset(T asset) { this(asset, null); }
    public HGAsset(T asset, FileHandle assetFileHandle) {
        this.obj = asset;
        this.afh = assetFileHandle;
    }

    @Override
    public void dispose() {

    }
}
