package automaticvariants;

import automaticvariants.AVSaveFile.Settings;
import automaticvariants.gui.*;
import com.google.gson.Gson;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import lev.Ln;
import lev.debug.LDebug;
import lev.gui.LImagePane;
import lev.gui.LSaveFile;
import skyproc.GLOB.GLOBType;
import skyproc.*;
import skyproc.gui.SPMainMenuConfig;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SUM;
import skyproc.gui.SUMGUI;

/**
 * ToDo: - Make compress work for disabled files
 *
 * @author Leviathan1753
 */
public class AV implements SUM {

    public static String version = "2.0.1 Beta";
    static private String header = "AV";
    /*
     * Exception lists
     */
    static Set<FormID> block = new HashSet<>();
    static Set<String> edidExclude = new HashSet<>();
    static Set<String> modExclude = new HashSet<>();
    /*
     * Script/Property names
     */
    static private Mod merger;
    GLOB texturesOn;
    GLOB statsOn;
    GLOB heightScale;
    GLOB healthScale;
    GLOB magickaScale;
    GLOB staminaScale;
    GLOB speedScale;
    GLOB tieStats;
    GLOB forceRepick;
    GLOB debugOn;
    GLOB debugRegional;
    /*
     * Other
     */
    public static LSaveFile save = new AVSaveFile();
    public static QUST quest;
    public static Gson gson = new Gson();
    static boolean heightOnF = false;
    //GUI
    static public SPMainMenuPanel settingsMenu;
    static public SPMainMenuConfig packageManagerConfig;
    static public PackagesManager packagesManagerPanel;
    static public PackagesOther packagesOtherPanel;
    static public WizSpecVariant wizVarSpecPanel;
    static public WizSpecPackage wizPackageSpecPanel;
    static public PackagesVariantSet wizVarSetSpecPanel;
    static public WizAnother wizAnother;
    static public WizPackages wizPackagesPanel;
    static public WizSet wizSetPanel;
    static public WizSetManual wizSetManualPanel;
    static public WizSetTool wizSetToolPanel;
    static public WizGenTexture wizGenPanel;
    static public WizVariant wizVarPanel;
    static public WizGroup wizGroupPanel;
    static public SettingsOther otherPanel;
    static public StatsPanel heightPanel;
    static public Font AVFont;
    static public Font AVFontSmall;
    static public Color green = new Color(67, 162, 10);
    static public Color darkGreen = new Color(61, 128, 21);
    static public Color orange = new Color(247, 163, 52);
    static public Color blue = new Color(0, 147, 196);
    static public Color yellow = new Color(255, 204, 26);
    static public Color lightGray = new Color(190, 190, 190);
    static public Color darkGray = new Color(110, 110, 110);
    static public boolean gatheringAndExiting = false;

    public static void main(String[] args) {
	ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
	try {
	    save.init();
	    if (handleArgs(arguments)) {
		SPGlobal.closeDebug();
		return;
	    }
	    cleanUp();
	    setSkyProcGlobals();
	    SUMGUI.open(new AV(), args);
	} catch (Exception e) {
	    // If a major error happens, print it everywhere and display a message box.
	    System.err.println(e.toString());
	    SPGlobal.logException(e);
	    JOptionPane.showMessageDialog(null, "There was an exception thrown during program execution: '" + e + "'  Check the debug logs.");
	    SPGlobal.closeDebug();
	}

    }

    static void cleanUp() {
	File delete = new File(SPGlobal.pathToInternalFiles + "Automatic Variants More Memory.bat");
	if (delete.isFile()) {
	    delete.delete();
	}
	delete = new File(SPGlobal.pathToInternalFiles + "AV Debug Mode.bat");
	if (delete.isFile()) {
	    delete.delete();
	}
	delete = new File(SPGlobal.pathToInternalFiles + "Gather Package Files.bat");
	if (delete.isFile()) {
	    delete.delete();
	}

	delete = new File("AV Starter.bat");
	if (delete.isFile()) {
	    delete.delete();
	}

	delete = new File("Files/Last AV Package Listing.txt");
	if (delete.isFile()) {
	    delete.delete();
	}
    }

