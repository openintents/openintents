package org.openintents.shopping;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class ShoppingBackupAgent {

	public class MyPrefsBackupAgent extends BackupAgentHelper {
	    // The name of the SharedPreferences file
	    static final String PREFS = "user_preferences";

	    // A key to uniquely identify the set of backup data
	    static final String PREFS_BACKUP_KEY = "prefs";

	    static final String DB_BACKUP_KEY = "db";

	    // Allocate a helper and add it to the backup agent
	    public void onCreate() {
	        SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(this, PREFS);
	        addHelper(PREFS_BACKUP_KEY, prefsHelper);

	        FileBackupHelper helper = new FileBackupHelper(this, "databases/" + ShoppingProvider.DATABASE_NAME);
	        addHelper(DB_BACKUP_KEY, helper);
	    }
	}
}
