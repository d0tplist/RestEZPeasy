import spark.Response;

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

    public JSONExample jsonExample(JSONExample example) {
        return example;
    }

    private String notpublished(String value) {
        return value;
    }

    protected String notevenpublished(String value, Response response) {
        response.redirect("http://www.anotherurl.peasy/");
        return value;
    }


}
