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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.bt32BitAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum btBroadphasesEnum implements Disposable {
    // bt32BitAxisSweep3 extends btAxisSweep3InternalInt
    BT_32BIT_AXIS_SWEEP_3 {
        @Override
        public btBroadphaseInterface getInstance() {
            // https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/bullet/SoftBodyTest.java#L61
            Vector3 worldAabbMin = new Vector3(-1000, -1000, -1000);
            Vector3 worldAabbMax = new Vector3(1000, 1000, 1000);
            int maxHandles = 1024;
            return getInstance(new bt32BitAxisSweep3(worldAabbMin, worldAabbMax, maxHandles));
        }
    },
    // btAxisSweep3 extends btAxisSweep3InternalShort
    BT_AXIS_SWEEP_3 {
        @Override
        public btBroadphaseInterface getInstance() {
            // https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/bullet/SoftBodyTest.java#L61
            Vector3 worldAabbMin = new Vector3(-1000, -1000, -1000);
            Vector3 worldAabbMax = new Vector3(1000, 1000, 1000);
            int maxHandles = 1024;
            return getInstance(new btAxisSweep3(worldAabbMin, worldAabbMax, maxHandles));
        }
    },
    // btAxisSweep3InternalInt
    BT_AXIS_SWEEP_3_INTERNAL_INT {
        @Override
        public btBroadphaseInterface getInstance() {
            return null; // new btAxisSweep3InternalInt()
        }
    },
    // btAxisSweep3InternalShort
    BT_AXIS_SWEEP_3_INTERNAL_SHORT {
        @Override
        public btBroadphaseInterface getInstance() {
            return null; // new btAxisSweep3InternalShort()
        }
    },
    // btDbvtBroadphase
    BT_DBVT_BROADPHASE {
        @Override
        public btBroadphaseInterface getInstance() {
            return getInstance(new btDbvtBroadphase());
        }
    },
    // btSimpleBroadphase
    BT_SIMPLE_BROADPHASE {
        @Override
        public btBroadphaseInterface getInstance() {
            return null;
        }
    };

    public Array<btBroadphaseInterface> instances = new Array<>(btBroadphaseInterface.class);

    btBroadphasesEnum() {
    }

    @Override
    public void dispose() {
        for (btBroadphaseInterface cc: instances) { cc.dispose(); }
    }

    public static void disposeAll() {
        for (btCollisionConfigurationsEnum cc: btCollisionConfigurationsEnum.values()) { cc.dispose(); }
    }

    public abstract btBroadphaseInterface getInstance();

    protected btBroadphaseInterface getInstance(btBroadphaseInterface tmp) {
        instances.add(tmp);
        return tmp;
    }
}