    public void makeAVQuest() {
	ScriptRef questScript = new ScriptRef("AVQuestScript");
	questScript.setProperty("TexturesOn", texturesOn.getForm());
	questScript.setProperty("StatsOn", statsOn.getForm());
	questScript.setProperty("HeightScale", heightScale.getForm());
	questScript.setProperty("HealthScale", healthScale.getForm());
	questScript.setProperty("MagickaScale", magickaScale.getForm());
	questScript.setProperty("StaminaScale", staminaScale.getForm());
	questScript.setProperty("SpeedScale", speedScale.getForm());
	questScript.setProperty("TieStats", tieStats.getForm());
	questScript.setProperty("ForceRepick", forceRepick.getForm());
	questScript.setProperty("DebugOn", debugOn.getForm());
	questScript.setProperty("DebugRegional", debugRegional.getForm());

	// Log Table
	Float[] logTable = new Float[1000];
	for (int i = 0; i < logTable.length; i++) {
	    logTable[i] = (float) Math.log((i + 1) / 1000.0);
	}
	questScript.setProperty("LogTable", logTable);

	quest = NiftyFunc.makeScriptQuest(questScript);
    }

    public static ScriptRef getQuestScript() {
	return AV.quest.getScriptPackage().getScript("AVQuestScript");
    }

    public void makeGlobals() {
	forceRepick = new GLOB("AVForceRepick", GLOBType.Short);
	forceRepick.setValue((float) AV.save.getInt(Settings.PACKAGES_FORCE_REPICK));
	forceRepick.setConstant(true);

	texturesOn = new GLOB("AVTexturesOn", GLOBType.Short);
	texturesOn.setValue(save.getBool(Settings.PACKAGES_ON));
	texturesOn.setConstant(true);

	statsOn = new GLOB("AVStatsOn", GLOBType.Short);
	statsOn.setValue(save.getBool(Settings.STATS_ON));
	statsOn.setConstant(true);

	double scale = 100.0 // To percent (.01) instead of ints (1)
		* 3.0; // Scaled to 3 standard deviations

	heightScale = new GLOB("AVHeightScale", GLOBType.Float);
	heightScale.setValue((float) (save.getInt(Settings.STATS_HEIGHT_MAX) / scale));
	heightScale.setConstant(true);

	healthScale = new GLOB("AVHealthScale", GLOBType.Float);
	healthScale.setValue((float) (save.getInt(Settings.STATS_HEALTH_MAX) / scale));
	healthScale.setConstant(true);

	magickaScale = new GLOB("AVMagickaScale", GLOBType.Float);
	magickaScale.setValue((float) (save.getInt(Settings.STATS_MAGIC_MAX) / scale));
	magickaScale.setConstant(true);

	staminaScale = new GLOB("AVStaminaScale", GLOBType.Float);
	staminaScale.setValue((float) (save.getInt(Settings.STATS_STAMINA_MAX) / scale));
	staminaScale.setConstant(true);

	speedScale = new GLOB("AVSpeedScale", GLOBType.Float);
	speedScale.setValue((float) (save.getInt(Settings.STATS_SPEED_MAX) / scale));
	speedScale.setConstant(true);

	tieStats = new GLOB("AVTieStats", GLOBType.Short);
	tieStats.setValue(save.getBool(Settings.STATS_TIE));
	tieStats.setConstant(true);

	debugOn = new GLOB("DebugOn", GLOBType.Short);
	debugOn.setValue(save.getBool(Settings.DEBUG_ON));
	debugOn.setConstant(true);

	debugRegional = new GLOB("DebugRegional", GLOBType.Short);
	debugRegional.setValue(save.getBool(Settings.DEBUG_REGIONAL));
	debugRegional.setConstant(true);
    }

    private static void setSkyProcGlobals() {
	/*
	 * Initializing Debug Log and Globals
	 */
	SPGlobal.createGlobalLog();
	SPGlobal.debugModMerge = false;
	SPGlobal.debugBSAimport = false;
	SPGlobal.debugNIFimport = false;
    }

