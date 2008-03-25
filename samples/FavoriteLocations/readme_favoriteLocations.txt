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

---------------------------------------------------------
release: 0.1.5
date: 2008-03-26

- FavoriteLocations is obsolete and will be no longer
  supported. Its functionality has been included in 
  the core.

---------------------------------------------------------
release: 0.1.4
date: 2008-03-12

bug fixes:
- GPS access permissions set correctly (issues #32, 38).

---------------------------------------------------------
release: 0.1.3
date: 2008-02-24

features:
- new icon in Android m5 look.

---------------------------------------------------------
release: 0.1.0
date: 2007-12-19

features:
- show list of favorite locations.
- add current location to database.
- use tag intent to tag selected location.
- view selected location on map.

use cases:
- tag selected location: 
  1) add current location to database (using menu)
  2) select this location (in list of other locations)
  3) tag selected location (using menu)

know issues:
- remove location from database and/or from list of favorites not possible.