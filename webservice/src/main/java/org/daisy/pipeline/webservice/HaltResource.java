package org.daisy.pipeline.webservice;

import org.restlet.data.Status;
import org.restlet.resource.Get;

public class HaltResource extends AdminResource {
	private long key;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthorized()) {
    		setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    		return;
    	}
		try {
			key = Long.parseLong(getQuery().getFirstValue("haltkey"));
		}
		catch(NumberFormatException e) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			    return;
		}
	}
	@Get
	public void getResource() {
		if (!isAuthorized()) {
    		setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    		return;
    	}
		try {
			if (!webservice().shutDown(key)) {
				setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				return;
			}
		} catch (Exception e) {
			setStatus(Status.CONNECTOR_ERROR_INTERNAL);
			return;
		}
		setStatus(Status.SUCCESS_NO_CONTENT);
	}
}
