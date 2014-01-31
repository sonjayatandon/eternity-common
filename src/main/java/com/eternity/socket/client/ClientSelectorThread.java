package com.eternity.socket.client;

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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

public class ClientSelectorThread extends SelectorThread implements Runnable {
	private static Logger log = LogManager.getLogger(ClientSelectorThread.class);

	// who we're connecting to
	private InetAddress hostAddress;
	private int port;

	private Selector selector;
	private ByteBuffer buffer;
	// stores requests for changing the selector's interest
	private BlockingQueue<ChangeRequest> pendingChanges = new LinkedBlockingQueue<ChangeRequest>();
	// stores data waiting to be sent
	private Map<SocketChannel, BlockingQueue<ByteBuffer>> pendingData = new ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>>();
	// stores responses per channel
	private Map<Pair<SocketChannel, String>, ResponseHandler> responseHandlers = new ConcurrentHashMap<Pair<SocketChannel, String>, ResponseHandler>();
	// used by handleResponse
	StringBuffer responseParts = new StringBuffer();
	ResponseHandler handler = null;
	private int connectionRetryCount = 0;
	private static int MAX_CONNECTION_RETRIES = 5;
	

	private boolean poll = true;

	SocketChannel socketChannel;

	public ClientSelectorThread(InetAddress hostAddress, int port, int bufferSize) {
		this.hostAddress = hostAddress;
		this.port = port;
		this.buffer = ByteBuffer.allocate(bufferSize);

		try {
			// open a selector
			selector = Selector.open();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public void openConnection() {
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(hostAddress, port));
			pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
			selector.wakeup();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public void closeConnection() {
		if (socketChannel.isOpen()) {
			try {
				socketChannel.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	public void run() {
		try {
			while (poll) {
				// process channel change requests
				synchronized (pendingChanges) {
				while (!pendingChanges.isEmpty()) {
					ChangeRequest changeRequest;
					try {
						changeRequest = pendingChanges.take();
						try {
							changeRequest.socket.register(this.selector, changeRequest.ops);
						} catch (ClosedChannelException e) {
							log.error("", e);
							continue; // TODO : continue? or break?
						}
						break;
					} catch (InterruptedException e) {
						log.error("", e);
					}
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
							log.debug("invalid key!");
							continue;
						}

						int ready = key.readyOps();
						if ((ready & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT)
							finishConnection(key);
						if ((ready & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
							write(key);
						if ((ready & SelectionKey.OP_READ) == SelectionKey.OP_READ)
							read(key);
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
		}
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			socketChannel.finishConnect();
			// we've connected, reset retry count
			connectionRetryCount = 0;
		} catch (IOException e) {
			log.error("", e);
			key.cancel();
			return;
		}

		// back to write mode
		key.interestOps(SelectionKey.OP_WRITE);
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		buffer.clear();
		int bytesRead = 0;

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

		// copy the data to free the buffer
		byte[] responseData = new byte[bytesRead];
		System.arraycopy(buffer.array(), 0, responseData, 0, bytesRead);
		
		handleResponse(socketChannel, responseData, bytesRead);
	}

	private void handleResponse(SocketChannel socketChannel, byte[] data, int bytesRead) throws IOException {
		log.debug("processing: " + new String(data));
		for (int i = 0; i < data.length; i++) {
			if (data[i] == Constants.startOfTransmission) {
				handler = responseHandlers.get(new Pair<SocketChannel, String>(socketChannel, responseParts.toString()));
				log.debug("got handler " + handler + " from key " + responseParts.toString());
				// clear StringBuffer
				responseParts.delete(0, responseParts.length());
				continue;
			}
			if (data[i] == Constants.endOfTransmission) {
				handler.setMessage(responseParts.toString());
				log.debug("setting message " + responseParts.toString() + " on handler " + handler);
				// clear StringBuffer
				responseParts.delete(0, responseParts.length());				
				handler.run();				
				continue;
			}
			responseParts.append((char) data[i]);
		}
	}

	public void send(String data, ResponseHandler responseHandler) {
		
		if (socketChannel.socket().isClosed()) {
			connectionRetryCount++;
			if (connectionRetryCount < MAX_CONNECTION_RETRIES) {
				log.info("socket is closed, retrying connect");
				openConnection();
			} else {
				log.info("retried opeing connection " + MAX_CONNECTION_RETRIES + " times without success, giving up :-(");
			}
		}
		
		String messageId = MessageIdGenerator.getId();
		responseHandlers.put(new Pair<SocketChannel, String>(socketChannel, messageId), responseHandler);
		
		synchronized (pendingData) {
			BlockingQueue<ByteBuffer> queue = pendingData.get(socketChannel);
			if (queue == null) {
				queue = new LinkedBlockingQueue<ByteBuffer>();
				pendingData.put(socketChannel, queue);
			}
			byte[] message = (messageId + Constants.startOfTransmission + data + Constants.endOfTransmission).getBytes();
			queue.add(ByteBuffer.wrap(message));
			pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_WRITE));
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
					// remove processed element
					queue.remove(0);
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

	public void stopPolling() {
		this.poll = false;
	}
}
