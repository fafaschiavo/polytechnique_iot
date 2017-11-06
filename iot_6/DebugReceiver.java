import java.util.NoSuchElementException;

public class DebugReceiver implements SimpleMessageHandler{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;

    public DebugReceiver(){

    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m){
        incoming.enqueue(m);
    }
	
    public void run(){
        while (true){

            try{
                String msg = incoming.dequeue();
                System.out.println("DebugReceiver received: " + msg);
            } catch (NoSuchElementException e){
                // System.out.println("Nothing in queue...");
            }

        }
    }
}