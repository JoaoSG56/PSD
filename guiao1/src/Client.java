import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

class ClientServerResponses implements Runnable{
    private SocketChannel socket;

    public ClientServerResponses(SocketChannel socket){
        this.socket = socket;
    }

    public void run(){
        ByteBuffer bb = ByteBuffer.allocate(100);
        try {

            while (true) {
                this.socket.read(bb);
                bb.flip();
                System.out.println("RESPOSTA: ");
                System.out.println(new String(bb.array(),bb.arrayOffset(),bb.limit(),StandardCharsets.UTF_8));
                bb.clear();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}

public class Client {
    public static void main(String[] args) {
        try {
            SocketChannel socket = SocketChannel.open();
            SocketAddress ss = new InetSocketAddress("127.0.0.1",12345);

            socket.connect(ss);

            System.out.println("Connected");

            ByteBuffer bb;

            Thread t = new Thread(new ClientServerResponses(socket));
            t.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            System.out.print("echo>: ");
            String input = reader.readLine();

            while(!input.equals("\n")){
                //System.out.println("enviando " + input);
                bb = ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8));
                socket.write(bb);
                //System.out.println("mensagem enviada");
                bb.clear();
                //System.out.print("echo>: ");
                input = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
