import java.util.ArrayList;

public class Integrate {
    private static final int nthreads = 4;
    public static void main(String[] args) {
        ArrayList<TaskExecutor> threads = new ArrayList<TaskExecutor>(nthreads);
        for (int i = 0; i < nthreads; i++) {
            threads.add(i, new TaskExecutor());
        }
        TaskExecutor.pushTask(new Task(0.000001, 1));
        for (int i = 0; i < nthreads; i++) {
            threads.get(i).run();
        }
        for (int i = 0; i < nthreads; i++) {
            if (threads.get(i).isAlive()) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Result: " + Double.toString(TaskExecutor.getResult()));
        System.out.println("Accuracy: " + Double.toString(TaskExecutor.getAccuracy()));
    }
}
