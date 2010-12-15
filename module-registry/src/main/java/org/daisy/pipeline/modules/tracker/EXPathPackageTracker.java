package org.daisy.pipeline.modules.tracker;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.daisy.expath.parser.EXPathPackageParser;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class EXPathPackageTracker extends BundleTracker {

	private Map<String, Bundle> bundles = new HashMap<String, Bundle>();
	ModuleRegistry mRegistry;
	public EXPathPackageTracker(BundleContext context,ModuleRegistry reg) {
		super(context, Bundle.ACTIVE, null);
		mRegistry=reg;
	}
	private EXPathPackageParser mParser;
	
	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		Bundle result = null;
		URL url = bundle.getResource("expath-pkg.xml");
		if (url != null) {
		//	System.out.println("tracking: " + bundle.getSymbolicName());
			Module module = mParser.parse(url);
			mRegistry.addModule(module);
			result = bundle;
		}

		// Finally
		return result;
	}
	public void setParser(EXPathPackageParser parser) {
		this.mParser = parser;
	}
	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		super.removedBundle(bundle, event, object);
		System.out.println("removing: " + bundle.getSymbolicName() + "["
				+ event + "]");
		bundles.remove(bundle.getSymbolicName());
	}

}
