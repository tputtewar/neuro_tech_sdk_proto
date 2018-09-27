package com.neuro.app.surveillance;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.neuro.app.dao.DBConnection;
import com.neuro.app.dao.DBService;
import com.neuro.app.util.BasePanel;
import com.neuro.app.util.CameraType;
import com.neuro.app.util.ColorTableCellRenderer;
import com.neuro.app.util.LicensingPanel;
import com.neuro.app.util.NotificationStatus;
import com.neuro.app.util.Roles;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLTemplate;
import com.neurotec.biometrics.NLivenessMode;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.event.ChangeEvent;
import com.neurotec.event.ChangeListener;
import com.neurotec.images.NImage;
import com.neurotec.images.NImages;
import com.neurotec.io.NBuffer;
import com.neurotec.samples.ConnectToDevicePanel;
import com.neurotec.samples.Subject;
import com.neurotec.samples.swing.ImageThumbnailFileChooser;
import com.neurotec.samples.util.Utils;
import com.neurotec.surveillance.NSEDMatchResult;
import com.neurotec.surveillance.NSurveillance;
import com.neurotec.surveillance.NSurveillanceEvent;
import com.neurotec.surveillance.NSurveillanceEventDetails;
import com.neurotec.surveillance.NSurveillanceEventDetails.BestMatchCollection;
import com.neurotec.surveillance.NSurveillanceListener;
import com.neurotec.surveillance.NSurveillanceSource;
import com.neurotec.surveillance.NSurveillanceTrackingType;

public final class OUTWatchList extends BasePanel implements ActionListener {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;
	private static final String PANEL_TITLE = "OutWard Watchlist";
	// ===========================================================
	// Private fields
	// ===========================================================

	private SurveillanceView view;
	private JFileChooser fc;
	private ImageThumbnailFileChooser fcSubject;

	private long currentFrameIndex;

	private JButton btnAddSubject;
	private JButton btnClearSubjects;
	private JButton btnConnectTo;
	private JButton btnDisconnect;
	private JButton btnRefresh;
	private JButton btnStart;
	private JButton btnStop;
	private ButtonGroup buttonGroupSource;
	@SuppressWarnings("rawtypes")
	private JComboBox comboBoxCameras;
	private JLabel lblStatus;
	private JPanel panelBottom;
	private JPanel panelControls;
	private JPanel panelMain;
	private JPanel panelRight;
	private JPanel panelSource;
	private JPanel panelSourceOuter;
	private JPanel panelStatus;
	private JPanel panelTop;
	private JPanel panelWatchList;
	private JPanel panelWatchListControls;
	private JPanel panelWatchListTable;
	private JRadioButton rbCamera;
	private JRadioButton rbVideo;
	private JScrollPane scrollPaneResults;
	private JScrollPane spView;
	private JSplitPane splitPane;
	private JTable oTableResults;
	private JTextField tfVideo;
	private int unknownID = 1;
	private NBiometricClient biometricClient;
	private DBService dbService;

	private final NSurveillanceListener imageCapturedListener = new NSurveillanceListener() {

		public void eventOccured(NSurveillanceEvent ev) {
			for (NSurveillanceEventDetails details : ev.getEventDetailsArray()) {
				NImage image = details.getOriginalImage();
				view.setFrame(image, details.getTimeStamp());
				image.dispose();
				details.dispose();
			}
		}
	};

	private final NSurveillanceListener subjectAppearedListener = new NSurveillanceListener() {

		public void eventOccured(NSurveillanceEvent ev) {
			for (NSurveillanceEventDetails details : ev.getEventDetailsArray()) {
				Subject subject = new Subject(details.getTraceIndex());
				subject.setBoundingRectangle(details.getRectangle(), details.getTimeStamp());
				view.addSubject(subject);
				details.dispose();
			}
		}

	};

