import java.util.Stack;
import java.util.concurrent.Semaphore;

/**
 * Created by Lengl on 17.08.2015.
 */
public class TaskExecutor extends Thread {
    //Any calls to this variables should be synchronized!
    private static double result;
    private static Stack <Task> globalStack;
    private static final int localStackCapacity = 30;
    private static int workingThreadsCount;
    private static int livingThreadsCount = 0;
    //this semaphore represents amount of available tasks in globalStack
    private static Semaphore globalStackLocker;
    private static final Task terminatorTask = Task.noTasks;

    private Stack <Task> localStack;

    //This one actually duplicates pushInGlobalStack, I'm not sure if it should exist.
    public static void pushTask(Task task) {
        globalStack.push(task);
        globalStackLocker.release();
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

    private synchronized Task popFromGlobalStack() {
        workingThreadsCount--;
        if (workingThreadsCount == 0 && globalStack.isEmpty()){
            for (int i = 0; i < livingThreadsCount; i++) {
                globalStack.push(terminatorTask);
            }
            globalStackLocker.release(livingThreadsCount);
            //here we are saying to the externals that result is ready
            this.notifyAll();
        } else {
            try {
                globalStackLocker.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            workingThreadsCount++;
        }
        return globalStack.pop();
    }

    private synchronized void pushInGlobalStack(Task task) {
        globalStack.push(task);
        globalStackLocker.release();
    }

    private synchronized void addToResult (double summand) {
        result += summand;
    }

    private synchronized void comeAlive() {
        livingThreadsCount++;
        workingThreadsCount++;
    }

    private synchronized void die() {
        livingThreadsCount--;
    }

    public static double getResult() {
        return result;
    }
}
