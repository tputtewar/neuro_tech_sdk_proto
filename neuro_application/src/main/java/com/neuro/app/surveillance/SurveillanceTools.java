package com.neuro.app.surveillance;

import com.neurotec.beans.NParameterBag;
import com.neurotec.beans.NParameterDescriptor;
import com.neurotec.biometrics.NBiometricEngine;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.licensing.NLicense;
import com.neurotec.plugins.NPlugin;
import com.neurotec.plugins.NPluginState;
import com.neurotec.surveillance.NSurveillance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SurveillanceTools {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static SurveillanceTools instance;

	private static final String ADDRESS = "/local";
	private static final String PORT = "5000";

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static SurveillanceTools getInstance() {
		synchronized (SurveillanceTools.class) {
			if (instance == null) {
				instance = new SurveillanceTools();
			}
			return instance;
		}
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private final Map<String, Boolean> licenses;
	private final NSurveillance surveillance;
	private final NSurveillance defaultSurveillance;
	private final NBiometricEngine engine;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private SurveillanceTools() {
		licenses = new HashMap<String, Boolean>();
		surveillance = new NSurveillance();
		defaultSurveillance = new NSurveillance();
		engine = new NBiometricEngine();
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private boolean isLicenseObtained(String license) {
		if (license == null) {
			throw new NullPointerException("license");
		}
		if (licenses.containsKey(license)) {
			return licenses.get(license);
		} else {
			return false;
		}
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public boolean obtainLicenses(List<String> names) throws IOException {
		if (names == null) {
			return true;
		}
		boolean result = true;
		for (String license : names) {
			if (isLicenseObtained(license)) {
				System.out.println(license + ": " + " already obtained");
			} else {
				boolean state = NLicense.obtainComponents(ADDRESS, PORT, license);
				licenses.put(license, state);
				if (state) {
					System.out.println(license + ": obtainted");
				} else {
					result = false;
					System.out.println(license + ": not obtained");
				}
			}
		}
		return result;
	}

	public NDevice connectDevice(String url, boolean remote) throws IOException {
		NDeviceManager deviceManager = surveillance.getDeviceManager();
		NPlugin plugin = NDeviceManager.getPluginManager().getPlugins().get("Media");
		if (plugin.getState() == NPluginState.PLUGGED && NDeviceManager.isConnectToDeviceSupported(plugin)) {
			NParameterDescriptor[] parameters = NDeviceManager.getConnectToDeviceParameters(plugin);
			NParameterBag bag = new NParameterBag(parameters);
			if (remote) {
				bag.setProperty("DisplayName", "IP Camera");
				bag.setProperty("Url", url);
			} else {
				bag.setProperty("DisplayName", "Video file");
				bag.setProperty("FileName", url);
			}
			return deviceManager.connectToDevice(plugin, bag.toPropertyBag());
		}
		throw new IOException("Failed to connect specified device!");
	}

	public Map<String, Boolean> getLicenses() {
		return licenses;
	}

	public NSurveillance getSurveillance() {
		return surveillance;
	}

	public NSurveillance getDefaultSurveillance() {
		return defaultSurveillance;
	}

	public NBiometricEngine getEngine() {
		return engine;
	}

}
