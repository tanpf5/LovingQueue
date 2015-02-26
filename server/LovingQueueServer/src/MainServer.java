import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;



public class MainServer implements Runnable{

	public Queue<QueueElement> queue;
	public ReadWriteLock rwlock; //read write lock for queue
	public int currentNumber=0;
	public String name;
	public int port;
	public String ip;
	public boolean status;  //0 is to be opened.1 is already open
	public Thread serverThread;
	public MainServer() {
		rwlock= new ReentrantReadWriteLock();
		queue=new LinkedList<QueueElement>();
		status=false;
	}
	
	public void start() throws BindException{
		ServerSocket ss=null;
		while (status) {
			try
			{
				ss = new ServerSocket(port);
				System.out.println("Listening:\n");
				ss.setSoTimeout(30000);
				Socket s=ss.accept();
				ss.close();
				BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter output = new PrintWriter(s.getOutputStream(),true);
				String message;
				while ((message=input.readLine())!=null) {
					if (message.equals("join_queue")) {
						QueueElement newElement=genNewElement();
						System.out.println(String.format("ADD:%d %s",newElement.queueNumber,newElement.password));
						output.println(String.format("%d %s", newElement.queueNumber,newElement.password));
					}
					if (message.equals("query_queue")) {
						rwlock.readLock().lock();
						String tmp=queue.peek()!=null?String.format("%d",queue.peek().queueNumber):"None";
						rwlock.readLock().unlock(); 
						System.out.println("query!\n");
						output.println(tmp);
					}
				}
				s.close();	
			} catch (BindException e3) {
				throw e3;
			}catch (SocketTimeoutException e2) {
				if (ss!=null) {
					try {
						ss.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Timeout");
			}catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	public QueueElement genNewElement() {
		QueueElement newElement=new QueueElement(++currentNumber,generatePassword());
		rwlock.writeLock().lock();
		queue.offer(newElement);
		rwlock.writeLock().unlock();
		return newElement;
	}
	
	private String generatePassword() {
		int password=0;
		Random rand=new Random();
		for (int i=0;i<4;++i)
			password=password*10+rand.nextInt(10)+0;
		return String.format("%04d", password);
		
	}
	
	@Override
	public void run() {
		try {
			status=true;
			start();
		
		} catch (BindException e) {
			status=false;
			// TODO Auto-generated catch block
			SwingUtilities.invokeLater(new Runnable() {   
				public void run() {    
					invokeRemoteService();// 可能需要等待  
				}
				private void invokeRemoteService() {
					
					JOptionPane.showMessageDialog(null,"Address already in use!","Error", JOptionPane.ERROR_MESSAGE);
				}   
	        });  
			return;
		}
	}
	
	public void stop(){
		stop();
	}
}

class QueueElement {
	public int queueNumber;
	public String password;
	public QueueElement(int num,String password) {
		queueNumber=num;
		this.password=password;
	}
}
