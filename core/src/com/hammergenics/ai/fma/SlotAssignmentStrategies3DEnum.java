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

package com.hammergenics.ai.fma;

import com.badlogic.gdx.ai.fma.FreeSlotAssignmentStrategy;
import com.badlogic.gdx.ai.fma.SlotAssignmentStrategy;
import com.badlogic.gdx.ai.fma.SoftRoleSlotAssignmentStrategy;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.ai.fma.SoftRoleSlotAssignmentStrategy.SlotCostProvider;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum SlotAssignmentStrategies3DEnum {
    // https://github.com/libgdx/gdx-ai/wiki/Formation-Motion#slot-assignment-strategies
    FREE,
    // https://github.com/libgdx/gdx-ai/wiki/Formation-Motion#hard-and-soft-roles
    SOFT_ROLE;

    public SlotAssignmentStrategy<Vector3> getInstance() {
        switch (this) {
            case FREE: return new FreeSlotAssignmentStrategy<>();
            default: return null;
        }
    }

    public SlotAssignmentStrategy<Vector3> getInstance(SlotCostProvider<Vector3> slotCostProvider, float costThreshold) {
        switch (this) {
            case SOFT_ROLE: return new SoftRoleSlotAssignmentStrategy<>(slotCostProvider, costThreshold);
            default: return null;
        }
    }
}