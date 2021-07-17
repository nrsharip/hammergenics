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


import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSimpleDynamicsWorld;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import com.badlogic.gdx.physics.bullet.softbody.btSoftMultiBodyDynamicsWorld;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.physics.bullet.collision.btBroadphasesEnum;
import com.hammergenics.physics.bullet.collision.btCollisionConfigurationsEnum;

import static com.hammergenics.physics.bullet.collision.btBroadphasesEnum.BT_DBVT_BROADPHASE;
import static com.hammergenics.physics.bullet.collision.btCollisionConfigurationsEnum.BT_DEFAULT_COLLISION_CONFIGURATION;
import static com.hammergenics.physics.bullet.collision.btCollisionConfigurationsEnum.BT_SOFTBODY_RIGIDBODY_COLLISION_CONFIGURATION;
import static com.hammergenics.physics.bullet.dynamics.btConstraintSolversEnum.BT_SEQUENTIAL_IMPULSE_SOLVER;

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
public enum btDynamicsWorldTypesEnum implements Disposable {
    BT_SIMPLE_DYNAMICS_WORLD(1) {
        @Override
        public btDynamicsWorld createBtDynamicsWorld() {
            collisionConfig = BT_DEFAULT_COLLISION_CONFIGURATION.getInstance();
            // CustomCollisionDispatcher
            // btCollisionDispatcher
            // btCollisionDispatcherMt
            dispatcher = new btCollisionDispatcher(collisionConfig);

            // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
            // For the broad phase I’ve chosen the btDbvtBroadphase implementation,
            // which is a Dynamic Bounding Volume Tree implementation.
            // In most scenario’s this implementation should suffice.
            broadPhase = BT_DBVT_BROADPHASE.getInstance();
            return new btSimpleDynamicsWorld(dispatcher, broadPhase, null, collisionConfig);
        }

        @Override
        public void resetBtDynamicsWorld(float scale) { }
    },
    BT_DISCRETE_DYNAMICS_WORLD(2) {
        @Override
        public btDynamicsWorld createBtDynamicsWorld() {
            collisionConfig = BT_DEFAULT_COLLISION_CONFIGURATION.getInstance();
            // CustomCollisionDispatcher
            // btCollisionDispatcher
            // btCollisionDispatcherMt
            dispatcher = new btCollisionDispatcher(collisionConfig);

            // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
            // For the broad phase I’ve chosen the btDbvtBroadphase implementation,
            // which is a Dynamic Bounding Volume Tree implementation.
            // In most scenario’s this implementation should suffice.
            broadPhase = BT_DBVT_BROADPHASE.getInstance();
            return new btDiscreteDynamicsWorld(dispatcher, broadPhase, null, collisionConfig);
        }

        @Override
        public void resetBtDynamicsWorld(float scale) {
            dynamicsWorld.setGravity(Vector3.Y.cpy().scl(-10f * scale));
            dynamicsWorld.setConstraintSolver(BT_SEQUENTIAL_IMPULSE_SOLVER.getInstance());
        }
    },
    BT_CONTINUOUS_DYNAMICS_WORLD(3) {
        @Override
        public btDynamicsWorld createBtDynamicsWorld() {
            return null;
        }

        @Override
        public void resetBtDynamicsWorld(float scale) { }
    },
    BT_SOFT_RIGID_DYNAMICS_WORLD(4) {
        @Override
        public btDynamicsWorld createBtDynamicsWorld() {
            collisionConfig = BT_SOFTBODY_RIGIDBODY_COLLISION_CONFIGURATION.getInstance();
            // CustomCollisionDispatcher
            // btCollisionDispatcher
            // btCollisionDispatcherMt
            dispatcher = new btCollisionDispatcher(collisionConfig);

            // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
            // For the broad phase I’ve chosen the btDbvtBroadphase implementation,
            // which is a Dynamic Bounding Volume Tree implementation.
            // In most scenario’s this implementation should suffice.
            broadPhase = BT_DBVT_BROADPHASE.getInstance();
            return new btSoftRigidDynamicsWorld(dispatcher, broadPhase, null, collisionConfig);
        }

        @Override
        public void resetBtDynamicsWorld(float scale) { }
    },
    BT_GPU_DYNAMICS_WORLD(5) {
        @Override
        public btDynamicsWorld createBtDynamicsWorld() { return null; }

        @Override
        public void resetBtDynamicsWorld(float scale) { }
    },
    BT_SOFT_MULTIBODY_DYNAMICS_WORLD(6) {
        @Override
        public btDynamicsWorld createBtDynamicsWorld() {
            collisionConfig = BT_SOFTBODY_RIGIDBODY_COLLISION_CONFIGURATION.getInstance();
            // CustomCollisionDispatcher
            // btCollisionDispatcher
            // btCollisionDispatcherMt
            dispatcher = new btCollisionDispatcher(collisionConfig);

            // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
            // For the broad phase I’ve chosen the btDbvtBroadphase implementation,
            // which is a Dynamic Bounding Volume Tree implementation.
            // In most scenario’s this implementation should suffice.
            broadPhase = BT_DBVT_BROADPHASE.getInstance();
            return new btSoftMultiBodyDynamicsWorld(dispatcher, broadPhase, null, collisionConfig);
        }

        @Override
        public void resetBtDynamicsWorld(float scale) { }
    };

