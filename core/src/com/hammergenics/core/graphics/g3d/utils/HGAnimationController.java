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

package com.hammergenics.core.graphics.g3d.utils;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.hammergenics.HGEngine;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.model.AnimationInfo;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGAnimationController extends AnimationController {
    public EditableModelInstance mi = null;
    // TODO: decouple animation controller from the engine (since it is really a part of model instance).
    //       Make use of AnimationInfo.keyTimes2callbacks and AnimationInfo.KeyTimeCallback interface
    public HGEngine eng = null;
    public long soundId = -1;

    public HGAnimationController(ModelInstance target) {
        super(target);
        if (target instanceof EditableModelInstance) { mi = (EditableModelInstance)target; }
    }

    @Override
    protected void applyAnimation(Animation animation, float time) {
        super.applyAnimation(animation, time);

        AnimationInfo info = mi.anim2info.get(animation);

        FileHandle soundFileHandle = info.keyTimes2sounds.get(info.floorKeyTime(time));

        // TODO: use AnimationInfo.KeyTimeCallback.applyAnimation instead of directly handling the
        //       assets (sound play) here.
        Sound sound = eng.getAsset(soundFileHandle, Sound.class);
        // https://github.com/libgdx/libgdx/wiki/Sound-effects
        if (sound != null && soundId == -1) {
            soundId = sound.play(1f);
        } else if (sound == null) {
            soundId = -1;
        }
    }

    // TODO: decouple animation controller from the engine (since it is really a part of model instance).
    //       Make use of AnimationInfo.keyTimes2callbacks and AnimationInfo.KeyTimeCallback interface
    public void setEng(HGEngine eng) { this.eng = eng; }
}