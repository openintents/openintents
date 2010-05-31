set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_flashlight
copy flashlight* translations_flashlight
tar -cvvzf translations_flashlight.tgz translations_flashlight