/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.File;
import java.util.ArrayList;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class SpecPackage extends SpecFile {

    public String Packager = "";
    public ArrayList<String> OriginalAuthors = new ArrayList<>();
    String version = "1.0";
    public String Release_Date = "";
    public String Last_Updated = "";
    public String comments = "";

    public SpecPackage(File f) {
	super(f);
    }

    @Override
    void printToLog(String header) {
	SPGlobal.log(header, AVPackage.depth + "    --- Package Specifications loaded: --");
	for (String s : print()) {
	    SPGlobal.log(header, AVPackage.depth + "    |   " + s);
	}
	SPGlobal.log(header, AVPackage.depth + "    -------------------------------------");
    }

    @Override
    public String printHelpInfo() {
	String out = "";
	for (String s : print()) {
	    out += s + "\n";
	}
	return out;
    }

    @Override
    ArrayList<String> print() {
	ArrayList<String> out = new ArrayList<>();
	out.add("Original Authors:");
	for (String s : OriginalAuthors) {
	    out.add("   > " + s);
	}
	out.add("\nPackager: " + Packager);
	if (Release_Date != null) {
	    out.add("Release Date: " + Release_Date);
	}
	if (Last_Updated != null) {
	    out.add("Last Updated: " + Last_Updated);
	}
	if (comments != null) {
	    out.add("Comments: " + comments);
	}
	return out;
    }
}
