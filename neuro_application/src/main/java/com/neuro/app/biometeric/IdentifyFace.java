package com.neuro.app.biometeric;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.neuro.app.util.BasePanel;
import com.neuro.app.util.LicensingPanel;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NSubject.FaceCollection;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.samples.swing.ImageThumbnailFileChooser;
import com.neurotec.samples.util.Utils;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

public final class IdentifyFace extends BasePanel implements ActionListener {

	// ===========================================================
	// Private static fields
	// ===========================================================
	private static final long serialVersionUID = 1L;

	private static final String PANEL_TITLE = "Identify faces";
	private static final String TEMPLATES_LOADING_PANEL_BORDER_TEXT = "Templates loading";
	private static final String LOAD_TEMPLATE_BUTTON_TEXT = "Load template";
	private static final String LOADED_TEMPLATES_LABEL_COUNT_LABEL_TEXT = "Loaded templates: ";
	private static final String LOADED_TEMPLATES_COUNT_TEXT = "Templates count";
	private static final String IMAGE_FOR_IDENTIFICATION_PANEL_BORDER_TEXT = "Image / template for identification";
	private static final String OPEN_IMAGE_BUTTON_TEXT = "Open";
	private static final String IDENTIFICATION_PANEL_BORDER_TEXT = "Identification";
	private static final String IDENTIFY_BUTTON_TEXT = "Identify";
	private static final String MATCHING_FAR_LABEL_TEXT = "Matching FAR: ";
	private static final String RESET_DEFAULTS_BUTTON_TEXT = "Default";

	private static final List<String> THRESHOLDS = new ArrayList<String>();

