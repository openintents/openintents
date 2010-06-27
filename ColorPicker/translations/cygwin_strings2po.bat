set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_colorpicker
copy colorpicker* translations_colorpicker
tar -cvvzf translations_colorpicker.tgz translations_colorpicker