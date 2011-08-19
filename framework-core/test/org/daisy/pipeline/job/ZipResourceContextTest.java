package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.transform.sax.SAXSource;

import org.daisy.common.base.Provider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZipResourceContextTest {
	ZipFile mFile;
	private ZipResourceContext ctxt;
	private Provider<InputStream> res;

	@Before
	public void setUp() throws ZipException, IOException, URISyntaxException {
		mFile = new ZipFile(new File(this.getClass().getClassLoader()
				.getResource("test.zip").toURI()));
		ctxt = new ZipResourceContext(mFile);
		res = ctxt.getResource("1.txt");
	}

	@Test
	public void testEntries() {

		HashMap<String, Boolean> res = new HashMap<String, Boolean>();
		res.put("1.txt", false);
		res.put("2.txt", false);
		for (String strRes : ctxt.getNames()) {
			res.put(strRes, true);
		}
		Assert.assertEquals(2, res.values().size());
		for (String strRes : res.keySet()) {
			Assert.assertEquals(strRes + " is not in the zip file", true, res
					.get(strRes));
		}
	}

	@Test
	public void resourceAsInputStream() throws IOException {

		InputStream is = res.provide();
		byte buff[] = new byte[256];
		int read = is.read(buff);
		Assert.assertTrue(read > 0);
	}
/*
	@Test
	public void resourceAsSource() throws IOException {
		InputStream is = ((SAXSource) res.asSource()).getInputSource()
				.getByteStream();
		byte buff[] = new byte[256];
		int read = is.read(buff);
		Assert.assertTrue(read > 0);
	}
	*/
/*
	@Test
	public void resourceAsFile() throws IOException {
		File file = res.asFile();
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.canRead());
		Assert.assertTrue(file.length() != 0);
	}

	@Test
	public void resourceAsURI() throws IOException {
		URI uri = res.asURI();
		File file = new File(uri);
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.canRead());
		Assert.assertTrue(file.length() != 0);
	}
	*/
}
