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

package com.hammergenics.screens.physics.bullet.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.hammergenics.HGEngine;
import com.hammergenics.screens.graphics.g3d.PhysicalModelInstance;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGContactListener extends ContactListener {
    // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // Because it’s not possible in Java to use global callback methods, the wrapper adds the ContactListener class
    // to take care of that. This is also the reason that we don’t have to inform bullet to use our ContactListener,
    // the wrapper takes care of that when you construct the ContactListener.

    // callback events present:
    // * onContactAdded
    //   see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    //   The onContactAdded method is called whenever a contact point is added to a manifold.
    //   As we’ve seen earlier, as soon as a manifold has one or more contact points, there’s a collision.
    //   So basically this method is called when a collision occurs between two objects.
    // * onContactProcessed
    // * onContactStarted
    // * onContactEnded
    //
    // Exception in thread "LWJGL Application" com.badlogic.gdx.utils.GdxRuntimeException:
    // Only one method per callback event can be overridden.
    // at com.badlogic.gdx.physics.bullet.collision.ContactListener.<init>(ContactListener.java:79)
    // at com.hammergenics.screens.physics.bullet.collision.HGContactListener.<init>(HGContactListener.java:31)
    //
    // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    // the ContactListener has multiple signatures of the same method which you can override.
    // You can only override one of those, because the wrapper will only call one method for an event.
    // Looking at our ContactListener, we never use the btManifoldPoint.
    // So if we use the signature which doesn’t include that argument, then the wrapper doesn’t have to create it.

    public HGEngine eng;

    public HGContactListener(HGEngine eng) { this.eng = eng; }

    //
    // onContactAdded
    //

    @Override
    public boolean onContactAdded(btManifoldPoint cp,
                                  btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
                                  btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
        int hc0 = colObj0Wrap.getCollisionObject().getUserValue();
        int hc1 = colObj1Wrap.getCollisionObject().getUserValue();

        btRigidBody rb0 = eng.hc2rb.get(hc0);
        btRigidBody rb1 = eng.hc2rb.get(hc1);
        PhysicalModelInstance mi0 = eng.rb2mi.get(rb0);
        PhysicalModelInstance mi1 = eng.rb2mi.get(rb1);

//        Gdx.app.debug(getTag(), ""
//                + " hc0: " + Integer.toHexString(hc0)
//                + " hc1: " + Integer.toHexString(hc1));
//        Gdx.app.debug(getTag(), ""
//                + " rb0: " + Integer.toHexString(rb0.hashCode())
//                + " rb1: " + Integer.toHexString(rb1.hashCode()));
        Gdx.app.debug(getTag(), ""
                + " mi0: " + mi0.nodes.get(0).id
                + " mi1: " + mi1.nodes.get(0).id);
//        Gdx.app.debug(getTag(), ""  + " rb0 world transform:\n" + rb0.getWorldTransform());
//        Gdx.app.debug(getTag(), ""  + " rb1 world transform:\n" + rb1.getWorldTransform());

        //return super.onContactAdded(cp, colObj0Wrap, partId0, index0, colObj1Wrap, partId1, index1);
        return true;
    }

//    @Override
//    public boolean onContactAdded(btManifoldPoint cp,
//                                  btCollisionObject colObj0, int partId0, int index0,
//                                  btCollisionObject colObj1, int partId1, int index1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(cp, colObj0, partId0, index0, colObj1, partId1, index1);
//    }
//
//    @Override
//    public boolean onContactAdded(btManifoldPoint cp,
//                                  int userValue0, int partId0, int index0,
//                                  int userValue1, int partId1, int index1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(cp, userValue0, partId0, index0, userValue1, partId1, index1);
//    }
//
//    @Override
//    public boolean onContactAdded(btManifoldPoint cp,
//                                  btCollisionObjectWrapper colObj0Wrap, int partId0, int index0, boolean match0,
//                                  btCollisionObjectWrapper colObj1Wrap, int partId1, int index1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(cp, colObj0Wrap, partId0, index0, match0, colObj1Wrap, partId1, index1, match1);
//    }
//
//    @Override
//    public boolean onContactAdded(btManifoldPoint cp,
//                                  btCollisionObject colObj0, int partId0, int index0, boolean match0,
//                                  btCollisionObject colObj1, int partId1, int index1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(cp, colObj0, partId0, index0, match0, colObj1, partId1, index1, match1);
//    }
//
//    @Override
//    public boolean onContactAdded(btManifoldPoint cp,
//                                  int userValue0, int partId0, int index0, boolean match0,
//                                  int userValue1, int partId1, int index1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(cp, userValue0, partId0, index0, match0, userValue1, partId1, index1, match1);
//    }
//
//    @Override
//    public boolean onContactAdded(btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
//                                  btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(colObj0Wrap, partId0, index0, colObj1Wrap, partId1, index1);
//    }
//
//    @Override
//    public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0,
//                                  btCollisionObject colObj1, int partId1, int index1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(colObj0, partId0, index0, colObj1, partId1, index1);
//    }
//
//    @Override
//    public boolean onContactAdded(int userValue0, int partId0, int index0,
//                                  int userValue1, int partId1, int index1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(userValue0, partId0, index0, userValue1, partId1, index1);
//    }
//
//    @Override
//    public boolean onContactAdded(btCollisionObjectWrapper colObj0Wrap, int partId0, int index0, boolean match0,
//                                  btCollisionObjectWrapper colObj1Wrap, int partId1, int index1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(colObj0Wrap, partId0, index0, match0, colObj1Wrap, partId1, index1, match1);
//    }
//
//    @Override
//    public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, boolean match0,
//                                  btCollisionObject colObj1, int partId1, int index1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(colObj0, partId0, index0, match0, colObj1, partId1, index1, match1);
//    }
//
//    @Override
//    public boolean onContactAdded(int userValue0, int partId0, int index0, boolean match0,
//                                  int userValue1, int partId1, int index1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        return super.onContactAdded(userValue0, partId0, index0, match0, userValue1, partId1, index1, match1);
//    }

    //
    // onContactProcessed
    //

    @Override
    public void onContactProcessed(btManifoldPoint cp, btCollisionObject colObj0, btCollisionObject colObj1) {
        //Gdx.app.debug(getTag(), "");
        //super.onContactProcessed(cp, colObj0, colObj1);
    }

//    @Override
//    public void onContactProcessed(btManifoldPoint cp, int userValue0, int userValue1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(cp, userValue0, userValue1);
//    }
//
//    @Override
//    public void onContactProcessed(btManifoldPoint cp,
//                                   btCollisionObject colObj0, boolean match0,
//                                   btCollisionObject colObj1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(cp, colObj0, match0, colObj1, match1);
//    }
//
//    @Override
//    public void onContactProcessed(btManifoldPoint cp,
//                                   int userValue0, boolean match0,
//                                   int userValue1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(cp, userValue0, match0, userValue1, match1);
//    }
//
//    @Override
//    public void onContactProcessed(btCollisionObject colObj0, btCollisionObject colObj1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(colObj0, colObj1);
//    }
//
//    @Override
//    public void onContactProcessed(int userValue0, int userValue1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(userValue0, userValue1);
//    }
//
//    @Override
//    public void onContactProcessed(btCollisionObject colObj0, boolean match0,
//                                   btCollisionObject colObj1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(colObj0, match0, colObj1, match1);
//    }
//
//    @Override
//    public void onContactProcessed(int userValue0, boolean match0,
//                                   int userValue1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactProcessed(userValue0, match0, userValue1, match1);
//    }

    //
    // onContactStarted
    //

    @Override
    public void onContactStarted(btPersistentManifold manifold) {
        //Gdx.app.debug(getTag(), "");
        //super.onContactStarted(manifold);
    }

//    @Override
//    public void onContactStarted(btCollisionObject colObj0, btCollisionObject colObj1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactStarted(colObj0, colObj1);
//    }
//
//    @Override
//    public void onContactStarted(int userValue0, int userValue1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactStarted(userValue0, userValue1);
//    }
//
//    @Override
//    public void onContactStarted(btPersistentManifold manifold, boolean match0, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactStarted(manifold, match0, match1);
//    }
//
//    @Override
//    public void onContactStarted(btCollisionObject colObj0, boolean match0,
//                                 btCollisionObject colObj1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactStarted(colObj0, match0, colObj1, match1);
//    }
//
//    @Override
//    public void onContactStarted(int userValue0, boolean match0,
//                                 int userValue1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactStarted(userValue0, match0, userValue1, match1);
//    }

    //
    // onContactEnded
    //

    @Override
    public void onContactEnded(btPersistentManifold manifold) {
        //Gdx.app.debug(getTag(), "");
        //super.onContactEnded(manifold);
    }

//    @Override
//    public void onContactEnded(btCollisionObject colObj0, btCollisionObject colObj1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactEnded(colObj0, colObj1);
//    }
//
//    @Override
//    public void onContactEnded(int userValue0, int userValue1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactEnded(userValue0, userValue1);
//    }
//
//    @Override
//    public void onContactEnded(btPersistentManifold manifold, boolean match0, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactEnded(manifold, match0, match1);
//    }
//
//    @Override
//    public void onContactEnded(btCollisionObject colObj0, boolean match0,
//                               btCollisionObject colObj1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactEnded(colObj0, match0, colObj1, match1);
//    }
//
//    @Override
//    public void onContactEnded(int userValue0, boolean match0,
//                               int userValue1, boolean match1) {
//        Gdx.app.debug(getTag(), "");
//        super.onContactEnded(userValue0, match0, userValue1, match1);
//    }

    private String getTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        return stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
    }
}