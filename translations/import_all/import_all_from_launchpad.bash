#!/bin/bash

# Import translations for all applications
# Jan 8, 2011, Peli

# $1..main path
# $2..translation file name
function execute
{
    mainpath="$1"
	translationfilename="$2"
    scriptpath="../../$mainpath/translations"
    echo "Translating $mainpath"
    ../scripts/androidxml2po.bash -lp "translations_$translationfilename" -a "../../$mainpath" -n "$translationfilename" -i
}


echo "Extracing translation files..."
tar -xvvzf launchpad-export.tar.gz

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
