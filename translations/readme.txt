Import new translations:

1) Download translations from  https://translations.launchpad.net/openintents/trunk/+export

2) Place tar file into folder "import_all".

3) Execute import_all/import_all_from_launchpad.bash or ..._cygwin.bat

4) (Optional from time to time): Check whether new languages have been added.
Enter [app]/translations and check whether androidxml2po.bash contains all languages
that have been translated.

5) Compile to check whether all works as expected.


Export new strings for translations:
1) Execute /export_all/export_all_to_launchpad.bash or ..._cygwin.bat

2) Upload at https://translations.launchpad.net/openintents/trunk/+translations-upload
