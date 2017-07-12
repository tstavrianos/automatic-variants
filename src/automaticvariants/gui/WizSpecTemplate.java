/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.PackageNode;
import automaticvariants.SpecFile;
import java.io.IOException;
import javax.swing.JOptionPane;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class WizSpecTemplate extends WizTemplate {

    SpecFile target;

    public WizSpecTemplate(SPMainMenuPanel parent_, String title) {
	super(parent_, title, AV.packagesManagerPanel, null);
    }

    @Override
    protected void initialize() {
	super.initialize();

	spacing = 12;

	setNext(AV.packagesManagerPanel);
    }

    @Override
    public void onNext() {
	if (!WizNewPackage.open) {
	    save();
	}
    }

    public void save() {
	if (target == null) {
	    return;
	}
	try {
	    SUMGUI.setPatchNeeded(true);
	    target.export();
	} catch (IOException ex) {
	    JOptionPane.showMessageDialog(null, "There was an error exporting the spec file, please contact Leviathan1753");
	}
    }

    public void load(PackageNode n) {
	editing.load(n);
    }
}
