/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import automaticvariants.AVFileVars.SpecHolder;
import automaticvariants.SpecVariantSet.VariantType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lev.LPair;
import lev.LShrinkArray;
import lev.Ln;
import skyproc.*;
import skyproc.NIF.TextureSet;
import skyproc.exceptions.BadParameter;
import skyproc.gui.SPProgressBarPlug;

/**
 *
 * @author Justin Swanson
 */
abstract public class VariantFactory<T extends VariantProfile> {

    static String header = "VariantFactory";
    public ArrayList<T> profiles = new ArrayList<>();
    int folderNumber = 1;

    public void createVariants(Mod source) {
	if (needed()) {
	    int step = 1;
	    int steps = 3;
	    SPProgressBarPlug.reset();
	    SPProgressBarPlug.setMax(steps);

	    SPProgressBarPlug.setStatusNumbered(step++, steps, "Matching With Profiles");
	    prepProfiles();
	    dropVariantSetsInProfiles();
	    SPProgressBarPlug.incrementBar();

	    SPProgressBarPlug.setStatusNumbered(step++, steps, "Creating Variant Records");
	    clearUnusedProfiles();
	    createVariantRecords(source);
	    if (AV.save.getBool(AVSaveFile.Settings.PACKAGES_ORIG_AS_VAR)) {
		implementOriginalAsVar();
	    }
	    SPProgressBarPlug.incrementBar();

	    SPProgressBarPlug.setStatusNumbered(step++, steps, "Creating Structure Records");
	    createStructureRecords(source);
	    SPProgressBarPlug.incrementBar();
	}
    }

    public abstract String debugName();

    public String debugFolder() {
	return AVFileVars.debugFolder + debugName() + "/" + folderNumber++ + " - ";
    }

