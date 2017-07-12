/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.PackageNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import lev.Ln;
import lev.gui.LList;
import lev.gui.LTextField;
import skyproc.gui.SPList;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class WizVariant extends WizTemplate {

    LTextField nameField;
    SPList<File> varTextures;

    public WizVariant(SPMainMenuPanel parent_) {
	super(parent_, "Create Variant", AV.packagesManagerPanel, AV.wizGroupPanel);
    }

    @Override
    protected void initialize() {
	super.initialize();

	setQuestionText("Name your new variant and add textures.");

	nameField = new LTextField("Variant Name", AV.AVFont, AV.yellow);
	nameField.putUnder(question, x, spacing);
	nameField.setSize(settingsPanel.getWidth() - 2 * x, 50);
	Add(nameField);

	varTextures = new SPList<>("Variant Textures", AV.AVFont, AV.yellow);
	varTextures.setUnique(true);
	varTextures.addEnterButton("Add Texture", new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		JFileChooser fd = new JFileChooser(lastQuery);
		fd.setMultiSelectionEnabled(true);
		File[] chosen = new File[0];
		if (fd.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		    lastQuery = fd.getSelectedFile().getParentFile();
		    chosen = fd.getSelectedFiles();
		}
		for (File f : chosen) {
		    if (Ln.isFileType(f, "DDS")) {
			varTextures.addElement(f);
		    }
		}
	    }
	});
	varTextures.putUnder(nameField, x, spacing);
	varTextures.setSize(settingsPanel.getWidth() - x * 2, 250);
	Add(varTextures);

	setNext(AV.wizVarSpecPanel);
    }

    @Override
    public void onOpen(SPMainMenuPanel parent) {
	if (AV.wizVarPanel.isVisible()) {
	    SUMGUI.helpPanel.setDefaultPos();
	    SUMGUI.helpPanel.setTitle("Create Variant");
	    SUMGUI.helpPanel.setContent("Add the textures that make this variant unique from the others.\n\n"
		    + textureNameWarning());
	    SUMGUI.helpPanel.hideArrow();
	    editing.load(WizNewPackage.newPackage.targetPackage, WizNewPackage.newPackage.targetSet, WizNewPackage.newPackage.targetGroup, null);
	    nameField.clearHighlight();
	    varTextures.clearHighlight();
	}
    }

    @Override
    public boolean testNext() {
	String trimmed = nameField.getText().trim();
	if (trimmed.equals("")) {
	    nameField.highlightChanged();
	    return false;
	} else {
	    nameField.clearHighlight();
	}
	for (PackageNode p : WizNewPackage.newPackage.targetGroup.getAll(PackageNode.Type.VAR)) {
	    if (p.src.getName().equalsIgnoreCase(trimmed)) {
		return false;
	    }
	}
	if (varTextures.getAll().isEmpty()) {
	    varTextures.highlightChanged();
	    return false;
	} else {
	    varTextures.clearHighlight();
	}
	return true;
    }

    public void reset() {
	nameField.setText("");
	varTextures.clear();
    }

    @Override
    public void onNext() {
	String trimmed = nameField.getText().trim();
	File f = new File(WizNewPackage.newPackage.targetGroup.src.getPath() + "\\" + trimmed);
	PackageNode packageNode = new PackageNode(f, PackageNode.Type.VAR);
	WizNewPackage.newPackage.targetVariant = packageNode;
	WizNewPackage.newPackage.varTextures = varTextures.getAll();

	AV.wizVarSpecPanel.open();
	AV.wizVarSpecPanel.setBack(AV.wizVarPanel);
	AV.wizVarSpecPanel.setNext(AV.wizAnother);
	AV.wizVarSpecPanel.editing.load(WizNewPackage.newPackage.targetPackage, WizNewPackage.newPackage.targetSet, WizNewPackage.newPackage.targetGroup, WizNewPackage.newPackage.targetVariant);
    }
}
