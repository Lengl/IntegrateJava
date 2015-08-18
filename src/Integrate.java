import java.util.ArrayList;

/**
 * Created by Lengl on 18.08.2015.
 */
public class Integrate {
    private static final int nthreads = 2;
    public static void main(String[] args) {
        ArrayList<TaskExecutor> threads = new ArrayList<TaskExecutor>(nthreads);
        for (int i = 0; i < nthreads; i++) {
            threads.add(i, new TaskExecutor());
        }
        TaskExecutor.pushTask(new Task(0.1, 1));
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
        System.out.println(Double.toString(TaskExecutor.getResult()));
    }
}
