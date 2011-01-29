#!/bin/bash

# Export translations for all applications
# Jan 8, 2011, Peli

# $1..main path
# $2..translation file name
function execute
{
    mainpath=$1
	translationfilename=$2
    scriptpath=../../$mainpath/translations
	translationspath=translations_$translationfilename
    echo "Translating $mainpath"
    mkdir translations_$translationfilename
    ../scripts/androidxml2po.bash -lp "../import_all/translations_$translationfilename" -a "../../$mainpath" -n "$translationfilename" -ex "translations_$translationfilename" -e
}

execute aboutapp/AboutApp aboutapp
execute colorpicker/ColorPicker colorpicker
execute countdown/Countdown countdown
execute distribution/DistributionLibrary distributionlibrary
execute filemanager/FileManager filemanager
execute flashlight/Flashlight flashlight
execute notepad/NotePad notepad
execute safe/Safe safe
execute shoppinglist/ShoppingList shoppinglist
execute UpdateChecker updatechecker

echo "Creating tar.gz file for upload..."
tar -cvvzf launchpad-upload.tar.gz translations_*