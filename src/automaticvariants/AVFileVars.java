/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import automaticvariants.AVSaveFile.Settings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import javax.swing.JOptionPane;
import lev.LMergeMap;
import lev.Ln;
import skyproc.*;
import skyproc.exceptions.BadMod;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.MissingMaster;
import skyproc.exceptions.Uninitialized;
import skyproc.gui.SPProgressBarPlug;

/**
 * All the functionality concerning setting up variants associated with race
 * switching:
 *
 * Texture variants
 *
 * @Author Justin Swanson
 */
public class AVFileVars {

    static String header = "AV_FileVar";
    final public static String AVPackagesDir = SPGlobal.pathToDataFixed + "SkyProc Patchers\\Automatic Variants\\AV Packages\\";
    public static String AVTexturesDir = SPGlobal.pathToDataFixed + "textures\\AV Packages\\";
    public static String AVMeshesDir = SPGlobal.pathToDataFixed + "meshes\\AV Packages\\";
    static String debugFolder = "File Variants/";
    public static PackageNode AVPackages = new PackageNode(new File(AVPackagesDir), PackageNode.Type.ROOT);
    public static VariantFactoryNPC npcFactory = new VariantFactoryNPC();
    public static VariantFactoryWEAP weapFactory = new VariantFactoryWEAP();
    public static Mod templateMerger = new Mod(new ModListing("AVTemplateMerger", false));

    enum AVFileLogs {

	PackageImport;
    }

    static void setUpFileVariants(Mod source) throws IOException, Uninitialized, BadParameter {

	importVariants(true);
	AVPackages.prune();
	cleanBadSets();

	npcFactory.createVariants(source);

	weapFactory.createVariants(source);
	SPProgressBarPlug.done();
    }

    static public void cleanBadSets() {
	for (PackageNode avPackageC : AVFileVars.AVPackages.getAll(PackageNode.Type.PACKAGE)) {
	    AVPackage avPackage = (AVPackage) avPackageC;
	    for (PackageNode varSetP : avPackage.getAll(PackageNode.Type.VARSET)) {
		VariantSet varSet = (VariantSet) varSetP;
		if (varSet.spec == null || varSet.isEmpty()) {
		    SPGlobal.logError(header, "Skipping " + varSet.src + " because it was empty or missing a spec file.");
		    PackageNode p = (PackageNode) varSet.getParent();
		    p.remove(varSet);
		}
	    }
	}
    }

    /*
     * Shared methods
     */
    public static void importVariants(boolean progressBar) throws IOException {
	String header = "Import Variants";
	ArrayList<File> srcDirs = new ArrayList<>();
	srcDirs.add(new File(AVPackagesDir));
	// wipe
	AVPackages = new PackageNode(new File(AVPackagesDir), PackageNode.Type.ROOT);
	RerouteFile.reroutes.clear();
	for (File srcDir : srcDirs) {
	    if (srcDir.isDirectory()) {
		File[] files = srcDir.listFiles();
		if (progressBar) {
		    SPProgressBarPlug.setStatusNumbered(0, 0, "Importing AV Packages");
		    SPProgressBarPlug.reset();
		    SPProgressBarPlug.setMax(files.length);
		}
		for (File packageFolder : files) {
		    if (packageFolder.isDirectory()) {
			AVPackage avPackage = new AVPackage(packageFolder);
			AVPackages.mergeIn(avPackage);
			if (progressBar) {
			    SPProgressBarPlug.incrementBar();
			}
		    }
		}
		if (progressBar) {
		    SPProgressBarPlug.done();
		}
	    }
	}
    }

    static public void moveOut() {
	ArrayList<File> files = Ln.generateFileList(new File(AVPackagesDir), false);
	boolean pass = true;
	for (File src : files) {
	    if (isDDS(src)) {
		pass = move(src, AVFileVars.AVTexturesDir);
	    } else if (isNIF(src)) {
		pass = move(src, AVFileVars.AVMeshesDir);
	    }
	}
	if (!pass) {
	    JOptionPane.showMessageDialog(null,
		    "<html>Error creating hard link redirects of AV Packages.<br>"
		    + "Please report to Leviathan1753.</html>");
	    SPGlobal.logError("Move Out", "Failed to move some files out to their texture locations.");
	}
    }

    public static boolean move(File src, String dest) {
	File destFile = new File(dest + src.getPath().substring(src.getPath().indexOf("\\") + 1));
	boolean pass = true;
	if (destFile.isFile()) {
	    pass = pass && destFile.delete();
	}
	if (pass) {
	    if (AV.save.getBool(Settings.MOVE_PACKAGE_FILES)) {
		pass = pass && Ln.moveFile(src, destFile, false);
	    } else {
		try {
		    Ln.makeDirs(destFile);
		    Files.createLink(destFile.toPath(), src.toPath());
		    pass = pass && destFile.exists();
		} catch (IOException | UnsupportedOperationException ex) {
		    SPGlobal.logException(ex);
		    try {
			Files.copy(destFile.toPath(), src.toPath());
		    } catch (IOException ex1) {
			SPGlobal.logException(ex1);
			pass = false;
		    }
		}
	    }
	}
	return pass;
    }

