
package org.daisy.pipeline.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicDatabaseManager {
	
	private EntityManagerFactory entityManagerFactory = null;
	 
	private EntityManager entityManager = null;
	
	private String dbPath;
	private String persistenceUnit;
	
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(BasicDatabaseManager.class.getName());
	
	public BasicDatabaseManager(String dbPath, String persistenceUnit) {
		this.dbPath = dbPath;
		this.persistenceUnit = persistenceUnit;
	}
	
	private void openDB() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("javax.persistence.jdbc.url", String.format("jdbc:derby:%s;create=true", this.dbPath));
		
		/* tried to list properties dynamically to avoid using persistence.xml
		 * but it turns out you have to use it
		
		Class[] classes = { WSClient.class, WSRequestLogEntry.class};
		StringBuffer classNames = new StringBuffer();
		for (Class c : classes) {
			if (classNames.length() > 0)
				classNames.append(";");
			classNames.append(c.getName());
		}
		// specify list of class names as a property
		properties.put("openjpa.MetaDataFactory","jpa(Types="+classNames+")");
		
		properties.put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		properties.put("eclipselink.ddl-generation", "create-tables");
		properties.put("eclipselink.ddl-generation.output-mode", "database");
		
		properties.put("eclipselink.logging.level", "fine");
		properties.put("eclipselink.logging.thread", "false");
		properties.put("eclilpselink.logging.session", "false");
		properties.put("eclipselink.logging.exceptions", "true");
		properties.put("eclipselink.logging.timestamp", "false");
		*/
		try {
			entityManagerFactory = Persistence.createEntityManagerFactory(this.persistenceUnit, properties);
			entityManager= entityManagerFactory.createEntityManager();
		
		}
		catch (Exception e) {
			logger.error("Database exception " + e.getMessage());
		}
	}
	
	private void closeDB() {
		if (entityManager != null) {
			entityManager.close();
		}
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}
	}
	
	public void addObject(BasicDatabaseObject obj) {
		try {
			openDB();
			entityManager.getTransaction().begin();
			entityManager.persist(obj);
			entityManager.getTransaction().commit();
		}
		finally {
			closeDB();
		}
	}
	
	public boolean deleteObject(BasicDatabaseObject obj) {
		boolean retval = false;
		try {
			openDB();
			entityManager.getTransaction().begin();
			BasicDatabaseObject theObject = entityManager.find(obj.getClass(), obj.getInternalId());
			if (theObject != null) {
				entityManager.remove(theObject);
				retval = true;
			}
			entityManager.getTransaction().commit();
		}
		finally {
			closeDB();
		}
		return retval;
	}
	
	public void updateObject(String internalId, BasicDatabaseObject newData) {
		openDB();
		entityManager.getTransaction().begin();
		BasicDatabaseObject objInDB = entityManager.find(newData.getClass(), internalId);
		if (objInDB == null) {
			closeDB();
			return;
		}
		objInDB.copyData(newData);
		entityManager.getTransaction().commit();
		closeDB();
	}
	
	public BasicDatabaseObject getObjectByInternalId(String internalId, Class classname) {
		BasicDatabaseObject foundObject = null;
		
		try {
			openDB();
			
			if (entityManager != null && entityManager.isOpen()) {
				foundObject = entityManager.find(classname, internalId);
			}
		}
		finally {
			closeDB();
		}
		return foundObject;
	}
	
	public List<BasicDatabaseObject> runQuery(String queryString) {
		List<BasicDatabaseObject> list = null;
		try {
			openDB();
			Query q = entityManager.createQuery(queryString);
			list = q.getResultList();
		}
		finally {
			closeDB();
		}
		return list;
	}
	

}
