package org.openintents.registrycloud.data;

import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class IntentUri {

	@Persistent
	String title;
	@Persistent
	String basicUri;
	@Persistent
	List<String> uriVariants;
	@Persistent
	List<String> mimeTypes;
	@Persistent
	String description;
}
