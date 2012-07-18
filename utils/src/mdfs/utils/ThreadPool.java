package mdfs.utils;

import java.util.concurrent.*;

/**
 * Package: mdfs.utils
 * Created: 2012-07-18
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ThreadPool extends ThreadPoolExecutor{


    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveMillisec, int maximumQueued) {

        super(corePoolSize, maximumPoolSize, keepAliveMillisec, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maximumQueued, true),
                Executors.defaultThreadFactory(), new WaitPolicy());
    }



     public static class WaitPolicy implements RejectedExecutionHandler{
         private int idle = 5;

         @Override
         public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {

             while(threadPoolExecutor.getQueue().remainingCapacity() < 1){
                 try {
                     Thread.sleep(idle);
                 } catch (InterruptedException e) {
                 }
             }
             threadPoolExecutor.execute(runnable);

         }

     }
}
