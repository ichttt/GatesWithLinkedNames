package ichttt.mods.gateNameLink;

import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import logicsim.Gate;
import logicsim.TextLabel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionList {
    private static Map<Gate, TextLabel> textLabelHashMap = new HashMap<>();


    public static boolean hasGate(Gate gate) {
        return textLabelHashMap.keySet().stream().
                anyMatch(gate::equals);
    }

    public static TextLabel getTextLabel(Gate gate) {
        Gate uniqueGate = textLabelHashMap.keySet().stream().
                filter(gate::equals).
                findAny().
                orElse(null);
        if (uniqueGate == null) {
            return null;
        }
        return textLabelHashMap.get(uniqueGate);
    }

    public static void addToMap(Gate gate, TextLabel label) {
        textLabelHashMap.put(gate, label);
    }

    public static void removeFromMap(Gate gate) {
        textLabelHashMap.remove(gate);
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
        TextLabel currentTextLabel = null;
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
                    LSMLLog.warning("Could not load save data. Reason: Got Gate while awaiting TextLabel");
                    return;
                }
                currentGate = getGate(loadedGates, x, y);
                if (currentGate == null) {
                    LSMLLog.warning("Could not find Gate with posx %s and posy %s!", x, y);
                    skipNext = true;
                    continue;
                }
                expectGate = false;
            } else if (line.startsWith(GateSaveHandler.TEXT_IDENTIFIER)) {
                if (expectGate) {
                    LSMLLog.warning("Could not load save data. Reason: Got TextLabel while awaiting Gate");
                    return;
                }
                currentTextLabel = getTextLabel(loadedGates, x, y, split[2]);
                if (currentTextLabel == null) {
                    LSMLLog.warning("Could not find TextLabel with posx %s and posy %s and text %s!", x, y, split[2]);
                    continue;
                }
                map.put(currentGate, currentTextLabel);
                expectGate = true;
            }
        }
        textLabelHashMap = map;
    }

    @Nullable
    private static Gate getGate(List<Gate> loadedGates, int posx, int posy) {
        for (Gate gate : loadedGates) {
            if (gate.x == posx && gate.y == posy) {
                return gate;
            }
        }
        return null;
    }

    @Nullable
    private static TextLabel getTextLabel(List<Gate> loadedGates, int posx, int posy, String text) {
        for (Gate gate : loadedGates) {
            if (gate instanceof TextLabel) {
                if (((TextLabel) gate).text.equals(text) && gate.x == posx && gate.y == posy) {
                    return (TextLabel) gate;
                }
            }
        }
        return null;
    }
}
