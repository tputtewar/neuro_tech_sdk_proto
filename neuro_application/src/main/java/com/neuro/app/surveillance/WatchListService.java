package com.neuro.app.surveillance;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.neuro.app.util.BasePanel;

public class WatchListService extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4328755910661879713L;
	BasePanel panel1 = null;
	BasePanel panel2 = null;

	public static void main(String args[]) throws Throwable {
		WatchListService service = new WatchListService();
		service.init("surveillance");
	}

	@SuppressWarnings("static-access")
	public WatchListService() throws Throwable {
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		panel1 = new OUTWatchList(50);
		panel2 = new INWatchList(50);
		this.add(panel1, BorderLayout.EAST);
		this.add(panel2, BorderLayout.WEST);
		this.setExtendedState(this.MAXIMIZED_BOTH);
		this.setSize(1000, 1000);

	}

	public void init(String componentName) {
		obtainLicenses(panel1, panel1.getName(), "surveillance");
		obtainLicenses(panel2, panel2.getName(), "surveillance1");
	}

	private void obtainLicenses(BasePanel panel, String watchListName, String componentName) {
		try {
			if (!panel.isObtained()) {

				boolean status = false;
				if (componentName != null) {
					if (componentName.equals("surveillance")) {
						status = SurveillanceTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
						SurveillanceTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
					} else if (componentName.equals("surveillance1")) {

						status = INSurveillanceTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
						INSurveillanceTools.getInstance().obtainLicenses(panel.getOptionalLicenses());

					}

					panel.getLicensing().setRequiredComponents(panel.getRequiredLicenses(), componentName);
					panel.getLicensing().setOptionalComponents(panel.getOptionalLicenses(), componentName);
				}
				panel.updateLicensing(status);
			}
		} catch (Exception e) {
			showError(null, e);
		}
	}

	private void showError(Component parentComponent, Throwable e) {
		e.printStackTrace();
		String message;
		if (e.getMessage() != null) {
			message = e.getMessage();
		} else if (e.getCause() != null) {
			message = e.getCause().getMessage();
		} else {
			message = "An error occurred. Please see log for more details.";
		}
		JOptionPane.showMessageDialog(parentComponent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
