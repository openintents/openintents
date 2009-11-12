package org.openintents.registrycloud.data;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType= IdentityType.APPLICATION)
public class IntentProtocol {

	@NotPersistent
	private static PersistenceManager pm;	

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	String title;
	@Persistent
	String action;
	@Persistent
	String input;
	
	
	//List<IntentExtra> extras;
	@Persistent
	String output;
	@Persistent
	String description;
	
	
	//List<Application> providingApplications;
	
	//List<Application> usingApplications;
	
	@Persistent
	private int version;
	
	public Long getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the PersistenceManager for the IntentProtocol Class, creates one if not
	 * existent or closed
	 * 
	 * @return PersistenceManager
	 */
	private static PersistenceManager getPersistenceManager() {
		if (pm == null) {
			pm = PMF.get().getPersistenceManager();
		} else if (pm.isClosed()) {
			pm = PMF.get().getPersistenceManager();
		}
		return pm;
	}

	
	/**
	 * Returns all IntentProtocols as a List from the persistent storage
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<IntentProtocol> findAll() {
		String sqlFetchAll = "select from " + IntentProtocol.class.getName();
		Query query = getPersistenceManager().newQuery(sqlFetchAll);
		List schedules = (List) query.execute();
		return schedules;
	}
	
	/**
	 * Finds the IntentProtocol in the database by the given id
	 * 
	 * @param id
	 *            integer with the id of the IntentProtocol
	 * @return IntentProtocol object found in the database
	 */
	public static IntentProtocol find(int id) {
		IntentProtocol intentProtocol = IntentProtocol.getPersistenceManager().getObjectById(
				IntentProtocol.class, id);
		return intentProtocol;
	}
	/**
	 * Deletes the IntentProtocol object from the persistent storage
	 * 
	 * @return
	 */
	public boolean delete() {
		boolean wasSuccessful;
		try {
			getPersistenceManager().deletePersistent(this);
			wasSuccessful = true;
		} catch (Exception e) {
			wasSuccessful = false;
		} finally {
			getPersistenceManager().close();
		}
		return wasSuccessful;
	}

	
	/**
	 * Makes the IntentProtocol object persistent
	 * 
	 * @return boolean true if successful, false if not
	 */
	public boolean persist() {
		boolean wasSuccessful;
		try {
			version++;
			getPersistenceManager().makePersistent(this);
			wasSuccessful = true;
		} catch (Exception e) {
			wasSuccessful = false;
		} finally {
			getPersistenceManager().close();
		}
		return wasSuccessful;
	}

}
