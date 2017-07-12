/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import lev.Ln;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class VariantGroup extends PackageNode {

    static String depth = "* +   ";

    VariantGroup(File groupDir) {
	super(groupDir, Type.VARGROUP);
    }

    public void load() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "### Adding Variant Group: " + src);
	}
	for (File f : src.listFiles()) {
	    if (f.isDirectory()) {
		Variant v = new Variant(f);
		v.load();
		add(v);
	    }
	}
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "####################################");
	}
    }

    @Override
    public ArrayList<Variant> getVariants() {
	ArrayList<PackageNode> vars = getAll(Type.VAR);
	ArrayList<Variant> out = new ArrayList<>(vars.size());
	for (PackageNode p : vars) {
	    out.add((Variant) p);
	}
	return out;
    }

    public void mergeInGlobals(ArrayList<PackageNode> globalFiles) {
	for (Variant v : getVariants()) {
	    v.mergeInGlobals(globalFiles);
	}
    }

    public void deleteMatches(ArrayList<File> files) throws FileNotFoundException, IOException {
	for (File common : files) {
	    for (Variant v : getVariants()) {
		for (PackageNode c : v.getAll(Type.TEXTURE)) {
		    if (common.getName().equalsIgnoreCase(c.src.getName())) {
			if (SPGlobal.logging()) {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  ------------------------------");
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  Comparing");
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, "Consoldiate", "  " + common);
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "    and");
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, "Consoldiate", "  " + c.src);
			    SPGlobal.flush();
			}
			if (Ln.validateCompare(common, c.src, 0)) {
			    if (isReroute()) {
				c.src.delete();
			    }
			    if (SPGlobal.logging()) {
				SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  Deleted " + c + " because it was a common file.");
			    }
			}
		    }
		}
	    }
	}
    }

    public ArrayList<PackageNode> consolidateCommonFilesInternal() throws FileNotFoundException, IOException {
	ArrayList<PackageNode> out = new ArrayList<>();
	ArrayList<Variant> vars = getVariants();
	if (vars.size() > 1) {
	    Variant first = vars.get(0);
	    // For each texture in the first variant
	    for (PackageNode tex : first.getAll(Type.TEXTURE)) {
		if (SPGlobal.logging()) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  ---------------");
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  CHECKING " + tex.src);
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  ---------------");
		}
		boolean textureCommon = true;
		ArrayList<PackageNode> delete = new ArrayList<>();
		// Check each other variant's textures.
		for (int i = 1; i < vars.size(); i++) {
		    boolean variantContained = false;
		    for (PackageNode texRhs : vars.get(i).getAll(Type.TEXTURE)) {
			if (SPGlobal.logging()) {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "    ------------------------------");
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "    Comparing");
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, "Consoldiate", "    " + tex.src);
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "      and");
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, "Consoldiate", "    " + texRhs.src);
			    SPGlobal.flush();
			}
			// If other variant had texture, move on to next and
			// mark that texture for deletion
			if (tex.src.getName().equalsIgnoreCase(texRhs.src.getName())
				&& Ln.validateCompare(tex.src, texRhs.src, 0)) {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "      Matched");
			    delete.add(texRhs);
			    variantContained = true;
			    break;
			} else {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "      DID NOT match.");
			}
		    }
		    // If one variant in group did not have texture, then
		    // it is not a common texture
		    if (!variantContained) {
			SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  == Was NOT a common texture: " + tex.src);
			textureCommon = false;
			break;
		    }
		}
		// If common texture, return it and delete
		// the duplicate textures
		if (textureCommon) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  == WAS a common texture: " + tex.src);
		    out.add(tex);
		    for (PackageNode p : delete) {
			if (!p.isReroute() && p.src.delete()) {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  " + p.src + " was deleted.");
			} else {
			    SPGlobal.logError(src.getName(), "  !!!" + p.src + " was NOT successfully deleted.");
			}
		    }
		}
	    }
	}
	return out;
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
