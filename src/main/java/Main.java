import com.google.common.util.concurrent.Monitor;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws Exception {
        int count = 1000;

//        // with issue
//        Set<Integer> uniqueSequences = getUniqueSequences(
//                new SequenceGenerator(), count);

//        //solved issue using mutex
//        Set<Integer> uniqueSequences = getUniqueSequences(
//                new SequenceGeneratorUsingSynchronizedBlock(), count);

//        //Using ReentrantLock
//        Set<Integer> uniqueSequences = getUniqueSequences(
//                new SequenceGeneratorUsingReentrantLock(), count);

//        // Using Semaphore
//        Set<Integer> uniqueSequences = getUniqueSequences(
//                new SequenceGeneratorUsingSemaphore(), count);

        //   Using Guava's Monitor Class
        Set<Integer> uniqueSequences = getUniqueSequences(
                new SequenceGeneratorUsingMonitor(), count);

        Assert.assertEquals(count, uniqueSequences.size());
    }

    private static Set<Integer> getUniqueSequences(SequenceGenerator generator, int count) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Set<Integer> uniqueSequences = new LinkedHashSet<>();
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(executor.submit(generator::getNextSequence));
        }

        for (Future<Integer> future : futures) {
            uniqueSequences.add(future.get());
        }

        executor.awaitTermination(1, TimeUnit.SECONDS);
        executor.shutdown();

        return uniqueSequences;
    }
}

class SequenceGeneratorUsingMonitor extends SequenceGenerator {

    private Monitor mutex = new Monitor();

    @Override
    public int getNextSequence() {
        mutex.enter();
        try {
            return super.getNextSequence();
        } finally {
            mutex.leave();
        }
    }
}

class SequenceGenerator {

    private int currentValue = 0;

    public int getNextSequence() {
        currentValue = currentValue + 1;
        return currentValue;
    }
}

// Using synchronized Keyword
class SequenceGeneratorUsingSynchronizedBlock extends SequenceGenerator {

    private Object mutex = new Object();

    @Override
    public int getNextSequence() {
        synchronized (mutex) {
            return super.getNextSequence();
        }
    }
}

//    Using ReentrantLock
class SequenceGeneratorUsingReentrantLock extends SequenceGenerator {

    private ReentrantLock mutex = new ReentrantLock();

    @Override
    public int getNextSequence() {
        try {
            mutex.lock();
            return super.getNextSequence();
        } finally {
            mutex.unlock();
        }
    }
}

//    Using Semaphore
class SequenceGeneratorUsingSemaphore extends SequenceGenerator {

    private Semaphore mutex = new Semaphore(1);

    @Override
    public int getNextSequence() {
        try {
            mutex.acquire();
            return super.getNextSequence();
        } catch (InterruptedException e) {
            // exception handling code
            return 0;
        } finally {
            mutex.release();
        }
    }
}