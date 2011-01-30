set PATH=.;"C:\Program Files\Ant\apache-ant-1.8.2\bin";%PATH%

cd ../../countdown/Countdown
call ant update-recent-changes

cd ../../filemanager/FileManager
call ant update-recent-changes

cd ../../flashlight/Flashlight
call ant update-recent-changes

cd ../../notepad/NotePad
call ant update-recent-changes

cd ../../safe/Safe
call ant update-recent-changes

cd ../../shoppinglist/ShoppingList
call ant update-recent-changes
pause
