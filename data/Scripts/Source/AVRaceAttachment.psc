Scriptname AVRaceAttachment extends ActiveMagicEffect 

import Utility

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Properties
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
AVQuestScript Property AVQuest Auto

;; Skin and Regional Variants
Float Property RaceHeightOffset Auto
FormList Property AltOptions Auto
Form[] Property CellIndexing Auto

;; Stat Variants
;; Numbers are stored in pairs
;; First is the variant number from the options above
;; Second is the percent difference from normal
GlobalVariable Property AVStatsOn Auto
int[] Property HeightVariants Auto
int[] Property HealthVariants Auto
int[] Property MagickaVariants Auto
int[] Property StaminaVariants Auto
int[] Property SpeedVariants Auto

bool once = false

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
EVENT OnEffectStart(Actor Target, Actor Caster)
	
	;; Don't want to do anything if any of these conditions exist
	if (once || Caster.IsDead() || Caster.IsDisabled() || Caster == Game.GetPlayer()) 
		return
	Else
		once = true;
	EndIf
	
	; float heightVar = 1
	; float healthVar = 1
	; float magickaVar = 1
	; float staminaVar = 1
	; float speedVar = 1
		
	if (AVQuest.TexturesOn.getValue() == 1 && RaceHeightOffset != 0)
		; AV's hack to get alt skin information to the scripts
		; Using the .00XX digits of the height field
		int skinIndex = (Caster.GetScale() * 10000 / RaceHeightOffset) as Int % 100
			
		;; Has Variants For Skin
		if (skinIndex < AltOptions.GetSize())
			FormList cellsList = AltOptions.GetAt(skinIndex) as FormList
			FormList defaultList = cellsList.getAt(cellsList.getSize() - 1) as FormList
			FormList regionalList
			int numVariants = 0
			Cell curCell = Caster.GetParentCell()
			Bool hasRegional = False
			Bool allowDefault = AVQuest.IndexOf(AVQuest.ExclusiveCellList, curCell) == -1
			
			;; Find out if this cell has any regional variants
			If (AVQuest.DebugOn.GetValue() > 0.0 && AVQuest.DebugRegional.GetValue() > 0.0)
				Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] Spawning in cell: " + curCell + " which is exclusive? " + !allowDefault)
			EndIf
			int cellIndex = AVQuest.IndexOf(CellIndexing, curCell)
			If (cellIndex != -1)
				If (AVQuest.DebugOn.GetValue() > 0.0 && AVQuest.DebugRegional.GetValue() > 0.0)
					Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] This cell is on the list: " + curCell + " at index " + cellIndex)
				EndIf
				regionalList = cellsList.getAt(cellIndex) as FormList
				if (regionalList != None)
					hasRegional = true
					If (AVQuest.DebugOn.GetValue() > 0.0 && AVQuest.DebugRegional.GetValue() > 0.0)
						Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] Has regional variants in FLST: " + regionalList)
					EndIf
				Endif
			EndIf
			
			;; Calc number of variants
			if (hasRegional)
				numVariants += regionalList.getSize()
			EndIf
			if (!hasRegional || allowDefault)
				numVariants += defaultList.getSize()
			EndIf
			If (AVQuest.DebugOn.GetValue() > 0.0)
				Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] " + Caster.GetActorBase() + " of skin index " + skinIndex + " has " + numVariants + " options.")
			EndIf
			
			;; Rolling the dice and picking the variant
			if (numVariants > 0)
				int rand = Utility.RandomInt(0, numVariants - 1)
				Form skin
				if (allowDefault && rand < defaultList.getSize()) 
					skin = defaultList.GetAt(rand) as Form
					If (AVQuest.DebugOn.GetValue() > 0.0)
						Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] " + Caster.GetActorBase() + " Picked variant skin " + rand + ": " + skin)
					EndIf
				else
					if (allowDefault)
						rand -= defaultList.getSize() ;; decrement to get inside the regional list index range
					endif
					skin = regionalList.GetAt(rand) as Form
					If (AVQuest.DebugOn.GetValue() > 0.0)
						Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] " + Caster.GetActorBase() + " Picked REGIONAL variant skin " + rand + ": " + skin)
					EndIf
				EndIf
				Caster.EquipItem(skin, true, true)
			EndIf
			
			;;  Variant specific stat differences
			;;
			;heightVar = grabValue(rand, HeightVariants) / 100.0
			;Debug.Trace("[AV|" + Caster.GetFormID() + "|Height] Base of " + heightVar)
			;healthVar = grabValue(rand, HealthVariants) / 100.0;
			;Debug.Trace("[AV|" + Caster.GetFormID() + "|Health] Base of " + healthVar)
			;magickaVar = grabValue(rand, MagickaVariants) / 100.0
			;Debug.Trace("[AV|" + Caster.GetFormID() + "|Magicka] Base of " + magickaVar)
			;staminaVar = grabValue(rand, StaminaVariants) / 100.0
			;Debug.Trace("[AV|" + Caster.GetFormID() + "|Stamina] Base of " + staminaVar)
			;speedVar = grabValue(rand, SpeedVariants) / 100.0
			;Debug.Trace("[AV|" + Caster.GetFormID() + "|Speed] Base of " + speedVar)
			
		elseIf (AVQuest.DebugOn.GetValue() > 0.0)
			Debug.Trace("[AV|" + Caster.GetFormID() + "|ChangeSkin] " + Caster.GetActorBase() + " Did not have variants.  (SkinIndex " + skinIndex + ")")
		EndIf	
		
	EndIf
	
	; if (AVQuest.StatsOn.getValue() == 1 )	
		; float rand
		; float strength
		
		; if (AVQuest.TieStats.GetValue() == 1)
			; strength = AVQuest.getNormalDistValue() + 4 ;; Standardize it to 0 to 8
			;;strength = AVQuest.getNormalDistValue()
			; Debug.Trace("[AV|" + Caster.GetFormID() + "|Stat] Strength rolled: " + strength)
		; Else
			; strength = 4
		; EndIf
		
		; if (AVQuest.HeightScale.getValue() != 0)
			; heightVar = AVQuest.rollSkewed(strength) * AVQuest.HeightScale.GetValue() + 1
		; EndIf
		; healthVar *= rollAV(Caster, Caster.GetFormID(), "Health", AVQuest.HealthScale.GetValue(), strength)
		; magickaVar *= rollAV(Caster, Caster.GetFormID(), "Magicka", AVQuest.MagickaScale.GetValue(), strength)
		; staminaVar *= rollAV(Caster, Caster.GetFormID(), "Stamina", AVQuest.StaminaScale.GetValue(), strength)
		; speedVar *= rollAV(Caster, Caster.GetFormID(), "SpeedMult", AVQuest.SpeedScale.GetValue(), strength)
	; EndIf
	
	;; Set stat variants
	; if (heightVar != 1.0)
		; Caster.SetScale(heightVar)
		; Debug.Trace("[AV|" + Caster.GetFormID() + "|Height] Changed height to " + heightVar)
	; EndIf
	; setAV(Caster, Caster.GetFormID(), "Health", healthVar)
	; setAV(Caster, Caster.GetFormID(), "Magicka", magickaVar)
	; setAV(Caster, Caster.GetFormID(), "Stamina", staminaVar)
	; setAV(Caster, Caster.GetFormID(), "SpeedMult", speedVar)

EndEvent

;; Used to check if specific variant has special settings
int Function grabValue (int variant, int[] array)
	int counter = 0;
	while (counter < array.Length)
		if (variant == array[counter])
			return array[counter + 1]
		EndIf
		if (variant > array[counter])
			return 100
		EndIf
		counter += 2
	EndWhile
	return 100
EndFunction

string Function grabStrValue (int variant, int[] array, string[] valueArray)
	int counter = 0;
	while (counter < array.Length)
		if (variant == array[counter])
			return valueArray[counter]
		EndIf
		if (variant > array[counter])
			return ""
		EndIf
		counter += 1
	EndWhile
	
	return ""
EndFunction

float Function rollAV (Actor Caster, int ID, String av, float scale, float strength) 
	if (scale != 0)
		float rand = AVQuest.rollSkewed(strength)
		rand = rand * scale + 1
		return rand;
	EndIf
	return 1;
EndFunction

Function setAV (Actor Caster, int ID, String av, float amount)
	if (amount != 1.0)
		Caster.setAV(av, Caster.getAV(av) * amount)
		If (AVQuest.DebugOn.GetValue() > 0.0)
			Debug.Trace("[AV|" + ID + "|" + av + "] Changed to " + amount)
		EndIf
	EndIf
EndFunction