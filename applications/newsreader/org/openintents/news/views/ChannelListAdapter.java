package org.openintents.news.views;

/*
<!-- 
 * Copyright (C) 2007-2008 OpenIntents.org
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
 -->*/





import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import java.util.HashMap;
import java.util.List;
import org.openintents.provider.News;





/*
 *
 *@author ronan 'zero' schwarz
 */
 public class ChannelListAdapter extends BaseAdapter {

		private Context context;

		private List <HashMap> data;

		private static final String _TAG="ChannelListAdapter";


		public ChannelListAdapter(Context context, List<HashMap> data){

			this.context=context;
			this.data=data;
		}
	
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		/*
		 *
		 *@return HashMap with data fields for item at position
		 */
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return this.data.get(position);
		}

		public long getItemId(int position) {
			long id=0;
			HashMap cData=(HashMap)this.data.get(position);
			try
			{
				id=Long.parseLong(((String)cData.get(News._ID)));
			}
			catch (NumberFormatException nfe)
			{
				Log.e(_TAG,"couldent retrieve id from dataset");
			}
			return id;
		}


		

		public View getView(int position, View convertView, ViewGroup parent) {
			SimpleNewsChannelView smv;
			
			HashMap cData=(HashMap)this.data.get(position);
			if (convertView==null)
			{
				
				smv=new SimpleNewsChannelView(this.context,cData);

			}
			else{
				smv=(SimpleNewsChannelView)convertView;
				smv.setData(cData);
			
				
			}
			
			return smv;
		}



 }