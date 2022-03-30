import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ChatSession extends Handler {
    private SelectionKey key;
    private ByteBuffer stored; // possibly a queue...

    public ChatSession(SelectionKey key) {
// initialization
        this.key = key;
    }
    public void handleRead(ByteBuffer in) throws IOException {
// store input
        in.flip();
        System.out.println(new String(in.array(),in.arrayOffset(),in.limit(), StandardCharsets.UTF_8));
        this.stored = in.duplicate();
    }
    public void handleWrite() throws IOException{
// write from stored
        ((SocketChannel)this.key.channel()).write(this.stored);
    }

    public SelectionKey getKey(){
        return this.key;
    }

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
