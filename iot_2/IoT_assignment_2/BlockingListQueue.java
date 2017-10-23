import java.util.LinkedList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.NoSuchElementException;

/**
 * Basic implementation with a LinkedList.
 */
public class BlockingListQueue implements URLQueue {

  private final LinkedList<String> queue;
  private final HashSet<String> seen = new HashSet<String>();
  private Boolean is_locked = false;

  public BlockingListQueue() {
    queue = new LinkedList<String>();
  }

  public boolean isEmpty() {
    return queue.size() == 0;
  }

  public boolean isFull() {
    return false;
  }

  public synchronized void enqueue(String url) {

    // int timer_counter = 0;
    while(is_locked){
      try{
        wait();
      }catch(InterruptedException e){}
    }

    is_locked = true;

    if (!seen.contains(url)) {
      seen.add(url);
      queue.add(url);
    }

    is_locked = false;
    notifyAll();
    
  }

  public synchronized String dequeue() {
    while(is_locked){
      try{
        wait();
      }catch(InterruptedException e){}
    }

    is_locked = true;
    String to_return = queue.remove();
    is_locked = false;
    notifyAll();

    return to_return;
  }

}
