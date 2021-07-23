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

import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.fma.patterns.DefensiveCircleFormationPattern;
import com.badlogic.gdx.ai.fma.patterns.OffensiveCircleFormationPattern;
import com.badlogic.gdx.math.Vector3;

/**
 * Add description here
 *
 * @author nrsharip
 */
public enum FormationPatterns3DEnum {
    // https://github.com/libgdx/gdx-ai/wiki/Formation-Motion
    DEFENSIVE_CIRCLE,
    OFFENSIVE_CIRCLE;

    public float memberRadius;

    public FormationPattern<Vector3> getInstance(float memberRadius) {
        this.memberRadius = memberRadius;
        switch (this) {
            case DEFENSIVE_CIRCLE: return new DefensiveCircleFormationPattern<>(memberRadius);
            case OFFENSIVE_CIRCLE: return new OffensiveCircleFormationPattern<>(memberRadius);
            default: return null;
        }
    }

    public static FormationPatterns3DEnum getByInstance(FormationPattern<Vector3> instance) {
        if (instance instanceof OffensiveCircleFormationPattern) {
            return OFFENSIVE_CIRCLE;
        } else if (instance instanceof DefensiveCircleFormationPattern) {
            return DEFENSIVE_CIRCLE;
        } else {
            return null;
        }
    }
}