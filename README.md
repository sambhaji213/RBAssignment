# RBAssignment

Requirements:

You have to make a service inside the app.
The service should run every 2 minutes
A Start/Stop button inside the app to start and stop the service
The service should fetch and save the data even if the app is destroyed, killed, or removed from the recent active tab.
The service should stop only if the user clicks the stop button (or) the app is force stopped from app->settings.
The data should be saved in a file in every 2 mins, it should in this pattern <time>:<ip>
  
When open the application click on the start button. If you have device above Android Nougat 7.0 then allow the storage permission and then background sync service will start the work for every 2 mintues.

You can find the file inside internal storage:

Folder Name = RB_Folder
File Name = rb_ip_file.txt

