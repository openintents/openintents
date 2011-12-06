#!/bin/bash

# Execute all translation scripts, following the detailed steps in readme.txt
# Dec 6, 2011, Peli

cd descriptions
./convert_descriptions2xml.bash
cd ..

cd import_all
./import_all_from_launchpad.bash
cd ..

cd descriptions
./convert_xml2descriptions.bash
cd ..

cd export_all
./export_all_to_launchpad.bash
cd ..
