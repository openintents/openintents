Import new translations:

1) Download translations from  https://translations.launchpad.net/openintents/trunk/+export
   (in the PO format)

2) Place tar file into folder "import_all".

3) Execute import_all/import_all_from_launchpad.bash or ..._cygwin.bat

4) Compile to check whether all works as expected.


Export new strings for translations:
1) Check list of applications in applications.txt.

2) Execute /export_all/export_all_to_launchpad.bash or ..._cygwin.bat

3) Commit the modified translation files. 
   (Launchpad pulls the converted strings directly from the Google code repository.)

-----------------

Translate market descriptions:

1) Execute descriptions/convert_descriptions2xml.bash or ..._cygwin.bat
   (Texts in [app]/promotion/description get converted to descriptions/res/values)

2) Follow steps above to import strings for translation.
   (Text in descriptions/res/values-[intl] are updated)

3) Execute descriptions/convert_xml2descriptions.bash or ..._cygwin.bat
   (Texts in descriptions/res/values-[intl] get converted to [app]/promotion/description/translations)

4) Follow steps above to export strings for translation.
