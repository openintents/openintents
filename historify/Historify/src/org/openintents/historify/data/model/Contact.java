package org.openintents.historify.data.model;

public class Contact {
	
	private String mKey;
	private String mName;

	public Contact(String key, String name) {
		this.mKey = key;
		this.mName = name;
	}

	public String getName() {
		return mName;
	}

	public String getLookupKey() {
		return mKey;
	}

}
