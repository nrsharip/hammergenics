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

package com.hammergenics.core.graphics.g3d.model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class AnimationInfo {
    public EditableModelInstance mi;
    public Animation a;

    public FloatArray keyTimes = new FloatArray(true, 16); // aggregated from all Node Animations
    // KeyTimeCallback is currently not being used
    public ArrayMap<Float, KeyTimeCallback> keyTimes2callbacks = new ArrayMap<>(Float.class, KeyTimeCallback.class);
    // see HGAnimationController.applyAnimation to see the use of sounds
    public ArrayMap<Float, FileHandle> keyTimes2sounds = new ArrayMap<>(Float.class, FileHandle.class);

    public ArrayMap<Node, NodeAnimation> n2nAnim = new ArrayMap<>(Node.class, NodeAnimation.class);
    public ArrayMap<NodeAnimation, Node> nAnim2n = new ArrayMap<>(NodeAnimation.class, Node.class);
    public ArrayMap<NodeAnimation, FloatArray> nAnim2keyTimes = new ArrayMap<>(NodeAnimation.class, FloatArray.class);
    public float minStep = 0f;

    // KeyTimeCallback is currently not being used
    public interface KeyTimeCallback {
        void applyAnimation(Animation animation, float time);
    }

    public AnimationInfo(EditableModelInstance mi, Animation animation) {
        this.mi = mi;
        this.a = animation;
        checkAnimation();
    }

    public void checkAnimation() {
        nAnim2keyTimes.clear();
        n2nAnim.clear();
        nAnim2n.clear();
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
            n2nAnim.put(nAnim.node, nAnim);
            nAnim2n.put(nAnim, nAnim.node);
        }

        // keyTimes is the array of all keyTimes for this animation
        // (aggregated from the individual NodeAnimation's NodeKeyFrames)
        // For keyTimes of a particular NodeAnimation (Node) see nAnim2keyTimes
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

        if (keyTimes.size > 1) {
            float lastTime = keyTimes.get(keyTimes.size - 1);
            if (a.duration < lastTime) { a.duration = lastTime; }
            minStep = Float.MAX_VALUE;
        } else {
            minStep = 0.1f;
        }

        float prev = 0f; float step;
        for (float keyTime:keyTimes.toArray()) {
            step = keyTime - prev;
            if (step == 0f) { continue; } // we're at the beginning
            minStep = Math.min(minStep, step);
            prev = keyTime;
        }

        //Gdx.app.debug(getClass().getSimpleName(), ""
        //        + mi.afh.nameWithoutExtension() + ":" + a.id + " - "
        //        + " min step: " + minStep
        //        + " OVERALL key times: " + keyTimes.toString()
        //);
    }

    public float floorKeyTime(float time) {
        if (keyTimes.size < 2) { return 0f; }
        for (int i = 1; i < keyTimes.size; i++) {
            if (time < keyTimes.get(i)) { return keyTimes.get(i-1); }
        }
        return 0f;
    }

    public NodeAnimation getNodeAnimation(Node node) {
        NodeAnimation nodeAnim = n2nAnim.get(node);

        if (nodeAnim == null) {
            nodeAnim = new NodeAnimation();
            nodeAnim.node = node;
            a.nodeAnimations.add(nodeAnim);
            checkAnimation();
        }

        return nodeAnim;
    }

    public void addNodeKeyFrame(NodeAnimation nodeAnim, Vector3 trans, Quaternion rot, Vector3 scale) {
        if (nodeAnim == null) { return; }

        if (trans != null && nodeAnim.translation == null) {
            nodeAnim.translation = new Array<>(true, 16, NodeKeyframe.class);
        }
        if (rot != null && nodeAnim.rotation == null) {
            nodeAnim.rotation = new Array<>(true, 16, NodeKeyframe.class);
        }
        if (scale != null && nodeAnim.scaling == null) {
            nodeAnim.scaling = new Array<>(true, 16, NodeKeyframe.class);
        }

        if (trans != null) {
            nodeAnim.translation.add(new NodeKeyframe<>(mi.currKeyTime, trans.cpy()));
        }
        if (rot != null) {
            nodeAnim.rotation.add(new NodeKeyframe<>(mi.currKeyTime, rot.cpy()));
        }
        if (scale != null) {
            nodeAnim.scaling.add(new NodeKeyframe<>(mi.currKeyTime, scale.cpy()));
        }
        checkAnimation();
    }
}