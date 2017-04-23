package ichttt.mods.gateNameLink;

import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.event.GateEvent;
import ichttt.logicsimModLoader.event.GlobalDrawEvent;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import jdk.internal.org.objectweb.asm.tree.LookupSwitchInsnNode;
import logicsim.Gate;
import logicsim.GateList;
import logicsim.TextLabel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias on 18.04.2017.
 */
public class GateTracker {

    private static List<Gate> additionalGates = new ArrayList<>();

    @Subscribe
    public void onGateCreate(GateEvent.GateGUICreationEvent event) {
        if (!GateNameLink.getShow() || event.gate instanceof TextLabel)
            return;
        GateNameLink.spawnNewTextField(event.gate);
        additionalGates.add(event.gate);
    }

    @Subscribe
    public void onGateRedraw(GlobalDrawEvent event) {
        GateList gateList = LogicSimModLoader.getApp().lsframe.lspanel.gates;
        List<Gate> gates = new ArrayList<>(gateList.gates);
        gates.addAll(additionalGates);
        for (Gate gate : gates) {
            if (gate instanceof TextLabel) continue;
            if (gateList.gates.contains(gate) && additionalGates.contains(gate))
                additionalGates.remove(gate);
            if (ConnectionList.hasGate(gate)) {
                TextLabel label = ConnectionList.getTextLabel(gate);
                Graphics graphics = event.graphics;
                if (ConnectionList.hasGateChanged(gate)) {
                    GateNameLink.relink(gate, label);
                    LogicSimModLoader.getApp().lsframe.repaint();
                }
                Color current = graphics.getColor();
                graphics.setColor(new Color(0x159B00));
                graphics.drawLine(gate.x + 25, gate.y, label.x, label.y);
                graphics.setColor(current);
            }
        }
    }

    @Subscribe
    public void onGateDelete(GateEvent.GateDeleteEvent event) {
        if (ConnectionList.hasGate(event.gate)) {
            List<Gate> gates = LogicSimModLoader.getApp().lsframe.lspanel.gates.gates;
            TextLabel toRemove = ConnectionList.getTextLabel(event.gate);
            for (int i = 0; i < gates.size(); i++) {
                if (gates.get(i).equals(toRemove)) {
                    LogicSimModLoader.getApp().lsframe.lspanel.gates.remove(i); //Remove the TextLabel
                    break;
                }
            }
            ConnectionList.removeFromMap(event.gate);
        }
    }
}
