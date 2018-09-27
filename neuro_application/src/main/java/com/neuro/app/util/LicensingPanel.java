package com.neuro.app.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.neuro.app.biometeric.FaceTools;
import com.neuro.app.surveillance.INSurveillanceTools;
import com.neuro.app.surveillance.SurveillanceTools;

public final class LicensingPanel extends JPanel {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	private static final String REQUIRED_COMPONENT_LICENSES_LABEL_TEXT = "Required component licenses: ";
	private static final String COMPONENTS_OBTAINED_STATUS_TEXT = "Component licenses successfuly obtained";
	private static final String COMPONENTS_NOT_OBTAINED_STATUS_TEXT = "Component licenses not obtained";

	private static final Color COMPONENTS_OBTAINED_STATUS_TEXT_COLOR = Color.green.darker();
	private static final Color COMPONENTS_NOT_OBTAINED_STATUS_TEXT_COLOR = Color.red.darker();

	private static final int BORDER_WIDTH_TOP = 5;
	private static final int BORDER_WIDTH_LEFT = 5;
	private static final int BORDER_WIDTH_BOTTOM = 5;
	private static final int BORDER_WIDTH_RIGHT = 5;

	// ===========================================================
	// Private fields
	// ===========================================================

	private final List<String> requiredComponents;
	private final List<String> optionalComponents;

	private JLabel lblRequiredComponentLicenses;
	private JLabel lblRequiredComponentLicensesList;
	private JLabel lblStatus;

	// ===========================================================
	// Public constructors
	// ===========================================================

	public LicensingPanel(List<String> required, List<String> optional) {
		super(new BorderLayout(), true);
		init();
		if (required == null) {
			requiredComponents = new ArrayList<String>();
		} else {
			requiredComponents = new ArrayList<String>(required);
		}
		if (optional == null) {
			optionalComponents = new ArrayList<String>();
		} else {
			optionalComponents = new ArrayList<String>(optional);
		}
	}

	public LicensingPanel() {
		this(null, null);
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void init() {
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		{
			lblRequiredComponentLicenses = new JLabel(REQUIRED_COMPONENT_LICENSES_LABEL_TEXT);
			lblRequiredComponentLicenses
					.setFont(new Font(lblRequiredComponentLicenses.getFont().getName(), Font.BOLD, 11));
			lblRequiredComponentLicenses.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT,
					BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
			this.add(lblRequiredComponentLicenses, BorderLayout.LINE_START);
		}
		{
			lblRequiredComponentLicensesList = new JLabel();
			lblRequiredComponentLicensesList
					.setFont(new Font(lblRequiredComponentLicensesList.getFont().getName(), Font.PLAIN, 11));
			lblRequiredComponentLicensesList.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP,
					BORDER_WIDTH_LEFT, BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
			this.add(lblRequiredComponentLicensesList, BorderLayout.CENTER);
		}
		{
			lblStatus = new JLabel();
			lblStatus.setFont(new Font(lblStatus.getFont().getName(), Font.PLAIN, 11));
			lblStatus.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT,
					BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
			setComponentObtainingStatus(false);
			this.add(lblStatus, BorderLayout.PAGE_END);
		}
	}

	private String getRequiredComponentsString(String componentName) {
		StringBuilder result = new StringBuilder();
//		Map<String, Boolean> licenses = FaceTools.getInstance().getLicenses();
		Map<String, Boolean> licenses = new HashMap<String, Boolean>();

		if (componentName.equals("surveillance")) {
			licenses = SurveillanceTools.getInstance().getLicenses();
		} else if (componentName.equals("surveillance1")) {
			licenses = INSurveillanceTools.getInstance().getLicenses();
		} else if (componentName.equals("faces")) {
			licenses = FaceTools.getInstance().getLicenses();
		}
		for (String component : requiredComponents) {
			if (licenses.get(component)) {
				result.append("<font color=green>").append(component).append("</font>, ");
			} else {
				result.append("<font color=red>").append(component).append("</font>, ");
			}
		}
		if (result.length() > 0) {
			result.delete(result.length() - 2, result.length());
		}
		return result.toString();
	}

	private String getOptionalComponentsString(String componentName) {
		if (optionalComponents == null) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		Map<String, Boolean> licenses = new HashMap<String, Boolean>();

		if (componentName.equals("surveillance")) {
			licenses = SurveillanceTools.getInstance().getLicenses();
		} else if (componentName.equals("surveillance1")) {
			licenses = INSurveillanceTools.getInstance().getLicenses();
		} else if (componentName.equals("faces")) {
			licenses = FaceTools.getInstance().getLicenses();
		}
		for (String component : optionalComponents) {
			if (licenses.get(component)) {
				result.append("<font color=green>").append(component).append(" (optional)</font>, ");
			} else {
				result.append("<font color=red>").append(component).append(" (optional)</font>, ");
			}
			if (result.length() > 0) {
				result.delete(result.length() - 2, result.length());
			}
		}
		return result.toString();
	}

	private void updateList(String componentName) {
		StringBuilder result = new StringBuilder("<html>").append(getRequiredComponentsString(componentName));
		if (!optionalComponents.isEmpty()) {
			result.append(", ").append(getOptionalComponentsString(componentName));
		}
		result.append("</html");
		lblRequiredComponentLicensesList.setText(result.toString());
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void setRequiredComponents(List<String> components, String componentName) {
		requiredComponents.clear();
		requiredComponents.addAll(components);
		updateList(componentName);
	}

	public void setOptionalComponents(List<String> components, String componentName) {
		optionalComponents.clear();
		optionalComponents.addAll(components);
		updateList(componentName);
	}

	public void setComponentObtainingStatus(boolean succeeded) {
		if (succeeded) {
			lblStatus.setText(COMPONENTS_OBTAINED_STATUS_TEXT);
			lblStatus.setForeground(COMPONENTS_OBTAINED_STATUS_TEXT_COLOR);
		} else {
			lblStatus.setText(COMPONENTS_NOT_OBTAINED_STATUS_TEXT);
			lblStatus.setForeground(COMPONENTS_NOT_OBTAINED_STATUS_TEXT_COLOR);
		}
	}

}
