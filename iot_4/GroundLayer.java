import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
// import java.nio.charset.StandardCharsets;

public class GroundLayer {

    /**
    * This {@code Charset} is used to convert between our Java native String
    * encoding and a chosen encoding for the effective payloads that fly over the
    * network.
    */
    // private static final Charset CONVERTER = StandardCharsets.UTF_8;

    /**
    * This value is used as the probability that {@code send} really sends a
    * datagram. This allows to simulate the loss of packets in the network.
    */
    public static double RELIABILITY = 1.0;
    public static DatagramSocket serverSocket;
    public static Layer upward_layer;
    public static reading_loop reading_loop_object;

    public static boolean start(int localPort) {
        try {
            serverSocket = new DatagramSocket(localPort);
            reading_loop_object = new reading_loop(serverSocket);
            Thread reading_thread = new Thread(reading_loop_object);
            reading_thread.start();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static boolean random_reliability()
    {
        return Math.random() >= 1.0 - RELIABILITY;
    }

    public static void deliverTo(Layer layer) {
        upward_layer = layer;
        reading_loop_object.set_upward_layer(layer);
    }

    public static void send(String payload, String destinationHost, int destinationPort) {
        if (random_reliability()) {
            try{
                byte[] sendData = payload.getBytes();
                InetAddress IPAddress = InetAddress.getByName(destinationHost);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, destinationPort);
                serverSocket.send(sendPacket);
            }catch (Exception e){
                System.err.println("Got an Exception while sending data...");
            }
        }

    }

    public static void close() {
        serverSocket.close();
        System.err.println("GroundLayer closed");
    }

}




class reading_loop implements Runnable {

    private DatagramSocket loop_serverSocket;
    private Layer upward_layer;
    private boolean is_upward_layer_set = false;

    public reading_loop(DatagramSocket serverSocket) {
        loop_serverSocket = serverSocket;
    }

    public void set_upward_layer(Layer layer_to_forward){
        upward_layer = layer_to_forward;
        is_upward_layer_set = true;
    }

    public void run () {
        while (true){
            try{
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                loop_serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData());

                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                if (is_upward_layer_set) {
                    upward_layer.receive(sentence, IPAddress + ":" + port);
                }
              
            }catch (Exception e){
                // System.err.println(e);
                // System.err.println("Got an Exception while reading loop...");
            }

        }
    }
}
