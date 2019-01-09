import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * A producer tries to insert data into an empty slot of the buffer.
 * A consumer tries to remove data from a filled slot in the buffer.
 * */

/**
 * 1. How are you ensuring upper/lower bounds?
 * For class Producer: use Semaphore n and int allProducts to ensure upper bound,
 *                      and use list.isEmpty() to ensure lower bound
 * For class Consumer: use Semaphore e to check whether e is 0 to ensure lower bound,
 *                      and use int allConsume to ensure upper bound
 *
 * 2. How are you preventing deadlock?
 * We may have discovered that the real cause of deadlocks is not multithreading,
 * but the way they request locking. So I provide an orderly access, the problem will be solved.
 *
 * 3. How are you protecting critical sections?
 * There are some synchronization mechanisms that must be implemented at the entry
 * and exit points of the critical section to ensure that these shared resources are mutually exclusive.
 * So I define Semaphores to make sure that shared resources can only be accessed by one single thread.
 * And only one process is allowed to enter at a time. If an existing process enters its own critical section,
 * all other processes attempting to enter the critical section must wait.
 *
 * 4. Conclusions, comments, suggestions
 * Conclusions: In my code, multiple producers and multiple consumers can also work together.
 * They can avoid Race Conditions, won't go below lower bound or upper bound.
 * And can protect the critical section or shared variables
 *
 * */
public class ProducerConsumer {
    /**
     * @parameter allProducts is to record how many products we have produced
     * @parameter allConsume is to record how many products we have consumed
     * @parameter list is to store the products
     * */
    private static final int sizeOfBuffer = 10;
    private static int allProducts = 0;
    private static int allConsume = 0;
    private static ArrayList list = new ArrayList(sizeOfBuffer);
    private static Semaphore n = new Semaphore(0);
    private static Semaphore mutex = new Semaphore(1);
    private static Semaphore e = new Semaphore(sizeOfBuffer);

    /**Producer*/
    static class Producer extends Thread {
        public void run() {
            try {
                while (n.availablePermits() != sizeOfBuffer ) { //judge whether the buffer is full
//                    if (allProducts==0){  //check the initial number of products
//                        System.out.println("The initial number of products(n): "+(allProducts-allConsume));
//                    }
                    Thread.sleep((long) (Math.random() * 2000));
                    e.acquire();
                    mutex.acquire();
                    if (allProducts >= sizeOfBuffer){ // if produce sizeOfBuffer products, break
                        mutex.release();
                        n.release(1);
                        break;
                    }
                    System.out.println("-----------Producing item: "+ allProducts +"-----------" );
                    System.out.println("Produce item: " + allProducts);
                    list.add(allProducts++);
                    mutex.release();
                    n.release(1);
                    System.out.println("Number of products(n): "+(allProducts - allConsume)+"\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**Consumer*/
    static class Consumer extends Thread {
        String id;
        public Consumer(String id) {
            this.id = id;
        }
        public void run() {
            try {
                while (e.availablePermits() != 0) {
                    Thread.sleep(2000);
                    if (allConsume >=sizeOfBuffer){
                        break;
                    }
                    n.acquire(1);
                    mutex.acquire();
                    allConsume++;
                    System.out.println("-----------Consuming-----------" );
                    if(allConsume <sizeOfBuffer || !list.isEmpty()){ // to prevent dead lock
                        System.out.println("Consumer \"" + id + "\" consume: " + list.remove(0));
                    }
                    else {
                        System.out.println("Sorry customer:" +id + ". No product now.");
                        break;
                    }
                    System.out.println("Number of products(n): "+(allProducts - allConsume)+"\n");
                    mutex.release();
                    e.release();
                }
                System.out.println( "Sorry customer:" +id + ". No product now.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args) {
        // Test: use 5 producers, 5 consumers
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep((long) (Math.random() * 2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cachedPool.execute(new Producer());
            cachedPool.execute(new Consumer(""+i+""));
        }
        cachedPool.shutdown();
//        for (int i=0;i<5;i++){
//            new Producer().start();
//            new Consumer(""+i+"").start();
//        }

    }
}




















































