package com.arsskyfix.client;

import net.minecraft.core.Direction;

final class CubeFaces {

    static final float EPS = 0.002f;

    // same order as FACES/NORMALS: UP, DOWN, NORTH, SOUTH, WEST, EAST
    static final Direction[] DIRECTIONS = {
        Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    static final float[][][] FACES = {
        { {0, 1, 1}, {0, 1, 0}, {1, 1, 0}, {1, 1, 1} },
        { {0, 0, 0}, {0, 0, 1}, {1, 0, 1}, {1, 0, 0} },
        { {1, 0, 0}, {1, 1, 0}, {0, 1, 0}, {0, 0, 0} },
        { {0, 0, 1}, {0, 1, 1}, {1, 1, 1}, {1, 0, 1} },
        { {0, 0, 0}, {0, 1, 0}, {0, 1, 1}, {0, 0, 1} },
        { {1, 0, 1}, {1, 1, 1}, {1, 1, 0}, {1, 0, 0} },
    };

    static final float[][] NORMALS = {
        {0, 1, 0}, {0, -1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
    };

    private CubeFaces() {
    }
}
