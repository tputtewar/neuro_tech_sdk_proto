package com.neuro.app.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsertImageTest {

	List<String> imageFileList = new ArrayList<String>();
	static Connection connection = null;

	public static void main(String[] args) throws SQLException {
		InsertImageTest imageTest = new InsertImageTest();
		connection = imageTest.getConnection();
//		imageTest.selectImageDetailsFromDB();
		imageTest.saveImageToDB();
		connection.close();
	}

	public Connection getConnection() {

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/neurodatabase", "root", "passw0rd");
		} catch (Exception e) {
			System.out.println("Error Occured While Getting the Connection: - " + e);
		}
		return connection;
	}

	public void selectImageDetailsFromDB() {
		Statement statement = null;

		try {

			// create the statement
			statement = connection.createStatement();

			// execute the query, and get a resultset
			ResultSet rs = statement.executeQuery("SELECT img_id,subject_title,type FROM subjectinfo");

			// iterate through the resultset
			while (rs.next()) {
				int subjectid = rs.getInt("img_id");
				String fileName = rs.getString("subject_title");
				String fileType = rs.getString("type");
				if (fileName != null && !imageFileList.contains(fileName)) {
					imageFileList.add(fileName);
				}
				// print the results
				System.out.format("%s, %s, %s\n", subjectid, fileName, fileType);
			}

		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

	public void saveImageToDB() {
		PreparedStatement statement = null;
		FileInputStream inputStream = null;
//		File folder = new File("D:\\neuroImgDB\\image122.png");
		String fileName = "D:\\neuroImgDB\\subject005.jpg";
		File imageFile = new File(fileName);
		try {
			// Implementing FilenameFilter to retrieve only image files

			/*
			 * FilenameFilter txtFileFilter = new FilenameFilter() { public boolean
			 * accept(File dir, String name) { if (name.endsWith(".jpg") ||
			 * name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
			 * return true; } else { return false; } } };
			 */

			// Passing txtFileFilter to listFiles() method to retrieve only txt files

//			File[] files = folder.listFiles(txtFileFilter);

//			for (File imageFile : files) {
			System.out.println(imageFile.getName());
//			if (!imageFileList.contains(imageFile.getName())) {
			inputStream = new FileInputStream(imageFile);
			statement = connection.prepareStatement(
					"insert into user (name, gender, dob, phone, email, empid, department, misc, picurl, pictemplate) "
							+ "values(?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, "Trupti Puttewar");
			statement.setString(2, "Female");
			String dateString = "1982-07-28";
			SimpleDateFormat formatter = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
			Date dob = formatter.parse(new Date().toString());

			Timestamp nowTimestamp = new Timestamp(dob.getTime());
			statement.setTimestamp(3, nowTimestamp);
			statement.setString(4, "9814562783");
			statement.setString(5, "rucha@gmail.com");
			statement.setInt(6, 3);
			statement.setString(7, "Software Engg");
			statement.setString(8, "AAAAA");
			statement.setString(9, fileName);
			statement.setBinaryStream(10, (InputStream) inputStream, (int) (imageFile.length()));

			statement.executeUpdate();
//			}
//			}

		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException: - " + e);
		} catch (SQLException e) {
			System.out.println("SQLException: - " + e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException Finally: - " + e);
			}

		}

	}

}