    public void prepProfiles() {
	BSA.loadInBSAs(BSA.FileType.NIF, BSA.FileType.DDS);
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Locate Unused.txt");
	}
	locateUnused();
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Load Variant Profiles.txt");
	}
	createProfileShells();
	loadProfileNifs();
	loadProfileRecords();
	finalizeProfiles();
	printProfiles();
    }

    public void loadProfileNifs() {
	for (VariantProfile profile : new ArrayList<>(profiles)) {
	    try {
		if (SPGlobal.logging()) {
		    SPGlobal.log(header, "Loading NIFs for profile " + profile);
		}
		profile.catalogNif(profile.getNifPath());
		if (profile.textures.isEmpty()) {
		    remove(profile);
		    SPGlobal.log(profile.toString(), "Removing profile with nif because it had no textures: " + profile.getNifPath());
		}
	    } catch (Throwable ex) {
		remove(profile);
		SPGlobal.log(profile.toString(), "Removing profile with nif because exception occured: " + profile.getNifPath());
		SPGlobal.logException(ex);
	    }
	}
    }

    public static ArrayList<TextureSet> loadNif(String nifPath, LShrinkArray in) {
	ArrayList<TextureSet> nifTextures = new ArrayList<>();
	try {
	    NIF nif = new NIF(nifPath, in);
	    nifTextures = nif.extractTextureSets();

	    // To uppercase
	    for (TextureSet t : nifTextures) {
		for (int i = 0; i < t.getTextures().size(); i++) {
		    t.getTextures().set(i, t.getTextures().get(i).toUpperCase());
		}
	    }

	} catch (BadParameter | java.nio.BufferUnderflowException ex) {
	    SPGlobal.logException(ex);
	}
	return nifTextures;
    }

    public void remove(VariantProfile p) {
	profiles.remove((T) p);
    }

    public abstract void locateUnused();

    public abstract void createProfileShells();

    public abstract void loadProfileRecords();

    void finalizeProfiles() {
	for (VariantProfile p : profiles) {
	    p.finalizeProfile();
	}
    }

    void clearUnusedProfiles() {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Clear Unused Profiles.txt");
	}
	for (VariantProfile profile : new ArrayList<>(profiles)) {
	    if (profile.matchedVariantSets.isEmpty()) {
		if (SPGlobal.logging()) {
		    SPGlobal.log(header, "Removing profile " + profile + " because it was empty.");
		    profile.print();
		}
		remove(profile);
	    }
	}
    }

    abstract boolean isUnused(FormID id);

    public void dropVariantSetsInProfiles() {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Processing Variant Seeds.txt");
	}
	for (PackageNode avPackageC : AVFileVars.AVPackages.getAll(PackageNode.Type.PACKAGE)) {
	    AVPackage avPackage = (AVPackage) avPackageC;
	    for (PackageNode varSetP : avPackage.getAll(PackageNode.Type.VARSET)) {
		VariantSet varSet = (VariantSet) varSetP;
		if (varSet.spec.getType() != getType()) {
		    continue;
		} else if (SPGlobal.logging()) {
		    SPGlobal.log("SortVariantSets", " /====================================");
		    SPGlobal.log("SortVariantSets", "| Processing: " + varSet.src);
		    SPGlobal.log("SortVariantSets", "|==============\\");
		    SPGlobal.log("SortVariantSets", "|== Files: =====|");
		    SPGlobal.log("SortVariantSets", "|==============/");
		    for (String s : varSet.getTextures()) {
			SPGlobal.log("SortVariantSets", "|    " + s);
		    }
		    SPGlobal.log("SortVariantSets", "|==============\\");
		    SPGlobal.log("SortVariantSets", "|== Seeds: =====|");
		    SPGlobal.log("SortVariantSets", "|==============/");
		}

		ArrayList<Seed> seeds = varSet.getSeeds();

		if (SPGlobal.logging()) {
		    for (Seed s : seeds) {
			s.print();
		    }
		}

		boolean absorbed = false;
		for (VariantProfile varProfile : profiles) {
		    if (varProfile.absorb(varSet, seeds)) {
			SPGlobal.log("Absorb", "  /======================================");
			SPGlobal.log("Absorb", " /=== " + varSet.src + " absorbed by:");
			SPGlobal.log("Absorb", "|=======================================");
			varProfile.printShort();
			SPGlobal.log("Absorb", " \\======================================");
			absorbed = true;
		    }
		}

		if (SPGlobal.logging()) {
		    if (!absorbed) {
			SPGlobal.logError("Absorbing", "Variant Set " + varSet.src + " could not be absorbed by any profile.");
		    } else {
			// Spacing in logs
			SPGlobal.log(header, "");
			SPGlobal.log(header, "");
			SPGlobal.log(header, "");
			SPGlobal.log(header, "");
		    }
		}
	    }
	}
    }

    public void printProfiles() {
	SPGlobal.log("Print", "===========================================================");
	SPGlobal.log("Print", "=============      Printing all Profiles     ==============");
	SPGlobal.log("Print", "===========================================================");
	SPGlobal.log("Print", "");
	for (VariantProfile v : profiles) {
	    v.print();
	}
    }

    public void createVariantRecords(Mod source) {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Generate Variants.txt");
	}
	for (VariantProfile profile : profiles) {
	    if (SPGlobal.logging()) {
		SPGlobal.log(toString(), " ***********> Generating profile " + profile);
	    }
	    profile.generateRecords();
	}
    }

    public abstract void implementOriginalAsVar();

    public abstract void createStructureRecords(Mod source);

    public abstract VariantType getType();

    public boolean needed() {
	for (PackageNode avPackageC : AVFileVars.AVPackages.getAll(PackageNode.Type.PACKAGE)) {
	    AVPackage avPackage = (AVPackage) avPackageC;
	    for (PackageNode varSetP : avPackage.getAll(PackageNode.Type.VARSET)) {
		VariantSet varSet = (VariantSet) varSetP;
		if (!varSet.isDisabled() && varSet.spec.getType() == getType()) {
		    return true;
		}
	    }
	}
	return false;
    }

    public T find(Seed s) {
	for (T profile : profiles) {
	    if (s.equals(profile.getSeed())) {
		return profile;
	    }
	}
	return null;
    }

    static int calcLCM(ArrayList specs) {
	int[] divs = new int[specs.size()];
	for (int i = 0; i < divs.length; i++) {
	    SpecHolder holder = ((SpecHolder) specs.get(i));
	    divs[i] = holder.spec.Probability_Divider;
	}
	return Ln.lcmm(divs);
    }
}
