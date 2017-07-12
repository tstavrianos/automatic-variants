/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import skyproc.genenums.Perspective;
import skyproc.genenums.Gender;
import automaticvariants.AVFileVars.ARMO_spec;
import automaticvariants.AVFileVars.SpecHolder;
import automaticvariants.SpecVariantSet.VariantType;
import java.util.*;
import lev.LMergeMap;
import skyproc.*;

/**
 *
 * @author Justin Swanson
 */
public class VariantFactoryNPC extends VariantFactory<VariantProfileNPC> {

    static String header = "VariantFactory - NPC";
    static String raceAttachScript = "AVRaceAttachment";
    static public HashSet<FormID> unusedRaces;
    static public HashSet<FormID> unusedSkins;
    static public LMergeMap<FormID, FormID> unusedPieces;
    static Map<FormID, LMergeMap<FormID, AVFileVars.ARMO_spec>> armors = new HashMap<>();
    static Map<FormID, AVFileVars.AV_Race> AVraces = new HashMap<>();

    @Override
    public void locateUnused() {

	if (unusedRaces != null) {
	    return;
	}

	Mod source = AV.getMerger();

	// Load all races, skins, pieces into containers
	unusedRaces = new HashSet<>(source.getRaces().numRecords());
	for (RACE race : source.getRaces()) {
	    unusedRaces.add(race.getForm());
	}
	unusedSkins = new HashSet<>(source.getArmors().numRecords());
	unusedPieces = new LMergeMap<>(false);
	LMergeMap<FormID, ARMA> unusedPiecesTmp = new LMergeMap<>(false);
	for (ARMO armor : source.getArmors()) {
	    if (!unusedSkins.contains(armor.getForm())) {
		unusedSkins.add(armor.getForm());
		for (FormID piece : armor.getArmatures()) {
		    ARMA arma = (ARMA) SPDatabase.getMajor(piece, GRUP_TYPE.ARMA);
		    if (arma != null) {
			unusedPiecesTmp.put(armor.getForm(), arma);
		    }
		}
	    }
	}


	// Removed used races/skins/pieces
	for (NPC_ n : source.getNPCs()) {
	    FormID skin = getUsedSkin(n);
	    if (AV.block.contains(skin)) {
		continue;
	    }
	    unusedRaces.remove(n.getRace());
	    unusedSkins.remove(skin);
	    if (unusedPiecesTmp.containsKey(skin)) {
		ArrayList<ARMA> tmpPieces = new ArrayList<>(unusedPiecesTmp.get(skin));
		for (ARMA piece : tmpPieces) {
		    if (piece.getRace().equals(n.getRace())) {
			unusedPiecesTmp.get(skin).remove(piece);
		    }
		}
	    }
	}

	// Load unused armor pieces into final container
	for (FormID skin : unusedPiecesTmp.keySet()) {
	    if (!unusedPiecesTmp.get(skin).isEmpty()) {
		for (ARMA piece : unusedPiecesTmp.get(skin)) {
		    unusedPieces.put(skin, piece.getForm());
		}
	    } else {
		unusedPieces.put(skin, new ArrayList<FormID>(0));
	    }
	}

	if (SPGlobal.logging()) {
	    SPGlobal.log(header, "Unused Races:");
	    for (FormID race : unusedRaces) {
		SPGlobal.log(header, "  " + SPDatabase.getMajor(race, GRUP_TYPE.RACE));
	    }
	    SPGlobal.log(header, "Unused Skins:");
	    for (FormID skin : unusedSkins) {
		SPGlobal.log(header, "  " + SPDatabase.getMajor(skin, GRUP_TYPE.ARMO));
	    }
	    SPGlobal.log(header, "Unused Pieces:");
	    for (FormID skin : unusedPieces.keySet()) {
		SPGlobal.log(header, "  For " + SPDatabase.getMajor(skin, GRUP_TYPE.ARMO));
		for (FormID piece : unusedPieces.get(skin)) {
		    SPGlobal.log(header, "    " + SPDatabase.getMajor(piece, GRUP_TYPE.ARMA));
		}
	    }
	}
    }

    static public FormID getUsedSkin(NPC_ npcSrc) {
	if (!npcSrc.getSkin().equals(FormID.NULL)) {
	    return npcSrc.getSkin();
	} else {
	    RACE race = (RACE) SPDatabase.getMajor(npcSrc.getRace(), GRUP_TYPE.RACE);
	    if (race == null) {
		return null;
	    }
	    if (!race.getWornArmor().equals(FormID.NULL)) {
		return race.getWornArmor();
	    } else {
		return null;
	    }
	}
    }

