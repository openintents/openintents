#!/bin/bash
#Wrapper for xml2po for android and launchpad: Import .xml's from .po's, or export/update .po's from string.xml's. Run from the /res directory. Provide a string with value "translator-credits" for Launchpad.
#Copyright 2009 by pjv. Licensed under GPLv3.

# Nov 14, 2009: Peli: Modified export_xml2po to work with new Launchpad scheme.
# Jan 8, 2011: Peli: Automatically delimit apostrophes: "'" <-> "\'"
# Jan 8, 2011: Peli: Check if a language .po file exists. In this way, all language codes can be included in this file.
# Jan 29, 2011: Peli: Read language codes dynamically. One central androidxml2po.bash file for all projects by introducing additional arguments.
# Jan 30, 2011: Peli: Delete lines starting with "#~" (deleted translations) from export .po files, as they may contain duplicates that cause an error message when uploading to Launchpad..
# Feb 9, 2011: Peli: Add option --nopo.
# Feb 10, 2011: Peli: Add option --notimestamp.
# Jan 8, 2012: Peli: Add special language cases: he<->iw (Hebrew), id<->in (Indonesian), yi<->ji (Yiddish)
#                    Skip "fil" (Filipino) as it is not supported on Android.
#                    Delimit "?" at the beginning of a string.

#Change the dirs where the files are located. Dirs cannot have leading "."'s or msgmerge will complain.
launchpad_po_files_dir="."
launchpad_pot_file_dir="."
android_xml_files_res_dir="../res/values"
#Change the typical filenames.
launchpad_po_filename="application_name"
android_xml_filename="strings"
#Export directory of merged .po files. MUST not start with ".", or msgmerge will complain.
export_po="export_po"
export_pot="export_pot"
#Location of xml2po
xml2po="xml2po"

# Languages will be determined automatically
languages=()

option_no_po=0
option_no_timestamp=0

# Delimit apostrophes: "'" -> "\'"
# argument $1 is file name
function delimitapostrophe
{
	qot="'"
	qot2="\\\\'"
	sed -i "s/$qot/$qot2/g" $1
	# but undo any double delimiters
	sed -i "s/\\\\$qot2/$qot2/g" $1
	
	# Delimit "?" at the beginning of a string:
	# Change ">?" into ">\?"
	# \x3f = "?"
	# \x3e = ">"
	qs="\\x3e\\x3f"
	qs2="\\x3e\\\\\\x3f"
	sed -i "s/$qs/$qs2/g" $1
}

# Undo delimit apostrophes: "\'" -> "'"
# argument $1 is file name
function undodelimitapostrophe
{
	qot="'"
	qot2="\\\\'"
	sed -i "s/$qot2/$qot/g" $1
	sed -i "s/$qot2/$qot/g" $1
	
	# Undo delimit "?" at the beginning of a string:
	# Change ">\?" into ">?"
	# \x3f = "?"
	# \x3e = ">"
	qs="\\x3e\\x3f"
	qs2="\\x3e\\\\\\x3f"
	sed -i "s/$qs2/$qs/g" $1
}

# Set a list of all available language codes.
# After this call, the list will be something like this:
# languages=("de" "fr" "zh_CN")
function setlanguagelist
{
	# ls: from the listing of files
	# grep: take only those that end with "po"
	# sed: and extract only the parts between the dash "-" and ".po"
	languages=( `ls "$launchpad_po_files_dir" | grep "po$" | sed "s/.*\-\(.*\)\.po/\1/"`)
}

# Convert language code into language code used in Android,
# by replaying "_" by "-r" behind the dash.
# e.g.: "zh_CN" -> "zh-rCN"
#       "de"    -> "de"
function set_androidlanguage_from_language
{
	androidlanguage=`echo $language | sed 's/\(.*\)_\(.*\)/\1\-r\2/g'`
	
	# Treat special cases:
	# See: http://colincooper.net/?p=238
	# http://code.google.com/p/android/issues/detail?id=3639
	if [ "$androidlanguage" = "he" ] ; then
		androidlanguage="iw"
	fi
	if [ "$androidlanguage" = "id" ] ; then
		androidlanguage="in"
	fi
	if [ "$androidlanguage" = "yi" ] ; then
		androidlanguage="ji"
	fi
}


# Remove time stamps: "POT-Creation-Date: 2010-05-29 13:09+0000\n" 
#                  -> "POT-Creation-Date: YEAR-MO-DA HO:MI+ZONE\n"
# argument $1 is file name
function removetimestamp
{
	replace="POT\-Creation\-Date\: .*\\\\n"
	by="POT\-Creation\-Date\: YEAR\-MO\-DA HO\:MI\+ZONE\\\\n"
	sed -i "s/$replace/$by/g" $1
	
	replace="PO\-Revision\-Date\: .*\\\\n"
	by="PO\-Revision\-Date\: YEAR\-MO\-DA HO\:MI\+ZONE\\\\n"
	sed -i "s/$replace/$by/g" $1
	
	replace="X\-Launchpad\-Export\-Date\: .*\\\\n"
	by="X\-Launchpad\-Export\-Date\: YEAR\-MO\-DA HO\:MI\+ZONE\\\\n"
	sed -i "s/$replace/$by/g" $1
	
	replace="X\-Generator\: Launchpad .*\\\\n"
	by="X\-Generator\: Launchpad \\(build nnnnn\\)\\\\n"
	sed -i "s/$replace/$by/g" $1
}

