import java.util.NoSuchElementException;

public class HelloReceiver implements SimpleMessageHandler{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;

    public HelloReceiver(){

    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m){
        incoming.enqueue(m);
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
            } catch (NoSuchElementException e){
                // System.out.println("Nothing in queue...");
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