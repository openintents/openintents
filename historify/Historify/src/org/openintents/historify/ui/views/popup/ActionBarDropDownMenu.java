package org.openintents.historify.ui.views.popup;

import java.util.ArrayList;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.ui.views.ActionBar.Action;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ActionBarDropDownMenu extends AbstractPopupWindow {

	public static class MenuModel {
		
		private List<Action> actions;
		private Context context;
		
		public MenuModel(Context context) {
			this.context = context;
			actions = new ArrayList<Action>();
		}
		
		public MenuModel add(int titleResId, View.OnClickListener onClickListener) {
			actions.add(new Action("> "+context.getString(titleResId), onClickListener));
			return this;
		}

		public Action[] toArray() {
			Action[] retval = new Action[actions.size()];
			return actions.toArray(retval);
		}
		
	}
	
	private ListView mListView;
	private ListAdapter mAdapter;
	
	public ActionBarDropDownMenu(Context context) {
		super(context);
		setArrowVisibility(View.GONE); 
	}

	public void setMenu(MenuModel menu) {
		
		mAdapter = new ArrayAdapter<Action>(mContext,R.layout.listitem_actionbar_dropdown,menu.toArray());
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int pos,
					long id) {
				dismiss();
				
				Action a = (Action) adapterView.getItemAtPosition(pos);
				if(a.onClickListener!=null) {
					a.onClickListener.onClick(view);
				}
			}
		});
	}
	
	@Override
	protected void addContent(ViewGroup contentRoot) {
				
		contentRoot.setBackgroundColor(mContext.getResources().getColor(R.color.background_dark));
		contentRoot.setPadding(0, 0, 0, 0);
		
		ViewGroup content = (ViewGroup)((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_actionbar_dropdown, contentRoot);
		
		mListView = (ListView)content.findViewById(R.id.actionbar_dropdown_lstMenu);
	}

}
