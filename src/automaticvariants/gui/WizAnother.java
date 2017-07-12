/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVFileVars;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import lev.gui.LButton;
import skyproc.gui.SPMainMenuPanel;

/**
 *
 * @author Justin Swanson
 */
public class WizAnother extends WizTemplate {

    LButton yes;
    LButton no;

    public WizAnother(SPMainMenuPanel parent_) {
	super(parent_, "Another Variant?", AV.packagesManagerPanel, null);
    }

    @Override
    protected void initialize() {
	super.initialize();

	spacing = 60;

	editing.setVisible(false);

	question.putUnder(header, question.getX(), 0);
	setQuestionText("Do you want to make another variant in the same group?");

	yes = new LButton("Yes");
	yes.centerOn(question, question.getBottom() + spacing);
	yes.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		AV.wizVarPanel.reset();
		AV.wizVarPanel.open();
	    }
	});
	Add(yes);

	no = new LButton("No - Done");
	no.centerOn(yes, yes.getBottom() + spacing);
	no.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		WizNewPackage.open = false;
		AV.packagesManagerPanel.open();
	    }
	});
	Add(no);

    }

    @Override
    public void onOpen(SPMainMenuPanel parent) {
	WizNewPackage.newPackage.save();
	AVFileVars.moveOut();
	PackagesManager.reloadPackageList();
    }
}
