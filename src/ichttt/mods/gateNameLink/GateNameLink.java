package ichttt.mods.gateNameLink;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.event.GateEvent;
import ichttt.logicsimModLoader.event.loading.LSMLPostInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.exceptions.MissingDependencyException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;
import logicsim.App;
import logicsim.Gate;
import logicsim.TextLabel;

import javax.swing.*;
import java.util.List;

@Mod(modid = GateNameLink.MODID, modName = "Gate Name Linkage", version = "1.0.0")
public class GateNameLink {
    public static final String MODID = "GateNameLink";
    private static boolean show;
    private static Config config;
    private static ConfigCategory general;

    public static void main(String[] args) throws Exception{
        Loader.getInstance().addMod(GateNameLink.class);
        LogicSimModLoader.startFromDev();
    }

    @Subscribe
    public void onRegistration(LSMLRegistrationEvent event) {
        ModContainer myModContainer = Loader.getInstance().getModContainerForModID(MODID);
        Preconditions.checkNotNull(myModContainer);
        event.registerSaveHandler(myModContainer.mod, GateSaveHandler.INSTANCE);
        event.registerModGui(myModContainer.mod, GuiListEntry.INSTANCE);
        if (!LogicSimModLoader.LSML_VERSION.isMinimum(new VersionBase(0,0,3)))
            throw new MissingDependencyException(myModContainer.mod, "LogicSimModLoader", new VersionBase(0,1,1));
    }

    @Subscribe
    public void onPreInit(LSMLPreInitEvent event) {
        ModContainer myModContainer = Loader.getInstance().getModContainerForModID(MODID);
        Preconditions.checkNotNull(myModContainer);
        config = new Config(myModContainer);
        general = new ConfigCategory("GENERAL");
        general.addEntry(new BooleanConfigEntry("showOnGateCreate", true, "Determines if a label should be giving if you create a new Gate"));
        config.addCategory(general);
        config.load();
        config.save();
        show = ((BooleanConfigEntry) general.getConfigEntry("showOnGateCreate")).value;
    }

    public static void setShow(boolean value) {
        show = value;
        ((BooleanConfigEntry) general.getConfigEntry("showOnGateCreate")).value = value;
        config.save();
    }

    public static boolean getShow() {
        return show;
    }


    @Subscribe
    public void onPostInit(LSMLPostInitEvent event) {
        GUIEntry.INSTANCE.init();
    }

    @Subscribe
    public void onGateCreate(GateEvent.GateConstructionEvent event) {
        if (!show || event.gate instanceof TextLabel)
            return;
        spawnNewTextField(event.gate);
    }

    public static void spawnNewTextField(Gate source) {
        App app = LogicSimModLoader.getApp();
        Preconditions.checkNotNull(app);
        String gateName = JOptionPane.showInputDialog(app.frame, "Please enter the name of the Gate!");
        if (Strings.isNullOrEmpty(gateName))
            return;
        TextLabel label = new TextLabel();
        label.text = gateName;
        addTextField(source, label);
    }

    public static void addTextField(Gate source, TextLabel label) {
        App app = LogicSimModLoader.getApp();
        relink(source, label);
        app.lsframe.lspanel.gates.addGate(label);
        ConnectionList.addToMap(source, label);
        app.frame.repaint();
    }

    public static void relink(Gate gate, TextLabel label) {
        label.x = gate.x + 50;
        label.y = gate.y;
    }

    @Subscribe
    public void onGateRedraw(GateEvent.GateDrawEvent event) {
        Gate gate = event.gate;
        if (gate instanceof TextLabel)
            return;
        if (ConnectionList.hasGate(gate)) {
            GateNameLink.relink(gate, ConnectionList.getTextLabel(gate));
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
