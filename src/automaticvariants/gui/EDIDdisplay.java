/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import skyproc.MajorRecord;

/**
 *
 * @author Justin Swanson
 */
public class EDIDdisplay<T extends MajorRecord> implements Comparable {

    T m;

    EDIDdisplay (T in) {
	m = in;
    }

    @Override
    public String toString() {
	return m.getEDID();
    }

    @Override
    public int compareTo(Object o) {
	return m.getEDID().compareTo(((EDIDdisplay) o).toString());
    }

}
