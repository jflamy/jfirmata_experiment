package app.owlcms.firmata.ui;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import javax.validation.constraints.NotNull;

import org.slf4j.LoggerFactory;

import app.owlcms.firmata.utils.VaadinBoot;
import ch.qos.logback.classic.Logger;

/**
 * Run {@link #main(String[])} to launch your app in Embedded Jetty.
 * @author mavi
 */
public final class Main {
	final static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	
    public static void main(@NotNull String[] args) throws Exception {
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
