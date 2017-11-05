import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class ClientFileSenderLayer implements Layer {

  private final Layer subLayer;

  public ClientFileSenderLayer(String fileName, String destinationHost,
      int destinationPort) {
    subLayer = new ConnectedLayer(destinationHost, destinationPort,
        (int) (Math.random() * Integer.MAX_VALUE));
    subLayer.deliverTo(this);
    subLayer.send("SEND " + fileName);
    System.out.println("SEND " + fileName);
  }

  @Override
  public void send(String line) {
    subLayer.send(line);
  }

  @Override
  public void receive(String payload, String sender) {
    System.out.println('"' + payload + "\" from " + sender);
  }

  @Override
  public void deliverTo(Layer above) {
    throw new UnsupportedOperationException("don't support any Layer above");
  }

  @Override
  public void close() {
    subLayer.send("**CLOSE**");
    subLayer.close();
  }

}

public class ClientFileSender {

  public static void main(String[] args) {
    if (args.length != 4) {
      System.err.println(
          "syntax : java FileSender myPort destinationHost destinationPort file");
      return;
    }
    Scanner sc;
    try {
      sc = new Scanner(new File(args[3]));
    } catch (FileNotFoundException e) {
      System.err.println(e);
      return;
    }
    if (GroundLayer.start(Integer.parseInt(args[0]))) {
      // GroundLayer.RELIABILITY = 0.5;
      Layer sender = new ClientFileSenderLayer(args[3], args[1], Integer.parseInt(args[2]));
      System.out.println("Started sending...");
      while (sc.hasNextLine()) {
        sender.send(sc.nextLine());
        // try{
        //     Thread.sleep(500);
        // }catch (Exception e){
        // }
      }
      System.out.println("Done sending...");
      sc.close();
      sender.close();
      GroundLayer.close();
    }
  }
}

// java Server_4 8004 localhost 8005
// java ClientFileSender 8005 localhost 8004 sample_file.txt