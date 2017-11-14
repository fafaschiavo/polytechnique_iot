import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

public class ListReceiver implements SimpleMessageHandler{

	private SynchronizedListQueue incoming_list_messages = new SynchronizedListQueue();
	private MuxDemuxSimple myMuxDemux = null;
	private String myID;

    public ListReceiver(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
        incoming_list_messages.enqueue(m);
    }

    public void run(){
        while (true){

            try{
                String msg = incoming_list_messages.dequeue();
                ListMessage new_list_request = new ListMessage(msg);
                if (!new_list_request.getPeerID().equals(myID)) {
                	System.out.println("Received a List Message <<<<<<<<<<<<<<<<<");
                }else{
                	System.out.println("Not for me >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            } catch (NoSuchElementException e){
                // System.out.println("Nothing in queue...");
            } catch (RuntimeException e){
                // Message received is not a Sync request, so just ignore it
            }

        }
    }

}