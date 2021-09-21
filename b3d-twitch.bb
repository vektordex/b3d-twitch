;This Twitch>B3D Interconnection has been written by René Marcel Dirks, also known as Dex

Global ChatServer$ = "irc.twitch.tv" 
Global ChatPort = 6667 
Global ChatHostName$ = "" ;Enter Your Hostname here.
Global ChatNickName$ = "" ;Enter Bot Username here
Global ChatChannel$

Const MaxEvents = 1 ;Change this to the amount of Events you programmed

Global Twitch_Integration=1 	;enables the Twitch Integration
Global Twitch_EventTimer		;90sec 
Global Twitch_Cooldown=10800 	;180sec
Global Twitch_EventID			;Determines the Event that was started
Global Twitch_EventName$		;Name to be displayed for the event
Global Twitch_EventDesc_A$
Global Twitch_EventDesc_B$
Global Twitch_EventDesc_C$
Global Twitch_EventOption_A$
Global Twitch_EventOption_B$
Global Twitch_Event_Timer
Global Twitch_Recoilspeed
Global Twitch_ViewerWinner
Global Twitch_EventState
Global Twitch_LogFile

Global Twitch_Time$

Global Twitch_VotersA, Twitch_VotersB

Global Twitch_ChatData$, Twitch_ChatName$

Type Twitch_ViewerChoice
	Field Viewername$
	Field Choice
End Type

Function Twitch_Connect()
	ChatStream = OpenTCPStream(ChatServer$, 6667) 
	
	If Not ChatStream Then RuntimeError "Failed to connect to twitch.tv"
	
	WriteLine ChatStream, "USER " + ChatNickName$ + " " + ChatHostName$ + " " + ChatServer$ + " :" + ChatNickName$ 
	WriteLine ChatStream, "PASS oauth:xxx" ;Enter your oauth code for your bot here, you can find it here: https://twitchapps.com/tmi/
	WriteLine ChatStream, "NICK " + ChatNickName$ 
	WriteLine ChatStream, "PONG "
	WriteLine ChatStream, "JOIN " + ChatChannel$
	
	; If you have a Log, make it add the connection here
;	Local TempMessage$="Connection to Twitch successful (Streamer: "+ChatChannel$+")"
;	AddChat (TempMessage$,255,255,255,"Twitch.TV API")
End Function

Function Twitch_GetChatData()
	Bytes = ReadAvail(ChatStream) 
	
	If Bytes <> 0 
		
		Twitch_ChatData$ = ReadLine(ChatStream)
		
		Twitch_Log_Add(Twitch_ChatData$)
		
		If Left$(Twitch_ChatData$, 4) = "PING" Then 
			
            WriteLine ChatStream, "PONG " + Mid$(Daten$, 7, Len(Daten$) - 6) 
			; Log Message here
			; AddChat ("Twitch.tv sent you a PING, sent PONG back.",255,255,255,CurrentTime()+" [SYSTEM]: ")
			
		EndIf 
		
		If Instr(Twitch_ChatData$,"PRIVMSG") Then
			; Message Handling and Filtering
			Local FindUserName = Instr(Twitch_ChatData$,"!")
			Twitch_ChatName$=Mid$(Twitch_ChatData$,2,FindUserName-2)
			
			Local ChatDatenPos = Instr(Twitch_ChatData$, " :",3)
			Twitch_ChatData$ = Right(Twitch_ChatData$,(Len(Twitch_ChatData$)-ChatDatenPos-1))
			
			AddChat(Twitch_ChatData$,CurrentTime()+" ["+Twitch_ChatName$+"]: ")
			; END Message Handling
			
			; Event Triggering
			If Instr(Twitch_ChatData$,"#event") Then 
				If Twitch_Cooldown < 1 Then
					Twitch_EventState=1
					PlaySound Sound_Event[0]
				EndIf
			; Special Events for your supporters and/or devs
			ElseIf Instr(Twitch_ChatData$,"#opevent") Then
				If Twitch_Cooldown < 1 Then
						Twitch_EventID = 900 ;The Event ID
						Twitch_EventName$="THE COMMANDER STRIKES" ; Event Name to be Displayed
						Twitch_EventDesc_A$="The Crimson Dawn Commander wants to hunt "+Character_Value_Name$+" down."
						Twitch_EventDesc_B$="Type #yes to join the hunt or #no to leave him alone." ; Two lines of Description
						Twitch_EventOption_A$="HUNT HIM DOWN!" ;Bhoice A
						Twitch_EventOption_B$="Nah, I like that guy!";Choice B
						Twitch_EventState=3
						Twitch_EventTimer = 90
						Twitch_Cooldown = 180
					EndIf
					
				EndIf
			ElseIf Twitch_EventState = 3 ;Count Votes
				If Instr(Twitch_ChatData$,"#yes") Or Instr(Twitch_ChatData$,"#a") Then
					Twitch_AddUserVote(Twitch_ChatName,1)
				ElseIf Instr(Twitch_ChatData$,"#no") Or Instr(Twitch_ChatData$,"#b") Then
					Twitch_AddUserVote(Twitch_ChatName,2)
				EndIf
			EndIf
			
			
		EndIf
		
		Bytes = ReadAvail(ChatStream) 
		
	EndIf
