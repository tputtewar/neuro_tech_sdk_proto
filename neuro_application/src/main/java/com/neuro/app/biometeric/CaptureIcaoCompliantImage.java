package com.neuro.app.biometeric;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.neuro.app.util.BasePanel;
import com.neuro.app.util.LicensingPanel;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.io.NFile;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

public final class CaptureIcaoCompliantImage extends BasePanel implements ActionListener {

	// ===========================================================
	// Nested classes
	// ===========================================================

	private static class CameraSelectionListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			FaceTools.getInstance().getClient().setFaceCaptureDevice((NCamera) e.getItem());
		}

	}

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	private static final String PANEL_TITLE = "Capture ICAO image";
	private static final String REFRESH_LIST_BUTTON_TEXT = "Refresh list";
	private static final String START_CAPTIRIN_BUTTON_TEXT = "Start capturing";
	private static final String STOP_CAPTURING_BUTTON_TEXT = "Stop capturing";
	private static final String FORCE_BUTTON_TEXT = "Force";
	private static final String SAVE_TEMPLATE_BUTTON_TEXT = "Save template";
	private static final String SAVE_IMAGE_BUTTON_TEXT = "Save image";

	// ===========================================================
	// Private fields
	// ===========================================================

	private final JFileChooser fc;
	private NFaceView faceView;
	private IcaoWarningsPanel icaoWarningsView;
	private NViewZoomSlider zoomSlider;
	private JPanel panelToolbar;
	private JPanel panelControls;
	private JPanel panelCameras;
	private JPanel panelCameraControls;
	private JPanel panelStatusBar;
	private JPanel panelSouthPanel;
	private JPanel panelSave;
	private JLabel lblStatus;
	private JButton btnRefreshList;
	private JButton btnStartCapturing;
	private JButton btnStopCapturing;
	private JButton btnForce;
	private JButton btnSaveTemplate;
	private JButton btnSaveImage;
	@SuppressWarnings("rawtypes")
	private JComboBox comboBoxCameras;
	private JScrollPane scrollPane;

	private NSubject subject;
	private boolean capturing;
	private final NDeviceManager deviceManager;

	private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();

	// ===========================================================
	// Public constructor
	// ===========================================================

	public CaptureIcaoCompliantImage() {
		super();
		this.setName(PANEL_TITLE);

		requiredLicenses.add("Biometrics.FaceExtraction");
		requiredLicenses.add("Biometrics.FaceSegmentsDetection");
		requiredLicenses.add("Devices.Cameras");

		FaceTools.getInstance().getClient().setUseDeviceManager(true);
		deviceManager = FaceTools.getInstance().getClient().getDeviceManager();
		deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
		deviceManager.initialize();
		fc = new JFileChooser();

	}

	// ===========================================================
	// Private methods
	// ===========================================================

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateCamerasList() {
		DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxCameras.getModel();
		model.setSelectedItem(null);
		model.removeAllElements();
		for (NDevice device : deviceManager.getDevices()) {
			model.addElement(device);
		}
		NCamera camera = FaceTools.getInstance().getClient().getFaceCaptureDevice();
		if ((camera == null) && (model.getSize() > 0)) {
			comboBoxCameras.setSelectedIndex(0);
		} else if (camera != null) {
			model.setSelectedItem(camera);
		}
	}

	private void startCapturing() {
		lblStatus.setText("");
		updateFacesTools();
		if (FaceTools.getInstance().getClient().getFaceCaptureDevice() == null) {
			JOptionPane.showMessageDialog(this, "Please select camera from the list.", "No camera selected",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		// Set face capture from stream.
		NFace face = new NFace();
		face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.STREAM));
		subject = new NSubject();
		subject.getFaces().add(face);
		faceView.setFace(face);
		icaoWarningsView.setFace(face);

		// Begin capturing.
		capturing = true;
		NBiometricTask task = FaceTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.CAPTURE,
				NBiometricOperation.SEGMENT, NBiometricOperation.CREATE_TEMPLATE), subject);
		FaceTools.getInstance().getClient().performTask(task, null, captureCompletionHandler);
		updateControls();
	}

	private void stopCapturing() {
		FaceTools.getInstance().getClient().cancel();
	}

	private void force() {
		FaceTools.getInstance().getClient().force();
	}

	private void saveTemplate() throws IOException {
		if (subject != null) {
			fc.setSelectedFile(new File("subject"));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				NFile.writeAllBytes(fc.getSelectedFile().getAbsolutePath(), subject.getTemplateBuffer());
			}
		}
	}

	private void saveImage() throws IOException {
		if (subject != null) {
			fc.setSelectedFile(new File("image.png"));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				subject.getFaces().get(1).getImage().save(fc.getSelectedFile().getAbsolutePath());
			}
		}
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@SuppressWarnings("rawtypes")
	@Override
	protected void initGUI() {
		setLayout(new BorderLayout());
		{
			panelToolbar = new JPanel();
			panelToolbar.setLayout(new BoxLayout(panelToolbar, BoxLayout.Y_AXIS));
			add(panelToolbar, BorderLayout.PAGE_START);
			{
				licensing = new LicensingPanel(requiredLicenses, Collections.<String>emptyList());
				panelToolbar.add(licensing);
			}
			{
				panelControls = new JPanel();
				panelControls.setLayout(new FlowLayout(FlowLayout.LEFT));
				panelControls.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				panelControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				panelToolbar.add(panelControls);
				{
					panelCameras = new JPanel();
					panelCameras.setLayout(new BorderLayout());
					panelCameras.setPreferredSize(new Dimension(560, 75));
					panelCameras.setBorder(BorderFactory.createTitledBorder("Cameras"));
					panelControls.add(panelCameras);
					{
						comboBoxCameras = new JComboBox();
						comboBoxCameras.addItemListener(new CameraSelectionListener());
						panelCameras.add(comboBoxCameras, BorderLayout.PAGE_START);
					}
					{
						panelCameraControls = new JPanel(new FlowLayout(FlowLayout.LEADING));
						panelCameras.add(panelCameraControls, BorderLayout.CENTER);
						{
							btnRefreshList = new JButton(REFRESH_LIST_BUTTON_TEXT);
							btnRefreshList.addActionListener(this);
							panelCameraControls.add(btnRefreshList);
						}
						{
							btnStartCapturing = new JButton(START_CAPTIRIN_BUTTON_TEXT);
							btnStartCapturing.addActionListener(this);
							panelCameraControls.add(btnStartCapturing);
						}
						{
							btnStopCapturing = new JButton(STOP_CAPTURING_BUTTON_TEXT);
							btnStopCapturing.addActionListener(this);
							panelCameraControls.add(btnStopCapturing);
						}
					}
				}
			}
		}
		{
			JPanel panelCenter = new JPanel(new BorderLayout());
			icaoWarningsView = new IcaoWarningsPanel();
			panelCenter.add(icaoWarningsView, BorderLayout.WEST);
			faceView = new NFaceView();
			faceView.setAutofit(true);
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(faceView);
			panelCenter.add(scrollPane, BorderLayout.CENTER);
			add(panelCenter, BorderLayout.CENTER);
		}
		{
			panelSouthPanel = new JPanel();
			panelSouthPanel.setLayout(new BorderLayout());
			add(panelSouthPanel, BorderLayout.PAGE_END);
			{
				panelStatusBar = new JPanel();
				panelStatusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
				panelSouthPanel.add(panelStatusBar, BorderLayout.WEST);
				{
					btnForce = new JButton(FORCE_BUTTON_TEXT);
					btnForce.addActionListener(this);
					panelStatusBar.add(btnForce);
				}
				{
					lblStatus = new JLabel();
					lblStatus.setPreferredSize(new Dimension(180, 20));
					panelStatusBar.add(lblStatus);
				}
			}
			{
				panelSave = new JPanel();
				panelSave.setLayout(new FlowLayout(FlowLayout.RIGHT));
				panelSouthPanel.add(panelSave, BorderLayout.EAST);
				{
					zoomSlider = new NViewZoomSlider();
					zoomSlider.setView(faceView);
					panelSave.add(zoomSlider);
				}
				{
					btnSaveTemplate = new JButton(SAVE_TEMPLATE_BUTTON_TEXT);
					btnSaveTemplate.addActionListener(this);
					panelSave.add(btnSaveTemplate);
				}
				{
					btnSaveImage = new JButton(SAVE_IMAGE_BUTTON_TEXT);
					btnSaveImage.addActionListener(this);
					panelSave.add(btnSaveImage);
				}
			}
		}
		updateCamerasList();
	}

	@Override
	protected void setDefaultValues() {
		// No default values.
	}

	@Override
	protected void updateControls() {
		comboBoxCameras.setEnabled(!capturing);
		btnRefreshList.setEnabled(!capturing);
		btnStartCapturing.setEnabled(!capturing);
		btnStopCapturing.setEnabled(capturing);
		btnForce.setEnabled(capturing);

		btnSaveTemplate.setEnabled(!capturing && (subject != null) && (subject.getStatus() == NBiometricStatus.OK));
		btnSaveImage.setEnabled(!capturing && (subject != null) && (subject.getStatus() == NBiometricStatus.OK));
	}

	@Override
	protected void updateFacesTools() {
		NBiometricClient client = FaceTools.getInstance().getClient();
		client.setUseDeviceManager(true);
		client.setFacesCheckIcaoCompliance(true);
	}

	// ===========================================================
	// Package private methods
	// ===========================================================

	void updateStatus(String status) {
		lblStatus.setText(status);
	}

	NSubject getSubject() {
		return subject;
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnRefreshList)) {
				updateCamerasList();
			} else if (ev.getSource().equals(btnStartCapturing)) {
				startCapturing();
			} else if (ev.getSource().equals(btnForce)) {
				force();
			} else if (ev.getSource().equals(btnStopCapturing)) {
				stopCapturing();
			} else if (ev.getSource().equals(btnSaveTemplate)) {
				saveTemplate();
			} else if (ev.getSource().equals(btnSaveImage)) {
				saveImage();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onDestroy() {
		deviceManager.dispose();
	}

	@Override
	public void onClose() {
		stopCapturing();
	}

	// ===========================================================
	// Inner classes
	// ===========================================================

	private class CaptureCompletionHandler implements CompletionHandler<NBiometricTask, Object> {

		public void completed(final NBiometricTask task, final Object attachment) {
			if (task.getError() != null) {
				failed(task.getError(), attachment);
				return;
			}
			final NBiometricStatus status = task.getStatus();
			if (status == NBiometricStatus.OK) {
				faceView.setFace(subject.getFaces().get(1));
				icaoWarningsView.setFace(subject.getFaces().get(1));
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					capturing = false;
					updateStatus(status.toString());
					updateControls();
				}

			});
		}

		public void failed(final Throwable th, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					capturing = false;
					showError(th);
					updateControls();
				}

			});
		}

	}

}
