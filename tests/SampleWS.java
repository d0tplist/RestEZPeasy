
public class SampleWS {

    public String sayHello() {
        return "Hello World!";
    }

    public String sum(int a, int b) {
        return "The sum is: " + (a + b);
    }

    public JSONExample json(String name, int age) {
        return new JSONExample(name, age);
    }

    private String notpublished(String value) {
        return value;
    }

    protected String notevenpublished(String value) {
        return value;
    }


}
