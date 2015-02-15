/**
 * 
 */
package org.ihtsdo.otf.tcc.api.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    AtomicInteger factoryCount = new AtomicInteger();
    ThreadGroup threadGroup;
    String threadNamePrefix;
    int threadPriority;

    public NamedThreadFactory(ThreadGroup threadGroup,
            String threadNamePrefix) {
        this(threadGroup, threadNamePrefix, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(ThreadGroup threadGroup,
            String threadNamePrefix, int threadPriority) {
        super();
        this.threadGroup = threadGroup;
        this.threadNamePrefix = threadNamePrefix;
        this.threadPriority = threadPriority;
        if (threadGroup.getMaxPriority() < threadPriority) {
            threadGroup.setMaxPriority(threadPriority);
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = threadNamePrefix + " " + 
                factoryCount.incrementAndGet();
        //AceLog.getAppLog().info("Creating thread: " + threadName);
        Thread t = new Thread(threadGroup, r, threadName) {

            @Override
            public void interrupt() {
                System.out.println("Interrupting: " + this.getName());
                super.interrupt(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void destroy() {
                System.out.println("destroying: " + this.getName());
                super.destroy(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Thread.State getState() {
                System.out.println("getting state: " + this.getName() + super.getState());
                return super.getState(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
                return new Thread.UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        e.printStackTrace();
                    }
                    
                };
                //return super.getUncaughtExceptionHandler(); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
        t.setPriority(threadPriority);
        return t;
    }
}