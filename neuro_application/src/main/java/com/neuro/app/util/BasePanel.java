package com.neuro.app.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.neurotec.util.concurrent.AggregateExecutionException;

public abstract class BasePanel extends JPanel {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	// ===========================================================
	// Protected fields
	// ===========================================================

	protected LicensingPanel licensing;
	protected List<String> requiredLicenses = new ArrayList<String>();
	protected  List<String> optionalLicenses = new ArrayList<String>();
	protected boolean obtained;
	protected String componentName;

	// ===========================================================
	// Public methods
	// ===========================================================

	public void init(String component) {
		setComponentName(component);
		initGUI();
		setDefaultValues();
		updateControls();
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	
	public final List<String> getRequiredLicenses() {
		return requiredLicenses;
	}

	public final List<String> getOptionalLicenses() {
		return optionalLicenses;
	}

	public final LicensingPanel getLicensing() {
		return licensing;
	}

	public final void updateLicensing(boolean status) {
		licensing.setComponentObtainingStatus(status);
		obtained = status;
	}

	public boolean isObtained() {
		return obtained;
	}

	public void showError(Throwable e) {
		e.printStackTrace();
		if (e instanceof AggregateExecutionException) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("Execution resulted in one or more errors:\n");
			for (Throwable cause : ((AggregateExecutionException) e).getCauses()) {
				sb.append(cause.toString()).append('\n');
			}
			JOptionPane.showMessageDialog(this, sb.toString(), "Execution failed", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showError(String message) {
		if (message == null) throw new NullPointerException("message");
		JOptionPane.showMessageDialog(this, message);
	}

	// ===========================================================
	// Abstract methods
	// ===========================================================

	protected abstract void initGUI();
	protected abstract void setDefaultValues();
	protected abstract void updateControls();
	protected abstract void updateFacesTools();

	public abstract void onDestroy();
	public abstract void onClose();

	protected void initTab() {
		// TODO Auto-generated method stub
		
	}
}
