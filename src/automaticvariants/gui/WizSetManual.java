/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVFileVars;
import automaticvariants.VariantProfileNPC;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import lev.gui.*;
import skyproc.gui.SPList;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPProgressBarPlug;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class WizSetManual extends WizTemplate {

    TreeSet<ProfileDisplay> races = new TreeSet<>();
    TreeSet<ProfileDisplay> skins = new TreeSet<>();
    TreeSet<ProfileDisplay> pieces = new TreeSet<>();
    LComboSearchBox<ProfileDisplay> racePicker;
    LComboSearchBox<ProfileDisplay> skinPicker;
    LComboSearchBox<ProfileDisplay> piecePicker;
    LButton resetPickers;
    LButton addButton;
    LCheckBox exclusive;
    SPList<ProfileDisplay> targetProfiles;
    LLabel progressLabel;
    LProgressBar progress;
    ArrayList<String> blockedSkins = new ArrayList<>();

    public WizSetManual(SPMainMenuPanel parent_) {
	super(parent_, "Target Profiles", AV.packagesManagerPanel, AV.wizSetPanel);
    }

    @Override
    protected void initialize() {
	super.initialize();

	spacing = 15;

	blockedSkins.add("SkinNaked");
	blockedSkins.add("SkinNakedBeast");
	blockedSkins.add("ArmorAfflicted");
	blockedSkins.add("ArmorAstrid");
	blockedSkins.add("ArmorManakin");

	setQuestionText("Please select the profiles your variant should target.");

	racePicker = new LComboSearchBox<>("Race", AV.AVFont, AV.yellow);
	racePicker.setSize(settingsPanel.getWidth() - x * 2, fieldHeight);
	racePicker.putUnder(question, x, spacing);
//	racePicker.addBoxActionListener(new ActionListener(){
//
//	    @Override
//	    public void actionPerformed(ActionEvent e) {
//		if (exclusive.isSelected()) {
//		    skinPicker.removeAllItems();
//		    piecePicker.removeAllItems();
//		    loadSkins();
//		    loadPieces();
//		}
//	    }
//	});
	racePicker.addMouseListener(new MouseListener() {

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
		SUMGUI.helpPanel.setDefaultPos();
		SUMGUI.helpPanel.setTitle("Pick Race");
		SUMGUI.helpPanel.setContent("Pick the race component of the profile you want to target.");
		SUMGUI.helpPanel.focusOn(racePicker, 0);
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(racePicker);

	skinPicker = new LComboSearchBox<>("Skin", AV.AVFont, AV.yellow);
	skinPicker.setSize(settingsPanel.getWidth() - x * 2, fieldHeight);
	skinPicker.putUnder(racePicker, racePicker.getX(), spacing);
	skinPicker.addMouseListener(new MouseListener() {

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
		SUMGUI.helpPanel.setDefaultPos();
		SUMGUI.helpPanel.setTitle("Pick Skin");
		SUMGUI.helpPanel.setContent("Pick the Armor component of the profile you want to target.");
		SUMGUI.helpPanel.focusOn(skinPicker, 0);
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(skinPicker);


	piecePicker = new LComboSearchBox<>("Armor Piece", AV.AVFont, AV.yellow);
	piecePicker.setSize(settingsPanel.getWidth() - x * 2, fieldHeight);
	piecePicker.putUnder(skinPicker, skinPicker.getX(), spacing);
	piecePicker.addMouseListener(new MouseListener() {

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
		SUMGUI.helpPanel.setDefaultPos();
		SUMGUI.helpPanel.setTitle("Pick Armor Piece");
		SUMGUI.helpPanel.setContent("Pick the Armor Piece component of the profile you want to target.");
		SUMGUI.helpPanel.focusOn(piecePicker, 0);
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		mainHelp();
	    }
	});
	Add(piecePicker);

	resetPickers = new LButton("Reset Choosers ^");
	resetPickers.putUnder(piecePicker, x, 10);
	resetPickers.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		resetPickers();
	    }
	});
//	Add(resetPickers);

	exclusive = new LCheckBox("Exclusive", AV.AVFont, AV.yellow);
	exclusive.setLocation(settingsPanel.getWidth() - x - exclusive.getWidth(), resetPickers.getY() + resetPickers.getHeight() / 2 - exclusive.getHeight() / 2);
	exclusive.addShadow();
//	Add(exclusive);

	targetProfiles = new SPList<>("Target Profiles", AV.AVFont, AV.yellow);
	targetProfiles.setUnique(true);
	targetProfiles.setSize(settingsPanel.getWidth() - 30, 150);
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
	targetProfiles.centerIn(settingsPanel, backButton.getY() - targetProfiles.getHeight() - 20);
	Add(targetProfiles);

	addButton = new LButton("Add Profile");
	addButton.setLocation(resetPickers.getLocation());
	addButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ProfileDisplay race = racePicker.getSelectedItem();
		ProfileDisplay skin = skinPicker.getSelectedItem();
		ProfileDisplay piece = piecePicker.getSelectedItem();
		if (race == null || skin == null || piece == null) {
		    return;
		}
		VariantProfileNPC profile = new VariantProfileNPC(
			race.profile.getRace(), skin.profile.getSkin(), piece.profile.getPiece());
		ProfileDisplay display = new ProfileDisplay(profile,
			profile.getRace().getEDID() + " | "
			+ profile.getSkin().getEDID() + " | "
			+ profile.getPiece().getEDID());
		targetProfiles.addElement(display);
	    }
	});
	Add(addButton);

	progressLabel = new LLabel("Loading up mods, please wait...", AV.AVFontSmall, AV.lightGray);
	progressLabel.centerIn(settingsPanel, question.getBottom() + spacing * 3);
	Add(progressLabel);

	progress = new LProgressBar(200, 20, AV.AVFontSmall, AV.lightGray);
	progress.centerIn(settingsPanel, progressLabel.getBottom() + 10);
	SPProgressBarPlug.addProgressBar(progress);
	Add(progress);

	setNext(AV.wizGenPanel);

	displaySwitch(true);
	SUMGUI.startImport(new Loader());
    }

    @Override
    public void onOpen(SPMainMenuPanel parent) {
	mainHelp();
	editing.load(WizNewPackage.newPackage.targetPackage, WizNewPackage.newPackage.targetSet, null, null);
    }

    public void resetAll() {
	resetPickers();
	targetProfiles.clear();
	exclusive.setSelected(false);
    }

    public void resetPickers() {
	racePicker.reset();
	skinPicker.reset();
	piecePicker.reset();
    }

    void mainHelp() {
	SUMGUI.helpPanel.setDefaultPos();
	SUMGUI.helpPanel.setTitle("Manually Picking Profiles");
	SUMGUI.helpPanel.setContent("Pick all the profiles that are used by NPCs that you want your variants to be added to.\n\n"
		+ WizSet.profileDesc() + "\n\n"
		+ "It's quite easy to manually pick profiles that won't ever actually exist in the game.  If you are not sure what profiles fit your variant, go back and use the AV tool.");
	SUMGUI.helpPanel.hideArrow();
    }

    @Override
    public boolean testNext() {
	return !targetProfiles.isEmpty();
    }

    @Override
    public void onNext() {
	WizNewPackage.newPackage.targetProfiles = targetProfiles.getAll();
	AV.wizGenPanel.open();
	AV.wizGenPanel.reset();
	AV.wizGenPanel.setBack(AV.wizSetManualPanel);
    }

    void displaySwitch(Boolean start) {
	racePicker.setVisible(!start);
	skinPicker.setVisible(!start);
	piecePicker.setVisible(!start);
	resetPickers.setVisible(!start);
	exclusive.setVisible(!start);
	targetProfiles.setVisible(!start);
	addButton.setVisible(!start);
	progressLabel.setVisible(start);
	progress.setVisible(start);
    }

    class Loader implements Runnable {

	@Override
	public void run() {
	    AVFileVars.npcFactory.prepProfiles();
	    for (VariantProfileNPC profile : AVFileVars.npcFactory.profiles) {
		races.add(new ProfileDisplay(profile, profile.getRace().getEDID()));
		skins.add(new ProfileDisplay(profile, profile.getSkin().getEDID()));
		pieces.add(new ProfileDisplay(profile, profile.getPiece().getEDID()));
	    }
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    loadRaces();
		}
	    });
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    loadSkins();
		}
	    });
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    loadPieces();
		}
	    });
	    displaySwitch(false);
	}
    }

    void loadRaces() {
	ProfileDisplay skin = skinPicker.getSelectedItem();
	ProfileDisplay piece = piecePicker.getSelectedItem();

	racePicker.removeAllItems();
	for (ProfileDisplay d : races) {
	    if (!exclusive.isSelected()
		    && (skin == null || skin.profile.getSkin().equals(d.profile.getSkin()))
		    && (piece == null || piece.profile.getPiece().equals(d.profile.getPiece()))) {
		racePicker.addItem(d);
	    }
	}
	racePicker.reset();
	racePicker.setSelectedIndex(-1);
    }

    void loadSkins() {
	ProfileDisplay race = racePicker.getSelectedItem();
	ProfileDisplay piece = piecePicker.getSelectedItem();

	skinPicker.removeAllItems();
	for (ProfileDisplay d : skins) {
	    if (!exclusive.isSelected()
		    || ((race == null || race.profile.getRace().equals(d.profile.getRace())) 
		    && (piece == null || piece.profile.getPiece().equals(d.profile.getPiece())))) {
		skinPicker.addItem(d);
	    }
	}
	skinPicker.reset();
	skinPicker.setSelectedIndex(-1);
    }

    void loadPieces() {
	ProfileDisplay race = racePicker.getSelectedItem();
	ProfileDisplay skin = skinPicker.getSelectedItem();

	piecePicker.removeAllItems();
	for (ProfileDisplay d : pieces) {
	    if (!exclusive.isSelected()
		    || ((race == null || race.profile.getRace().equals(d.profile.getRace())) 
		    && (skin == null || skin.profile.getSkin().equals(d.profile.getSkin())))) {
		piecePicker.addItem(d);
	    }
	}
	piecePicker.reset();
	piecePicker.setSelectedIndex(-1);
    }
}
