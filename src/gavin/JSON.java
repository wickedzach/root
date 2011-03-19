package gavin;

import gavin.utilities.OutputStreamWrap;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class JSON {
	private static byte[] lbrace = { 123 };
	private static byte[] rbrace = { 125 };
	private static byte[] lbracket = { 91 };
	private static byte[] rbracket = { 93 };
	private static byte[] colon = { 58 };
	private static byte[] comma = { 44 };
	private static byte[] quote = { 34 };
	private static byte[] true$ = { 116, 114, 117, 101 };
	private static byte[] false$ = { 102, 97, 108, 115, 101 };
	private static byte[] null$ = { 110, 117, 108, 108 };

	public static void convert(ResultSet source, OutputStream out) throws SQLException, IOException {
		OutputStreamWrap w = new OutputStreamWrap(out);

		// prepare columns information
		ResultSetMetaData metadata = source.getMetaData();
		int count = metadata.getColumnCount();
		byte[][] label = new byte[count][];
		int[] type = new int[count];
		for (int i = 0, j = 1; i < count; i++, j++) {
			label[i] = metadata.getColumnLabel(j).getBytes();
			type[i] = metadata.getColumnType(j);
		}

		w.w(lbracket);
		if (source.next()) {
			w.w(lbrace);
			convert(label[0], type[0], source, 1, out);
			for (int i = 1, j = 2; i < count; i++, j++) {
				w.w(comma);
				convert(label[i], type[i], source, j, out);
			}
			w.w(rbrace);
			while (source.next()) {
				w.w(comma).w(lbrace);
				convert(label[0], type[0], source, 1, out);
				for (int i = 1, j = 2; i < count; i++, j++) {
					w.w(comma);
					convert(label[i], type[i], source, j, out);
				}
				w.w(rbrace);
			}
		}
		w.w(rbracket);
	}

	private static void convert(byte[] label, int type, ResultSet source, int columnIndex, OutputStream out) throws IOException, SQLException {
		OutputStreamWrap w = new OutputStreamWrap(out);

		Object value = source.getObject(columnIndex);
		w.w(quote).w(label).w(quote).w(colon);
		if (value == null) {
			w.w(null$);
			return;
		}
		switch (type) {
		// start of boolean
		case Types.BOOLEAN:
			w.w(source.getBoolean(columnIndex) ? true$ : false$);
			break;
		// end of boolean
		// ----------------------------------------
		// start of number
		case Types.BIT:
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
		case Types.NUMERIC:
		case Types.DECIMAL:
			w.w(String.valueOf(value).getBytes());
			break;
		// end of number
		// ----------------------------------------
		// start of string
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
			w.w(quote).w(escape(value).getBytes()).w(quote);
			break;
		// end of string
		// ----------------------------------------
		// start other
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			w.w(quote).w(String.valueOf(value).getBytes()).w(quote);
			break;
		// end other
		// ----------------------------------------
		// start of unsupported
		// case Types.CLOB:
		// case Types.NCLOB:
		// case Types.NULL:
		// case Types.OTHER:
		// case Types.JAVA_OBJECT:
		// case Types.DISTINCT:
		// case Types.STRUCT:
		// case Types.ARRAY:
		// case Types.REF:
		// case Types.DATALINK:
		// case Types.ROWID:
		// case Types.SQLXML:
		// case Types.BINARY:
		// case Types.VARBINARY:
		// case Types.LONGVARBINARY:
		// case Types.BLOB:
		// end of unsupported
		default:
			w.w(null$);
		}
	}

	public static String escape(Object value) {
		String val = value.toString().replaceAll("[\\\"\\\\/]", "\\\\$0");
		val = val.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
		val = val.replace("\f", "\\f").replace("\b", "\\b");
		return Util.unicode(val);
	}

	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		Class.forName("net.sourceforge.jtds.jdbc.Driver");
		String url = "jdbc:jtds:sqlserver://127.0.0.1:1433/psOutLine";
		String user = "sa";
		String pass = "1qaz2wsx";
		Connection connection = DriverManager.getConnection(url, user, pass);
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM test");
		convert(result, System.out);
		result.close();
		statement.close();
		connection.close();
	}
}