package com.neuro.app.biometeric;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.SwingUtilities;

import com.neuro.app.util.BasePanel;
import com.neuro.app.util.LicensingPanel;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NSubject.FaceCollection;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.samples.swing.ImageThumbnailFileChooser;
import com.neurotec.samples.util.Utils;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

public final class VerifyFace extends BasePanel implements ActionListener {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	private static final String PANEL_TITLE = "Verify Face";
	private static final String LOAD_LEFT_ITEM_BUTTON_TEXT = "Load left";
	private static final String LOAD_RIGHT_ITEM_BUTTON_TEXT = "Load right";
	private static final String MATCHING_FAR_PANEL_BORDER_TEXT = "Matching FAR: ";
	private static final String RESET_DEFAULT_BUTTON_TEXT = "Default";
	private static final String VERIFY_BUTTON_TEXT = "Verify";
	private static final String CLEAR_ITEMS_BUTTON_TEXT = "Clear items";
	private static final String LEFT_ITEM_LOCATION_LABEL_TEXT = "Left item: ";
	private static final String RIGHT_ITEM_LOCATION_LABEL_TEXT = "Right item: ";
	private static final String SCORE_LABEL_TEXT = "Score";

	private static final String SUBJECT_LEFT = "left";
	private static final String SUBJECT_RIGHT = "right";

	private static final List<String> THRESHOLDS = new ArrayList<String>();

	// ===========================================================
	// Static constructor
	// ===========================================================

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
	private NSubject subjectLeft;
	private NSubject subjectRight;

	private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();
	private final VerificationHandler verificationHandler = new VerificationHandler();

	private JPanel panelToolBar;
	private JPanel panelControls;
	private JPanel panelMathingFar;
	private JPanel panelView;
	private JPanel panelVerificationManagement;
	private JPanel panelStatus;
	private JPanel panelLeftItemInfo;
	private JPanel panelRightItemInfo;
	private JPanel panelResult;
	private JPanel panelLeft;
	private JPanel panelRight;
	private JLabel lblLeftItemLocation;
	private JLabel lblRightItemLocation;
	private JLabel lblLeftItemLocationLabel;
	private JLabel lblRightItemLocationLabel;
	private JLabel lblScore;
	private JLabel lblScoreLabel;
	private JButton btnLoadLeft;
	private JButton btnLoadRight;
	private JButton btnVerify;
	private JButton btnResetDefault;
	private JButton btnClearItems;
	private NFaceView viewLeft;
	private NFaceView viewRight;
	private JPanel rightZoomSliderPanel;
	private JPanel leftZoomSliderPanel;
	private NViewZoomSlider leftZoomSlider;
	private NViewZoomSlider rightZoomSlider;
	private JComboBox comboBoxMatchingFarThreshold;
	private JScrollPane leftScrollPane;
	private JScrollPane rightScrollPane;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public VerifyFace() {
		super();
		this.setName(PANEL_TITLE);

		requiredLicenses.add("Biometrics.FaceExtraction");
		requiredLicenses.add("Biometrics.FaceMatching");

		fc = new ImageThumbnailFileChooser();
		fc.setIcon(Utils.createIconImage("images/Logo16x16.png"));

		subjectLeft = new NSubject();
		subjectRight = new NSubject();
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void loadItem(String position) throws IOException {
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			lblScore.setText("");
			NSubject subjectTmp = null;
			NFace face = null;
			try {
				subjectTmp = NSubject.fromFile(fc.getSelectedFile().getAbsolutePath());
				FaceCollection faces = subjectTmp.getFaces();
				if (faces.isEmpty()) {
					subjectTmp = null;
					throw new IllegalArgumentException("Template contains no face records.");
				}
				face = faces.get(0);
				templateCreationHandler.completed(NBiometricStatus.OK, position);
			} catch (UnsupportedOperationException e) {
				// Ignore. UnsupportedOperationException means file is not a valid template.
			}

			// If file is not a template, try to load it as an image.
			if (subjectTmp == null) {
				face = new NFace();
				face.setFileName(fc.getSelectedFile().getAbsolutePath());
				subjectTmp = new NSubject();
				subjectTmp.getFaces().add(face);
				updateFacesTools();
				FaceTools.getInstance().getClient().createTemplate(subjectTmp, position, templateCreationHandler);
			}

			if (SUBJECT_LEFT.equals(position)) {
				subjectLeft = subjectTmp;
				lblLeftItemLocation.setText(fc.getSelectedFile().getAbsolutePath());
				viewLeft.setFace(face);
			} else if (SUBJECT_RIGHT.equals(position)) {
				subjectRight = subjectTmp;
				lblRightItemLocation.setText(fc.getSelectedFile().getAbsolutePath());
				viewRight.setFace(face);
			} else {
				throw new AssertionError("Unknown subject position: " + position);
			}
		}
	}

	private void verify() {
		updateFacesTools();
		FaceTools.getInstance().getClient().verify(subjectLeft, subjectRight, null, verificationHandler);
	}

