package org.daisy.pipeline.modules;

import java.net.URL;


/**
 * The Interface ResourceLoader allows to get an accessible URI from the path provided. This is used for loading {@link Component} objects.
 */
public interface ResourceLoader {

	/**
	 * Loads the resource.
	 *
	 * @param path the path
	 * @return the uRL
	 */
	URL loadResource(String path);
}
