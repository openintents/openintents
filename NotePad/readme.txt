 ****************************************************************************
 * Copyright (C) 2008 OpenIntents.org                                       *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 *      http://www.apache.org/licenses/LICENSE-2.0                          *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************

The OpenIntents Notepad is based on Google's open source 
sample application Notepad that is provided with the Android SDK. 

OI Notepad allows to create, edit, and delete notes.

To obtain the current release, visit
  http://www.openintents.org


---------------------------------------------------------
release: 1.1.0
date: 2009-?

- upgrade Content Provider with new fields:
  tags, encrypted, theme
- support for encrypted notes through CryptoIntents
  (requires Android Password Safe or compatible)
- tags for notes.
- quickly filter notes by typing the first letters;
  searches through title and tags.
- open .txt files from SD card and save .txt files
  to SD card.
- prepare for permissions to access notes
  (but don't activate them yet).
- support for OI About.

---------------------------------------------------------
release: 1.0.2
date: 2008-12-10

- allow alternative menus that affect the whole list
  of notes. Allows support for ConvertCSV.

---------------------------------------------------------
release: 1.0.1
date: 2008-11-21

- removed Internet permission
- fix for lost note on screen lock
- revert twice to undo last revert
- broadcast changes to database so that extensions
  like VoiceNotes can listen.

---------------------------------------------------------
release: 1.0.0
date: 2008-10-29

- First public release on Android SDK 1.0.

Features: 
- Create, edit, delete notes.
- Send note.

Difference from the original Android SDK version:
- Fixed bug in connection with orientation change.
- Automatically pick title from first line of note.
  Drop title editor.