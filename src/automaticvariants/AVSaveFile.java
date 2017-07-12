/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.util.ArrayList;
import lev.Ln;
import skyproc.SkyProcSave;

/**
 *
 * @author Justin Swanson
 */
public class AVSaveFile extends SkyProcSave {

    @Override
    protected void initSettings() {
	Add(AVSaveFile.Settings.PACKAGES_ON,		true,	    true);
	Add(AVSaveFile.Settings.PACKAGES_ORIG_AS_VAR,	true,	    true);
	Add(AVSaveFile.Settings.PACKAGES_ALLOW_EXCLUSIVE_REGION,2,	    true);
	Add(AVSaveFile.Settings.IMPORT_AT_START,		false,	    false);
	Add(AVSaveFile.Settings.STATS_ON,			false,	    true);
	Add(AVSaveFile.Settings.STATS_HEIGHT_MAX,		15,	    true);
	Add(AVSaveFile.Settings.STATS_HEALTH_MAX,		25,	    true);
	Add(AVSaveFile.Settings.STATS_MAGIC_MAX,		25,	    true);
	Add(AVSaveFile.Settings.STATS_STAMINA_MAX,		25,	    true);
	Add(AVSaveFile.Settings.STATS_SPEED_MAX,		10,	    true);
	Add(AVSaveFile.Settings.STATS_TIE,			true,	    true);
	Add(AVSaveFile.Settings.SPEC_VAR_PROB,		15,	    false);
	Add(AVSaveFile.Settings.PACKAGES_FORCE_REPICK,	1,	    true);
	Add(AVSaveFile.Settings.PREV_VERSION,		0,	    false);
	Add(AVSaveFile.Settings.DISABLED_PACKAGES,		new ArrayList<String>(), false);
	Add(AVSaveFile.Settings.PACKAGE_LISTING,		new ArrayList<String>(), false);
	Add(AVSaveFile.Settings.DEBUG_ON,			false,	    true);
	Add(AVSaveFile.Settings.DEBUG_REGIONAL,		false,	    true);
	Add(AVSaveFile.Settings.MOVE_PACKAGE_FILES,		true,	    true);
	Add(AVSaveFile.Settings.LARGE_MULTIPLY_WARNING,		new ArrayList<String>(),	    false);
    }

