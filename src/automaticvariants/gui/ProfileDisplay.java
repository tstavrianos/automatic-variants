/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.VariantProfileNPC;
import java.util.Objects;

/**
 *
 * @author Justin Swanson
 */
public class ProfileDisplay implements Comparable {

    public VariantProfileNPC profile;
    String edid;

    ProfileDisplay(VariantProfileNPC profile) {
	this.profile = profile;
	edid = profile.getRace().getEDID() + " | "
		+ profile.getSkin().getEDID() + " | "
		+ profile.getPiece().getEDID();
    }

    ProfileDisplay(VariantProfileNPC profile, String edid) {
	this.profile = profile;
	this.edid = edid;
    }

    @Override
    public String toString() {
	return edid;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final ProfileDisplay other = (ProfileDisplay) obj;
	if (!Objects.equals(this.edid, other.edid)) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 67 * hash + Objects.hashCode(this.edid);
	return hash;
    }

    @Override
    public int compareTo(Object o) {
	return edid.compareTo(((ProfileDisplay) o).edid);
    }
}
