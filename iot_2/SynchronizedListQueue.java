import java.util.LinkedList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.NoSuchElementException;

/**
 * Basic implementation with a LinkedList.
 */
public class SynchronizedListQueue implements URLQueue {

  private final LinkedList<String> queue;
  private final HashSet<String> seen = new HashSet<String>();
  private Boolean is_locked = false;

  public SynchronizedListQueue() {
    queue = new LinkedList<String>();
  }

  public boolean isEmpty() {
    return queue.size() == 0;
  }

  public boolean isFull() {
    return false;
  }

  public void enqueue(String url) {

    // int timer_counter = 0;
    while(is_locked){
      try {
        // timer_counter = timer_counter + 1;
        // if (timer_counter == 20) {
        //   is_locked = false;
        // }
        Thread.sleep(100);
      } catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }

    is_locked = true;

    if (!seen.contains(url)) {
      seen.add(url);
      queue.add(url);
    }

    is_locked = false;
    
  }

  public String dequeue() {
    while(is_locked){
      try {
          Thread.sleep(100);
      } catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
      }
    }

    is_locked = true;
    String to_return = queue.remove();
    is_locked = false;

    return to_return;
  }

}
