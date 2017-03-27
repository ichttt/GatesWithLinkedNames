package ichttt.mods.gateNameLink;

import ichttt.logicsimModLoader.api.ISaveHandler;
import ichttt.logicsimModLoader.internal.LSMLLog;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class GateSaveHandler implements ISaveHandler {
    public static final GateSaveHandler INSTANCE = new GateSaveHandler();
    private GateSaveHandler() {}
    List<String> lines = new ArrayList<>();
    public static final String GATE_IDENTIFIER = "G";
    public static final String TEXT_IDENTIFIER = "T";
    public static final String SPLITTER = ",";
    public static final String SAVE_VER = "ver1"; //Don't forget to bump if you change anything regarding the save format

    @Override
    public List<String> saveLines() {
        return ConnectionList.genLines();
    }

    @Override
    public void loadLines(List<String> list) {
        if (list.isEmpty()) {
            LSMLLog.warning("Save list is empty!");
            return;
        }
        if (!list.get(0).equals(SAVE_VER)) {
            LSMLLog.warning("Non matching save version!");
        }
        list.remove(0);
        ConnectionList.readLines(list);
    }
}
