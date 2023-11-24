import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
class Monitor {
    private final Lock lock = new ReentrantLock();
    private final Condition eventOccurred = lock.newCondition();
    private boolean ready = false;

    public void provide() throws InterruptedException {
        lock.lock();
        try {
            while (ready) {
                eventOccurred.await();
            }
            ready = true;
            System.out.println("Запрос отправлен");
            eventOccurred.signal();
        } finally {
            lock.unlock();
            Thread.sleep(1000);  // Пауза после provide
        }
    }

    public void consume() throws InterruptedException {
        lock.lock();
        try {
            while (!ready) {
                eventOccurred.await();
            }
            ready = false;
            System.out.println("Запрос обработан");
            eventOccurred.signal();
        } finally {
            lock.unlock();
            Thread.sleep(1000);  // Пауза после consume
        }
    }
}

class ProducerThread extends Thread {
    private final Monitor monitor;

    public ProducerThread(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                monitor.provide();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ConsumerThread extends Thread {
    private final Monitor monitor;

    public ConsumerThread(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                monitor.consume();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Monitor monitor = new Monitor();

        ProducerThread producerThread = new ProducerThread(monitor);
        ConsumerThread consumerThread = new ConsumerThread(monitor);

        producerThread.start();
        consumerThread.start();
    }
}

