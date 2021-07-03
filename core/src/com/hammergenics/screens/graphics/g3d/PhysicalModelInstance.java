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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.screens.graphics.glutils.HGImmediateModeRenderer20;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicalModelInstance extends HGModelInstance implements Disposable {
    public btRigidBody rigidBody;
    public int rbHashCode = -1;
    public btRigidBody.btRigidBodyConstructionInfo constructionInfo;
    public btCollisionShape shape;
    public ShapesEnum shapeType;
    public Vector3 localInertia = Vector3.Zero.cpy();
    public float mass;

    public PhysicalModelInstance(Model model, float mass, ShapesEnum shape) { this(new HGModel(model), null, mass, shape, (String[])null); }
    public PhysicalModelInstance(Model model, float mass, ShapesEnum shape, String... rootNodeIds) { this(new HGModel(model), null, mass, shape, rootNodeIds); }
    public PhysicalModelInstance(HGModel hgModel, float mass, ShapesEnum shape) { this(hgModel, null, mass, shape, (String[])null); }
    public PhysicalModelInstance(HGModel hgModel, float mass, ShapesEnum shape, String... rootNodeIds) { this(hgModel, null, mass, shape, rootNodeIds); }
    public PhysicalModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shape) { this(hgModel, assetFL, mass, shape, (String[])null); }
    public PhysicalModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shapeType, String... rootNodeIds) {
        super(hgModel, assetFL, rootNodeIds);
        this.mass = mass;
        createRigidBody(shapeType);
    }

    @Override
    public void dispose() {
        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // Every time you construct a bullet class in java, the wrapper will also construct the same class
        // in the native (C++) library. But while in java the garbage collector takes care of memory management and will
        // free an object when you don’t use it anymore, in C++ you’re responsible for freeing the memory yourself.
        // You’re probably already familiar with this cconcept, because the same goes for a texture, model, model batch, shader etc.
        // Because of this, you have to manually dispose the object when you no longer need it.
        if (rigidBody != null) { rigidBody.dispose(); rigidBody = null; }
        if (constructionInfo != null) { constructionInfo.dispose(); constructionInfo = null; }
        if (shape != null) {
            shape.release();
            shape.dispose();
            shape = null;
        }
        super.dispose();
    }

    public enum ShapesEnum {
        BOX, MESH
    }

    public int createRigidBody(ShapesEnum shapeType) {
        if (rigidBody != null) { rigidBody.dispose(); }
        if (constructionInfo != null) { constructionInfo.dispose(); }
        if (shape != null) { shape.dispose(); }

        Vector3 translation = transform.getTranslation(new Vector3());
        Quaternion rotate = transform.getRotation(new Quaternion(), true).nor();
        Vector3 scl = transform.getScale(new Vector3());

        Vector3 dims = getBB(false).getDimensions(new Vector3()).scl(scl);
        Vector3 trn = new Vector3(0, getBB(false).getCenterY(), 0).scl(scl);
        switch (shapeType) {
            case BOX:
                shape = new btBoxShape(dims.scl(0.5f));
                shape.calculateLocalInertia(mass, localInertia);
                break; // scl(0.5f) so we get half-extents
            case MESH:
                shape = btBvhTriangleMeshShape.obtain(hgModel.obj.meshParts);
                // libgdx\extensions\gdx-bullet\jni\src\bullet\bulletcollision\collisionshapes\bttrianglemeshshape.cpp:184
                // btTriangleMeshShape::calculateLocalInertia
                //	 //moving concave objects not supported
                //	 btAssert(0);
                break;
            default:
                Gdx.app.error(getClass().getSimpleName(), "ERROR: unknown shape type: " + shapeType.name());
                return -1;
        }
        constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);

        rigidBody = new btRigidBody(constructionInfo);
        rbHashCode = rigidBody.hashCode();

        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // While this is easy to work with, you should keep in mind that the transform -as far as bullet is concerned-
        // only contains a position and rotation. Any other transformation, like for example scaling, is not supported.
        // In practice this means that you should never apply scaling directly to objects when using the bullet wrapper.
        // There are other ways to scale objects, but in general I would recommend to try to avoid scaling.
        rigidBody.setWorldTransform(new Matrix4()
                .setToTranslation(translation)
                .rotate(rotate.nor())
                .trn(trn)
        );
        rigidBody.setUserValue(rbHashCode);

        // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // we also inform Bullet that we want to receive collision events for this object by adding the CF_CUSTOM_MATERIAL_CALLBACK flag.
        // This flag is required for the onContactAdded method to be called.
        rigidBody.setCollisionFlags(
                rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        return rbHashCode;
    }

    public void syncWithRBTransform() {
        Vector3 translate = new Vector3();
        Vector3 scl = new Vector3();

        translate.set(0, -getBB(false).getCenterY(), 0);
        transform.getScale(scl);
        rigidBody.getWorldTransform(transform);
        translate(translate.scl(scl));
        scale(scl.x, scl.y, scl.z);
    }

    public void addRBShapeToRenderer(HGImmediateModeRenderer20 imr) {
        Vector3 scl = transform.getScale(new Vector3());
        Vector3 dims = getBB(false).getDimensions(new Vector3()).scl(scl);
        Matrix4 tmpM4 = new Matrix4();
        rigidBody.getWorldTransform(tmpM4);
        tmpM4.scale(dims.x, dims.y, dims.z);
        imr.box(tmpM4, Color.MAROON);
    }
}
