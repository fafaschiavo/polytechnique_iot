import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class Wget {

  public static void doMultiThreaded(String requestedURL, String proxyHost, int proxyPort) {
    int initial_amount_of_threads = Thread.activeCount();
    final URLQueue queue = new SynchronizedListQueue();
    Thread myThreads[] = new Thread[100000];
    int thread_index = 0;

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
        thread_index = thread_index + 1; 
      }

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
    doMultiThreaded(args[0], proxyHost, proxyPort);

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







