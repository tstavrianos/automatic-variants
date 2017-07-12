/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import lev.gui.LButton;
import lev.gui.LImagePane;
import skyproc.SPGlobal;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPSettingPanel;

/**
 *
 * @author Justin Swanson
 */
public class WelcomePage extends SPSettingPanel {

    LImagePane picture;
    
    public WelcomePage(SPMainMenuPanel parent_) {
	super(parent_, "", AV.orange);
    }

    @Override
    protected void initialize() {
	super.initialize();

	try {
	    picture = new LImagePane(WelcomePage.class.getResource("AV welcome.png"));
	    settingsPanel.add(picture);
	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	}

    }

    @Override
    public void onOpen(SPMainMenuPanel parent_) {
    }
}
