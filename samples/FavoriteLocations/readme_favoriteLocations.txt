 ****************************************************************************
 * Copyright (C) 2007 OpenIntents.org                                       *
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
release: 0.0.4
date: 2007-12-19

features:
- LocationsProvider
- TagsProvider
- TagView for OpenIntent.TAG action

use cases:
- view locations:
  1) select "show locations"
  2) add current location to database (using menu)
- view and add tags
  1) select "show tags"
  2) enter tag and content in text fields (or select tag from list of content)
  3) click "add"
- use OpenIntent.TAG
  1) create a new application, import OpenIntents-n-n-n.jar
  2) create an Intent with action = OpenIntent.TAG and uri = Tags.CONTENT_URI
  3) add content to Intent using putExtrag(Tags.QUERY_URI, myContentToTag)
  4) startSubActivity using the Intent
  5) return code is Activity.RESULT_OK if a tag has been added to the myContentToTag
  

know issues:
- LocationsProvider: remove location from database not possible.
- TagsProvider: remove tags and content from database not possible.