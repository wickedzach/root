package gavin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class SQLUtil {
	/**
	 * shrink SQL statement
	 * 
	 * @param sql
	 * @return shrink SQL from input
	 */
	private static CharSequence shrink(CharSequence sql) {
		StringBuilder m = new StringBuilder();
		StringBuilder s = new StringBuilder();
		int state = 0;
		char c;
		for (int i = 0, j = sql.length(), n; i < j; i++) {
			c = sql.charAt(i);
			if (state == 1) { // in '
				m.append(c);
				if (c == '\'') {
					state = 0;
					continue;
				}
			} else if (state == 2) { // in /**/
				if (c == '*') {
					if ((n = i + 1) < j) {
						if (sql.charAt(n) == '/') {
							i++; // ignore next
							state = 0;
							continue;
						}
					}
				}
			} else if (state == 3) { // after --
				if (c == '\r' || c == '\n') {
					state = 0;
					continue;
				}
			} else {
				if (c == '\'') { // is going to '
					m.append(handle(s)).append(c);
					s = new StringBuilder();
					state = 1;
					continue;
				} else if (c == '/') { // is going to /**/
					if ((n = i + 1) < j) {
						if (sql.charAt(n) == '*') {
							i++; // ignore next
							state = 2;
							continue;
						}
					}
				} else if (c == '-') { // is going to --
					if ((n = i + 1) < j) {
						if (sql.charAt(n) == '-') {
							i++; // ignore next
							state = 3;
							continue;
						}
					}
				} else if (c == '\r' || c == '\n') { // if CR LF ignore this
					continue;
				} else if ((c == ' ' || c == '\t') && (n = i + 1) < j && (sql.charAt(n) == ' ' || sql.charAt(n) == '\t')) {
					// if this is space or tab and next is space or tab ignore this
					continue;
				}
				if (c == '\t') {
					c = ' ';
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
		// result = result.replaceAll(" ?(::) ?", "$1");
		// result = result.replaceAll("([Tt][Oo][Pp] \\d+) ", "$1");
		result = result.replaceAll("([Ss][Ee][Ll][Ee][Cc][Tt])\\*", "$1 *");
		result = result.replaceAll("\\*([Ff][Rr][Oo][Mm])", "* $1");
		return result;
	}

	public static String read(File file) throws IOException {
		return read(file, Util.getEncoding());
	}

	public static String read(File file, String encoding) throws IOException {
		return shrink(IOUtil.read(file, encoding)).toString();
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
		Util.close(result, statement);
		return flag;
	}
}