#!/bin/bash

# Import translations for all applications
# Jan 8, 2011, Peli

# Jan 29, 2011: Peli: read list of apps from central place.
# Feb 12, 2011: Peli: Implement "STOP" command.
# Jan 9, 2012: Peli: Handle DOS file ending.

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


# echo "Extracing translation files..."
# tar -xvvzf launchpad-export.tar.gz

# Read all apps that should be translated.
# sed:
# - Convert DOS line ending to UNIX line ending using: sed 's///'
# - Remove comment lines starting with "#"
# - Remove empty lines
# apps=( `cat "../applications.txt" | sed -e "s/#.*$//" -e "/^$/d"`)
apps=( `cat "../applications.txt" | sed -e "s///" -e "s/#.*$//" -e "/^$/d"`)

for (( i = 0 ; i < ${#apps[@]} ; i+=2 ))
do
	if [ "${apps[$i]}" == "STOP" ] ; then
		break
	fi
	execute ${apps[$i]} ${apps[$i+1]}
done

