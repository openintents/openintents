#!/bin/bash


echo "Translating AboutApp"
mkdir translations_aboutapp
cd ../../AboutApp/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_aboutapp
cd ../../translations/export_all

echo "Translating Countdown"
mkdir translations_countdown
cd ../../Countdown/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_countdown
cd ../../translations/export_all

echo "Translating FileManager"
mkdir translations_filemanager
cd ../../FileManager/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_filemanager
cd ../../translations/export_all

echo "Translating Flashlight"
mkdir translations_flashlight
cd ../../Flashlight/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_flashlight
cd ../../translations/export_all

echo "Translating NotePad"
mkdir translations_notepad
cd ../../NotePad/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_notepad
cd ../../translations/export_all

echo "Translating Safe"
mkdir translations_safe
cd ../../Safe/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_safe
cd ../../translations/export_all

echo "Translating ShoppingList"
mkdir translations_shoppinglist
cd ../../ShoppingList/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_shoppinglist
cd ../../translations/export_all

echo "Translating UpdateChecker"
mkdir translations_updatechecker
cd ../../UpdateChecker/translations
androidxml2po.bash -i
cp *.pot *.po ../../translations/export_all/translations_updatechecker
cd ../../translations/export_all

echo "Creating tar.gz file for upload..."
tar -cvvzf launchpad-export.tar.gz translations_*