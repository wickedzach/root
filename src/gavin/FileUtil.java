package gavin;

import java.io.File;
import java.io.FileFilter;

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
}