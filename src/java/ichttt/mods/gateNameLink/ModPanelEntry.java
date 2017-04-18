package ichttt.mods.gateNameLink;

import ichttt.logicsimModLoader.gui.IModGuiInterface;
import ichttt.logicsimModLoader.util.I18nHelper;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Tobias on 19.03.2017.
 */
public class ModPanelEntry implements ActionListener, IModGuiInterface, FocusListener {
    public static ModPanelEntry INSTANCE = new ModPanelEntry();
    private ModPanelEntry() {}
    JPanel panel;
    JCheckBox checkBox;
    JTextField offsetX, offsetY;
    JLabel offsetXLabel, offsetYLabel;

    @Override
    public void setup() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        checkBox = new JCheckBox(GateNameLink.translate("autoShowdesc"));
        checkBox.setSelected(GateNameLink.getShow());
        checkBox.addActionListener(event -> GateNameLink.setShow(checkBox.isSelected()));
        offsetX = new JTextField();
        offsetX.setActionCommand("X");
        offsetX.addActionListener(this);
        offsetX.addFocusListener(this);
        offsetY = new JTextField();
        offsetY.setActionCommand("Y");
        offsetY.addActionListener(this);
        offsetY.addFocusListener(this);
        offsetXLabel = new JLabel(GateNameLink.translate("offsetXdesc"));
        offsetYLabel = new JLabel(GateNameLink.translate("offsetYdesc"));
        panel.add(checkBox);
        panel.add(new JLabel()); //What a dirty hack...
        panel.add(offsetXLabel);
        panel.add(offsetX);
        panel.add(offsetYLabel);
        panel.add(offsetY);
        updateText();
    }

    @Nonnull
    @Override
    public JPanel draw() {
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        processEvent((JTextField) event.getSource());
    }

    private void processEvent(JTextField label) {
        int newValue;
        try {
            newValue = Integer.parseInt(label.getText());
        } catch (NumberFormatException e) {
            GateNameLink.getLogger().fine("Got invalid String " + label.getText());
            updateText();
            return;
        }

        if (label == offsetX)
            GateNameLink.setOffsetX(newValue);
        else if (label == offsetY)
            GateNameLink.setOffsetY(newValue);
        else
            GateNameLink.getLogger().warning("Unknown label " + label.toString());
    }

    @Override
    public void focusGained(FocusEvent ignored) {
        updateText();
    }

    private void updateText() {
        offsetX.setText(GateNameLink.getOffsetX() + "");
        offsetY.setText(GateNameLink.getOffsetY() + "");
    }

    @Override
    public void focusLost(FocusEvent event) {
        processEvent((JTextField) event.getSource());
    }
}
