Scriptname AVQuestScript extends Quest  

float[] Property LogTable Auto

GlobalVariable Property TexturesOn Auto
GlobalVariable Property StatsOn Auto
GlobalVariable Property HeightScale Auto
GlobalVariable Property HealthScale Auto
GlobalVariable Property MagickaScale Auto
GlobalVariable Property StaminaScale Auto
GlobalVariable Property SpeedScale Auto
GlobalVariable Property TieStats Auto
GlobalVariable Property ForceRepick Auto
GlobalVariable Property DebugOn Auto
GlobalVariable Property DebugRegional Auto
Form[] Property ExclusiveCellList Auto

Event OnInit() 
	Debug.Trace("AV Quest Init Starting");
EndEvent

Function test () 
	int counter = 0
	float min = 1000
	float max = -1000
	float avg = 0;
	float avgStr = 0;
	while (counter < 5000)
		if (counter % 10 == 1) 
			Debug.Trace("Counter " + counter + " Avg " + avg / counter + " avgStr " + avgStr / counter)
		EndIf
	
		float strength = getNormalDistValue() + 4
		Debug.Trace("Strength " + strength)
		float val = rollSkewed(strength)
		Debug.Trace("Skewed " + val)
		
		if (min > val)
			min = val
		EndIf
		
		if (max < val)
			max = val
		EndIf
		
		avgStr += strength
		avg += val
		counter += 1
	EndWhile
	
	Debug.Trace("Min is " + min + " max is " + max + " avg is " + (avg / 5000) + " avgStr is " + (avgStr / 5000))
EndFunction

float Function getNormalDistValue()
	;; Box-Muller Transform
	;; Go wiki it
	int v1 = Utility.RandomInt(0, 999) 
	int v2 = Utility.RandomInt(0, 999)
	return Math.sqrt(-2 * LogTable[v1]) * Math.cos(Math.RadiansToDegrees(6.283185 * v2 / 1000.0))
EndFunction

float Function rollSkewed(float strength)
	float rand = getNormalDistValue()
	;Debug.Trace("[AV|Rand] Original Roll: " + rand)
	if (rand >= 0)
		rand = strength + (8 - strength) * (rand / 4) ;; start at the skew line and go right rand percent
	Else
		rand += 4 ;; bump rand up so it's positive
		rand = strength * (rand / 4) ;; go rand percent of left of the skew line
	EndIf
	rand -= 4 ;; Bring it back down to -4 to 4
	;Debug.Trace("[AV|Rand] Skewed at " + strength + ": " + rand)
	return rand
EndFunction

; int Function getRaceIndex(Race in) 
	; int counter = 0
	; while (counter < RaceList.getSize()) 
		; if (RaceList.getAt(counter) == in) 
			; return counter
		; EndIf
		; counter += 1
	; EndWhile
	; return counter
; EndFunction

int Function IndexOf(Form[] arr, Form target) 
	int counter = 0
	while (counter < arr.length) 
		if (arr[counter] == target) 
			return counter
		EndIf
		counter += 1
	EndWhile
	return -1
EndFunction