    public static void saveAVPackagesListing() throws IOException {
	ArrayList<String> packageListing = new ArrayList<>();
	ArrayList<File> files = Ln.generateFileList(new File(AVPackagesDir), false);
	for (File f : files) {
	    packageListing.add(f.getPath());
	}
	AV.save.setStrings(Settings.PACKAGE_LISTING, packageListing);
    }

    public static ArrayList<String> getAVPackagesListing() throws IOException {
	return AV.save.getStrings(Settings.PACKAGE_LISTING);
    }

    public static boolean importTemplateMod(ModListing listing) {
	if (!SPGlobal.getDB().hasMod(listing)) {
	    try {
		SPProgressBarPlug.pause(true);
		Mod mod = SPImporter.importMod(listing, GRUP_TYPE.values());
	    } catch (BadMod | MissingMaster ex) {
		SPGlobal.logException(ex);
		SPProgressBarPlug.pause(false);
		return false;
	    }
	}
	SPProgressBarPlug.pause(false);
	return true;
    }
    /*
     * Other Methods
     */

    static int readjustTXSTindices(int j) {
	// Because nif fields map 2->3 if facegen flag is on.
	int set = j;
	if (set == 2) {
	    set = 3;
	}
	return set;
    }

    public static void gatherFiles() {
	gatherFolder(AVTexturesDir);
	gatherFolder(AVMeshesDir);
    }

    public static void gatherFolder(String folder) {
	ArrayList<File> files = Ln.generateFileList(new File(folder), 2, 4, false);
	boolean fail = false;
	for (File file : files) {
	    File dest = new File(AVPackagesDir + file.getPath().substring(folder.length()));
	    if (dest.exists()) {
		dest.delete();
	    }
	    if (!Ln.moveFile(file, dest, false)) {
		fail = true;
	    }
	}
	if (fail) {
	    JOptionPane.showMessageDialog(null,
		    "<html>Error gathering files back to AV Package folder.</html>");
	}
    }

    static boolean isSpec(File f) {
	return Ln.isFileType(f, "JSON");
    }

    static boolean isDDS(File f) {
	return Ln.isFileType(f, "DDS");
    }

    static boolean isESP(File f) {
	return Ln.isFileType(f, "ESP");
    }

    static boolean isNIF(File f) {
	return Ln.isFileType(f, "NIF");
    }

    static boolean isReroute(File f) {
	return Ln.isFileType(f, "reroute");


    }

    static String standardizePath(File f) {
	return standardizePath(f.getPath());
    }

    static String standardizePath(String path) {
	int index = path.indexOf("AV Packages");
	if (index != -1) {
	    path = path.substring(index);
	}
	return path;
    }

    /*
     * Internal Classes
     */
    static class ARMO_spec extends SpecHolder {

	ARMO armo;

	ARMO_spec(ARMO armoSrc) {
	    this.armo = armoSrc;
	    spec = new SpecVariant();
	}

	ARMO_spec(ARMO armo, SpecVariant spec) {
	    this.armo = armo;
	    this.spec = spec;
	}
    }

    static class WEAP_spec extends SpecHolder {

	WEAP weap;

	WEAP_spec(WEAP weapSrc) {
	    this.weap = weapSrc;
	    spec = new SpecVariant();
	}

	WEAP_spec(WEAP weap, SpecVariant spec) {
	    this.weap = weap;
	    this.spec = spec;
	}
    }

    static class SpecHolder {

	SpecVariant spec;
    }

    static class AV_Race {

	RACE race;
	Map<FormID, LMergeMap<FormID, ARMO_spec>> variantMap = new HashMap<>();
	FLST AltOptions;
	FLST Cells;
	ArrayList<FormID> skinKey = new ArrayList<>();

	public AV_Race(FormID id) {
	    race = (RACE) SPDatabase.getMajor(id, GRUP_TYPE.RACE);
	    AltOptions = new FLST("AV_" + race.getEDID() + "_flst");
	}

	final public Set<FormID> getCells() {
	    HashSet<FormID> out = new HashSet<>();
	    for (LMergeMap<FormID, ARMO_spec> skin : variantMap.values()) {
		out.addAll(skin.keySet());
	    }
	    return out;
	}

	public ArrayList<ARMO_spec> getVariants() {
	    ArrayList<ARMO_spec> out = new ArrayList<>();
	    for (LMergeMap<FormID, ARMO_spec> skin : variantMap.values()) {
		out.addAll(skin.valuesFlat());
	    }
	    return out;
	}
    }
}
