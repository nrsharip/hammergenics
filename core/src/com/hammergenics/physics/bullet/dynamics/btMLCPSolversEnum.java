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

import com.badlogic.gdx.physics.bullet.dynamics.btDantzigSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btLemkeSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btMLCPSolverInterface;
import com.badlogic.gdx.physics.bullet.dynamics.btSolveProjectedGaussSeidel;
import com.badlogic.gdx.utils.Disposable;

public enum btMLCPSolversEnum implements Disposable {
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btDantzigLCP.cpp
    BT_DANTZIG("Dantzig"),
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btLemkeAlgorithm.h
    BT_LEMKE("Lemke"),
    // https://github.com/bulletphysics/bullet3/blob/master/src/BulletDynamics/MLCPSolvers/btSolveProjectedGaussSeidel.h#L22
    // This solver is mainly for debug/learning purposes: it is functionally equivalent to the
    // btSequentialImpulseConstraintSolver solver, but much slower (it builds the full LCP matrix)
    BT_GAUSS_SEIDEL("Gauss–Seidel");

    static {
        BT_DANTZIG.setInstance(new btDantzigSolver());
        BT_LEMKE.setInstance(new btLemkeSolver());
        BT_GAUSS_SEIDEL.setInstance(new btSolveProjectedGaussSeidel());
    }

    private boolean isApplied = false;
    public final String fullName;
    public btMLCPSolverInterface instance = null;

    btMLCPSolversEnum(String fullName) { this.fullName = fullName; }

    public btMLCPSolverInterface apply() {
        unsetAll();
        isApplied = true;
        return instance;
    }
    public void setInstance(btMLCPSolverInterface instance) {
        if (this.instance == null) { this.instance = instance; }
    }

    @Override public String toString() { return fullName; }
    @Override public void dispose() { if (instance != null) { instance.dispose(); } }

    public static void disposeAll() {
        for (btMLCPSolversEnum mlcps: btMLCPSolversEnum.values()) { mlcps.dispose(); }
    }

    private static void unsetAll() {
        for (btMLCPSolversEnum solver: btMLCPSolversEnum.values()) { solver.isApplied = false; }
    }

    public static btMLCPSolversEnum current() {
        for (btMLCPSolversEnum solver: btMLCPSolversEnum.values()) {
            if (solver.isApplied) { return solver; }
        }
        return null;
    }
}