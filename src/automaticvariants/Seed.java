/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.util.ArrayList;
import skyproc.FormID;

/**
 *
 * @author Justin Swanson
 */
public abstract class Seed {
    public abstract boolean load(ArrayList<FormID> ids);
    public abstract boolean isValid();
    public abstract void print();
    public abstract String getSeedHashCode();
}
