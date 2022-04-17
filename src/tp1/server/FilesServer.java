package tp1.server;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.server.resources.Discovery;
import tp1.server.resources.FileResource;
import tp1.server.resources.UsersResource;
import tp1.server.util.CustomLoggingFilter;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilesServer {

    private static Logger Log = Logger.getLogger(FilesServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "FileService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {
            Debug.setLogLevel(Level.INFO, Debug.SD2122);

            //config.register(UsersResource.class);
            //config.register(CustomLoggingFilter.class);
            //config.register(GenericExceptionMapper.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);

            Discovery d = new Discovery(new InetSocketAddress("227.227.227.227", 2277), "files", serverURI);
            //d.start();
            d.announce("files", serverURI);

            ResourceConfig config = new ResourceConfig();

            FileResource f = new FileResource(d);
            config.register(f);

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            //More code can be executed here...
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }
}