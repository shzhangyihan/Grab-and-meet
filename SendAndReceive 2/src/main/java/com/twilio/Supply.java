package com.twilio;

public class Supply {
	public String dest;
	public String number;
	public long stime;
	public long etime;
	public int items;
	
	Supply(String d, String c, long s,long e, int i) {
		dest = d;
		number = c;
		stime = s;
		etime = e;
		items = i;
	}
}

