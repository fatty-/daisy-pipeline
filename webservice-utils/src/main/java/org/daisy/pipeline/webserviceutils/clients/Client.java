package org.daisy.pipeline.webserviceutils.clients;


public interface Client {

	public enum Role {
		ADMIN, CLIENTAPP
	}

	public String getId();


	public String getSecret();


	public Role getRole();

	public String getContactInfo();


}
