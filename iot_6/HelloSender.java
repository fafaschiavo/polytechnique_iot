import java.util.NoSuchElementException;

public class HelloSender implements SimpleMessageHandler{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;

    public HelloSender(){

    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m){
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

            HelloMessage new_message = new HelloMessage("fabricio", 0, 2);
            String encoded_new_message = new_message.getHelloMessageAsEncodedString();
            myMuxDemux.send(encoded_new_message);
            try {
              Thread.sleep(2000);
            } catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
            }
        }
    }
}