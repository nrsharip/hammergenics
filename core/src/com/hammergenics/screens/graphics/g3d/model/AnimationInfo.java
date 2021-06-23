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

package com.hammergenics.screens.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationInfo {
    public DebugModelInstance mi;
    public Animation a;
    public FloatArray keyTimes = new FloatArray(true, 16); // aggregated from all Node Animations
    public ArrayMap<NodeAnimation, FloatArray> nAnim2keyTimes = new ArrayMap<>(NodeAnimation.class, FloatArray.class);
    public float minStep = 0f;

    public AnimationInfo(DebugModelInstance mi, Animation animation) {
        this.mi = mi;
        this.a = animation;
        checkAnimation();
    }

    public void checkAnimation() {
        nAnim2keyTimes.clear();
        FloatArray fa = new FloatArray(true, 16);
        float duplicate;
        for (NodeAnimation nAnim: a.nodeAnimations) {
            FloatArray keyTimes = new FloatArray(true, 16);
            fa.clear();
            if (nAnim.translation != null) for (NodeKeyframe<Vector3> kf: nAnim.translation) { fa.add(kf.keytime); }
            if (nAnim.rotation != null) for (NodeKeyframe<Quaternion> kf: nAnim.rotation) { fa.add(kf.keytime); }
            if (nAnim.scaling != null) for (NodeKeyframe<Vector3> kf: nAnim.scaling) { fa.add(kf.keytime); }
            fa.sort(); // IMPORTANT
            duplicate = -1f;
            for (float kt: fa.toArray()) {
                if (kt == duplicate) { continue; }
                keyTimes.add(kt);
                duplicate = kt;
            }
            nAnim2keyTimes.put(nAnim, keyTimes);
        }

        keyTimes.clear();
        fa.clear();
        for (ObjectMap.Entry<NodeAnimation, FloatArray> entry: nAnim2keyTimes) {
            //Gdx.app.debug(getClass().getSimpleName(), ""
            //        + mi.afh.nameWithoutExtension() + ":" + a.id + " - "
            //        + " node id: " + entry.key.node.id
            //        + " key times: " + entry.value.toString()
            //);
            fa.addAll(entry.value);
        }
        fa.sort(); // IMPORTANT
        duplicate = -1f;
        for (float kt: fa.toArray()) {
            if (kt == duplicate) { continue; }
            keyTimes.add(kt);
            duplicate = kt;
        }

        minStep = 0f; float prev = 0f;
        for (float keyTime:keyTimes.toArray()) { minStep = keyTime - prev; prev = keyTime; }

        //Gdx.app.debug(getClass().getSimpleName(), ""
        //        + mi.afh.nameWithoutExtension() + ":" + a.id + " - "
        //        + " min step: " + minStep
        //        + " OVERALL key times: " + keyTimes.toString()
        //);
    }
}