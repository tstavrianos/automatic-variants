/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.PackageNode;
import automaticvariants.VariantSet;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import lev.gui.LHelpPanel;
import lev.gui.LTree;

/**
 *
 * @author Justin Swanson
 */
public class PackageTree extends LTree {

    static Color disabledColor = new Color(150,150,150);
    LHelpPanel help;

    public PackageTree(LHelpPanel help) {
	super();
	setCellRenderer(new CellRenderer(getCellRenderer()));
	this.help = help;
    }

    private class CellRenderer implements TreeCellRenderer {

	JPanel renderer;
	JLabel title;
	TreeCellRenderer defaultR;

	public CellRenderer(TreeCellRenderer defaultR) {
	    this.defaultR = defaultR;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	    PackageNode item = (PackageNode) value;

	    if (hasFocus) {
		item.updateHelp(help, true);
	    }

	    Component defaultC = defaultR.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	    if (item.isDisabled()) {
		defaultC.setForeground(disabledColor);
	    }

	    if (item.type == PackageNode.Type.VARSET && ((VariantSet)item).spec == null) {
		defaultC.setForeground(Color.RED);
	    }

	    return defaultC;
	}
    }

}
