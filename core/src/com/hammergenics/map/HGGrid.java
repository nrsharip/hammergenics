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

package com.hammergenics.map;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.cellular.CellularAutomataGenerator;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;
import com.hammergenics.HGEngine;

import java.util.Arrays;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGrid extends Grid {
    public float min;
    public float max;
    public float mid;
    public float yScale = 1f;

    public HGGrid(int size) { super(size); }

    public void calculateMinMaxMid() {
        float[] values = new float[getArray().length];
        System.arraycopy(getArray(), 0, values, 0, values.length);
        Arrays.sort(values, 0, values.length);
        min = values[0];
        max = values[values.length - 1];
        mid = (max + min)/2f;
    }

    public void roundToDigits(int digits) {
        if (digits <= 0) { return; }
        double pow10 = Math.pow(10f, digits);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                float value = get(x, y);
                value = Math.round(value * pow10) / (float)pow10;
                set(x, y, value);
            }
        }
    }

    public void roundToStep(float step) {
        if (step >= 1f || step <= 0f) { return; }

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                float value = get(x, y);
                float mod = value % step;
                value -= mod;
                if (mod > step/2f) { value += step; }
                set(x, y, value);
            }
        }
    }

    // see: https://github.com/czyzby/noise4j
    public void generateNoise(float yScale, Array<NoiseStageInfo> stages) {
        if (stages.size == 0) { return; }
        final NoiseGenerator noiseGenerator = new NoiseGenerator();

        fill(0f);
        this.yScale = yScale;

        for (NoiseStageInfo stage: stages) {
            stage.seed = noiseStage(this, noiseGenerator, stage.radius, stage.modifier);
        }

        calculateMinMaxMid();
    }

    // see: https://github.com/czyzby/noise4j
    public int noiseStage(final Grid grid, final NoiseGenerator noiseGenerator, final int radius,
                          final float modifier) {
        return noiseStage(grid, noiseGenerator, radius, modifier, Generators.rollSeed());
    }

    // see: https://github.com/czyzby/noise4j
    public int noiseStage(final Grid grid, final NoiseGenerator noiseGenerator, final int radius,
                          final float modifier, int seed) {
        noiseGenerator.setRadius(radius);
        noiseGenerator.setModifier(modifier);
        // Seed ensures randomness, can be saved if you feel the need to
        // generate the same map in the future.
        noiseGenerator.setSeed(seed);
        noiseGenerator.generate(grid);
        return seed;
    }

    // see: https://github.com/czyzby/noise4j
    public void generateCellular() {
        final CellularAutomataGenerator cellularGenerator = new CellularAutomataGenerator();
        cellularGenerator.setAliveChance(0.5f);
        cellularGenerator.setIterationsAmount(4);
        cellularGenerator.generate(this);
    }

    // see: https://github.com/czyzby/noise4j
    public void generateDungeon() {
        // This algorithm likes odd-sized maps, although it works either way.
        final DungeonGenerator dungeonGenerator = new DungeonGenerator();
        dungeonGenerator.setRoomGenerationAttempts(500);
        dungeonGenerator.setMaxRoomSize(75);
        dungeonGenerator.setTolerance(10); // Max difference between width and height.
        dungeonGenerator.setMinRoomSize(9);
        dungeonGenerator.generate(this);
    }

    public static class NoiseStageInfo {
        public int radius = 32;
        public float modifier = 1f;
        public int seed = 0;
    }
}
