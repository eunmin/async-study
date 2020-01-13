import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalTime;
import java.util.concurrent.Executors;

public class NioExample {

    public static void connect(AsynchronousChannelGroup group, CompletionHandler<Void, AsynchronousSocketChannel> callback) {
        try {
            AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
            InetSocketAddress addr = new InetSocketAddress("localhost", 50007);
            channel.connect(addr, channel, callback);
        } catch (Throwable t) { t.printStackTrace(); }
    }

    public static void send(AsynchronousSocketChannel channel, int id, CompletionHandler<Integer, ByteBuffer> callback) {
        try {
            String request = "GET /" + id + "?10 HTTP/1.0\r\n\r\n";
            ByteBuffer writeBuf = ByteBuffer.allocate(2048);
            writeBuf.put(request.getBytes());
            writeBuf.flip();
            System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " send request");
            channel.write(writeBuf, writeBuf, callback);
        } catch (Throwable t) { t.printStackTrace(); }
    }

    public static void recv(AsynchronousSocketChannel channel, CompletionHandler<Integer, ByteBuffer> callback) {
        try {
            ByteBuffer readBuf = ByteBuffer.allocate(2048);
            channel.read(readBuf, readBuf, callback);
        } catch (Throwable t) { t.printStackTrace(); }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Wait for starting...");
        Thread.sleep(10000);
        InetSocketAddress addr = new InetSocketAddress("localhost", 50007);
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory());
        System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " connect");
        for (int i = 0; i < 10; i++) {
            final int id = i;
            connect(group, new CompletionHandler<Void, AsynchronousSocketChannel >() {
                @Override
                public void completed(Void result, AsynchronousSocketChannel channel) {
                    send(channel, id, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer buffer) {
                            recv(channel, new CompletionHandler<Integer, ByteBuffer>() {
                                @Override
                                public void completed(Integer result, ByteBuffer readBuf) {
                                    System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " response: " + new String(readBuf.array()));
                                    try { channel.close(); } catch (IOException e) { e.printStackTrace(); }
                                }
                                @Override
                                public void failed(Throwable exc, ByteBuffer attachment) {
                                }
                            });
                        }
                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                        }
                    });
                }
                @Override
                public void failed(Throwable exc, AsynchronousSocketChannel attachment) { }
            });
        }
        Thread.sleep(35000);
    }
}
