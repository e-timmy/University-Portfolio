package srdwb.utility;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/****************************************************************************************/

/**
 * Abstract command line options
 */
public abstract class CmdConfig {
	protected HashMap<String, Object> values;
	protected Options options;

	public CmdConfig(String[] args) {
		try {
			CommandLineParser cmd = new DefaultParser();
			options = getCmdOps();
			values = getDefaultValues();
			CommandLine line = cmd.parse(options, args);
			processOps(line);
		} catch(ParseException e) {
			System.err.println("Parse command line error: " + e.getMessage());
		}
	}

	public Object getValue(String key) {
		if (values.containsKey(key)) {
			return values.get(key);
		}
		return null;
	}

	/****************************************************************************************/

	protected abstract void processOps(CommandLine line);
	protected abstract Options getCmdOps();
	protected abstract HashMap<String, Object> getDefaultValues();
}

/****************************************************************************************/
