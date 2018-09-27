package com.neuro.app.dao;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.neuro.app.util.CameraType;
import com.neuro.app.util.NotificationStatus;
import com.neuro.app.util.Roles;
import com.neurotec.devices.NDeviceType;
import com.neurotec.images.NImage;

public final class DBConnection {
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private Connection connection = null;
	private HashMap<String, ArrayList<Object>> imageFileMap = null;
	private static DBConnection instance;
	private static String imagePath = "D:\\neuroImgDB";

	public static DBConnection getInstance() throws Exception {
		synchronized (DBConnection.class) {
			if (instance == null) {
				instance = new DBConnection();
			}
			return instance;
		}
	}

	public static void main(String[] args) throws Exception {
		DBConnection db = new DBConnection();
		try {
			db.connectDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DBConnection() throws Exception {
		imageFileMap = new HashMap<String, ArrayList<Object>>();
		connection = connectDataBase();
	}

	public Connection connectDataBase() throws Exception {

		try {
			if (connection == null) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/neurodatabase?autoReconnect=true&useSSL=false", "root",
						"passw0rd");
			}
		} catch (Exception e) {
			System.out.println("Error Occured While Getting the Connection: - " + e);
		}
		return connection;
	}

	/*
	 * public HashMap<String, ArrayList<Object>> getImageDetailsFromDB(String
	 * tableName) { // HashMap<String, <String, Object>> imageFileMap = new
	 * HashMap<String, <String, Object>>(); ArrayList<Object> innerMap = null;
	 * String fileName = null; int subjectid = 0; Blob fileBlob = null; String type
	 * = null; try {
	 * 
	 * // create the statement statement = connection.createStatement();
	 * 
	 * // execute the query, and get a resultset ResultSet rs =
	 * statement.executeQuery("SELECT * FROM " + tableName);
	 * 
	 * // iterate through the resultset while (rs.next()) { innerMap = new
	 * ArrayList<Object>(); subjectid = rs.getInt("img_id");
	 * 
	 * if (tableName.equalsIgnoreCase("inimageStore") ||
	 * tableName.equalsIgnoreCase("outimageStore")) { fileName =
	 * rs.getString("subjectid"); fileBlob = rs.getBlob("template");
	 * innerMap.add(fileName); } else if (tableName.equalsIgnoreCase("subjectinfo"))
	 * { fileName = rs.getString("subject_title"); fileBlob =
	 * rs.getBlob("subject_img"); type = rs.getString("type");
	 * innerMap.add(fileName);
	 * 
	 * } if (fileName != null) { byte[] imgbytes = fileBlob.getBytes(1, (int)
	 * fileBlob.length()); Buffer buffer = ByteBuffer.wrap(imgbytes);
	 * innerMap.add(buffer);
	 * 
	 * } if (tableName.equalsIgnoreCase("subjectinfo")) { innerMap.add(type); } //
	 * print the results System.out.format("%s, %s\n", subjectid, fileName);
	 * 
	 * imageFileMap.put(fileName, innerMap); }
	 * 
	 * } catch (SQLException e) { System.out.println("SQLException: - " + e); }
	 * finally { try { if (statement != null) { statement.close(); } } catch
	 * (SQLException e) { System.out.println("SQLException Finally: - " + e); }
	 * 
	 * } return imageFileMap; }
	 */

	public HashMap<String, ArrayList<Object>> getImageDetailsFromDB(String tableName) {
//		HashMap<String, <String, Object>> imageFileMap = new HashMap<String, <String, Object>>();
		ArrayList<Object> innerMap = null;
		String fileName = null;
		int subjectid = 0;
		Blob fileBlob = null;
		String type = null;
		try {

			// create the statement
			statement = connection.createStatement();

			// execute the query, and get a resultset
			ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

			// iterate through the resultset
			while (rs.next()) {
				innerMap = new ArrayList<Object>();
				subjectid = rs.getInt("id");

				if (tableName.equalsIgnoreCase("inimageStore") || tableName.equalsIgnoreCase("outimageStore")) {
					fileName = rs.getString("subjectid");
					fileBlob = rs.getBlob("template");
					innerMap.add(fileName);
				} else if (tableName.equalsIgnoreCase("subjectinfo")) {
					fileName = rs.getString("subject_title");
					fileBlob = rs.getBlob("subject_img");
					type = rs.getString("type");
					innerMap.add(fileName);

				} else if (tableName.equalsIgnoreCase("user")) {
					fileName = rs.getString("picname");
					fileBlob = rs.getBlob("pictemplate");
//					type = rs.getString("type");
					innerMap.add(fileName);

				}
				if (fileName != null) {
					byte[] imgbytes = fileBlob.getBytes(1, (int) fileBlob.length());
					Buffer buffer = ByteBuffer.wrap(imgbytes);
					innerMap.add(buffer);

				}
				if (tableName.equalsIgnoreCase("subjectinfo")) {
					innerMap.add(type);
				}
				// print the results
				System.out.format("%s, %s\n", subjectid, fileName);

				imageFileMap.put(fileName, innerMap);
			}

		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}
		return imageFileMap;
	}

