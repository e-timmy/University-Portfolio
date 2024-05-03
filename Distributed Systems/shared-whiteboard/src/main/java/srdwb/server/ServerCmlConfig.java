package srdwb.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.*;
import srdwb.utility.CmdConfig;

/****************************************************************************************/

/**
 * Server's command line options
 */
public class ServerCmlConfig extends CmdConfig {

	public ServerCmlConfig(String[] args) {
		super(args);
	}
	
	@Override
	protected void processOps(CommandLine line) {
		try {
			if (line.hasOption("address")) {
				values.put("address",line.getOptionValue("address"));
			}
			if(line.hasOption("port")) {
				values.put("port", Integer.parseInt(line.getOptionValue("port")));
			}
			if(line.hasOption("timeout")) {
				values.put("timeout", Integer.parseInt(line.getOptionValue("timeout")));
			}
			if(line.hasOption("server-secret")) {
				values.put("server-secret", line.getOptionValue("server-secret"));
			}
			if(line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -cp <path to jar> srdwb.ServerDriver", options);
			}
			
		} catch (NumberFormatException e) {
			System.err.println("Error when parsing commandline arg to number: " + e.getMessage());
		}
	}
	
	@Override
	protected Options getCmdOps() {
		Options options = new Options();
		options.addOption("a", "address", true, "Server address");
		options.addOption("p", "port", true, "Server port");
		options.addOption("t", "timeout", true, "Timeout in milliseconds");
		options.addOption("ss", "server-secret", true, "Server secret");
		options.addOption("h", "help", false, "Print this help message");
		return options;
	}
	
	@Override
	protected HashMap<String, Object> getDefaultValues(){
		return new HashMap<String, Object>(Map.of(
				"address", "127.0.0.1",
				"port", 3200,
				"timeout", 10000,
				"server-secret", "123"
			));
	}
}

/****************************************************************************************/

