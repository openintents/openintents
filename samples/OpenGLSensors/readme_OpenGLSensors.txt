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

   YOU MAY HAVE TO MANUALLY INCLUDE THE OPENINTENTS-lib.JAR FILE:

If it is not included yet, in the Eclipse Package Explorer, 
right-click on the imported project OpenGLSensors, 
select "Properties", then "Java Build Path" and tab "Libraries". 
There "Add JARs..." and select lib/openintents-lib.jar. 

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

OpenGLSensors demonstrates accessing the sensors and the sensor simulator.
The pyramid displayed will always show up, regardless how you hold the
device.
The bar magnet displayed will always point along the magnetic field.

---------------------------------------------------------
release: 0.1.4
date: 2008-03-12

known bugs:
- Uses full CPU even if activity moves to background (issue #43).

---------------------------------------------------------
release: 0.1.2
date: 2008-01-28

new features:
  - Displays pyramid that always points up,
    regardless how you hold the phone.
  - Works with accelerometer and orientation sensor.
  - Displays bar magnet for compass sensor.