    @Override
    public void loadProfileRecords() {
	SPGlobal.log(header, "===========================================================");
	SPGlobal.log(header, "==============      Loading NPC Records     ===============");
	SPGlobal.log(header, "===========================================================");
	for (ARMO armo : AV.getMerger().getArmors()) {
	    if (!isUnused(armo.getForm())) {
		if (!AV.block.contains(armo.getForm())) {
		    loadProfileSkin(armo);
		} else {
		    SPGlobal.log(header, "Blocked because it was on the blocklist: " + armo);
		}
	    }
	}
    }

    public void loadProfileSkin(ARMO armo) {
	for (FormID armaForm : armo.getArmatures()) {
	    // If a used piece
	    if ((!unusedPieces.containsKey(armo.getForm())
		    || !unusedPieces.get(armo.getForm()).contains(armaForm))
		    && !AV.block.contains(armo.getForm())) {

		ARMA arma = (ARMA) SPDatabase.getMajor(armaForm, GRUP_TYPE.ARMA);

		// Make sure it has a race
		if (arma == null) {
		    SPGlobal.logError(header, "FormID " + armaForm + " skipped because it couldn't be found in mods.");
		    continue;
		}
		if (arma.getRace().isNull()) {
		    SPGlobal.logError(header, arma + " skipped because it had no race.");
		    continue;
		}

		// Find profile with that nif
		String nifPath = "MESHES\\" + arma.getModelPath(Gender.MALE, Perspective.THIRD_PERSON).toUpperCase();
		VariantProfileNPC profile = find(null, null, null, nifPath);


		if (profile != null) {
		    try {
			Set<RACE> races = new HashSet<>();
			races.add((RACE) SPDatabase.getMajor(arma.getRace(), GRUP_TYPE.RACE));
			for (FormID raceID : arma.getAdditionalRaces()) {
			    races.add((RACE) SPDatabase.getMajor(raceID, GRUP_TYPE.RACE));
			}

			for (RACE r : races) {
			    //If profile is already filled, make duplicate
			    if (profile.getRace() != null) {
				SPGlobal.log(header, "Duplicating for " + profile.nifPath + " || " + profile.getRace());
				profile = new VariantProfileNPC(profile);
				profiles.add(profile);
			    }
			    //Load in record setup
			    profile.seed = new SeedNPC(r, armo, arma);

			    //Load Alt Textures
			    if (!profile.loadAltTextures(arma.getAltTextures(Gender.MALE, Perspective.THIRD_PERSON))) {
				profiles.remove(profile);
			    }
			}
		    } catch (Exception ex) {
			SPGlobal.logError(header, "Skipping profile " + profile + " because an exception occured.");
			SPGlobal.logException(ex);
			remove(profile);
		    }
		} else {
		    SPGlobal.log(header, "Skipped " + arma + ", could not find a profile matching nif: " + nifPath);
		}
	    }
	}
    }

    @Override
    public void createProfileShells() {
	SPGlobal.log(header, "====================================================================");
	SPGlobal.log(header, "===================      Creating NPC Profiles     =================");
	SPGlobal.log(header, "====================================================================");
	for (ARMO armo : AV.getMerger().getArmors()) {
	    if (!unusedSkins.contains(armo.getForm()) && !AV.block.contains(armo.getForm())) {
		for (FormID piece : armo.getArmatures()) {
		    if (!unusedPieces.containsKey(armo.getForm())
			    || !unusedPieces.get(armo.getForm()).contains(piece)) {
			ARMA arma = (ARMA) SPDatabase.getMajor(piece, GRUP_TYPE.ARMA);
			if (arma == null) {
			    if (SPGlobal.logging()) {
				SPGlobal.log(header, "Skipping " + piece + " because it didn't exist.");
			    }
			    continue;
			} else if (SPGlobal.logging()) {
			    SPGlobal.log(header, "Loading " + arma);
			}
			String nifPath = "MESHES\\" + arma.getModelPath(Gender.MALE, Perspective.THIRD_PERSON).toUpperCase();
			if (nifPath.equals("MESHES\\")) {
			    SPGlobal.log(header, "Skipping " + arma + " because it had no nif.");
			    continue;
			}
			if (find(null, null, null, nifPath) == null) {
			    VariantProfileNPC profile = new VariantProfileNPC();
			    profiles.add(profile);
			    profile.nifPath = nifPath;
			    if (SPGlobal.logging()) {
				SPGlobal.log(header, "Created profile " + profile);
			    }
			}
		    }
		}
	    }
	}
    }

