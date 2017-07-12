/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVFileVars;
import automaticvariants.PackageNode;
import java.awt.event.*;
import java.io.File;
import javax.swing.SwingUtilities;
import lev.Ln;
import lev.gui.*;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPQuestionPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class WizSet extends WizTemplate {

    LComboSearchBox sets;
    LLabel or;
    LTextField newSetName;
    LLabel locateTargetSkins;
    LButton manualPick;
    LButton analyzeTexture;

    public WizSet(SPMainMenuPanel parent_) {
	super(parent_, "Target Set", AV.packagesManagerPanel, AV.wizPackagesPanel);
    }

    @Override
    protected void initialize() {
	super.initialize();

	setQuestionText("Please select the set you want to add variants to.");

	sets = new LComboSearchBox("Existing Set", AV.AVFont, AV.yellow);
	sets.setSize(settingsPanel.getWidth() - x * 2, fieldHeight);
	sets.putUnder(question, x, spacing + 5);
	sets.addEnterButton("Next", new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (!sets.isEmpty()) {
		    WizNewPackage.newPackage.targetSet = (PackageNode) sets.getSelectedItem();
		    AV.wizGenPanel.open();
		}
	    }
	});
	Add(sets);

	or = new LLabel("Or add a new set:", AV.AVFont, AV.green);
	or.addShadow();
	or.centerOn(sets, sets.getBottom() + 40);
	Add(or);

	newSetName = new LTextField("New Set Name", AV.AVFont, AV.yellow);
	newSetName.setSize(settingsPanel.getWidth() - 2 * x, newSetName.getHeight());
	newSetName.putUnder(or, x, 10);
	Add(newSetName);

	locateTargetSkins = new LLabel("Locate Target Skins:", AV.AVFont, AV.yellow);
	locateTargetSkins.putUnder(newSetName, x, spacing);
	locateTargetSkins.addShadow();
	Add(locateTargetSkins);

	analyzeTexture = new LButton("Use Tool to Locate Skins");
	analyzeTexture.setSize(200, 50);
	analyzeTexture.centerOn(or, locateTargetSkins.getBottom() + spacing);
	analyzeTexture.setFocusable(true);
	analyzeTexture.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (checkName()) {
		    AV.wizSetToolPanel.open();
		    AV.wizSetToolPanel.setNext(AV.wizGenPanel);
		    AV.wizSetToolPanel.reset();
		}
	    }
	});
	analyzeTexture.addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		SUMGUI.helpPanel.setTitle("Use Tool To Locate Skins");
		SUMGUI.helpPanel.setContent("This tool will take in your alternate texture files and "
			+ "process your mods to find ALL the skins that use them.  This will help you if "
			+ "you have no clue what skins are associated with the NPC you are making variants for.\n\n"
			+ "Also, this tool can help you locate when there are multiple skins you need to consider.");
		SUMGUI.helpPanel.focusOn(analyzeTexture, 0);
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(analyzeTexture);

	manualPick = new LButton("Pick Skins Manually");
	manualPick.setSize(200, 50);
	manualPick.centerOn(or, analyzeTexture.getBottom() + spacing);
	manualPick.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (checkName()) {
		    AV.wizSetManualPanel.open();
		    AV.wizSetManualPanel.resetAll();
		}
	    }
	});
	manualPick.addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		SUMGUI.helpPanel.setTitle("Pick Skins Manually");
		SUMGUI.helpPanel.setContent("If you know exactly the NPC skins you want to target,"
			+ " you can quickly choose them yourself here.");
		SUMGUI.helpPanel.focusOn(manualPick, 0);
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(manualPick);

    }

    boolean checkName() {
	String trimmed = newSetName.getText().trim();
	for (PackageNode p : WizNewPackage.newPackage.targetPackage.getAll(PackageNode.Type.VARSET)) {
	    if (p.src.getName().equalsIgnoreCase(trimmed)) {
		trimmed = "";
		break;
	    }
	}
	if (!trimmed.equals("")) {
	    File f = new File(WizNewPackage.newPackage.targetPackage.src.getPath() + "\\" + trimmed);
	    PackageNode packageNode = new PackageNode(f, PackageNode.Type.VARSET);
	    WizNewPackage.newPackage.targetSet = packageNode;
	    return true;
	}
	newSetName.highlightChanged();
	return false;
    }

    @Override
    public void onOpen(SPMainMenuPanel parent_) {
	mainHelp();
	editing.load(WizNewPackage.newPackage.targetPackage
		, null
		, null
		, null);
	newSetName.clearHighlight();
    }

    public void reset() {
	sets.reset();
	newSetName.setText("");
	loadSets();
    }

    void mainHelp() {
	if (AV.wizSetPanel.isVisible()) {
	    SUMGUI.helpPanel.setDefaultPos();
	    SUMGUI.helpPanel.setTitle("Variant Set");
	    SUMGUI.helpPanel.setContent("A Variant Set contains variants that all target the same NPC skin(s).\n\n"
		    + "Name your set after the Actor you are making variants for. (Wolf, Giant, Rabbit, etc)\n\n"
		    + "To complete a Variant Set, you must select the Variant Profiles that it should target.  \n"
		    + profileDesc() + "\n\nYou can do this manually,"
		    + " or you can use the supplied AV tool if you don't know the profiles that apply to your textures.");
	    SUMGUI.helpPanel.hideArrow();
	}
    }

    public void loadSets() {
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		sets.removeAllItems();
	    }
	});
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		for (PackageNode p : WizNewPackage.newPackage.targetPackage.getAll(PackageNode.Type.VARSET)) {
		    sets.addItem(p);
		}
	    }
	});
	sets.setSelectedIndex(0);
    }
}
