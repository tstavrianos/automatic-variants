/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVFileVars;
import automaticvariants.AVSaveFile;
import automaticvariants.AVSaveFile.Settings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import lev.gui.LButton;
import lev.gui.LCheckBox;
import lev.gui.LComboBox;
import skyproc.SPGlobal;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPSettingPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class PackagesOther extends SPSettingPanel {

    LButton gatherAndExit;
    LCheckBox origAsVar;
    LCheckBox movePackageFiles;
    LComboBox allowRegions;
    LButton packageManager;
    boolean onceForce = true;

    public PackagesOther(SPMainMenuPanel parent_) {
	super(parent_, "Texture Variants", AV.orange);
    }

    @Override
    protected void initialize() {
	super.initialize();

	origAsVar = new LCheckBox("Original As Variant", AV.AVFont, AV.yellow);
	origAsVar.setOffset(0);
	origAsVar.tie(AVSaveFile.Settings.PACKAGES_ORIG_AS_VAR, AV.save, SUMGUI.helpPanel, true);
	origAsVar.addShadow();
	setPlacement(origAsVar);
	Add(origAsVar);

	movePackageFiles = new LCheckBox("Move Package Files", AV.AVFont, AV.yellow);
	movePackageFiles.setOffset(0);
	movePackageFiles.tie(AVSaveFile.Settings.MOVE_PACKAGE_FILES, AV.save, SUMGUI.helpPanel, true);
	movePackageFiles.addShadow();
	setPlacement(movePackageFiles);
	Add(movePackageFiles);

	allowRegions = new LComboBox("Allow Regional Variants", AV.AVFont, AV.yellow);
	allowRegions.setSize(220, 60);
	allowRegions.addItem("Block Regions");
	allowRegions.addItem("Allow Regions");
	allowRegions.addItem("Allow Exclusive Regions");
	allowRegions.tie(AVSaveFile.Settings.PACKAGES_ALLOW_EXCLUSIVE_REGION, AV.save, SUMGUI.helpPanel, true);
	setPlacement(allowRegions);
	Add(allowRegions);

	gatherAndExit = new LButton("Gather Files and Exit");
	gatherAndExit.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		AVFileVars.gatherFiles();
		AV.gatheringAndExiting = true;
		if (SPGlobal.logging()) {
		    SPGlobal.logMain("AV", "Closing program early because of gather and exit command.");
		}
		SUMGUI.exitProgram(false, true);
	    }
	});
	gatherAndExit.linkTo(AVSaveFile.Settings.PACKAGES_GATHER, AV.save, SUMGUI.helpPanel, true);
	setPlacement(gatherAndExit);
	Add(gatherAndExit);

	packageManager = new LButton("Package Manager");
	packageManager.centerIn(settingsPanel, settingsPanel.getHeight() - packageManager.getHeight() - 15);
	settingsPanel.add(packageManager);

	alignRight();

    }

    void forceRepick() {
	if (onceForce) {
	    onceForce = false;
	    SUMGUI.setPatchNeeded(true);
	    AV.save.setInt(Settings.PACKAGES_FORCE_REPICK, AV.save.getInt(Settings.PACKAGES_FORCE_REPICK) + 1);
	}
    }

    @Override
    public void onOpen(SPMainMenuPanel parent_) {
	packageManager.addActionListener(AV.packagesManagerPanel.getOpenHandler());
    }
}
