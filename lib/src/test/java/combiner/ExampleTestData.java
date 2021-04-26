package combiner;

public class ExampleTestData implements TestData {

    private String a;
    private String b;
    private int c;
    private double d;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    @Override
    public String toString() {
        return String.format("a=%s; b=%s; c=%d; d=%f", a, b, c, d);
    }

}
