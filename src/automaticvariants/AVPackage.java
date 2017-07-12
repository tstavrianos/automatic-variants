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
import lev.LMergeMap;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class AVPackage extends PackageNode {

    public SpecPackage spec;
    static String depth = "";
    ArrayList<File> templateMods = new ArrayList<>(1);

    public AVPackage(File packageFolder) throws FileNotFoundException, IOException {
	super(packageFolder, Type.PACKAGE);
	spec = new SpecPackage(packageFolder);
	loadSets();
    }

    ArrayList<VariantSet> getSets() {
	ArrayList<PackageNode> sets = getAll(Type.VARSET);
	ArrayList<VariantSet> out = new ArrayList<>(sets.size());
	for (PackageNode p : sets) {
	    out.add((VariantSet) p);
	}
	return out;
    }

    ArrayList<Variant> flatten() {
	ArrayList<Variant> out = new ArrayList<>();
	for (VariantSet s : getSets()) {
	    out.addAll(s.multiplyAndFlatten());
	}
	return out;
    }

    @Override
    public void consolidateCommonFiles() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "==============================================");
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "Consolidating Common Files " + src);
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "==============================================");
	    SPGlobal.flush();
	}
	for (VariantSet set : getSets()) {
	    set.consolidateCommonFiles();
	}
    }

    @Override
    public LMergeMap<File, File> getDuplicateFiles() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "==============================================");
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "Creating File Shortcuts " + src);
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "==============================================");
	    SPGlobal.flush();
	}
	LMergeMap<File, File> duplicates = new LMergeMap<>(false);
	for (VariantSet set : getSets()) {
	    duplicates.addAll(set.getDuplicateFiles());
	}

	return duplicates;
    }

    @Override
    public String printSpec() {
	if (spec != null) {
	    String out = spec.printHelpInfo();
	    if (!out.equals("")) {
		out += "\n";
	    }
	    return out;
	} else {
	    return "BAD SPEC FILE";
	}
    }

    final public void loadSets() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "************************************************************");
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "* Loading package: " + src);
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "************************************************************");
	}
	for (File f : src.listFiles()) {
	    if (f.isDirectory()) {
		VariantSet set = new VariantSet(f);
		if (set.loadVariants()) {
		    add(set);
		}
	    } else if (AVFileVars.isSpec(f)) {
		spec = AV.gson.fromJson(new FileReader(f), SpecPackage.class);
		spec.src = f;
	    }
	}
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "*** END package: " + src);
	}
    }
}
