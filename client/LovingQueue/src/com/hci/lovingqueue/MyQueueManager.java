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

import android.content.Intent;
import android.util.Log;
import android.view.View;

public class MyQueueManager {
	private static final String INTERNAL_STORAGE_FILE_NAME = "myqueue.txt";
	public static ArrayList<QueueElement> myqueue = new ArrayList<QueueElement>();
	public static File path;
	
    
	public static void WriteMyQueue() {
    	FileOutputStream outputStream;
    	File file = new File(path,INTERNAL_STORAGE_FILE_NAME);
    	if (!file.exists()) {
    		try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	try {
    		outputStream = new FileOutputStream(file);
    		PrintWriter pw=new PrintWriter(outputStream,true);
    		for (int i=0;i<myqueue.size();++i)
    			pw.println(myqueue.get(i).toString());
    		pw.close();
    		outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void ReadMyQueue() {
    	FileInputStream inputStream;
    	File file = new File(path,INTERNAL_STORAGE_FILE_NAME);
    	if (!file.exists()) {
    		return;
    	}
    	try {
    		inputStream = new FileInputStream(file);
    		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    		myqueue.clear();
    		String data;
    		while ((data=br.readLine())!=null) {
    			myqueue.add(QueueElement.parseString(data));
    		}
    		br.close();
    		inputStream.close();
    	} catch (FileNotFoundException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	

}
