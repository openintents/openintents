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

package org.openintents.historify.ui;


import org.openintents.historify.R;
import org.openintents.historify.data.providers.internal.FactoryTestProvider.FactoryTestConfig;
import org.openintents.historify.utils.Toaster;

import android.app.Activity;
import android.app.AlarmManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FactoryTestConfigActivity extends Activity {

	private EditText editTestSetSize, editEventInterval;
	private Button btnDone, btnRevert;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factory_test_config);

		editTestSetSize = (EditText)findViewById(R.id.factory_test_editTestSetSize);
		editEventInterval = (EditText)findViewById(R.id.factory_test_editEventInterval);

		FactoryTestConfig factoryTestConfig = FactoryTestConfig.load(this);
		editTestSetSize.setText(String.valueOf(factoryTestConfig.testSetSize));
		editEventInterval.setText(String.valueOf(factoryTestConfig.eventInterval / AlarmManager.INTERVAL_HOUR));
		
		btnDone = (Button)findViewById(R.id.factory_test_btnDone);
		btnRevert = (Button)findViewById(R.id.factory_test_btnRevert);
		
		btnDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onDone();
			}
		});
		
		btnRevert.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void onDone() {
		
		String strTestSetSize = editTestSetSize.getText().toString().trim();
		String strEventInterval = editEventInterval.getText().toString().trim();
		
		if(strTestSetSize.length()!=0 && strEventInterval.length()!=0) {
			
			Integer intTestSetSize=null, intEventInterval=null;
			
			try {
				intTestSetSize = Integer.parseInt(strTestSetSize);
				intEventInterval = Integer.parseInt(strEventInterval);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if(intTestSetSize!=null && intEventInterval!=null) {
				FactoryTestConfig factoryTestConfig = new FactoryTestConfig(intTestSetSize, intEventInterval * AlarmManager.INTERVAL_HOUR);
				factoryTestConfig.save(this);
				finish();
				return;
			}
		} 
			
		Toaster.toast(this, R.string.factory_test_config_error);

	}

}
