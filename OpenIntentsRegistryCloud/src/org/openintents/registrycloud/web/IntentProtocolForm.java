package org.openintents.registrycloud.web;

import org.openintents.registrycloud.data.IntentProtocol;

public class IntentProtocolForm {

	private IntentProtocol editIntent = null;

	public IntentProtocolForm(IntentProtocol editSchedule) {
		if (editSchedule != null) {
			this.editIntent = editSchedule;
		}
	}

	public String display() {
		String output = "<form action=\"/cloud/edit\" method=\"post\">"
				+ "<fieldset><legend>Intent</legend>"
				+ "<label for=\"name\">Intent action:</label> "
				+ "<input type=\"text\" name=\"intentaction\" id=\"intentaction\" value=\""
				+ (editIntent != null ? editIntent.getAction() : "")
				+ "\" />"
				+ "<label for=\"location\">Schedule Title:</label> "
				+ "<input type=\"text\" name=\"title\" id=\"title\" value=\""
				+ (editIntent != null ? editIntent.getTitle() : "")
				+ "\" />"
				
				+ (editIntent != null ? "<input type=\"hidden\" name=\"id\" value=\""
						+ editIntent.getId() + "\" />"
						: "")
				+ (editIntent == null ? "<input type=\"hidden\" name=\"action\" value=\"add\" />"
						: "")
				+ (editIntent != null ? "<input type=\"hidden\" name=\"action\" value=\"edit\" />"
						: "") + "<input type=\"submit\" value=\""
				+ (editIntent != null ? "edit" : "add") + " intent protocol\" /> "
				+ "<input type=\"submit\" name=\"action\" value=\"reset\" />"
				+ "</fieldset>" + "</form>";		
		return output;
	}

}