	public void saveImageToDB() {

		FileInputStream inputStream = null;
		File folder = new File(imagePath);
		try {
			// Implementing FilenameFilter to retrieve only image files

			FilenameFilter imgFileFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")
							|| name.endsWith(".bmp")) {
						return true;
					} else {
						return false;
					}
				}
			};

			// Passing imgFileFilter to listFiles() method to retrieve only image files

			File[] files = folder.listFiles(imgFileFilter);

			for (File imageFile : files) {
				System.out.println(imageFile.getName());
				if (!imageFileMap.containsKey(imageFile.getName())) {
					inputStream = new FileInputStream(imageFile);
					preparedStatement = connection
							.prepareStatement("insert into subjectinfo (subject_title, subject_img) " + "values(?,?)");
					preparedStatement.setString(1, imageFile.getName());
					preparedStatement.setBinaryStream(2, (InputStream) inputStream, (int) (imageFile.length()));

					preparedStatement.executeUpdate();
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException: - " + e);
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {

			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

// You need to close the resultSet,statement, connection
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}

			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			System.out.println("SQLException close(): - " + e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void saveDeviceInDB(HashMap<String, EnumSet> deviceList) {

		try {

			for (Entry<String, EnumSet> mapping : deviceList.entrySet()) {

				for (Iterator it = mapping.getValue().iterator(); it.hasNext();) {
					preparedStatement = connection
							.prepareStatement("insert into camera (name, location,type) " + "values(?,?,?)");
					NDeviceType deviceType = (NDeviceType) it.next();
					if (deviceType.getValue() == 64) {

						System.out.println(mapping.getValue() + " ::: " + deviceType.getValue());
						preparedStatement.setString(1, mapping.getKey());
						preparedStatement.setString(2, deviceType.name());
						preparedStatement.setInt(3, deviceType.getValue());
						preparedStatement.executeUpdate();

					}
				}

			}
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {

			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

	public List<String> getDevicesFromDB() {
		List<String> deviceNameList = new ArrayList<String>();

		String devicename = null;
		int deviceid = 0;
		String devicetype = null;
		String location = null;
		try {

			// create the statement
			statement = connection.createStatement();

			// execute the query, and get a resultset
			ResultSet rs = statement.executeQuery("SELECT * FROM camera");

			// iterate through the resultset
			while (rs.next()) {
				deviceid = rs.getInt("id");
				devicename = rs.getString("name");

				if (devicename != null) {
					deviceNameList.add(devicename);
				}
				devicetype = rs.getString("type");
				location = rs.getString("location");
				System.out.format("%s,%s, %s, %s\n", deviceid, devicename, devicetype, location);
			}

		} catch (SQLException e) {
			System.out.println("SQLException catch: - " + e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}
		return deviceNameList;
	}

	public HashMap<String, String> checkIfSubjectPresentInInsideOutInfoInDB(String subjectId, int age, String gender) {
		HashMap<String, String> dbResult = new HashMap<String, String>();
		try {
			String query = "SELECT * FROM insideoutinfo where subjectid='" + subjectId + "' AND age=" + age
					+ " AND gender='" + gender + "';";
			System.out.println(query);
			// create the statement
			statement = connection.createStatement();

			// execute the query, and get a resultset
			ResultSet rs = statement.executeQuery(query);

			// iterate through the resultset
			while (rs.next()) {
				String dbSubjectid = rs.getString("subjectid");
				int dbScore = rs.getInt("score");
				int dbAge = rs.getInt("age");
				int dbIslive = rs.getInt("islive");
				String dbGender = rs.getString("gender");
				String dbType = rs.getString("type");
				Date dbTimeStamp = rs.getDate("timestamp");

				dbResult.put("subjectid", dbSubjectid);
				dbResult.put("score", dbScore + "");
				dbResult.put("age", dbAge + "");
				dbResult.put("islive", dbIslive + "");
				dbResult.put("gender", dbGender);
				dbResult.put("type", dbType);
				dbResult.put("timestamp", dbTimeStamp.toString());

			}
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}
		return dbResult;
	}

	public String checkIfSubjectPresentInUserInfoInDB(String picname, String type) {
		String dbSubjectid = null;
		try {
			String query = "SELECT name FROM user where picname='" + picname + "';";
			System.out.println(query);
			// create the statement
			statement = connection.createStatement();

			// execute the query, and get a resultset
			ResultSet rs = statement.executeQuery(query);

			// iterate through the resultset
			while (rs.next()) {
				dbSubjectid = rs.getString("picname");

			}
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}
		return dbSubjectid;
	}

	public void insertInsideOutInfoInDB(String subjectId, int score, int age, String gender, int isLive,
			String deviceType) {

		try {

			preparedStatement = connection.prepareStatement(
					"insert into insideoutinfo (subjectid, score, age, gender, islive, type, timestamp) "
							+ "values(?,?,?,?,?,?,?)");
			preparedStatement.setString(1, subjectId);
			preparedStatement.setInt(2, score);
			preparedStatement.setInt(3, age);
			preparedStatement.setString(4, gender);
			preparedStatement.setInt(5, isLive);
			preparedStatement.setString(6, deviceType);
			preparedStatement.setTimestamp(7, getTimestamp());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} catch (ParseException e) {
			System.out.println("ParseException: - " + e);
		} finally {

			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

	private Timestamp getTimestamp() throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
		Date now = (Date) sdf.parse(new java.util.Date().toString());

		Timestamp nowTimestamp = new Timestamp(now.getTime());
		return nowTimestamp;
	}

	public void insertSubjectInfoForUnknownInDB(String subjectId, NImage image, String deviceType) {

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write((RenderedImage) image.toImage(), "png", os);
			InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
			if (!imageFileMap.containsKey(subjectId)) {
				preparedStatement = connection.prepareStatement(
						"insert into unidentifiedperson (name, picurl,pictemplate,approve,purpose,picname) "
								+ "values(?,?,?,?,?,?)");
				preparedStatement.setString(1, subjectId);
				preparedStatement.setString(2, "null");
				preparedStatement.setBinaryStream(3, (InputStream) inputStream);
				preparedStatement.setString(4, "unIdentified");
				preparedStatement.setString(5, "null");
				preparedStatement.setString(6, subjectId);
				preparedStatement.executeUpdate();
			}

		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException: - " + e);
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} catch (IOException e) {
			System.out.println("IOException: - " + e);
		} finally {

			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

	public void insertNotificationInDB(Roles target, String origin, String title, String description,
			NotificationStatus status, Timestamp timeStamp) {

		try {

			preparedStatement = connection
					.prepareStatement("insert into notification (target, origin, title,description,status,timestamp) "
							+ "values(?,?,?,?,?,?)");
			preparedStatement.setString(1, target.toString());
			preparedStatement.setString(2, origin);
			preparedStatement.setString(3, title);
			preparedStatement.setString(4, description);
			preparedStatement.setString(5, status.toString());
//			preparedStatement.setTimestamp(6, getTimestamp());timeStampOut
			preparedStatement.setTimestamp(6, timeStamp);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {

			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

	public void insertAttendanceInHistoryInDB(CameraType cameraType, String matchedId,  Timestamp timeStampType,
			Timestamp timestamp) {

		try {

			preparedStatement = connection
					.prepareStatement("insert into history (cameratype, name, timein, timeout,timestamp) "
							+ "values(?,?,?,?,?)");
			preparedStatement.setString(1, cameraType.toString());
			preparedStatement.setString(2, matchedId);
			if(cameraType.toString().equalsIgnoreCase("OUT")) {
				preparedStatement.setTimestamp(3, null);
				preparedStatement.setTimestamp(4, timeStampType);
			}else {
				preparedStatement.setTimestamp(3, timeStampType);
				preparedStatement.setTimestamp(4, null);
			}
			preparedStatement.setTimestamp(5, timestamp);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {

			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

}