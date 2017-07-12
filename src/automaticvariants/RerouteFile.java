/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.*;
import lev.LMergeMap;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class RerouteFile extends PackageNode {

    File routeFile;
    static LMergeMap<File, RerouteFile> reroutes = new LMergeMap<>(false, false);

    public RerouteFile(File src) throws FileNotFoundException, IOException {
	super(src, Type.REROUTE);
	BufferedReader in = new BufferedReader(new FileReader(src));
	File to = new File(in.readLine());
	in.close();
	routeFile = src;
	this.src = to;
	reroutes.put(this.src, this);
	if (AVFileVars.isDDS(this.src)) {
	    type = PackageNode.Type.TEXTURE;
	}
    }

    @Override
    public boolean isReroute() {
	return true;
    }

    @Override
    public long fileSize() {
	return routeFile.length();
    }

    @Override
    public String toString() {
	return routeFile.getName().replace(".reroute", "") + " (R)";
    }

    @Override
    public boolean isDisabled() {
	return isDisabled(routeFile);
    }

    @Override
    public void enable(boolean enable) {
	enable(enable, routeFile);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final RerouteFile other = (RerouteFile) obj;
	if (this.routeFile != other.routeFile && (this.routeFile == null || !this.routeFile.getName().equalsIgnoreCase(other.routeFile.getName()))) {
	    return false;
	}
	return true;
    }

    public static File createRerouteFile(File from, File to) throws IOException {
	File reroute = new File(from.getPath() + ".reroute");
	if (!from.delete()) {
	    SPGlobal.logError("Create Reroute", "Could not delete routed file " + from);
	}
	if (reroute.isFile()) {
	    reroute.delete();
	}
	BufferedWriter out = new BufferedWriter(new FileWriter(reroute));
	out.write(to.getPath());
	out.close();
	return reroute;
    }

    void writeRouteTo(File from, File to) throws IOException {
	BufferedWriter out = new BufferedWriter(new FileWriter(from));
	out.write(to.getPath());
	out.close();
    }

    public void changeRouteTo(File f) throws IOException {
	writeRouteTo(routeFile, f);
    }
}
