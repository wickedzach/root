package gavin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SQLUtil {
	private static CharSequence process(CharSequence input) {
		StringBuilder m = new StringBuilder();
		StringBuilder s = new StringBuilder();
		int state = 0;
		char c;
		for (int i = 0, j = input.length(); i < j; i++) {
			c = input.charAt(i);
			if (state == 1) { // in '
				s.append(c);
				if (c == '\'') {
					m.append(s);
					s = new StringBuilder();
					state = 0;
					continue;
				}
			} else if (state == 2) { // in /**/
				if (c == '*') {
					if ((i + 1) < j) {
						if (input.charAt(i + 1) == '/') {
							i++; // ignore next
							state = 0;
							continue;
						}
					}
				}
			} else if (state == 3) {// after --
				if (c == '\r' || c == '\n') {
					state = 0;
					continue;
				}
			} else {
				if (c == '\'') { // is going to '
					s.append(c);
					m.append(handle(s));
					s = new StringBuilder();
					state = 1;
					continue;
				} else if (c == '/') { // is going to /**/
					if ((i + 1) < j) {
						if (input.charAt(i + 1) == '*') {
							i++; // ignore next
							state = 2;
							continue;
						}
					}
				} else if (c == '-') { // is going to --
					if ((i + 1) < j) {
						if (input.charAt(i + 1) == '-') {
							i++; // ignore next
							state = 3;
							continue;
						}
					}
				} else if (c == '\r' || c == '\n') {// other state if CR LF ignore
					continue;
				}
				s.append(c);
			}
		}
		if (state == 0) {
			m.append(handle(s));
			s = null;
		}
		return m;
	}

	private static String handle(CharSequence s) {
		String result = s.toString();
		result = result.replaceAll("\\s+", " ");
		result = result.replaceAll(" ?([\\+\\-\\*/%&\\|^\\!=<>~\\(\\)\\.,]) ?", "$1");
		result = result.replaceAll(" ?(::) ?", "$1");
		result = result.replaceAll("([Tt][Oo][Pp] \\d+) ", "$1");
		result = result.replaceAll("([Ss][Ee][Ll][Ee][Cc][Tt])\\*", "$1 *");
		result = result.replaceAll("\\*([Ff][Rr][Oo][Mm])", "* $1");
		return result;
	}

	public static String read(File file) throws IOException {
		return read(file, Charset.defaultCharset().name());
	}

	public static String read(File file, String encoding) throws IOException {
		return process(IOUtil.read(file, encoding)).toString();
	}

	// temp
	public static Object[] scalar(Statement statement, int length, String sql) throws SQLException {
		ResultSet rs = statement.executeQuery(sql);
		if (rs.next()) {
			if (length == -1) {
				length = rs.getMetaData().getColumnCount();
			}
			Object[] result = new Object[length];
			for (int i = 0, j = 1; i < result.length; i++, j++) {
				result[i] = rs.getObject(j);
			}
			rs.close();
			return result;
		}
		rs.close();
		return null;
	}

	public static void insert(Connection connection, String table, Map<String, ?> values) {
		// TODO
	}

	public static void update(Connection connection, String table, Map<String, ?> keys, Map<String, ?> values) {
		// TODO
	}

	public static void delete(Connection connection, String table, Map<String, ?> keys) {
		// TODO
	}

	public static boolean exists(Connection connection, String table, Map<String, ?> keys) throws SQLException {
		StringBuilder s = new StringBuilder();
		s.append("SELECT CASE WHEN EXISTS(SELECT 0 FROM ");
		s.append(table);
		boolean flag = keys != null && keys.size() > 0;

		if (flag) {
			Iterator<String> iter = keys.keySet().iterator();
			s.append(" WHERE ").append(iter.next()).append("=?");
			while (iter.hasNext()) {
				s.append(" AND ").append(iter.next()).append("=?");
			}
		}
		s.append(") THEN 1 ELSE NULL END");
		PreparedStatement statement = connection.prepareStatement(s.toString());
		if (flag) {
			Iterator<?> iter = keys.values().iterator();
			statement.setObject(1, iter.next());
			int i = 2;
			while (iter.hasNext()) {
				statement.setObject(i++, iter.next());
			}
		}
		ResultSet result = statement.executeQuery();
		result.next();
		flag = result.getBoolean(1);
		result.close();
		statement.close();

		return flag;
	}

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		Class.forName("net.sourceforge.jtds.jdbc.Driver");
		String url = "jdbc:jtds:sqlserver://127.0.0.1:1433/psOutLine";
		String user = "sa";
		String pass = "1qaz2wsx";
		Connection connection = DriverManager.getConnection(url, user, pass);
		Map<String, Object> keys;

		keys = new HashMap<String, Object>();
		keys.put("Miscode", "AA");
		System.out.println(exists(connection, "syMisCode", keys));

		keys = new HashMap<String, Object>();
		keys.put("Miscode", "XX");
		System.out.println(exists(connection, "syMisCode", keys));

		connection.close();
	}
}