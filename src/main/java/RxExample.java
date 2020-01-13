import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Executors;

public class RxExample {
    public static Flowable<AsynchronousSocketChannel> connect(AsynchronousChannelGroup group) {
        return Flowable.create(new FlowableOnSubscribe<AsynchronousSocketChannel>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter emitter) throws Throwable {
                try {
                    AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
                    InetSocketAddress addr = new InetSocketAddress("localhost", 50007);
                    channel.connect(addr, channel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
                        @Override
                        public void completed(Void result, AsynchronousSocketChannel attachment) {
                            emitter.onNext(channel);
                            emitter.onComplete();
                        }

                        @Override
                        public void failed(Throwable exc, AsynchronousSocketChannel attachment) { }
                    });
                } catch (Throwable t) { t.printStackTrace(); }
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static Flowable<ByteBuffer> send(AsynchronousSocketChannel channel, int id) {
        return Flowable.create(new FlowableOnSubscribe<ByteBuffer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<ByteBuffer> emitter) throws Throwable {
                try {
                    String request = "GET /" + id + "?10 HTTP/1.0\r\n\r\n";
                    ByteBuffer writeBuf = ByteBuffer.allocate(2048);
                    writeBuf.put(request.getBytes());
                    writeBuf.flip();
                    System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " send request");
                    channel.write(writeBuf, writeBuf, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            emitter.onNext(writeBuf);
                            emitter.onComplete();
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) { }
                    });
                } catch (Throwable t) { t.printStackTrace(); }
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static Flowable<ByteBuffer> recv(AsynchronousSocketChannel channel) {
        return Flowable.create(new FlowableOnSubscribe<ByteBuffer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<ByteBuffer> emitter) throws Throwable {
                try {
                    ByteBuffer readBuf = ByteBuffer.allocate(2048);
                    channel.read(readBuf, readBuf, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            emitter.onNext(readBuf);
                            emitter.onComplete();
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) { }
                    });
                } catch (Throwable t) { t.printStackTrace(); }
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Wait for starting...");
        Thread.sleep(10000);
        InetSocketAddress addr = new InetSocketAddress("localhost", 50007);
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory());
        System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " connect");

        for (int i = 0; i < 10; i++) {
            final int id = i;
            connect(group).flatMap(channel ->
                    send(channel, id).flatMap( writeBuf ->
                        recv(channel).map(readBuf -> {
                            System.out.println(Thread.currentThread().getName() + " " + LocalTime.now() + " response: " + new String(readBuf.array()));
                            return Optional.empty();
            }))).subscribe();
        }
        Thread.sleep(35000);
    }
}
