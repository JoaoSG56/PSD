import java.util.concurrent.Flow;

public class Publisher implements Flow.Publisher {
    private Flow.Subscriber subscriber;
    @Override
    public void subscribe(Flow.Subscriber subscriber) {
        this.subscriber=subscriber;
    }
}
