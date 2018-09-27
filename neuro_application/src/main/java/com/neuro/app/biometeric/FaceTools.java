package com.neuro.app.biometeric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.licensing.NLicense;

public final class FaceTools {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String ADDRESS = "/local";
	private static final String PORT = "5000";
	private static FaceTools instance;

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static FaceTools getInstance() {
		synchronized (FaceTools.class) {
			if (instance == null) {
				instance = new FaceTools();
			}
			return instance;
		}
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private final NBiometricClient client;
	private final NBiometricClient defaultClient;
	private final Map<String, Boolean> obtainedLicenses;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private FaceTools() {
		obtainedLicenses = new HashMap<String, Boolean>();
		client = new NBiometricClient();
		defaultClient = new NBiometricClient();
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private boolean obtain(String address, String port, List<String> licenses) throws Exception {
		if (licenses == null) {
			throw new RuntimeException("Null license list");
		}

		boolean result = true;
		for (String license : licenses) {
			if (!isLicenseObtained(license)) {
				boolean state = NLicense.obtainComponents(address, port, license);
				obtainedLicenses.put(license, state);
				System.out.println(license + ": " + (state ? "obtainted" : "not obtained"));
			} else {
				System.out.println(license + ": " + " already obtained");
			}
		}
		return result;
	}

	private boolean isLicenseObtained(String license) {
		if (obtainedLicenses.containsKey(license)) {
			return obtainedLicenses.get(license);
		} else {
			return false;
		}
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void resetParameters() {
		client.reset();
	}

	public boolean obtainLicenses(List<String> licenses) throws Exception {
		return obtain(ADDRESS, PORT, licenses);
	}

	public NBiometricClient getClient() {
		return client;
	}

	public NBiometricClient getDefaultClient() {
		return defaultClient;
	}

	public Map<String, Boolean> getLicenses() {
		return obtainedLicenses;
	}

}
