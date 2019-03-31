import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class TestMethodParamNames {


    public static void main(String[] args) throws Exception {

        Method[] methods = SampleWS.class.getDeclaredMethods();

        for (Method method : methods) {
            System.out.println(method.getName());
            for (Parameter parameter : method.getParameters()) {
                System.out.println("\t"+parameter.getName());
            }
        }
    }
}
