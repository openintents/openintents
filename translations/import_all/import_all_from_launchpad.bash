#!/bin/bash

# Import translations for all applications
# Jan 8, 2011, Peli

# Jan 29, 2011: Peli: read list of apps from central place.
# Feb 12, 2011: Peli: Implement "STOP" command.
# Jan 9, 2012: Peli: Handle DOS file ending.
# Jan 19, 2012: Peli: add option manualdownload.

# $1..translation file name
# $2..main path
function execute
{
	translationfilename="$1"
	mainpath="$2"
	scriptpath="../../$mainpath/translations"
	echo "Translating $mainpath"
	../scripts/androidxml2po.bash -lp "translations/export_all/translations_$translationfilename" -a "../../$mainpath" -n "$translationfilename" $manualdownload -i
}

manualdownload=""

if [ -e "launchpad-export.tar.gz" ] ; then
	manualdownload="--manualdownload"

	# Extract translation files as obtained from Launchpad
	echo "Extracing translation files..."
	tar -xvvzf launchpad-export.tar.gz
fi

# Read all apps that should be translated.
# sed:
# - Convert DOS line ending to UNIX line ending using: sed 's/\r//'
# - Remove comment lines starting with "#"
# - Remove empty lines
# apps=( `cat "../applications.txt" | sed -e "s/#.*$//" -e "/^$/d"`)
apps=( `cat "../applications.txt" | sed -e "s/\r//" -e "s/#.*$//" -e "/^$/d"`)

for (( i = 0 ; i < ${#apps[@]} ; i+=2 ))
do
	if [ "${apps[$i]}" == "STOP" ] ; then
		break
	fi
	execute ${apps[$i]} ${apps[$i+1]}
done

