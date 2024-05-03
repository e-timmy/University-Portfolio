package srdwb.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import srdwb.utility.CmdConfig;

/**
 * Adjustments to Client implementation
 */
public class ClientCmdConfig extends CmdConfig{
	
	public ClientCmdConfig(String[] args) {
		super(args);
	}
	
	@Override
	protected void processOps(CommandLine line) {
		try {
			if (line.hasOption("address")) {
				values.replace("address", line.getOptionValue("address"));
			}
			if(line.hasOption("port")) {
				values.replace("port", Integer.parseInt(line.getOptionValue("port")));
			}
			if(line.hasOption("timeout")) {
				values.replace("timeout", Integer.parseInt(line.getOptionValue("timeout")));
			}
			if(line.hasOption("directory")) {
				values.replace("directory", line.getOptionValue("directory"));
			}
			if(line.hasOption("server-address")) {
				values.replace("server-address", line.getOptionValue("server-address"));
			}
			if(line.hasOption("server-port")) {
				values.replace("server-port", Integer.parseInt(line.getOptionValue("server-port")));
			}
			if(line.hasOption("server-secret")) {
				values.replace("server-secret", line.getOptionValue("server-secret"));
			}
			if(line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -cp <path to jar> srdwb.ClientDriver", options);
			}
		} catch (NumberFormatException e) {
			System.err.println("Error when parsing commandline arg to number: " + e.getMessage());
		}
	}
	
	@Override
	protected Options getCmdOps() {
		Options options = new Options();
		options.addOption("a", "address", true, "Client address");
		options.addOption("p", "port", true, "Client port");
		options.addOption("t", "timeout", true, "Timeout in milliseconds");
		options.addOption("d", "directory", true, "Save folder");
		options.addOption("sa", "server-address", true, "Server ip");
		options.addOption("sp", "server-port", true, "Server port");
		options.addOption("ss", "server-secret", true, "Server secret");
		options.addOption("h", "help", false, "Print this help message");
		return options;
	}
	
	@Override
	protected HashMap<String, Object> getDefaultValues(){
		return new HashMap<String, Object>(Map.of(
				"address", "127.0.0.1",
				"port", 3201,
				"timeout", 10000,
				"directory", "./",
				"server-address", "127.0.0.1",
				"server-port", 3200,
				"server-secret", "serverIsSecured"
			));
	}
}
