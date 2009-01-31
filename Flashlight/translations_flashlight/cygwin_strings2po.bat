set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
tar -cvvzf flashlight.tgz flashlight.pot flashlight*.po