    int type;
    // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // https://pybullet.org/Bullet/phpBB3/viewtopic.php?t=5449#p19521
    // it’s generally better to use bits which aren’t used for anything else.
    public final static short FLAG_GROUND = 1<<8;
    public final static short FLAG_OBJECT = 1<<9;
    public final static short FLAG_ALL = -1;

    public btDynamicsWorld dynamicsWorld;
    // BROAD PHASE:
    // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Ideally we’d first check if the two objects are near each other, for example using a bounding box or bounding sphere.
    // And only if they are near each other, we’d use the more accurate specialized collision algorithm.

    // The first phase, where we find collision objects that are near each other, is called the broad phase.
    // It’s therefore crucial that the broad phase is highly optimized. Bullet does this by caching the collision information,
    // so it doesn’t have to recalculate it every time. There are several implementations you can choose from,
    // but in practice this is done in the form a tree. I’ll not go into detail about this, but if you want to know more
    // about it, you can search for “axis aligned bounding box tree” or in short “AABB tree”.
    public btBroadphaseInterface broadPhase;
    // The second phase, where a more accurate specialized collision algorithm is used, is called the near phase.

    // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Before we can start the actual collision detection we need a few helper classes.
    public btCollisionConfiguration collisionConfig;
    public btDispatcher dispatcher;

    // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Because it’s not possible in Java to use global callback methods, the wrapper adds the ContactListener class
    // to take care of that. This is also the reason that we don’t have to inform bullet to use our ContactListener,
    // the wrapper takes care of that when you construct the ContactListener.
    public ContactListener contactListener;

    btDynamicsWorldTypesEnum(int type) {
        this.type = type;

        initBullet();

        dynamicsWorld = createBtDynamicsWorld();
    }

    public static btDynamicsWorldTypesEnum selected = null;
    public static void setSelected(btDynamicsWorldTypesEnum selected) {
        if (btDynamicsWorldTypesEnum.selected == null) {
            btDynamicsWorldTypesEnum.selected = selected;
        }
    }

    public abstract btDynamicsWorld createBtDynamicsWorld();
    public abstract void resetBtDynamicsWorld(float scale);
    public static void resetAllBtDynamicsWorlds(float scale) {
        for (btDynamicsWorldTypesEnum dwt: btDynamicsWorldTypesEnum.values()) {
            dwt.resetBtDynamicsWorld(scale);
        }
    }

    public static btDynamicsWorldTypesEnum findByType(int type) {
        for (btDynamicsWorldTypesEnum dwt: btDynamicsWorldTypesEnum.values()) {
            if (dwt.type == type) { return dwt; }
        }
        Gdx.app.error("bullet", "ERROR: undefined dynamics world type " + type);
        return null;
    }

    @Override
    public String toString() { return this.name().replace("BT_", "").replace("_", " "); }

