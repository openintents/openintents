package org.openintents.historify.ui.views.popup;

import org.openintents.historify.R;
import org.openintents.historify.ui.views.TimeLineTopPanel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TimeLineOptionsPopupWindow extends AbstractPopupWindow {

	private TimeLineTopPanel mPanel;
	
	public TimeLineOptionsPopupWindow(TimeLineTopPanel panel) {
		super(panel.getContext());
		mPanel = panel;
	}

	@Override
	protected void addContent(ViewGroup contentRoot) {
		
		ViewGroup contentView = (ViewGroup) ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_timeline_options, contentRoot);
		
		TextView txtHidePanel = (TextView)contentView.findViewById(R.id.timeline_options_txtHidePanel);
		txtHidePanel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
				mPanel.onHide();
			}
		});
	}
		
}
