#!/bin/bash

# Export translations for all applications
# Jan 8, 2011, Peli

# Jan 29, 2011: Peli: read list of apps from central place.

# $1..translation file name
# $2..main path
function execute
{
	translationfilename=$1
    mainpath=$2
    scriptpath=../../$mainpath/translations
	translationspath=translations_$translationfilename
    echo "Translating $mainpath"
    mkdir translations_$translationfilename
    ../scripts/androidxml2po.bash -lp "../import_all/translations_$translationfilename" -a "../../$mainpath" -n "$translationfilename" -ex "translations_$translationfilename" -e
}

# Read all apps that should be translated.
# sed: Remove comment lines starting with "#"
apps=( `cat "../applications.txt" | sed -e "s/#.*$//" -e "/^$/d"`)

for (( i = 0 ; i < ${#apps[@]} ; i+=2 ))
do
	execute ${apps[$i]} ${apps[$i+1]}
done


echo "Creating tar.gz file for upload..."
tar -cvvzf launchpad-upload.tar.gz translations_*