package tp1.server.resources;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * <p>A class to perform service discovery, based on periodic service contact endpoint 
 * announcements over multicast communication.</p>
 * 
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 * 
 * <p>Service announcements have the following format:</p>
 * 
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	private Hashtable<String, ArrayList<URI>> uris = new Hashtable<>();

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	
	// The pre-aggreed multicast endpoint assigned to perform discovery. 
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("227.227.227.227", 2277);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 5000;
	static final int SIZE_ARRAY_URI = 10000;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private InetSocketAddress addr;
	private String serviceName;
	private String serviceURI;

	/**
	 * @param  serviceName the name of the service to announce
	 * @param  serviceURI an uri string - representing the contact endpoint of the service being announced
	 */
	public Discovery( InetSocketAddress addr, String serviceName, String serviceURI) {
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI  = serviceURI;
	}
	
	/**
	 * Continuously announces a service given its name and uri
	 * 
	 * @param serviceName the composite service name: <domain:service>
	 * @param serviceURI - the uri of the service
	 */
	public void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", DISCOVERY_ADDR, serviceName, serviceURI));

		var pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();

		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);
		// start thread to send periodic announcements
		new Thread(() -> {
			try (var ds = new DatagramSocket()) {
				for (;;) {
					try {
						Thread.sleep(DISCOVERY_PERIOD);
						ds.send(pkt);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Listens for the given composite service name, blocks until a minimum number of replies is collected.
	 * @return the discovery results as an array
	 */
	
	public void listener() {
		Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n", DISCOVERY_ADDR.getAddress(), DISCOVERY_ADDR.getPort()));

		final int MAX_DATAGRAM_SIZE = 65536;
		var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);

		new Thread(() -> {
		    try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
			    joinGroupInAllInterfaces(ms);
				for(;;) {
					try {
						pkt.setLength(MAX_DATAGRAM_SIZE);
						ms.receive(pkt);
					
						var msg = new String(pkt.getData(), 0, pkt.getLength());
						//System.out.printf( "FROM %s (%s) : %s\n", pkt.getAddress().getCanonicalHostName(), pkt.getAddress().getHostAddress(), msg);
						var tokens = msg.split(DELIMITER);

						if (tokens.length == 2) {
							//TODO: to complete by recording the received information from the other node.
							String serviceName = tokens[0];
							String serviceURI = tokens[1];
							URI uri = URI.create(serviceURI);

							if(uris.get(serviceName) == null) {
								ArrayList<URI> arrayURI = new ArrayList<>();
								uris.put(serviceName, arrayURI);
							}

							ArrayList<URI> list = uris.get(serviceName);
							boolean found = false;

							for (URI u: list) {
								if(u.toString().equalsIgnoreCase(uri.toString())){
									found = true;
								}
							}

							if(!found){
								list.add(uri);
								uris.put(serviceName, list);
							}

							/*
							System.out.println("nome do servico: " + serviceName);
							System.out.println("quantidade de servicos no mapa: " + uris.size());
							System.out.println("size da lista do servico: " + list.size());
							for (URI u: list) {
								System.out.print(u.toString() + ",   ");
							}
							System.out.println();
							System.out.println();
							*/

						}
					} catch (IOException e) {
						e.printStackTrace();
						try {
							Thread.sleep(DISCOVERY_PERIOD);
						} catch (InterruptedException e1) {
						// do nothing
						}
						Log.finest("Still listening...");
					}
				}
  		    } catch (IOException e) {
			    e.printStackTrace();
		    }
		}).start();
	}

	/**
	 * Returns the known servers for a service.
	 * 
	 * @param  serviceName the name of the service being discovered
	 * @return an array of URI with the service instances discovered. 
	 * 
	 */
	public URI[] knownUrisOf(String serviceName) {
		//TODO: You have to implement this!!
		ArrayList<URI> array = uris.get(serviceName);
		URI[] list = new URI[array.size()];

		for(int i = 0; i < array.size(); i++){
			list[i] = array.get(i);
		}

		/*
		System.out.println("Array do servico: ");
		for (URI u: list) {
			System.out.print(u.toString() + ",   ");
		}
		System.out.println();
		 */

		return list;
	}	
	
	private void joinGroupInAllInterfaces(MulticastSocket ms) throws SocketException {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		while (ifs.hasMoreElements()) {
			NetworkInterface xface = ifs.nextElement();
			try {
				ms.joinGroup(DISCOVERY_ADDR, xface);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}	

	/**
	 * Starts sending service announcements at regular intervals... 
	 */
	public void start() {
		announce(serviceName, serviceURI);
		listener();
	}


	// Main just for testing purposes
	public static void main( String[] args) throws Exception {
		Discovery discovery = new Discovery( DISCOVERY_ADDR, "test", "http://" + InetAddress.getLocalHost().getHostAddress());
		discovery.start();
	}

}
