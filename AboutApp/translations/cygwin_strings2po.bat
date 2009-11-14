set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_aboutapp
copy aboutapp* translations_aboutapp
tar -cvvzf translations_aboutapp.tgz translations_aboutapp

pause