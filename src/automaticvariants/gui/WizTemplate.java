/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import java.io.File;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPQuestionPanel;
import skyproc.gui.SPSettingPanel;

/**
 *
 * @author Justin Swanson
 */
public class WizTemplate extends SPQuestionPanel {

    int x = 15;
    int fieldHeight = 65;
    PackageEditing editing;
    static File lastQuery = new File(".");

    public WizTemplate(SPMainMenuPanel parent_, String title, SPSettingPanel cancel, SPSettingPanel back) {
	super(parent_, title, AV.orange, cancel, back);
    }

    @Override
    protected void initialize() {
	super.initialize();

	spacing = 25;

	editing = new PackageEditing(settingsPanel);
	editing.setLocation(0, header.getBottom());
	settingsPanel.add(editing);

	question.putUnder(editing, question.getX(), 0);
	setQuestionFont(AV.AVFont);
	setQuestionCentered();
	setQuestionColor(AV.green);

    }

    @Override
    public void onCancel() {
	WizNewPackage.newPackage.clear();
	WizNewPackage.open = false;
    }

    public static String textureNameWarning() {
	return "NOTE:\n"
		+ "The texture file names MUST match the file names of the original texture they are replacing.  \n"
		    + "For example, if the original texture is \"wolf.dds\" and you rename it \"wolf_black.dds\", it will not process correctly.";
    }

    public static String profileDesc() {
	return "Profiles are a unique combination of: \n      Race\n   + Skin (Armor)\n   + Armor Piece (ArmorAddon)\n that combine to give each NPC their look.";
    }

    public static String chosenProfiles() {
	return "This is the list of profiles that this Variant Set will be applied to.";
    }

}
