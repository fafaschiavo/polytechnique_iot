import java.util.HashMap;
import java.util.Map;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

public class DispatchLayer implements Layer {

  private static Map<Integer, Layer> table = new HashMap<Integer, Layer>();
  private static Layer dispatcher = null;
  public PrintWriter writer;
  private LinkedList<String> packet_queue = new LinkedList<String>();
  private static File_queue_manager queue_manager = new File_queue_manager();

    public static synchronized void start() {
        if (dispatcher == null)
            dispatcher = new DispatchLayer();
        GroundLayer.deliverTo(dispatcher);

        // Lunch the package manager.
        // The package manager will receive all the packages and pass them to the right
        // receiver in the right order. It runs in the background, so that the DispatchLayer.receive
        // can return immediately.
        Thread queue_manager_thread = new Thread(queue_manager, "Queue Manager");
        queue_manager_thread.start();
    }

    @SuppressWarnings("boxing")
    public static synchronized void register(Layer layer, int sessionId) {
        if (dispatcher != null) {
            table.put(sessionId, layer);
            GroundLayer.deliverTo(dispatcher);
        } else
          GroundLayer.deliverTo(layer);
    }

    private DispatchLayer() { // singleton pattern
    }

    @Override
    public void send(String payload) {
        throw new UnsupportedOperationException("don't use this for sending");
    }

    @Override
    public void receive(String payload, String source) {
        int connectionId = Integer.parseInt(payload.split(";")[0]);
        String connection_host = source.split("/")[1].split(":")[0];
        int connection_port = Integer.parseInt(source.split(":")[1]);

        Layer layer_for_this_packet = table.get(connectionId);
        if (layer_for_this_packet == null) {
            FileReceiver new_connected_layer = new FileReceiver(connection_host, connection_port, connectionId);
            this.register(new_connected_layer.getSubLayer(), connectionId);
        }else{
            layer_for_this_packet = table.get(connectionId);
        }

        queue_manager.reload_manager(table);
        queue_manager.add_to_queue(payload);
    }

    @Override
    public void deliverTo(Layer above) {
        throw new UnsupportedOperationException(
            "don't support a single Layer above");
    }

    @Override
    public void close() { // nothing
    }

}

class File_queue_manager implements Runnable {

    private static Map<Integer, Layer> table = new HashMap<Integer, Layer>();
    private static Map<Integer, Integer> package_order_table = new HashMap<Integer, Integer>();
    private static LinkedList<String> packet_queue = new LinkedList<String>();

    public void reload_manager(Map<Integer, Layer> new_table){
        table = new_table;
    }

    public void add_to_queue(String packege){
        packet_queue.add(packege);
    }

    @Override
    public void run() {

        while(true){
            try{
                Thread.sleep(1);
            }catch (Exception e){
            }

            if (packet_queue.size() > 0) {

                String next_package = packet_queue.remove();
                int connection_id = Integer.parseInt(next_package.split(";")[0]);
                int package_number = Integer.parseInt(next_package.split(";")[1]);
                String package_content = next_package.split(";")[2];

                // Check if this is the first file, if it is add the oroder controller to the queue,
                // if it isn't, start processing
                if (package_order_table.get(connection_id) != null) {
                    // Check if this is the right file in the right order. If it is, send it to the FileReceiver
                    // to be processed, if it isn't put it back again in the back of the queue.
                    // If it is an already old package that came repeated, just discart it 
                    int current_package = package_order_table.get(connection_id);
                    if (package_number == current_package) {

                        try{
                            Layer receiver_for_this_packet = table.get(connection_id);
                            receiver_for_this_packet.receive(package_content, "");
                        } catch (NullPointerException e) {
                            // FileReceiver is not ready yet, put package in the end of the queue
                            packet_queue.add(next_package);
                        }  

                        package_order_table.put(connection_id, package_number + 1);
                    }
                    else if(package_number > current_package){
                        packet_queue.add(next_package);
                    }else{

                    }
                }else{
                    package_order_table.put(connection_id, 1);
                }
            }
            
        }

    }
}

class FileReceiver implements Layer {

    private final Layer subLayer;
    public Boolean still_receiving = true;
    public Boolean started_receiving = false;
    public PrintWriter writer;
    public String payload;
    public String sender;
    public int current_package;


    public FileReceiver(String destinationHost, int destinationPort,
      int connectionId) {
        subLayer = new ConnectedLayer(destinationHost, destinationPort,
            connectionId);
        subLayer.deliverTo(this);
    }

    public Layer getSubLayer() {
        return subLayer;
    }

    @Override
    public void send(String payload) {
        throw new UnsupportedOperationException(
            "don't support any send from above");
    }

    @Override
    public void receive(String payload, String sender) {

        if (!payload.contains("--ACK--") && !payload.contains("--HELLO--")) {

            if (payload.toLowerCase().contains("SEND".toLowerCase()) && !started_receiving) {
                started_receiving = true;
                try{
                    writer = new PrintWriter("_received_" + payload.split("SEND ")[1], "UTF-8");
                }catch (Exception e){}
            }else if (payload.trim().equals("**CLOSE**")) {
                still_receiving = false;
                writer.close();
            }else{
                writer.println(payload);
            }

        }

        current_package = current_package + 1;

    }

    @Override
    public void deliverTo(Layer above) {
        throw new UnsupportedOperationException("don't support any Layer above");
    }

    @Override
    public void close() {
        // here, first wait for completion
        System.out.println("closing");
        subLayer.close();
    }

}