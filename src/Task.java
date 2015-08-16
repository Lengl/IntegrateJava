public class Task {
    private final static double accuracy = 0.000001;

    private double leftEnd;
    private double rightEnd;
    private double funcLeft;
    private double funcRight;
    private double area;

    public double getFuncLeft() {
        return funcLeft;
    }

    public double getArea() {
        return area;
    }

    public Task (double leftEnd, double rightEnd) {
        this.leftEnd = leftEnd;
        this.funcLeft = function(leftEnd);
        this.rightEnd = rightEnd;
        this.funcRight = function(rightEnd);
        this.area = (rightEnd - leftEnd) * (funcRight + funcLeft) / 2;
    }

    public TaskReturnValues calculate() {
        double mid = (rightEnd + leftEnd) / 2;
        Task taskLeft = new Task(leftEnd, mid);
        Task taskRight = new Task(mid, rightEnd);
        double diff = taskLeft.getArea() + taskRight.getArea() - this.area;
        if (Math.abs(diff) < accuracy) {
            return new TaskReturnValues(this.area, null);
        }
        else {
            this.leftEnd = mid;
            this.funcLeft = taskRight.getFuncLeft();
            this.area = taskRight.getArea();
            return new TaskReturnValues(0, taskLeft);
        }
    }

    //This is exactly the function we are trying to integrate
    private static double function(double x) {
        double temp1 = 1 / x;
        double temp2 = Math.sin(temp1);
        return temp2 * temp2 * temp1 * temp1;
    }
}
