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

package com.hammergenics.ai.steer.behaviors;

import com.badlogic.gdx.ai.steer.behaviors.Jump;
import com.badlogic.gdx.math.Vector3;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class Y3DGravityComponentHandler implements Jump.GravityComponentHandler<Vector3> {
    @Override public float getComponent(Vector3 vector) { return vector.y; }
    @Override public void setComponent(Vector3 vector, float value) { vector.y = value; }
}
