package com.neuro.app.biometeric;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NICAOWarning;
import com.neurotec.biometrics.NLAttributes;
import com.neurotec.util.NCollectionChangedAction;
import com.neurotec.util.event.NCollectionChangeEvent;
import com.neurotec.util.event.NCollectionChangeListener;

public class IcaoWarningsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Color COLOR_GOOD = new Color(0xFF008000);
	private static final Color COLOR_BAD = new Color(0xFFFF0000);
	private static final Color COLOR_IDETERMINATE = new Color(0xFFFFA500);

	private final NCollectionChangeListener objectsCollectionChanged = new NCollectionChangeListener() {
		
		public void collectionChanged(NCollectionChangeEvent event) {
			if (event.getAction() == NCollectionChangedAction.ADD) {
				if (event.getSource().equals(face.getObjects())) {
					if (attributes != null) {
						attributes.removePropertyChangeListener(attributesPropertyChange);
					}
					attributes = (NLAttributes) event.getNewItems().get(0);
					attributes.addPropertyChangeListener(attributesPropertyChange);
				}
			} else if ((event.getAction() == NCollectionChangedAction.REMOVE) || (event.getAction() == NCollectionChangedAction.RESET)) {
				if (event.getSource().equals(face.getObjects())) {
					if (attributes != null) {
						attributes.removePropertyChangeListener(attributesPropertyChange);
					}
				}
			}
			SwingUtilities.invokeLater(new Runnable() {

				
				public void run() {
					warningsChanged();
				}
			});
		}
	};

	private final PropertyChangeListener attributesPropertyChange = new PropertyChangeListener() {
		
		public void propertyChange(PropertyChangeEvent evt) {
			SwingUtilities.invokeLater(new Runnable() {

				
				public void run() {
					warningsChanged();
				}
			});
		}
	};

	private NFace face;
	private NLAttributes attributes;

	private Set<JLabel> labels;

	private JLabel lblBackgroundUniformity;
	private JLabel lblBlink;
	private JLabel lblDarkGlasses;
	private JLabel lblExpression;
	private JLabel lblFaceDetected;
	private JLabel lblGrayscaleDensity;
	private JLabel lblMouthOpen;
	private JLabel lblLookingAway;
	private JLabel lblRedEye;
	private JLabel lblFaceDarkness;
	private JLabel lblUnnaturalSkinTone;
	private JLabel lblColorsWashedOut;
	private JLabel lblPixelation;
	private JLabel lblSkinReflection;
	private JLabel lblGlassesReflection;
	private JLabel lblPitch;
	private JLabel lblRoll;
	private JLabel lblSaturation;
	private JLabel lblSharpness;
	private JLabel lblTooClose;
	private JLabel lblTooEast;
	private JLabel lblTooFar;
	private JLabel lblTooNorth;
	private JLabel lblTooSouth;
	private JLabel lblTooWest;
	private JLabel lblYaw;
	private JPanel panelWarnings;

	public IcaoWarningsPanel() {
		super();
		initGui();
	}

	private void initGui() {
		labels = new HashSet<JLabel>();
		panelWarnings = new JPanel();
		
		setLayout(new BorderLayout());
		panelWarnings.setLayout(new BoxLayout(panelWarnings, BoxLayout.Y_AXIS));
		
		lblFaceDetected = createLabelAndAdd("Face Detected", panelWarnings, labels);
		lblExpression = createLabelAndAdd("Expression", panelWarnings, labels);
		lblDarkGlasses = createLabelAndAdd("Dark Glasses", panelWarnings, labels);
		lblBlink = createLabelAndAdd("Blink", panelWarnings, labels);
		lblMouthOpen = createLabelAndAdd("Mouth Open", panelWarnings, labels);
		lblLookingAway = createLabelAndAdd("Looking Away", panelWarnings, labels);
		lblRedEye = createLabelAndAdd("Red Eye", panelWarnings, labels);
		lblFaceDarkness = createLabelAndAdd("Face Darkness", panelWarnings, labels);
		lblUnnaturalSkinTone = createLabelAndAdd("Unnatural Skin Tone", panelWarnings, labels);
		lblColorsWashedOut = createLabelAndAdd("Colors Washed Out", panelWarnings, labels);
		lblPixelation = createLabelAndAdd("Pixelation", panelWarnings, labels);
		lblSkinReflection = createLabelAndAdd("Skin Reflection", panelWarnings, labels);
		lblGlassesReflection = createLabelAndAdd("Glasses Reflection", panelWarnings, labels);
		lblRoll = createLabelAndAdd("Roll", panelWarnings, labels);
		lblYaw = createLabelAndAdd("Yaw", panelWarnings, labels);
		lblPitch = createLabelAndAdd("Pitch", panelWarnings, labels);
		lblTooClose = createLabelAndAdd("Too Close", panelWarnings, labels);
		lblTooFar = createLabelAndAdd("Too Far", panelWarnings, labels);
		lblTooNorth = createLabelAndAdd("Too North", panelWarnings, labels);
		lblTooSouth = createLabelAndAdd("Too South", panelWarnings, labels);
		lblTooWest = createLabelAndAdd("Too West", panelWarnings, labels);
		lblTooEast = createLabelAndAdd("Too East", panelWarnings, labels);
		lblSharpness = createLabelAndAdd("Sharpness", panelWarnings, labels);
		lblGrayscaleDensity = createLabelAndAdd("Grayscale Density", panelWarnings, labels);
		lblSaturation = createLabelAndAdd("Saturation", panelWarnings, labels);
		lblBackgroundUniformity = createLabelAndAdd("Background Uniformity", panelWarnings, labels);

		add(panelWarnings, BorderLayout.WEST);
	}

	private JLabel createLabelAndAdd(String labelText, JPanel panel, Set<JLabel> labels) {
		JLabel label = new JLabel();
		label.setFont(new Font("Tahoma", 1, 9)); // NOI18N
		label.setText(labelText);
		panel.add(label);
		labels.add(label);
		return label;
	}

	private void subscribeToFaceEvents() {
		if (face != null) {
			face.getObjects().addCollectionChangeListener(objectsCollectionChanged);
			if (face.getObjects().isEmpty()) {
				attributes = null;
			} else {
				attributes = face.getObjects().get(0);
				attributes.addPropertyChangeListener(attributesPropertyChange);
			}
		}
	}

	private void unsubscribeFromFaceEvents() {
		if (face != null) {
			face.getObjects().removeCollectionChangeListener(objectsCollectionChanged);
		}
		if (attributes != null) {
			attributes.removePropertyChangeListener(attributesPropertyChange);
		}
	}

	private Color getConfidenceColor(EnumSet<NICAOWarning> set, NICAOWarning warning, int confidence) {
		if (set.contains(warning)) {
			if (confidence >= 0 && confidence <= 100) {
				return COLOR_BAD;
			} else {
				return COLOR_IDETERMINATE;
			}
		} else {
			return COLOR_GOOD;
		}
	}

	private Color getColor(EnumSet<NICAOWarning> set, NICAOWarning... warnings) {
		for (NICAOWarning w : warnings) {
			if (set.contains(w)) {
				return COLOR_BAD;
			}
		}
		return COLOR_GOOD;
	}

	private String getConfidenceString(String name, int value) {
		return String.format("%s: %s", name, (value <= 100) ? value : "N/A");
	}

	private void warningsChanged() {
		if (attributes == null) {
			updateAllLabels(COLOR_IDETERMINATE);
		} else {
			EnumSet<NICAOWarning> warnings = attributes.getIcaoWarnings();
			if (warnings.contains(NICAOWarning.FACE_NOT_DETECTED)) {
				updateAllLabels(COLOR_IDETERMINATE);
				updateLabel(lblFaceDetected, COLOR_BAD);
			} else {
				updateLabel(lblFaceDetected, COLOR_GOOD);
				updateLabel(lblExpression, getConfidenceColor(warnings, NICAOWarning.EXPRESSION, attributes.getExpressionConfidence() & 0xFF));
				updateLabel(lblDarkGlasses, getConfidenceColor(warnings, NICAOWarning.DARK_GLASSES, attributes.getDarkGlassesConfidence() & 0xFF));
				updateLabel(lblBlink, getConfidenceColor(warnings, NICAOWarning.BLINK, attributes.getBlinkConfidence() & 0xFF));
				updateLabel(lblMouthOpen, getConfidenceColor(warnings, NICAOWarning.MOUTH_OPEN, attributes.getMouthOpenConfidence() & 0xFF));
				updateLabel(lblLookingAway, getConfidenceColor(warnings, NICAOWarning.LOOKING_AWAY, attributes.getLookingAwayConfidence() & 0xFF));
				updateLabel(lblRedEye, getConfidenceColor(warnings, NICAOWarning.RED_EYE, attributes.getRedEyeConfidence() & 0xFF));
				updateLabel(lblFaceDarkness, getConfidenceColor(warnings, NICAOWarning.FACE_DARKNESS, attributes.getFaceDarknessConfidence() & 0xFF));
				updateLabel(lblUnnaturalSkinTone, getConfidenceColor(warnings, NICAOWarning.UNNATURAL_SKIN_TONE, attributes.getUnnaturalSkinToneConfidence() & 0xFF));
				updateLabel(lblColorsWashedOut, getConfidenceColor(warnings, NICAOWarning.WASHED_OUT, attributes.getWashedOutConfidence() & 0xFF));
				updateLabel(lblPixelation, getConfidenceColor(warnings, NICAOWarning.PIXELATION, attributes.getPixelationConfidence() & 0xFF));
				updateLabel(lblSkinReflection, getConfidenceColor(warnings, NICAOWarning.SKIN_REFLECTION, attributes.getSkinReflectionConfidence() & 0xFF));
				updateLabel(lblGlassesReflection, getConfidenceColor(warnings, NICAOWarning.GLASSES_REFLECTION, attributes.getGlassesReflectionConfidence() & 0xFF));
				updateLabel(lblRoll, getColor(warnings, NICAOWarning.ROLL_LEFT, NICAOWarning.ROLL_RIGHT));
				updateLabel(lblYaw, getColor(warnings, NICAOWarning.YAW_LEFT, NICAOWarning.YAW_RIGHT));
				updateLabel(lblPitch, getColor(warnings, NICAOWarning.PITCH_UP, NICAOWarning.PITCH_DOWN));
				updateLabel(lblTooClose, getColor(warnings, NICAOWarning.TOO_NEAR));
				updateLabel(lblTooFar, getColor(warnings, NICAOWarning.TOO_FAR));
				updateLabel(lblTooNorth, getColor(warnings, NICAOWarning.TOO_NORTH));
				updateLabel(lblTooSouth, getColor(warnings, NICAOWarning.TOO_SOUTH));
				updateLabel(lblTooWest, getColor(warnings, NICAOWarning.TOO_WEST));
				updateLabel(lblTooEast, getColor(warnings, NICAOWarning.TOO_EAST));
				updateLabel(lblSharpness, getColor(warnings, NICAOWarning.SHARPNESS), getConfidenceString("Sharpness", attributes.getSharpness() & 0xFF));
				updateLabel(lblSaturation, getColor(warnings, NICAOWarning.SATURATION), getConfidenceString("Saturation", attributes.getSaturation() & 0xFF));
				updateLabel(lblGrayscaleDensity, getColor(warnings, NICAOWarning.GRAYSCALE_DENSITY), getConfidenceString("Grayscale Density", attributes.getGrayscaleDensity() & 0xFF));
				updateLabel(lblBackgroundUniformity, getColor(warnings, NICAOWarning.BACKGROUND_UNIFORMITY), getConfidenceString("Background Uniformity", attributes.getBackgroundUniformity() & 0xFF));
			}
		}
		repaint();
	}

	private void updateAllLabels(Color color) {
		for (JLabel lbl : labels) {
			lbl.setForeground(color);
		}
	}

	private void updateLabel(JLabel label, Color color, String text) {
		label.setForeground(color);
		label.setText(text);
	}

	private void updateLabel(JLabel label, Color color) {
		label.setForeground(color);
	}

	public NFace getFace() {
		return face;
	}

	public void setFace(NFace face) {
		if ((face == null) || !face.equals(this.face)) {
			unsubscribeFromFaceEvents();
			this.face = face;
			subscribeToFaceEvents();
			warningsChanged();
		}
	}

}
