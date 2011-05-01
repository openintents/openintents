#!/bin/bash

# Convert descriptions 2 xml
# Feb 13, 2011, Peli

# execute1: Extract application name and promo text from strings.xml
#           of the respective application.
# $1..translation file name
# $2..main path
function execute1
{
	translationfilename="$1"
    mainpath="$2"
	descriptionpath="../../$mainpath/../promotion/description/translations"
	xmlpath="../../$mainpath/res"
	xmltitle="$mainpath"
	
	rm $descriptionpath/*.txt
	echo "Translating $mainpath"
    ../scripts/descriptions2xml.bash -dp "$descriptionpath" -xp "$xmlpath" -v
}

# execute2: Extract translation of description body from xml file
#           and append it to the various applications/description directories.
# $1..translation file name
# $2..main path
function execute2
{
	#translationfilename="$1"
    #mainpath="$2"
	rootpath="../.."
	descriptionpath="../promotion/description/translations"
	xmlpath="res"
	xmltitle="$mainpath"
	
	mkdir $descriptionpath
	echo "Translating $mainpath"
    ../scripts/descriptions2xml.bash -r "$rootpath" -dp "$descriptionpath" -xp "$xmlpath" -e
}


# Read all apps that should be translated.
# sed: Remove comment lines starting with "#"
apps=( `cat "../applications_description.txt" | sed -e "s/#.*$//" -e "/^$/d"`)

# execute STEP 1 #######################
echo "############## STEP 1 ################"
for (( i = 0 ; i < ${#apps[@]} ; i+=2 ))
do
	if [ "${apps[$i]}" == "STOP" ] ; then
		break
	fi
	if [ "${apps[$i]}" == "descriptions" ] ; then
		continue
	else
		execute1 ${apps[$i]} ${apps[$i+1]}
	fi
done



# execute STEP 2 #######################
echo "############## STEP 2 ################"
execute2