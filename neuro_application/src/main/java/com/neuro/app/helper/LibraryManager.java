package com.neuro.app.helper;

import java.lang.reflect.Field;

import com.neuro.app.util.Utils;
import com.sun.jna.Platform;

public final class LibraryManager {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String WIN32_X86 = "Win32_x86-64";
	private static final String WIN64_X64 = "Win64_x64";
	private static final String LINUX_X86 = "Linux_x86";
	private static final String LINUX_X86_64 = "Linux_x86_64";

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static void initLibraryPath() {
		String libraryPath = getLibraryPath();
		String jnaLibraryPath = System.getProperty("jna.library.path");
		if (Utils.isNullOrEmpty(jnaLibraryPath)) {
			System.setProperty("jna.library.path", libraryPath.toString());
		} else {
			System.setProperty("jna.library.path", String.format("%s%s%s", jnaLibraryPath, Utils.PATH_SEPARATOR, libraryPath.toString()));
		}
		System.setProperty("java.library.path",String.format("%s%s%s", System.getProperty("java.library.path"), Utils.PATH_SEPARATOR, libraryPath.toString()));
		if (Platform.isMac()) {
			String jnaPlatformLibraryPath = System.getProperty("jna.platform.library.path");
			if (Utils.isNullOrEmpty(jnaPlatformLibraryPath)) {
				System.setProperty("jna.platform.library.path", libraryPath.toString());
			} else {
				System.setProperty("jna.platform.library.path", String.format("%s%s%s", jnaPlatformLibraryPath, Utils.PATH_SEPARATOR, libraryPath.toString()));
			}
			System.setProperty("java.platform.library.path",String.format("%s%s%s", System.getProperty("java.platform.library.path"), Utils.PATH_SEPARATOR, libraryPath.toString()));
		}
		try {
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

	public static String getLibraryPath() {
		StringBuilder path = new StringBuilder();
		int index = Utils.getWorkingDirectory().lastIndexOf(Utils.FILE_SEPARATOR);
		if (index == -1) {
			return null;
		}
		String workingDir = Utils.getWorkingDirectory();
		String part = Utils.getWorkingDirectory().substring(0, index);
		if (Platform.isWindows()) {
			if (part.endsWith("Bin")) {
				path.append(part);
				path.append(Utils.FILE_SEPARATOR);
				path.append(Platform.is64Bit() ? WIN64_X64 : WIN32_X86);
			}else {
				path.append(workingDir);
				path.append(Utils.FILE_SEPARATOR);
				path.append("bin");
				path.append(Utils.FILE_SEPARATOR);
				path.append(Platform.is64Bit() ? WIN64_X64 : WIN32_X86);
			}
		} else if (Platform.isLinux()) {
			index = part.lastIndexOf(Utils.FILE_SEPARATOR);
			if (index == -1) {
				return null;
			}
			part = part.substring(0, index);
			path.append(part);
			path.append(Utils.FILE_SEPARATOR);
			path.append("Lib");
			path.append(Utils.FILE_SEPARATOR);
			path.append(Platform.is64Bit() ? LINUX_X86_64 : LINUX_X86);
		} else if (Platform.isMac()) {
			index = part.lastIndexOf(Utils.FILE_SEPARATOR);
			if (index == -1) {
				return null;
			}
			part = part.substring(0, index);
			path.append(part);
			path.append(Utils.FILE_SEPARATOR);
			path.append("Frameworks");
			path.append(Utils.FILE_SEPARATOR);
			path.append("MacOSX");
		}
		return path.toString();
	}

}
