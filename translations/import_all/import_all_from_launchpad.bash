#!/bin/bash

echo "Extracing translation files..."
tar -xvvzf launchpad-export.tar.gz

echo "Translating AboutApp"
cp translations_aboutapp/* ../../AboutApp/translations
cd ../../AboutApp/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating Countdown"
cp translations_countdown/* ../../Countdown/translations
cd ../../Countdown/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating FileManager"
cp translations_filemanager/* ../../FileManager/translations
cd ../../FileManager/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating Flashlight"
cp flashlight/* ../../Flashlight/translations
cp translations_flashlight/* ../../Flashlight/translations
cd ../../Flashlight/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating NotePad"
cp translations_notepad/* ../../NotePad/translations
cd ../../NotePad/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating Safe"
cp translations_safe/* ../../Safe/translations
cd ../../Safe/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating ShoppingList"
cp translations_shoppinglist/* ../../ShoppingList/translations
cd ../../ShoppingList/translations
androidxml2po.bash -i
cd ../../translations/import_all

echo "Translating UpdateChecker"
cp translations_updatechecker/* ../../UpdateChecker/translations
cd ../../UpdateChecker/translations
androidxml2po.bash -i
cd ../../translations/import_all
