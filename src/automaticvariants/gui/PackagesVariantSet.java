/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVSaveFile;
import skyproc.gui.LFormIDPicker;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class PackagesVariantSet extends WizSpecTemplate {

    LFormIDPicker seeds;

    public PackagesVariantSet(SPMainMenuPanel parent_) {
	super(parent_, "Variant Set Specs");
    }

    @Override
    protected void initialize() {
	super.initialize();
	seeds = new LFormIDPicker("Seed NPCs", AV.AVFont, AV.yellow);
	seeds.linkTo(AVSaveFile.Settings.SPEC_VAR_AUTHOR, AV.save, SUMGUI.helpPanel, true);
	setPlacement(seeds);
	Add(seeds);

	alignRight();
    }
}
