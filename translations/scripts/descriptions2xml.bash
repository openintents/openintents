#!/bin/bash
#Wrapper for xml2po for android and launchpad: Import .xml's from .po's, or export/update .po's from string.xml's. Run from the /res directory. Provide a string with value "translator-credits" for Launchpad.
#Copyright 2011 by OpenIntents. Licensed under GPLv3.


#Change the dirs where the files are located.
descriptionfile="description.txt"
xmlfile="strings.xml"
xmltitle="application"

rootpath=""
descriptionpath=""
xmlpath=""
xmltitle=""

newline="\r\n"  # window style


# Set a list of all available language codes.
# After this call, the list will be something like this:
# languages=("de" "fr" "zh-rCN")
function setlanguagelist
{
	# ls: from the listing of files
	# grep: take only those that start with "values-"
	# sed: and extract only the parts after the dash "-"
	languages=( `ls "$xmlpath" | grep "^values-" | sed "s/values\-\(.*\)/\1/"`)
	
	# add "en" as default:
	languages="en $languages"
}

function import_description2xml
{
	echo "Convert $descriptionfile -> $xmlfile"
	
	# For better visibility:
	tab="	"
	
	echo "$tab<description application=\"$xmltitle\">" > "$xmlfile"
	cat "$descriptionfile" >> "$xmlfile"
	echo "" >> "$xmlfile"
	echo "$tab</description>" >> "$xmlfile"
	
	# Convert "#$ international" into "<international />"
	sed -i "s/^#\$[ ]*international[ ]*$/$tab$tab<international \/>/g" "$xmlfile"
	
	# Convert "#$ include ..." into "<include file="..." />"
	sed -i "s/^#\$[ ]*include[ ]*\(.*\)[ ]*$/$tab$tab<include file=\"\1\" \/>/g" "$xmlfile"
	
	# Convert comment lines starting with "#" into "<!-- ... -->"
	sed -i "s/^#\(.*\)/$tab$tab<!-- \1 -->/g" "$xmlfile"
	
	# Convert empty lines into "<emptyline />"
	sed -i "s/^[ ]*$/$tab$tab<emptyline \/>/g" "$xmlfile"
	
	# Convert lines starting with "*" into "<listitem>...</listitem>"
	sed -i "s/^\*[ ]*\(.*\)$/$tab$tab<listitem>\1<\/listitem>/g" "$xmlfile"
	
	# Convert lines starting with "http" into "<link href="..." />"
	sed -i "s/^[ ]*\(http.*\)[ ]*$/$tab$tab<link href=\"\1\" \/>/g" "$xmlfile"
	
	# Convert all other lines (those that don't start with tab yet) into "<string>...</string>"
	sed -i "s/^\([^$tab].*\)/$tab$tab<string>\1<\/string>/g" "$xmlfile"
}

function appendinternationalnames
{
	appnamesfile="$outpath/application_names.txt"
	appnames=""
	while read line
	do
		appnames="$appnames, $line"
	done < "$appnamesfile"
	
	# Remove last comma:
	appnames=`echo -n "$appnames" | sed "s/^\, \(.*\)$/\1/"`
	
	echo "$appnames" >> "$outfile"
}


# $1..include file name
function includefile
{
	incfile="$rootpath/$xmltitle/$descriptionpath/../$1"
	cat "$incfile" >> "$outfile"
}

# Simple XML parser:
# $E contains the element, and $C the content.
rdom () { local IFS=\> ; read -d \< E C ;}

function export_xml2description
{
	echo "Convert $xmlpath -> $descriptionpath"
	
	setlanguagelist
	
	for language in ${languages[@]}
	do
		xmlfile="$xmlpath/values-$language/strings.xml"
		if [[ $language == "en" ]] ; then
			xmlfile="$xmlpath/values/strings.xml"
		fi
		outfile=
		# cat "$xmlfile"
		while rdom; do
			# echo "element: $E, content: $C"
			if [[ $E == description* ]] ; then
				# append footer for old file name prefix
				# (if it existed)
				
				# extract file name prefix.
				xmltitle=( `echo "$E" | sed "s/.*\"\(.*\)\"/\1/"`)
				echo $xmltitle
				outpath="$rootpath/$xmltitle/$descriptionpath"
				outfile="$outpath/description-$language.txt"
				
				mkdir $outpath
				# echo "Output to: $outfile"
			fi
			if [[ $E == string* ]]; then
				echo -n "$C " >> $outfile
			fi
			if [[ $E == emptyline* ]]; then
				echo -n -e "$newline$newline" >> $outfile
			fi
			if [[ $E == listitem* ]]; then
				echo -n -e "$newline" >> $outfile
				echo -n "* $C" >> $outfile
			fi
			if [[ $E == link* ]]; then
				echo -n -e "$newline" >> $outfile
				# extract part between quotes "..."
				echo -n "$E" | sed "s/[^\"]*\"\(.*\)\"[^\"]*/\1/" >> $outfile
			fi
			if [[ $E == include* ]]; then
				echo -n -e "$newline" >> $outfile
				# extract part between quotes "..."
				includefilename=`echo -n "$E" | sed "s/[^\"]*\"\(.*\)\"[^\"]*/\1/"`
				includefile $includefilename
			fi
			if [[ $E == international* ]]; then
				echo -n -e "$newline" >> $outfile
				appendinternationalnames
			fi
			#if [[ $E = title ]]; then
			#	echo $C
			#	exit
			#fi
		done < "$xmlfile" ###> titleOfXHTMLPage.txt
		
		echo -n -e "$newline" >> $outfile
					
	done
}

