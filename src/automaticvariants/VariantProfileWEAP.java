/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import automaticvariants.AVFileVars.WEAP_spec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import skyproc.*;

/**
 *
 * @author Justin Swanson
 */
public class VariantProfileWEAP extends VariantProfile<WEAP> {

    SeedWEAP seed = new SeedWEAP();
    private ArrayList<WEAP> matchedWeapons = new ArrayList<>();
    HashMap<Variant, WEAP> templateMap = new HashMap<>();

    public VariantProfileWEAP(WEAP in) {
	seed.setNifPath(in.getModelFilename());
    }

    public void addWeapon(WEAP in) {
	matchedWeapons.add(in);
    }

    @Override
    public void print() {
	super.print();
	SPGlobal.log(toString(), "    Matched weapons: ");
	for (WEAP weapon : matchedWeapons) {
	    SPGlobal.log(toString(), "      " + weapon.toString());
	}
	SPGlobal.log(toString(), "");
	SPGlobal.log(toString(), "");
    }

    @Override
    public void printShort() {
	SPGlobal.log(toString(), "|   NIF: " + getNifPath());
    }

    public boolean is(String nifPath) {
	return seed.getNifPath().equalsIgnoreCase(nifPath);
    }

    @Override
    public String getNifPath() {
	return seed.getNifPath();
    }

    public void setNifPath(String in) {
    }

    @Override
    public Seed getSeed() {
	return seed;
    }

    @Override
    public void generateRecord(Variant var) {
	String varEDID = NiftyFunc.EDIDtrimmer(generateEDID(var));
	for (WEAP weapon : matchedWeapons) {
	    if (SPGlobal.logging()) {
		SPGlobal.log(Integer.toString(ID), " *****************> Generating for weapon " + weapon);
	    }
	    WEAP dup = (WEAP) generateFor(weapon, varEDID);
	    WEAP template = getTemplateCopy(var, varEDID);
	    if (template != null) {
		SPGlobal.log(toString(), " * Templating to " + template);
		dup.setName(subInNewName(template, weapon));
		dup.setValue(subInNewValue(template, weapon));
		dup.setTemplate(template.getForm());
	    } else {
		SPGlobal.log(toString(), " * No template.  Generating records: ");
		weapon = generateSupportRecords(weapon, varEDID, var);
	    }
	    if (weapon != null) {
		VariantFactoryWEAP.weapons.put(weapon, new WEAP_spec(dup, var.spec));
	    }
	    if (SPGlobal.logging()) {
		SPGlobal.log(Integer.toString(ID), " *****************>");
	    }
	}
    }

    String subInNewName(WEAP template, WEAP orig) {
	if (!orig.getTemplate().equals(FormID.NULL)) {
	    WEAP templateTop = orig.getTemplateTop();
	    if (templateTop != null) {
		String origName = orig.getName();
		String topName = templateTop.getName();
		String ret = origName.replaceAll(topName, template.getName());
		return ret;
	    }
	}
	return template.getName();
    }

    int subInNewValue(WEAP template, WEAP orig) {
	double scale = 1.0;
	if (!orig.getTemplate().equals(FormID.NULL)) {
	    WEAP templateTop = orig.getTemplateTop();
	    if (templateTop != null) {
		int origVal = orig.getValue();
		int topVal = templateTop.getValue();
		scale = ((double) origVal) / topVal;
	    }
	}
	return (int) Math.round(template.getValue() * scale);
    }

    WEAP getTemplateCopy(Variant var, String varEDID) {
	if (var.isTemplated()) {
	    WEAP dup = templateMap.get(var);
	    if (dup != null) {
		SPGlobal.log(toString(), " * Variant is templated and exists: " + dup);
		return dup;
	    } else {
		// If not stored, create and store it
		WEAP template = getTemplateRecord(var);
		if (template != null) {
		    SPGlobal.log(toString(), " * Variant is templated to " + template + " GENERATING RECORDS:");
		    dup = (WEAP) generateFor(template, varEDID);
		    dup = generateSupportRecords(dup, varEDID, var);
		    templateMap.put(var, dup);
		    return dup;
		}
	    }
	}
	return null;
    }