    static void readInExceptions() throws IOException {
	try {
	    BufferedReader in = new BufferedReader(new FileReader("Files/BlockList.txt"));
	    Set target = null;
	    String read;
	    while (in.ready()) {
		read = in.readLine();
		if (read.indexOf("//") != -1) {
		    read = read.substring(0, read.indexOf("//"));
		}
		read = read.trim().toUpperCase();
		if (read.contains("ARMO BLOCKS")) {
		    target = block;
		} else if (read.contains("EDID BLOCKS")) {
		    target = edidExclude;
		} else if (read.contains("MOD BLOCKS")) {
		    target = modExclude;
		} else if (target != null && !read.equals("")) {
		    if (target == block) {
			target.add(new FormID(read));
		    } else {
			target.add(read);
		    }
		}
	    }

	    Set<String> tmp = new HashSet<>(modExclude);
	    for (String s : tmp) {
		if (s.contains(".ESP") || s.contains(".ESM")) {
		    SPGlobal.addModToSkip(new ModListing(s));
		    modExclude.remove(s);
		}
	    }
	    for (String s : modExclude) {
		SPGlobal.addModToSkip(s);
	    }
	} catch (FileNotFoundException ex) {
	    SPGlobal.logError("ReadInExceptions", "Failed to locate 'BlockList.txt'");
	}
    }

    static boolean handleArgs(ArrayList<String> arguments) throws IOException, InterruptedException {
	Ln.toUpper(arguments);
	String gather = "-GATHER";

	if (arguments.contains(gather)) {
	    AVFileVars.gatherFiles();
	    return true;
	}

	return false;
    }

    public static void exitProgram() {
	SPGlobal.log(header, "Exit requested.");
	save.saveToFile();
	LDebug.wrapUpAndExit();
    }

    @Override
    public JFrame openCustomMenu() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean needsPatching() {


	//Need to check if packages have changed.
	ArrayList<File> files = Ln.generateFileList(new File(AVFileVars.AVPackagesDir), false);
	try {
	    ArrayList<String> last = AVFileVars.getAVPackagesListing();
	    if (files.size() != last.size()) {
		if (SPGlobal.logging()) {
		    SPGlobal.logMain(header, "Needs update because number of package files changed.");
		}
		return true;
	    }

	    for (File f : files) {
		String path = f.getPath();
		if (!last.contains(path)) {
		    if (SPGlobal.logging()) {
			SPGlobal.logMain(header, "Needs update because last package list didn't contain: " + path);
		    }
		    return true;
		}
	    }

	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	    return true;
	}
	return false;
    }

    @Override
    public void onExit(boolean patchWasGenerated) throws IOException {
	if (!gatheringAndExiting) {
	    AVFileVars.saveAVPackagesListing();
	    AVFileVars.moveOut();
	}
	if (patchWasGenerated) {
	    AV.save.setInt(Settings.PREV_VERSION, NiftyFunc.versionToNum(AV.version));
	}
    }

    @Override
    public void onStart() throws Exception {
	// Font init
	try {
	    AVFont = Font.createFont(Font.TRUETYPE_FONT, SettingsOther.class.getResource("Sony_Sketch_EF.ttf").openStream());
	    AVFont = AVFont.deriveFont(Font.BOLD, 19);
	} catch (IOException | FontFormatException ex) {
	    SPGlobal.logException(ex);
	    AVFont = new Font("Serif", Font.BOLD, 16);
	}

	// Debug Init
	SPGlobal.newSpecialLog(AVFileVars.AVFileLogs.PackageImport, AVFileVars.debugFolder + "Package Imports.txt");

	// Prep AV
	readInExceptions();
	if (save.getBool(Settings.MOVE_PACKAGE_FILES)) {
	    AVFileVars.gatherFiles();
	} else {
	    AVFileVars.moveOut();
	}
    }

    @Override
    public ArrayList<ModListing> requiredMods() {
	return new ArrayList<>(0);
    }

    @Override
    public String description() {
	return "";
    }

    @Override
    public String getName() {
	return "Automatic Variants";
    }

    @Override
    public GRUP_TYPE[] dangerousRecordReport() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GRUP_TYPE[] importRequests() {
	return new GRUP_TYPE[]{
		    GRUP_TYPE.NPC_, GRUP_TYPE.RACE,
		    GRUP_TYPE.ARMO, GRUP_TYPE.ARMA,
		    GRUP_TYPE.TXST, GRUP_TYPE.LVLN,
		    GRUP_TYPE.WEAP, GRUP_TYPE.LVLI,
		    GRUP_TYPE.CONT, GRUP_TYPE.FLST,
		    GRUP_TYPE.STAT};
    }

    @Override
    public boolean hasStandardMenu() {
	return true;
    }

