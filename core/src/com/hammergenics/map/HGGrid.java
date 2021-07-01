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

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.cellular.CellularAutomataGenerator;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.hammergenics.HGEngine.MAP_CENTER;
import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGGrid extends Grid {
    // these are to trick NoiseGenerator to generate the noise at the custom position (x0,z0)
    public int x0, z0;

    public float min;
    public float max;
    public float mid;
    public float yScale = 1f;
    public float step = -1f;
    Array<NoiseStageInfo> noiseStages = new Array<>(true, 16, NoiseStageInfo.class);

    public HGGrid(int size) { this(size, 0, 0); }

    public HGGrid(int size, int x0, int z0) {
        super(size);
        this.x0 = x0;
        this.z0 = z0;
    }

    // the overrides below are to trick NoiseGenerator to generate the noise at the custom position (x0,z0)
    @Override
    public float get(int x, int y) { return super.get(x0 + x, z0 + y); }

    @Override
    public float set(int x, int y, float value) { return super.set(x0 + x, z0 + y, value); }

    @Override
    public boolean isIndexValid(int x, int y) { return super.isIndexValid(x - x0, y - z0); }

    @Override
    public int toIndex(int x, int y) { return super.toIndex(x - x0, y - z0); }

    @Override
    public int toX(int index) { return super.toX(index) + x0; }

    @Override
    public int toY(int index) { return super.toY(index) + z0; }

    public int getX0() { return getX0(true); }
    public int getX0(boolean adjCenter) { return adjCenter ? x0 - MAP_CENTER : x0; }
    public int getZ0() { return getZ0(true); }
    public int getZ0(boolean adjCenter) { return adjCenter ? z0 - MAP_CENTER : z0; }

    public static Rectangle getCombinedBounds(Array<HGGrid> grids, Rectangle out) {
        if (out == null) { return null; }
        int x0 = Integer.MAX_VALUE; int z0 = Integer.MAX_VALUE;
        int x1 = Integer.MIN_VALUE; int z1 = Integer.MIN_VALUE;
        for (HGGrid grid: grids) {
            if (x0 > grid.getX0()) { x0 = grid.getX0(); }
            if (z0 > grid.getZ0()) { z0 = grid.getZ0(); }

            if (x1 < grid.getX0() + grid.getWidth()) { x1 = grid.getX0() + grid.getWidth(); }
            if (z1 < grid.getZ0() + grid.getHeight()) { z1 = grid.getZ0() + grid.getHeight(); }
        }

        return out.set(x0, z0, x1 - x0, z1 - z0);
    }

    public void calculateMinMaxMid() {
        float[] values = new float[getArray().length];
        System.arraycopy(getArray(), 0, values, 0, values.length);
        Arrays.sort(values, 0, values.length);
        min = values[0];
        max = values[values.length - 1];
        int scale = new BigDecimal(Float.toString(min)).scale();
        mid = BigDecimal.valueOf((max + min)/2f).setScale(scale, ROUND_HALF_UP).floatValue();
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

        calculateMinMaxMid();
    }

    public void roundToStep(float step) {
        if (step >= 1f || step <= 0f) { return; }
        this.step = step;
        int scale = new BigDecimal(Float.toString(step)).scale();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                float value = get(x, y);
                float mod = value % step;
                value -= mod;
                if (mod > step/2f) { value += step; }
                value = BigDecimal.valueOf(value).setScale(scale, ROUND_HALF_UP).floatValue();
                set(x, y, value);
            }
        }

        calculateMinMaxMid();
    }

    // see: https://github.com/czyzby/noise4j
    public void generateNoise(float yScale, Array<NoiseStageInfo> stages) {
        if (stages.size == 0) { return; }
        final NoiseGenerator noiseGenerator = new NoiseGenerator();

        fill(0f);
        this.yScale = yScale;
        this.step = -1f;

        noiseStages.clear();
        noiseStages.addAll(stages);

        for (NoiseStageInfo stage: stages) {
            if (stage.seed < 0) { stage.seed = Generators.rollSeed(); }
            noiseStage(this, noiseGenerator, stage.radius, stage.modifier, stage.seed);
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
        public int seed = -1;
    }
}
