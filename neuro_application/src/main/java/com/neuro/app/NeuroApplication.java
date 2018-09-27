package com.neuro.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.neuro.app.biometeric.CaptureIcaoCompliantImage;
import com.neuro.app.biometeric.FaceTools;
import com.neuro.app.dao.DBConnection;
import com.neuro.app.dao.DBService;
import com.neuro.app.helper.LibraryManager;
import com.neuro.app.surveillance.SurveillanceTools;
import com.neuro.app.surveillance.WatchListService;
import com.neuro.app.util.BasePanel;
import com.neurotec.lang.NCore;
import com.neurotec.samples.util.Utils;

public final class NeuroApplication implements ChangeListener {

	// ===========================================================
	// Static constructor
	// ===========================================================

	static {
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			Logger.getLogger(Logger.getLogger("global").getName()).log(Level.FINE, e.getMessage(), e);
		}
	}

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static void main(String[] args) throws Exception {
		new NeuroApplication();
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private JTabbedPane tabbedPane;
	private WatchListService panelWatchListService;
	private BasePanel panelcaptureIcaoCompliantImage;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private NeuroApplication() throws Exception {

		LibraryManager.initLibraryPath();

		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			public void run() {
				JFrame frame = new JFrame();
				try {
					Dimension d = new Dimension(800, 600);

					frame.setSize(d);
					frame.setMinimumSize(d);
					frame.setPreferredSize(d);
					frame.setExtendedState(frame.MAXIMIZED_BOTH);
					frame.setResizable(true);
					frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
					frame.setTitle("Neuro Demo Application For Face Recognition");
					frame.setIconImage(Utils.createIconImage("images/logo.png"));

					tabbedPane = new JTabbedPane();
					frame.add(tabbedPane);

					addTabs(tabbedPane);

					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							dispose();
						}
					});
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
					
				} catch (Throwable e) {
					showError(null, e);
				}
			}
		});
		
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void addTabs(JTabbedPane tabbedPane) throws Throwable {
		tabbedPane.addChangeListener(this);

		panelcaptureIcaoCompliantImage = new CaptureIcaoCompliantImage();
		panelcaptureIcaoCompliantImage.init("faces");
		tabbedPane.addTab(panelcaptureIcaoCompliantImage.getName(), panelcaptureIcaoCompliantImage);

		panelWatchListService = new WatchListService();
		tabbedPane.addTab("Watchlist", panelWatchListService.getContentPane());

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

	private void dispose() {
		for (Component component : tabbedPane.getComponents()) {
			if (component instanceof BasePanel) {
				((BasePanel) component).onDestroy();
			}
		}
		NCore.shutdown();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void stateChanged(ChangeEvent ev) {
		if (ev.getSource() == tabbedPane) {
			try {
				switch (tabbedPane.getSelectedIndex()) {
				case 0:
					obtainLicenses(panelcaptureIcaoCompliantImage);
					FaceTools.getInstance().resetParameters();
					break;
				case 1:
					panelWatchListService.init("surveillance");
					break;
				default:
					throw new IndexOutOfBoundsException("unreachable");
				}
			} catch (Exception e) {
				showError(null, e);
			}
		}
	}

	public void obtainLicenses(BasePanel panel) {
		try {
			if (!panel.isObtained()) {

				boolean status = false;
				String componentName = panel.getComponentName();
				if (componentName != null) {
					if (componentName.equals("surveillance")) {
						status = SurveillanceTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
						SurveillanceTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
					} else if (componentName.equals("faces")) {
						status = FaceTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
						FaceTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
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

}
