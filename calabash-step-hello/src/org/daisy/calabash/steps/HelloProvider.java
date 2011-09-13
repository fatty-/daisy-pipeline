package org.daisy.calabash.steps;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.calabash.XProcStepProvider;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

public class HelloProvider implements XProcStepProvider {

	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new Hello(runtime, step);
	}

	private class Hello extends DefaultStep {

		private ReadablePipe source = null;
		private WritablePipe result = null;

		/**
		 * Creates a new instance of Identity
		 */
		public Hello(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		public void setInput(String port, ReadablePipe pipe) {
			source = pipe;
		}

		public void setOutput(String port, WritablePipe pipe) {
			result = pipe;
		}

		public void reset() {
			source.resetReader();
			result.resetWriter();
		}

		public void run() throws SaxonApiException {
			super.run();

			System.err.println("Hello!");

			while (source.moreDocuments()) {
				XdmNode doc = source.read();
				runtime.finest(
						this,
						step.getNode(),
						"Message step " + step.getName() + " read "
								+ doc.getDocumentURI());
				result.write(doc);
			}
		}
	}
}
