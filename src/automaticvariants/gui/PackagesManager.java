/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AVSaveFile.Settings;
import automaticvariants.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import lev.Ln;
import lev.gui.*;
import skyproc.SPGlobal;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPSettingPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class PackagesManager extends SPSettingPanel {

    public static PackageTree tree;
    LScrollPane treeScroll;
    LImagePane display;
    LButton enableButton;
    LButton disableButton;
    LButton otherSettings;
    LButton createNewPackage;
    JPopupMenu optionsMenu;
    LMenuItem enable;
    LMenuItem disable;
    LMenuItem delete;
    LMenuItem compress;
    LMenuItem editSpec;
    public LLabel dimensions;

    public PackagesManager(SPMainMenuPanel parent_) {
	super(parent_, "Texture Variants", AV.orange);
    }

    @Override
    protected void initialize() {
	super.initialize();

	tree = new PackageTree(SUMGUI.helpPanel);
	
	treeScroll = new LScrollPane(tree);
	treeScroll.setSize(SUMGUI.middleDimensions.width - 30,
		SUMGUI.middleDimensions.height - 165);
	treeScroll.setLocation(SUMGUI.middleDimensions.width / 2 - treeScroll.getWidth() / 2, last.y + 10);
	Add(treeScroll);

	Dimension size = new Dimension(125, 25);

	otherSettings = new LButton("Other Settings", size);
	createNewPackage = new LButton("Create New Variant", size);
	otherSettings.setLocation(getSpacing(otherSettings, createNewPackage, true));
	otherSettings.addActionListener(AV.packagesOtherPanel.getOpenHandler());
	Add(otherSettings);


	createNewPackage.setLocation(getSpacing(otherSettings, createNewPackage, false));
	createNewPackage.addActionListener(AV.wizPackagesPanel.getOpenHandler());
	Add(createNewPackage);


	enableButton = new LButton("Enable", size);
	enableButton.setLocation(otherSettings.getX(), otherSettings.getY() - enableButton.getHeight() - 15);
	enableButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		enableSelection(true);
	    }
	});
	enableButton.linkTo(Settings.PACKAGES_ENABLE, AV.save, SUMGUI.helpPanel, true);
	enableButton.setFollowPosition(false);
	Add(enableButton);


	disableButton = new LButton("Disable", size);
	disableButton.setLocation(createNewPackage.getX(), otherSettings.getY() - enableButton.getHeight() - 15);
	disableButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		enableSelection(false);
	    }
	});
	disableButton.linkTo(Settings.PACKAGES_DISABLE, AV.save, SUMGUI.helpPanel, true);
	disableButton.setFollowPosition(false);
	Add(disableButton);

	optionsMenu = new JPopupMenu();
	enable = new LMenuItem("Enable");
	enable.linkTo(Settings.PACKAGES_ENABLE, AV.save, SUMGUI.helpPanel, true);
	enable.setFollowPosition(false);
	enable.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		enableSelection(true);
	    }
	});
	optionsMenu.add(enable.getItem());

	disable = new LMenuItem("Disable");
	disable.linkTo(Settings.PACKAGES_DISABLE, AV.save, SUMGUI.helpPanel, true);
	disable.setFollowPosition(false);
	disable.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		enableSelection(false);
	    }
	});
	optionsMenu.add(disable.getItem());

	delete = new LMenuItem("Delete");
	delete.linkTo(Settings.PACKAGES_DELETE, AV.save, SUMGUI.helpPanel, true);
	delete.setFollowPosition(false);
	delete.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete these packages?", "Confirm Delete",
			JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
		    deleteSelected();
		}
	    }
	});
