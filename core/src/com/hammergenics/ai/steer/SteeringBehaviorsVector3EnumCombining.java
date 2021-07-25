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

package com.hammergenics.ai.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.math.Vector3;
import com.hammergenics.HGEngine;
import com.hammergenics.core.graphics.g3d.PhysicalModelInstance.ShapesEnum;
import com.hammergenics.core.graphics.g3d.SteerableModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum SteeringBehaviorsVector3EnumCombining {
    // see https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
    // COMBINING STEERING BEHAVIORS: https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#combining-steering-behaviors
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#blended-steering
    // This combination behavior simply sums up all the behaviors,
    // applies their weights, and truncates the result before returning.
    BLENDED_STEERING(BlendedSteering.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new BlendedSteering<>(stubOwner); }
            return instance;
        }
    },
    // https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#priority-steering
    PRIORITY_STEERING(PrioritySteering.class) {
        @Override
        public SteeringBehavior<Vector3> getInstance() {
            // single threaded processing is assumed: returning a singleton
            if (instance == null) { instance = new PrioritySteering<>(stubOwner); }
            return instance;
        }
    };

    // SteeringBehavior (and it's descendants) seem to be the "stateless" algorithm providers
    // meaning they don't store the actual state of a particular steering of a particular owner etc.
    // That is why it is safe to initialize the SteeringBehavior instances with the stub components
    // and replace them on a particular use.
    public static final Steerable<Vector3> stubOwner;

    static {
        stubOwner = new SteerableModelInstance(HGEngine.boxHgModel, 0f, ShapesEnum.BOX);
    }

    public Class<?> clazz;
    protected SteeringBehavior<Vector3> instance = null;

    SteeringBehaviorsVector3EnumCombining(Class<?> clazz) {
        this.clazz = clazz;
    }

    // SteeringBehavior (and it's descendants) seem to be the "stateless" algorithm providers
    // meaning they don't store the actual state of a particular steering of a particular owner etc.
    // That is why one instance could be shared among different multiple calls for acceleration
    // calculations. In case of multithreaded processing pooling should be considered as the way
    // to acquire instances of SteeringBehavior, for now single threaded processing is assumed
    // and getInstance() is expected to return a singleton.
    public abstract SteeringBehavior<Vector3> getInstance();

    // Init Methods
    public static void initBlendedSteering() { }
    public static void initPrioritySteering() { }
}