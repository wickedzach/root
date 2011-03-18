package gavin;

import java.util.Enumeration;

public class ImportExport {
	public enum Type {
		Folder, Archive
	}

	public static void execute(Import src, Export dst) {
		Enumeration<String> tables = src.getTables();
		String table;
		int rowIndex, cellIndex;
		while (tables.hasMoreElements()) {
			table = tables.nextElement();
			dst.createTable(table, src.getHeaders(table));
			while (src.hasRow()) {
				rowIndex = src.nextRowIndex();
				dst.createRow(table, rowIndex);
				while (src.hasCell(table, rowIndex)) {
					cellIndex = src.nextCellIndex();
					dst.createCell(table, rowIndex, cellIndex, src.getCellValue(table, rowIndex, cellIndex));
				}
			}
		}
		src.finish();
		dst.flush();
	}

	public abstract class Common {
		Type type;

		public Common(Type type) {
			this.type = type;
		}
	}

	public abstract class Import extends Common {
		public Import(Type type) {
			super(type);
		}

		public abstract Enumeration<String> getTables();

		public abstract Enumeration<String> getHeaders(String table);

		public abstract boolean hasRow();

		public abstract int nextRowIndex();

		public abstract boolean hasCell(String table, int rowIndex);

		public abstract int nextCellIndex();

		public abstract Object getCellValue(String table, int rowIndex, int cellIndex);

		public abstract void finish();
	}

	public abstract class Export extends Common {
		public Export(Type type) {
			super(type);
		}

		public abstract void createTable(String table, Enumeration<String> headers);

		public abstract void createRow(String table, int rowIndex);

		public abstract void createCell(String table, int rowIndex, int cellIndex, Object cellValue);

		public abstract void flush();
	}
}