    WEAP generateSupportRecords(WEAP weapon, String varEDID, Variant var) {

	setStats(weapon, var);

	// Set Third Person
	String nifPath = getNifPath(var, false);
	weapon.getModelData().setFileName(getCleanNifPath(nifPath));
	//Generate and set alt textures
	Map<String, TXST> txsts = generateTXSTs(var, nifPath);
	if (!txsts.isEmpty()) {
	    loadAltTextures(weapon.getModelData().getAltTextures(), txsts, nifPath);
	} else if (!var.isTemplated()) {
	    SPGlobal.logError(toString(), " * Skipped because no TXSTs were generated and it was not templated: " + var.printName("_") + " for " + weapon);
	    return null;
	}

	// Set First Person
	STAT stat = new STAT(varEDID + weapon.getEDID() + "_stat");
	String firstPersonNifPath = getNifPath(var, true);
	stat.getModelData().setFileName(getCleanNifPath(nifPath));
	if (!firstPersonNifPath.equals(nifPath)) {
	    txsts = generateTXSTs(var, firstPersonNifPath);
	    if (!txsts.isEmpty()) {
		loadAltTextures(stat.getModelData().getAltTextures(), txsts, firstPersonNifPath);
	    } else if (!var.isTemplated()) {
		SPGlobal.logError(toString(), " * Skipped because no TXSTs were generated and it was not templated." + var.printName("_") + " for " + weapon);
		return null;
	    }
	}
	stat = (STAT) NiftyFunc.mergeDuplicate(stat);
	weapon.setFirstPersonModel(stat.getForm());
	return weapon;
    }

    void setStats(WEAP weap, Variant var) {
	if (!"".equals(var.spec.Name_Set)) {
	    weap.setName(var.spec.Name_Set);
	}
	if (!"".equals(var.spec.Name_Prefix)) {
	    weap.setName(var.spec.Name_Prefix + " " + weap.getName());
	}
	if (!"".equals(var.spec.Name_Affix)) {
	    weap.setName(weap.getName() + " " + var.spec.Name_Affix);
	}
	AVNum speed = AVNum.factory(var.spec.Speed);
	if (speed.modified()) {
	    weap.setSpeed(speed.value(weap.getSpeed()));
	}
	AVNum gold = AVNum.factory(var.spec.Gold_Value);
	if (gold.modified()) {
	    weap.setValue((int) gold.value(weap.getValue()));
	}
	AVNum enchantment = AVNum.factory(var.spec.Enchantment);
	if (enchantment.modified()) {
	    weap.setEnchantmentCharge((int) enchantment.value(weap.getEnchantmentCharge()));
	}
	if (var.spec.Enchantment_Form.length() >= 6) {
	    try {
		FormID enchantmentID = new FormID(var.spec.Enchantment_Form);
		weap.setEnchantment(enchantmentID);
	    } catch (Exception e) {
		SPGlobal.logException(e);
	    }
	}
	AVNum weight = AVNum.factory(var.spec.Weight);
	if (weight.modified()) {
	    weap.setWeight(weight.value(weap.getWeight()));
	}
	AVNum reach = AVNum.factory(var.spec.Reach);
	if (reach.modified()) {
	    weap.setReach(reach.value(weap.getReach()));
	}
	AVNum damage = AVNum.factory(var.spec.Damage);
	if (damage.modified()) {
	    weap.setDamage((int) damage.value(weap.getDamage()));
	}
	AVNum crit = AVNum.factory(var.spec.Crit);
	if (crit.modified()) {
	    weap.setCritMult(crit.value(weap.getCritMult()));
	}
	AVNum critDamage = AVNum.factory(var.spec.Crit_Damage);
	if (critDamage.modified()) {
	    weap.setCritDamage((int) critDamage.value(weap.getCritDamage()));
	}
	AVNum stagger = AVNum.factory(var.spec.Stagger);
	if (stagger.modified()) {
	    weap.setStagger(stagger.value(weap.getStagger()));
	}
	AVNum rangeMin = AVNum.factory(var.spec.Range_Min);
	if (rangeMin.modified()) {
	    weap.setMinRange(rangeMin.value(weap.getMinRange()));
	}
	AVNum rangeMax = AVNum.factory(var.spec.Range_Max);
	if (rangeMax.modified()) {
	    weap.setMaxRange(rangeMax.value(weap.getMaxRange()));
	}
	AVNum numProj = AVNum.factory(var.spec.Num_Proj);
	if (numProj.modified()) {
	    weap.setNumProjectiles((int) numProj.value(weap.getNumProjectiles()));
	}
    }

    @Override
    public String getNif(FormID id, boolean firstPerson) {
	WEAP template = (WEAP) SPDatabase.getMajor(id, GRUP_TYPE.WEAP);
	if (template == null) {
	    AVFileVars.importTemplateMod(id.getMaster());
	    template = (WEAP) SPDatabase.getMajor(id, GRUP_TYPE.WEAP);
	}
	if (template != null) {
	    if (firstPerson) {
		STAT stat = (STAT) SPDatabase.getMajor(template.getFirstPersonModel(), GRUP_TYPE.STAT);
		if (stat != null) {
		    return "MESHES\\" + stat.getModelData().getFileName();
		}
	    } else {
		return "MESHES\\" + template.getModelData().getFileName();
	    }
	}
	return "";
    }
}
