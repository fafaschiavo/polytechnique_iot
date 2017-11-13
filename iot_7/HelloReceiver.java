import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

public class HelloReceiver implements SimpleMessageHandler{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;
    private HashMap<String, InetAddress> ip_addresses = new HashMap<String, InetAddress>();
    private String myID;


    public HelloReceiver(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
        incoming.enqueue(m);
        ip_addresses.put(m, ip_address);
    }
	
    public void run(){
        while (true){

            // /////////////////////////////////////////////////////////////////////////////////////////////
            // HelloReceiver only receives messages
            // /////////////////////////////////////////////////////////////////////////////////////////////

            try{

                String msg = incoming.dequeue();
                HelloMessage received_message = new HelloMessage(msg);
                System.out.println("HelloReceiver received: " + received_message.getHelloMessageAsEncodedString());
                myMuxDemux.touch_new_peer(received_message.getSenderID(), ip_addresses.get(msg), received_message.getSequenceNumber(), received_message.getHelloInterval());

            } catch (NoSuchElementException e){
                // Nothing in queue
            } catch (RuntimeException e){
                System.out.println(e);
                System.out.println("Invalid Hello...");
            }

            // /////////////////////////////////////////////////////////////////////////////////////////////
            // HelloReceiver never generate messages
            // /////////////////////////////////////////////////////////////////////////////////////////////

            // HelloMessage new_message = new HelloMessage("fabricio", 0, 2);
            // String encoded_new_message = new_message.getHelloMessageAsEncodedString();
            // myMuxDemux.send(encoded_new_message);
            // try {
            //   Thread.sleep(2000);
            // } catch(InterruptedException ex) {
            //   Thread.currentThread().interrupt();
            // }

        }
    }
}