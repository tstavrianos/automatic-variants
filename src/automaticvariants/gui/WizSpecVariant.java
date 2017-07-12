/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.*;
import automaticvariants.AVSaveFile.Settings;
import java.util.ArrayList;
import lev.gui.*;
import skyproc.FormID;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPStringList;
import skyproc.gui.SUMGUI;

/**
 *
 * @Author Justin Swanson
 */
public class WizSpecVariant extends WizSpecTemplate {

    LTextField author;
    LNumericSetting probDiv;
    SPStringList region;
    LCheckBox exclusiveRegion;
    LNumericSetting height;
    LNumericSetting health;
    LNumericSetting magicka;
    LNumericSetting stamina;
    LNumericSetting speed;
    LTextField namePrefix;
    LTextField nameAffix;

    public WizSpecVariant(SPMainMenuPanel parent_) {
	super(parent_, "Variant Spec");
    }

    @Override
    protected void initialize() {
	super.initialize();

	author = new LTextField("Author", AV.AVFont, AV.yellow);
	author.linkTo(Settings.SPEC_VAR_AUTHOR, AV.save, SUMGUI.helpPanel, true);
	setPlacement(author);
	Add(author);

	probDiv = new LNumericSetting("Probability Divider", AV.AVFont, AV.yellow, 1, 99, 1);
	probDiv.linkTo(Settings.SPEC_VAR_PROB, AV.save, SUMGUI.helpPanel, true);
	setPlacement(probDiv);
	Add(probDiv);

	region = new SPStringList("Regions To Spawn In", AV.AVFont, AV.yellow);
	region.setSize(settingsPanel.getWidth() - 2 * x, 200);
	region.linkTo(Settings.SPEC_VAR_REGION, AV.save, SUMGUI.helpPanel, true);
	setPlacement(region);
	Add(region);

	exclusiveRegion = new LCheckBox("Exclusive Region", AV.AVFont, AV.yellow);
	exclusiveRegion.linkTo(Settings.SPEC_VAR_REGION_EXCLUDE, AV.save, SUMGUI.helpPanel, true);
	exclusiveRegion.addShadow();
	exclusiveRegion.setOffset(2);
	setPlacement(exclusiveRegion);
	Add(exclusiveRegion);

//	height = new LNumericSetting("Relative Height", AV.AVFont, AV.yellow, 1, 1000, 1);
//	height.linkTo(Settings.SPEC_VAR_HEIGHT, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(height);
//	Add(height);
//
//	health = new LNumericSetting("Relative Health", AV.AVFont, AV.yellow, 1, 1000, 1);
//	health.linkTo(Settings.SPEC_VAR_HEALTH, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(health);
//	Add(health);
//
//	magicka = new LNumericSetting("Relative Magicka", AV.AVFont, AV.yellow, 1, 1000, 1);
//	magicka.linkTo(Settings.SPEC_VAR_MAGICKA, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(magicka);
//	Add(magicka);
//
//	stamina = new LNumericSetting("Relative Stamina", AV.AVFont, AV.yellow, 1, 1000, 1);
//	stamina.linkTo(Settings.SPEC_VAR_STAMINA, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(stamina);
//	Add(stamina);
//
//	speed = new LNumericSetting("Relative Speed", AV.AVFont, AV.yellow, 1, 1000, 1);
//	speed.linkTo(Settings.SPEC_VAR_SPEED, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(speed);
//	Add(speed);
//
//	namePrefix = new LTextField("Name Prefix", AV.AVFont, AV.yellow);
//	namePrefix.linkTo(Settings.SPEC_VAR_NAME_PREFIX, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(namePrefix);
//	    Add(namePrefix);

//	nameAffix = new LTextField("Name Affix", AV.AVFont, AV.yellow);
//	nameAffix.linkTo(Settings.SPEC_VAR_NAME_AFFIX, AV.save, SUMGUI.helpPanel, true);
//	setPlacement(nameAffix);
//	    Add(nameAffix);

	alignRight();

    }

    @Override
    public void load(PackageNode n) {
	super.load(n);
	Variant v = (Variant) n;
	load(v.spec);
    }

    public void load(SpecVariant s) {

	author.setText(s.Author);

	probDiv.setValue(s.Probability_Divider);

	region.clear();
	for (String[] a : s.Region_Include) {
	    if (a.length == 2) {
		region.addElement(a[0] + a[1]);
	    }
	}

	exclusiveRegion.setSelected(s.Exclusive_Region);

//	health.setValue(s.Health);
//	magicka.setValue(s.Magicka);
//	stamina.setValue(s.Stamina);
//	speed.setValue(s.Speed);
//	height.setValue(s.Height);
//
//	namePrefix.setText(s.Name_Prefix);
//
//	nameAffix.setText(s.Name_Affix);

	target = s;
    }

    @Override
    public void onOpen(SPMainMenuPanel parent) {
	if (WizNewPackage.open) {
	    WizNewPackage pack = WizNewPackage.newPackage;
	    load(new SpecVariant(WizNewPackage.newPackage.targetVariant.src));
	}
    }

    @Override
    public void save() {
	if (target == null) {
	    return;
	}

	SpecVariant v = (SpecVariant) target;

	v.Author = author.getText();
	v.Probability_Divider = probDiv.getValue();
	ArrayList<String> regionsList = region.getAll();
	ArrayList<String[]> out = new ArrayList<>(regionsList.size());
	for (int i = 0; i < regionsList.size(); i++) {
	    String id = regionsList.get(i);
	    if (id.length() > 6) {
		out.add(FormID.parseString(id));
	    }
	}
	String[][] stringarray = new String[0][];
	v.Region_Include = out.toArray(stringarray);
	v.Exclusive_Region = this.exclusiveRegion.isSelected();
//	v.Health = health.getValue();
//	v.Magicka = magicka.getValue();
//	v.Stamina = stamina.getValue();
//	v.Speed = speed.getValue();
//	v.Height = height.getValue();
//	v.Name_Prefix = this.namePrefix.getText();
//	v.Name_Affix = this.nameAffix.getText();

	super.save();
    }
}
