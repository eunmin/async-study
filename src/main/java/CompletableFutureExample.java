import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class CompletableFutureExample {

    public static CompletableFuture<AsynchronousSocketChannel> connect(AsynchronousChannelGroup group) {
        CompletableFuture<AsynchronousSocketChannel> f = new CompletableFuture<>();
        try {
            AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
            InetSocketAddress addr = new InetSocketAddress("localhost", 50007);
            channel.connect(addr, channel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
                @Override
                public void completed(Void result, AsynchronousSocketChannel attachment) {
                    f.complete(channel);
                }

                @Override
                public void failed(Throwable exc, AsynchronousSocketChannel attachment) { }
            });
        } catch (Throwable t) { t.printStackTrace(); }
        return f;
    }

    public static CompletableFuture<ByteBuffer> send(AsynchronousSocketChannel channel, int id) {
        CompletableFuture<ByteBuffer> f = new CompletableFuture<>();
        try {
            String request = "GET /" + id + "?10 HTTP/1.0\r\n\r\n";
            ByteBuffer writeBuf = ByteBuffer.allocate(2048);
            writeBuf.put(request.getBytes());
            writeBuf.flip();
            System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " send request");
            channel.write(writeBuf, writeBuf, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    f.complete(writeBuf);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) { }
            });
        } catch (Throwable t) { t.printStackTrace(); }
        return f;
    }

    public static CompletableFuture<ByteBuffer> recv(AsynchronousSocketChannel channel) {
        CompletableFuture<ByteBuffer> f = new CompletableFuture<>();
        try {
            ByteBuffer readBuf = ByteBuffer.allocate(2048);
            channel.read(readBuf, readBuf, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    f.complete(readBuf);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) { }
            });
        } catch (Throwable t) { t.printStackTrace(); }
        return f;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Wait for starting...");
        Thread.sleep(10000);
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory());
        System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " connect");

        for (int i = 0; i < 10; i++) {
            final int id = i;
            connect(group).thenCompose(socket ->
                    send(socket, id).thenCompose(socket1 ->
                            recv(socket).thenAccept(readBuf ->
                                    System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " response: " + new String(readBuf.array())))));
        }
        Thread.sleep(35000);
    }
}
