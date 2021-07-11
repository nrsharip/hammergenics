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

package com.hammergenics.physics.bullet.dynamics;


import com.badlogic.gdx.Gdx;

// https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/Dynamics/btDynamicsWorld.h#L30
// enum btDynamicsWorldType
// {
//     BT_SIMPLE_DYNAMICS_WORLD = 1,
//     BT_DISCRETE_DYNAMICS_WORLD = 2,
//     BT_CONTINUOUS_DYNAMICS_WORLD = 3,
//     BT_SOFT_RIGID_DYNAMICS_WORLD = 4,
//     BT_GPU_DYNAMICS_WORLD = 5,
//     BT_SOFT_MULTIBODY_DYNAMICS_WORLD = 6,
//     BT_DEFORMABLE_MULTIBODY_DYNAMICS_WORLD = 7
// };
// https://github.com/libgdx/libgdx/blob/024282e47e9b5d8ec25373d3e1e5ddfe55122596/extensions/gdx-bullet/jni/src/bullet/BulletDynamics/Dynamics/btDynamicsWorld.h#L31
// enum btDynamicsWorldType
// {
//     BT_SIMPLE_DYNAMICS_WORLD=1,
//     BT_DISCRETE_DYNAMICS_WORLD=2,
//     BT_CONTINUOUS_DYNAMICS_WORLD=3,
//     BT_SOFT_RIGID_DYNAMICS_WORLD=4,
//     BT_GPU_DYNAMICS_WORLD=5,
//     BT_SOFT_MULTIBODY_DYNAMICS_WORLD=6
// };
public enum btDynamicsWorldTypesEnum {
    BT_SIMPLE_DYNAMICS_WORLD(1),
    BT_DISCRETE_DYNAMICS_WORLD(2),
    BT_CONTINUOUS_DYNAMICS_WORLD(3),
    BT_SOFT_RIGID_DYNAMICS_WORLD(4),
    BT_GPU_DYNAMICS_WORLD(5),
    BT_SOFT_MULTIBODY_DYNAMICS_WORLD(6);

    int type;
    btDynamicsWorldTypesEnum(int type) { this.type = type; }

    public static btDynamicsWorldTypesEnum findByType(int type) {
        for (btDynamicsWorldTypesEnum dw: btDynamicsWorldTypesEnum.values()) {
            if (dw.type == type) { return dw; }
        }
        Gdx.app.error("bullet", "ERROR: undefined dynamics world type " + type);
        return null;
    }

    @Override
    public String toString() { return this.name().replace("BT_", "").replace("_", " "); }
}