    @Override
    protected void initHelp() {
	helpInfo.put(AVSaveFile.Settings.PACKAGES_ON, "This feature will create and reorganize records to make actors"
		+ " with different textures spawn."
		+ "\n\nThe variants are created from 3rd party AV Packages that you download and install."
		+ "\n\nYou can right-click components for more options such as editing "
		+ "specifications and compressing packages."
		+ "\n\nIf you disable a package that you have been playing with, "
		+ "make sure to reset the cells of your savegame.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_DISABLE, "This will disable any selected items and their children.\n\n"
		+ "Disabled items will not be integrated into the AV patch.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_ENABLE, "This will enable any selected items and their children.\n\n"
		+ "Enabled items will be integrated into the AV patch.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_COMPRESS, "This will run AV's compression algorithm over the package,"
		+ " making the package as small as possible.  It will move common files to Variant Set positions, as"
		+ " well as replacing any duplicate files with reroute 'shortcuts' so that there is only one of each file.\n\n"
		+ "NOTES: It is recommended that you make a backup in case something goes wrong, as this function will be moving and"
		+ " deleting things.\n\n"
		+ "This function will enable the selection before compressing.\n\n"
		+ "This may take a while to process.  A popup will appear showing the results when it is complete.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_ORIG_AS_VAR, "This will add the non-AV actor setup as a variant, and add it to the list of options. \n\n"
		+ "If turned off, then only variants explicitly part of an AV Package will spawn.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_ALLOW_EXCLUSIVE_REGION, "AV supports a feature where AV Packages can specify regions that they want their variants to spawn.  In addition, they can claim those regions as exclusive so only their variants spawn there.  You can block some of these features with the following options:\n\n"
		+ "Block Regions:  This will force all variants spawn anywhere, no matter what.\n\n"
		+ "Allow Regions:  This will allow variants to be able to spawn only in some areas if they want to.\n\n"
		+ "Allow Exclusive Regions:  In addition to allowing regions, this will also allow variants to claim their "
		+ "zones as exclusive, blocking other variants from spawning there unless they have that zone marked too.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_FORCE_REPICK, "This is a setting that applies once the next time you start your game. "
		+ "It will force every NPC in your game to choose a new variant.  Turning this on will only make them do it once; It will not make NPCs repeatedly switch.\n\n"
		+ "This can be very useful if you've added variants and want to make existing NPCs choose between them.\n\nThis can also be used as a resetting "
		+ "feature to force an NPC to repick if they have bugged out.");

	helpInfo.put(AVSaveFile.Settings.PACKAGES_GATHER, "This is a utility function that should only be used if you want to modify AV Package contents "
		+ "manually.\n\n"
		+ "NOTE:  This will not generate a working patch.  Just close the program normally to do that.\n\n"
		+ "In normal operation, AV moves all of the variant textures out of the AV Packages folder and into "
		+ "the 'Data/Textures/' folder for in-game use.\n"
		+ "This button will simply gather them back to the AV Packages folder and quit, so that you can modify "
		+ "AV Packages manually in windows.  Make sure to re-run the patcher before attempting to play with AV again.");

	helpInfo.put(AVSaveFile.Settings.STATS_ON, "This variant setup will randomly skew the stats of an actor "
		+ "so that each spawn has a different height, health, speed, etc.\n\n"
		+ "This is applied in addition to the variant-specific stat differences that modders can"
		+ " put on their specific variants.  So, for example, if one of your AV Packages introduces "
		+ "a red troll with 15% more health, then any red troll spawns will have 15% bonus health that "
		+ "the modder desired, but will then be skewed by this setup to be slightly higher or lower for "
		+ "each spawn.\n\n"
		+ "NOTE:  This type of variant is very new, and has large potential for bugs.  Use at your own risk.");

	helpInfo.put(AVSaveFile.Settings.STATS_HEIGHT_MAX, "This determines the maximum percentage difference from "
		+ "the normal height an actor can be. \n\n"

		+ "The probability of what height an actor will spawn "
		+ "as follows a bell curve, where normal height is most"
		+ " common, and the (max / min) height is very rare (about 0.5% chance).\n\n"

		+ "NOTE: The Bethesda function used to set the size of an"
		+ " actor does NOT change its hitbox size.  Therefore, if you're"
		+ " playing an archer type character trying to get headshots, it is recommended you"
		+ " keep this setting fairly conservative.");

	helpInfo.put(AVSaveFile.Settings.STATS_HEALTH_MAX, "This determines the maximum percentage difference from "
		+ "the normal health pool an actor can have. \n\n"

		+ "The probability of how much health an actor will spawn "
		+ "with follows a bell curve, where normal health is most"
		+ " common, and the (max / min) health is very rare (about 0.5% chance).");

	helpInfo.put(AVSaveFile.Settings.STATS_MAGIC_MAX, "This determines the maximum percentage difference from "
		+ "the normal mana pool an actor can have. \n\n"

		+ "The probability of how much mana an actor will spawn "
		+ "with follows a bell curve, where normal size is most"
		+ " common, and the (max / min) size is very rare (about 0.5% chance).");

	helpInfo.put(AVSaveFile.Settings.STATS_STAMINA_MAX, "This determines the maximum percentage difference from "
		+ "the normal stamina pool an actor can have. \n\n"

		+ "The probability of how much stamina an actor will spawn "
		+ "with follows a bell curve, where normal stamina is most"
		+ " common, and the (max / min) stamina is very rare (about 0.5% chance).");

	helpInfo.put(AVSaveFile.Settings.STATS_SPEED_MAX, "This determines the maximum percentage difference from "
		+ "the normal speed an actor can have. \n\n"

		+ "The probability of what speed an actor will spawn "
		+ "with follows a bell curve, where normal speed is most"
		+ " common, and the (max / min) speed is very rare (about 0.5% chance).");

	helpInfo.put(AVSaveFile.Settings.STATS_TIE, "This setting will tie all the stat differences together, "
		+ "so that more often units will spawn either strong or weak in all areas, rather than"
		+ " randomly getting differences for each stat.\n\n"
		+ "For example, with this setting you will more likely encounter units that have more health and speed "
		+ "if they are taller, and less health and speed if they are shorter.");

	helpInfo.put(AVSaveFile.Settings.AV_SETTINGS,
		"These are AV settings related to this patcher program.");

	helpInfo.put(AVSaveFile.Settings.IMPORT_AT_START,
		"If enabled, AV will begin importing your mods when the program starts.\n\n"
		+ "If turned off, the program will wait until it is necessary before importing.\n\n"
		+ "NOTE: This setting will not take effect until the next time the program is run.\n\n"
		+ "Benefits:\n"
		+ "- Faster patching when you close the program.\n"
		+ "- More information displayed in GUI, as it will have access to the records from your mods."
		+ "\n\n"
		+ "Downsides:\n"
		+ "- Having this on might make the GUI respond sluggishly while it processes in the "
		+ "background.");

	helpInfo.put(AVSaveFile.Settings.SPEC_PACKAGE_PACKAGER,
		"Put your name as the person who compiled this package.  Especially useful if you "
		+ "are simply packaging other people's work.");

	helpInfo.put(AVSaveFile.Settings.SPEC_PACKAGE_ORIGAUTHORS,
		"Put the original authors who created the material contained in the package.");

	helpInfo.put(AVSaveFile.Settings.SPEC_PACKAGE_RELEASE,
		"Put the date that you first uploaded this package.");

	helpInfo.put(AVSaveFile.Settings.SPEC_PACKAGE_UPDATE,
		"Put the date that you last changed this package.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_AUTHOR,
		"Put the original author who created the material contained in this variant.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_HEALTH,
		"This is a percentage that setting will modify the NPC's stat to be greater/smaller"
		+ " than normal whenever this specific variant is picked.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_MAGICKA,
		"This is a percentage that setting will modify the NPC's stat to be greater/smaller"
		+ " than normal whenever this specific variant is picked.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_STAMINA,
		"This is a percentage that setting will modify the NPC's stat to be greater/smaller"
		+ " than normal whenever this specific variant is picked.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_SPEED,
		"This is a percentage that setting will modify the NPC's stat to be greater/smaller"
		+ " than normal whenever this specific variant is picked.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_HEIGHT,
		"This is a percentage that setting will modify the NPC's stat to be greater/smaller"
		+ " than normal whenever this specific variant is picked.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_NAME_AFFIX,
		"This setting will append text to the end of the NPC's name.\n\n"
		+ "For example, you could put \"of Doom\" and the variant would "
		+ "spawn as\n"
		+ "\"(Troll/Skeever/Falmer) of Doom\"" );

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_NAME_PREFIX,
		"This setting will prepend text to the beginning of the NPC's name.\n\n"
		+ "For example, you could put \"Diseased\" and the variant would "
		+ "spawn as\n"
		+ "\"Diseased (Troll/Skeever/Falmer)\"" );

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_PROB,
		"This setting will reduce the chance this variant will spawn compared to others by 1/X.\n"
		+ "A setting of 2 would mean the variant would spawn half as often as a normal variant.\n\n"
		+ "This setting can be used to adjust the probabilities of variants spawning in your package."
		+ "You can also use it to make your variants rare compared to other author's variants.");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_REGION,
		"This is a list of Cell FormIDs that this variant will be allowed to spawn in.  This means the "
		+ "variant WILL NOT spawn anywhere else but the Cells you specify.\n\n"
		+ "If you don't specify any Cells, the variant will spawn anywhere.\n\n"
		+ "NOTE: It is up to you to locate the desired Cell FormIDs and type them in correctly.\n\n"
		+ "AV expects FormIDs in this format:  Last 6 digits followed by the Mod they originated from.\n\n"
		+ "Example: 00123456 from Skyrim.esm would be typed in as \"123456Skyrim.esm\".\n\n"
		+ "You can have comments by using semicolons (;)");

	helpInfo.put(AVSaveFile.Settings.SPEC_VAR_REGION_EXCLUDE,
		"This will make the regions you specified for this variant to spawn in exclusive.  This means that no other variants besides this one will spawn there.  The one exception to this rule is that other variants that name these exclusive regions specifically will still spawn there.\n\n"
		+ "NOTE: This setting only applies if regions have been specified for this variant.");

	helpInfo.put(AVSaveFile.Settings.WIZ_PARTIAL_MATCH,
		"This will display all profiles that contain at least one texture that you are using.  "
		+ "Turning this setting off will only display profiles that use all the textures.");

	helpInfo.put(AVSaveFile.Settings.DEBUG_ON,
		"This only has an effect on in-game papyrus logs.\n\n"
		+ "This will allow AV to print debug papyrus messages.  Turn off to block all debug messages.");

	helpInfo.put(AVSaveFile.Settings.DEBUG_REGIONAL,
		"This only has an effect on in-game papyrus logs.\n\n"
		+ "This will allow debug messages about regional variants.");

	helpInfo.put(AVSaveFile.Settings.MOVE_PACKAGE_FILES,
		"After patching, AV needs to move out the package files into locations Skyrim can "
		+ "see and use.\n\n"
		
		+ "ON:  AV will physically move the files to the directories Skyrim expects.  This causes"
		+ " issues with mod manager programs that like to keep track of installed files.\n\n"
		
		+ "OFF:  AV will use a Windows feature called Hard Links, and make redirect links in the "
		+ "directories Skyrim expects, instead of actually moving the files.  This may cause issues "
		+ "for some users if their operating systems for some reason do not like Hard Links.");
    }

    public enum Settings {

	PREV_VERSION,
	PACKAGES_ON,
	PACKAGES_GATHER,
	PACKAGES_ORIG_AS_VAR,
	PACKAGES_ALLOW_EXCLUSIVE_REGION,
	PACKAGES_FORCE_REPICK,
	PACKAGES_COMPRESS,
	PACKAGES_EDIT,
	PACKAGES_ENABLE,
	PACKAGES_DISABLE,
	PACKAGES_DELETE,
	SPEC_PACKAGE_PACKAGER,
	SPEC_PACKAGE_RELEASE,
	SPEC_PACKAGE_UPDATE,
	SPEC_PACKAGE_ORIGAUTHORS,
	SPEC_VAR_AUTHOR,
	SPEC_VAR_PROB,
	SPEC_VAR_REGION,
	SPEC_VAR_REGION_EXCLUDE,
	SPEC_VAR_HEALTH,
	SPEC_VAR_MAGICKA,
	SPEC_VAR_STAMINA,
	SPEC_VAR_SPEED,
	SPEC_VAR_HEIGHT,
	SPEC_VAR_NAME_AFFIX,
	SPEC_VAR_NAME_PREFIX,
	STATS_ON,
	STATS_HEIGHT_MAX,
	STATS_HEALTH_MAX,
	STATS_MAGIC_MAX,
	STATS_STAMINA_MAX,
	STATS_SPEED_MAX,
	STATS_TIE,
	WIZ_PARTIAL_MATCH,
	IMPORT_AT_START,
	DISABLED_PACKAGES,
	PACKAGE_LISTING,
	DEBUG_ON,
	DEBUG_REGIONAL,
	AV_SETTINGS,
	MOVE_PACKAGE_FILES,
	LARGE_MULTIPLY_WARNING;
    }
}
