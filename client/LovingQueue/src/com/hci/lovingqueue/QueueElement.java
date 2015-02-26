package com.hci.lovingqueue;

public class QueueElement {
	public String serverName;
	public String serverAddress;
	public int queuenum;
	public String password;
	public int reminder; //0为未提醒过，1为提醒过
	public QueueElement(String sn,String sa,int qn,String pw,int rd) {
		serverName=sn;
		serverAddress=sa;
		queuenum=qn;
		password=pw;
		reminder = rd;
	}
	public String toString() {
		return String.format("%s|%s|%d|%s|%d", serverName,serverAddress,queuenum,password,reminder);
	}
	
	public static QueueElement parseString(String s) {
		if (s==null)
			return null;
		String[] tmp=s.split("\\|");
		if (tmp.length!=5)
			return null;
		return new QueueElement(tmp[0],tmp[1],Integer.parseInt(tmp[2]),tmp[3],Integer.parseInt(tmp[4]));
	}
}
