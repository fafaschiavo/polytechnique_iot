import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class Wget {

  public static void doThreadedPool(String requestedURL, String proxyHost, int proxyPort) {
    int thread_pool_size = 20;
    int initial_amount_of_threads = Thread.activeCount();
    final URLQueue queue = new BlockingListQueue();
    Thread myThreads[] = new Thread[thread_pool_size];
    Boolean is_threads_done[] = new Boolean[thread_pool_size];
    int thread_index = 0;

    for(int i=0; i<thread_pool_size; i++){
      is_threads_done[i] = true;
    }

    URLprocessing.handler = new URLprocessing.URLhandler() {
      // this method is called for each matched url
      public void takeUrl(String url) {
        queue.enqueue(url);
      }
    };
    // to start, we push the initial url into the queue
    URLprocessing.handler.takeUrl(requestedURL);
    while (!queue.isEmpty() || initial_amount_of_threads < Thread.activeCount()) {

      if (initial_amount_of_threads < Thread.activeCount() && queue.isEmpty()) {
        try{
          Thread.sleep(100);
        } catch(InterruptedException ex) {}
      }

      System.out.println("Number of active threads: " + Thread.activeCount());

      if (!queue.isEmpty()) {
        String url = queue.dequeue();
        call_xurl call_xurl_object = new call_xurl(url, proxyHost, proxyPort);  
        myThreads[thread_index] = new Thread(call_xurl_object);
        myThreads[thread_index].start();
        is_threads_done[thread_index] = !myThreads[thread_index].isAlive(); 
      }

      do {
        thread_index = thread_pool_size;
        for(int i=0; i<thread_pool_size; i++){
          try{
            if (!myThreads[i].isAlive()) {
              is_threads_done[i] = true;
            }else{
              is_threads_done[i] = false;
            }

          }catch(NullPointerException e){
            is_threads_done[i] = true;
          }

          if (is_threads_done[i]) {
            thread_index = i;
          }
        }

        if (thread_index == thread_pool_size) {
          try {
              Thread.sleep(100);
          } catch(Exception e) {}
        }

      } while (thread_index == thread_pool_size);


    }
  }

  public static void main(String[] args) {
    System.out.println("Number of active threads: " + Thread.activeCount());
    if (args.length < 1) {
      System.err.println("Usage: java Wget url [proxyHost proxyPort]");
      System.exit(-1);
    }
    String proxyHost = null;
    if (args.length > 1)
      proxyHost = args[1];
    int proxyPort = -1;
    if (args.length > 2)
      proxyPort = Integer.parseInt(args[2]);
    doThreadedPool(args[0], proxyHost, proxyPort);

    System.out.println("I am done here");

  }

}


class call_xurl implements Runnable {
 
  private String URL_to_request;  
  private String host;
  private int port;

  // colocar getter e setter pro atributo id
  public call_xurl(String requestedURL, String proxyHost, int proxyPort) {
    URL_to_request = requestedURL;
    host = proxyHost;
    port = proxyPort;
   }

  public void run () {
    System.out.println(URL_to_request);
    System.out.println(host);
    System.out.println(port);
    Xurl.query(URL_to_request, host, port);
  }
}

// java Wget http://java2s.com/







