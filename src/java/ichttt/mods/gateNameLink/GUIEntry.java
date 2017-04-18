package ichttt.mods.gateNameLink;

import ichttt.logicsimModLoader.gui.MenuBarHandler;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.util.LSMLUtil;
import logicsim.Gate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GUIEntry implements ActionListener {
    public static final GUIEntry INSTANCE = new GUIEntry();
    private JMenuItem bar;

    private GUIEntry() {
        bar = new JMenuItem(GateNameLink.translate("addLabel"));
    }

    public void init() {
        bar.addActionListener(this);
        MenuBarHandler.mods.add(bar);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        List<Gate> gates = LogicSimModLoader.getApp().lsframe.lspanel.gates.gates;
        for (Gate g : gates) {
            if (g.active) {
                if (ConnectionList.hasGate(g)) {
                    LSMLUtil.showMessageDialogOnWindowIfAvailable(GateNameLink.translate("hasAlreadyLabel"));
                    return;
                }
                GateNameLink.spawnNewTextField(g);
                return;
            }
        }
    }
}
