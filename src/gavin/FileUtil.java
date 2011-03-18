package gavin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

	public static void delete(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					delete(child);
				}
			}
		}
		file.delete();
	}

	public static void delete(File file, FileFilter... filter) {
		boolean delete = accept(file, filter);
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				if (delete) {
					for (File child : children) {
						delete(child);
					}
				} else {
					for (File child : children) {
						delete(child, filter);
					}
				}
			}
		}
		if (delete) {
			file.delete();
		}
	}

	public static boolean accept(File file, FileFilter... filters) {
		for (FileFilter filter : filters) {
			if (!filter.accept(file)) {
				return false;
			}
		}
		return true;
	}

	public static String getExtention(File file) {
		String name = file.getName();
		int index = name.lastIndexOf('.');
		return index != -1 ? name.substring(index + 1).toLowerCase() : null;
	}

	// public static void convert(File file) throws IOException {
	// if (file.isDirectory()) {
	// File[] children = file.listFiles();
	// if (children != null) {
	// for (File child : children) {
	// convert(child);
	// }
	// }
	// }
	// if (file.isFile()) {
	// String text = IOUtil.read(file, "big5");
	// OutputStream output = new FileOutputStream(file);
	// output.write(text.getBytes("UTF-8"));
	// output.flush();
	// output.close();
	// }
	// }
	//
	// public static void main(String[] args) throws IOException {
	// File d = new File("C:\\Documents and Settings\\gavin\\桌面\\aloha\\src");
	// convert(d);
	// }
}