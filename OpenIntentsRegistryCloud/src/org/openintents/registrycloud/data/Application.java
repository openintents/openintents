package org.openintents.registrycloud.data;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Application {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;
	
	@Persistent
	List<IntentProtocol> providedIntents;
	@Persistent
	List<IntentUri> providedUris;
	
	@Persistent
	List<IntentProtocol> usedIntents;
	@Persistent
	List<IntentUri> usedUris;
	
	public Key getId() {
		return id;
	}
}
