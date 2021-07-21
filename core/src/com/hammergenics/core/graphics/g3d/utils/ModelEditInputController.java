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

package com.hammergenics.core.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.hammergenics.HGEngine;
import com.hammergenics.core.ModelEditScreen;
import com.hammergenics.core.graphics.g3d.EditableModelInstance;
import com.hammergenics.core.graphics.g3d.HGModelInstance;
import com.hammergenics.core.graphics.g3d.model.AnimationInfo;
import com.hammergenics.core.utils.AttributesMap;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditInputController extends SpectatorInputController {
    public ModelEditScreen modelES;
    public HGEngine eng;

    public boolean editMode = false;

    // main Model Instances
    public EditableModelInstance hoveredOverMI = null;
    public AttributesMap hoveredOverMIAttributes = null;
    public EditableModelInstance draggedMI = null;
    // bounding box, corners
    public HGModelInstance hoveredOverBBMI = null;
    public Array<HGModelInstance> hoveredOverCornerMIs = null;
    public HGModelInstance hoveredOverCorner = null;
    public AttributesMap hoveredOverCornerAttributes = null;
    // node
    public Node hoveredOverNode = null;

    public ModelEditInputController(ModelEditScreen modelES, Camera camera) {
        this(modelES, camera, new ModelEditorGestureProcessor());
    }

    public ModelEditInputController(ModelEditScreen modelES, Camera camera, ModelEditorGestureProcessor megp) {
        super(camera, megp);
        this.modelES = modelES;
        this.eng = modelES.eng;
        megp.meic = this;
    }

    public static class ModelEditorGestureProcessor extends SpectatorGestureProcessor {
        public ModelEditInputController meic;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            if (meic.checkTouchDown(x, y, pointer, button)) {
                return super.touchDown(x, y, pointer, button);
            } else {
                return false;
            }
            //return super.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            if (meic.modelES != null) {
                if (meic.checkTap(x, y, count, button)) {
                    return super.tap(x, y, count, button);
                } else {
                    return false;
                }
            }
            return super.tap(x, y, count, button);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (meic.modelES != null) {
                if (meic.checkPan(x, y, deltaX, deltaY, touchDownButton, meic.getOverallDistance())) {
                    return super.pan(x, y, deltaX, deltaY);
                } else {
                    return false;
                }
            }
            return super.pan(x, y, deltaX, deltaY);
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            if (meic.modelES != null) {
                if (meic.checkPanStop(x, y, pointer, button)) {
                    return super.panStop(x, y, pointer, button);
                } else {
                    return false;
                }
            }
            return super.panStop(x, y, pointer, button);
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        // Ctrl + A: selecting all the model instances
        if (keysPressed.size == 2
                && (keysPressed.contains(Keys.CONTROL_LEFT) || keysPressed.contains(Keys.CONTROL_RIGHT))
                && keysPressed.contains(Keys.A)) {
            EditableModelInstance curr = eng.getCurrMI();
            eng.selectedMIs.clear();
            eng.selectedMIs.addAll(eng.editableMIs);
            if (curr != null) { eng.makeCurrMI(curr); }
            modelES.stage.reset();
        }
        // Delete: deleting all selected model instances
        if (keysPressed.size == 1 && (keysPressed.contains(Keys.DEL) || keysPressed.contains(Keys.FORWARD_DEL))) {
            eng.selectedMIs.forEach(eng::removeEditableModelInstance);
            eng.selectedMIs.clear();
            modelES.stage.reset();
        }
        return super.keyUp(keycode);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (modelES != null) { checkMouseMoved(screenX, screenY); }

        return super.mouseMoved(screenX, screenY);
    }

    public void checkMouseMoved(int screenX, int screenY) {
        Ray ray = modelES.perspectiveCamera.getPickRay(screenX, screenY);

        if (hoveredOverBBMI != null && hoveredOverCornerMIs.size > 0) {
            // we're hovering over some model instance having bounding box rendered as well
            // let's check if we're hovering over a corner of that bounding box:
            Array<HGModelInstance> outCorners;
            outCorners = eng.rayMICollision(ray, hoveredOverCornerMIs, new Array<>(HGModelInstance.class));
            if (outCorners.size > 0 && !outCorners.get(0).equals(hoveredOverCorner)) {
                // we're hovering over the new corner, need to restore the attributes of the previous corner (if any)
                eng.restoreAttributes(hoveredOverCorner, hoveredOverCornerAttributes);
                hoveredOverCorner = outCorners.get(0); // SWITCHING THE HOVERED OVER CORNER
                hoveredOverCornerAttributes = new AttributesMap();
                eng.saveAttributes(hoveredOverCorner, hoveredOverCornerAttributes);
                hoveredOverCorner.setAttributes(new BlendingAttribute(1f));
                return; // nothing else should be done
            } else if (outCorners.size == 0) {
                // we're not hovering over any corners
                eng.restoreAttributes(hoveredOverCorner, hoveredOverCornerAttributes);
                hoveredOverCorner = null;
                if (hoveredOverCornerAttributes != null) { hoveredOverCornerAttributes.clear(); }
                hoveredOverCornerAttributes = null;
            } else if (outCorners.get(0).equals(hoveredOverCorner)) {
                // we're hovering over the same corner, nothing else to do here
                return;
            }
        }

        if (modelES.stage.nodesCheckBox.isChecked()) {
            // we have nodes rendered, let's check if we're hovering over any of the nodes
            // of all the models currently present
            Array<BoundingBox> outNodeBBs;
            Array<BoundingBox> inNodeBBs = new Array<>(true, 16, BoundingBox.class);
            ArrayMap<BoundingBox, EditableModelInstance> bb2mi = new ArrayMap<>(BoundingBox.class, EditableModelInstance.class);
            for (EditableModelInstance editableMi: eng.editableMIs) {
                Array<BoundingBox> bbs = editableMi.bb2n.keys().toArray();
                for (BoundingBox bb: bbs) { bb2mi.put(bb, editableMi); }
                inNodeBBs.addAll(bbs);
            }
            // inNodeBBs now have all nodes of all models present
            outNodeBBs = eng.rayBBCollision(ray, inNodeBBs, new Array<>(true, 16, BoundingBox.class));
            for (BoundingBox bb:outNodeBBs) {
                Gdx.app.debug(getClass().getSimpleName(), "node: " + bb2mi.get(bb).bb2n.get(bb).id);
            }
            // outNodeBBs have all the nodes (if any) intersected by the ray from the mouse pointer
            // sorted by the distance from the ray's origin ascending
            if (outNodeBBs.size > 0) {
                // we got some nodes intersected by the ray, switching the hovered over model
                // to the model owning the closest node intersected
                switchHoveredOverMI(new Array<>(new EditableModelInstance[]{bb2mi.get(outNodeBBs.get(0))}));
                // saving the info on the hovered over node in both engine and the model instance itself
                hoveredOverNode = hoveredOverMI.bb2n.get(outNodeBBs.get(0));
                hoveredOverMI.hoveredOverNode = hoveredOverNode;
                return; // nothing else should be done
            } else {
                // no nodes intersected, nullify all references
                if (hoveredOverMI != null) { hoveredOverMI.hoveredOverNode = null; }
                hoveredOverNode = null;
            }
        }

        Array<EditableModelInstance> out = null;
        if (hoveredOverMI != null) {
            // this is in case we hovered over the node of the model instance obscured by the
            // bounding box of another model instance - we still want to keep the current hovered
            // model to the one with the node previously intersected in case the ray intersects
            // this model instance
            out = eng.rayMICollision(ray, new Array<>(new EditableModelInstance[]{hoveredOverMI}),
                    new Array<>(EditableModelInstance.class));
        }

        if (out == null || out.size == 0) {
            // in case we're not intersecting any previously hovered model instances
            // check if we intersect any models at all. If we do then switch the current hovered
            // model instance.
            out = eng.rayMICollision(ray, eng.editableMIs, new Array<>(EditableModelInstance.class));
            switchHoveredOverMI(out);
        }
    }

    private void switchHoveredOverMI(Array<EditableModelInstance> mis) {
        if (mis.size > 0 && !mis.get(0).equals(hoveredOverMI)) {
            // no need to dispose the box and the corners - will be done in HGModelInstance on dispose()
            eng.auxMIs.clear();
            hoveredOverMI = mis.get(0); // SWITCHING THE HOVERED OVER MODEL INSTANCE

            hoveredOverBBMI = hoveredOverMI.getBBHgModelInstance(Color.BLACK);
            eng.auxMIs.add(hoveredOverBBMI);
            hoveredOverCornerMIs = hoveredOverMI.getCornerHgModelInstances(Color.RED);
            eng.auxMIs.addAll(hoveredOverCornerMIs);
        } else if (mis.size == 0) {
            hoveredOverMI = null;
            hoveredOverBBMI = null;
            // no need to dispose the box and the corners - will be done in HGModelInstance on dispose()
            eng.auxMIs.clear();
        }
    }

    public boolean checkTouchDown(float x, float y, int pointer, int button) {
        if (hoveredOverMI != null) {
            Vector3 currTranslation = hoveredOverMI.transform.getTranslation(new Vector3());
            Vector3 currScale = hoveredOverMI.transform.getScale(new Vector3());
            Quaternion currRotation = hoveredOverMI.transform.getRotation(new Quaternion());

            Gdx.app.debug(getClass().getSimpleName(), "b translation: " + currTranslation);
            Gdx.app.debug(getClass().getSimpleName(), "b scale: " + currScale);
            Gdx.app.debug(getClass().getSimpleName(), "b rotation: " + currRotation);
            Gdx.app.debug(getClass().getSimpleName(), "b:\n" + hoveredOverMI.transform);

            editMode = true;
        }
        return true;
    }

    public boolean checkTap(float x, float y, int count, int button) {
        switch (button) {
            case Buttons.LEFT:
                boolean ctrlPressed = keysPressed.contains(Keys.CONTROL_LEFT) || keysPressed.contains(Keys.CONTROL_RIGHT);
                if (hoveredOverMI != null && ctrlPressed) {
                    if (eng.selectedMIs.indexOf(hoveredOverMI, true) >= 0) {
                        eng.selectedMIs.removeValue(hoveredOverMI, true);
                    } else {
                        eng.selectedMIs.add(hoveredOverMI);
                    }
                } else if (hoveredOverMI != null && hoveredOverMI != eng.getCurrMI()) {
                    eng.setCurrMI(hoveredOverMI);
                } else if (hoveredOverMI == null) {
                    eng.setCurrMI(null);
                }
                modelES.stage.reset();
                return false;
            case Buttons.MIDDLE: // fall through
            case Buttons.RIGHT:
                return true;
        }
        return true;
    }

    public boolean checkPan(float x, float y, float deltaX, float deltaY, int touchDownButton, float overallDistance) {
        float fracX = deltaX / Gdx.graphics.getWidth(), fracY = deltaY / Gdx.graphics.getHeight();
        Camera cam = modelES.perspectiveCamera;

        Vector3 miCenter = null;
        Vector3 miTranslation = null;
        Vector3 miScale = null;
        Quaternion miRot = null;
        Matrix4 miTransform = null;

        if (hoveredOverMI != null) {
            miCenter = hoveredOverMI.getBB().getCenter(new Vector3());
            miTransform = hoveredOverMI.transform.cpy();
            miTranslation = miTransform.getTranslation(new Vector3());
            miScale = miTransform.getScale(new Vector3());
            // see getRotation() description:
            // normalizeAxes True to normalize the axes, necessary when the matrix might also include scaling.
            miRot = miTransform.getRotation(new Quaternion(), true);
            // see https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
            // see https://j3d.org/matrix_faq/matrfaq_latest.html
            // see http://web.archive.org/web/20041029003853/http://web.archive.org/web/20041029003853/http://www.j3d.org/matrix_faq/matrfaq_latest.html#Q50
        }

        switch (touchDownButton) {
            case Buttons.LEFT:
                if (hoveredOverMI != null && hoveredOverCorner != null) {
                    // we hold the left button pressed on the model instance's corner - applying scaling
                    eng.setCurrMI(hoveredOverMI);
                    modelES.stage.reset();

                    Vector3 corner = hoveredOverCorner.getBB().getCenter(new Vector3());

                    Vector3 coordCenter = cam.project(miCenter.cpy(), 0, 0, cam.viewportWidth, cam.viewportHeight);
                    Vector3 coordCorner = cam.project(corner.cpy(), 0, 0, cam.viewportWidth, cam.viewportHeight);
                    Vector3 coordDelta = new Vector3(deltaX, -deltaY, 0);
                    Vector3 coordHlfDiag = coordCorner.cpy().sub(coordCenter);
                    Vector3 coordDir = coordHlfDiag.cpy().nor();

                    // need to make sure the gesture matches the corner correctly.
                    // e.g. the gesture is top-right:
                    // * for the top-right corner the scale should be increased
                    // * for the bottom-left corner the scale should be decreased
                    int sign = coordDelta.dot(coordDir) > 0 ? 1 : -1;
                    float scale = 1 + sign * 0.04f ;

                    hoveredOverMI.scale(scale, scale, scale);
//                    Gdx.app.debug(getClass().getSimpleName(), ""
//                            + " coordCenter: " + coordCenter + " coordCorner: " + coordCorner
//                            + " coordHlfDiag: " + coordHlfDiag + " coordDir: " + coordDir
//                            + " sign: " + sign
//                            + " x: " + x + " y: " + y
//                            + " deltaX: " + deltaX + " deltaY: " + deltaY
//                            + " coordDir.x: " + coordDir.x + " coordDir.y: " + coordDir.y
//                            + " scale: " + scale
//                            + " cam.viewportWidth: " + cam.viewportWidth + " cam.viewportHeight: " + cam.viewportHeight
//                            + "\nprojtest100: " + cam.project(new Vector3(1,0,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest010: " + cam.project(new Vector3(0,1,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest001: " + cam.project(new Vector3(0,0,1), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest200: " + cam.project(new Vector3(2,0,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest020: " + cam.project(new Vector3(0,2,0), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "\nprojtest002: " + cam.project(new Vector3(0,0,2), 0, 0, cam.viewportWidth, cam.viewportHeight)
//                            + "cam.combined: \n" + cam.combined
//                    );
                    return false;
                } else if (hoveredOverMI != null && hoveredOverNode != null && hoveredOverNode.getParent() != null) {
                    // we hold the left button pressed on the model instance's node
                    Ray ray = cam.getPickRay(x + deltaX, y - deltaY);
                    Node node = hoveredOverNode;
                    Node parent = hoveredOverNode.getParent();

                    Vector3 nodeTrans = node.globalTransform.getTranslation(new Vector3());
                    Quaternion nodeRot = node.globalTransform.getRotation(new Quaternion(), true);
                    Vector3 nodeScale = node.globalTransform.getScale(new Vector3());

                    Vector3 parentTrans = parent.globalTransform.getTranslation(new Vector3());
                    Quaternion parentRot = parent.globalTransform.getRotation(new Quaternion(), true);
                    Vector3 parentScale = parent.globalTransform.getScale(new Vector3());

                    // Example of parent global transform multiplied by child's local transform to get the resulting child's global transform:
                    //            parent.global                        child.local                             child.global
                    //  [100.0|     -0.0|      0.0|0.0] [-3.576E-7|-2.980E-8|   -0.999|0.204]   [-3.576E-5|-2.980E-6| -99.999| 20.420]
                    //  [  0.0|-1.192E-5|   99.999|0.0] [      1.0|      0.0|-3.874E-7|0.148] = [-1.788E-5|  -99.999|5.960E-6| 19.320]
                    //  [ -0.0|  -99.999|-1.192E-5|0.0] [-5.960E-8|   -0.999| 5.960E-8|0.193]   [-1.788E-5|  -99.999|5.960E-6| 19.320]
                    //  [  0.0|      0.0|      0.0|1.0] [      0.0|      0.0|      0.0|  1.0]   [      0.0|      0.0|     0.0|    1.0]
                    //
                    // Basically this means that:
                    //                    node.GT  =                     parent.GT  MUL node.LT
                    // parent.GT.inv MUL (node.GT) =  parent.GT.inv MUL (parent.GT  MUL node.LT) : associativity
                    // parent.GT.inv MUL  node.GT  = (parent.GT.inv MUL  parent.GT) MUL node.LT  : definition of inverse
                    // parent.GT.inv MUL  node.GT  =                                    node.LT

                    float radius = nodeTrans.cpy().mul(miTransform).sub(parentTrans.cpy().mul(miTransform)).len();
                    Vector3 intersection = new Vector3();
                    if (!Intersector.intersectRaySphere(ray, parentTrans.cpy().mul(miTransform), radius, intersection)) {
                        if (!Intersector.intersectRayPlane(ray, new Plane(cam.direction, parentTrans.cpy().mul(miTransform)), intersection)) {
                            Gdx.app.error(getClass().getSimpleName(), "ERROR shouldn't be here");
                            return false;
                        }
                    }

                    Vector3 dirOld = nodeTrans.cpy().sub(parentTrans).nor();
                    Vector3 dirNew = intersection.cpy().sub(parentTrans.cpy().mul(miTransform)).nor();
                    Quaternion rot = new Quaternion().setFromCross(dirOld, dirNew).nor();

                    Matrix4 tmpGlobal = new Matrix4();
                    Matrix4 tmpLocal = new Matrix4();

                    tmpGlobal.setToTranslationAndScaling(parentTrans, parentScale);
                    tmpGlobal.rotate(rot.mul(parentRot.cpy().nor()).nor());

                    Node parent2 = parent.getParent();
                    if (parent.inheritTransform && parent2 != null) {
                        // parent.LT = parent2.GT.inv MUL parent.GT (see above)
                        tmpLocal.set(parent2.globalTransform.cpy().inv().mul(tmpGlobal));
                    } else {
                        tmpLocal.set(tmpGlobal);
                    }
                    if (hoveredOverMI.isAnimEditMode()) {
                        Animation anim = hoveredOverMI.selectedAnimation;
                        AnimationInfo info = hoveredOverMI.anim2info.get(anim);
                        NodeAnimation nodeAnim = info.getNodeAnimation(parent);
                        info.addNodeKeyFrame(nodeAnim,
                                tmpLocal.getTranslation(new Vector3()),
                                tmpLocal.getRotation(new Quaternion()).nor(),
                                null);
                        hoveredOverMI.animApplyKeyTime();
                    } else {
                        parent.translation.set(tmpLocal.getTranslation(new Vector3()));
                        parent.rotation.set(tmpLocal.getRotation(new Quaternion()).nor());
                    }
                    hoveredOverMI.calculateTransforms();
                    // this update will also affect:
                    // parent.localTransform
                    // parent.globalTransform
                    // node.localTransform
                    // node.globalTransform

                    return false;
                } else if ((keysPressed.contains(Keys.SHIFT_LEFT) || keysPressed.contains(Keys.SHIFT_RIGHT))
                        && hoveredOverMI != null) {
                    // we hold the SHIFT key and left button pressed on the model instance itself - applying rotation
                    eng.setCurrMI(hoveredOverMI);
                    modelES.stage.reset();

                    // removing the rotation and scale components from the transform
                    hoveredOverMI.setToTranslation(miTranslation);
                    // rotating as per the gesture
                    hoveredOverMI.rotate(cam.up.cpy().nor(), fracX * 360f);
                    hoveredOverMI.rotate(cam.direction.cpy().crs(cam.up).nor(), fracY * 360f);
                    // restoring the original rotation
                    hoveredOverMI.rotate(miRot);
                    // restoring the original scale
                    hoveredOverMI.scale(miScale.x, miScale.y, miScale.z);
                    return false;
                } else if ((keysPressed.contains(Keys.CONTROL_LEFT) || keysPressed.contains(Keys.CONTROL_RIGHT))
                        && hoveredOverMI != null) {
                    // we hold the CTRL key and left button pressed on the model instance itself - applying vert translation
                    eng.setCurrMI(hoveredOverMI);
                    modelES.stage.reset();
                    draggedMI = hoveredOverMI;

                    // removing the rotation and scale components from the transform
                    draggedMI.setToTranslation(miTranslation);
                    // translating as per the gesture
                    Vector3 tmpV = Vector3.Y.cpy().scl(4 * -fracY * overallDistance);
                    draggedMI.translate(tmpV);
                    // restoring the original rotation
                    draggedMI.rotate(miRot);
                    // restoring the original scale
                    draggedMI.scale(miScale.x, miScale.y, miScale.z);
                    return false;
                } else if (hoveredOverMI != null) {
                    // we hold the left button pressed on the model instance itself - applying hor translation
                    eng.setCurrMI(hoveredOverMI);
                    modelES.stage.reset();
                    draggedMI = hoveredOverMI;

                    // removing the rotation and scale components from the transform
                    draggedMI.setToTranslation(miTranslation);
                    // translating as per the gesture
                    Vector3 tmpV = cam.direction.cpy().crs(cam.up).nor().scl(4 * fracX * overallDistance);
                    draggedMI.translate(tmpV);
                    tmpV.set(cam.up).y = 0;
                    tmpV.nor().scl(4 * -fracY * overallDistance);
                    draggedMI.translate(tmpV);
                    // restoring the original rotation
                    draggedMI.rotate(miRot);
                    // restoring the original scale
                    draggedMI.scale(miScale.x, miScale.y, miScale.z);
                    return false;
                }
                return true;
            case Buttons.MIDDLE:
                return true;
            case Buttons.RIGHT:
                // keeping the right free so the camera rotation stays available
                return true;
        }
        return true;
    }

    public boolean checkPanStop(float x, float y, int pointer, int button) {
        if (hoveredOverMI != null) {
            Vector3 currTranslation = hoveredOverMI.transform.getTranslation(new Vector3());
            Vector3 currScale = hoveredOverMI.transform.getScale(new Vector3());
            Quaternion currRotation = hoveredOverMI.transform.getRotation(new Quaternion(), true);

            Gdx.app.debug(getClass().getSimpleName(), "a translation: " + currTranslation);
            Gdx.app.debug(getClass().getSimpleName(), "a scale: " + currScale);
            Gdx.app.debug(getClass().getSimpleName(), "a rotation: " + currRotation);
            Gdx.app.debug(getClass().getSimpleName(), "a:\n" + hoveredOverMI.transform);

            editMode = false;
        }
        // TODO: fix BB checkbox
        //eng.resetBBModelInstances();
        draggedMI = null;
        return true;
    }
}