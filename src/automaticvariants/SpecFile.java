/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.*;
import java.util.ArrayList;
import lev.Ln;
import skyproc.FormID;
import skyproc.GRUP_TYPE;
import skyproc.MajorRecord;
import skyproc.SPDatabase;

/**
 *
 * @author Justin Swanson
 */
public abstract class SpecFile implements Serializable {

    File src;

    SpecFile() {
    }

    SpecFile(File folderDir) {
	this.src = new File(folderDir.getPath() + "\\" + "Specifications.json");
    }

    abstract ArrayList<String> print();

    abstract void printToLog(String header);

    abstract public String printHelpInfo();

    public void export() throws IOException {
	if (src.isFile()) {
	    src.delete();
	}

	BufferedWriter out = new BufferedWriter(new FileWriter(src));
	out.write(Ln.toJsonPretty(AV.gson.toJsonTree(this), "src"));
	out.close();
    }

    public String printFormID(String[] formID, GRUP_TYPE ... type) {
	FormID id = new FormID(formID[0], formID[1]);
	MajorRecord r = SPDatabase.getMajor(id, type);
	String content = "";
	if (r != null) {
	    content += r.getEDID() + "  |  ";
	}
	content += id.getFormStr();
	return content;
    }
}
