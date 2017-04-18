package ichttt.mods.gateNameLink;

import logicsim.Gate;

/**
 * Basic stuff
 */
public class GatePosCache {
    public final Gate gate;
    private int posX, posY;

    public GatePosCache(Gate gate) {
        this.gate = gate;
        this.posX = gate.x;
        this.posY = gate.y;
    }

    public boolean hasChanged() {
        if (gate.x == posX && gate.y == posY)
            return false;
        posX = gate.x;
        posY = gate.y;
        return true;
    }
}
