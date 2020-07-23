package com.tolpp.sevq;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class EventQueueTest {

    @Test
    public void testQueue() throws Exception {
        CountDownLatch cdl = new CountDownLatch(10);
        CountDownLatch cdlSum = new CountDownLatch(4);
        CountDownLatch cdlMultiply = new CountDownLatch(4);
        EventQueue queue = new EventQueue(Executors.newFixedThreadPool(4));

        queue.addEventListener(TextEvent.class, event -> {
            try {
                int waitInMilliseconds = (int) (6000 * Math.random()) + 1000;
                System.out.println("Waiting: " + waitInMilliseconds);
                Thread.sleep(waitInMilliseconds);


            } catch (InterruptedException e) {
                e.printStackTrace();
                cdl.countDown();
            }
            System.out.println("TextEvent happened with text: " + event.getText());
            cdl.countDown();
        });

        queue.addEventListener(Executors.newSingleThreadExecutor(), TwoNumbersEvent.class, event -> {
            System.out.println("Sum of numbers: " + (event.getNumberOne() + event.getNumberTwo()));
            cdlSum.countDown();
        });

        queue.addEventListener(TwoNumbersEvent.class, event -> {
            System.out.println("Multiply of numbers: " + (event.getNumberOne() * event.getNumberTwo()));
            cdlMultiply.countDown();
        });


        queue.send(new TextEvent("Text1"));
        queue.send(new TextEvent("Text2"));
        queue.send(new TextEvent("Text3"));
        queue.send(new TextEvent("Text4"));
        queue.send(new TextEvent("Text5"));
        queue.send(new TextEvent("Text6"));
        queue.send(new TextEvent("Text7"));
        queue.send(new TextEvent("Text8"));
        queue.send(new TextEvent("Text9"));
        queue.send(new TextEvent("Text10"));

        queue.send(new TwoNumbersEvent(1, 2));
        queue.send(new TwoNumbersEvent(3, 2));
        queue.send(new TwoNumbersEvent(5, 2));
        queue.send(new TwoNumbersEvent(7, 2));

        cdl.await();
        cdlSum.await();
        cdlMultiply.await();
    }

    private static class TextEvent implements Event {
        private final String text;

        public TextEvent(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private static class TwoNumbersEvent implements Event {
        private final int numberOne;
        private final int numberTwo;

        public TwoNumbersEvent(int numberOne, int numberTwo) {
            this.numberOne = numberOne;
            this.numberTwo = numberTwo;
        }

        public int getNumberOne() {
            return numberOne;
        }

        public int getNumberTwo() {
            return numberTwo;
        }
    }
}
