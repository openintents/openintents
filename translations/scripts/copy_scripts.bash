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
    cp androidxml2po.bash $scriptfile
	sed -i "s/xxxxxFILENAMExxxxx/$translationfilename/g" $scriptfile
	sed -i "s/THIS IS THE MASTER FILE./THIS IS A GENERATED COPY. ANY MODIFICATIONS WILL BE OVERWRITTEN BY trunk\/translations\/scripts\/copy_scripts\.bash/g" $scriptfile
}

execute AboutApp aboutapp
execute ColorPicker colorpicker
execute Countdown countdown
execute FileManager filemanager
execute Flashlight flashlight
execute NotePad notepad
execute Safe safe
execute ShoppingList shoppinglist
execute UpdateChecker updatechecker

