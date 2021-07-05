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
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Disposable;
import com.hammergenics.screens.graphics.glutils.HGImmediateModeRenderer20;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class PhysicalModelInstance extends HGModelInstance implements Disposable {
    public btRigidBody rigidBody;
    public btRigidBody.btRigidBodyConstructionInfo rigidBodyConstructionInfo;
    public btCollisionShape collisionShape;
    public btMotionState motionState;
    public ShapesEnum shapeType;
    public Vector3 localInertia = Vector3.Zero.cpy();
    public float mass;

    public int rbHashCode = -1;

    private final Vector3 translation = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Vector3 scale = new Vector3();
    private final Matrix4 tmpM4 = new Matrix4();

    // see: https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/#using-motion-states
    public static class HGbtMotionState extends btMotionState {
        public final HGModelInstance mi;
        public final Matrix4 transform;

        private final Vector3 translation = new Vector3();
        private final Quaternion rotation = new Quaternion();
        private final Vector3 scale = new Vector3();
        private final Matrix4 tmpM4 = new Matrix4();

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            transform.getTranslation(translation);
            transform.getRotation(rotation, true).nor();
            tmpM4.setToTranslation(translation).rotate(rotation.nor());
            worldTrans.set(tmpM4);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.getScale(scale);
            transform.set(worldTrans);
            mi.scale(scale.x, scale.y, scale.z);
        }

        public HGbtMotionState(HGModelInstance mi) {
            this.mi = mi;
            this.transform = mi.transform;
        }
    }

    public PhysicalModelInstance(Model model, float mass, ShapesEnum shapeType) { this(new HGModel(model), null, mass, shapeType, (String[])null); }
    public PhysicalModelInstance(Model model, float mass, ShapesEnum shapeType, String... rootNodeIds) { this(new HGModel(model), null, mass, shapeType, rootNodeIds); }
    public PhysicalModelInstance(HGModel hgModel, float mass, ShapesEnum shapeType) { this(hgModel, null, mass, shapeType, (String[])null); }
    public PhysicalModelInstance(HGModel hgModel, float mass, ShapesEnum shapeType, String... rootNodeIds) { this(hgModel, null, mass, shapeType, rootNodeIds); }
    public PhysicalModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shapeType) { this(hgModel, assetFL, mass, shapeType, (String[])null); }
    public PhysicalModelInstance(HGModel hgModel, FileHandle assetFL, float mass, ShapesEnum shapeType, String... rootNodeIds) {
        super(hgModel, assetFL, rootNodeIds);
        this.mass = mass;
        this.shapeType = shapeType;
        createRigidBody();
        setMotionState(new HGbtMotionState(this));
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
        if (rigidBodyConstructionInfo != null) { rigidBodyConstructionInfo.dispose(); rigidBodyConstructionInfo = null; }
        if (collisionShape != null) {
            collisionShape.release();
            collisionShape.dispose();
            collisionShape = null;
        }
        if (motionState != null) { motionState.dispose(); motionState = null; }
        super.dispose();
    }

    @Override public void trn(Vector3 vector) { super.trn(vector); syncRBWithTransform(); }
    @Override public void trn(float x, float y, float z) { super.trn(x, y, z); syncRBWithTransform(); }
    @Override public void translate(Vector3 translation) { super.translate(translation); syncRBWithTransform(); }
    @Override public void setToTranslation(Vector3 vector) { super.setToTranslation(vector); syncRBWithTransform(); }
    @Override public void setToTranslation(float x, float y, float z) { super.setToTranslation(x, y, z); syncRBWithTransform(); }
    @Override public void setToTranslationAndScaling(Vector3 translation, Vector3 scaling) { super.setToTranslationAndScaling(translation, scaling); syncRBWithTransform(true); }
    @Override public void setToTranslationAndScaling(float x, float y, float z, float sX, float sY, float sZ) { super.setToTranslationAndScaling(x, y, z, sX, sY, sZ); syncRBWithTransform(true);}
    @Override public void scl(float factor) { super.scl(factor); syncRBWithTransform(true); }
    @Override public void scl(Vector3 factor) { super.scl(factor); syncRBWithTransform(true); }
    @Override public void scale(float scaleX, float scaleY, float scaleZ) { super.scale(scaleX, scaleY, scaleZ); syncRBWithTransform(true); }
    @Override public void setToScaling(float factor) { super.setToScaling(factor); syncRBWithTransform(true); }
    @Override public void setToScaling(Vector3 factor) { super.setToScaling(factor); syncRBWithTransform(true); }
    @Override public void rotate(Vector3 axis, float degrees) { super.rotate(axis, degrees); syncRBWithTransform(); }
    @Override public void rotate(Quaternion rotation) { super.rotate(rotation); syncRBWithTransform(); }
    @Override public void rotate(Vector3 v1, Vector3 v2) { super.rotate(v1, v2); syncRBWithTransform(); }

    public void syncRBWithTransform() { syncRBWithTransform(false); }
    public void syncRBWithTransform(boolean resetShape) {
        transform.getTranslation(translation);
        transform.getRotation(rotation, true).nor();
        tmpM4.setToTranslation(translation).rotate(rotation.nor());

        if (resetShape) {
            transform.getScale(scale);
            resetRigidBodyShape(scale);
            rigidBody.setCollisionShape(collisionShape);
        }

        rigidBody.proceedToTransform(tmpM4);
    }

    public enum ShapesEnum {
        BOX, MESH
    }

    public void setMotionState(btMotionState motionState) {
        if (this.motionState != null) { this.motionState.dispose(); this.motionState = null; }
        this.motionState = motionState;
        rigidBody.setMotionState(motionState);
    }

    public int createRigidBody() {
        if (rigidBody != null) { rigidBody.dispose(); rigidBody = null; }
        if (rigidBodyConstructionInfo != null) {
            rigidBodyConstructionInfo.dispose();
            rigidBodyConstructionInfo = null;
        }

        transform.getTranslation(translation);
        transform.getRotation(rotation, true).nor();
        transform.getScale(scale);

        resetRigidBodyShape(scale);

        rigidBodyConstructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, collisionShape, localInertia);
        rigidBody = new btRigidBody(rigidBodyConstructionInfo);
        rbHashCode = rigidBody.hashCode();

        // IMPORTANT: see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // While this is easy to work with, you should keep in mind that the transform -as far as bullet is concerned-
        // only contains a position and rotation. Any other transformation, like for example scaling, is not supported.
        // In practice this means that you should never apply scaling directly to objects when using the bullet wrapper.
        // There are other ways to scale objects, but in general I would recommend to try to avoid scaling.
        Matrix4 tmp = new Matrix4().setToTranslation(translation).rotate(rotation.nor());
        rigidBody.setWorldTransform(tmp);
        rigidBody.setUserValue(rbHashCode);

        // see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
        // we also inform Bullet that we want to receive collision events for this object by adding the CF_CUSTOM_MATERIAL_CALLBACK flag.
        // This flag is required for the onContactAdded method to be called.
        rigidBody.setCollisionFlags(
                rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        return rbHashCode;
    }

    public void resetRigidBodyShape(Vector3 scale) {
        if (collisionShape != null) { collisionShape.dispose(); collisionShape = null; }
        Vector3 dims = getBB(false).getDimensions(new Vector3()).scl(scale);
        // models are assumed to be centered to origin (see HGModel::centerToOrigin),
        // otherwise an offset to the model's bounding box center should be taken into account
        switch (shapeType) {
            case BOX:
                collisionShape = new btBoxShape(dims.scl(0.5f));
                collisionShape.calculateLocalInertia(mass, localInertia);
                break; // scl(0.5f) so we get half-extents
            case MESH:
                collisionShape = btBvhTriangleMeshShape.obtain(hgModel.obj.meshParts);
                // libgdx\extensions\gdx-bullet\jni\src\bullet\bulletcollision\collisionshapes\bttrianglemeshshape.cpp:184
                // btTriangleMeshShape::calculateLocalInertia
                //	 //moving concave objects not supported
                //	 btAssert(0);
                break;
            default:
                Gdx.app.error(getClass().getSimpleName(), "ERROR: unknown shape type: " + shapeType.name());
                return;
        }
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
