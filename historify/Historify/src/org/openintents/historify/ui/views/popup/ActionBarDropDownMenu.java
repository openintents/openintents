/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.historify.ui.views.popup;

import java.util.ArrayList;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.ui.views.ActionBar.Action;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Popup window shown when the user selects a menu symbol on the action bar. It
 * displays a list containing the menu items represented by a {@link MenuModel}
 * instance. The list could be aligned to the left or the right side of the
 * screen.
 * 
 * @author berke.andras
 */
public class ActionBarDropDownMenu extends AbstractPopupWindow {

	public static class MenuModel {

		private int gravity;

		private List<Action> actions;
		private Context context;

		public MenuModel(Context context) {
			this.context = context;
			actions = new ArrayList<Action>();
		}

		public MenuModel setGravity(int gravity) {
			this.gravity = gravity;
			return this;
		}

		public int getGravity() {
			return gravity;
		}

		public MenuModel add(int titleResId,
				View.OnClickListener onClickListener) {
			actions.add(new Action((Gravity.LEFT == gravity ? "> " : "")
					+ context.getString(titleResId), onClickListener));
			return this;
		}

		public Action[] toArray() {
			Action[] retval = new Action[actions.size()];
			return actions.toArray(retval);
		}

	}

	private ListView mListView;
	private ListAdapter mAdapter;
	private int listItemResId;

	public ActionBarDropDownMenu(Context context) {
		super(context);
		setArrowVisibility(View.GONE);
		listItemResId = R.layout.listitem_actionbar_dropdown_left;
	}

	@Override
	protected void addContent(ViewGroup contentRoot) {

		contentRoot.setBackgroundColor(mContext.getResources().getColor(
				R.color.background_dark));
		contentRoot.setPadding(0, 0, 0, 0);

		ViewGroup content = (ViewGroup) ((LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.popupwindow_actionbar_dropdown, contentRoot);

		mListView = (ListView) content
				.findViewById(R.id.actionbar_dropdown_lstMenu);
	}

	public void setMenu(MenuModel menu) {

		listItemResId = menu.getGravity() == Gravity.LEFT ? R.layout.listitem_actionbar_dropdown_left
				: R.layout.listitem_actionbar_dropdown_right;
		mAdapter = new ArrayAdapter<Action>(mContext, listItemResId,
				R.id.actionbar_dropdown_text, menu.toArray());
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view,
					int pos, long id) {
				dismiss();

				Action a = (Action) adapterView.getItemAtPosition(pos);
				if (a.onClickListener != null) {
					a.onClickListener.onClick(view);
				}
			}
		});
	}

}
