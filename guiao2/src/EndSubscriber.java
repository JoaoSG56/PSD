import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;

public class EndSubscriber implements Flow.Subscriber {
    private Flow.Subscription subscription;
    private List<String> consumedElements = new LinkedList<>();
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription=subscription;
    }

    @Override
    public void onNext(Object item) {
        System.out.println("Got: " + item);
        this.consumedElements.add((String)item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("Done");
    }
}