	static {
		THRESHOLDS.add("1%");
		THRESHOLDS.add("0.1%");
		THRESHOLDS.add("0.01%");
		THRESHOLDS.add("0.001%");
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private final ImageThumbnailFileChooser fc;

	private NSubject subject;
	private final List<NSubject> subjects;

	private final EnrollHandler enrollHandler = new EnrollHandler();
	private final IdentificationHandler identificationHandler = new IdentificationHandler();
	private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();

	private JPanel panelToolBar;
	private JPanel panelControls;
	private JPanel panelImageForIdentification;
	private JPanel panelImageLoadingControls;
	private JPanel panelIdentification;
	private JPanel panelIdentificationControls;
	private JLabel lblMatchingFar;
	private JLabel lblLoadedTemplatesCount;
	private JLabel lblLoadedTemplatesCount2;
	private JLabel lblProbePath;
	private JButton btnLoadTemplates;
	private JButton btnOpenProbe;
	private JButton btnIdentify;
	private JButton btnResetDefaults;
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneResults;
	private JTable resultsTable;
	private JComboBox comboBoxMatchingFarThreshold;
	private NFaceView view;
	private NViewZoomSlider zoomSlider;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public IdentifyFace() {
		super();
		subjects = new ArrayList<NSubject>();
		fc = new ImageThumbnailFileChooser();
		fc.setIcon(Utils.createIconImage("images/Logo16x16.png"));
		this.setName(PANEL_TITLE);

		requiredLicenses.add("Biometrics.FaceExtraction");
		requiredLicenses.add("Biometrics.FaceMatching");
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void openTemplates() throws IOException {
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			((DefaultTableModel) resultsTable.getModel()).setRowCount(0);
			subjects.clear();

			// Create subjects from selected templates.
			for (File file : fc.getSelectedFiles()) {
				NSubject s = NSubject.fromFile(file.getAbsolutePath());
				s.setId(file.getName());
				subjects.add(s);
			}
			lblLoadedTemplatesCount2.setText(String.valueOf(subjects.size()));
		}
		updateControls();
	}

	private void openProbe() throws IOException {
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			((DefaultTableModel) resultsTable.getModel()).setRowCount(0);
			lblProbePath.setText("");
			subject = null;
			NFace face = null;
			try {
				subject = NSubject.fromFile(fc.getSelectedFile().getAbsolutePath());
				subject.setId(fc.getSelectedFile().getName());
				FaceCollection faces = subject.getFaces();
				if (faces.isEmpty()) {
					subject = null;
					throw new IllegalArgumentException("Template contains no face records.");
				}
				face = faces.get(0);
				templateCreationHandler.completed(NBiometricStatus.OK, null);
			} catch (UnsupportedOperationException e) {
				// Ignore. UnsupportedOperationException means file is not a valid template.
			}

			// If file is not a template, try to load it as an image.
			if (subject == null) {
				face = new NFace();
				face.setFileName(fc.getSelectedFile().getAbsolutePath());
				subject = new NSubject();
				subject.setId(fc.getSelectedFile().getName());
				subject.getFaces().add(face);
				updateFacesTools();
				FaceTools.getInstance().getClient().createTemplate(subject, null, templateCreationHandler);
			}

			view.setFace(face);
			lblProbePath.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	private void identify() {
		if ((subject != null) && !subjects.isEmpty()) {
			((DefaultTableModel) resultsTable.getModel()).setRowCount(0);
			updateFacesTools();

			// Clean earlier data before proceeding, enroll new data
			FaceTools.getInstance().getClient().clear();

			// Create enrollment task.
			NBiometricTask enrollmentTask = new NBiometricTask(EnumSet.of(NBiometricOperation.ENROLL));

			// Add subjects to be enrolled.
			for (NSubject s : subjects) {
				enrollmentTask.getSubjects().add(s);
			}

			// Enroll subjects.
			FaceTools.getInstance().getClient().performTask(enrollmentTask, null, enrollHandler);
		}
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@Override
	protected void initGUI() {
		setLayout(new BorderLayout());
		{
			panelToolBar = new JPanel();
			panelToolBar.setLayout(new BoxLayout(panelToolBar, BoxLayout.Y_AXIS));
			add(panelToolBar, BorderLayout.PAGE_START);
			{
				licensing = new LicensingPanel(requiredLicenses, Collections.<String>emptyList());
				panelToolBar.add(licensing);
			}
			{
				panelControls = new JPanel();
				panelControls.setLayout(new FlowLayout(FlowLayout.LEFT));
				panelControls.setBorder(BorderFactory.createTitledBorder(TEMPLATES_LOADING_PANEL_BORDER_TEXT));
				panelControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				panelToolBar.add(panelControls);
				{
					btnLoadTemplates = new JButton(LOAD_TEMPLATE_BUTTON_TEXT);
					btnLoadTemplates.addActionListener(this);
					panelControls.add(btnLoadTemplates);
				}
				{
					lblLoadedTemplatesCount = new JLabel(LOADED_TEMPLATES_LABEL_COUNT_LABEL_TEXT);
					panelControls.add(lblLoadedTemplatesCount);
				}
				{
					lblLoadedTemplatesCount2 = new JLabel(LOADED_TEMPLATES_COUNT_TEXT);
					panelControls.add(lblLoadedTemplatesCount2);
				}
			}
			{
				panelImageForIdentification = new JPanel();
				panelImageForIdentification.setLayout(new BorderLayout());
				panelImageForIdentification
						.setBorder(BorderFactory.createTitledBorder(IMAGE_FOR_IDENTIFICATION_PANEL_BORDER_TEXT));
				add(panelImageForIdentification, BorderLayout.CENTER);
				{
					panelImageLoadingControls = new JPanel();
					panelImageLoadingControls.setLayout(new FlowLayout(FlowLayout.LEFT));
					panelImageForIdentification.add(panelImageLoadingControls, BorderLayout.PAGE_START);
					{
						btnOpenProbe = new JButton(OPEN_IMAGE_BUTTON_TEXT);
						btnOpenProbe.addActionListener(this);
						panelImageLoadingControls.add(btnOpenProbe, BorderLayout.PAGE_START);
					}
					{
						lblProbePath = new JLabel();
						panelImageLoadingControls.add(lblProbePath);
					}
					{
						view = new NFaceView();
						view.setAutofit(true);
						scrollPane = new JScrollPane();
						panelImageForIdentification.add(scrollPane, BorderLayout.CENTER);
						scrollPane.setViewportView(view);
					}
				}
			}
			{
				panelIdentification = new JPanel();
				panelIdentification.setLayout(new BorderLayout());
				panelIdentification.setBorder(BorderFactory.createTitledBorder(IDENTIFICATION_PANEL_BORDER_TEXT));
				add(panelIdentification, BorderLayout.PAGE_END);
				{
					panelIdentificationControls = new JPanel();
					panelIdentificationControls.setLayout(new FlowLayout(FlowLayout.LEFT));
					panelIdentification.add(panelIdentificationControls, BorderLayout.PAGE_START);
					{
						btnIdentify = new JButton(IDENTIFY_BUTTON_TEXT);
						btnIdentify.addActionListener(this);
						btnIdentify.setEnabled(false);
						panelIdentificationControls.add(btnIdentify);
					}
					{
						lblMatchingFar = new JLabel(MATCHING_FAR_LABEL_TEXT);
						panelIdentificationControls.add(lblMatchingFar);
					}
					{
						DefaultComboBoxModel model = new DefaultComboBoxModel(THRESHOLDS.toArray());
						comboBoxMatchingFarThreshold = new JComboBox(model);
						panelIdentificationControls.add(comboBoxMatchingFarThreshold);
					}
					{
						btnResetDefaults = new JButton(RESET_DEFAULTS_BUTTON_TEXT);
						btnResetDefaults.addActionListener(this);
						panelIdentificationControls.add(btnResetDefaults);
					}
					{
						zoomSlider = new NViewZoomSlider();
						zoomSlider.setView(view);
						panelIdentificationControls.add(zoomSlider);
					}
				}
				{
					resultsTable = new JTable();
					resultsTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Score" }) {

						private final Class<?>[] types = new Class<?>[] { String.class, Integer.class };
						private final boolean[] canEdit = new boolean[] { false, false };

						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return types[columnIndex];
						}

						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return canEdit[columnIndex];
						}

					});
					DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
					leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
					resultsTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
					scrollPaneResults = new JScrollPane();
					scrollPaneResults.setViewportView(resultsTable);
					scrollPaneResults.setPreferredSize(new Dimension(100, 100));
					panelIdentification.add(scrollPaneResults, BorderLayout.CENTER);
				}
			}
		}
	}

	@Override
	protected void setDefaultValues() {
		comboBoxMatchingFarThreshold.setSelectedItem(
				Utils.matchingThresholdToString(FaceTools.getInstance().getDefaultClient().getMatchingThreshold()));
	}

	@Override
	protected void updateControls() {
		btnIdentify.setEnabled(!subjects.isEmpty() && (subject != null)
				&& ((subject.getStatus() == NBiometricStatus.OK) || (subject.getStatus() == NBiometricStatus.NONE)));
	}

	@Override
	protected void updateFacesTools() {
		try {
			FaceTools.getInstance().getClient().setMatchingThreshold(
					Utils.matchingThresholdFromString(comboBoxMatchingFarThreshold.getSelectedItem().toString()));
		} catch (ParseException e) {
			e.printStackTrace();
			FaceTools.getInstance().getClient()
					.setMatchingThreshold(FaceTools.getInstance().getDefaultClient().getMatchingThreshold());
			comboBoxMatchingFarThreshold.setSelectedItem(
					Utils.matchingThresholdToString(FaceTools.getInstance().getDefaultClient().getMatchingThreshold()));
			JOptionPane.showMessageDialog(this, "FAR is not valid. Using default value.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// ===========================================================
	// Package private methods
	// ===========================================================

	NSubject getSubject() {
		return subject;
	}

	void setSubject(NSubject subject) {
		this.subject = subject;
	}

	List<NSubject> getSubjects() {
		return subjects;
	}

	void appendIdentifyResult(String name, int score) {
		((DefaultTableModel) resultsTable.getModel()).addRow(new Object[] { name, score });
	}

	void prependIdentifyResult(String name, int score) {
		((DefaultTableModel) resultsTable.getModel()).insertRow(0, new Object[] { name, score });
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnLoadTemplates)) {
				openTemplates();
			} else if (ev.getSource().equals(btnOpenProbe)) {
				openProbe();
			} else if (ev.getSource().equals(btnResetDefaults)) {
				comboBoxMatchingFarThreshold.setSelectedItem(Utils
						.matchingThresholdToString(FaceTools.getInstance().getDefaultClient().getMatchingThreshold()));
			} else if (ev.getSource().equals(btnIdentify)) {
				identify();
			}
		} catch (Exception e) {
			e.printStackTrace();
			updateControls();
			JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onClose() {
	}

	// ===========================================================
	// Inner classes
	// ===========================================================

	private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, Object> {

		public void completed(final NBiometricStatus status, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					updateControls();
					if (status != NBiometricStatus.OK) {
						setSubject(null);
						JOptionPane.showMessageDialog(IdentifyFace.this, "Template was not created: " + status, "Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}

			});
		}

		public void failed(final Throwable th, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					updateControls();
					showError(th);
				}

			});
		}

	}

	private class EnrollHandler implements CompletionHandler<NBiometricTask, Object> {

		public void completed(NBiometricTask task, Object attachment) {
			if (task.getStatus() == NBiometricStatus.OK) {

				// Identify current subject in enrolled ones.
				FaceTools.getInstance().getClient().identify(getSubject(), null, identificationHandler);
			} else {
				JOptionPane.showMessageDialog(IdentifyFace.this, "Enrollment failed: " + task.getStatus(), "Error",
						JOptionPane.WARNING_MESSAGE);
			}
		}

		public void failed(final Throwable th, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					updateControls();
					showError(th);
				}

			});
		}

	}

	private class IdentificationHandler implements CompletionHandler<NBiometricStatus, Object> {

		public void completed(final NBiometricStatus status, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					if ((status == NBiometricStatus.OK) || (status == NBiometricStatus.MATCH_NOT_FOUND)) {

						// Match subjects.
						for (NSubject s : getSubjects()) {
							boolean match = false;
							for (NMatchingResult result : getSubject().getMatchingResults()) {
								if (s.getId().equals(result.getId())) {
									match = true;
									prependIdentifyResult(result.getId(), result.getScore());
									break;
								}
							}
							if (!match) {
								appendIdentifyResult(s.getId(), 0);
							}
						}
					} else {
						JOptionPane.showMessageDialog(IdentifyFace.this, "Identification failed: " + status, "Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}

			});
		}

		public void failed(final Throwable th, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					updateControls();
					showError(th);
				}

			});
		}

	}

}
