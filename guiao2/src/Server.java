import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class Server {
    public static void main(String[] args) throws IOException {
        Selector sel = SelectorProvider.provider().openSelector();
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress("localhost",12345));
        ss.configureBlocking(false);
        ss.register(sel,SelectionKey.OP_ACCEPT);
        System.out.println("Listeing on " + ss.getLocalAddress());
        while(true){
            sel.select();

            for(Iterator<SelectionKey> i = sel.selectedKeys().iterator(); i.hasNext();){
                SelectionKey key = i.next();

                if(key.isAcceptable()){
                    SocketChannel s = ss.accept();

                    if (s!=null){
                        s.configureBlocking(false);
                        SelectionKey nkey = s.register(sel,SelectionKey.OP_READ);
                        nkey.attach(new ChatSession(nkey));
                    }

                } else if(key.isReadable()){
                    ChatSession handler = (ChatSession) key.attachment();
                    ByteBuffer buf = ByteBuffer.allocate(100);
                    ((SocketChannel)key.channel()).read(buf);
                    handler.handleRead(buf);
                    key.interestOps(SelectionKey.OP_WRITE);

                } else if(key.isWritable()){
                    ChatSession handler = (ChatSession) key.attachment();
                    handler.handleWrite();
                    key.interestOps(SelectionKey.OP_READ);
                }
                i.remove();
            }

        }
    }
}
