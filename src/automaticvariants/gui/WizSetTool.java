/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVFileVars;
import automaticvariants.AVSaveFile.Settings;
import automaticvariants.VariantProfileNPC;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import lev.Ln;
import lev.gui.*;
import skyproc.*;
import skyproc.gui.SPList;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPProgressBarPlug;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class WizSetTool extends WizTemplate {

    SPList<File> textures;
    LButton analyze;
    LCheckBox partialMatch;
    LLabel progressLabel;
    LProgressBar progress;
    LTextPane exception;
    SPList<ProfileDisplay> potentialProfiles;
    SPList<ProfileDisplay> targetProfiles;
    Set<ProfileDisplay> matchingProfiles;
    Set<ProfileDisplay> halfMatchingProfiles;
    static int attempt = 1;

    public WizSetTool(SPMainMenuPanel parent_) {
	super(parent_, "Target Profiles", AV.packagesManagerPanel, AV.wizSetPanel);
    }

    @Override
    protected void initialize() {
	super.initialize();

	spacing = 40;

	setQuestionText("Analyze textures to narrow down profile options.");

	textures = new SPList<>("Textures to Analyze", AV.AVFont, AV.yellow);
	textures.setUnique(true);
	textures.addEnterButton("Add Texture", new ActionListener() {

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
			textures.addElement(f);
		    }
		}
	    }
	});
	textures.putUnder(question, x, spacing);
	textures.setSize(settingsPanel.getWidth() - x * 2, 250);
	Add(textures);

	analyze = new LButton("Analyze");
	analyze.setSize(100, analyze.getHeight());
	analyze.centerOn(textures, textures.getBottom() + 15);
	analyze.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (!textures.isEmpty()) {
		    displaySwitch(1);
		    SUMGUI.startImport(new Analyze(textures.getAll()));
		} else {
		    textures.highlightChanged();
		}
	    }
	});
	Add(analyze);
	progressLabel = new LLabel("Loading up mods, please wait...", AV.AVFontSmall, AV.lightGray);
	progressLabel.centerIn(settingsPanel, question.getBottom() + spacing * 3);
	Add(progressLabel);

	progress = new LProgressBar(200, 20, AV.AVFontSmall, AV.lightGray);
	progress.centerIn(settingsPanel, progressLabel.getBottom() + 10);
	SPProgressBarPlug.addProgressBar(progress);
	Add(progress);

	exception = new LTextPane(settingsPanel.getWidth() - 30, 300, AV.yellow);
	exception.setText("An exception has occured.  Please submit a debug report to Leviathan.");
	exception.putUnder(question, 15, 50);
	Add(exception);

	potentialProfiles = new SPList<>("Matching Profiles", AV.AVFont, AV.yellow);
	potentialProfiles.putUnder(question, x, 15);
	potentialProfiles.setSize(settingsPanel.getWidth() - x * 2, 220);
	potentialProfiles.setRemoveButton("Add Profiles", new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		targetProfiles.addElements(potentialProfiles.getSelectedElements());
	    }
	});
	potentialProfiles.addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
		profileDisplayHelp();
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		profileDisplayHelp();
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(potentialProfiles);

	partialMatch = new LCheckBox("Display Partially Matching", AV.AVFont, AV.yellow);
	partialMatch.addShadow();
	partialMatch.putUnder(potentialProfiles, x, 15);
	partialMatch.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		fillMatching();
	    }
	});
	partialMatch.linkTo(Settings.WIZ_PARTIAL_MATCH, AV.save, SUMGUI.helpPanel, true);
	Add(partialMatch);

	targetProfiles = new SPList<>("Chosen Profiles", AV.AVFont, AV.yellow);
	targetProfiles.setSize(settingsPanel.getWidth() - 2 * x, 150);
	targetProfiles.setUnique(true);
	targetProfiles.setLocation(x, backButton.getY() - targetProfiles.getHeight() - 20);
	targetProfiles.addMouseListener(new MouseListener() {

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
		SUMGUI.helpPanel.setTitle("Chosen Profiles");
		SUMGUI.helpPanel.setContent(chosenProfiles());
		SUMGUI.helpPanel.focusOn(targetProfiles, 0);
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(targetProfiles);

	SUMGUI.startImport();
    }

    @Override
    public void onOpen(SPMainMenuPanel parent) {
	mainHelp();
	targetProfiles.clearHighlight();
	editing.load(WizNewPackage.newPackage.targetPackage, WizNewPackage.newPackage.targetSet, null, null);
    }

    @Override
    public boolean testNext() {
	boolean pass = !targetProfiles.isEmpty();
	if (!pass) {
	    targetProfiles.highlightChanged();
	}
	return pass;
    }

    @Override
    public void onNext() {
	WizNewPackage.newPackage.targetProfiles = targetProfiles.getAll();
	AV.wizGenPanel.open();
	AV.wizGenPanel.reset();
	AV.wizGenPanel.setBack(AV.wizSetToolPanel);
    }

    void reset() {
	textures.clearHighlight();
	textures.clear();
	partialMatch.setSelected(false);
	potentialProfiles.clear();
	targetProfiles.clear();
	displaySwitch(0);
    }

    void updateHelp(VariantProfileNPC profile) {
	SUMGUI.helpPanel.setDefaultPos();
	SUMGUI.helpPanel.setTitle("Profile Contents:");
	String contents =
		"Race:  " + profile.getRace().getEDID() + "\n"
		+ "Skin:  " + profile.getSkin().getEDID() + "\n"
		+ "Piece: " + profile.getPiece().getEDID() + "\n\n"
		+ "Textures Used: \n"
		+ profile.printAllTextures();
	SUMGUI.helpPanel.setContent(contents);
	SUMGUI.helpPanel.hideArrow();
    }

    void displaySwitch(final int stage) {
	SwingUtilities.invokeLater(new Runnable() {

	    public void run() {
		if (stage == 0) {
		    setQuestionText("Analyze textures to narrow down profile options.");
		}
		if (stage == 2) {
		    setQuestionText("Pick profiles that makes sense for your variant.");
		}
		textures.setVisible(stage == 0);
		analyze.setVisible(stage == 0);
		progress.setVisible(stage == 1);
		progressLabel.setVisible(stage == 1);
		targetProfiles.setVisible(stage == 2);
		potentialProfiles.setVisible(stage == 2);
		exception.setVisible(stage == 3);
		partialMatch.setVisible(stage == 2);
	    }
	});
    }

    void fillMatching() {
	potentialProfiles.clear();
	TreeSet<ProfileDisplay> tree = new TreeSet<>();
	tree.addAll(matchingProfiles);
	if (partialMatch.isSelected()) {
	    tree.addAll(halfMatchingProfiles);
	}
	potentialProfiles.addElements(tree);
    }

    void profileDisplayHelp() {
	ProfileDisplay display = potentialProfiles.getSelectedElement();
	if (display != null) {
	    updateHelp(display.profile);
	}
    }

    void mainHelp() {
	if (AV.wizSetToolPanel.isVisible()) {
	    SUMGUI.helpPanel.setDefaultPos();
	    SUMGUI.helpPanel.setTitle("Profile Locator Tool");
	    SUMGUI.helpPanel.setContent("This tool will analyze the textures you are using in this set and show you the profiles that would use those textures.\n\n"
		    + profileDesc() + "\n\n"
		    + "Most of the time you will want to pick all the profiles that match, unless one doesn't make logical sense (ex: Werewolf skin for a wolf variant), "
		    + "or you have some artistic reason for not wanting to include some.\n\n"
		    + "You can optionally show profiles that contain at least one of the textures you are using.  You can add those if they make sense as well.\n\n"
		    + textureNameWarning());
	    SUMGUI.helpPanel.hideArrow();
	}
    }

    class Analyze implements Runnable {

	ArrayList<File> files;

	public Analyze(ArrayList<File> in) {
	    files = in;
	}

	@Override
	public void run() {
	    try {
		AVFileVars.npcFactory.prepProfiles();
		SPGlobal.newLog("Set Tool/Run " + attempt++ + ".txt");
		printSourceTextures();

		matchingProfiles = new HashSet<>();
		halfMatchingProfiles = new HashSet<>();
		ArrayList<String> textureList = new ArrayList<>();
		for (File f : textures.getAll()) {
		    textureList.add(f.getName().toUpperCase());
		}

		for (VariantProfileNPC profile : AVFileVars.npcFactory.profiles) {
		    ProfileDisplay display = new ProfileDisplay(profile);
		    matchingProfiles.add(display);
		    for (String s : textureList) {
			if (profile.hasTexture(s)) {
			    halfMatchingProfiles.add(display);
			} else {
			    matchingProfiles.remove(display);
			}
		    }
		    halfMatchingProfiles.removeAll(matchingProfiles);
		}

		fillMatching();

		displaySwitch(2);
	    } catch (Exception e) {
		SPGlobal.logException(e);
		displaySwitch(3);
	    }
	}
    }

    void printSourceTextures() {
	SPGlobal.log("Set Tool", "Original files:");
	for (File f : textures.getAll()) {
	    SPGlobal.log("Set Tool", "  " + f.getPath());
	}
    }
}
