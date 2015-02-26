package com.hci.lovingqueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MessageService extends Service {	 
	    //获取消息线程
	    private MessageThread messageThread = null;
	 
	    //点击查看
	    private Intent messageIntent = null;
	    private PendingIntent messagePendingIntent = null;
	 
	    //通知栏消息
	    private int messageNotificationID = 1000;
	    private Notification messageNotification = null;
	    private NotificationManager messageNotificatioManager = null;
	 
	    public IBinder onBind(Intent intent) {
	        return null;
	    }
	     
	    @Override
		public void onCreate() {
	    	 //初始化
	        messageNotification = new Notification();
	        messageNotification.tickerText = "LovingQueue";
	        messageNotification.icon = R.drawable.ic_launcher;
	        messageNotification.defaults = Notification.DEFAULT_SOUND;
	        messageNotificatioManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	        //点击跳转的activity
	        messageIntent = new Intent(this, MyQueueListActivity.class);
	        messagePendingIntent = PendingIntent.getActivity(this,0,messageIntent,0);
	     
	        //开启线程
	        messageThread = new MessageThread();
	        messageThread.isRunning = true;
	        messageThread.start();
	       // Toast.makeText(MessageService.this, "Starting service", Toast.LENGTH_LONG).show();
			super.onCreate();
		}

		/**
	     * 从服务器端获取消息
	     *
     */
	    class MessageThread extends Thread{
	        //运行状态，下一步骤有大用
	        public boolean isRunning = true;
	        public void run() {
	            while(isRunning){
	                try {
	                    //休息10分钟
	                    Thread.sleep(30000);
	                    //获取服务器消息
	                    for (int i = 0;i < MyQueueManager.myqueue.size();i++){
	                    	QueueElement allInfo = MyQueueManager.myqueue.get(i);
	                    	if (allInfo.reminder == 0){
		                		Socket s = new Socket();
		                		try{
		                			String serverStr = allInfo.serverAddress;
		                			int serverPort=22222;
		                			String[] splitServerAddr=serverStr.split(":");
		                			if (splitServerAddr.length>1) {
		                				serverStr=splitServerAddr[0];
		                				serverPort=Integer.parseInt(splitServerAddr[1]);
		                			}
		                			InetAddress serverAddr = InetAddress.getByName(serverStr);
		                			s.connect(new InetSocketAddress(serverAddr,serverPort),5000);
		                			OutputStream out = s.getOutputStream();
		                			PrintWriter output = new PrintWriter(out,true);
		                			BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
		                			output.println("query_queue");
		                			String reply=input.readLine();
		                			
		                			int currentNum;
		                			if (reply.contentEquals("None"))
		                				currentNum = -2;
		                			else currentNum = Integer.parseInt(reply);
		                			int queueNum = allInfo.queuenum;
		                			
		                			if (queueNum - currentNum <= 2){
		                				MyQueueManager.myqueue.get(i).reminder = 1;
		    	                        //更新通知栏
		    	                        messageNotification.setLatestEventInfo(MessageService.this,"Your turn in "+allInfo.serverName+" is coming!","CurrentNum is "+currentNum,messagePendingIntent);
		    	                        messageNotificatioManager.notify(messageNotificationID, messageNotification);
		    	                        //每次通知完，通知ID递增一下，避免消息覆盖掉
		    	                        messageNotificationID++;
		                			}
		                			
		                			s.close();
		                		} catch (UnknownHostException e1) {
		                		} catch (ConnectException e2) { 
		               
		                		} catch (SocketTimeoutException e3) {
		                			
		                		} catch (IOException e4) {
		                			
		                		}
		                    	
		                    }
	                    }
	                 
               } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
	    }
		@Override
			public void onDestroy() {
			          //  System.exit(0);
			            //或者，二选一，推荐使用System.exit(0)，这样进程退出的更干净
			            messageThread.isRunning = false;
			            super.onDestroy();
			}
    /**
     * 这里以此方法为服务器Demo，仅作示例
    * @return 返回服务器要推送的消息，否则如果为空的话，不推送
     */

}