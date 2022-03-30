import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import static com.ea.async.Async.await;



public class Echo {
    public static void ciclo(FutureSocketChannel s){
        final var bb = ByteBuffer.allocate(100);
        s.read(bb)
                .thenCompose(n->{
                    bb.flip();
                    return s.write(bb);})
                .thenRun(()->ciclo(s));
    }

    public static void main(String[] args) throws Exception {
        var ss = new FutureServerSocketChannel();
        ss.bind(new InetSocketAddress("localhost",12345));

        ss.accept()
                .thenAccept(Echo::ciclo);
        while(true){
            Thread.sleep(1000);
        }
    }
}