	NImage imageNew = null;
	BestMatchCollection matches = null;
	int score = 0;
	private final NSurveillanceListener subjectTrackListener = new NSurveillanceListener() {

		@SuppressWarnings("resource")
		public void eventOccured(NSurveillanceEvent ev) {
			for (NSurveillanceEventDetails details : ev.getEventDetailsArray()) {
				if (currentFrameIndex <= details.getFrameIndex()) {
					currentFrameIndex = details.getFrameIndex();
					view.setSubjectRectangle(details.getTraceIndex(), details.getRectangle(), details.getTimeStamp());
				}

				final NImage image = details.getFace().getImage();
				imageNew = image;
				String unMatchedId = null;
				NSEDMatchResult bestMatch = null;
				final Date date = (Date) details.getTimeStamp();
				matches = details.getBestMatches();
				final String matchedId;
				if (matches.isEmpty()) {
					matchedId = null;
				} else {
					bestMatch = matches.get(0);
					for (NSEDMatchResult match : matches) {
						if (bestMatch.getScore() < match.getScore()) {
							bestMatch = match;
						}
					}
					matchedId = bestMatch.getId();
					score = bestMatch.getScore();
				}

				if (matchedId != null) {
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							incrementDetectedCount(matchedId, date);
						}
					});
					try {
						Timestamp timeStampOut = dbService.getTimestamp(new Date(date.getTime()));
						Timestamp timestamp = new Timestamp((new java.util.Date()).getTime());
						dbService.saveInsideOutInfoToDB(matchedId, score, 18, "", 1,
								dbService.getDeviceType(PANEL_TITLE));
						dbService.saveTheNotification(Roles.ADMIN, "SurveillanceApp", "Identified", matchedId,
								NotificationStatus.VISIBLE, timeStampOut);
						dbService.markAttendanceInHistory(CameraType.OUT, matchedId, timeStampOut, timestamp);

					} catch (Exception e) {
						System.out.println("SQLException: - " + e);
						e.printStackTrace();
					}

				}

