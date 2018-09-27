package com.neuro.app.helper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.neurotec.licensing.NLicense;

public final class LicenseManager {

	// ===========================================================
	// Private static final fields
	// ===========================================================

	private static final String ADDRESS = "/local";
	private static final String PORT = "5000";

	// ===========================================================
	// Public static final fields
	// ===========================================================

	public static final String PROGRESS_CHANGED_PROPERTY = "progress";
	public static final String LAST_STATUS_MESSAGE_PROPERTY = "last-status-message";

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static LicenseManager instance;

	// ===========================================================
	// Private fields
	// ===========================================================

	private final PropertyChangeSupport propertyChangeSupport;
	private final Set<String> obtainedLicenses;
	private final Map<String, Boolean> licenseCache;
	private int progress;
	private String lastStatusMessage;
	private boolean debug = true;

	// ===========================================================
	// Private constructors
	// ===========================================================

	private LicenseManager() {
		propertyChangeSupport = new PropertyChangeSupport(this);
		obtainedLicenses = new HashSet<String>();
		licenseCache = new HashMap<String, Boolean>();
		lastStatusMessage = "";
	}

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static LicenseManager getInstance() {
		synchronized (LicenseManager.class) {
			if (instance == null) {
				instance = new LicenseManager();
			}
			return instance;
		}
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void setProgress(int progress) {
		int oldProgress = getProgress();
		this.progress = progress;
		propertyChangeSupport.firePropertyChange(PROGRESS_CHANGED_PROPERTY, oldProgress, progress);
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public boolean isActivated(String component) {
		return isActivated(component, false);
	}

	public boolean isActivated(String component, boolean cache) {
		if (component == null) {
			throw new NullPointerException("component");
		}
		if (cache) {
			if (licenseCache.containsKey(component)) {
				return licenseCache.get(component);
			}
		}
		boolean result;
		try {
			result = NLicense.isComponentActivated(component);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (cache) {
			licenseCache.put(component, result);
		}
		return result;
	}

	public synchronized boolean obtain(Collection<String> licenses) throws IOException {
		return obtain(licenses, ADDRESS, PORT);
	}

	public synchronized boolean obtain(Collection<String> licenses, String address, String port) throws IOException {
		String oldStatus = lastStatusMessage;
		lastStatusMessage = String.format("Obtaining licenses from server %s:%s\n", address, port);
		propertyChangeSupport.firePropertyChange(LAST_STATUS_MESSAGE_PROPERTY, oldStatus, lastStatusMessage);
		if (debug) {
			System.out.print(lastStatusMessage);
		}
		int i = 0;
		setProgress(i);
		boolean result = false;
		try {
			for (String license : licenses) {
				boolean obtained = false;
				try {
					obtained = NLicense.obtainComponents(address, port, license);
					if (obtained) {
						obtainedLicenses.add(license);
					}
					result |= obtained;
				} finally {
					oldStatus = lastStatusMessage;
					lastStatusMessage = license + ": " + (obtained ? "obtained" : "not obtained") + "\n";
					propertyChangeSupport.firePropertyChange(LAST_STATUS_MESSAGE_PROPERTY, oldStatus, lastStatusMessage);
					if (debug) {
						System.out.print(lastStatusMessage);
					}
				}
				setProgress(++i);
			}
		} finally {
			setProgress(100);
		}
		return result;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isProgress() {
		return (progress != 0) && (progress != 100);
	}

	public int getProgress() {
		return progress;
	}

	public int getLicenseCount() {
		return obtainedLicenses.size();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

}
