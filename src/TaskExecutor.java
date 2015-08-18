import java.util.Stack;

/**
 * Created by Lengl on 17.08.2015.
 */
public class TaskExecutor extends Thread {
    private static final int localStackCapacity = 30;
    private static final Task terminatorTask = Task.noTasks;
    //Any calls to this variables should be synchronized!
    private static double result = 0.0;
    private static int workingThreadsCount = 0;
    private static final Object wtcLock = new Object();
    private static int livingThreadsCount = 0;
    private static final Object ltcLock = new Object();
    private static Stack<Task> globalStack = new Stack<Task>();
    private static final Object gsLock = new Object();
    private static final Object gsNotEmpty = new Object();

    private Stack<Task> localStack = new Stack<Task>();

    //This one should exist because we need static method - even if he copies pushInGlobalStack
    public static void pushTask(Task task) {
        synchronized (gsLock) {
            globalStack.push(task);
            synchronized (gsNotEmpty) {
                gsNotEmpty.notify();
            }
        }
    }

    public void run() {
        comeAlive();
        while (true) {
            Task curTask = popFromStack();
            if (curTask == terminatorTask) {
                break;
            }
            TaskReturnValues taskRes = curTask.calculate();
            while (taskRes.task != Task.noTasks) {
                pushInStack(taskRes.task);
                taskRes = curTask.calculate();
            }
            addToResult(taskRes.area);
        }
        die();
    }

    private Task popFromStack() {
        if (!localStack.isEmpty()) {
            return localStack.pop();
        } else {
            return popFromGlobalStack();
        }
    }

    private void pushInStack(Task task) {
        localStack.push(task);
        if (localStack.size() == localStackCapacity) {
            //push extra task from local stack to global
            //we leave 5 tasks to avoid extra calls to global stack
            for (int i = 0; i < localStackCapacity - 5; i++) {
                pushInGlobalStack(localStack.pop());
            }
        }
    }

    //Those methods synchronized because they work with shared variables

    private Task popFromGlobalStack() {
        synchronized (wtcLock) {
            synchronized (gsLock) {
                workingThreadsCount--;
                //If we finished
                if (workingThreadsCount == 0 && globalStack.isEmpty()) {
                    for (int i = 0; i < livingThreadsCount; i++) {
                        globalStack.push(terminatorTask);
                    }
                    synchronized (gsNotEmpty) {
                        gsNotEmpty.notifyAll();
                    }
                } else {
                    synchronized (gsNotEmpty) {
                        try {
                            while (globalStack.isEmpty())
                                gsNotEmpty.wait(); //wait until global stack refilled
                        } catch (InterruptedException e) {
                        }
                    }
                    workingThreadsCount++;
                }
                return globalStack.pop();
            }
        }
    }

    private synchronized void pushInGlobalStack(Task task) {
        synchronized (gsLock) {
            globalStack.push(task);
            synchronized (gsNotEmpty) {
                gsNotEmpty.notify();
            }
        }
    }

    private synchronized void comeAlive() {
        synchronized (ltcLock) {livingThreadsCount++;}
        synchronized (wtcLock) {workingThreadsCount++;}
    }

    private synchronized void die() {
        synchronized (ltcLock) {livingThreadsCount--;}
    }

    //It is ok to be synchronized - we never else change result
    private synchronized void addToResult (double summand) {
        result += summand;
    }

    public static double getResult() {
        return result;
    }
}
