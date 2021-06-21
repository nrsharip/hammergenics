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

package com.hammergenics.screens.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.hammergenics.HGEngine;
import com.hammergenics.screens.ModelEditScreen;
import com.hammergenics.screens.graphics.g3d.DebugModelInstance;
import com.hammergenics.screens.graphics.g3d.HGModelInstance;
import com.hammergenics.screens.utils.AttributesMap;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ModelEditInputController extends SpectatorInputController {
    public ModelEditScreen modelES;
    public HGEngine eng;

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
            if (meic.modelES != null) { meic.checkTap(x, y, count, button); }
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
    public boolean mouseMoved(int screenX, int screenY) {
        if (modelES != null) { checkMouseMoved(screenX, screenY); }

        return super.mouseMoved(screenX, screenY);
    }

    public void checkMouseMoved(int screenX, int screenY) {
        Ray ray = modelES.perspectiveCamera.getPickRay(screenX, screenY);

        if (eng.hoveredOverBBMI != null && eng.hoveredOverCornerMIs.size > 0) {
            // we're hovering over some model instance having bounding box rendered as well
            // let's check if we're hovering over a corner of that bounding box:
            Array<HGModelInstance> outCorners;
            outCorners = eng.rayMICollision(ray, eng.hoveredOverCornerMIs, new Array<>(HGModelInstance.class));
            if (outCorners.size > 0 && !outCorners.get(0).equals(eng.hoveredOverCorner)) {
                // we're hovering over the new corner, need to restore the attributes of the previous corner (if any)
                eng.restoreAttributes(eng.hoveredOverCorner, eng.hoveredOverCornerAttributes);
                eng.hoveredOverCorner = outCorners.get(0); // SWITCHING THE HOVERED OVER CORNER
                eng.hoveredOverCornerAttributes = new AttributesMap();
                eng.saveAttributes(eng.hoveredOverCorner, eng.hoveredOverCornerAttributes);
                eng.hoveredOverCorner.setAttributes(new BlendingAttribute(1f));
                return; // nothing else should be done
            } else if (outCorners.size == 0) {
                // we're not hovering over any corners
                eng.restoreAttributes(eng.hoveredOverCorner, eng.hoveredOverCornerAttributes);
                eng.hoveredOverCorner = null;
                if (eng.hoveredOverCornerAttributes != null) { eng.hoveredOverCornerAttributes.clear(); }
                eng.hoveredOverCornerAttributes = null;
            } else if (outCorners.get(0).equals(eng.hoveredOverCorner)) {
                // we're hovering over the same corner, nothing else to do here
                return;
            }
        }

        if (modelES.stage.nodesCheckBox.isChecked() && eng.hoveredOverMI != null) {
            Array<BoundingBox> outNodeBBs;
            outNodeBBs = eng.rayBBCollision(ray, eng.hoveredOverMI.bb2n.keys().toArray(), new Array<>(true, 16, BoundingBox.class));
            for (BoundingBox bb:outNodeBBs) {
                Gdx.app.debug(getClass().getSimpleName(), "node: " + eng.hoveredOverMI.bb2n.get(bb).id);
            }
            if (outNodeBBs.size > 0) {
                eng.hoveredOverNode = eng.hoveredOverMI.bb2n.get(outNodeBBs.get(0));
                eng.hoveredOverMI.hoveredOverNode = eng.hoveredOverNode;
                return; // nothing else should be done
            } else {
                eng.hoveredOverMI.hoveredOverNode = null;
                eng.hoveredOverNode = null;
            }
        }

        Array<DebugModelInstance> out = eng.rayMICollision(ray, eng.dbgMIs, new Array<>(DebugModelInstance.class));
        if (out.size > 0 && !out.get(0).equals(eng.hoveredOverMI)) {
            // no need to dispose the box and the corners - will be done in HGModelInstance on dispose()
            eng.auxMIs.clear();
            eng.hoveredOverMI = out.get(0); // SWITCHING THE HOVERED OVER MODEL INSTANCE

            eng.hoveredOverBBMI = eng.hoveredOverMI.getBBHgModelInstance(Color.BLACK);
            eng.auxMIs.add(eng.hoveredOverBBMI);
            eng.hoveredOverCornerMIs = eng.hoveredOverMI.getCornerHgModelInstances(Color.RED);
            eng.auxMIs.addAll(eng.hoveredOverCornerMIs);
        } else if (out.size == 0) {
            eng.hoveredOverMI = null;
            eng.hoveredOverBBMI = null;
            // no need to dispose the box and the corners - will be done in HGModelInstance on dispose()
            eng.auxMIs.clear();
        }
    }

    public boolean checkTouchDown(float x, float y, int pointer, int button) {
        if (eng.hoveredOverMI != null) {
            Vector3 currTranslation = eng.hoveredOverMI.transform.getTranslation(new Vector3());
            Vector3 currScale = eng.hoveredOverMI.transform.getScale(new Vector3());
            Quaternion currRotation = eng.hoveredOverMI.transform.getRotation(new Quaternion());

            Gdx.app.debug(getClass().getSimpleName(), "b translation: " + currTranslation);
            Gdx.app.debug(getClass().getSimpleName(), "b scale: " + currScale);
            Gdx.app.debug(getClass().getSimpleName(), "b rotation: " + currRotation);
            Gdx.app.debug(getClass().getSimpleName(), "b:\n" + eng.hoveredOverMI.transform);
        }
        return true;
    }

    public void checkTap(float x, float y, int count, int button) {
        Ray ray = modelES.perspectiveCamera.getPickRay(x, y);
        switch (button) {
            case Buttons.LEFT:
                Array<DebugModelInstance> out = eng.rayMICollision(ray, eng.dbgMIs, new Array<>(DebugModelInstance.class));
                if (out != null && out.size > 0) {
                    eng.currMI = out.get(0);
                    modelES.stage.reset();
                }
                break;
            case Buttons.MIDDLE:
                break;
            case Buttons.RIGHT:
                break;
        }
    }

    public boolean checkPan(float x, float y, float deltaX, float deltaY, int touchDownButton, float overallDistance) {
        float fracX = deltaX / Gdx.graphics.getWidth(), fracY = deltaY / Gdx.graphics.getHeight();
        Camera cam = modelES.perspectiveCamera;

        Vector3 miCenter = null;
        Vector3 miTranslation = null;
        Vector3 miScale = null;
        Quaternion miRot = null;
        Matrix4 miTransform = null;

        if (eng.hoveredOverMI != null) {
            miCenter = eng.hoveredOverMI.getBB().getCenter(new Vector3());
            miTransform = eng.hoveredOverMI.transform.cpy();
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
                if (eng.hoveredOverMI != null && eng.hoveredOverCorner != null) {
                    // we hold the left button pressed on the model instance's corner - applying scaling
                    eng.currMI = eng.hoveredOverMI;
                    modelES.stage.reset();

                    Vector3 corner = eng.hoveredOverCorner.getBB().getCenter(new Vector3());

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

                    eng.hoveredOverMI.transform.scale(scale, scale, scale);
                    eng.hoveredOverMI.bbHgModelInstanceReset();
                    eng.hoveredOverMI.bbCornersReset();
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
                } else if (eng.hoveredOverMI != null && eng.hoveredOverNode != null && eng.hoveredOverNode.getParent() != null) {
                    // we hold the left button pressed on the model instance's node
                    Ray ray = cam.getPickRay(x + deltaX, y - deltaY);
                    Node node = eng.hoveredOverNode;
                    Node parent = eng.hoveredOverNode.getParent();

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
                    parent.translation.set(tmpLocal.getTranslation(new Vector3()));
                    parent.rotation.set(tmpLocal.getRotation(new Quaternion()).nor());
                    eng.hoveredOverMI.calculateTransforms();
                    // this update will also affect:
                    // parent.localTransform
                    // parent.globalTransform
                    // node.localTransform
                    // node.globalTransform

                    return false;
                } else if ((keysPressed.contains(Keys.SHIFT_LEFT) || keysPressed.contains(Keys.SHIFT_RIGHT))
                        && eng.hoveredOverMI != null) {
                    // we hold the SHIFT key and left button pressed on the model instance itself - applying rotation
                    eng.currMI = eng.hoveredOverMI;
                    modelES.stage.reset();

                    // removing the rotation and scale components from the transform
                    eng.hoveredOverMI.transform.setToTranslation(miTranslation);
                    // rotating as per the gesture
                    eng.hoveredOverMI.transform.rotate(cam.up.cpy().nor(), fracX * 360f);
                    eng.hoveredOverMI.transform.rotate(cam.direction.cpy().crs(cam.up).nor(), fracY * 360f);
                    // restoring the original rotation
                    eng.hoveredOverMI.transform.rotate(miRot);
                    // restoring the original scale
                    eng.hoveredOverMI.transform.scale(miScale.x, miScale.y, miScale.z);

                    eng.hoveredOverMI.bbHgModelInstanceReset();
                    eng.hoveredOverMI.bbCornersReset();

                    return false;
                } else if ((keysPressed.contains(Keys.CONTROL_LEFT) || keysPressed.contains(Keys.CONTROL_RIGHT))
                        && eng.hoveredOverMI != null) {
                    // we hold the CTRL key and left button pressed on the model instance itself - applying vert translation
                    eng.currMI = eng.hoveredOverMI;
                    modelES.stage.reset();
                    eng.draggedMI = eng.hoveredOverMI;

                    // removing the rotation and scale components from the transform
                    eng.draggedMI.transform.setToTranslation(miTranslation);
                    // translating as per the gesture
                    Vector3 tmpV = Vector3.Y.cpy().scl(4 * -fracY * overallDistance);
                    eng.draggedMI.transform.translate(tmpV);
                    // restoring the original rotation
                    eng.draggedMI.transform.rotate(miRot);
                    // restoring the original scale
                    eng.draggedMI.transform.scale(miScale.x, miScale.y, miScale.z);

                    eng.draggedMI.bbHgModelInstanceReset();
                    eng.draggedMI.bbCornersReset();

                    return false;
                } else if (eng.hoveredOverMI != null) {
                    // we hold the left button pressed on the model instance itself - applying hor translation
                    eng.currMI = eng.hoveredOverMI;
                    modelES.stage.reset();
                    eng.draggedMI = eng.hoveredOverMI;

                    // removing the rotation and scale components from the transform
                    eng.draggedMI.transform.setToTranslation(miTranslation);
                    // translating as per the gesture
                    Vector3 tmpV = cam.direction.cpy().crs(cam.up).nor().scl(4 * fracX * overallDistance);
                    eng.draggedMI.transform.translate(tmpV);
                    tmpV.set(cam.up).y = 0;
                    tmpV.nor().scl(4 * -fracY * overallDistance);
                    eng.draggedMI.transform.translate(tmpV);
                    // restoring the original rotation
                    eng.draggedMI.transform.rotate(miRot);
                    // restoring the original scale
                    eng.draggedMI.transform.scale(miScale.x, miScale.y, miScale.z);

                    eng.draggedMI.bbHgModelInstanceReset();
                    eng.draggedMI.bbCornersReset();

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
        if (eng.hoveredOverMI != null) {
            Vector3 currTranslation = eng.hoveredOverMI.transform.getTranslation(new Vector3());
            Vector3 currScale = eng.hoveredOverMI.transform.getScale(new Vector3());
            Quaternion currRotation = eng.hoveredOverMI.transform.getRotation(new Quaternion(), true);

            Gdx.app.debug(getClass().getSimpleName(), "a translation: " + currTranslation);
            Gdx.app.debug(getClass().getSimpleName(), "a scale: " + currScale);
            Gdx.app.debug(getClass().getSimpleName(), "a rotation: " + currRotation);
            Gdx.app.debug(getClass().getSimpleName(), "a:\n" + eng.hoveredOverMI.transform);
        }
        // TODO: fix BB checkbox
        //eng.resetBBModelInstances();
        eng.draggedMI = null;
        return true;
    }
}