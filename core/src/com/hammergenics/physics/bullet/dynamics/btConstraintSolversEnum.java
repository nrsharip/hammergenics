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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolverType;
import com.badlogic.gdx.utils.Disposable;

// CONSTRAINT SOLVERS:
// https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
// simply said: constraints can be used to attach objects to each other
//
// "Exploring MLCP solvers and Featherstone" by Erwin Coumans:
// http://goo.gl/84N71q (https://www.gdcvault.com/play/1020076/Physics-for-Game-Programmers-Exploring)
// "Normal and Friction Stabilization Techniques for Interactive Rigid Body Constraint-based
// Contact Force Computations" by Morten Silcowitz, Sarah Niebe, Kenny Erleben
// https://diglib.eg.org/bitstream/handle/10.2312/PE.vriphys.vriphys10.089-095/089-095.pdf
// https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/ConstraintSolver/btConstraintSolver.h#L32
// enum btConstraintSolverType
// {
//     BT_SEQUENTIAL_IMPULSE_SOLVER = 1,
//     BT_MLCP_SOLVER = 2,
//     BT_NNCG_SOLVER = 4,
//     BT_MULTIBODY_SOLVER = 8,
//     BT_BLOCK_SOLVER = 16,
// };
public enum btConstraintSolversEnum implements Disposable {
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h
    BT_SEQUENTIAL_IMPULSE_SOLVER(btConstraintSolverType.BT_SEQUENTIAL_IMPULSE_SOLVER, "Sequential Impulse", "SI"),
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btMLCPSolver.h
    BT_MLCP_SOLVER(btConstraintSolverType.BT_MLCP_SOLVER, "Mixed Linear Complementarity Problem", "MLCP"),
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/ConstraintSolver/btNNCGConstraintSolver.h
    BT_NNCG_SOLVER(btConstraintSolverType.BT_NNCG_SOLVER, "Nonlinear Nonsmooth Conjugate Gradient", "NNCG"),
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/Featherstone/btMultiBodyConstraintSolver.h
    BT_MULTIBODY_SOLVER(8, "Multi-Body", "MB");
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/Dynamics/btDiscreteDynamicsWorldMt.h#L24
    // TODO: look into multi-threaded solvers' pooling

    public final int type;
    public final String fullName;
    public final String abbreviation;
    public btConstraintSolver instance = null;

    btConstraintSolversEnum(int type, String fullName, String abbreviation) {
        this.type = type;
        this.fullName = fullName;
        this.abbreviation = abbreviation;
    }

    public btConstraintSolver getInstance() { return instance; }
    public void setInstance(btConstraintSolver instance) {
        if (this.instance == null) { this.instance = instance; }
    }

    public static btConstraintSolversEnum findByType(int type) {
        for (btConstraintSolversEnum cs: btConstraintSolversEnum.values()) {
            if (cs.type == type) { return cs; }
        }
        Gdx.app.error("bullet", "ERROR: undefined constraint solver type " + type);
        return null;
    }

    @Override
    public String toString() { return toString(false); }
    public String toString(boolean abbr) { return abbr ? abbreviation : fullName; }

    @Override
    public void dispose() { if (instance != null) { instance.dispose(); } }

    public static void disposeAll() {
        for (btConstraintSolversEnum cs: btConstraintSolversEnum.values()) { cs.dispose(); }
    }
}