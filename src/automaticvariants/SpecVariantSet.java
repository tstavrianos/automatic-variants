/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import automaticvariants.gui.ProfileDisplay;
import java.io.File;
import java.util.ArrayList;
import skyproc.*;

/**
 *
 * @author Justin Swanson
 */
public class SpecVariantSet extends SpecFile {

    public VariantType Type = VariantType.NPC_;
    public String[][] Target_FormIDs = new String[0][];

    public SpecVariantSet(File src) {
	super(src);
    }

    public boolean isValid() {
	return true;
    }
    
    public VariantType getType() {
	if (Type == null) {
	    Type = VariantType.NPC_;
	}
	return Type;
    }

    @Override
    ArrayList<String> print() {
	ArrayList<String> out = new ArrayList<>();
	if (isValid()) {
	    out.add("   | Type: " + Type);
	    out.add("   | Target FormIDs: ");
	    for (String[] s : Target_FormIDs) {
		out.add("   |   " + s[0] + " | " + s[1]);
	    }
	} else {
	    out.add(" Error loading spec files.  It's possible AV doesn't have read permissions.");
	}
	return out;
    }

    public void loadSkins(ArrayList<ProfileDisplay> in) {
	Target_FormIDs = new String[in.size()][6];
	for (int i = 0; i < in.size(); i++) {
	    VariantProfileNPC profile = in.get(i).profile;
	    RACE race = profile.getRace();
	    ARMO skin = profile.getSkin();
	    ARMA piece = profile.getPiece();
	    Target_FormIDs[i][0] = race.getFormStr().substring(0, 6);
	    Target_FormIDs[i][1] = race.getFormStr().substring(6);
	    Target_FormIDs[i][2] = skin.getFormStr().substring(0, 6);
	    Target_FormIDs[i][3] = skin.getFormStr().substring(6);
	    Target_FormIDs[i][4] = piece.getFormStr().substring(0, 6);
	    Target_FormIDs[i][5] = piece.getFormStr().substring(6);
	}
    }

    @Override
    void printToLog(String set) {
	SPGlobal.log(set, VariantSet.depth + "   --- Set Specifications loaded: --");
	for (String s : print()) {
	    SPGlobal.log(set, VariantSet.depth + s);
	}
	SPGlobal.log(set, VariantSet.depth + "   -------------------------------------");
    }

    @Override
    public String printHelpInfo() {
	String content = "Seeds:";
	for (String[] formID : Target_FormIDs) {
	    content += "\n    ";
	    content += printFormID(formID, GRUP_TYPE.NPC_, GRUP_TYPE.RACE, GRUP_TYPE.ARMA, GRUP_TYPE.ARMO);
	}
	return content;
    }

    public enum VariantType {

	NPC_,
	WEAP;
    }
}
