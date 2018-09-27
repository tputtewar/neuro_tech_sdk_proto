package com.neuro.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

public final class Settings {

	// ==============================================
	// Private static fields
	// ==============================================

	private static Settings defaultInstance;
	private static final String HOME_PATH = System.getProperty("user.home");
	private static final String APP_DATA_PATH = HOME_PATH + System.getProperty("file.separator") + "AppData";
	private static final String APP_DATA_LOCAL_PATH = APP_DATA_PATH + System.getProperty("file.separator") + "Local";
	private static final String NEURO_DIRECTORY_PATH = APP_DATA_LOCAL_PATH + System.getProperty("file.separator") + "Neurotechnology";

	// ==============================================
	// Private fields
	// ==============================================

	private final String propertiesPath;

	// ==============================================
	// Public static methods
	// ==============================================

	public static Settings getDefault(String sampleName) {
		synchronized (Settings.class) {
			if (defaultInstance == null) {
				defaultInstance = new Settings(sampleName);
			}
			return defaultInstance;
		}

	}

	// ==============================================
	// Private fields
	// ==============================================

	private final Properties properties = new Properties();

	// ==============================================
	// Private constructor
	// ==============================================

	private Settings(String sampleName) {
		String sampleDirectoryPath = NEURO_DIRECTORY_PATH + System.getProperty("file.separator") + sampleName;
		propertiesPath = sampleDirectoryPath + System.getProperty("file.separator") + "user.properties";

		File sampleDirectory = new File(sampleDirectoryPath);
		if (!sampleDirectory.exists() || !sampleDirectory.isDirectory()) {
			sampleDirectory.mkdirs();
		}
		File propertiesFile = new File(propertiesPath);
		if (propertiesFile.exists()) {
			InputStream is = null;
			try {
				is = new FileInputStream(propertiesFile);
				properties.load(is);
			} catch (IOException e) {
				e.printStackTrace();
				loadDefaultSettings();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// ==============================================
	// Private methods
	// ==============================================

	private void loadDefaultSettings() {
		properties.put("lastDirectory", "");
	}

	// ==============================================
	// Public methods
	// ==============================================

	public String getLastDirectory() {
		return properties.getProperty("lastDirectory");
	}

	public void setLastDirectory(String value) {
		properties.setProperty("lastDirectory", value);
	}

	public void save() {
		File propertiesFile = new File(propertiesPath);
		OutputStream os = null;
		try {
			os = new FileOutputStream(propertiesFile);
			properties.store(os, null);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString());
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