End Function

Function Twitch_Update()
	
	If HUD=1 Then Text3D(Text_Font[8],D3DOR-146,D3DOU-274,"twitch.tv/"+Character_Value_Name$+" to play!",1,0,0)
	
	Select Twitch_EventState
		Case 0 ;Base State of Twitch, Bot-Cooldown, no events possible
			If Twitch_Event_Timer>0 Then
				Select Twitch_EventID
					Case 1
						If Twitch_ViewerWinner = 1 Then
							;Insert Option 1 Happening here
						ElseIf Twitch_ViewerWinner = 2 Then
							;Insert Option 2 Happening here
						EndIf
				End Select
			Else
				
			EndIf
			
			If Twitch_Time$ <> CurrentTime()
				Twitch_Cooldown = Twitch_Cooldown - 1
				Twitch_Event_Timer = Twitch_Event_Timer - 1
			EndIf
			Twitch_Time$=CurrentTime()
			If HUD=1 
				If Twitch_Cooldown>0 Then
					;Display a cooldown to not spam events
				Else
					;Prompt Users to type #event in chat
				EndIf
			EndIf
		Case 1 ; Event got Triggered, find random Event
			Twitch_Cooldown = 120
			Twitch_EventID = Rand(1,MaxEvents)
			Twitch_EventTimer = 60
			Twitch_EventState = 2
		Case 2 ; Load Event Data
			Select Twitch_EventID
				Case 1						;Example Event
					Twitch_EventName$="Things"
					Twitch_EventDesc_A$="A description"
					Twitch_EventDesc_B$="Another Description"
					Twitch_EventDesc_C$="How disastrous would YOU like it?"
					Twitch_EventOption_A$="Give us the standard!"
					Twitch_EventOption_B$="Go insane!"
				Default ;Tutorial Event, make sure this exists for your own sanity
					Twitch_EventName$="You've been DEFAULTED!"
					Twitch_EventDesc_A$="Due to a bug, "+Character_Value_Name$+" is now on your mercy."
					Twitch_EventDesc_B$="Use !yes for a lootbox or !no for some pirates!"
					Twitch_EventOption_A$="Lootbox!"
					Twitch_EventOption_B$="Pirates!"
			End Select
			Twitch_EventState=3
		Case 3 ; Display Event Data to streamer, collect votes, countdown 1:30
						
			Twitch_Updatevotes()
			
			If Twitch_EventTimer< 0 Then
				Twitch_EventState = 4
			EndIf
		Case 4 ; Execute Event.
			Twitch_ViewerWinner = Twitch_CountUserVotes()
			Select Twitch_EventID
				Case 1
			
				Default ;Tutorial Event Execution
					If Twitch_ViewerWinner = 1 Then
						
						AddChat ("Chat Voted for A","")
					ElseIf Twitch_ViewerWinner = 2
				
						AddChat ("Chat Voted for B","")
					EndIf
			End Select
			Twitch_EventState=5
		Case 5 ; After Executing Event, Clean up Vote Database, Set cooldown 
			For Votes.Twitch_ViewerChoice = Each Twitch_ViewerChoice
				Delete Votes
			Next
			Twitch_EventState=0
	End Select
End Function

Function Twitch_AddUserVote(Username$, VoteState)
	
	Local UserFound=0
	For Votes.Twitch_ViewerChoice = Each Twitch_ViewerChoice
		If Votes\Viewername$ =Twitch_ChatName$ Then
			Votes\Choice = VoteState
			UserFound=1
		EndIf
	Next
	
	If UserFound = 0 Then
		Votes.Twitch_ViewerChoice = New Twitch_ViewerChoice
		Votes\Viewername$ = Twitch_ChatName$
		Votes\Choice=VoteState
	EndIf
	
End Function

Function Twitch_CountUserVotes()
	Local VotesA = 0
	Local VotesB = 0
	Local VoteWin = 0
	
	For Votes.Twitch_ViewerChoice = Each Twitch_ViewerChoice
		If Votes\Choice = 1 Then VotesA = VotesA + 1
		If Votes\Choice = 2 Then VotesB = VotesB + 1
	Next
	
	If VotesA > VotesB Then VoteWin = 1
	If VotesB > VotesA Then VoteWin = 2
	
	Return VoteWin
End Function

Function Twitch_Updatevotes()
	Twitch_VotersA = 0
	Twitch_VotersB = 0
	
	For Votes.Twitch_ViewerChoice = Each Twitch_ViewerChoice
		If Votes\Choice = 1 Then Twitch_VotersA = Twitch_VotersA + 1
		If Votes\Choice = 2 Then Twitch_VotersB = Twitch_VotersB + 1
	Next
	
End Function

Function Twitch_Log_Start()
	Twitch_LogFile=WriteFile(UserData+"\twitch_chatlog.txt")
	WriteLine Twitch_LogFile,"Starting Log of Twitch_Chat."
End Function

Function Twitch_Log_End()
	CloseFile Twitch_LogFile
End Function

Function Twitch_Log_Add(twitch_Message$)
	WriteLine Twitch_LogFile,twitch_Message$
End Function

Function Twitch_AddEmote(Emote)
	;to come in a future update
End Function