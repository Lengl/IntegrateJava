//This class represents a segment on the axis with left & right borders
//And some-kind-of-function above this segement
public class Task {
    //This is OK difference between whole segment area and sum of two little segments
    public final static double accuracy = 0.000001;

    private double leftEnd;
    private double rightEnd;
    private double funcLeft;
    private double funcRight;
    private double area;

    public double getFuncLeft() {
        return funcLeft;
    }

    public double getFuncRight() {
        return funcRight;
    }

    public double getArea() {
        return area;
    }

    public Task (double leftEnd, double rightEnd) {
        if (leftEnd > rightEnd)
            throw new IllegalArgumentException("bad segment: left > right");
        this.leftEnd = leftEnd;
        this.funcLeft = function(leftEnd);
        this.rightEnd = rightEnd;
        this.funcRight = function(rightEnd);
        this.area = (rightEnd - leftEnd) * (funcRight + funcLeft) / 2;
    }

    //compares area of a whole segment and sum of left and right segments
    //(look if function is straight enough on this segments)
    //if good - returns it's area & null pointer for the next task
    //else - refreshes itself and return not null object for another task
    public TaskReturnValues calculate() {
        double mid = (rightEnd + leftEnd) / 2;
        Task taskLeft = new Task(leftEnd, mid);
        Task taskRight = new Task(mid, rightEnd);
        double diff = taskLeft.getArea() + taskRight.getArea() - this.area;
        //Here is the deal: We put left segment in stack and keep right segment calculating
        //This could be VERY BAD if right is ALWAYS calculated accurate enough
        //TODO: consider random left & right segments in the stack
        //TODO: get to know if you actually need Double.compare or just <= is OK
        if (Double.compare(Math.abs(diff), accuracy) <= 0) {
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
