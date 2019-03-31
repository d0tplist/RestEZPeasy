import org.ezpeasy.Peasy;

public class SampleApp {


    public static void main(String[] args) {

        Peasy.start(8181).publish(SampleWS.class);

    }
}