				details.dispose();
			}
		}

	};

	private final NSurveillanceListener subjectDisappearedListener = new NSurveillanceListener() {

		public void eventOccured(NSurveillanceEvent ev) {
			for (NSurveillanceEventDetails details : ev.getEventDetailsArray()) {

				String unMatchedId = null;
				unMatchedId = "anonymous0" + unknownID + ".png";
				System.out.println(unMatchedId);
				try {
					Timestamp timeStampOut = dbService
							.getTimestamp(new Date(details.getTimeStamp().getTime()));
					Timestamp timestamp = new Timestamp((new java.util.Date()).getTime());

					addUnknownSubjectToDb(unMatchedId, imageNew);
					dbService.saveInsideOutInfoToDB(unMatchedId, score, 18, "", 1,
							dbService.getDeviceType(PANEL_TITLE));
					dbService.saveSubjectInfoForUnknownToDB(unMatchedId, imageNew,
							dbService.getDeviceType(PANEL_TITLE));
					dbService.saveTheNotification(Roles.ADMIN, "SurveillanceApp", "UnIdentified", unMatchedId,
							NotificationStatus.HIDDEN, timeStampOut);
					dbService.markAttendanceInHistory(CameraType.OUT, unMatchedId, timeStampOut, timestamp);
					unknownID++;
					if ((oTableResults.getModel().getRowCount() != 0)) {
						oTableResults.repaint();
					}
					((DefaultTableModel) oTableResults.getModel()).addRow(new Object[] { unMatchedId, 0, null, 0 });
				} catch (Exception e) {
					System.out.println("SQLException: - " + e);
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				}
				view.removeSubject(details.getTraceIndex());
				details.dispose();
			}
		}

	};

	private final ChangeListener runningChangeListener = new ChangeListener() {

		public void stateChanged(ChangeEvent e) {
			NSurveillance surveillance = SurveillanceTools.getInstance().getSurveillance();
			if (SurveillanceTools.getInstance().getSurveillance().isRunning()) {
				currentFrameIndex = 0;
			} else {
				surveillance.getSources().clear();
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					if (SurveillanceTools.getInstance().getSurveillance().isRunning()) {
						lblStatus.setText("Surveillance is running.");
					} else {
						lblStatus.setText("Surveillance finished.");
					}
					updateControls();
				}
			});
		}
	};

	private final NSurveillanceListener anyEventListener = new NSurveillanceListener() {

		public void eventOccured(NSurveillanceEvent ev) {
			for (final NSurveillanceEventDetails details : ev.getEventDetailsArray()) {
				Throwable th = details.getError();
				if (th != null) {
					th.printStackTrace();

					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							lblStatus.setText("Error: " + details.getStatus());
							updateControls();
						}
					});
					showError(th);
				}
				details.dispose();
			}
		}

	};

	protected void addUnknownSubjectToDb(String unMatchedId, NImage image) throws Throwable {
		NSubject subject = null;
		NBiometricTask task = null;
		try {

			subject = new NSubject();
			NFace face = new NFace();
			face.setImage(image);
			subject.getFaces().add(face);
			subject.setId(unMatchedId);

			task = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL_WITH_DUPLICATE_CHECK), subject);
			biometricClient.performTask(task);

			if (task.getStatus() != NBiometricStatus.OK) {
				System.out.format("addUnknownSubjectToDb :: Identification was unsuccessful. Status: {0}.",
						task.getStatus());
				if (task.getError() != null)
					throw task.getError();
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	// ===========================================================
	// Public constructor
	// ===========================================================

	public OUTWatchList() {
		super();
		requiredLicenses = new ArrayList<String>();
		requiredLicenses.add("Surveillance");
		optionalLicenses = new ArrayList<String>();
	}

	// ===========================================================
	// Private methods
	// ===========================================================
	private int percent;

	public OUTWatchList(int percent) throws Throwable {
		super();
		this.setName(PANEL_TITLE);
		requiredLicenses = new ArrayList<String>();
		requiredLicenses.add("Surveillance");
		requiredLicenses.add("Biometrics.FaceExtraction");
		requiredLicenses.add("Biometrics.FaceSegmentsDetection");
		requiredLicenses.add("Devices.Cameras");
		optionalLicenses = new ArrayList<String>();
		this.percent = percent;

		// set connection to Mysql database
		DBConnection.getInstance().connectDataBase();
		dbService = new DBService();

		// set connection to Mysql database using SDK
		biometricClient = new NBiometricClient();
		biometricClient.setDatabaseConnectionToOdbc("Dsn=neurotechnology;UID=root;PWD=passw0rd", "outimageStore");
		biometricClient.setFacesLivenessMode(NLivenessMode.PASSIVE_AND_ACTIVE);

		initGUI();
		initTab();

		addWatchSubject();
		checkForUpdatesInWatchlist();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateCameras() {
		DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxCameras.getModel();
		model.removeAllElements();
		for (NDevice device : SurveillanceTools.getInstance().getSurveillance().getDeviceManager().getDevices()) {
			model.addElement(device);
		}
		if (comboBoxCameras.getItemCount() > 0) {
			comboBoxCameras.setSelectedIndex(0);
		}
	}

	private void showConnectTo() {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		ConnectToDevicePanel connectPanel = new ConnectToDevicePanel(dialog);
		dialog.getContentPane().add(connectPanel);
		dialog.setSize(new Dimension(500, 500));
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		if (connectPanel.isResultOk()) {
			NDevice newDevice = null;
			newDevice = SurveillanceTools.getInstance().getSurveillance().getDeviceManager()
					.connectToDevice(connectPanel.getSelectedPlugin(), connectPanel.getParameters());
			updateCameras();
			comboBoxCameras.setSelectedItem(newDevice);

			if (comboBoxCameras.getSelectedItem() != newDevice) {
				if (newDevice != null) {
					SurveillanceTools.getInstance().getSurveillance().getDeviceManager()
							.disconnectFromDevice(newDevice);
				}

				JOptionPane.showMessageDialog(this,
						"Failed to create connection to device using specified connection details", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private NSurveillanceSource getSource() throws IOException {
		NCamera camera;
		if (rbCamera.isSelected()) {
			camera = (NCamera) comboBoxCameras.getSelectedItem();
		} else if (rbVideo.isSelected()) {
			String file = tfVideo.getText();
			camera = (NCamera) SurveillanceTools.getInstance().connectDevice(file, false);
		} else {
			throw new AssertionError("One of the three radio buttons must be selected.");
		}
		return new NSurveillanceSource(NSurveillanceTrackingType.FACES, camera);
	}

	private void addWatchSubject() throws Throwable {

		TreeMap<String, ArrayList<Object>> sorted = dbService.getImageList("user");

		List<String> idsList = null;
		for (Entry<String, ArrayList<Object>> mapping : sorted.entrySet()) {
			try {
				System.out.println(mapping.getKey() + " ==> " + mapping.getValue());
//				if (mapping.getValue().get(2).equals("OUT")) {
				final NSubject subject = new NSubject();
				NFace face = new NFace();
				face.setImage(NImage.fromMemory((ByteBuffer) mapping.getValue().get(1)));
				subject.getFaces().add(face);
				final String id = mapping.getKey();
				subject.setId(id);
				idsList = Arrays.asList(biometricClient.listIds());
				NBiometricStatus status = SurveillanceTools.getInstance().getEngine().createTemplate(subject);
				if (status == NBiometricStatus.OK) {
					NBuffer template = subject.getTemplateBuffer();
					try {
						SurveillanceTools.getInstance().getSurveillance().addTemplate(id, template);
						if (!idsList.contains(id)) {
							SwingUtilities.invokeLater(new Runnable() {

								public void run() {

									NBiometricTask task = biometricClient
											.createTask(EnumSet.of(NBiometricOperation.ENROLL), subject);
									biometricClient.performTask(task);

									if (task.getStatus() != NBiometricStatus.OK) {
										System.out.format(
												"addWatchSubject :: Identification was unsuccessful. Status: {0}.",
												task.getStatus());
										if (task.getError() != null)
											try {
												throw task.getError();
											} catch (Throwable e) {
												e.printStackTrace();
											}
									}
								}

							});
						}
					} finally {
						template.dispose();
					}

					if ((oTableResults.getModel().getRowCount() != 0)) {
						oTableResults.repaint();
					}
					((DefaultTableModel) oTableResults.getModel()).addRow(new Object[] { id, 0, null, 0 });
				} else {
					System.out.println("Template creation failed: " + status);
				}
//				}
			} catch (Exception e) {
				System.out.println("Exception: " + e);
			}
		}
	}

	private void clearWatchSubjects() {
		SurveillanceTools.getInstance().getSurveillance().removeAllTemplates();
		((DefaultTableModel) oTableResults.getModel()).setRowCount(0);
	}

	private void checkForUpdatesInWatchlist() {
		// New timer which works!
		int delay = 300000; // milliseconds
		ActionListener loadSubjectFromDBtaskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					addWatchSubject();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		};
		new Timer(delay, loadSubjectFromDBtaskPerformer).start();
	}

	private void incrementDetectedCount(String id, Date date) {
		DefaultTableModel model = (DefaultTableModel) oTableResults.getModel();
		int rowCount = oTableResults.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			String rowId = (String) model.getValueAt(i, 0);
			int state = (Integer) model.getValueAt(i, 3);
			if (rowId.equals(id)) {
				// Updated BY Trupti
				if (state == 0) {
					model.setValueAt((Integer) model.getValueAt(i, 1) + 1, i, 1);
					model.setValueAt((Integer) model.getValueAt(i, 3) + 2, i, 3);
					model.setValueAt(new Timestamp(date.getTime()), i, 2);
				} else if (state == 1) {
					model.setValueAt((Integer) model.getValueAt(i, 1) + 1, i, 1);
					model.setValueAt((Integer) model.getValueAt(i, 3) + 1, i, 3);
					model.setValueAt(new Timestamp(date.getTime()), i, 2);
				}
			} else {
				// else added BY Trupti
				if (state == 0) {
					model.setValueAt((Integer) model.getValueAt(i, 3) + 1, i, 3);
				}
			}
			oTableResults.repaint();
		}
	}

	@SuppressWarnings("unused")
	private void clearDetectedCounts() {
		DefaultTableModel model = (DefaultTableModel) oTableResults.getModel();
		int rowCount = oTableResults.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			model.setValueAt(0, i, 1);
		}
	}

	private void startSurveillance() throws IOException {
		NSurveillance surveillance = SurveillanceTools.getInstance().getSurveillance();
		surveillance.getSources().add(getSource());
		// clearDetectedCounts();
		surveillance.start();
	}

	private void stopSurveillance() {
		NSurveillance surveillance = SurveillanceTools.getInstance().getSurveillance();
		surveillance.stop();
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@SuppressWarnings("rawtypes")
	@Override
	protected void initGUI() {
		GridBagConstraints gridBagConstraints;

		buttonGroupSource = new ButtonGroup();
		splitPane = new JSplitPane();
		panelMain = new JPanel();
		panelTop = new JPanel();
		panelSourceOuter = new JPanel();
		panelSource = new JPanel();
		rbCamera = new JRadioButton();
		rbVideo = new JRadioButton();
		tfVideo = new JTextField();
		comboBoxCameras = new JComboBox();
		btnConnectTo = new JButton();
		btnDisconnect = new JButton();
		btnRefresh = new JButton();
		panelControls = new JPanel();
		btnStart = new JButton();
		btnStop = new JButton();
		spView = new JScrollPane();
		panelRight = new JPanel();
		panelWatchList = new JPanel();
		panelWatchListControls = new JPanel();
		btnAddSubject = new JButton();
		btnClearSubjects = new JButton();
		panelWatchListTable = new JPanel();
		scrollPaneResults = new JScrollPane();
		oTableResults = new JTable();
		panelBottom = new JPanel();
		panelStatus = new JPanel();
		lblStatus = new JLabel();

		setLayout(new BorderLayout());

		splitPane.setResizeWeight(0.5);

		panelMain.setLayout(new BorderLayout());

		panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));

		panelSourceOuter.setBorder(BorderFactory.createTitledBorder("OutWard Source"));
		panelSourceOuter.setLayout(new FlowLayout(FlowLayout.LEADING));

		GridBagLayout panelSourceLayout = new GridBagLayout();
		panelSourceLayout.columnWidths = new int[] { 0, 5, 0, 5, 0, 5, 0 };
		panelSourceLayout.rowHeights = new int[] { 0, 5, 0, 5, 0 };
		panelSource.setLayout(panelSourceLayout);

		buttonGroupSource.add(rbCamera);
		rbCamera.setText("Camera");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		panelSource.add(rbCamera, gridBagConstraints);

		comboBoxCameras.setPreferredSize(new Dimension(250, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 5;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		panelSource.add(comboBoxCameras, gridBagConstraints);

		btnRefresh.setText("Refresh");
		btnRefresh.setPreferredSize(new Dimension(80, 23));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		panelSource.add(btnRefresh, gridBagConstraints);

		panelSourceOuter.add(panelSource);

		panelTop.add(panelSourceOuter);

		panelControls.setLayout(new FlowLayout(FlowLayout.LEADING));

		btnStart.setText("Start Surveillance");
		panelControls.add(btnStart);

		btnStop.setText("Stop Surveillance ");
		panelControls.add(btnStop);

		panelTop.add(panelControls);

		panelMain.add(panelTop, BorderLayout.NORTH);
		panelMain.add(spView, BorderLayout.CENTER);

		splitPane.setLeftComponent(panelMain);

		panelRight.setLayout(new BorderLayout());

		panelWatchList.setBorder(BorderFactory.createTitledBorder("OutWardWatch list"));
		panelWatchList.setLayout(new BorderLayout());

		panelWatchListControls.setLayout(new FlowLayout(FlowLayout.LEADING));

		btnClearSubjects.setText("Clear");
		panelWatchListControls.add(btnClearSubjects);

		panelWatchList.add(panelWatchListControls, BorderLayout.NORTH);

		panelWatchListTable.setLayout(new GridLayout(1, 1));

		scrollPaneResults.setPreferredSize(new Dimension(200, 0));

		oTableResults.setModel(
				new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Status", "DetectedAt", "State" }) {

					private static final long serialVersionUID = 1L;

					private final Class<?>[] types = new Class<?>[] { String.class, Integer.class, String.class,
							Integer.class };

					private final boolean[] canEdit = new boolean[] { false, false, false, false };

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
		oTableResults.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
		oTableResults.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);

		oTableResults.setDefaultRenderer(Object.class, new ColorTableCellRenderer());

		scrollPaneResults.setViewportView(oTableResults);

		panelWatchListTable.add(scrollPaneResults);

		panelWatchList.add(panelWatchListTable, BorderLayout.CENTER);

		panelRight.add(panelWatchList, BorderLayout.CENTER);

		splitPane.setRightComponent(panelRight);

		add(splitPane, BorderLayout.CENTER);

		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.Y_AXIS));

		panelStatus.setLayout(new FlowLayout(FlowLayout.LEADING));

		lblStatus.setText("Ready");
		panelStatus.add(lblStatus);

		panelBottom.add(panelStatus);

		add(panelBottom, BorderLayout.SOUTH);

		licensing = new LicensingPanel(requiredLicenses, optionalLicenses);
		add(licensing, BorderLayout.NORTH);

		fc = new ImageThumbnailFileChooser();
		view = new SurveillanceView();
		spView.setViewportView(view);

		fcSubject = new ImageThumbnailFileChooser();
		fcSubject.setIcon(Utils.createIconImage("images/logo.png"));
		fcSubject.setFileFilter(new Utils.ImageFileFilter(NImages.getOpenFileFilter()));
		fcSubject.setMultiSelectionEnabled(true);

		updateCameras();
		rbCamera.setSelected(true);

		tfVideo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (fc.showOpenDialog(OUTWatchList.this) == JFileChooser.APPROVE_OPTION) {
					tfVideo.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}

		});
		btnStart.addActionListener(this);
		btnStop.addActionListener(this);
		btnRefresh.addActionListener(this);
		btnConnectTo.addActionListener(this);
		btnDisconnect.addActionListener(this);
		btnAddSubject.addActionListener(this);
		btnClearSubjects.addActionListener(this);
		rbCamera.addActionListener(this);
		rbVideo.addActionListener(this);
	}

	@Override
	protected void setDefaultValues() {

	}

	@Override
	protected void updateControls() {
		boolean busy = SurveillanceTools.getInstance().getSurveillance().isRunning();

		btnRefresh.setEnabled(!busy && rbCamera.isSelected());
		btnConnectTo.setEnabled(!busy && rbCamera.isSelected());
		btnDisconnect.setEnabled(!busy && rbCamera.isSelected() && (comboBoxCameras.getSelectedItem() != null)
				&& ((NDevice) comboBoxCameras.getSelectedItem()).isDisconnectable());
		rbVideo.setEnabled(!busy);
		rbCamera.setEnabled(!busy);
		comboBoxCameras.setEnabled(!busy && rbCamera.isSelected() && (comboBoxCameras.getItemCount() > 0));
		tfVideo.setEnabled(!busy && rbVideo.isSelected());

		btnStart.setEnabled(!busy && (!rbCamera.isSelected() || (comboBoxCameras.getSelectedItem() != null)));
		btnStop.setEnabled(busy);
		btnAddSubject.setEnabled(!busy);
		btnClearSubjects.setEnabled(!busy);
	}

	@Override
	protected void updateFacesTools() {

	}

	protected void initTab() {
		NSurveillance surveillance = SurveillanceTools.getInstance().getSurveillance();

		// In case it has been added already (can't remove native callback in a native
		// thread).
		surveillance.removeRunningChangeListener(runningChangeListener);

		surveillance.addListener(anyEventListener);
		surveillance.addRunningChangeListener(runningChangeListener);
		surveillance.addImageCapturedListener(imageCapturedListener);
		surveillance.addSubjectAppearedListener(subjectAppearedListener);
		surveillance.addSubjectTrackListener(subjectTrackListener);
		surveillance.addSubjectDisappearedListener(subjectDisappearedListener);

		// hiding the status column in watchList jTable -- TRUPTI

		oTableResults.getColumnModel().getColumn(1).setMinWidth(-1);
		oTableResults.getColumnModel().getColumn(1).setWidth(-1);
		oTableResults.getColumnModel().getColumn(1).setMaxWidth(-1);
		oTableResults.getColumnModel().getColumn(1).setPreferredWidth(-1);

		// hiding the state coulmn in watchList jTable -- TRUPTI

		oTableResults.getColumnModel().getColumn(3).setMinWidth(-1);
		oTableResults.getColumnModel().getColumn(3).setWidth(-1);
		oTableResults.getColumnModel().getColumn(3).setMaxWidth(-1);
		oTableResults.getColumnModel().getColumn(3).setPreferredWidth(-1);

	}

	protected void cleanupTab() {
		NSurveillance surveillance = SurveillanceTools.getInstance().getSurveillance();
		surveillance.stop();

		surveillance.removeListener(anyEventListener);
		surveillance.removeImageCapturedListener(imageCapturedListener);
		surveillance.removeSubjectAppearedListener(subjectAppearedListener);
		surveillance.removeSubjectTrackListener(subjectTrackListener);
		surveillance.removeSubjectDisappearedListener(subjectDisappearedListener);
	}

	@Override
	public void onDestroy() {

	}

	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnStart)) {
				startSurveillance();
			} else if (ev.getSource().equals(btnStop)) {
				stopSurveillance();
			} else if (ev.getSource().equals(btnConnectTo)) {
				showConnectTo();
			} else if (ev.getSource().equals(btnDisconnect)) {
				NDevice device = (NDevice) comboBoxCameras.getSelectedItem();
				if (device != null) {
					SurveillanceTools.getInstance().getSurveillance().getDeviceManager().disconnectFromDevice(device);
				}
				updateCameras();
			} else if (ev.getSource().equals(btnRefresh)) {
				updateCameras();
			} else if (ev.getSource().equals(rbVideo)) {
				if (tfVideo.getText().isEmpty()) {
					if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
						tfVideo.setText(fc.getSelectedFile().getAbsolutePath());
					}
				}
			} else if (ev.getSource().equals(btnAddSubject)) {
				addWatchSubject();
			} else if (ev.getSource().equals(btnClearSubjects)) {
				clearWatchSubjects();
			}
			updateControls();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
			updateControls();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = getParent().getSize();
		int w = d.width * percent / 100;
		int h = percent;
		return new Dimension(w, h);
	}

	public void resetParameters() {
		biometricClient.reset();
	}

}
