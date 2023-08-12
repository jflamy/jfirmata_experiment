package app.owlcms.firmata.ui;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.github.javaparser.quality.NotNull;

import app.owlcms.firmata.utils.VaadinBoot;
import ch.qos.logback.classic.Logger;

/**
 * Run {@link #main(String[])} to launch your app in Embedded Jetty.
 * @author mavi
 */
public final class Main {
	final static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	
    public static void main(@NotNull String[] args) throws Exception {
        InputStream in = Main.class.getResourceAsStream("/build.properties");
        Properties props = new Properties();
        props.load(in);
        String windowsVersion = props.getProperty("windowsVersion");
		logger.info("{} {}{}built {} ({})", "owlcms-firmata",
        		props.getProperty("version"), 
        		windowsVersion != null && !windowsVersion.startsWith("$") ? " ("+windowsVersion+") " : " ", 
        		props.getProperty("buildTimestamp"), 
        		props.getProperty("buildZone")
        		);
        
    	int port = 8080;
    	while (!isTcpPortAvailable(port)) {
    		port++;
    	}
        var vaadinBoot = new VaadinBoot();
        vaadinBoot.setPort(port);
        vaadinBoot.withArgs(args).run();
    }
    
    public static boolean isTcpPortAvailable(int port) {
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

	public static Logger getStartupLogger() {
		return logger;
	}   
}
