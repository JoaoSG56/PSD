import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

class ClientHandler implements Runnable {
    private SocketChannel s;
    private ArrayList<SocketChannel> receivers;

    public ClientHandler(SocketChannel s,ArrayList<SocketChannel> receivers){
        this.s = s;
        this.receivers = receivers;
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(100);
        while(true) {
            try {
                s.read(buf);
                System.out.println("mensagem lida");
                buf.flip();
                for (SocketChannel r : receivers) {
                    r.write(buf.duplicate());
                    System.out.println("mensagem enviada");
                }
                System.out.println("mensagens enviadas");
                buf.clear();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

public class Server{
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress("127.0.0.1",12345));
        ArrayList<SocketChannel> receivers = new ArrayList<>();
        System.out.println("Listeing on " + ss.getLocalAddress());
        while(true){
            SocketChannel s = ss.accept();
            System.out.println("Connection received " + s.getLocalAddress());
            receivers.add(s);
            Thread t = new Thread(new ClientHandler(s,receivers));
            t.start();
        }
    }

}

