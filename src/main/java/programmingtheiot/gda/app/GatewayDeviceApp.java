/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * You may find it more helpful to your design to adjust the
 * functionality, constants and interfaces (if there are any)
 * provided within in order to meet the needs of your specific
 * Programming the Internet of Things project.
 */

package programmingtheiot.gda.app;

import org.apache.commons.cli.*;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main GDA application.
 * 
 */
public class GatewayDeviceApp
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(GatewayDeviceApp.class.getName());
	
	public static final long DEFAULT_TEST_RUNTIME = 60000L;
	
	// private var's
	
	private String configFile = ConfigConst.DEFAULT_CONFIG_FILE_NAME;

	// constructors
	
	/**
	 * Default.
	 * 
	 * @param configFile
	 */
	public GatewayDeviceApp()
	{
		super();
		
		_Logger.info("Initializing GDA...");
	}
	
	
	// static
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Map<String, String> argMap = parseArgs(args);

		if (argMap.containsKey(ConfigConst.CONFIG_FILE_KEY)) {
			System.setProperty(ConfigConst.CONFIG_FILE_KEY, argMap.get(ConfigConst.CONFIG_FILE_KEY));
		}

		GatewayDeviceApp gwApp = new GatewayDeviceApp();
		
		gwApp.startApp();
		
		boolean runForever =
			ConfigUtil.getInstance().getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_RUN_FOREVER_KEY);
		
		if (runForever) {
			try {
				while (true) {
					Thread.sleep(2000L);
				}
			} catch (InterruptedException e) {
				// ignore
			}
			
			gwApp.stopApp(0);
		} else {
			try {
				Thread.sleep(DEFAULT_TEST_RUNTIME);
			} catch (InterruptedException e) {
				// ignore
			}
			
			gwApp.stopApp(0);
		}
	}
	
	/**
	 * Parse any arguments passed in on app startup.
	 * <p>
	 * This method should be written to check if any valid command line args are provided,
	 * including the name of the config file. Once parsed, call {@link #initConfig(String)}
	 * with the name of the config file, or null if the default should be used.
	 * <p>
	 * If any command line args conflict with the config file, the config file
	 * in-memory content should be overridden with the command line argument(s).
	 * 
	 * @param args The non-null and non-empty args array.
	 */
	private static Map<String, String> parseArgs(String[] args)
	{
		// store command line values in a map
		Map<String, String> argMap = new HashMap<String, String>();
		
		if (args != null && args.length > 0)  {
			// create the parser and options - only need one for now ("c" for config file)
			CommandLineParser parser = new DefaultParser();
			Options options = new Options();

			options.addOption("c", true, "The relative or absolute path of the config file.");

			try {
				CommandLine cmdLineArgs = parser.parse(options, args);

				if (cmdLineArgs.hasOption("c")) {
					argMap.put(ConfigConst.CONFIG_FILE_KEY, cmdLineArgs.getOptionValue("c"));
				} else {
					_Logger.info("No custom config file specified. Using default.");
				}
			} catch (ParseException e) {
				_Logger.warning("Failed to parse command line args. Ignoring - using defaults.");
			}
		}

		return argMap;
	}
	
	
	// public methods
	
	/**
	 * Initializes and starts the application.
	 * 
	 */
	public void startApp()
	{
		_Logger.info("Starting GDA...");
		
		try {
			// TODO: Your code here
			
			_Logger.info("GDA started successfully.");
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
			
			stopApp(-1);
		}
	}
	
	/**
	 * Stops the application.
	 * 
	 * @param code The exit code to pass to {@link System.exit()}
	 */
	public void stopApp(int code)
	{
		_Logger.info("Stopping GDA...");
		
		try {
			// TODO: Your code here
			
			_Logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to cleanly stop GDA. Exiting.", e);
		}
		
		System.exit(code);
	}
	
	
	// private methods
	

}
