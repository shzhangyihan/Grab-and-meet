package com.twilio;

import static spark.Spark.post;

import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;
import java.io.*;
import java.util.*;
import java.util.LinkedList;

public class ReceiveSms {
	public static void main(String[] args) {
		LinkedList<Supply> supplyList = new LinkedList<Supply>();
		LinkedList<Demand> demandList = new LinkedList<Demand>();

		post("/receive-sms", (req, res) -> {
			String toRe = "wrong message format, should be: go place time items OR list place items";

			/*System.out.println(req.headers());
			System.out.println(req.contentType());*/
			String dest = null;
			String ltime = null;
			String items = null;
			long currTime = 0;
			long eTime = 0;

			//split request body
			String reqBody = req.body();
			String[] splitedBody = reqBody.split("&");


			//get phone number
			String phoneNum = splitedBody[17];
			phoneNum = phoneNum.substring(8);
			phoneNum = "+" + phoneNum;

			//get real message text
			String realContent = splitedBody[10];
			realContent = realContent.substring(5);
			String[] splitedContent = realContent.split("\\+");

			if(splitedContent[0].equals("go"))
			{
				dest = splitedContent[1];
				ltime = splitedContent[2];
				items = splitedContent[3];
				int duration = extractInt(ltime);
				Date date = new Date();
				currTime = date.getTime();
				eTime = currTime + duration*60000;
				Supply newSup = new Supply(dest, phoneNum, currTime, eTime, extractInt(items));
				/*System.err.println(dest);
				System.err.println(phoneNum);
				System.err.println(currTime);
				System.err.println(eTime);
				System.err.println(extractInt(items));*/
				supplyList.add(newSup);
				for(int i = 0; i < supplyList.size(); i++)
				{
					System.out.println(supplyList.size());
					Supply curr = supplyList.get(i);
					System.out.println(curr.dest + " " + curr.number + " " + curr.stime + " " + curr.etime + " " + curr.items);
				}
				toRe = "Received";
			}
			else if(splitedContent[0].equals("list"))
			{
				dest = splitedContent[1];
				items = splitedContent[2];

				LinkedList<Supply> supListInDem = filter(supplyList, dest, extractInt(items));
				System.out.println(dest + " " + extractInt(items) + " " + supListInDem.size());
				for(int i = 0; i < supListInDem.size(); i++)
				{
					System.out.println(supListInDem.size());
					Supply curr = supListInDem.get(i);
					System.out.println(curr.dest + " " + curr.number + " " + curr.stime + " " + curr.etime + " " + curr.items);
				}

				Demand newDem = new Demand(dest, phoneNum, extractInt(items), supListInDem);
				demandList.add(newDem);
				toRe = convertToString(supListInDem);
			}
			else if(splitedContent[0].equals("a") ||
					splitedContent[0].equals("b") ||
					splitedContent[0].equals("c") ||
					splitedContent[0].equals("d") ||
					splitedContent[0].equals("e") ) {
				LinkedList<Supply> su = null;
				int itms = 0;
				String ret;
				String dst = null;
				Demand d = null;
				for(Demand de: demandList) {
					if(de.number.equals(phoneNum)) {
						su = de.supListInDem;
						itms = de.items;
						dst = de.dest;
						d = de;
						break;
					}
				}
				if(su != null) {
					int index = (int)(splitedContent[0].charAt(0)) - (int)'a';
					if(index < su.size()) {
						String pN = su.get(index).number;
						ret = checkAviliable(su, pN, itms);
						if (ret != null) {
							toRe = "Congratulation! " + ret + " is willing to help you! Call for the help!";
							demandList.remove(d);
						} else {
							LinkedList<Supply> supListInDem = filter(supplyList, dst, itms);
							d.supListInDem = supListInDem;
							toRe = "Sorry the last option is no longer available.\n" + convertToString(supListInDem);
						}
					}
					else
						toRe = "Selection out of bound!";
				}
			}
			else
			{
				toRe = "Wrong message format, should be: go place time items OR list place items";
			}

			Message sms = new Message.Builder()
					.body(new Body(toRe))
					.build();

			//System.out.println(res.raw());
			MessagingResponse twiml = new MessagingResponse.Builder()
					.message(sms)
					.build();
			
			return twiml.toXml();
		});
	}

	public static LinkedList<Supply> filter(LinkedList<Supply> list, String dest, int items)
	{
		LinkedList<Supply> result = new LinkedList<Supply>();
		Date date = new Date();
		Iterator<Supply> iter = list.iterator();

		while(iter.hasNext()){
			Supply temp = iter.next();
			if(temp.etime < date.getTime()){
				iter.remove();
				continue;
			}

			if(temp.dest.equals(dest) && temp.items>=items){
				result.add(temp);
			}
		}
		Collections.sort(result, new Comparator<Supply>(){
			@Override
			public int compare(Supply s1, Supply s2){
				return (int) (s1.etime - s2.etime);
			}
		});

		while(result.size()>5){
			result.removeLast();
		}

		return result;
	}

	public static int extractInt(String in)
	{
		Scanner scan = new Scanner(in).useDelimiter("[^0-9]+");
		return scan.nextInt();
	}

	public static String convertToString(LinkedList<Supply> supply)
	{
		String s1 =  "There are "+supply.size()+" people going to "+
				supply.get(0).dest+".\n"+
				"Reply to choose one of them to help you! "+'\n';
		for (int i=0;i<supply.size();i++){
			Supply sp = supply.get(i);
			if (sp.items==1){
				s1 += "("+((char)(97+i))+")" + " Going in "+ (sp.etime-sp.stime)/60000 +
						" mins, able to take "+sp.items+" item.\n";
			}
			else{
				s1 += "("+((char)(97+i))+")" + "Going in "+(sp.etime-sp.stime)/60000+
						"mins, able to take "+sp.items+" items.\n";
			}

		}
		return s1;
	}

	public static String checkAviliable(LinkedList<Supply> supplylist, String Number, int items) {
		Date date = new Date();
		String ret = null;
		long curr = date.getTime();

		ListIterator<Supply> it = supplylist.listIterator();
		while(it.hasNext()) {
			System.out.println("11");
			Supply su = it.next();
			if(curr > su.etime) {
				it.remove();
				continue;
			}

			if(su.number.equals(Number)) {
				System.out.println("22");
				if(su.items > items) {
					ret = su.number;
					su.items = su.items - items;
				}
				else if(su.items == items) {
					ret = su.number;
					it.remove();
				}
				else if(su.items < items) {
					ret = null;
				}
			}
		}

		return ret;
	}

}
