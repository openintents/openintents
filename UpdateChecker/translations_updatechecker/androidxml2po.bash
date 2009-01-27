#!/bin/bash
#
#Wrapper for xml2po for android and launchpad: Import .xml's from .po's, or export/update .po's from string.xml's. Run from the /res directory. Provide a string with value "translator-credits" for Launchpad.
#Copyright 2009 by pjv. Licensed under GPLv3.

#Set the languages here (long version is the android resource append string).
short_lang=("nl" "de" "fr" "ar" "es" "he" "hu" "id" "it" "pl" "pt_BR" "ru" "sv" "zh_CN") #do not include template language ("en" usually).
long_lang=("nl" "de" "fr" "ar" "es" "he" "hu" "id" "it" "pl" "pt-rBR" "ru" "sv" "zh-rCN") #do not include template language ("en" usually).
#Change the dirs where the files are located. Dirs cannot have leading "."'s or msgmerge will complain.
launchpad_po_files_dir="."
launchpad_pot_file_dir="."
android_xml_files_res_dir="../res/values"
#Change the typical filenames.
launchpad_po_filename="updatechecker"
android_xml_filename="strings"
#Location of xml2po
xml2po="xml2po"

function import_po2xml
{
for (( i=0 ; i<${#short_lang[*]} ; i=i+1 )) ;
do
    echo "Importing .xml from .po for "${short_lang[i]}""
    mkdir -p "${android_xml_files_res_dir}"-"${long_lang[i]}"
    ${xml2po} -a -l "${short_lang[i]}" -p "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${short_lang[i]}".po "${android_xml_files_res_dir}"/"${android_xml_filename}".xml > "${android_xml_files_res_dir}"-"${long_lang[i]}"/"${android_xml_filename}".xml
done
}

function export_xml2po
{
echo "Exporting .xml to .pot"
${xml2po} -a -l "${short_lang[i]}" -o "${launchpad_pot_file_dir}"/"${launchpad_po_filename}".pot "${android_xml_files_res_dir}"/"${android_xml_filename}".xml

for (( i=0 ; i<${#short_lang[*]} ; i=i+1 )) ;
do
    if [ -e "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${short_lang[i]}".po ] ; then
    	echo "Exporting .xml to updated .po for "${short_lang[i]}""
    	echo "Making temporary folder: .tmp."${launchpad_po_files_dir}""
    	mkdir -p .tmp."${launchpad_po_files_dir}"
    	if [ -e "${android_xml_files_res_dir}"-"${long_lang[i]}"/"${android_xml_filename}".xml ] ; then
        	${xml2po} -a -u "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${short_lang[i]}".po "${android_xml_files_res_dir}"/"${android_xml_filename}".xml
        else
        	${xml2po} -a -u "${launchpad_po_files_dir}"/"${launchpad_po_filename}"-"${short_lang[i]}".po "${android_xml_files_res_dir}"/"${android_xml_filename}".xml
        fi
    fi 
done
}

function usage
{
    echo "Wrapper for xml2po for android and launchpad."
    echo "Usage: androidxml2po -i        Import .xml's from .po's. Updates the .xml's."
    echo "       androidxml2po -e        Export/update .po's from string.xml's. Overwrites the .pot and merges the .po's."
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
        -h | --help )           		usage
                                		exit
                                		;;
        * )                     		usage
                                		exit 1
    esac
    shift
done
usage
