package gavin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONValue;

public final class SQLUtil {
	/**
	 * shrink SQL statement
	 * 
	 * @param sql
	 * @return shrink SQL from input
	 */
	private static final CharSequence shrink(CharSequence sql) {
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

	private static final String handle(CharSequence s) {
		String result = s.toString();
		result = result.replaceAll("\\s+", " ");
		result = result.replaceAll(" ?([\\+\\-\\*/%&\\|^\\!=<>~\\(\\)\\.,]) ?", "$1");
		// result = result.replaceAll(" ?(::) ?", "$1");
		// result = result.replaceAll("([Tt][Oo][Pp] \\d+) ", "$1");
		result = result.replaceAll("([Ss][Ee][Ll][Ee][Cc][Tt])\\*", "$1 *");
		result = result.replaceAll("\\*([Ff][Rr][Oo][Mm])", "* $1");
		return result;
	}

	public static final String read(File file) throws IOException {
		return read(file, Util.encoding());
	}

	public static final String read(File file, String encoding) throws IOException {
		return shrink(FileUtils.readFileToString(file, encoding)).toString();
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

	public static final boolean exists(Connection connection, String table, Map<String, ?> keys) throws SQLException {
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

	public static final String toJSONString(ResultSet rest) throws SQLException {
		List<Map<String, Object>> rows = new ArrayList<>();
		if (rest.next()) {
			ResultSetMetaData metadata = rest.getMetaData();
			String[] label = new String[metadata.getColumnCount()];
			int[] type = new int[metadata.getColumnCount()];
			for (int i = 0, j = 1; i < label.length; i++, j++) {
				label[i] = metadata.getColumnLabel(j);
				type[i] = metadata.getColumnType(j);
			}
			metadata = null;
			//
			Map<String, Object> row;
			rows.add(row = new HashMap<>(label.length));
			for (int i = 0, j = 1; i < label.length; i++, j++) {
				row.put(label[i], new ResultSetJSON(rest.getObject(j), type[i]));
			}
			while (rest.next()) {
				rows.add(row = new HashMap<>(label.length));
				for (int i = 0, j = 1; i < label.length; i++, j++) {
					row.put(label[i], new ResultSetJSON(rest.getObject(j), type[i]));
				}
			}
			row = null;
		}
		return JSONArray.toJSONString(rows);
	}

	static class ResultSetJSON implements JSONAware {
		static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
		static SimpleDateFormat BOTH_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Object value;
		int type;

		public ResultSetJSON(Object value, int type) {
			super();
			if (value != null) {
				switch (type) {
				case Types.NULL:
					value = null;
					break;
				case Types.DATE:
					value = DATE_FORMAT.format(((java.sql.Date) value).getTime());
					break;
				case Types.TIME:
					value = TIME_FORMAT.format(((java.sql.Time) value).getTime());
					break;
				case Types.TIMESTAMP:
					value = BOTH_FORMAT.format(((java.sql.Timestamp) value).getTime());
					break;
				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
					value = Hex.encodeHexString((byte[]) value);
					break;
				case Types.ARRAY:
					java.sql.Array arr = ((java.sql.Array) value);
					try (ResultSet rest = arr.getResultSet()) {
						List<Object> array = new ArrayList<>();
						while (rest.next()) {
							array.add(new ResultSetJSON(rest.getObject(2), arr.getBaseType()));
						}
						value = JSONArray.toJSONString(array);
						arr.free();
						array.clear();
					} catch (SQLException e) {}
					break;
				default:
					break;
				// BIT
				// TINYINT
				// SMALLINT
				// INTEGER
				// BIGINT
				// FLOAT
				// REAL
				// DOUBLE
				// NUMERIC
				// DECIMAL
				// CHAR
				// VARCHAR
				// LONGVARCHAR
				// OTHER
				// JAVA_OBJECT
				// DISTINCT
				// STRUCT
				// BLOB
				// CLOB
				// REF
				// DATALINK
				// BOOLEAN
				// ROWID
				// NCHAR
				// NVARCHAR
				// LONGNVARCHAR
				// NCLOB
				// SQLXML
				}
			}
			this.value = value;
			this.type = type;
		}

		public String toJSONString() {
			return JSONValue.toJSONString(value);
		}
	}

	public static void main(String[] args) throws Exception {
		String url, usr, pwd, sql;

		url = "jdbc:mysql://192.168.8.168:3306/misc";
		usr = "root";
		pwd = "";
		//
		// url = "jdbc:postgresql://192.168.8.168:5432/b2b";
		// usr = "postgres";
		// pwd = "";
		//

		// sql = "SELECT * FROM counter LIMIT 10;";
		// sql = "SELECT * FROM \"BMain\" LIMIT 10";
		try (Connection conn = DriverManager.getConnection(url, usr, pwd)) {
			try (ResultSet rest = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE" })) {
				System.out.println(toJSONString(rest));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			try (ResultSet rest = conn.getMetaData().getColumns(null, null, null, null)) {
				System.out.println(toJSONString(rest));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			//
			// try (Statement stat = conn.createStatement()) {
			// try (ResultSet rest = stat.executeQuery(sql)) {
			// System.out.println(toJSONString(rest));
			// } catch (Exception e) {
			// System.out.println(e.getMessage());
			// }
			// } catch (Exception e) {
			// System.out.println(e.getMessage());
			// }
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}