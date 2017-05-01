package ichttt.mods.gateNameLink;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.config.entry.IntConfigEntry;
import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.event.loading.LSMLPostInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.exceptions.MissingDependencyException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.util.I18nHelper;
import logicsim.App;
import logicsim.Gate;
import logicsim.TextLabel;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mod(modid = GateNameLink.MODID, modName = "Gates with linked names", version = "1.1.3", author = "Tobias Hotz")
public class GateNameLink {

    public static final String MODID = "GateNameLink";
    private static BooleanConfigEntry show;
    private static Config config;
    private static Logger logger;
    private static I18nHelper i18n;
    private static IntConfigEntry offsetX, offsetY;

    public static Logger getLogger() {
        return logger;
    }

    public static String translate(String s) {
        return i18n.translate(s);
    }

    public static void main(String[] args) {
        Loader.getInstance().addMod(GateNameLink.class);
        LogicSimModLoader.startFromDev();
    }

    @Subscribe
    public void onRegistration(LSMLRegistrationEvent event) {
        logger = LSMLLog.getCustomLogger(MODID);
        ModContainer myModContainer = Loader.getInstance().getModContainerForModID(MODID);
        Preconditions.checkNotNull(myModContainer);
        event.registerSaveHandler(myModContainer.mod, GateSaveHandler.INSTANCE);
        event.registerModGui(myModContainer.mod, ModPanelEntry.INSTANCE);
        try {
            event.checkForUpdate(new UpdateContext(myModContainer, new URL("https://raw.githubusercontent.com/ichttt/GatesWithLinkedNames/master/updateinfo.txt")).
                    withWebsite(new URL("https://github.com/ichttt/GatesWithLinkedNames")).
                    enableAutoUpdate(new URL("https://github.com/ichttt/GatesWithLinkedNames/blob/master/GateList.jar?raw=true"),
                            new URL("https://raw.githubusercontent.com/ichttt/GatesWithLinkedNames/master/GateList.modinfo")));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not register UpdateChecker", e);
        }
        VersionBase required_LSML = new VersionBase(0, 2, 2);
        if (!LogicSimModLoader.LSML_VERSION.isMinimum(required_LSML))
            throw new MissingDependencyException(myModContainer.mod, "LogicSimModLoader", required_LSML);

    }

    @Subscribe
    public void onPreInit(LSMLPreInitEvent event) {
        ModContainer myModContainer = Loader.getInstance().getModContainerForModID(MODID);
        Preconditions.checkNotNull(myModContainer);
        i18n = new I18nHelper(MODID);

        //Setup config
        config = new Config(myModContainer);
        ConfigCategory general = new ConfigCategory("GENERAL");
        ConfigCategory offsets = new ConfigCategory("Offsets");
        show = new BooleanConfigEntry("showOnGateCreate", true, "Determines if a label should be giving if you create a new Gate");
        List<String> comments = new ArrayList<>(2);
        comments.add("The offset to X");
        comments.add("Positive means to the right, negative to the left");
        offsetX = new IntConfigEntry(true, "offsetX", 55, new ArrayList<>(comments));
        comments.set(1, "Positive means up, negative down");
        offsetY = new IntConfigEntry(true, "offsetY", 0, comments);
        general.addEntry(show);
        offsets.addEntry(offsetX);
        offsets.addEntry(offsetY);
        config.addCategory(general);
        config.addCategory(offsets);
        config.load();
        config.save();

        LSMLEventBus.EVENT_BUS.register(new GateTracker());
    }

    @Subscribe
    public void onPostInit(LSMLPostInitEvent event) {
        GUIEntry.INSTANCE.init();
    }

    public static void setShow(boolean value) {
        show.value = value;
        config.save();
    }

    public static boolean getShow() {
        return show.value;
    }

    public static void relinkAllAndRepaint() {
        List<Gate> list = LogicSimModLoader.getApp().lsframe.lspanel.gates.gates;
        for (Gate g : list) {
            if (ConnectionList.hasGate(g)) {
                relink(g, ConnectionList.getTextLabel(g));
            }
        }
        LogicSimModLoader.getApp().lsframe.lspanel.repaint();
    }

    public static void setOffsetX(int value) {
        if (value == offsetX.value)
            return;
        offsetX.value = value;
        config.save();
        relinkAllAndRepaint();
    }

    public static int getOffsetX() {
        return offsetX.value;
    }

    public static void setOffsetY(int value) {
        if (value == offsetY.value)
            return;
        offsetY.value = value;
        config.save();
        relinkAllAndRepaint();
    }

    public static int getOffsetY() {
        return offsetY.value;
    }

    public static void spawnNewTextField(Gate source) {
        App app = LogicSimModLoader.getApp();
        Preconditions.checkNotNull(app);
        String gateName = JOptionPane.showInputDialog(app.frame, translate("enterName"));
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
        label.x = gate.x + offsetX.value;
        label.y = gate.y + offsetY.value;
    }

}
