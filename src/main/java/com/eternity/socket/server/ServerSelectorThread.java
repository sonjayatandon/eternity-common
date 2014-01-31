package com.eternity.socket.server;

/*
The MIT License (MIT)

Copyright (c) 2011 Sonjaya Tandon

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. * 
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.eternity.socket.common.ChangeRequest;
import com.eternity.socket.common.Constants;
import com.eternity.socket.common.SelectorThread;

public class ServerSelectorThread extends SelectorThread implements Runnable {
	private static Logger log = LogManager.getLogger(ServerSelectorThread.class);

	private int port;
	private Selector selector;
	private ServerSocketChannel sChannel;
	private ServerSocket socket;
	private ByteBuffer buffer;
	// holds received data
	private BlockingQueue<DataEvent> queue;

	// stores requests for changing the selector's interest
	private BlockingQueue<ChangeRequest> pendingChanges = new LinkedBlockingQueue<ChangeRequest>();
	// stores data waiting to be sent
	private Map<SocketChannel, BlockingQueue<ByteBuffer>> pendingData = new ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>>();
	private StringBuffer responseParts = new StringBuffer();
	
	private boolean poll = true;

	public ServerSelectorThread(BlockingQueue<DataEvent> queue, int port, int bufferSize) {
		this.queue = queue;
		this.port = port;
		this.buffer = ByteBuffer.allocate(bufferSize);
	}

	public void run() {
		try {
			try {
				// open a selector
				selector = Selector.open();
				// open a channel
				sChannel = ServerSocketChannel.open();
				sChannel.configureBlocking(false);
				// open a socket
				// TODO : specify an IP or leave it wide open?
				socket = sChannel.socket();
				socket.bind(new InetSocketAddress(port));
				sChannel.register(selector, SelectionKey.OP_ACCEPT);
			} catch (IOException e) {
				log.error("", e);
			}

			while (poll) {
				// process channel change requests
				synchronized (pendingChanges) {

				ChangeRequest changeRequest;
				while ((changeRequest = pendingChanges.poll()) != null) {
					SelectionKey key = changeRequest.socket.keyFor(selector);
					key.interestOps(changeRequest.ops);
				}
				}

				try {
					// block until we have something to do
					selector.select();
				} catch (IOException e) {
					log.error("", e);
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				while (it.hasNext()) {
					try {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();

						if (!key.isValid()) {
							log.info("invalid key!");
							continue;
						}

						int ready = key.readyOps();
						if ((ready & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
							read(key);
						}
						if ((ready & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
							write(key);
						}
						if ((ready & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
							accept(key);
						}
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}
		} finally {
			try {
				if (selector != null && selector.isOpen()) {
					selector.close();
				}
			} catch (IOException e) {
			}
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ie) {
			}
			try {
				if (sChannel != null && sChannel.isOpen()) {
					sChannel.close();
				}
			} catch (IOException ie) {
			}
		}
	}
	
	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		int bytesRead = 0;
		synchronized (buffer) {
			buffer.clear();

			try {
				bytesRead = socketChannel.read(buffer);
			} catch (IOException e) {
				// client closed connection, cleanup
				log.error("", e);
				key.cancel();
				socketChannel.close();
				return;
			}

			if (bytesRead == -1) {
				key.channel().close();
				key.cancel();
				return;
			}
		}
		// queue a copy of the data so we can re-use the buffer
		byte[] data = new byte[bytesRead];
		System.arraycopy(buffer.array(), 0, data, 0, bytesRead);

		// break up the data into discrete messages
		for (int i = 0; i < data.length; i++) {
			if (data[i] == Constants.endOfTransmission) {
				// add EOT char
				responseParts.append((char) data[i]);
				// send out data for processing
				queue.add(new DataEvent(socketChannel, responseParts.toString().getBytes()));
				// clear StringBuffer				
				responseParts.delete(0, responseParts.length());				
				continue;
			}
			responseParts.append((char) data[i]);
		}
		
	}

	public void send(SocketChannel socketChannel, byte[] data) {
		synchronized (pendingChanges) {
			pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			synchronized (pendingData) {
				BlockingQueue<ByteBuffer> queue = pendingData.get(socketChannel);
				if (queue == null) {
					queue = new LinkedBlockingQueue<ByteBuffer>();
					pendingData.put(socketChannel, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}
		// let the selector know there's data to send
		selector.wakeup();
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
			BlockingQueue<ByteBuffer> queue = pendingData.get(socketChannel);
			// process pendingData
			while (queue != null && !queue.isEmpty()) {
				ByteBuffer buffer;
				try {
					buffer = queue.take();
					socketChannel.write(buffer);
					// socket is full and we still have data
					if (buffer.remaining() > 0) {
						break;
					}
				} catch (InterruptedException e) {
					log.error("", e);
				}
			}
			// done writing, switch back to read interest
			if (queue != null && queue.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private void accept(SelectionKey key) throws IOException {
		// only server socket channels have pending interest
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		Socket newSocket = socketChannel.socket();
		newSocket.setKeepAlive(true);

		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	public void stopPolling() {
		this.poll = false;
	}
}
