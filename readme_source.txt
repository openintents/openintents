 ****************************************************************************
 * Copyright (C) 2007-2008 OpenIntents.org                                  *
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

OpenIntents defines and implements open interfaces for
improved interoperability of Android applications.

To obtain the current release, visit
  http://www.openintents.org

-----------------------------------------------------------------------------

OpenIntents

The directory structure is organized as follows:

lib/
   contains the latest library JAR version

openintents/
   contains all core functionality

release/
   contains scripts to build the full release

samples/
   contains samples that use openintents

tools/
   contains additional tools (SensorSimulator)


!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!!  Application notes
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

The following applies to all samples and applications:

YOU HAVE TO MANUALLY INCLUDE THE OPENINTENTS-lib.JAR FILE:
 
If it is not included yet, in the Eclipse Package Explorer, 
right-click on the imported project PresentPicker, 
select "Properties", then "Java Build Path" and tab "Libraries". 
There "Add JARs..." and select lib/openintents-lib.jar. 

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

The following applies to all tools:

THIS IS A JAVA STANDALONE APPLICATION

* To launch it, open the sensorsimulator.jar file with your JRE
  (requires Java Runtime Environment 1.6.0 or higher).
  
* To open the project in Eclipse (requires source code):
  1) File / Import...
  2) General / Existing Projects into Workspace...
  3) Next >
  4) Select root directory (that contains the the SensorSimulator)
  5) Finish

* To create a jar file in Eclipse using the Ant script:
  1) In Package Explorer, right-click "build.xml"
  2) Run As / Ant Build
  3) Find the result in bin/sensorsimulator.jar
 
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!