    @Override
    boolean isUnused(FormID id) {
	return unusedSkins.contains(id)
		|| unusedRaces.contains(id)
		|| unusedPieces.containsValue(id);
    }

    @Override
    public void implementOriginalAsVar() {
	for (FormID armoSrc : armors.keySet()) {
	    ARMO src = (ARMO) SPDatabase.getMajor(armoSrc, GRUP_TYPE.ARMO);
	    for (FormID race : armors.get(armoSrc).keySet()) {
		armors.get(armoSrc).put(race, new ARMO_spec(src));
	    }
	}
    }

    @Override
    public void createStructureRecords(Mod source) {

	createAVRaceObjects();

	setUpExclusiveCellList();

	// Generate FormLists of RACE variants
	generateFormLists(source);

	// Generate Spells that use Race Switcher Magic Effects
	generateSPELvariants(source);

	// Add AV keywords to NPCs that have alt skins
	tagNPCs(source);
    }

    static void createAVRaceObjects() {
	for (FormID armoSrcForm : armors.keySet()) {
	    for (FormID race : armors.get(armoSrcForm).keySet()) {
		AVraces.put(race, new AVFileVars.AV_Race(race));
	    }
	}
	for (FormID armoSrcForm : armors.keySet()) {
	    for (FormID race : armors.get(armoSrcForm).keySet()) {
		ArrayList<AVFileVars.ARMO_spec> armoVars = armors.get(armoSrcForm).get(race);
		LMergeMap<FormID, AVFileVars.ARMO_spec> cellToARMO = new LMergeMap<>(false);
		for (AVFileVars.ARMO_spec armorSpec : armoVars) {
		    Set<FormID> cells = armorSpec.spec.getRegions();
		    if (cells.isEmpty() || AV.save.getInt(AVSaveFile.Settings.PACKAGES_ALLOW_EXCLUSIVE_REGION) < 1) {
			cellToARMO.put(FormID.NULL, armorSpec);
		    } else {
			for (FormID cell : armorSpec.spec.getRegions()) {
			    cellToARMO.put(cell, armorSpec);
			}
		    }
		}

		AVraces.get(race).variantMap.put(armoSrcForm, cellToARMO);
	    }
	}
    }

    static void setUpExclusiveCellList() {
	// If not allowed, leave it empty and return
	if (AV.save.getInt(AVSaveFile.Settings.PACKAGES_ALLOW_EXCLUSIVE_REGION) < 2) {
	    return;
	}
	ArrayList<Variant> vars = AVFileVars.AVPackages.getVariants();
	Set<FormID> eCells = new HashSet<>();
	for (Variant var : vars) {
	    if (var.spec.Exclusive_Region) {
		for (String[] formID : var.spec.Region_Include) {
		    FormID id = FormID.parseString(formID);
		    if (!id.isNull()) {
			if (SPGlobal.logging() && !eCells.contains(id)) {
			    SPGlobal.log("Exclusive Cells", "Adding exclusive cell " + id + " from var " + var + " with source " + var.src);
			}
			eCells.add(id);
		    }
		}
	    }
	}
	AV.quest.getScriptPackage().getScript("AVQuestScript").setProperty("ExclusiveCellList", eCells.toArray(new FormID[0]));
    }

