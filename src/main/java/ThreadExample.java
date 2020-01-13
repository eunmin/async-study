import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadExample {

    public static Socket connect() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost", 50007), 3000);
        } catch (IOException e) { e.printStackTrace(); }
        return socket;
    }

    public static void send(Socket socket, int id) {
        try {
            String request = "GET /" + id + "?10 HTTP/1.0\r\n\r\n";
            System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " send: Hello");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(request);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void recv(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " recv: " + in.readLine());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Wait for starting...");
        Thread.sleep(10000);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        for(int i = 0; i < 10; i++) {
            final int id = i;
            executor.submit(() -> {
                Socket socket = connect();
                send(socket, id);
                recv(socket);
            });
        }

        Thread.sleep(35000);
    }
}
