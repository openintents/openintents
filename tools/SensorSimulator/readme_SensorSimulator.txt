 ****************************************************************************
 * Copyright (C) 2008 OpenIntents.org                                  *
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


!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

   THIS IS A JAVA STANDALONE APPLICATION

* To launch it, open the sensorsimulator.jar file with your JRE
  (requires Java Runtime Environment 1.6.0 or higher).
  
* To open the project in Eclipse (requires source code):
  1) File / Import...
  2) General / Existing Projects into Workspace...
  3) Next >
  4) Select root directory (that contains the the SensorSimulator)
  5) Finish

  * alternatively do *
  
  1) File / New / Project...
  2) Java / Java project
  3) Create project from existing source
  4) For project name enter: SensorSimulator

* To create a jar file in Eclipse using the Ant script:
  1) In Package Explorer, right-click "build.xml"
  2) Run As / Ant Build
  3) Find the result in bin/sensorsimulator.jar
  
  * alternatively create it manually: *

  1) File / Export...
  2) Java / JAR file
  3) Deselect all; select only org.openintents.tools.sensorsimulator
  4) Select the export destination
  5) Next / Next / Select the class of the application entry point:
     org.openintents.tools.sensorsimulator.SensorSimulator
     
  

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

---------------------------------------------------------
release: 0.1.4
date: 2008-03-12

new features:
  - support for Sensors class updateSensorRate() and related methods.
  - new physical model for accelerometer that allows for higher 
    time-resolution. Accelerometer can be specified by spring constant
    and damping terms.

---------------------------------------------------------
release: 0.1.3
date: 2008-02-24

new features:
  - update interval can be set and is monitored
  - more settings for accelerometer / acceleration:
    pixel per meter, limit for accelerometer.
  - random contribution to sensors
  
known issues:
  - the new Sensors methods related to sensor update
    rate are not yet implemented.

---------------------------------------------------------
release: 0.1.2
date: 2008-01-28

new features:
  - runs as standalone Java application
  - also runs as Java applet in web browser, but
    connection to Android emulator is not possible 
    due to browser security restrictions
  - sensors: accelerometer, compass, orientation, temperature
  - settings: gravity, magnetic field, temperature

known issues:
  - requires JRE 1.6.
  - official definitions or compass and orientation 
    sensors are not fully specified.
    Currently these definitions are used:
    - compass: magnetic field in micro-Tesla
    - orientation: yaw, pitch, and roll (in this order)
      in degree from -180 to +180.
    - temperature: temperature in degree Celsius
    