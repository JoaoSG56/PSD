import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ChatSession extends Handler {
    private SelectionKey key;
    private EndSubscriber subscriber;

    public ChatSession(SelectionKey key) {
// initialization
        this.key = key;
        this.subscriber = new EndSubscriber();
    }
    public void handleRead(ByteBuffer in) throws IOException {
// store input
        this.subscriber.onNext(new String(in.flip().array(),in.arrayOffset(),in.limit(), StandardCharsets.UTF_8));

    }
    /*
    public void handleWrite() throws IOException{
// write from stored
        ((SocketChannel)this.key.channel()).write(this.stored);
    }
    */

    @Override
    public void publish(LogRecord record) {
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