function import_po2xml
{
setlanguagelist
#create temporary copy of original language without delimiters:
cp "${android_xml_files_res_dir}"/"${android_xml_filename}".xml tmp_strings.xml
undodelimitapostrophe tmp_strings.xml

for language in ${languages[@]}
do
    if [ -e "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${language}".po ] ; then
		set_androidlanguage_from_language
		if [ "${androidlanguage}" = "fil" ] ; then
			# "fil" is currently not supported on Android
			echo "Skipping Filipino"
		else
			echo "Importing .xml from .po for "${language}" ("${androidlanguage}")"
			mkdir -p "${android_xml_files_res_dir}"-"${androidlanguage}"
			${xml2po} -a -l "${language}" -p "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${language}".po tmp_strings.xml > "${android_xml_files_res_dir}"-"${androidlanguage}"/"${android_xml_filename}".xml
			delimitapostrophe "${android_xml_files_res_dir}"-"${androidlanguage}"/"${android_xml_filename}".xml
		fi
    fi
done
rm tmp_strings.xml
}

function export_xml2po
{
setlanguagelist
#create temporary copy of original language without delimiters:
cp "${android_xml_files_res_dir}"/"${android_xml_filename}".xml tmp_strings.xml
undodelimitapostrophe tmp_strings.xml

# Create clean export folder for exported .po files:
echo "Making export folder: ${export_po}"
mkdir -p "${export_po}"
rm "${export_po}"/*
echo "Making export folder: ${export_pot}"
mkdir -p "${export_pot}"
rm "${export_pot}"/*

echo "Exporting .xml to .pot"
${xml2po} -a -o "${export_pot}"/"${launchpad_po_filename}".pot tmp_strings.xml
undodelimitapostrophe "${export_pot}"/"${launchpad_po_filename}".pot
if [ $option_no_timestamp -eq 1 ] ; then
	removetimestamp "${export_pot}"/"${launchpad_po_filename}".pot
fi

if [ $option_no_po -eq 0 ] ; then
	for language in ${languages[@]}
	do
	    if [ -e "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${language}".po ] ; then
			set_androidlanguage_from_language
			if [ "${androidlanguage}" = "fil" ] ; then
				# "fil" is currently not supported on Android
				echo "Skipping Filipino (only doing literal copy)"
				
				# Export a literal copy as was obtained from Launchpad
				cp "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${language}".po "${language}".po
				
				# Take out lines starting with "#~".
				# These are deleted translations, and Launchpad may show an error
				# if duplicates appear there.
				sed -i "s/#~.*//g" "${language}".po
				
				if [ $option_no_timestamp -eq 1 ] ; then
					removetimestamp "${language}".po
				fi
				mv "${language}".po "${export_po}"/"${language}".po
			else
				# All other languages (except Filipino)
		    	echo "Exporting .xml to updated .po for "${language}" ("${androidlanguage}")"
		    	if [ -e "${android_xml_files_res_dir}"-"${androidlanguage}"/"${android_xml_filename}".xml ] ; then
					# Create separate copy, because Launchpad exports "project-xx.po" but imports "xx.po".
					# WORKAROUND: 
					#    msgmerge will not merge properly, if export file is not in current directory.
					#    Therefore: first create .po file in current directory, then
					#    move it to export directory.
					cp "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${language}".po "${language}".po
		            ${xml2po} -a -u "${language}".po tmp_strings.xml
					undodelimitapostrophe "${language}".po
					
					# Take out lines starting with "#~".
					# These are deleted translations, and Launchpad may show an error
					# if duplicates appear there.
					sed -i "s/#~.*//g" "${language}".po
					
					if [ $option_no_timestamp -eq 1 ] ; then
						removetimestamp "${language}".po
					fi

					mv "${language}".po "${export_po}"/"${language}".po
		        else
				    echo "-"
		        fi
			fi
	    fi
	done
fi
rm tmp_strings.xml
}

function usage
{
    echo "Wrapper for xml2po for android and launchpad."
    echo "Usage: androidxml2po -lp .. -a .. -n .. -i        Import .xml's from .po's. Updates the .xml's."
    echo "       androidxml2po -lp .. -a .. -n .. -ex .. -e        Export/update .po's from string.xml's. Overwrites the .pot and merges the .po's."
	echo "Parameters:"
	echo " -lp: Lauchpad files path (.po and .pot files)."
	echo " -a:  Android project path."
	echo " -n:  Launchpad name for translation files."
	echo " -ex: Export path for Launchpad files."
	echo " --no-po: Option for export to suppress generation of .po files."
    echo "Set variables correctly inside. Run from the /res directory. Provide a string with value "translator-credits" for Launchpad."
    echo ""
    echo "Copyright 2009 by pjv. Licensed under GPLv3."
}

###Main
while [ "$1" != "" ]; do
    case $1 in
        -i | --po2xml | --import )         	shift
							import_po2xml
        					exit
                                		;;
        -e | --xml2po | --export )    		export_xml2po
        					exit
							;;
		-lp )	# Launchpad files path
				shift
				launchpad_po_files_dir="$1"
				launchpad_pot_files_dir="$1"
				;;
		-a )    # Android project path
				shift
				android_xml_files_res_dir="$1/res/values"
				;;
		-n )    # Launchpad name
				shift
				launchpad_po_filename="$1"
				;;
		-ex )   # Export .po and .pot path
				shift
				export_po="$1"
				export_pot="$1"
				;;
		--nopo )	# Suppress generation of po file in export
				option_no_po=1
				;;
		--notimestamp )   # Remove timestamp from po and pot files in export
				option_no_timestamp=1
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
