package ichttt.mods.gateNameLink;

import ichttt.logicsimModLoader.gui.IModGuiInterface;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Tobias on 19.03.2017.
 */
public class GuiListEntry implements ActionListener, IModGuiInterface {
    public static GuiListEntry INSTANCE = new GuiListEntry();
    private GuiListEntry() {}
    JPanel panel;
    JCheckBox checkBox;
    @Override
    public void setup() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0));
        checkBox = new JCheckBox("Auto show on when a Gate has been created");
        checkBox.addActionListener(this);
        checkBox.setSelected(GateNameLink.getShow());
        checkBox.addActionListener(this);
        panel.add(checkBox);

    }

    @Nonnull
    @Override
    public JPanel draw() {
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GateNameLink.setShow(checkBox.isSelected());
    }
}
