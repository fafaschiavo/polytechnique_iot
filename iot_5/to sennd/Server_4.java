import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

class FileReceiver implements Layer {

    private final Layer subLayer;
    public Boolean still_receiving = true;
    public Boolean started_receiving = false;
    public PrintWriter writer;

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

public class Server_4 {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println(
              "syntax : java Server_4 myPort destinationHost destinationPort");
            return;
        }

        if (GroundLayer.start(Integer.parseInt(args[0]))) {
            // GroundLayer.RELIABILITY = 0.5;
            FileReceiver receiver = new FileReceiver(args[1], Integer.parseInt(args[2]), (int) (Math.random() * Integer.MAX_VALUE));
            while(receiver.still_receiving){
                try{
                    Thread.sleep(1);
                }catch (Exception e){
                }
            }
            receiver.close();
            GroundLayer.close();
        }

    }
}
