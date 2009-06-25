package org.openintents.countdown.activity;

import java.util.ArrayList;

import org.openintents.compatibility.activitypicker.DialogHostingActivity;
import org.openintents.countdown.R;
import org.openintents.intents.AutomationIntents;
import org.openintents.utils.SDKVersion;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

/**
 * Displays the shortcut creation dialog and launches, if necessary, the
 * appropriate activity.
 */
public class SelectTaskDialog implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener {
	
	private static final String TAG = "SelectTaskDialog";
	private static final boolean debug = true;
	
    private AddAdapter mAdapter;
    private Activity mActivity;


	/**
	 * Show the option to choose either "Shortcuts"
	 * or "Automation tasks".
	 * 
	 * @param intent
	 */
    Dialog createDialog(Activity activity) {
    	mActivity = activity;
        mAdapter = new AddAdapter(mActivity);
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.menu_set_action));
        builder.setAdapter(mAdapter, this);
        
        builder.setInverseBackgroundForced(true);

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(this);

        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
        cleanup();
    }

    private void cleanup() {
        mActivity.dismissDialog(CountdownEditorActivity.DIALOG_SET_AUTOMATION);
    }

    /**
     * Handle the action clicked in the "Add to home" dialog.
     */
    public void onClick(DialogInterface dialog, int which) {
        Resources res = mActivity.getResources();
        cleanup();
        
        switch (which) {
            case AddAdapter.ITEM_SHORTCUT: {
                // Insert extra item to handle picking application
                Bundle bundle = new Bundle();
                
                ArrayList<String> shortcutNames = new ArrayList<String>();
                shortcutNames.add(res.getString(R.string.group_applications));
                bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
                
                ArrayList<ShortcutIconResource> shortcutIcons =
                        new ArrayList<ShortcutIconResource>();
                shortcutIcons.add(ShortcutIconResource.fromContext(mActivity,
                        R.drawable.ic_launcher_application));
                bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
                
                Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(Intent.EXTRA_INTENT,
                        new Intent(Intent.ACTION_CREATE_SHORTCUT));
                pickIntent.putExtra(Intent.EXTRA_TITLE,
                        mActivity.getText(R.string.title_select_shortcut));
                pickIntent.putExtras(bundle);
                
                if (SDKVersion.SDKVersion < 3) {
                	if (debug) Log.i(TAG, "Compatibility mode for ActivityPicker");
	                // SDK 1.1 backward compatibility:
	                // We launch our own version of ActivityPicker:
	                pickIntent.setClass(mActivity, DialogHostingActivity.class);
	                pickIntent.putExtra(DialogHostingActivity.EXTRA_DIALOG_ID, 
	                			DialogHostingActivity.DIALOG_ID_ACTIVITY_PICKER);
                } else {
                	if (debug) Log.i(TAG, "Call system ActivityPicker");
                }
                
                mActivity.startActivityForResult(pickIntent, CountdownEditorActivity.REQUEST_CODE_PICK_SHORTCUT);
                break;
            }

            case AddAdapter.ITEM_AUTOMATION_TASK: {
                Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(Intent.EXTRA_INTENT,
                        new Intent(AutomationIntents.ACTION_EDIT_AUTOMATION_SETTINGS));
                pickIntent.putExtra(Intent.EXTRA_TITLE,
                        mActivity.getText(R.string.title_select_automation_task));

                if (SDKVersion.SDKVersion < 3) {
                	if (debug) Log.i(TAG, "Compatibility mode for ActivityPicker");
	                // SDK 1.1 backward compatibility:
	                // We launch our own version of ActivityPicker:
	                pickIntent.setClass(mActivity, DialogHostingActivity.class);
	                pickIntent.putExtra(DialogHostingActivity.EXTRA_DIALOG_ID, 
	                			DialogHostingActivity.DIALOG_ID_ACTIVITY_PICKER);
                } else {
                	if (debug) Log.i(TAG, "Call system ActivityPicker");
                }
                
                mActivity.startActivityForResult(pickIntent, CountdownEditorActivity.REQUEST_CODE_PICK_AUTOMATION_TASK);
                break;
            }
            
        }
    }

}