    @Override
    public void dispose() {
        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // Every time you construct a bullet class in java, the wrapper will also construct the same class
        // in the native (C++) library. But while in java the garbage collector takes care of memory management and will
        // free an object when you don’t use it anymore, in C++ you’re responsible for freeing the memory yourself.
        // You’re probably already familiar with this cconcept, because the same goes for a texture, model, model batch, shader etc.
        // Because of this, you have to manually dispose the object when you no longer need it.
        if (dynamicsWorld != null) { dynamicsWorld.dispose(); }
        //if (collisionConfig != null) { collisionConfig.dispose(); }
        if (dispatcher != null) { dispatcher.dispose(); }
        //if (broadPhase != null) { broadPhase.dispose(); }
    }

    public static void disposeAll() {
        for (btDynamicsWorldTypesEnum dwt: btDynamicsWorldTypesEnum.values()) { dwt.dispose(); }

        btConstraintSolversEnum.disposeAll();
        btMLCPSolversEnum.disposeAll();
        btCollisionConfigurationsEnum.disposeAll();
        btBroadphasesEnum.disposeAll();
        //contactListener.dispose();
    }

    // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#loading-the-correct-dll
    // Set this to the path of the lib to use it on desktop instead of the default lib.
    private final static String customDesktopLib = "E:\\...\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";
    private final static boolean debugBullet = false;
    private static boolean bulletLoaded = false;
    public static void initBullet() {
        if (bulletLoaded) { return; }
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#getting-the-sources
        //   sources: libgdx-024282e47e9b5d8ec25373d3e1e5ddfe55122596.zip:
        //      https://github.com/libgdx/libgdx/releases/tag/gdx-parent-1.10.0
        //      https://github.com/libgdx/libgdx/tree/024282e47e9b5d8ec25373d3e1e5ddfe55122596
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#getting-the-compileride
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#building-the-debug-dll
        //
        //   ISSUE:
        //      1>...\Platforms\Win32\PlatformToolsets\v141\Toolset.targets(34,5):
        //      error MSB8036: The Windows SDK version 8.1 was not found.
        //      Install the required version of Windows SDK or change the SDK version in the project property pages or by right-clicking the solution and selecting "Retarget solution".
        //      SOLUTION: right-click VS solution -> Retarget Projects -> select the SDK
        //   ISSUE:
        //      1>------ Build started: Project: gdxBullet, Configuration: Debug x64 ------
        //      1>softbody_wrap.cpp
        //      1>...\gdx-bullet\jni\swig-src\softbody\softbody_wrap.cpp(179): fatal error C1083: Cannot open include file: 'jni.h': No such file or directory
        //      ...
        //      SOLUTION: right-click VS solution -> Properties -> Configuration: Debug, Platform: All Platforms -> C/C++ -> General -> Additional Include Directories
        //                add the following directory: <path to JDK>/include
        //   ISSUE:
        //      1>------ Build started: Project: gdxBullet, Configuration: Debug Win32 ------
        //      1>softbody_wrap.cpp
        //      1>...\include\jni.h(45): fatal error C1083: Cannot open include file: 'jni_md.h': No such file or directory
        //      ...
        //      SOLUTION: right-click VS solution -> Properties -> Configuration: Debug, Platform: All Platforms -> C/C++ -> General -> Additional Include Directories
        //                add the following directory: <path to JDK>/include/win32

        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#loading-the-correct-dll
        // Need to initialize bullet before using it.
        if (Gdx.app.getType() == Application.ApplicationType.Desktop && debugBullet) {
            System.load(customDesktopLib);
        } else {
            Bullet.init();
        }
        Gdx.app.log("bullet", "version: " + LinearMath.btGetVersion() + " debug: " + debugBullet);
        // Release (gradle: libgdx-1.10.0):
        // [Bullet] Version = 287
        // Debug (https://github.com/libgdx/libgdx/tree/024282e47e9b5d8ec25373d3e1e5ddfe55122596):
        // [Bullet] Version = 287
        // Bullet Github: https://github.com/bulletphysics/bullet3/blob/master/src/LinearMath/btScalar.h#L28
        // #define BT_BULLET_VERSION 317
        // see: https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Debugging#debugging

        //contactListener = new HGContactListener(this);
        bulletLoaded = true;
    }
}