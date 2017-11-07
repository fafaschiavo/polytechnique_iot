import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;

public class HelloSender implements SimpleMessageHandler{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;
    private String myID;


    public HelloSender(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
    }
	
    public void run(){
        while (true){

            // /////////////////////////////////////////////////////////////////////////////////////////////
            // HelloReceiver never receives messages
            // /////////////////////////////////////////////////////////////////////////////////////////////

            // try{
            //     String msg = incoming.dequeue();
            //     System.out.println("HelloSender received:");
            //     System.out.println(msg);
            // } catch (NoSuchElementException e){
            //     System.out.println("Nothing in queue...");
            // }

            // /////////////////////////////////////////////////////////////////////////////////////////////
            // HelloSender only generate messages
            // /////////////////////////////////////////////////////////////////////////////////////////////

            HelloMessage new_message = new HelloMessage(myID, 0, 2);
            String[] valid_peers = myMuxDemux.get_valid_peers();
            System.out.println("Current valid peers in peer list:" + Arrays.toString(valid_peers));
            for (int i=0; i < valid_peers.length ; i++) {
                new_message.addPeer(valid_peers[i]);
            }
            String encoded_new_message = new_message.getHelloMessageAsEncodedString();
            System.out.println("HelloSender sent: " + encoded_new_message);
            myMuxDemux.send(encoded_new_message);
            try {
              Thread.sleep(2000);
            } catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
            }
        }
    }
}