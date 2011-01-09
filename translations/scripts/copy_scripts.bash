# Copies androidxml2po.bash into the various projects
# and modifies the script slighly.

# Peli, Jan 8, 2011

# $1..main path
# $2..translation file name
function execute
{
    mainpath=$1
	translationfilename=$2
    scriptfile=../../$mainpath/translations/androidxml2po.bash
    echo "Copying scripts for $mainpath"
    cp androidxml2po.bash $scriptfile
	sed -i "s/xxxxxFILENAMExxxxx/$translationfilename/g" $scriptfile
	sed -i "s/THIS IS THE MASTER FILE./THIS IS A GENERATED COPY. ANY MODIFICATIONS WILL BE OVERWRITTEN BY trunk\/translations\/scripts\/copy_scripts\.bash/g" $scriptfile
}

execute aboutapp/AboutApp aboutapp
execute colorpicker/ColorPicker colorpicker
execute countdown/Countdown countdown
execute distribution/DistributionLibrary distributionlibrary
execute filemanager/FileManager filemanager
execute flashlight/Flashlight flashlight
execute notepad/NotePad notepad
execute safe/Safe safe
execute shoppinglist/ShoppingList shoppinglist
execute UpdateChecker updatechecker

