package com.hci.lovingqueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hci.lovingqueue.R;
import com.zxing.activity.CaptureActivity;

public class MainActivity extends Activity {
	private static final int REQUEST_CODE_CAMERA=1;
	private static final int REQUEST_CODE_QUEUE=2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyQueueManager.path=this.getFilesDir();
        MyQueueManager.ReadMyQueue();
        setContentView(R.layout.main);
        //启动推送服务
        Intent intent = new Intent();
        intent.setAction("com.hci.lovingqueue.action.MY_SERVICE");
	    startService(intent);
	    
        Button scanBarCodeButton = (Button) this.findViewById(R.id.btn_scan_barcode);
        scanBarCodeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//打开扫描界面扫描条形码或二维码
				Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
				startActivityForResult(openCameraIntent, REQUEST_CODE_CAMERA);
			}
		});
    }
    
    
    public void check_myqueue(View view) {
    	MyQueueManager.ReadMyQueue();
    	Intent intent=new Intent(MainActivity.this,MyQueueListActivity.class);
    	startActivity(intent);
    }
    
    public static final String EXTRA_MESSAGE_SERVER_NAME="ServerInformation";
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//处理扫描结果（在界面上显示）
		if (resultCode == RESULT_OK) {
			if (requestCode==REQUEST_CODE_CAMERA) {
				Bundle bundle = data.getExtras();
				String scanResult = bundle.getString("result");
				Intent serverIntent=new Intent(this,DisplayServerInformationActivity.class);
				serverIntent.putExtra(EXTRA_MESSAGE_SERVER_NAME, scanResult);
				startActivityForResult(serverIntent,REQUEST_CODE_QUEUE);
			}
			if (requestCode==REQUEST_CODE_QUEUE) {
				Bundle bundle = data.getExtras();
				String reply = bundle.getString(DisplayServerInformationActivity.QUEUE_ELEMENT);
				String[] replys=reply.split(" ");
				String[] serverInformations=bundle.getString(EXTRA_MESSAGE_SERVER_NAME).split("\\|");
				if (replys.length!=2 || serverInformations.length!=2) {
					return;
				}
				MyQueueManager.myqueue.add(new QueueElement(serverInformations[1],serverInformations[0],Integer.parseInt(replys[0]),replys[1],0));
				MyQueueManager.WriteMyQueue();
			}
		}
	}
}