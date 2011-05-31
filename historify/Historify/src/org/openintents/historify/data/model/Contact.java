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

	public static class Comparator implements java.util.Comparator<Contact> {

		public int compare(Contact c1, Contact c2) {
			return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mKey == null) ? 0 : mKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (mKey == null) {
			if (other.mKey != null)
				return false;
		} else if (!mKey.equals(other.mKey))
			return false;
		return true;
	}

	
}
