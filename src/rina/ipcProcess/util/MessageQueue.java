
/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

/**
 * contains the two queues of incoming and outgoing messages on different threads
 * Note: now we only use the incoming queue(receive queue)
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */


package rina.ipcProcess.util;

import java.util.*;
import java.util.concurrent.Semaphore;

public class MessageQueue {

	private  Queue<byte[]> receiveQueue = null;

	private  Queue<byte[]> sendQueue = null;

	/**
	 * the following are for sync receive queue
	 */
	private boolean recv_1st_flag = false;

	//private Semaphore recv_mutex_add = new Semaphore(1);


	private Semaphore recv_mutex_add = new Semaphore(1);

	private Semaphore recv_mutex_get = new Semaphore(0);




	/**
	 * Constructor
	 */
	public MessageQueue()
	{
		receiveQueue = new LinkedList<byte[]>();
		sendQueue = new LinkedList<byte[]>();
	}


	/**
	 * add a message to the queue of received message
	 * @param data message 
	 */
	public  void addReceive(byte[] data)
	{
		try {
			this.recv_mutex_add.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		receiveQueue.offer(data);


		this.recv_mutex_get.release();


	}
	/**
	 * 
	 * @return the polled message from the queue
	 */
	public   byte[] getReceive()
	{

		if(recv_1st_flag == true)
		{
			this.recv_mutex_add.release();
		}else
		{
			recv_1st_flag = true;
		}


		try {
			this.recv_mutex_get.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return receiveQueue.poll();

	}

	/**
	 * add a message to the queue of sent messages
	 * @param data
	 */
	public synchronized void addSend(byte[] data)
	{
		sendQueue.offer(data);
	}
	/**
	 * 
	 * @return the message polled from the queue of messages to send
	 */
	public synchronized byte[] getSend()
	{
		return sendQueue.poll();
	}

	/**
	 * @return the Queue of received messages
	 */
	public synchronized Queue<byte[]> getReceiveQueue() {
		return receiveQueue;
	}



	/**
	 * @param receiveQueue the receiveQueue to set
	 */
	public synchronized void setReceiveQueue(Queue<byte[]> receiveQueue) {
		this.receiveQueue = receiveQueue;
	}



	/**
	 * @return the sendQueue
	 */
	public synchronized Queue<byte[]> getSendQueue() {
		return sendQueue;
	}



	/**
	 * @param sendQueue the sendQueue to set
	 */
	public synchronized void setSendQueue(Queue<byte[]> sendQueue) {
		this.sendQueue = sendQueue;
	}

}