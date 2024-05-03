package srdwb.utility;

import java.io.IOException;
import java.util.logging.*;

/**
 * Singleton Class used for logging
 *
 */
public class Logging {
	private static Logging _instance = new Logging();
	private Logging() {
		
	}
	public static Logging getInstance() {
		return _instance;
	}
	
	/**
	 * @param logger
	 * specify logger name, i.e. "client"
	 * @param msg
	 * logging message
	 */
	public void info(String logger, String msg) {
		Logger _logger = getLogger(logger);
		_logger.info(msg);
	}
	
	/**
	 * @param logger
	 * specify logger name, i.e. "client"
	 * @param msg
	 * logging message
	 */
	public void debug(String logger, String msg) {
		Logger _logger = getLogger(logger);
		_logger.fine(msg);
	}
	
	/**
	 * @param logger
	 * specify logger name, i.e. "client"
	 * @param msg
	 * logging message
	 */
	public void warn(String logger, String msg) {
		Logger _logger = getLogger(logger);
		_logger.warning(msg);
	}
	
	/**
	 * @param logger
	 * specify logger name, i.e. "client"
	 * @param msg
	 * logging message
	 */
	public void error(String logger, String msg) {
		Logger _logger = getLogger(logger);
		_logger.severe(msg);
	}
	
	private Logger getLogger(String logger) {
		LogManager manager = LogManager.getLogManager();
		Logger _logger = manager.getLogger(logger);
		if (_logger == null) {
			_logger = Logger.getLogger(logger);
			_logger.setLevel(Level.FINEST);
			try {
				_logger.addHandler(new FileHandler(logger + ".log"));
						
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _logger;
	}
	
}
