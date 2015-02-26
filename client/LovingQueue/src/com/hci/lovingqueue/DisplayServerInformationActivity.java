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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hci.lovingqueue.R;

public class DisplayServerInformationActivity extends Activity {

	private TextView serverAddress;
	private TextView serverName;
	private String scanResult;
	private String[] splitResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_server_information);
		scanResult=getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE_SERVER_NAME);
		splitResult=scanResult.split("\\|");
		serverAddress = (TextView) this.findViewById(R.id.dsia_server_address);
		serverName = (TextView) this.findViewById(R.id.dsia_server_name);
		serverAddress.setText(splitResult[0]);
		if (splitResult.length>1) {
			serverName.setText(splitResult[1]);
		}
		
	}

	public static final String QUEUE_ELEMENT="QueueElement";
	public void requestQueue(View view) {
		Socket s = new Socket();
		try {
			String serverStr = splitResult[0];
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
			output.println("join_queue");
			String reply=input.readLine();
			if (reply!=null) {
				String[] replys=reply.split(" ");
				if (replys.length==2) {
					Toast.makeText(this, "Queue Successfully!", Toast.LENGTH_LONG).show();
					Intent mIntent=this.getIntent();
					mIntent.putExtra(QUEUE_ELEMENT, reply);
					this.setResult(RESULT_OK, mIntent);
					this.finish();
				} else {
					Toast.makeText(this, "Please don't repeat queuing", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "Queue Failed", Toast.LENGTH_LONG).show();
			}
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

}
