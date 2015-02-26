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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyQueueListActivity extends ListActivity {

	private List<Map<String, Object>> mData;
	private MyAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mData = getData();
		adapter = new MyAdapter(this);
		setListAdapter(adapter);
	}
	
	
	 // ListView 中某项被选中后的逻辑
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
         
		Socket s = new Socket();
		try{
			String serverIpPort = (String)mData.get(position).get("list_server_address");
			int serverPort=22222;
			String serverStr = serverIpPort;
			String[] splitServerAddr=serverIpPort.split(":");
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
				currentNum = -1;
			else currentNum = Integer.parseInt(reply);
			String queueNum_s = (String)(mData.get(position).get("list_queue_num"));
			int queueNum = Integer.parseInt(queueNum_s);
			String serverName = (String)mData.get(position).get("list_server_name");
			
			if (queueNum >= currentNum && currentNum != -1)//查询
				new AlertDialog.Builder(this)
	        	.setTitle("CurrentNum")
	        	.setMessage("CurrentNum:\n"+reply)
	        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        		@Override
	        		public void onClick(DialogInterface dialog, int which) {
	        		}
	        	})
	        	.show();	
			else //是否重新排队
				new AlertDialog.Builder(this)
        	.setMessage("Your number is out of date.\n"+"Do you want to queue up again?")
        	.setPositiveButton("YES", new QueueAgainOnClickListener(adapter,position,serverIpPort,serverName))
        	.setNegativeButton("NO", new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int which) {
        		}
        	})        	
        	.show();	
			s.close();
		} catch (UnknownHostException e1) {
			Toast.makeText(this, "Unknown host", Toast.LENGTH_LONG).show();
		} catch (ConnectException e2) { 
			Toast.makeText(this, "Connection Refused", Toast.LENGTH_LONG).show();
		} catch (SocketTimeoutException e3) {
			Toast.makeText(this, "Cant not connect to the server", Toast.LENGTH_LONG).show();
		} catch (IOException e4) {
			Toast.makeText(this, e4.toString(), Toast.LENGTH_LONG).show();
		}
    }
    
	
	
	private List<Map<String,Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i=0;i<MyQueueManager.myqueue.size();++i) {
			if (MyQueueManager.myqueue.get(i)==null)
				continue;
			map=new HashMap<String,Object>();
			map.put("list_server_name", MyQueueManager.myqueue.get(i).serverName);
			map.put("list_server_address", MyQueueManager.myqueue.get(i).serverAddress);
			map.put("list_queue_num",  String.format("%d",MyQueueManager.myqueue.get(i).queuenum));
			map.put("list_password",  MyQueueManager.myqueue.get(i).password);
			list.add(map);
		}
		return list;
	}
	
	public final class ViewHolder {
		public TextView serverName;
		public TextView queuenum;
		public TextView password;
		public Button deleteBtn;
	}
	
	public class MyAdapter extends BaseAdapter{
		 
        private LayoutInflater mInflater;
         
         
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        
        class MyOnDeleteClickListener implements View.OnClickListener {

        	private int position;
        	private MyAdapter map;
        	
        	public MyOnDeleteClickListener(MyAdapter ma,int p) {
        		map = ma;
        		position = p;
        	}
			@Override
			public void onClick(View arg0) {
				new AlertDialog.Builder(MyQueueListActivity.this)
	        	.setMessage("Are you sure to delete this item?")
	        	.setPositiveButton("YES", new DeleteOnClickListener(adapter,position))
	        	.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	        		@Override
	        		public void onClick(DialogInterface dialog, int which) {
	        		}
	        	})        	
	        	.show();	
			}
        	
        }
        
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
             
            ViewHolder holder = null;
            if (convertView == null) {
                 
                holder=new ViewHolder();  
                 
                convertView = mInflater.inflate(R.layout.activity_my_queue_list, null);
                holder.serverName = (TextView)convertView.findViewById(R.id.list_server_name);
                holder.queuenum = (TextView)convertView.findViewById(R.id.list_queue_num);
                holder.password =(TextView)convertView.findViewById(R.id.list_password); 
                holder.deleteBtn = (Button)convertView.findViewById(R.id.list_delete_btn);
                convertView.setTag(holder);
                 
            }else {
                 
                holder = (ViewHolder)convertView.getTag();
            }
             
             
            holder.serverName.setText((String)mData.get(position).get("list_server_name"));
            holder.queuenum.setText((String)mData.get(position).get("list_queue_num"));
            holder.password.setText((String)mData.get(position).get("list_password"));
             
            holder.deleteBtn.setOnClickListener(new MyOnDeleteClickListener(this,position));
             
             
            return convertView;
        }
         
    }	
	
	public class QueueAgainOnClickListener implements DialogInterface.OnClickListener{
		private int position;
    	private MyAdapter map;
    	String serverStr;
    	String serverName;
		protected QueueAgainOnClickListener(MyAdapter ma,int p,String ss,String sn) {
			super();
			map = ma;
			position = p;
			serverStr = ss;
			serverName = sn;
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			//删除原有过期的项目
			MyQueueManager.myqueue.remove(position);
			MyQueueManager.WriteMyQueue();
			mData = getData();
			map.notifyDataSetChanged();
			
			//重新排队
			int serverPort=22222;
			String serverIpPort = serverStr;
			String[] splitServerAddr=serverStr.split(":");
			if (splitServerAddr.length>1) {
				serverStr=splitServerAddr[0];
				serverPort=Integer.parseInt(splitServerAddr[1]);
			}
			String[] replys = null;
			Socket s = new Socket();
			try {
				InetAddress serverAddr = InetAddress.getByName(serverStr);
				s.connect(new InetSocketAddress(serverAddr,serverPort),5000);
				OutputStream out = s.getOutputStream();
				PrintWriter output = new PrintWriter(out,true);
				BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
				output.println("join_queue");
				String reply=input.readLine();
				if (reply!=null) {
					replys=reply.split(" ");
					if (replys.length==2) {
						Toast.makeText(MyQueueListActivity.this, "Queue Successfully!", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(MyQueueListActivity.this, "Queue Failed", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(MyQueueListActivity.this, "Queue Failed", Toast.LENGTH_LONG).show();
				}
				s.close();
			} catch (UnknownHostException e1) {
				Toast.makeText(MyQueueListActivity.this, "Unknown host", Toast.LENGTH_LONG).show();
			} catch (ConnectException e2) { 
				Toast.makeText(MyQueueListActivity.this, "Connection Refused", Toast.LENGTH_LONG).show();
			} catch (SocketTimeoutException e3) {
				Toast.makeText(MyQueueListActivity.this, "Cant not connect to the server", Toast.LENGTH_LONG).show();
			} catch (IOException e4) {
				Toast.makeText(MyQueueListActivity.this, e4.toString(), Toast.LENGTH_LONG).show();
			}
			
			//插入并显示
			MyQueueManager.myqueue.add(new QueueElement(serverName,serverIpPort,Integer.parseInt(replys[0]),replys[1],0));
			MyQueueManager.WriteMyQueue();
			mData = getData();
			map.notifyDataSetChanged();
		}
		
	}
	
	public class DeleteOnClickListener implements DialogInterface.OnClickListener{
		private int position;
    	private MyAdapter map;
		protected DeleteOnClickListener(MyAdapter ma,int p) {
			super();
			map = ma;
			position = p;
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			MyQueueManager.myqueue.remove(position);
			MyQueueManager.WriteMyQueue();
			mData = getData();
			map.notifyDataSetChanged();
		}
	}

}
