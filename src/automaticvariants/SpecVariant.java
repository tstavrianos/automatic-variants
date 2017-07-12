/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import skyproc.FormID;
import skyproc.GRUP_TYPE;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class SpecVariant extends SpecFile {

    public int Probability_Divider = 1;
    public String Author = "";
    public String[][] Region_Include = new String[0][];
    public boolean Exclusive_Region = false;
    public String Name_Affix = "";
    public String Name_Prefix = "";
    public String Name_Set = "";

    // NPC
    public String Health = "%100";
    public String Height = "%100";
    public String Magicka = "%100";
    public String Stamina = "%100";
    public String Speed = "%100";

    // Items
    public String Gold_Value = "%100";
    public String Enchantment = "%100";
    public String Enchantment_Form = "";
    public String Weight = "%100";
    public String Reach = "%100";
    public String Damage = "%100";
    public String Crit = "%100";
    public String Crit_Damage = "%100";
    public String Stagger = "%100";
    public String Range_Min = "%100";
    public String Range_Max = "%100";
    public String Num_Proj = "%100";
    public String Armor = "%100";
    public String Spawn_Level = "%100";
    public String FirstPersonModelName = "";
    public String Template_Form = "";

    public static SpecVariant prototype = new SpecVariant();

    SpecVariant() {
	super();
    }

    public SpecVariant(File src) {
	super(src);
    }

    public SpecVariant merge(SpecVariant rhs) {
	SpecVariant out = new SpecVariant();
	out.Probability_Divider = Probability_Divider * rhs.Probability_Divider;

	ArrayList<String[]> regionInclude = new ArrayList<>(Arrays.asList(Region_Include));
	regionInclude.addAll(Arrays.asList(rhs.Region_Include));
	out.Region_Include = regionInclude.toArray(new String[0][]);

	out.Exclusive_Region = this.Exclusive_Region || rhs.Exclusive_Region;

	out.Name_Set = "".equals(this.Name_Set) ? rhs.Name_Set : this.Name_Set;
	out.Name_Affix = "".equals(this.Name_Affix) ? rhs.Name_Affix : this.Name_Affix;
	out.Name_Prefix = "".equals(this.Name_Prefix) ? rhs.Name_Prefix : this.Name_Prefix;

	//NPC
	out.Health = AVNum.merge(out.Health, rhs.Health);
	out.Height = AVNum.merge(out.Height, rhs.Height);
	out.Magicka = AVNum.merge(out.Magicka, rhs.Magicka);
	out.Stamina = AVNum.merge(out.Stamina, rhs.Stamina);
	out.Speed = AVNum.merge(out.Speed, rhs.Speed);

	//Items
	out.Gold_Value = AVNum.merge(this.Gold_Value, rhs.Gold_Value);
	out.Enchantment = AVNum.merge(this.Enchantment, rhs.Enchantment);
	if (this.Enchantment_Form.length() > 6) {
	    out.Enchantment_Form = this.Enchantment_Form;
	} else {
	    out.Enchantment_Form = rhs.Enchantment_Form;
	}
	out.Weight = AVNum.merge(this.Weight, rhs.Weight);
	out.Reach = AVNum.merge(this.Gold_Value, rhs.Reach);
	out.Damage = AVNum.merge(this.Damage, rhs.Damage);
	out.Crit = AVNum.merge(this.Crit, rhs.Crit);
	out.Crit_Damage = AVNum.merge(this.Crit_Damage, rhs.Crit_Damage);
	out.Stagger = AVNum.merge(this.Stagger, rhs.Stagger);
	out.Range_Min = AVNum.merge(this.Range_Min, rhs.Range_Min);
	out.Range_Max = AVNum.merge(this.Range_Max, rhs.Range_Max);
	out.Num_Proj = AVNum.merge(this.Num_Proj, rhs.Num_Proj);
	out.Armor = AVNum.merge(this.Armor, rhs.Armor);
	out.Spawn_Level = AVNum.merge(this.Spawn_Level, rhs.Spawn_Level);
	if (this.Template_Form.length() > 6) {
	    out.Template_Form = this.Template_Form;
	} else {
	    out.Template_Form = rhs.Template_Form;
	}

	return out;
    }

    @Override
    ArrayList<String> print() {
	ArrayList<String> out = new ArrayList<String>();
	if (Author != null && !Author.equals("")) {
	    out.add("Author: " + Author);
	}
	if (Probability_Divider != 1) {
	    out.add("Probability Div: 1/" + Probability_Divider);
	}
	if (Region_Include != null && Region_Include.length > 0) {
	    out.add("Region Include: ");
	    for (String[] s : Region_Include) {
		String tmp = "";
		for (String part : s) {
		    tmp += part + " ";
		}
		out.add("   " + tmp);
	    }
	    out.add("Exclusive Region: " + Exclusive_Region);
	}
	if (Name_Prefix != null && !Name_Prefix.equals("")) {
	    out.add("Name Prefix: " + Name_Prefix);
	}
	if (Name_Affix != null && !Name_Affix.equals("")) {
	    out.add("Name Affix: " + Name_Affix);
	}
	return out;
    }

    @Override
    void printToLog(String header) {
	SPGlobal.log(header, Variant.depth + "    --- Variant Specifications loaded: --");
	for (String s : print()) {
	    SPGlobal.log(header, Variant.depth + "    |   " + s);
	}
	SPGlobal.log(header, Variant.depth + "    -------------------------------------");
    }

    @Override
    public String printHelpInfo() {
	String out = "";
	if (!Name_Affix.equals("") || !Name_Prefix.equals("")) {
	    out += "Spawning Name: " + Name_Prefix + " [NAME] " + Name_Affix + "\n";
	}
	if (!Author.equals("")) {
	    out += "Author: " + Author + "\n";
	}
	if (Probability_Divider != 1) {
	    out += "Relative Probability: 1/" + Probability_Divider + "\n";
	}
	if (Probability_Divider != 1) {
	    out += "Template: " + Template_Form + "\n";
	}
	if (Region_Include.length > 0) {
	    out += "Regions To Spawn In:";
	    for (String[] formID : Region_Include) {
		out += "\n    " + printFormID(formID, GRUP_TYPE.ALCH);
	    }
	    out += "\n";
	}
	return out;
    }

    public Set<FormID> getRegions() {
	Set<FormID> out = new HashSet<>();
	for (String[] formID : Region_Include) {
	    FormID id = FormID.parseString(formID);
	    if (!id.isNull()) {
		out.add(id);
	    }
	}
	return out;
    }
}