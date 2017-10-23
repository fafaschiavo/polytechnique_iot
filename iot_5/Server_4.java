import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class FileReceiver implements Layer {

  private final Layer subLayer;

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
      FileReceiver receiver = new FileReceiver(args[1],
          Integer.parseInt(args[2]), (int) (Math.random() * Integer.MAX_VALUE));
      receiver.close();
      GroundLayer.close();
    }
  }
}
