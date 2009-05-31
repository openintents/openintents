set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_updatechecker
copy updatechecker* translations_updatechecker
tar -cvvzf translations_updatechecker.tgz translations_updatechecker