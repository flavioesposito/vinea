/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
 
package rina.rib.util;

import java.util.*;
import java.util.concurrent.Semaphore;

public class EventQueue {

	private  Queue<Event> receiveQueue = null;

	private  Queue<Event> sendQueue = null;

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
	public EventQueue()
	{
		receiveQueue = new LinkedList<Event>();
		sendQueue = new LinkedList<Event>();
	}


	/**
	 * add a message to the queue of received message
	 * @param message 
	 */
	public  void addReceive(Event event)
	{
		try {
			this.recv_mutex_add.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		receiveQueue.offer(event);


		this.recv_mutex_get.release();


	}
	/**
	 * 
	 * @return the polled message from the queue
	 */
	public   Event getReceive()
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
	public synchronized void addSend(Event event)
	{
		sendQueue.offer(event);
	}
	/**
	 * 
	 * @return the message polled from the queue of messages to send
	 */
	public synchronized Event getSend()
	{
		return sendQueue.poll();
	}

	/**
	 * @return the Queue of received messages
	 */
	public synchronized Queue<Event> getReceiveQueue() {
		return receiveQueue;
	}



	/**
	 * @param receiveQueue the receiveQueue to set
	 */
	public synchronized void setReceiveQueue(Queue<Event> receiveQueue) {
		this.receiveQueue = receiveQueue;
	}



	/**
	 * @return the sendQueue
	 */
	public synchronized Queue<Event> getSendQueue() {
		return sendQueue;
	}



	/**
	 * @param sendQueue the sendQueue to set
	 */
	public synchronized void setSendQueue(Queue<Event> sendQueue) {
		this.sendQueue = sendQueue;
	}

}