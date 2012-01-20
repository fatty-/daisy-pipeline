package org.daisy.pipeline.database;

// An abstract database object
public abstract class BasicDatabaseObject {
	public abstract String getInternalId();
	public abstract void copyData(BasicDatabaseObject object);		
}
