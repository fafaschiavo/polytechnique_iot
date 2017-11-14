import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ListSender implements SimpleMessageHandler{

	private SynchronizedListQueue incoming_sync_requests = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;
    private String myID;

    public ListSender(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
        incoming_sync_requests.enqueue(m);
    }

    public void run(){
        while (true){

            try{
                String msg = incoming_sync_requests.dequeue();
                SynMessage new_sync_request = new SynMessage(msg);
                System.out.println("SYNC REQUEST RECEIVED =================");
                System.out.println("ListReceiver received: " + msg);
            } catch (NoSuchElementException e){
                // System.out.println("Nothing in queue...");
            } catch (RuntimeException e){
                // Message recceived is not a Sync request, so just ignore it
            }

        }
    }

}