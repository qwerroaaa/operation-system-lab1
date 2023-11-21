import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Monitor {
    private final Lock lock = new ReentrantLock();
    private final Condition eventOccurred = lock.newCondition();
    private boolean isEventOccured = false;

    public void produceEvent() throws InterruptedException {
        lock.lock();
        try {
            // Имитация генерации события с задержкой в одну секунду
            Thread.sleep(1000);

            // Устанавливаем флаг события и будим ожидающий поток
            isEventOccured = true;
            eventOccurred.signal();
            System.out.println("Событие произошло!");
        } finally {
            lock.unlock();
        }
    }

    public void consumeEvent() throws InterruptedException {
        lock.lock();
        try {
            // Ожидаем события
            while (!isEventOccured) {
                eventOccurred.await();
            }

            // Обрабатываем событие и сбрасываем флаг
            System.out.println("Событие обработано!");
            isEventOccured = false;
        } finally {
            lock.unlock();
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
                monitor.produceEvent();
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
                monitor.consumeEvent();
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
