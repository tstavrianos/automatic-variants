/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.*;
import automaticvariants.AVSaveFile.Settings;
import lev.gui.LTextField;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPStringList;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class WizSpecPackage extends WizSpecTemplate {

    LTextField packager;
    SPStringList origAuthors;
    LTextField release;
    LTextField update;

    public WizSpecPackage(SPMainMenuPanel parent_) {
	super(parent_, "Package Spec");
    }

    @Override
    protected void initialize() {
	super.initialize();

	packager = new LTextField("Packager", AV.AVFont, AV.yellow);
	packager.linkTo(Settings.SPEC_PACKAGE_PACKAGER, AV.save, SUMGUI.helpPanel, true);
	packager.setSize(settingsPanel.getWidth() - 2 * x, packager.getHeight());
	setPlacement(packager);
	Add(packager);

	origAuthors = new SPStringList("Original Content Authors", AV.AVFont, AV.yellow);
	origAuthors.setSize(settingsPanel.getWidth() - 2 * x, 250);
	origAuthors.linkTo(Settings.SPEC_PACKAGE_ORIGAUTHORS, AV.save, SUMGUI.helpPanel, true);
	setPlacement(origAuthors);
	Add(origAuthors);

	release = new LTextField("Release Date", AV.AVFont, AV.yellow);
	release.linkTo(Settings.SPEC_PACKAGE_RELEASE, AV.save, SUMGUI.helpPanel, true);
	release.setSize(settingsPanel.getWidth() - 2 * x, release.getHeight());
	setPlacement(release);
	Add(release);

	update = new LTextField("Last Updated", AV.AVFont, AV.yellow);
	update.linkTo(Settings.SPEC_PACKAGE_UPDATE, AV.save, SUMGUI.helpPanel, true);
	update.setSize(settingsPanel.getWidth() - 2 * x, update.getHeight());
	setPlacement(update);
	Add(update);

	alignRight();

    }

    @Override
    public void load(PackageNode n) {
	super.load(n);
	AVPackage p = (AVPackage) n;
	load(p.spec);
    }

    public void load(SpecPackage s) {

	packager.setText(s.Packager);
	for (String author : s.OriginalAuthors) {
	    origAuthors.addElement(author);
	}
	release.setText(s.Release_Date);
	update.setText(s.Last_Updated);

	target = s;
    }

    @Override
    public void onOpen(SPMainMenuPanel parent) {
	if (WizNewPackage.open) {
	    load(new SpecPackage(WizNewPackage.newPackage.targetPackage.src));
	}
    }

    @Override
    public void onNext() {
	super.onNext();
	if (WizNewPackage.open){
	    AV.wizSetPanel.open();
	    AV.wizSetPanel.setBack(AV.wizPackageSpecPanel);
	}
    }

    @Override
    public void onClose(SPMainMenuPanel parent) {
	origAuthors.clear();
    }

    @Override
    public void save() {
	if (target == null) {
	    return;
	}

	SpecPackage v = (SpecPackage) target;

	v.Packager = packager.getText();
	v.OriginalAuthors = origAuthors.getAll();
	v.Release_Date = release.getText();
	v.Last_Updated = update.getText();

	super.save();
    }
}