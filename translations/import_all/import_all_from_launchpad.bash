#!/bin/bash

# Import translations for all applications
# Jan 8, 2011, Peli

# $1..main path
# $2..translation file name
function execute
{
    mainpath=$1
	translationfilename=$2
    scriptpath=../../$mainpath/translations
    echo "Translating $mainpath"
    cp translations_$translationfilename/* $scriptpath
    cd $scriptpath
    androidxml2po.bash -i
	# back to the original directory
    cd -
}


echo "Extracing translation files..."
tar -xvvzf launchpad-export.tar.gz

execute AboutApp aboutapp
execute ColorPicker colorpicker
execute Countdown countdown
execute FileManager filemanager
execute Flashlight flashlight
execute NotePad notepad
execute Safe safe
execute ShoppingList shoppinglist
execute UpdateChecker updatechecker
