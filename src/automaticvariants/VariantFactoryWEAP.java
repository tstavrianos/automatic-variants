/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import automaticvariants.AVFileVars.WEAP_spec;
import automaticvariants.AVNum.AVNumSet;
import automaticvariants.SpecVariantSet.VariantType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lev.LMergeMap;
import skyproc.*;

/**
 *
 * @author Justin Swanson
 */
public class VariantFactoryWEAP extends VariantFactory<VariantProfileWEAP> {

    static LMergeMap<WEAP, WEAP_spec> weapons = new LMergeMap<>(false);
    static Map<WEAP, LVLI> llists = new HashMap<>();

    @Override
    public void locateUnused() {
    }

    @Override
    public void createProfileShells() {
	SPGlobal.log(header, "====================================================================");
	SPGlobal.log(header, "===================      Creating WEAP Profiles     =================");
	SPGlobal.log(header, "====================================================================");
	for (WEAP weapon : AV.getMerger().getWeapons()) {
	    WEAP templateTop = getTemplateTop(weapon);
	    if (templateTop == null) {
		continue;
	    }
	    String nifPath = templateTop.getModelFilename();
	    if (!"".equals(nifPath)) {
		Seed test = new SeedWEAP(nifPath);
		if (test.isValid()) {
		    VariantProfileWEAP profile = find(test);
		    if (profile == null) {
			profile = new VariantProfileWEAP(templateTop);
			profiles.add(profile);
		    }
		    profile.addWeapon(weapon);
		}
	    }
	}
    }

    public WEAP getTemplateTop(WEAP in) {
	int counter = 0;
	while (in != null && !in.getTemplate().isNull()) {
	    // Circular safeguard
	    if (counter++ > 25) {
		return null;
	    }
	    in = (WEAP) SPDatabase.getMajor(in.getTemplate(), GRUP_TYPE.WEAP);
	}
	return in;
    }

    @Override
    public void loadProfileRecords() {
    }

    @Override
    boolean isUnused(FormID id) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void implementOriginalAsVar() {
	for (WEAP weapSrc : weapons.keySet()) {
	    weapons.put(weapSrc, new WEAP_spec(weapSrc));
	}
    }

    @Override
    public void createStructureRecords(Mod source) {
	generateLLists();
	subIn(source);
    }

    public void subIn(Mod source) {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Substitute In.txt");
	    SPGlobal.debugStream = false;
	}

	Map<FormID, MajorRecord> replacements = new HashMap<>(llists.size());
	Map<FormID, MajorRecord> srcReference = new HashMap<>(llists.size());
	for (WEAP weapon : llists.keySet()) {
	    replacements.put(weapon.getForm(), llists.get(weapon));
	    srcReference.put(weapon.getForm(), weapon);
	}

	// Replace in existing LLists
	if (SPGlobal.logging()) {
	    SPGlobal.log(header, "====================================================");
	    SPGlobal.log(header, "==============  Substituting LLists ================");
	    SPGlobal.log(header, "====================================================");
	}
	for (LVLI existingList : source.getLeveledItems()) {
	    Map<FormID, Integer> nums = NiftyFunc.replaceMajors(existingList.getEntryForms(), replacements);
	    for (FormID f : nums.keySet()) {
		Integer num = nums.get(f);
		if (num > 0) {
		    SPGlobal.getGlobalPatch().addRecord(existingList);
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "  Replaced " + srcReference.get(f) + ", " + num + " times in " + existingList);
		    }
		}
	    }
	}

	// Replace in containers
	if (SPGlobal.logging()) {
	    SPGlobal.log(header, "====================================================");
	    SPGlobal.log(header, "============  Substituting Containers ==============");
	    SPGlobal.log(header, "====================================================");
	}
	for (CONT cont : source.getContainers()) {
	    Map<FormID, Integer> nums = NiftyFunc.replaceMajors(cont.getItemForms(), replacements);
	    for (FormID f : nums.keySet()) {
		Integer num = nums.get(f);
		if (num > 0) {
		    SPGlobal.getGlobalPatch().addRecord(cont);
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "  Replaced " + srcReference.get(f) + ", " + num + " times in " + cont);
		    }
		}
	    }
	}

	// Replace in form lists
	if (SPGlobal.logging()) {
	    SPGlobal.log(header, "====================================================");
	    SPGlobal.log(header, "============  Substituting Form Lists ==============");
	    SPGlobal.log(header, "====================================================");
	}
	for (FLST flst : source.getFormLists()) {
	    Map<FormID, Integer> nums = NiftyFunc.replaceMajors(flst.getFormIDEntries(), replacements);
	    for (FormID f : nums.keySet()) {
		Integer num = nums.get(f);
		if (num > 0) {
		    SPGlobal.getGlobalPatch().addRecord(flst);
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "  Replaced " + srcReference.get(f) + ", " + num + " times in " + flst);
		    }
		}
	    }
	}

	// Replace in NPC inventories
	if (SPGlobal.logging()) {
	    SPGlobal.log(header, "====================================================");
	    SPGlobal.log(header, "===============  Substituting NPCs =================");
	    SPGlobal.log(header, "====================================================");
	}
	for (NPC_ npc : source.getNPCs()) {
	    Map<FormID, Integer> nums = NiftyFunc.replaceMajors(npc.getItemForms(), replacements);
	    for (FormID f : nums.keySet()) {
		Integer num = nums.get(f);
		if (num > 0) {
		    SPGlobal.getGlobalPatch().addRecord(npc);
		    if (SPGlobal.logging()) {
			SPGlobal.log(header, "  Replaced " + srcReference.get(f) + ", " + num + " times in " + npc);
		    }
		}
	    }
	}
	SPGlobal.debugStream = true;
    }

    public void generateLLists() {
	if (SPGlobal.logging()) {
	    SPGlobal.newLog(debugFolder() + "Generate LLists.txt");
	}
	for (WEAP weapSrc : weapons.keySet()) {
	    LVLI list = new LVLI(weapSrc.getEDID() + "_llist");
	    if (SPGlobal.logging()) {
		SPGlobal.log(header, "Generating for " + weapSrc);
		SPGlobal.log(header, "  Generating " + list);
	    }
	    llists.put(weapSrc, list);
	    int lcm = calcLCM(weapons.get(weapSrc));
	    for (WEAP_spec weapNew : weapons.get(weapSrc)) {
		if (SPGlobal.logging()) {
		    SPGlobal.log(header, "    Generating " + (lcm / weapNew.spec.Probability_Divider) + " entries for " + weapNew.weap);
		}
		int level = 1;
		AVNum levelSpec = AVNum.factory(weapNew.spec.Spawn_Level);
		if (levelSpec.getClass().equals(AVNumSet.class)) {
		    level = (int) levelSpec.value();
		}
		for (int i = 0; i < lcm / weapNew.spec.Probability_Divider; i++) {
		    list.addEntry(weapNew.weap.getForm(), level, 1);
		}
	    }
	    list.splitEntries();
	}
    }

    @Override
    public VariantType getType() {
	return VariantType.WEAP;
    }

    @Override
    public String debugName() {
	return "Weapon";
    }
}
