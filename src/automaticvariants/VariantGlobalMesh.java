/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class VariantGlobalMesh extends PackageNode {

    static String depth = "* +   ";
    public SpecVariant spec;

    VariantGlobalMesh(File groupDir) {
	super(groupDir, Type.GLOBALMESH);
	spec = new SpecVariant(groupDir);
    }

    public void load() throws FileNotFoundException, IOException {
	File globalMesh = null;
	for (File f : src.listFiles()) {
	    if (AVFileVars.isSpec(f)) {
		try {
		    spec = AV.gson.fromJson(new FileReader(f), SpecVariant.class);
		    if (spec != null) {
			spec.src = f;
			if (SPGlobal.logging()) {
			    spec.printToLog(src.getName());
			}
		    }
		} catch (com.google.gson.JsonSyntaxException ex) {
		    SPGlobal.logException(ex);
		    JOptionPane.showMessageDialog(null, "Global Mesh " + f.getPath() + " had a bad specifications file.  Skipped.");
		}
	    } else if (AVFileVars.isNIF(f)) {
		globalMesh = f;
	    }
	}
	if (globalMesh != null) {
	    if (SPGlobal.logging()) {
		SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "* Adding Global Mesh: " + src);
	    }
	    PackageNode globalMeshN = new PackageNode(globalMesh, Type.MESH);
	    add(globalMeshN);
	} else if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "* Skipped Global Mesh: " + src);
	}
    }

    @Override
    public ArrayList<Variant> getVariants() {
	return new ArrayList<>(0);
    }

    public VariantSet getSet() {
	return (VariantSet) getParent();
    }

    @Override
    public String printName(String spacer) {
	PackageNode p = (PackageNode) this.getParent();
	return p.printName(spacer) + spacer + src.getName();
    }
}
