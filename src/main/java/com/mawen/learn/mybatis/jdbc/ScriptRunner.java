package com.mawen.learn.mybatis.jdbc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public class ScriptRunner {

	private static final String LINE_SEPARATOR = System.lineSeparator();

	private static final String DEFAULT_DELIMITER = ";";

	private static final Pattern DELIMITER_PATTERN = Pattern.compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE);
	private static final Logger log = LoggerFactory.getLogger(ScriptRunner.class);

	private final Connection connection;

	private boolean stopOnError;
	private boolean throwWarning;
	private boolean autoCommit;
	private boolean sendFullScript;
	private boolean removeCRs;
	private boolean escapeProcessing = true;
	private String delimiter = DEFAULT_DELIMITER;
	private boolean fullLineDelimiter;

	private PrintWriter logWriter = new PrintWriter(System.out);
	private PrintWriter errorLogWriter = new PrintWriter(System.err);

	public ScriptRunner(Connection connection) {
		this.connection = connection;
	}

	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public void setThrowWarning(boolean throwWarning) {
		this.throwWarning = throwWarning;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public void setSendFullScript(boolean sendFullScript) {
		this.sendFullScript = sendFullScript;
	}

	public void setRemoveCRs(boolean removeCRs) {
		this.removeCRs = removeCRs;
	}

	public void setEscapeProcessing(boolean escapeProcessing) {
		this.escapeProcessing = escapeProcessing;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void setFullLineDelimiter(boolean fullLineDelimiter) {
		this.fullLineDelimiter = fullLineDelimiter;
	}

	public void setLogWriter(PrintWriter logWriter) {
		this.logWriter = logWriter;
	}

	public void setErrorLogWriter(PrintWriter errorLogWriter) {
		this.errorLogWriter = errorLogWriter;
	}

	public void runScript(Reader reader) {
		setAutoCommit();

		try {
			if (sendFullScript) {
				executeFullScript(reader);
			}
			else {
				executeLineByLine(reader);
			}
		}
		finally {
			rollbackConnection();
		}
	}

	private void executeFullScript(Reader reader) {
		StringBuilder script = new StringBuilder();
		try {
			BufferedReader lineReader = new BufferedReader(reader);
			String line;
			while ((line = lineReader.readLine()) != null) {
				script.append(line);
				script.append(LINE_SEPARATOR);
			}
			String command = script.toString();
			println(command);
			executeStatement(command);
			commitConnection();
		}
		catch (Exception e) {
			String message = "Error executing: " + script + ". Cause: " + e;
			printlnError(message);
			throw new RuntimeSqlException(e);
		}
	}

	private void executeLineByLine(Reader reader) {
		StringBuilder command = new StringBuilder();
		try {
			BufferedReader lineReader = new BufferedReader(reader);
			String line;
			while ((line = lineReader.readLine()) != null) {
				handleLine(command, line);
			}
			commitConnection();
			checkForMissingLineTerminator(command);
		}
		catch (Exception e) {
			String message = "Error executing: " + command + ". Cause: " + e;
			printlnError(message);
			throw new RuntimeSqlException(message, e);
		}
	}

	public void closeConnection() {
		try {
			connection.close();
		}
		catch (Exception ignored) {

		}
	}

	private void setAutoCommit() {
		try {
			if (autoCommit != connection.getAutoCommit()) {
				connection.setAutoCommit(autoCommit);
			}
		}
		catch (Throwable t) {
			throw new RuntimeSqlException("Could not set AutoCommit to " + autoCommit + ". Cause: " + t, t);
		}
	}

	private void commitConnection() {
		try {
			if (!connection.getAutoCommit()) {
				connection.commit();
			}
		}
		catch (Throwable e) {
			throw new RuntimeSqlException("Could not commit transaction. Cause: " + e, e);
		}
	}

	private void rollbackConnection() {
		try {
			if (!connection.getAutoCommit()) {
				connection.rollback();
			}
		}
		catch (Throwable e) {

		}
	}

	private void checkForMissingLineTerminator(StringBuilder command) {
		if (command != null && command.toString().trim().length() > 0) {
			throw new RuntimeException("Line missing end-of-line terminator (" + delimiter + ") => " + command);
		}
	}

	private void handleLine(StringBuilder command, String line) throws SQLException {
		String trimmedLine = line.trim();
		if (lineIsComment(trimmedLine)) {
			Matcher matcher = DELIMITER_PATTERN.matcher(trimmedLine);
			if (matcher.find()) {
				delimiter = matcher.group(5);
			}
			print(trimmedLine);
		}
		else if (commandReadyToExecute(trimmedLine)) {
			command.append(line, 0, line.lastIndexOf(delimiter));
			command.append(LINE_SEPARATOR);
			println(command);
			executeStatement(command.toString());
			command.setLength(0);
		}
		else if (trimmedLine.length() > 0) {
			command.append(line);
			command.append(LINE_SEPARATOR);
		}
	}

	private boolean lineIsComment(String trimmedLine) {
		return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
	}

	private boolean commandReadyToExecute(String trimmedLine) {
		return !fullLineDelimiter && trimmedLine.contains(delimiter) || fullLineDelimiter && trimmedLine.equals(delimiter);
	}

	private void executeStatement(String command) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.setEscapeProcessing(escapeProcessing);
			String sql = command;
			if (removeCRs) {
				sql = sql.replace("\r\n", "\n");
			}

			try {
				boolean hasResults = statement.execute(sql);
				while (!(!hasResults && statement.getUpdateCount() == -1)) {
					checkWarnings(statement);
					printResults(statement, hasResults);
					hasResults = statement.getMoreResults();
				}
			}
			catch (SQLWarning e) {
				throw e;
			}
			catch (SQLException e) {
				if (stopOnError) {
					throw e;
				}
				else {
					String message = "Error executing: " + command + ". Cause: " + e;
					printlnError(message);
				}
			}
		}
	}

	private void checkWarnings(Statement statement) throws SQLException {
		if (!throwWarning) {
			return;
		}

		SQLWarning warnings = statement.getWarnings();
		if (warnings != null) {
			throw warnings;
		}
	}

	private void printResults(Statement statement, boolean hasResults) {
		if (!hasResults) {
			return;
		}

		try (ResultSet rs = statement.getResultSet()) {
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();

			for (int i = 0; i < cols; i++) {
				String name = md.getColumnLabel(i + 1);
				print(name + "\t");
			}
			println("");

			while (rs.next()) {
				for (int i = 0; i < cols; i++) {
					String value = rs.getString(i + 1);
					print(value + "\t");
				}
				println("");
			}
		}
		catch (SQLException e) {
			printlnError("Error printing results: " + e.getMessage());
		}
	}

	private void print(Object o) {
		if (logWriter != null) {
			logWriter.print(o);
			logWriter.flush();
		}
	}

	private void println(Object o) {
		if (logWriter != null) {
			logWriter.println(o);
			logWriter.flush();
		}
	}

	private void printlnError(Object o) {
		if (errorLogWriter != null) {
			errorLogWriter.println(o);
			errorLogWriter.flush();
		}
	}
}