//	optionsMenu.add(delete.getItem());

	compress = new LMenuItem("Compress");
	compress.linkTo(Settings.PACKAGES_COMPRESS, AV.save, SUMGUI.helpPanel, true);
	compress.setFollowPosition(false);
	compress.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		compress();
	    }
	});
	optionsMenu.add(compress.getItem());

	editSpec = new LMenuItem("Edit Specs");
	editSpec.linkTo(Settings.PACKAGES_EDIT, AV.save, SUMGUI.helpPanel, true);
	editSpec.setFollowPosition(false);
	editSpec.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		editSpec();
	    }
	});
	optionsMenu.add(editSpec.getItem());


	//Add popup listener
	MouseAdapter ma = new MouseAdapter() {

	    private void myPopupEvent(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		JTree tree = (JTree) e.getSource();
		TreePath path = tree.getPathForLocation(x, y);
		if (path == null) {
		    return;
		}

		tree.setSelectionPath(path);
		PackageNode sel = (PackageNode) path.getLastPathComponent();

		compress.setVisible(sel.type == PackageNode.Type.PACKAGE
			|| sel.type == PackageNode.Type.VARSET
			|| sel.type == PackageNode.Type.ROOT);

		editSpec.setVisible(
			sel.type == PackageNode.Type.VAR
//			|| sel.type == PackageNode.Type.VARSET
			|| sel.type == PackageNode.Type.PACKAGE);

		optionsMenu.show(tree, x + 10, y);
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
		    myPopupEvent(e);
		}
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
		    myPopupEvent(e);
		}
	    }
	};
	tree.addMouseListener(ma);

	dimensions = new LLabel("", new Font("Serif", Font.PLAIN, 12), AV.lightGray);
	dimensions.setLocation(SUMGUI.rightDimensions.width /2 - dimensions.getWidth()/2, 0);

	display = new LImagePane();
	display.setMaxSize(SUMGUI.rightDimensions.width, 0);
	display.allowAlpha(false);
	display.setVisible(true);
	display.setLocation(0,dimensions.getHeight() + 2);
	PackageNode.display = display;

	reloadPackageList();

    }

    @Override
    public void onOpen(SPMainMenuPanel parent_) {
	SUMGUI.helpPanel.setDefaultPos();
	SUMGUI.helpPanel.clearBottomArea();
	SUMGUI.helpPanel.addToBottomArea(display);
	SUMGUI.helpPanel.addToBottomArea(dimensions);
	SUMGUI.helpPanel.setBottomAreaHeight(SUMGUI.rightDimensions.width + 2 + dimensions.getHeight());
    }

    public void enableSelection(boolean enable) {
	SUMGUI.setPatchNeeded(true);
	for (PackageNode p : getSelected()) {
	    p.enable(enable);
	}
	tree.repaint();
    }

    public ArrayList<PackageNode> getSelected() {
	TreePath[] paths = tree.getSelectionPaths();
	ArrayList<PackageNode> out = new ArrayList<>(paths.length);
	for (TreePath p : paths) {
	    out.add((PackageNode) p.getLastPathComponent());
	}
	return out;
    }

    public void deleteSelected() {
	for (PackageNode p : getSelected()) {
	    if (p.src.isFile()) {
		try {
		    Files.delete(p.src.toPath());
		} catch (IOException ex) {
		    SPGlobal.logException(ex);
		}
	    } else if (p.src.isDirectory()) {
		Ln.deleteDirectory(p.src);
	    }
	}
	reloadPackageList();
	tree.repaint();
    }

    public PackageNode getSelectedComponent() {
	return (PackageNode) tree.getSelectionPaths()[0].getLastPathComponent();
    }

    public void compress() {
	try {
	    // Enable selecton and move files
	    int row = tree.getLeadSelectionRow();
	    PackageNode p = getSelectedComponent();

	    // Select the same node
	    p = (PackageNode) tree.getPathForRow(row).getLastPathComponent();

	    // Compress
	    long before = p.fileSize();
	    if (SPGlobal.logging()) {
		SPGlobal.log(p.src.getName(), "Current file size: " + before + "->" + Ln.toMB(before) + "MB");
	    }

	    p.consolidateCommonFiles();
	    PackageNode.rerouteFiles(p.getDuplicateFiles());

	    long after = p.fileSize();
	    if (SPGlobal.logging()) {
		SPGlobal.log(p.src.getName(), "After file size: " + after + "->" + Ln.toMB(after) + "MB");
		SPGlobal.log(p.src.getName(), "Reduced size by: " + (before - after) + "->" + Ln.toMB(before - after) + "MB");
	    }
	    JOptionPane.showMessageDialog(null, "<html>"
		    + "Compressed: " + p.src.getPath() + "<br>"
		    + "  Starting size: " + Ln.toMB(before) + " MB<br>"
		    + "    Ending size: " + Ln.toMB(after) + " MB<br>"
		    + "    Saved space: " + Ln.toMB(before - after) + " MB"
		    + "</html>");
	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	}
    }

    public void editSpec() {
	PackageNode p = getSelectedComponent();
	switch (p.type) {
	    case VAR:
		AV.wizVarSpecPanel.open();
		Variant v = ((Variant) p);
		AV.wizVarSpecPanel.load(v);
		AV.wizVarSpecPanel.setNext(this);
		AV.wizVarSpecPanel.setBack(null);
		break;
	    case VARSET:
		AV.wizVarSetSpecPanel.open();
		VariantSet vs = ((VariantSet) p);
		AV.wizVarSetSpecPanel.load(vs);
		AV.wizVarSetSpecPanel.setNext(this);
		AV.wizVarSetSpecPanel.setBack(null);
		break;
	    case PACKAGE:
		AV.wizPackageSpecPanel.open();
		AVPackage avp = ((AVPackage) p);
		AV.wizPackageSpecPanel.load(avp);
		AV.wizPackageSpecPanel.setNext(this);
		AV.wizPackageSpecPanel.setBack(null);
		break;
	}
    }

    public static void reloadPackageList() {
	try {
	    AVFileVars.importVariants(false);
	    AVFileVars.AVPackages.sort();
	    tree.setModel(new DefaultTreeModel(AVFileVars.AVPackages));
	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	}
    }
}
