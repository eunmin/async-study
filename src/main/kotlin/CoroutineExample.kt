import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousChannelGroup.withFixedThreadPool
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.time.LocalTime
import java.util.concurrent.Executors.defaultThreadFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun connect(group: AsynchronousChannelGroup): AsynchronousSocketChannel = suspendCoroutine { cont ->
    val channel = AsynchronousSocketChannel.open(group)
    channel.connect(InetSocketAddress("localhost", 50007), channel, object: CompletionHandler<Void, AsynchronousSocketChannel> {
        override fun completed(result: Void?, attachment: AsynchronousSocketChannel?) {
            cont.resume(channel)
        }
        override fun failed(exc: Throwable?, attachment: AsynchronousSocketChannel?) {}
    })
}

suspend fun send(channel: AsynchronousSocketChannel, id: Int): ByteBuffer = suspendCoroutine { cont ->
    val request = "GET /${id}?10 HTTP/1.0\r\n\r\n"
    val writeBuf = ByteBuffer.allocate(2048)
    writeBuf.put(request.toByteArray())
    writeBuf.flip()
    println("${Thread.currentThread().getName()} ${LocalTime.now()} send request")
    channel.write(writeBuf, writeBuf, object: CompletionHandler<Int, ByteBuffer> {
        override fun completed(result: Int?, attachment: ByteBuffer?) {
            cont.resume(writeBuf)
        }
        override fun failed(exc: Throwable?, attachment: ByteBuffer?) {}
    })
}

suspend fun recv(channel: AsynchronousSocketChannel): ByteBuffer = suspendCoroutine { cont ->
    val readBuf = ByteBuffer.allocate(2048);
    channel.read(readBuf, readBuf, object: CompletionHandler<Int, ByteBuffer> {
        override fun completed(result: Int?, attachment: ByteBuffer?) {
            cont.resume(readBuf)
        }
        override fun failed(exc: Throwable?, attachment: ByteBuffer?) {}
    })
}

fun main() {
    println("Wait for starting...")
    Thread.sleep(10000)
    val group = withFixedThreadPool(10, defaultThreadFactory())

    runBlocking {
        for (id in 0 until 10) {
            launch {
                val channel = connect(group)
                send(channel, id)
                val readBuf = recv(channel)
                println(String(readBuf.array()))
            }
        }
    }
}