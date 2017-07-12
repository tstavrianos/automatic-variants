/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.PackageNode;
import java.awt.Color;
import java.awt.Component;
import lev.gui.LComponent;
import lev.gui.LLabel;
import lev.gui.LPanel;

/**
 *
 * @author Justin Swanson
 */
public class PackageEditing extends LComponent {

    LPanel panel;
    LLabel editing;
    LLabel packageName;
    LLabel variantName;
    Component center;

    static String spacer = " - ";

    public PackageEditing(Component centerIn) {
	super();

	center = centerIn;

	panel = new LPanel();
	add(panel);

	editing = new LLabel("EDITING", AV.AVFont, AV.green);
	editing.addShadow();
	editing.setLocation(0, 0);
	panel.add(editing);

	packageName = new LLabel("Test", AV.AVFontSmall, Color.LIGHT_GRAY);
	panel.add(packageName);

	variantName = new LLabel("Test", AV.AVFontSmall, Color.LIGHT_GRAY);
	variantName.putUnder(packageName, 0, 0);
	panel.add(variantName);

	setSize(10, variantName.getY() + variantName.getHeight());
    }

    void load(PackageNode n) {
	String name = n.printName(spacer);
	if (n.type != PackageNode.Type.PACKAGE) {
	    packageName.setText(name.substring(0, name.indexOf(spacer)));
	    variantName.setText(name.substring(name.indexOf(spacer) + 3));
	} else {
	    packageName.setText(name);
	    variantName.setText("");
	}
	load();
    }

    void load(PackageNode p, PackageNode s, PackageNode g, PackageNode v) {
	if (p == null) {
	    packageName.setText("");
	} else {
	    packageName.setText(p.src.getName());
	}
	if (s == null) {
	    variantName.setText("");
	} else {
	    String str = s.src.getName();
	    if (g != null) {
		str += " - " + g.src.getName();
	    }
	    if (v != null) {
		str += " - " + v.src.getName();
	    }
	    variantName.setText(str);
	}
	load();
    }

    private void load() {
	int totalLength;
	if (variantName.getWidth() > packageName.getWidth()) {
	    totalLength = variantName.getWidth();
	} else {
	    totalLength = packageName.getWidth();
	}
	totalLength += editing.getWidth() + 10;
	packageName.setLocation(editing.getWidth() + 10, 0);
	variantName.setLocation(packageName.getX(), variantName.getY());
	setSize(totalLength, getSize().height);
	this.centerIn(center, getY());
	setVisible(true);
    }

    @Override
    public final void setSize(int width, int height) {
	super.setSize(width, height);
	panel.setSize(width, height);
    }
}