    @Override
    public SPMainMenuPanel getStandardMenu() {
	settingsMenu = new SPMainMenuPanel(green);
	settingsMenu.addLogo(this.getLogo());
	settingsMenu.setVersion(version, new Point(80, 100));
	settingsMenu.setBackgroundPicture(SettingsOther.class.getResource("AV background.jpg"));
	settingsMenu.setMainFont(AVFont, 25, 40, 27);
	try {
	    LImagePane donate1 = donateButton();
	    donate1.setLocation(120, settingsMenu.getHeight() - 52);
	    settingsMenu.add(donate1);
	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	}
	try {
	    LImagePane donate2 = donateButton();
	    donate2.setLocation(SUMGUI.progress.getWidth() / 2 - donate2.getWidth() / 2, 85);
	    SUMGUI.progress.setSize(SUMGUI.progress.getWidth(), SUMGUI.progress.getHeight() + 30 + donate2.getHeight());
	    SUMGUI.progress.add(donate2);
	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	}


	packagesManagerPanel = new PackagesManager(settingsMenu);
	packagesOtherPanel = new PackagesOther(settingsMenu);
	wizVarSpecPanel = new WizSpecVariant(settingsMenu);
	wizPackageSpecPanel = new WizSpecPackage(settingsMenu);
	wizVarSetSpecPanel = new PackagesVariantSet(settingsMenu);
	wizAnother = new WizAnother(settingsMenu);
	wizPackagesPanel = new WizPackages(settingsMenu);
	wizSetPanel = new WizSet(settingsMenu);
	wizSetToolPanel = new WizSetTool(settingsMenu);
	wizSetManualPanel = new WizSetManual(settingsMenu);
	wizGenPanel = new WizGenTexture(settingsMenu);
	wizGroupPanel = new WizGroup(settingsMenu);
	wizVarPanel = new WizVariant(settingsMenu);
	packageManagerConfig = settingsMenu.addMenu(packagesManagerPanel, false, save, Settings.PACKAGES_ON);

//	heightPanel = new SettingsStatsPanel(settingsMenu);
//	settingsMenu.addMenu(heightPanel, true, save, Settings.STATS_ON);

	otherPanel = new SettingsOther(settingsMenu);
	settingsMenu.addMenu(otherPanel, false, save, Settings.AV_SETTINGS);

	settingsMenu.setWelcomePanel(new WelcomePage(settingsMenu));

	return settingsMenu;
    }

    public LImagePane donateButton() throws IOException {
	final LImagePane donate = new LImagePane(SettingsOther.class.getResource("ConsiderDonatingDark.png"));
	donate.addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
		try {
		    java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=38U9Z82KLA3EU&lc=US&item_name=Automatic%20Variants&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest"));
		} catch (Exception ex) {
		    SPGlobal.logException(ex);
		}
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		try {
		    donate.setImage(SettingsOther.class.getResource("ConsiderDonating.png"));
		} catch (IOException ex) {
		    SPGlobal.logException(ex);
		}
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		try {
		    donate.setImage(SettingsOther.class.getResource("ConsiderDonatingDark.png"));
		} catch (IOException ex) {
		    SPGlobal.logException(ex);
		}
	    }
	});
	return donate;
    }

    @Override
    public boolean hasLogo() {
	return true;
    }

    @Override
    public URL getLogo() {
	return SettingsOther.class.getResource("AutoVarGUITitle.png");
    }

    @Override
    public boolean hasSave() {
	return true;
    }

    @Override
    public LSaveFile getSave() {
	return save;
    }

    @Override
    public void runChangesToPatch() throws Exception {

	if (save.getBool(Settings.MOVE_PACKAGE_FILES)) {
	    AVFileVars.gatherFiles();
	} else {
	    AVFileVars.moveOut();
	}

	makeGlobals();
	makeAVQuest();

	AVFileVars.setUpFileVariants(getMerger());
    }

    static public Mod getMerger() {
	if (merger == null) {
	    merger = new Mod("AVTemporary", false);
	    merger.addAsOverrides(SPGlobal.getDB());
	}
	return merger;
    }

    @Override
    public boolean hasCustomMenu() {
	return false;
    }

    @Override
    public boolean importAtStart() {
	return getSave().getBool(Settings.IMPORT_AT_START);
    }

    @Override
    public String getVersion() {
	return version;
    }

    @Override
    public Mod getExportPatch() {
	Mod patch = new Mod(getListing());
	patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
	patch.setAuthor("Leviathan1753");
	return patch;
    }

    @Override
    public Color getHeaderColor() {
	return green;
    }

    @Override
    public ModListing getListing() {
	return new ModListing(getName(), false);
    }
}
