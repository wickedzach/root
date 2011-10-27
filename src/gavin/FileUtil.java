package gavin;

import java.io.File;
import java.io.FileFilter;

public final class FileUtil {

	public static final void delete(File file) {
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

	public static final void delete(File file, FileFilter... filter) {
		boolean delete = accept(file, true, filter);
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

	/**
	 * 
	 * @param file
	 *            target file to filter
	 * @param flag
	 *            if true any accept than true else any not accept than false
	 * @param filters
	 *            the filters
	 * @return
	 */
	public static final boolean accept(File file, boolean flag, FileFilter... filters) {
		for (FileFilter filter : filters) {
			if (flag == filter.accept(file)) {
				return flag;
			}
		}
		return !flag;
	}

	public static final String getExtention(File file) {
		String name = file.getName();
		int index = name.lastIndexOf('.');
		return index != -1 ? name.substring(index + 1).toLowerCase() : null;
	}
}