    void generateFormLists(Mod source) {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Generate Form Lists.txt");
	    SPGlobal.log(header, "====================================================================");
	    SPGlobal.log(header, "Generating FormLists for each ARMO variant");
	    SPGlobal.log(header, "====================================================================");
	}
	// For each race with variants
	for (FormID raceID : AVraces.keySet()) {
	    AVFileVars.AV_Race avr = AVraces.get(raceID);
	    RACE raceSrc = avr.race;
	    if (SPGlobal.logging()) {
		SPGlobal.log(header, "  Generating for race " + raceSrc);
	    }

	    // Generate Cell Index FLST
	    avr.Cells = new FLST("AV_" + avr.race.getEDID() + "_cells_flst");
	    for (FormID cell : avr.getCells()) {
		if (!cell.isNull()) {
		    avr.Cells.addFormEntryAtIndex(cell, 0);
		}
	    }

	    // For each skin with variants applied to that race
	    for (FormID skinID : avr.variantMap.keySet()) {
		ARMO skinSrc = (ARMO) SPDatabase.getMajor(skinID, GRUP_TYPE.ARMO);
		FLST flstSkin = new FLST("AV_" + skinSrc.getEDID() + "_" + raceSrc.getEDID() + "_flst");
		if (raceSrc.getWornArmor().equals(skinID)) {
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "    Generating for normal skin " + skinSrc);
		    }
		    avr.AltOptions.addFormEntryAtIndex(flstSkin.getForm(), 0);
		    avr.skinKey.add(0, skinID);
		} else {
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "    Generating for alt skin " + skinSrc);
		    }
		    avr.AltOptions.addFormEntry(flstSkin.getForm());
		    avr.skinKey.add(skinID);
		}

		// Calculate the lowest common mult between variant probablity dividers
		int lowestCommMult = calcLCM(avr.variantMap.get(skinID).valuesFlat());

		// For each cell
		for (FormID cell : avr.getCells()) {
		    FLST flstCell = new FLST("AV_" + skinSrc.getEDID() + "_" + raceSrc.getEDID() + "_cell_ " + cell.getFormStr() + "_flst");
		    if (!cell.isNull()) {
			flstSkin.addFormEntryAtIndex(flstCell.getForm(), 0);
		    } else {
			flstSkin.addFormEntry(flstCell.getForm());
		    }

		    if (avr.variantMap.get(skinID).containsKey(cell)) {
			if (SPGlobal.logging()) {
			    SPGlobal.log(header, "      Generating for cell " + cell);
			}

			// For each variant
			for (AVFileVars.ARMO_spec armorSpec : avr.variantMap.get(skinID).get(cell)) {
			    if (SPGlobal.logging()) {
				SPGlobal.log(header, "        Generating " + (lowestCommMult / armorSpec.spec.Probability_Divider) + " entries for " + armorSpec.armo);
			    }
			    // Generate correct number of entries to get probability
			    for (int i = 0; i < lowestCommMult / armorSpec.spec.Probability_Divider; i++) {
				flstCell.addFormEntry(armorSpec.armo.getForm());
			    }
			}
		    }
		}
	    }
	}
    }

    static void generateSPELvariants(Mod source) {
	for (AVFileVars.AV_Race avr : AVraces.values()) {
	    ScriptRef script = new ScriptRef(raceAttachScript);
	    script.setProperty("AVQuest", AV.quest.getForm());

	    script.setProperty("AltOptions", avr.AltOptions.getForm());
	    ArrayList<FormID> cells = avr.Cells.getFormIDEntries();
	    if (!cells.isEmpty()) {
		script.setProperty("CellIndexing", cells.toArray(new FormID[0]));
	    }
	    script.setProperty("RaceHeightOffset", avr.race.getHeight(Gender.MALE));

	    setStats(script);

	    // Generate the spell
	    SPEL spell = NiftyFunc.genScriptAttachingSpel(script, avr.race.getEDID());
	    avr.race.addSpell(spell.getForm());
	    SPGlobal.getGlobalPatch().addRecord(avr.race);
	}
    }

    static void setStats(ScriptRef script) {
//	ArrayList<Integer> heights = new ArrayList<>();
//	ArrayList<Integer> healths = new ArrayList<>();
//	ArrayList<Integer> magickas = new ArrayList<>();
//	ArrayList<Integer> staminas = new ArrayList<>();
//	ArrayList<Integer> speeds = new ArrayList<>();
//	ArrayList<Integer> prefixKey = new ArrayList<>();
//	ArrayList<String> prefix = new ArrayList<>();
//	ArrayList<Integer> affixKey = new ArrayList<>();
//	ArrayList<String> affix = new ArrayList<>();
//	int index = 0;
//	for (AVFileVars.ARMO_spec variant : avr.getVariants()) {
//	    if (variant.spec.Height.equals(SpecVariant.prototype.Height)) {
//		heights.add(index);
//		heights.add(variant.spec.Height);
//	    }
//	    if (variant.spec.Health != SpecVariant.prototype.Health) {
//		healths.add(index);
//		healths.add(variant.spec.Health);
//	    }
//	    if (variant.spec.Magicka != SpecVariant.prototype.Magicka) {
//		magickas.add(index);
//		magickas.add(variant.spec.Magicka);
//	    }
//	    if (variant.spec.Stamina != SpecVariant.prototype.Stamina) {
//		staminas.add(index);
//		staminas.add(variant.spec.Stamina);
//	    }
//	    if (variant.spec.Speed != SpecVariant.prototype.Speed) {
//		speeds.add(index);
//		speeds.add(variant.spec.Speed);
//	    }
//	    if (!variant.spec.Name_Prefix.equals("")) {
//		prefixKey.add(index);
//		prefix.add(variant.spec.Name_Prefix);
//	    }
//	    if (!variant.spec.Name_Affix.equals("")) {
//		affixKey.add(index);
//		affix.add(variant.spec.Name_Affix);
//	    }
//	    index++;
//	}
//	if (!heights.isEmpty()) {
//	    script.setProperty("HeightVariants", heights.toArray(new Integer[0]));
//	}
//	if (!healths.isEmpty()) {
//	    script.setProperty("HealthVariants", healths.toArray(new Integer[0]));
//	}
//	if (!magickas.isEmpty()) {
//	    script.setProperty("MagickaVariants", magickas.toArray(new Integer[0]));
//	}
//	if (!staminas.isEmpty()) {
//	    script.setProperty("StaminaVariants", staminas.toArray(new Integer[0]));
//	}
//	if (!speeds.isEmpty()) {
//	    script.setProperty("SpeedVariants", speeds.toArray(new Integer[0]));
//	}
//	if (!prefixKey.isEmpty()) {
//	    script.setProperty("PrefixKey", prefixKey.toArray(new Integer[0]));
//	    script.setProperty("Prefix", prefix.toArray(new String[0]));
//	}
//	if (!affixKey.isEmpty()) {
//	    script.setProperty("AffixKey", affixKey.toArray(new Integer[0]));
//	    script.setProperty("Affix", affix.toArray(new String[0]));
//	}
    }

    static void standardizeNPCtag(NPC_ n) {
	float weight = n.getWeight() * 100;
	int tmp = (int) Math.round(weight);
	if (tmp != weight) {
	    n.setWeight(tmp / 100);
	    SPGlobal.getGlobalPatch().addRecord(n);
	}
    }

    void tagNPCs(Mod source) {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Tagging NPCs.txt");
	    SPGlobal.log(header, "====================================================================");
	    SPGlobal.log(header, "Tagging NPCs that have alt skins");
	    SPGlobal.log(header, "====================================================================");
	}
	RACE foxRace = (RACE) SPDatabase.getMajor(new FormID("109C7CSkyrim.esm"), GRUP_TYPE.RACE);
	for (NPC_ n : source.getNPCs()) {
	    FormID skin = getUsedSkin(n);
	    if (skin != null
		    && (!n.isTemplated() || !n.get(NPC_.TemplateFlag.USE_TRAITS)) // Not templated with traits
		    && !skin.isNull() // If has alt skin
		    && AVraces.containsKey(n.getRace())) {  // If we have variants for it
		// If fox race but does not have FOX in the name
		// We skip it as it's most likely a lazy modder
		// using the default race: FoxRace
		standardizeNPCtag(n);
		if (n.getRace().equals(foxRace.getForm())
			&& !n.getEDID().toUpperCase().contains("FOX")
			&& !n.getName().toUpperCase().contains("FOX")) {
		    tagNPC(n, 99);
		}
		ArrayList<FormID> skins = AVraces.get(n.getRace()).skinKey;
		int index = skins.indexOf(skin);
		if (index != -1) {
		    tagNPC(n, index);
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "Tagged " + n + " for skin " + SPDatabase.getMajor(skins.get(index), GRUP_TYPE.ARMO));
		    }
		} else {
		    SPGlobal.log(header, "EXCLUDE BECAUSE NO VARIANTS " + n);
		    tagNPC(n, 99);
		}
		SPGlobal.getGlobalPatch().addRecord(n);
	    }
	}
    }

    static void tagNPC(NPC_ n, float i) {
	n.setHeight(n.getHeight() + i / 10000);
    }

    static FormID getTemplatedSkin(NPC_ n) {
	if (n == null) {
	    return null;
	}
	if (!n.getTemplate().isNull() && n.get(NPC_.TemplateFlag.USE_TRAITS)) {
	    return getTemplatedSkin((NPC_) SPDatabase.getMajor(n.getTemplate(), GRUP_TYPE.NPC_));
	} else {
	    return n.getSkin();
	}
    }

    public VariantProfileNPC find(RACE race, ARMO skin, ARMA piece, String nifPath) {
	for (VariantProfileNPC prof : profiles) {
	    if (prof.is(race, skin, piece, nifPath)) {
		return prof;
	    }
	}
	return null;
    }

    @Override
    public VariantType getType() {
	return VariantType.NPC_;
    }

    @Override
    public String debugName() {
	return "NPC";
    }
}
