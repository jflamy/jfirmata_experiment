package app.owlcms.firmata.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import com.github.javaparser.quality.NotNull;
import com.github.mvysny.vaadinboot.VaadinBoot;

import app.owlcms.utils.LoggerUtils;
import app.owlcms.utils.Resource;
import app.owlcms.utils.ResourceWalker;
import ch.qos.logback.classic.Logger;

/**
 * Run {@link #main(String[])} to launch your app in Embedded Jetty.
 * 
 * @author mavi
 */
public final class Main {
	public static String deviceConfigs = null;
	public static int port = 8080;
	final static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	public static void main(@NotNull String[] args) throws Exception {
		logVersion();
		parseOptions(args);
		while (!isTcpPortAvailable(port)) {
			port++;
		}
		ResourceWalker.checkForLocalOverrideDirectory(() -> populateLocalDirectory());
		var vaadinBoot = new VaadinBoot();
		vaadinBoot.setAppName("owlcms-firmata");
		vaadinBoot.setPort(port);
		vaadinBoot.run();
	}

	public static Logger getStartupLogger() {
		return logger;
	}

	private static String getDefaultConfigDir() {
		if (System.getProperty("os.name").startsWith("Windows")) {
			deviceConfigs = System.getProperty("user.home") + "/" + "owlcms" + "/" + "devices";
		} else {
			deviceConfigs = System.getProperty("user.home") + "/" + ".owlcms" + "/" + "devices";
		}
		return deviceConfigs;
	}

	private static boolean isTcpPortAvailable(int port) {
		try (ServerSocket serverSocket = new ServerSocket()) {
			// setReuseAddress(false) is required only on macOS,
			// otherwise the code will not work correctly on that platform
			serverSocket.setReuseAddress(false);
			serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private static void logVersion() throws IOException {
		InputStream in = Main.class.getResourceAsStream("/build.properties");
		Properties props = new Properties();
		props.load(in);
		String windowsVersion = props.getProperty("windowsVersion");
		logger.info("{} {}{}built {} ({})", "owlcms-firmata",
		        props.getProperty("version"),
		        windowsVersion != null && !windowsVersion.startsWith("$") ? " (" + windowsVersion + ") " : " ",
		        props.getProperty("buildTimestamp"),
		        props.getProperty("buildZone"));
	}

	private static void parseOptions(String[] args) {
		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption(Option.builder("p").longOpt("port")
		        .desc("port number to use. if unavailable, try higher ports until found.")
		        .hasArg()
		        .build());
		options.addOption(Option.builder("d").longOpt("device-configs")
		        .desc("directory where device configuration .xlsx files are found ")
		        .hasArg()
		        .build());

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("port")) {
				// print the value of block-size
				port = Integer.parseInt(line.getOptionValue("port"));
			}
			logger.info("setting port to {}", port);

			if (line.hasOption("device-configs")) {
				// print the value of block-size
				logger.info("setting deviceConfigs to {}", deviceConfigs);
				deviceConfigs = line.getOptionValue("port");
			} else {
				deviceConfigs = getDefaultConfigDir();
			}
			logger.info("setting config directory to {}", deviceConfigs);
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
		return;
	}

	private static void populateLocalDirectory() {
		File overrideDir = new File(Main.deviceConfigs);
		overrideDir.mkdirs();

		// copy resources from classpath into the directory, flatten
		List<Resource> resources = new ResourceWalker().getResourceList("/devices",
		        ResourceWalker::relativeName, null, Locale.getDefault());
		resources.stream()
		        .filter(r -> !r.getFileName().endsWith("Pinout.xlsx"))
		        .forEach(r -> {
			        try {
				        FileUtils.copyInputStreamToFile(r.getStream(),
				                new File(Main.deviceConfigs + "/" + r.getFilePath().getFileName()));
			        } catch (IOException e) {
				        LoggerUtils.logError(logger, e);
			        }
		        });
	}
}
