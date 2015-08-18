import java.util.ArrayList;

public class Integrate {
    private static final int nthreads = 2;
    public static void main(String[] args) {
        ArrayList<TaskExecutor> threads = new ArrayList<TaskExecutor>(nthreads);
        for (int i = 0; i < nthreads; i++) {
            threads.add(i, new TaskExecutor());
        }
        for (int i = 0; i < nthreads; i++) {
            threads.get(i).start();
        }
        long startTime = System.currentTimeMillis();
        TaskExecutor.pushTask(new Task(0.00001, 1));
        for (int i = 0; i < nthreads; i++) {
            if (threads.get(i).isAlive()) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Thread #" + Integer.toString(i) +
                    ", amount of drops: " + Integer.toString(threads.get(i).localStackDropCount) +
                    ", amount of work: " + Integer.toString(threads.get(i).getTempAccuracyCount()));
        }
        long stopTime = System.currentTimeMillis();
        System.out.println("Result: " + Double.toString(TaskExecutor.getResult()));
        System.out.println("Accuracy: " + Double.toString(TaskExecutor.getAccuracyCount()* Task.accuracy));
        System.out.println("Elapsed time was " + (stopTime - startTime) + " miliseconds.");
    }
}
