set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_countdown
copy countdown* translations_countdown
tar -cvvzf translations_countdown.tgz translations_countdown