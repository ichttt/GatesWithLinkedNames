package ichttt.mods.gateNameLink;

import ichttt.logicsimModLoader.init.LogicSimModLoader;
import logicsim.Gate;
import logicsim.TextLabel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionList {
    private static Map<Gate, TextLabel> textLabelHashMap = new HashMap<>();
    private static Map<Gate, GatePosCache> cacheMap = new HashMap<>();


    public static boolean hasGate(Gate gate) {
        return textLabelHashMap.keySet().stream().
                anyMatch(gate::equals);
    }

    public static void removeTextLabelIfPresent(TextLabel label) {
        ArrayList<Gate> gates = new ArrayList<>();
        textLabelHashMap.forEach((key, value) -> {
            if (value.equals(label)) gates.add(key);
        });
        for (Gate g : gates) {
            textLabelHashMap.remove(g);
        }
    }

    public static boolean hasGateChanged(Gate gate) {
        GatePosCache cache = cacheMap.get(gate);
        if (cache == null) {
            GateNameLink.getLogger().severe("Could not find cache for gate, creating new one!");
            cacheMap.put(gate, new GatePosCache(gate));
        }
        return cacheMap.get(gate).hasChanged();
    }

    public static TextLabel getTextLabel(Gate gate) {
        return textLabelHashMap.get(gate);
    }

    public static void addToMap(Gate gate, TextLabel label) {
        textLabelHashMap.put(gate, label);
        cacheMap.put(gate, new GatePosCache(gate));
    }

    public static void removeFromMap(Gate gate) {
        textLabelHashMap.remove(gate);
        cacheMap.remove(gate);
    }

    public static List<String> genLines() {
        List<String> lines = new ArrayList<>(textLabelHashMap.size()*2);
        lines.add(GateSaveHandler.SAVE_VER);
        textLabelHashMap.forEach((gate, textLabel) -> {
            lines.add(GateSaveHandler.GATE_IDENTIFIER + gate.x + GateSaveHandler.SPLITTER + gate.y);
            lines.add(GateSaveHandler.TEXT_IDENTIFIER + textLabel.x + GateSaveHandler.SPLITTER + textLabel.y + GateSaveHandler.SPLITTER + textLabel.text);
        });
        return lines;
    }

    public static void readLines(List<String> lines) {
        boolean expectGate = true;
        boolean skipNext = false;
        Map<Gate, TextLabel> map = new HashMap<>();
        List<Gate> loadedGates = LogicSimModLoader.getApp().lsframe.lspanel.gates.gates;
        Gate currentGate = null;
        TextLabel currentTextLabel;
        textLabelHashMap.clear();

        for (String line : lines) {
            if (skipNext) {
                skipNext = false;
                continue;
            }
            String split[] = line.split(GateSaveHandler.SPLITTER);
            int x = Integer.parseInt(split[0].substring(1));
            int y = Integer.parseInt(split[1]);
            if (line.startsWith(GateSaveHandler.GATE_IDENTIFIER)) {
                if (!expectGate) {
                    GateNameLink.getLogger().warning("Could not load save data. Reason: Got Gate while awaiting TextLabel");
                    return;
                }
                currentGate = getGate(loadedGates, x, y);
                if (currentGate == null) {
                    GateNameLink.getLogger().warning(String.format("Could not find Gate with posx %s and posy %s!", x, y));
                    skipNext = true;
                    continue;
                }
                expectGate = false;
            } else if (line.startsWith(GateSaveHandler.TEXT_IDENTIFIER)) {
                if (expectGate) {
                    GateNameLink.getLogger().warning("Could not load save data. Reason: Got TextLabel while awaiting Gate");
                    return;
                }
                currentTextLabel = getTextLabel(loadedGates, x, y, split[2]);
                if (currentTextLabel == null) {
                    GateNameLink.getLogger().warning(String.format("Could not find TextLabel with posx %s and posy %s and text %s!", x, y, split[2]));
                    continue;
                }
                map.put(currentGate, currentTextLabel);
                expectGate = true;
            }
        }
        textLabelHashMap = map;
        //rebuild cache
        cacheMap.clear();
        textLabelHashMap.keySet().forEach(gate -> cacheMap.put(gate, new GatePosCache(gate)));
    }

    @Nullable
    private static Gate getGate(List<Gate> loadedGates, int posx, int posy) {
        return loadedGates.stream().
                filter(gate -> gate.x == posx && gate.y == posy).
                findAny().
                orElse(null);
    }

    @Nullable
    private static TextLabel getTextLabel(List<Gate> loadedGates, int posx, int posy, String text) {
        return (TextLabel) loadedGates.stream().
                filter(gate -> gate instanceof TextLabel).
                filter(gate -> ((TextLabel) gate).text.equals(text) && gate.x == posx && gate.y == posy).
                findAny().
                orElse(null);
    }
}
