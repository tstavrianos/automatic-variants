/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import skyproc.SPGlobal;

/**
 *
 * @Author Justin Swanson
 */
public class Variant extends PackageNode implements Serializable {

    String name = "";
    ArrayList<String> textureNames;
    public SpecVariant spec;
    static String depth = "* +   # ";

    public Variant(File variantDir) {
	super(variantDir, Type.VAR);
	this.name = variantDir.getName();
	spec = new SpecVariant(variantDir);
    }

    Variant() {
	super(null, Type.VAR);
	spec = new SpecVariant();
    }

    Variant(Variant rhs) {
	this();
	name = rhs.name;
	for (PackageNode p : rhs.getAll(Type.TEXTURE)) {
	    add(new PackageNode(p.src, Type.TEXTURE));
	}
	for (PackageNode p : rhs.getAll(Type.MESH)) {
	    add(new PackageNode(p.src, Type.MESH));
	}
	parent = rhs.parent;
    }

    public void load() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "  Adding Variant: " + src);
	}
	for (File f : src.listFiles()) {
	    if (AVFileVars.isDDS(f)) {
		if (SPGlobal.logging()) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "    Added texture: " + f);
		}
		PackageNode c = new PackageNode(f, Type.TEXTURE);
		add(c);
	    } else if (AVFileVars.isNIF(f)) {
		if (SPGlobal.logging()) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "    Added nif: " + f);
		}
		PackageNode c = new PackageNode(f, Type.MESH);
		add(c);
	    } else if (AVFileVars.isSpec(f)) {
		try {
		    spec = AV.gson.fromJson(new FileReader(f), SpecVariant.class);
		    if (spec != null) {
			spec.src = f;
			if (SPGlobal.logging()) {
			    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName());
			}
		    }
		} catch (com.google.gson.JsonSyntaxException ex) {
		    SPGlobal.logException(ex);
		    JOptionPane.showMessageDialog(null, "Variant " + f.getPath() + " had a bad specifications file.  Skipped.");
		}
	    } else if (AVFileVars.isReroute(f)) {
		RerouteFile c = new RerouteFile(f);
		if (SPGlobal.logging()) {
		    SPGlobal.logSpecial(AVFileVars.AVFileLogs.PackageImport, src.getName(), depth + "    Added ROUTED file: " + c.routeFile);
		}
		add(c);
	    }
	}
    }

    public void mergeInGlobals(ArrayList<PackageNode> globalFiles) {
	ArrayList<PackageNode> texs = getAll(Type.TEXTURE);
	for (PackageNode global : globalFiles) {
	    if (global.type == Type.GENTEXTURE) {
		boolean exists = false;
		for (PackageNode tex : texs) {
		    if (global.src.getName().equalsIgnoreCase(tex.src.getName())) {
			exists = true;
			break;
		    }
		}
		if (!exists) {
		    add(new PackageNode(global.src, Type.TEXTURE));
		}
	    }
	}

	ArrayList<PackageNode> mesh = getAll(Type.MESH);
	if (mesh.isEmpty()) {
	    for (PackageNode global : globalFiles) {
		if (global.type == Type.GENMESH) {
		    add(new PackageNode(global.src, Type.MESH));
		    break;
		}
	    }
	}
    }

    public Variant merge(Variant rhs) {
	Variant out = new Variant(rhs);
	out.name = name + "_" + rhs.src.getName();
	for (PackageNode p : getAll(Type.TEXTURE)) {
	    out.add(new PackageNode(p.src, Type.TEXTURE));
	}
	for (PackageNode p : getAll(Type.MESH)) {
	    out.add(new PackageNode(p.src, Type.MESH));
	}
	spec.Probability_Divider *= rhs.spec.Probability_Divider;
	return out;
    }

    public VariantGroup getGroup() {
	return (VariantGroup) getParent();
    }

    public ArrayList<File> getTextureFiles() {
	ArrayList<File> out = new ArrayList<>();
	for (PackageNode p : getAll(Type.TEXTURE)) {
	    out.add(p.src);
	}
	return out;
    }

    public void absorbGlobalMesh(VariantGlobalMesh globalMesh) {
	name += "_" + globalMesh.src.getName();
	removeAll(Type.MESH);
	PackageNode mesh = globalMesh.getAll(Type.MESH).get(0);
	add(new PackageNode(mesh));
	spec = spec.merge(globalMesh.spec);
    }

    public ArrayList<String> getTextureNames() {
	if (textureNames == null) {
	    ArrayList<File> files = getTextureFiles();
	    textureNames = new ArrayList<>();
	    for (int i = 0; i < files.size(); i++) {
		textureNames.add(files.get(i).getName().toUpperCase());
	    }
	}
	return textureNames;
    }

    @Override
    public String printSpec() {
	if (spec != null) {
	    String out = spec.printHelpInfo();
	    if (!out.equals("")) {
		out += "\n";
	    }
	    return out;
	} else {
	    return "BAD SPEC FILE";
	}
    }

    @Override
    public String printName(String spacer) {
	PackageNode p = (PackageNode) this.getParent();
	return p.printName(spacer) + spacer + name;
    }

    public AVPackage getPackage() {
	return (AVPackage) getParent().getParent().getParent();
    }

    public boolean isTemplated() {
	return spec.Template_Form.length() > 6;
    }

    @Override
    public int hashCode() {
	return printName("").hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final Variant other = (Variant) obj;
	return printName("").equals(other.printName(""));
    }
}
