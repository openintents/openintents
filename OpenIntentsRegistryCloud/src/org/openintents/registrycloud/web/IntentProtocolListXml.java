package org.openintents.registrycloud.web;

import java.util.List;

import org.openintents.registrycloud.data.IntentProtocol;

import com.google.gwt.user.client.ui.Panel;

public class IntentProtocolListXml {

	private List<IntentProtocol> intents;

	public IntentProtocolListXml(List<IntentProtocol> intents) {
		this.intents = intents;
	}

	public String display() {
		String output = "<intent-protocols>";
		for (IntentProtocol s : intents) {
			output += "<intent-protocol action=\"" + s.getAction() + "\" title = \""
					+ s.getTitle() + "\">";

				output += "</intent-protocol>";

		}
		output += "</intent-protocols>";
		return output;
	}
}
