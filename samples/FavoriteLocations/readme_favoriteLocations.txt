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