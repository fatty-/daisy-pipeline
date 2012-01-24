package org.daisy.pipeline.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Entity
public class Client extends BasicDatabaseObject {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Client.class.getName());

	@Id
	@GeneratedValue
	private String internalId;

	@Override
	public String getInternalId() {
		return internalId;
	}

	public enum Role {
	    ADMIN, CLIENTAPP
	}

	// the fields for each client object
	private String id;
	private String secret;
	private Role role = Role.CLIENTAPP;

	// in the future, use a separate table to list contact information for client app maintainers
	// with a single field, we'll just store email info
	private String contactInfo;


	public Client() {
	}

	public Client(String id, String secret, Role role, String contactInfo) {
		this.id = id;
		this.secret = secret;
		this.role = role;
		this.contactInfo = contactInfo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}

	// copy everything except the database-generated Id
	@Override
	public void copyData(BasicDatabaseObject object) {
		if (!(object instanceof Client)) {
			logger.warn("Could not copy data from differently-typed object.");
			return;
		}

		Client client = (Client) object;
		id = client.getId();
		secret = client.getSecret();
		role = client.getRole();
		contactInfo = client.getContactInfo();

	}

}
