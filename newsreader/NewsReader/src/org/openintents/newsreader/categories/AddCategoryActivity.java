package org.openintents.newsreader.categories;
/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/

import org.openintents.newsreader.R;
import org.openintents.provider.News;
import org.openintents.provider.News.Categories;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddCategoryActivity extends Activity {
	protected News mNews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Dialog);
		
		mNews = new News(getContentResolver());

		setContentView(R.layout.add_category);
		setTitle(R.string.addcategory);
		

		Button b = (Button) findViewById(R.id.cancel);
		b.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				// cancel
				setResult(RESULT_CANCELED);
				finish();
			}

		});
		b = (Button) findViewById(R.id.ok);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				EditText editView = (EditText) findViewById(R.id.editview);

				// add trimmed category
				String newCat = editView.getText().toString().trim();
				if (newCat.length() > 0) {
					ContentValues cs = new ContentValues();
					cs.put(Categories.NAME, newCat);
					Uri uri = mNews.insertIfNotExists(Categories.CONTENT_URI,
							"upper(" + Categories.NAME + ") = upper(?)",
							new String[] { newCat }, cs);

					if (uri == null) {
						Toast.makeText(AddCategoryActivity.this,
								R.string.category_exists, Toast.LENGTH_LONG)
								.show();
					} else {
						setResult(RESULT_OK, new Intent().putExtra(
								Categories.NAME, newCat));
						finish();
					}
				}
			}
		});
	}
}
