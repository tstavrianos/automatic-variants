/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVSaveFile;
import automaticvariants.AVSaveFile.Settings;
import lev.gui.LCheckBox;
import lev.gui.LComboBox;
import lev.gui.LLabel;
import lev.gui.LNumericSetting;
import skyproc.SPGlobal;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPSettingDefaultsPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class SettingsOther extends SPSettingDefaultsPanel {

    LCheckBox importOnStartup;
    LLabel papyrusDebug;
    LCheckBox debugOn;
    LCheckBox debugRegional;

    public SettingsOther(SPMainMenuPanel parent_) {
	super(parent_, "Other Settings", AV.orange, AV.save);
    }

    @Override
    protected void initialize() {
	super.initialize();

	importOnStartup = new LCheckBox("Import Mods on Startup", AV.AVFont, AV.yellow);
	importOnStartup.tie(AVSaveFile.Settings.IMPORT_AT_START, AV.save, SUMGUI.helpPanel, true);
	importOnStartup.setOffset(2);
	importOnStartup.addShadow();
	setPlacement(importOnStartup);
	AddSetting(importOnStartup);

	last.y += 20;
	papyrusDebug = new LLabel("Papyrus Debug Options", AV.AVFont, AV.yellow);
	papyrusDebug.addShadow();
	setPlacement(papyrusDebug);
	Add(papyrusDebug);

	debugOn = new LCheckBox("Master Debug Switch", AV.AVFont, AV.yellow);
	debugOn.tie(AVSaveFile.Settings.DEBUG_ON, AV.save, SUMGUI.helpPanel, true);
	debugOn.setOffset(2);
	debugOn.addShadow();
	setPlacement(debugOn);
	AddSetting(debugOn);

	debugRegional = new LCheckBox("Regional Debug", AV.AVFont, AV.yellow);
	debugRegional.tie(AVSaveFile.Settings.DEBUG_REGIONAL, AV.save, SUMGUI.helpPanel, true);
	debugRegional.setOffset(2);
	debugRegional.addShadow();
	setPlacement(debugRegional);
	AddSetting(debugRegional);

	alignRight();

    }
}
