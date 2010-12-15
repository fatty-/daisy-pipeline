package org.daisy.pipeline.modules.tracker;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.daisy.expath.parser.EXPathPackageParser;
import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class DefaultModuleRegistry implements ModuleRegistry {

	HashMap<URI, Module> mComponentsMap= new HashMap<URI, Module>();
	HashSet<Module> mModules= new HashSet<Module>();
	private EXPathPackageParser mParser;

	/*
	private final Function<Bundle, Module> toModule = new Function<Bundle, Module>() {

		public Module apply(Bundle bundle) {
		
			return parser.parse(bundle.getEntry("expath-pkg.xml"));
		}

	};
	 */
	private EXPathPackageTracker tracker;
	

	public DefaultModuleRegistry() {
	}

	public void init(BundleContext context) {
		tracker = new EXPathPackageTracker(context,this);
		tracker.setParser(mParser);
		tracker.open();
		
	}
	
	

	public void close() {
		tracker.close();
	}

	public void setParser(EXPathPackageParser parser) {
		this.mParser = parser;
	}

	public Iterator<Module> iterator() {
		// TODO cache the modules and synchronize with tracker
		return mModules.iterator();
	}

	public Module getModuleByComponent(URI uri) {
		return mComponentsMap.get(uri);
	}

	public Module resolveDependency(URI component, Module source) {
		// TODO check cache, otherwise delegate to resolver
		return null;
	}

	@Override
	public Iterable<URI> getComponents() {
		return mComponentsMap.keySet();
	}

	@Override
	public void addModule(Module module) {
		mModules.add(module);
		for(Component component: module.getComponents()){
			mComponentsMap.put(component.getURI(), module);
		}		
	}
}
