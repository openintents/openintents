package org.openintents.filemanager.util;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {

	private Map<String, String> mMimeTypes;

	public MimeTypes() {
		mMimeTypes = new HashMap<String,String>();
	}
	
	public void put(String type, String extension) {
		mMimeTypes.put(type, extension);
	}
	
	public String getMimeType(String filename) {
		
		String extension = FileUtils.getExtension(filename);
		
		String mimetype = mMimeTypes.get(extension);
		
		return mimetype;
	}
	

}
