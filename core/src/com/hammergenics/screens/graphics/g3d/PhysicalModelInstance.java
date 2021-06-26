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

package com.hammergenics.screens.graphics.g3d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicalModelInstance extends DebugModelInstance implements Disposable {
    public btRigidBody rigidBody;
    public int rbHashCode = -1;
    public btRigidBody.btRigidBodyConstructionInfo constructionInfo;
    public btCollisionShape shape;
    public Vector3 localInertia = Vector3.Zero.cpy();
    public float mass;

    public PhysicalModelInstance(Model model, float mass) { this(new HGModel(model), null, mass, (String[])null); }
    public PhysicalModelInstance(Model model, float mass, String... rootNodeIds) { this(new HGModel(model), null, mass, rootNodeIds); }
    public PhysicalModelInstance(HGModel hgModel, float mass) { this(hgModel, null, mass, (String[])null); }
    public PhysicalModelInstance(HGModel hgModel, float mass, String... rootNodeIds) { this(hgModel, null, mass, rootNodeIds); }
    public PhysicalModelInstance(HGModel hgModel, FileHandle assetFL, float mass) { this(hgModel, assetFL, mass, (String[])null); }
    public PhysicalModelInstance(HGModel hgModel, FileHandle assetFL, float mass, String... rootNodeIds) {
        super(hgModel, assetFL, rootNodeIds);
        this.mass = mass;
        createRigidBody();
    }

    @Override
    public void dispose() {
        super.dispose();

        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // Every time you construct a bullet class in java, the wrapper will also construct the same class
        // in the native (C++) library. But while in java the garbage collector takes care of memory management and will
        // free an object when you don’t use it anymore, in C++ you’re responsible for freeing the memory yourself.
        // You’re probably already familiar with this cconcept, because the same goes for a texture, model, model batch, shader etc.
        // Because of this, you have to manually dispose the object when you no longer need it.
        if (rigidBody != null) { rigidBody.dispose(); }
        if (constructionInfo != null) { constructionInfo.dispose(); }
        if (shape != null) { shape.dispose(); }
    }

    public int createRigidBody() {
        if (rigidBody != null) { rigidBody.dispose(); }
        if (constructionInfo != null) { constructionInfo.dispose(); }
        if (shape != null) { shape.dispose(); }

        BoundingBox bbOrig = getBB(false);
        BoundingBox bbTran = getBB(true);
        //Gdx.app.debug("rb", "" +
        //        "id: " + nodes.get(0).id + " bbOrig: " + bbOrig + " bbTran: " + bbTran);
        //Gdx.app.debug("rb", "" +
        //        "id: " + nodes.get(0).id
        //        + " bbO.center: " + bbOrig.getCenter(new Vector3())
        //        + " bbT.center: " + bbTran.getCenter(new Vector3()));
        //Gdx.app.debug("rb", "" +
        //        "id: " + nodes.get(0).id
        //        + " bbO.dims: " + bbOrig.getDimensions(new Vector3())
        //        + " bbT.dims: " + bbTran.getDimensions(new Vector3()));
        Vector3 dimensions = bbTran.getDimensions(new Vector3());
        Vector3 center = bbTran.getCenter(new Vector3());
        shape = new btBoxShape(dimensions.scl(0.5f)); // scl(0.5f) so we get half-extents
        //Gdx.app.debug("mi", "id: " + nodes.get(0).id + " dimensions: " + dimensions);

        shape.calculateLocalInertia(mass, localInertia);
        constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);

        rigidBody = new btRigidBody(constructionInfo);
        rbHashCode = rigidBody.hashCode();

        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // While this is easy to work with, you should keep in mind that the transform -as far as bullet is concerned-
        // only contains a position and rotation. Any other transformation, like for example scaling, is not supported.
        // In practice this means that you should never apply scaling directly to objects when using the bullet wrapper.
        // There are other ways to scale objects, but in general I would recommend to try to avoid scaling.
        rigidBody.setWorldTransform(new Matrix4().translate(center));
        //Gdx.app.debug("rb", "" +
        //        "id: " + nodes.get(0).id
        //        + " rb.worldTransform:\n" + rigidBody.getWorldTransform());
        rigidBody.setUserValue(rbHashCode);

        // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // we also inform Bullet that we want to receive collision events for this object by adding the CF_CUSTOM_MATERIAL_CALLBACK flag.
        // This flag is required for the onContactAdded method to be called.
        rigidBody.setCollisionFlags(
                rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

//        public final static class CollisionFlags {
//            CF_STATIC_OBJECT = 1;
//            CF_KINEMATIC_OBJECT = 2;
//            CF_NO_CONTACT_RESPONSE = 4;
//            CF_CUSTOM_MATERIAL_CALLBACK = 8;
//            CF_CHARACTER_OBJECT = 16;
//            CF_DISABLE_VISUALIZE_OBJECT = 32;
//            CF_DISABLE_SPU_COLLISION_PROCESSING = 64;
//            CF_HAS_CONTACT_STIFFNESS_DAMPING = 128;
//            CF_HAS_CUSTOM_DEBUG_RENDERING_COLOR = 256;
//            CF_HAS_FRICTION_ANCHOR = 512;
//            CF_HAS_COLLISION_SOUND_TRIGGER = 1024;
//        }
//
//        public final static class CollisionObjectTypes {
//            CO_COLLISION_OBJECT = 1;
//            CO_RIGID_BODY = 2;
//            CO_GHOST_OBJECT = 4;
//            CO_SOFT_BODY = 8;
//            CO_HF_FLUID = 16;
//            CO_USER_TYPE = 32;
//            CO_FEATHERSTONE_LINK = 64;
//        }
//
//        public final static class AnisotropicFrictionFlags {
//            CF_ANISOTROPIC_FRICTION_DISABLED = 0;
//            CF_ANISOTROPIC_FRICTION = 1;
//            CF_ANISOTROPIC_ROLLING_FRICTION = 2;
//        }
        return rbHashCode;
    }

    public void syncWithRBTransform() {
        Vector3 translate = new Vector3();
        Vector3 scl = new Vector3();

        translate.set(0, -getBB(false).getCenterY(), 0);
        transform.getScale(scl);
        rigidBody.getWorldTransform(transform);
        transform.translate(translate.scl(scl));
        transform.scale(scl.x, scl.y, scl.z);
        bbHgModelInstanceReset();
        bbCornersReset();
    }
}
