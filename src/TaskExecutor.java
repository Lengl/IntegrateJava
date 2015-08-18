import java.util.Stack;

public class TaskExecutor extends Thread {
    private static final int localStackCapacity = 15;
    private static final Task terminatorTask = Task.noTasks;
    //Any calls to this variables should be synchronized!
    private static int accuracyCount = 0;
    private static double result = 0.0;
    private static int workingThreadsCount = 0;
    private static final Object wtcLock = new Object();
    private static int livingThreadsCount = 0;
    private static final Object ltcLock = new Object();
    private static Stack<Task> globalStack = new Stack<Task>();
    private static final Object gsLock = new Object();
    private static final Object gsNotEmpty = new Object();

    //Those are local variables for each thread and should not be synchronized
    private Stack<Task> localStack = new Stack<Task>();
    private double tempResult = 0;
    private int tempAccuracyCount = 0;
    public int localStackDropCount = 0;

    public int getTempAccuracyCount() {
        return tempAccuracyCount;
    }

    public static int getAccuracyCount() {
        return accuracyCount;
    }

    //This one should exist because we need static method - even if he copies pushInGlobalStack
    public static void pushTask(Task task) {
        synchronized (gsNotEmpty) {
            synchronized (gsLock) {
                globalStack.push(task);
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
            tempResult += taskRes.area;
            tempAccuracyCount++;
        }
        addToResult(tempResult);
        addToAccuracyCount(tempAccuracyCount);
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
            localStackDropCount++;
            //push extra task from local stack to global
            //we leave 5 tasks to avoid extra calls to global stack
            for (int i = 0; i < localStackCapacity - 5; i++) {
                pushInGlobalStack(localStack.pop());
            }
        }
    }

    //Explanation: why is it OK to set finishedCondition in synced block and use it in unsynced
    //When it can fail: IF it was true, but became false
    //Why not: there can be only one process getting true on this condition on one time.
    //Others either are waiting (good) or somewhere in other part of the code (bad?)
    //If they are not waiting, they are out from while, so there was a task in GS when they got up
    //But no one except him could take it, as far as this code is synced. So GS wasn't empty, wrong.
    //IF it was false, but become true: impossible, the last one always will be the last one.
    private Task popFromGlobalStack() {
        boolean finishedCondition;
        synchronized (wtcLock) {
            workingThreadsCount--;
            finishedCondition = workingThreadsCount == 0 && globalStack.isEmpty();
        }
        if (finishedCondition) {
            //If we finished calculating the task (we are THE LAST THREAD)
            synchronized (gsNotEmpty) {
                //refill global stack with terminatorTasks and pop one for ourselves.
                synchronized (gsLock) {
                    for (int i = 0; i < livingThreadsCount; i++) {
                        globalStack.push(terminatorTask);
                    }
                    gsNotEmpty.notifyAll();
                    return globalStack.pop();
                }
            }
        } else {
            //If we aren't THE LAST THREAD
            synchronized (gsNotEmpty) {
                try {
                    while (globalStack.isEmpty())
                        gsNotEmpty.wait(); //wait until global stack refilled
                } catch (InterruptedException e) {
                }
                synchronized (wtcLock) {
                    workingThreadsCount++;
                }
                synchronized (gsLock) {
                    return globalStack.pop();
                }
            }
        }
    }

    private void pushInGlobalStack(Task task) {
        synchronized (gsNotEmpty) {
            synchronized (gsLock) {
                globalStack.push(task);
                gsNotEmpty.notify();
            }
        }
    }

    private void comeAlive() {
        synchronized (ltcLock) {livingThreadsCount++;}
        synchronized (wtcLock) {workingThreadsCount++;}
    }

    private void die() {
        synchronized (ltcLock) {livingThreadsCount--;}
    }

    //It is ok to be synchronized - we never else change result
    private synchronized void addToResult (double summand) {
        result += summand;
    }

    private synchronized void addToAccuracyCount (int summand) {
        accuracyCount += summand;
    }

    public static double getResult() {
        return result;
    }
}
