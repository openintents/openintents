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

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

   YOU HAVE TO MANUALLY INCLUDE THE OPENINTENTS-lib-n.n.n.JAR FILE:

In the Eclipse Package Explorer, right-click on the imported 
project FavoriteLocations, select "Properties", then "Java Build Path" 
and tab "Libraries". There "Add External JARs..." and select
lib/openintents-lib-n.n.n.jar. 

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

PresentPicker picks the right present for you.
It demonstrates the usage of the Shopping List application.

---------------------------------------------------------
release: 0.1.1
date: 2008-01-07

features:
- Select contact and search criteria
- MagicOracle for finding suitable presents
- Write result to shopping list
- view shopping list

use cases:
- fill out the forms and press "Search".  

known issues:
- MagicOracle fails in some rare instances
