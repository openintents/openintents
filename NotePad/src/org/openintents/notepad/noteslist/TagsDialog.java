package org.openintents.notepad.noteslist;

import org.openintents.notepad.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class TagsDialog extends AlertDialog implements OnClickListener {

    private static final String BUNDLE_TAGS = "tags";
    
    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public TagsDialog(Context context) {
        super(context);
        setTitle(context.getText(R.string.menu_edit_tags));
        setButton(context.getText(android.R.string.ok), this);
        setButton2(context.getText(android.R.string.cancel), (OnClickListener) null);
        setIcon(R.drawable.ic_menu_edit);
        
        LayoutInflater inflater = 
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_edit_tags, null);
        setView(view);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        final MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) view.findViewById(R.id.edit);
        textView.setAdapter(adapter);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        textView.setThreshold(0);
        textView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (textView.isPopupShowing()) {
					textView.dismissDropDown();
				} else {
					textView.showDropDown();
				}
			}
		});
        String[] mTagList = new String[0];
		if (mTagList.length < 1) {
			textView.setHint(R.string.tags_hint);
		}
    }
    

    static final String[] COUNTRIES = new String[] {
    "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
    "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
    "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
    "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
    "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
    "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory",
    "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
    "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
    "Cayman Islands", "Central African Republic", "Chad", "Chile", "China",
    "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
    "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
    "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
    "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
    "Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
    "Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia",
    "French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar",
    "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau",
    "Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary",
    "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica",
    "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
    "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
    "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands",
    "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova",
    "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
    "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand",
    "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Marianas",
    "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
    "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
    "Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe", "Saint Helena",
    "Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon",
    "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Saudi Arabia", "Senegal",
    "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
    "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Korea",
    "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden",
    "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "The Bahamas",
    "The Gambia", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey",
    "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
    "Ukraine", "United Arab Emirates", "United Kingdom",
    "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan",
    "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara",
    "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"
    };
    
    @Override
	public void onClick(DialogInterface dialog, int which) {
    	if (which == BUTTON1) {
    		
    		// Save new tags
    		
    	}
		
	}


	@Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putString(BUNDLE_TAGS, "");
        return state;
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String tags = savedInstanceState.getString(BUNDLE_TAGS);
    }
}
