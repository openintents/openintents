#!/bin/bash

# Convert descriptions 2 xml
# Feb 13, 2011, Peli

# Jan 9, 2012: Peli: Handle DOS file ending.

# $1..translation file name
# $2..main path
function execute
{
	translationfilename="$1"
    mainpath="$2"
	descriptionfile="../../$mainpath/../promotion/description/description.txt"
	xmlfile="translations/${translationfilename}.xml"
	xmltitle="$mainpath"
    echo "Translating $mainpath"
    ../scripts/descriptions2xml.bash -d "$descriptionfile" -x "$xmlfile" -t "$xmltitle" -i
}

# Read all apps that should be translated.
# sed:
# - Convert DOS line ending to UNIX line ending using: sed 's/\r//'
# - Remove comment lines starting with "#"
# - Remove empty lines
# apps=( `cat "../applications_description.txt" | sed -e "s/#.*$//" -e "/^$/d"`)
apps=( `cat "../applications_description.txt" | sed -e "s/\r//" -e "s/#.*$//" -e "/^$/d"`)

for (( i = 0 ; i < ${#apps[@]} ; i+=2 ))
do
	if [ "${apps[$i]}" == "STOP" ] ; then
		break
	fi
	if [ "${apps[$i]}" == "descriptions" ] ; then
		continue
	else
		execute ${apps[$i]} ${apps[$i+1]}
	fi
done

# Copy all descriptions together
outfile="res/values/strings.xml"
echo '<?xml version="1.0" encoding="utf-8"?>' > "$outfile"
echo '<main>' >> "$outfile"
echo '	<translators>translator-credits</translators>' >> "$outfile"
cat translations/* >> "$outfile"
echo '</main>' >> "$outfile"
