package com.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SendSms {
	public static final String ACCOUNT_SID = "AC9025b006995c11716bd34b526df2257f";
	public static final String AUTH_TOKEN = "1fd2ed1cd69ab7057c8e76794cc56631";

	public static void main(String[] args) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		
		Message message = Message.creator(
				new PhoneNumber("+18587668473"), 
				new PhoneNumber("+14159854259"), 
				"Hello world"
		).create();
		
		System.out.println(message.getSid());
	}

}
