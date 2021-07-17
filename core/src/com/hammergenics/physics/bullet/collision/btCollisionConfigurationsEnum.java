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

package com.hammergenics.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyRigidBodyCollisionConfiguration;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public enum btCollisionConfigurationsEnum implements Disposable {
    // btDefaultCollisionConfiguration
    BT_DEFAULT_COLLISION_CONFIGURATION {
        @Override
        public btCollisionConfiguration getInstance() {
            return getInstance(new btDefaultCollisionConfiguration());
        }
    },
    // btSoftBodyRigidBodyCollisionConfiguration
    BT_SOFTBODY_RIGIDBODY_COLLISION_CONFIGURATION {
        @Override
        public btCollisionConfiguration getInstance() {
            return getInstance(new btSoftBodyRigidBodyCollisionConfiguration());
        }
    };

    public Array<btCollisionConfiguration> instances = new Array<>(btCollisionConfiguration.class);

    btCollisionConfigurationsEnum() {
    }

    @Override
    public void dispose() {
        for (btCollisionConfiguration cc: instances) { cc.dispose(); }
    }

    public static void disposeAll() {
        for (btCollisionConfigurationsEnum cc: btCollisionConfigurationsEnum.values()) { cc.dispose(); }
    }

    public abstract btCollisionConfiguration getInstance();

    protected btCollisionConfiguration getInstance(btCollisionConfiguration tmp) {
        instances.add(tmp);
        return tmp;
    }
}