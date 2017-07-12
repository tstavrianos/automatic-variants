/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.util.ArrayList;
import java.util.Objects;
import skyproc.*;

/**
 *
 * @author Justin Swanson
 */
public class SeedWEAP extends Seed {

    private String nifPath = "";
    
    public SeedWEAP() {
	
    }
    
    public SeedWEAP(String nifPath) {
	setNifPath(nifPath);
    }
    
    @Override
    public boolean load(ArrayList<FormID> ids) {
	if (!ids.isEmpty()) {
	    WEAP origWeap = (WEAP) SPDatabase.getMajor(ids.get(0), GRUP_TYPE.WEAP);
	    setNifPath(origWeap.getModelFilename());
	    return isValid();
	}
	return false;
    }

    @Override
    public boolean isValid() {
	return!"".equals(nifPath);
    }

    @Override
    public void print() {
	SPGlobal.log("SeedProfile", "|        NIF: " + nifPath);
	SPGlobal.log("SeedProfile", " \\====================================");
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final SeedWEAP other = (SeedWEAP) obj;
	if (!this.nifPath.equalsIgnoreCase(other.nifPath)) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 83 * hash + Objects.hashCode(this.nifPath);
	return hash;
    }
    
    public String getNifPath() {
	return nifPath;
    }
    
    public final void setNifPath(String s) {
	if (s.toUpperCase().indexOf("MESHES\\") != 0) {
	    s = "MESHES\\" + s;
	}
	nifPath = s;
    }
    
    @Override
    public String getSeedHashCode() {
	int hash = hashCode();
	if (hash >= 0) {
	    return Integer.toString(hash);
	} else {
	    return "n" + Integer.toString(-hash);
	}
    }
}
