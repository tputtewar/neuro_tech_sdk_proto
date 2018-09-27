package com.neuro.app.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.neuro.app.surveillance.SurveillanceTools;
import com.neuro.app.util.CameraType;
import com.neuro.app.util.NotificationStatus;
import com.neuro.app.util.Roles;
import com.neurotec.devices.NDevice;
import com.neurotec.images.NImage;

public class DBService {
	private static DBConnection instance;

	public DBService() throws Exception {
		super();
		instance = DBConnection.getInstance();
	}

	public TreeMap<String, ArrayList<Object>> getImageList(String tableName) throws Exception {
		HashMap<String, ArrayList<Object>> imageDBList = DBConnection.getInstance().getImageDetailsFromDB(tableName);

		// TreeMap keeps all entries in sorted order
		TreeMap<String, ArrayList<Object>> sorted = new TreeMap<String, ArrayList<Object>>(imageDBList);
		System.out.println("HashMap after sorting by keys in ascending order ");
		return sorted;
	}

	@SuppressWarnings("rawtypes")
	public void saveDevicesInDB() throws Exception {
		List<String> deviceDBList = instance.getDevicesFromDB();
		HashMap<String, EnumSet> deviceList = new HashMap<String, EnumSet>();
		for (NDevice device : SurveillanceTools.getInstance().getSurveillance().getDeviceManager().getDevices()) {
			if (device.getDisplayName() != null && !deviceDBList.contains(device.getDisplayName())) {
				deviceList.put(device.getDisplayName(), device.getDeviceType());
			}
		}
		if (!deviceList.isEmpty()) {
			instance.saveDeviceInDB(deviceList);
		}
	}

	public void saveInsideOutInfoToDB(String subjectId, int score, int age, String gender, int isLive,
			String deviceType) throws Exception {
		HashMap<String, String> subjectDBList = instance.checkIfSubjectPresentInInsideOutInfoInDB(subjectId, age,
				gender);
		if (subjectDBList.isEmpty()) {
			instance.insertInsideOutInfoInDB(subjectId, score, age, gender, isLive, deviceType);
		}
	}

	public Timestamp getTimestamp(Date date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date now = sdf.parse(date.toString());

		Timestamp nowTimestamp = new Timestamp(now.getTime());
		return nowTimestamp;
	}

	public void saveSubjectInfoForUnknownToDB(String subjectId, NImage image, String deviceType) throws Exception {
		String dbSubject = instance.checkIfSubjectPresentInUserInfoInDB(subjectId, deviceType);
		if (dbSubject == null) {
			instance.insertSubjectInfoForUnknownInDB(subjectId, image, deviceType);
		}
	}

	public String getDeviceType(String type) {
		String deviceType = null;
		if (type.equalsIgnoreCase("OutWard Watchlist")) {
			deviceType = "OUT";
		} else if (type.equalsIgnoreCase("InWard Watchlist")) {
			deviceType = "IN";
		}
		return deviceType;
	}

	public void saveTheNotification(Roles target, String origin, String title, String matchedId,
			NotificationStatus status, Timestamp timeStampOut) {
		instance.insertNotificationInDB(target, origin, title, matchedId, status, timeStampOut);
	}

	public void markAttendanceInHistory(CameraType cameratype, String name, Timestamp timeStampType,
			Timestamp timestamp) {
		instance.insertAttendanceInHistoryInDB(cameratype, name, timeStampType, timestamp);

	}

}