	private void clear() {
		viewLeft.setFace(null);
		viewRight.setFace(null);
		subjectLeft.clear();
		subjectRight.clear();
		updateControls();
		lblScore.setText("");
		lblLeftItemLocation.setText("");
		lblRightItemLocation.setText("");
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@Override
	protected void initGUI() {
		setLayout(new BorderLayout());
		panelToolBar = new JPanel();
		panelToolBar.setLayout(new BoxLayout(panelToolBar, BoxLayout.Y_AXIS));
		add(panelToolBar, BorderLayout.PAGE_START);
		{
			licensing = new LicensingPanel(requiredLicenses, Collections.<String>emptyList());
			panelToolBar.add(licensing);
		}
		{
			panelControls = new JPanel();
			panelControls.setLayout(new FlowLayout());
			panelControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			panelToolBar.add(panelControls);
			{
				btnLoadLeft = new JButton(LOAD_LEFT_ITEM_BUTTON_TEXT);
				btnLoadLeft.addActionListener(this);
				panelControls.add(btnLoadLeft);
			}
			{
				panelMathingFar = new JPanel();
				panelMathingFar.setLayout(new FlowLayout());
				panelMathingFar.setBorder(BorderFactory.createTitledBorder(MATCHING_FAR_PANEL_BORDER_TEXT));
				panelControls.add(panelMathingFar);
				{
					DefaultComboBoxModel model = new DefaultComboBoxModel(THRESHOLDS.toArray());
					comboBoxMatchingFarThreshold = new JComboBox(model);
					panelMathingFar.add(comboBoxMatchingFarThreshold);
				}
				{
					btnResetDefault = new JButton(RESET_DEFAULT_BUTTON_TEXT);
					btnResetDefault.addActionListener(this);
					panelMathingFar.add(btnResetDefault);
				}
			}
			{
				btnLoadRight = new JButton(LOAD_RIGHT_ITEM_BUTTON_TEXT);
				btnLoadRight.addActionListener(this);
				panelControls.add(btnLoadRight);
			}
		}
		panelView = new JPanel();
		panelView.setLayout(new GridLayout());
		add(panelView, BorderLayout.CENTER);
		{
			panelLeft = new JPanel();
			panelLeft.setLayout(new BorderLayout());
			panelView.add(panelLeft);
			{
				leftScrollPane = new JScrollPane();
				viewLeft = new NFaceView();
				viewLeft.setAutofit(true);
				leftScrollPane.setViewportView(viewLeft);
				panelLeft.add(leftScrollPane, BorderLayout.CENTER);
			}
			{
				leftZoomSliderPanel = new JPanel();
				leftZoomSliderPanel.setLayout(new BorderLayout());
				panelLeft.add(leftZoomSliderPanel, BorderLayout.SOUTH);
				{
					leftZoomSlider = new NViewZoomSlider();
					leftZoomSlider.setView(viewLeft);
					leftZoomSliderPanel.add(leftZoomSlider, BorderLayout.WEST);
				}
			}
		}
		{
			panelRight = new JPanel();
			panelRight.setLayout(new BorderLayout());
			panelView.add(panelRight);
			{
				rightScrollPane = new JScrollPane();
				viewRight = new NFaceView();
				viewRight.setAutofit(true);
				rightScrollPane.setViewportView(viewRight);
				panelRight.add(rightScrollPane, BorderLayout.CENTER);
			}
			{
				rightZoomSliderPanel = new JPanel();
				rightZoomSliderPanel.setLayout(new BorderLayout());
				panelRight.add(rightZoomSliderPanel, BorderLayout.SOUTH);
				{
					rightZoomSlider = new NViewZoomSlider();
					rightZoomSlider.setView(viewRight);
					rightZoomSliderPanel.add(rightZoomSlider, BorderLayout.EAST);
				}
			}
		}
		panelResult = new JPanel();
		panelResult.setLayout(new BoxLayout(panelResult, BoxLayout.Y_AXIS));
		add(panelResult, BorderLayout.PAGE_END);
		{
			panelLeftItemInfo = new JPanel();
			panelLeftItemInfo.setLayout(new FlowLayout(FlowLayout.LEFT));
			panelResult.add(panelLeftItemInfo);
			{
				lblLeftItemLocationLabel = new JLabel(LEFT_ITEM_LOCATION_LABEL_TEXT);
				panelLeftItemInfo.add(lblLeftItemLocationLabel);
			}
			{
				lblLeftItemLocation = new JLabel();
				panelLeftItemInfo.add(lblLeftItemLocation);
			}
			panelRightItemInfo = new JPanel();
			panelRightItemInfo.setLayout(new FlowLayout(FlowLayout.LEFT));
			panelResult.add(panelRightItemInfo);
			{
				lblRightItemLocationLabel = new JLabel(RIGHT_ITEM_LOCATION_LABEL_TEXT);
				panelRightItemInfo.add(lblRightItemLocationLabel);
			}
			{
				lblRightItemLocation = new JLabel();
				panelRightItemInfo.add(lblRightItemLocation);
			}
		}
		{
			panelVerificationManagement = new JPanel();
			panelVerificationManagement.setLayout(new FlowLayout(FlowLayout.LEFT));
			panelResult.add(panelVerificationManagement);
			{
				{
					btnVerify = new JButton(VERIFY_BUTTON_TEXT);
					btnVerify.addActionListener(this);
					btnVerify.setEnabled(false);
					panelVerificationManagement.add(btnVerify);
				}
				{
					btnClearItems = new JButton(CLEAR_ITEMS_BUTTON_TEXT);
					btnClearItems.addActionListener(this);
					panelVerificationManagement.add(btnClearItems);
				}
			}
			panelStatus = new JPanel();
			panelStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
			panelResult.add(panelStatus);
			{
				lblScoreLabel = new JLabel(SCORE_LABEL_TEXT);
				panelStatus.add(lblScoreLabel);
			}
			{
				lblScore = new JLabel();
				panelStatus.add(lblScore);
			}
		}
	}

	@Override
	protected void setDefaultValues() {
		comboBoxMatchingFarThreshold.setSelectedItem(Utils.matchingThresholdToString(FaceTools.getInstance().getDefaultClient().getMatchingThreshold()));
	}

	@Override
	protected void updateControls() {
		if (subjectLeft.getFaces().isEmpty()
			|| subjectLeft.getFaces().get(0).getObjects().isEmpty()
			|| (subjectLeft.getFaces().get(0).getObjects().get(0).getTemplate() == null)
			|| subjectRight.getFaces().isEmpty()
			|| subjectRight.getFaces().get(0).getObjects().isEmpty()
			|| (subjectRight.getFaces().get(0).getObjects().get(0).getTemplate() == null)) {
			btnVerify.setEnabled(false);
		} else {
			btnVerify.setEnabled(true);
		}
	}

	@Override
	protected void updateFacesTools() {
		NBiometricClient client = FaceTools.getInstance().getClient();
		client.setFacesDetectAllFeaturePoints(true);
		try {
			FaceTools.getInstance().getClient().setMatchingThreshold(Utils.matchingThresholdFromString(comboBoxMatchingFarThreshold.getSelectedItem().toString()));
		} catch (ParseException e) {
			e.printStackTrace();
			FaceTools.getInstance().getClient().setMatchingThreshold(FaceTools.getInstance().getDefaultClient().getMatchingThreshold());
			comboBoxMatchingFarThreshold.setSelectedItem(Utils.matchingThresholdToString(FaceTools.getInstance().getDefaultClient().getMatchingThreshold()));
			JOptionPane.showMessageDialog(this, "FAR is not valid. Using default value.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// ===========================================================
	// Package private methods
	// ===========================================================

	void updateLabel(String msg) {
		lblScore.setText(msg);
	}

	NSubject getLeft() {
		return subjectLeft;
	}

	NSubject getRight() {
		return subjectRight;
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	
	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnLoadLeft)) {
				loadItem(SUBJECT_LEFT);
			} else if (ev.getSource().equals(btnLoadRight)) {
				loadItem(SUBJECT_RIGHT);
			} else if (ev.getSource().equals(btnVerify)) {
				verify();
			} else if (ev.getSource().equals(btnClearItems)) {
				clear();
			} else if (ev.getSource().equals(btnResetDefault)) {
				comboBoxMatchingFarThreshold.setSelectedItem(Utils.matchingThresholdToString(FaceTools.getInstance().getDefaultClient().getMatchingThreshold()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
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

	private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, String> {

		
		public void completed(final NBiometricStatus status, final String subject) {
			SwingUtilities.invokeLater(new Runnable() {

				
				public void run() {
					if (status != NBiometricStatus.OK) {
						JOptionPane.showMessageDialog(VerifyFace.this, "Template was not created: " + status, "Error", JOptionPane.WARNING_MESSAGE);
					}
					updateControls();
				}

			});
		}

		
		public void failed(final Throwable th, final String subject) {
			SwingUtilities.invokeLater(new Runnable() {

				
				public void run() {
					showError(th);
				}

			});
		}

	}

	private class VerificationHandler implements CompletionHandler<NBiometricStatus, String> {

		
		public void completed(final NBiometricStatus status, final String subject) {
			SwingUtilities.invokeLater(new Runnable() {

				
				public void run() {
					if (status == NBiometricStatus.OK) {
						int score = getLeft().getMatchingResults().get(0).getScore();
						String msg = "Score of matched templates: " + score;
						updateLabel(msg);
						JOptionPane.showMessageDialog(VerifyFace.this, msg, "Match", JOptionPane.PLAIN_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(VerifyFace.this, "Templates didn't match.", "No match", JOptionPane.WARNING_MESSAGE);
					}
				}

			});
		}

		
		public void failed(final Throwable th, final String subject) {
			SwingUtilities.invokeLater(new Runnable() {

				
				public void run() {
					showError(th);
				}

			});
		}

	}

}
