#!/bin/bash

# Import translations for all applications
# Jan 8, 2011, Peli

# Jan 29, 2011: Peli: read list of apps from central place.
# Feb 12, 2011: Peli: Implement "STOP" command.

# $1..translation file name
# $2..main path
function execute
{
	translationfilename="$1"
    mainpath="$2"
    scriptpath="../../$mainpath/translations"
    echo "Translating $mainpath"
    ../scripts/androidxml2po.bash -lp "translations/export_all/translations_$translationfilename" -a "../../$mainpath" -n "$translationfilename" -i
}


echo "Extracing translation files..."
tar -xvvzf launchpad-export.tar.gz

# Read all apps that should be translated.
# sed: Remove comment lines starting with "#"
apps=( `cat "../applications.txt" | sed -e "s/#.*$//" -e "/^$/d"`)

for (( i = 0 ; i < ${#apps[@]} ; i+=2 ))
do
	if [ "${apps[$i]}" == "STOP" ] ; then
		break
	fi
	execute ${apps[$i]} ${apps[$i+1]}
done