function extract_values2description
{
	echo "Convert $xmlpath -> $descriptionpath"
	
	setlanguagelist
	
	i=0
	
	for language in ${languages[@]}
	do
		xmlfile="$xmlpath/values-$language/strings.xml"
		if [[ $language == "en" ]] ; then
			xmlfile="$xmlpath/values/strings.xml"
		fi
		outfile="$descriptionpath/description-$language.txt"
		txt_application=
		txt_promotext=
		# cat "$xmlfile"
		while rdom; do
			# echo "element: $E, content: $C"
			
			if [[ $E == "string name=\"app_name\"" ]]; then
				txt_application="$C"
				array_application[i]="$C"
				let "i=i+1"
			fi
			if [[ $E == "string name=\"about_comments\"" ]]; then
				txt_promotext="$C"
			fi
			#if [[ $E = title ]]; then
			#	echo $C
			#	exit
			#fi
		done < "$xmlfile" ###> titleOfXHTMLPage.txt
		
		echo -n "# Application:" >> $outfile
		echo -n -e "$newline" >> $outfile
		echo -n "$txt_application" >> $outfile
		echo -n -e "$newline$newline" >> $outfile
		echo -n "# Promo text:" >> $outfile
		echo -n -e "$newline" >> $outfile
		echo -n "$txt_promotext" >> $outfile
		echo -n -e "$newline$newline" >> $outfile
		echo -n "# Description:" >> $outfile
		echo -n -e "$newline" >> $outfile
	done
	
	# Store all application names
	outfile="$descriptionpath/application_names_tmp.txt"
	echo -n "" > $outfile
	for (( j=1; j<i; j++ ))
	do
		echo -n "${array_application[$j]}" >> $outfile
		echo -n -e "$newline" >> $outfile
	done
	outfile2="$descriptionpath/application_names.txt"
	cat $outfile | sort | uniq > $outfile2
	rm $outfile
}

function usage
{
    echo "Wrapper for descriptions2xml."
    echo "Usage: descriptions2xml -d .. -x .. -t .. -i    Convert descriptions to xml."
    echo "       descriptions2xml -r .. -dp .. -xp .. -e          Convert xml to descriptions."
    echo "       descriptions2xml -r .. -dp .. -xp .. -v          Extract app name and promo text from res/values/strings.xml."
	echo "Parameters:"
	echo " -d: description file name"
	echo " -x: xml file name"
	echo " -t: xml title"
	echo " -r: root path"
	echo " -dp: output path of translated description files"
	echo " -xp: path of xml files"
    echo ""
    echo "Copyright 2011 by OpenIntents. Licensed under GPLv3."
}

###Main
while [ "$1" != "" ]; do
    case $1 in
        -i | --import )		import_description2xml
        					exit
							;;
        -e | --export )    	export_xml2description
        					exit
							;;
        -v | --values )    	extract_values2description
        					exit
							;;
		-d )    # Description file name
				shift
				descriptionfile="$1"
				;;
		-x )    # XML file name
				shift
				xmlfile="$1"
				;;
		-t )    # Title within XML file
				shift
				xmltitle="$1"
				;;
		-dp )    # Description path
				shift
				descriptionpath="$1"
				;;
		-xp )    # XML path
				shift
				xmlpath="$1"
				;;
		-r )    # XML path
				shift
				rootpath="$1"
				;;
        -h | --help )           		usage
                                		exit
                                		;;
        * )                     		usage
                                		exit 1
    esac
    shift
done
usage
