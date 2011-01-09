set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_distributionlibrary
copy distributionlibrary* translations_distributionlibrary
tar -cvvzf translations_distributionlibrary.tgz translations_distributionlibrary