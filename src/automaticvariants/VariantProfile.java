/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.DataFormatException;
import lev.LShrinkArray;
import skyproc.NIF.TextureSet;
import skyproc.*;

/**
 *
 * @author Justin Swanson
 */
abstract public class VariantProfile<T extends MajorRecord> {

    static int nextID = 0;
    static Map<String, Map<Integer, TextureSet>> nifInfoDatabase = new HashMap<>();
    Map<Integer, ArrayList<String>> textures = new HashMap<>();
    Map<Integer, ArrayList<String>> altTextures = new HashMap<>();
    String texturesPrintout;
    Set<String> texturesFlat;
    Set<String> textureNames;
    ArrayList<VariantSet> matchedVariantSets = new ArrayList<>();
    public int ID;

    VariantProfile() {
	ID = nextID++;
    }

    VariantProfile(VariantProfile<T> rhs) {
	this();
	textures = new HashMap<>();
	for (Integer key : rhs.textures.keySet()) {
	    ArrayList<String> list = new ArrayList<>();
	    for (String value : rhs.textures.get(key)) {
		list.add(value);
	    }
	    textures.put(key, list);
	}
	matchedVariantSets = new ArrayList<>(rhs.matchedVariantSets);
    }

    public boolean loadAltTextures(ArrayList<AltTextures.AltTexture> recordAltTextures) {
	for (AltTextures.AltTexture altTex : recordAltTextures) {
	    TXST txst = (TXST) SPDatabase.getMajor(altTex.getTexture(), GRUP_TYPE.TXST);
	    if (txst == null) {
		SPGlobal.logError(toString(), "Error locating txst with formID: " + altTex.getTexture());
		continue;
	    }
	    ArrayList<String> txstTextures = txst.getTextures();
	    if (!textures.containsKey(altTex.getIndex())) {
		SPGlobal.logError(getNifPath(), "Skipping profile " + toString() + ", because it did not have a nif node name of: " + altTex.getName());
		return false;
	    }
	    altTextures.put(altTex.getIndex(), new ArrayList<String>());
	    ArrayList<String> profileAltTextures = altTextures.get(altTex.getIndex());
	    for (int i = 0; i < txstTextures.size(); i++) {
		if (txstTextures.get(i) == null) {
		    profileAltTextures.add("");
		} else {
		    profileAltTextures.add("TEXTURES\\" + txstTextures.get(i).toUpperCase());
		}
	    }
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 29 * hash + this.ID;
	return hash;
    }

    public void finalizeProfile() {
	for (Integer key : altTextures.keySet()) {
	    ArrayList<String> tex = textures.get(key);
	    ArrayList<String> altTex = altTextures.get(key);
	    for (int i = 0; i < tex.size() && i < altTex.size(); i++) {
		tex.set(i, altTex.get(i));
	    }
	}
	altTextures.clear();
    }

    public boolean catalogNif(String nifPath) {
	if (!nifInfoDatabase.containsKey(nifPath)) {
	    try {
		LShrinkArray nifRawData = BSA.getUsedFile(nifPath);
		if (nifRawData != null) {
		    ArrayList<TextureSet> nifTextures = VariantFactory.loadNif(nifPath, nifRawData);
		    Map<Integer, TextureSet> nifData = new HashMap<>();
		    nifInfoDatabase.put(nifPath.toUpperCase(), nifData);
		    for (TextureSet t : nifTextures) {
			textures.put(t.getIndex(), t.getTextures());
			nifData.put(t.getIndex(), t);
		    }
		    return true;
		} else {
		    SPGlobal.log(toString(), " * Could not catalog nif because it could not find file: " + nifPath);
		}
	    } catch (IOException | DataFormatException ex) {
		SPGlobal.logException(ex);
	    }
	}
	return false;
    }

    @Override
    public String toString() {
	return "ID: " + ID;
    }

    public void print() {
	SPGlobal.log(toString(), " /========================================");
	SPGlobal.log(toString(), "| Profile Records and NIF: ");
	printShort();
	SPGlobal.log(toString(), " \\========================================");
	SPGlobal.log(toString(), "    \\===== NIF nodes and Textures Used: ==");
	for (Integer n : textures.keySet()) {
	    SPGlobal.log(toString(), "    |===== " + n + " ====/");
	    SPGlobal.log(toString(), "    |=========================/");
	    int i = 0;
	    for (String s : textures.get(n)) {
		SPGlobal.log(toString(), "    | " + i++ + ": " + s);
	    }
	    SPGlobal.log(toString(), "    \\=====================================");
	}
	SPGlobal.log(toString(), "");
    }

    public abstract void printShort();

    public String profileHashCode() {
	return getSeed().getSeedHashCode();
    }

    public boolean absorb(VariantSet varSet, Collection<Seed> seeds) {
	for (Seed s : seeds) {
	    if (getSeed().equals(s)) {
		matchedVariantSets.add(varSet);
		return true;
	    }
	}
	return false;
    }

    public String generateEDID(Variant var) {
	return "AV_" + profileHashCode() + "_" + var.printName("_");
    }

    public abstract String getNifPath();

    public abstract Seed getSeed();

    public final void generateRecords() {
	for (VariantSet varSet : matchedVariantSets) {
	    if (SPGlobal.logging()) {
		SPGlobal.log(toString(), " *************> Generating set " + varSet.printName("-"));
	    }
	    ArrayList<Variant> vars = varSet.multiplyAndFlatten(getGlobalMeshes());
	    for (Variant var : vars) {
		if (SPGlobal.logging()) {
		    SPGlobal.log(toString(), " ***************> Generating var " + var.printName("-"));
		}
		generateRecord(var);
		if (SPGlobal.logging()) {
		    SPGlobal.log(toString(), " ******************************>");
		    SPGlobal.log(toString(), "");
		}
	    }
	}
    }

    public abstract void generateRecord(Variant var);

    public ArrayList<VariantGlobalMesh> getGlobalMeshes() {
	ArrayList<VariantGlobalMesh> globalMeshes = new ArrayList<>();
	for (VariantSet varSet : matchedVariantSets) {
	    globalMeshes.addAll(varSet.getGlobalMeshes());
	}
	return globalMeshes;
    }

    public String getNifPath(Variant var, boolean firstPerson) {
	String nif = getNifPathInternal(var, firstPerson);
	catalogNif(nif);
	return nif;
    }

    private String getNifPathInternal(Variant var, boolean firstPerson) {
	String targetNifPath;
	ArrayList<PackageNode> varNifs = var.getAll(PackageNode.Type.MESH);
	String firstPersonPath = var.spec.FirstPersonModelName.toUpperCase();
	if (varNifs.size() > 0) {
	    for (int i = 0; i < varNifs.size(); i++) {
		targetNifPath = varNifs.get(i).src.getPath();
		if ("".equals(firstPersonPath)
			|| firstPerson == targetNifPath.toUpperCase().endsWith(firstPersonPath)) {
		    SPGlobal.log(toString(), " * Using variant nif file: " + targetNifPath);
		    return targetNifPath;
		}
	    }
	}

	// Look in template for NIF path
	if (var.isTemplated()) {
	    FormID templateID = new FormID(var.spec.Template_Form);
	    String template = getNif(templateID, firstPerson);
	    if (!"".equals(template)) {
		SPGlobal.log(toString(), " * Using template nif file: " + template);
		return template;
	    }
	}

	// If first person, and cannot find.. revert to 3rd person
	if (firstPerson) {
	    return getNifPath(var, false);
	}

	// Cannot find any NIF replacements
	targetNifPath = getNifPath();
	SPGlobal.log(toString(), " * Using default nif file: " + targetNifPath);
	return targetNifPath;
    }

    public abstract String getNif(FormID id, boolean firstPerson);

    public String getCleanNifPath(Variant var, boolean firstPerson) {
	return getCleanNifPath(getNifPath(var, firstPerson));
    }

    public String getCleanNifPath(String path) {
	if (path.indexOf("MESHES\\") == 0) {
	    path = path.substring(7);
	}
	return path;
    }

    public void loadAltTextures(ArrayList<AltTextures.AltTexture> alts, Map<String, TXST> txsts, String nifPath) {
	alts.clear();

	Map<Integer, TextureSet> nifInfo = nifInfoDatabase.get(nifPath.toUpperCase());
	for (Integer index : nifInfo.keySet()) {
	    String nifNodeName = nifInfo.get(index).getName();
	    if (txsts.containsKey(nifNodeName)) {
		if (SPGlobal.logging()) {
		    SPGlobal.log(toString(), " * | Loading TXST for " + nifNodeName + " index " + index);
		}
		alts.add(new AltTextures.AltTexture(nifNodeName, txsts.get(nifNodeName).getForm(), index));
	    }
	}
    }

    public Map<String, TXST> generateTXSTs(Variant var, String nifPath) {
	if (SPGlobal.logging()) {
	    SPGlobal.log(toString(), " * ==> Generating TXSTs");
	}
	Map<String, TXST> out = new HashMap<>();

	Map<Integer, TextureSet> nifInfo = nifInfoDatabase.get(nifPath.toUpperCase());
	for (Integer index : nifInfo.keySet()) {
	    String nodeName = nifInfo.get(index).getName();
	    if (shouldGenerate(var, index)) {
		String edid = NiftyFunc.EDIDtrimmer(generateEDID(var) + "_" + nodeName + "_txst");
		if (SPGlobal.logging()) {
		    SPGlobal.log(toString(), " * | Generating: " + edid);
		}

		// Create TXST
		TXST txst = new TXST(edid);
		txst.set(TXST.TXSTflag.FACEGEN_TEXTURES, true);

		// For each texture there normally...
		ArrayList<File> varFiles = var.getTextureFiles();
		for (int i = 0; i < textures.get(index).size(); i++) {
		    String texture = textures.get(index).get(i);
		    if (texture.length() < 9) {
			continue;
		    }
		    texture = texture.substring(9);
		    if (texture.equals("")) {
			continue;
		    }
		    // Then check if there is a variant file that matches
		    int set = readjustTXSTindices(i);
		    txst.setNthMap(set, texture);
		    for (File varFile : varFiles) {
			if (texture.contains(varFile.getName().toUpperCase())) {
			    // And then sub it in the TXST
			    String varTex = varFile.getPath();
			    varTex = varTex.substring(varTex.indexOf("AV Packages"));
			    txst.setNthMap(set, varTex);
			    if (SPGlobal.logging()) {
				SPGlobal.log(toString(), " * |    Loading " + i + ": " + varTex);
			    }
			    break;
			}
		    }
		}

		txst = (TXST) NiftyFunc.mergeDuplicate(txst);

		out.put(nodeName, txst);
	    }
	}
	if (SPGlobal.logging()) {
	    SPGlobal.log(toString(), " * =====================================>");
	}
	return out;
    }

    static int readjustTXSTindices(int j) {
	// Because nif fields map 2->3 if facegen flag is on.
	if (j == 2) {
	    return 3;
	}
	return j;
    }

    public boolean shouldGenerate(Variant var, int index) {
	// Also need to check if TXST already exists with data
	return profileContainsVarTex(var, index);
    }

    public boolean profileContainsVarTex(Variant var, int index) {
	ArrayList<String> varTextures = var.getTextureNames();
	for (String profileTexture : textures.get(index)) {
	    if (!"".equals(profileTexture)) {
		for (String varTex : varTextures) {
		    if (profileTexture.contains(varTex)) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    T generateFor(T in, String varEDID) {
	String edid = varEDID + "_" + in.getEDID() + "_" + in.getType();
	T dup = (T) SPGlobal.getGlobalPatch().makeCopy(in, edid);
	if (SPGlobal.logging()) {
	    SPGlobal.log(toString(), " * ==> Generated : " + dup);
	}
	return dup;
    }

    public T getTemplateRecord(Variant var) {
	FormID templateF = new FormID(var.spec.Template_Form);
	T template = (T) SPDatabase.getMajor(templateF);
	if (template == null) {
	    AVFileVars.importTemplateMod(templateF.getMaster());
	    template = (T) SPDatabase.getMajor(templateF);
	}
	if (template != null) {
	    ArrayList<MajorRecord> copies = NiftyFunc.deepCopySubRecords(template, template.getFormMaster());
	    if (SPGlobal.logging()) {
		for (MajorRecord m : copies) {
		    SPGlobal.log(toString(), " *   Deep Copied : " + m);
		}
	    }
	}
	return template;
    }

}
