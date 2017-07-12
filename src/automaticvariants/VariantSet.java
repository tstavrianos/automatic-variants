/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import automaticvariants.AVSaveFile.Settings;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import lev.LMergeMap;
import lev.Ln;
import skyproc.*;

/**
 *
 * @Author Justin Swanson
 */
public class VariantSet extends PackageNode implements Serializable {

    public SpecVariantSet spec;
    static String setMeshName = "[Global Meshes]";
    static String depth = "* +";
    ArrayList<Variant> flat;

    VariantSet(File setDir) {
	super(setDir, Type.VARSET);
	spec = new SpecVariantSet(src);
    }

    final public boolean loadVariants() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + " Adding Variant Set: " + src);
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth);
	}

	ArrayList<File> files = new ArrayList<>(Arrays.asList(src.listFiles()));

	for (File f : files) {
	    if (AVFileVars.isSpec(f)) {
		try {
		    spec = AV.gson.fromJson(new FileReader(f), SpecVariantSet.class);
		    if (spec != null) {
			spec.src = f;
			if (SPGlobal.logging()) {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName());
			}
		    }
		} catch (com.google.gson.JsonSyntaxException ex) {
		    SPGlobal.logException(ex);
		    JOptionPane.showMessageDialog(null, "Variant set " + f.getPath() + " had a bad specifications file.  Skipped.");
		} catch (FileNotFoundException ex) {
		    SPGlobal.logException(ex);
		}

	    } else if (f.isDirectory()) {
		if (f.getName().equalsIgnoreCase(setMeshName)) {
		    if (SPGlobal.logging()) {
			SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "   ********* Loading Global Meshes");
		    }
		    PackageNode globalMeshDirN = new PackageNode(f, Type.GLOBALMESHSET);
		    add(globalMeshDirN);
		    for (File globalMeshDir : f.listFiles()) {
			VariantGlobalMesh ms = new VariantGlobalMesh(globalMeshDir);
			ms.load();
			globalMeshDirN.add(ms);
		    }
		    if (SPGlobal.logging()) {
			SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "   ******************");
		    }
		} else {
		    VariantGroup v = new VariantGroup(f);
		    v.load();
		    add(v);
		}

	    } else if (AVFileVars.isDDS(f)) {
		PackageNode c = new PackageNode(f, Type.GENTEXTURE);
		add(c);
		if (SPGlobal.logging()) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "   Loaded common texture: " + f);
		}
	    } else if (AVFileVars.isNIF(f)) {
		PackageNode c = new PackageNode(f, Type.GENMESH);
		add(c);
		if (SPGlobal.logging()) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "   Loaded common mesh: " + f);
		}
	    } else if (AVFileVars.isReroute(f)) {
		RerouteFile c = new RerouteFile(f);
		if (AVFileVars.isDDS(c.src)) {
		    c.type = PackageNode.Type.GENTEXTURE;
		    if (SPGlobal.logging()) {
			SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "   Loaded ROUTED common texture: " + c.src);
		    }
		}
		add(c);
	    } else if (SPGlobal.logging()) {
		SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "   Skipped file: " + f);
	    }
	}

	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "");
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "++++++ END Variant Set: " + src);
	}
	return true;
    }

    ArrayList<VariantGroup> getGroups() {
	ArrayList<PackageNode> groups = getAll(Type.VARGROUP);
	ArrayList<VariantGroup> out = new ArrayList<>(groups.size());
	for (PackageNode p : groups) {
	    out.add((VariantGroup) p);
	}
	return out;
    }

    ArrayList<VariantGlobalMesh> getGlobalMeshes() {
	ArrayList<VariantGlobalMesh> out = new ArrayList<>();
	for (PackageNode globalMeshSet : getAll(Type.GLOBALMESHSET)) {
	    ArrayList<PackageNode> globalMeshes = globalMeshSet.getAll(Type.GLOBALMESH);
	    for (PackageNode p : globalMeshes) {
		out.add((VariantGlobalMesh) p);
	    }
	}
	return out;
    }

    ArrayList<Variant> multiplyAndFlatten() {
	if (flat != null) {
	    return flat;
	}
	return multiplyAndFlatten(getGlobalMeshes());
    }

    ArrayList<Variant> multiplyAndFlatten(ArrayList<VariantGlobalMesh> globalMeshes) {
	if (flat == null) {
	    // Flatten Groups
	    mergeInGlobals();
	    flat = new ArrayList<>();
	    ArrayList<VariantGroup> groups = getGroups();
	    if (!groups.isEmpty()) {
		flat.addAll(groups.get(0).getVariants());
		if (groups.size() > 1) {
		    for (int i = 1; i < groups.size(); i++) {
			ArrayList<Variant> groupVars = groups.get(i).getVariants();
			ArrayList<Variant> tmp = new ArrayList<>(flat.size() * groupVars.size());
			for (Variant a : flat) {
			    for (Variant b : groupVars) {
				Variant merge = a.merge(b);
				tmp.add(merge);
			    }
			}
			flat = tmp;
		    }

		    // Find average group size and adjust probability
		    float sum = 0;
		    for (VariantGroup g : groups) {
			sum += g.getAll(Type.VAR).size();
		    }
		    sum /= groups.size();
		    int avg = Math.round(sum);

		    for (Variant v : flat) {
			v.spec.Probability_Divider *= (avg * (groups.size() - 1));
		    }
		}
	    }

	    //Multiply in Global Meshes
	    ArrayList<Variant> tmp = new ArrayList<>(flat);
	    for (VariantGlobalMesh globalMesh : globalMeshes) {
		for (Variant v : tmp) {
		    Variant copy = new Variant(v);
		    copy.absorbGlobalMesh(globalMesh);
		    flat.add(copy);
		}
	    }
	}
	// Warn about large multiplies
	if (flat.size() > 10000
		&& !AV.save.getStrings(Settings.LARGE_MULTIPLY_WARNING).contains(src.getPath())) {
	    int response = JOptionPane.showConfirmDialog(null, "Variant set had an abnormally large number of resulting variants: " + flat.size() + "\n"
		    + src.getPath() + "\n\n"
		    + "This can cause the patcher to take a long time to patch,\n"
		    + "and/or cause stability problems for Skyrim.\n\nStop the patcher by clicking the big red X on the\n"
		    + "progress bar if you want to cancel.\n\n"
		    + "Do you want AV to stop warning you of this particular set's size?",
		    "Large Variant Set Warning", JOptionPane.YES_NO_OPTION);
	    if (response == JOptionPane.YES_OPTION) {
		AV.save.addString(Settings.LARGE_MULTIPLY_WARNING, src.getPath());
	    }
	}
	return flat;
    }

    @Override
    public void consolidateCommonFiles() throws FileNotFoundException, IOException {
	// First, check current common textures and see if any groups have them
	SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "Consolidating common files");
	SPGlobal.flush();
	for (VariantGroup g : getGroups()) {
	    g.deleteMatches(toFiles(getAll(Type.GENTEXTURE)));
	}

	// Check each Variant against each other and return any files that are common to all.
	ArrayList<PackageNode> commonFiles = new ArrayList<>();
	ArrayList<File> moved = new ArrayList<>();
	for (VariantGroup g : getGroups()) {
	    commonFiles.addAll(g.consolidateCommonFilesInternal());
	}
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "Common Files:");
	    for (PackageNode c : commonFiles) {
		SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  " + c.src.getName());
	    }
	}
	for (PackageNode c : commonFiles) {
	    File dest = new File(c.src.getParentFile().getParentFile().getParent() + "/" + c.src.getName());
	    if (!Ln.moveFile(c.src, dest, true)) {
		SPGlobal.logError(src.getName(), "!!!" + c.src + " was NOT successfully moved.");
	    }
	    moved.add(dest);
	}

	// See if any new common textures are present in other groups
	for (VariantGroup g : getGroups()) {
	    g.deleteMatches(moved);
	}
    }

    @Override
    public LMergeMap<File, File> getDuplicateFiles() throws FileNotFoundException, IOException {
	LMergeMap<File, File> duplicates = new LMergeMap<>(false);
	for (VariantGroup group : getGroups()) {
	    for (Variant var : group.getVariants()) {
		for (PackageNode tex : var.getAll(Type.TEXTURE)) {
		    if (!tex.getClass().equals(RerouteFile.class)) {
			boolean found = false;
			for (File key : duplicates.keySet()) {
			    if (Ln.validateCompare(tex.src, key, 0)) {
				if (SPGlobal.logging()) {
				    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  " + tex.src);
				    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  was the same as ");
				    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  " + key);
				}
				duplicates.put(key, tex.src);
				found = true;
				break;
			    }
			}
			if (!found) {
			    if (SPGlobal.logging()) {
				SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  UNIQUE: " + tex.src);
			    }
			    duplicates.put(tex.src, tex.src);
			}
			SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), "  --------------------------");
		    }
		}
	    }
	}
	return duplicates;
    }

    public boolean isEmpty() {
	if (getAll(Type.VARGROUP).isEmpty()) {
	    return true;
	}
	for (VariantGroup g : getGroups()) {
	    if (!g.getAll(Type.VAR).isEmpty()) {
		return false;
	    }
	}
	return true;
    }

    public ArrayList<NPC_> getSeedNPCs() {
	ArrayList<NPC_> seedNPCs = new ArrayList<>();
	for (String[] s : spec.Target_FormIDs) {
	    FormID id = new FormID(s[0], s[1]);
	    NPC_ npc = (NPC_) SPDatabase.getMajor(id, GRUP_TYPE.NPC_);
	    if (npc == null) {
		SPGlobal.logError(src.getPath(), "Could not locate NPC with FormID: " + id);
		continue;
	    } else {
		seedNPCs.add(npc);
	    }
	}
	return seedNPCs;
    }

    public ArrayList<Seed> getSeeds() {
	ArrayList<Seed> out = new ArrayList<>();
	for (String[] s : spec.Target_FormIDs) {
	    ArrayList<FormID> ids = new ArrayList<>();
	    for (int i = 2; i < s.length + 2; i += 2) {
		FormID id = new FormID(s[i - 2], s[i - 1]);
		ids.add(id);
	    }
	    Seed seed;
	    switch (spec.getType()) {
		case WEAP:
		    seed = new SeedWEAP();
		    break;
		default:
		    seed = new SeedNPC();
	    }
	    if (seed.load(ids)) {
		out.add(seed);
	    }
	}
	return out;
    }

    public Set<String> getTextures() {
	Set<String> out = new HashSet<>();
	for (PackageNode p : getAll(Type.GENTEXTURE)) {
	    out.add(p.src.getName().toUpperCase());
	}
	for (VariantGroup g : getGroups()) {
	    for (Variant v : g.getVariants()) {
		for (PackageNode p : v.getAll(Type.TEXTURE)) {
		    out.add(p.src.getName().toUpperCase());
		}
	    }
	}
	return out;
    }

    @Override
    public void finalizeComponent() {
	mergeInGlobals();
	super.finalizeComponent();
    }

    public void mergeInGlobals() {
	ArrayList<PackageNode> globals = getAll(Type.GENTEXTURE);
	globals.addAll(getAll(Type.GENMESH));
	for (VariantGroup g : getGroups()) {
	    g.mergeInGlobals(globals);
	}
    }

    public AVPackage getPackage() {
	return (AVPackage) getParent();
    }

    @Override
    public String printSpec() {
	if (spec != null) {
	    return spec.printHelpInfo() + divider;
	} else {
	    return "MISSING SPEC FILE!" + divider;
	}
    }

    @Override
    public String printName(String spacer) {
	PackageNode p = (PackageNode) this.getParent();
	return p.printName(spacer) + spacer + src.getName();
    }
}
