package com.neuro.app;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class NeuroApplicationTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public NeuroApplicationTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(NeuroApplicationTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}
}
