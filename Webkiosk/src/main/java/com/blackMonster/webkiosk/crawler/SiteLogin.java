package com.blackMonster.webkiosk.crawler;

import android.content.Context;

import com.blackMonster.webkiosk.utils.M;
import com.blackMonster.webkiosk.utils.NetworkUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages logging in to Webkiosk website.
 */
class SiteLogin {


	private static final String TAG = "SiteLogin";

	private HttpClient httpclient=null;


	HttpClient getConnection() {
		return httpclient;
	}

	int login(String colg,String enroll, String pass, Context context)  {

		if (!NetworkUtils.isInternetAvailable(context)) return LoginStatus.CONN_ERROR;

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		WebkioskWebsite.initiliseLoginDetails(formparams, colg, enroll, pass);
		
		httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(WebkioskWebsite.getLoginUrl(colg));
		BufferedReader reader=null;
		Integer status = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
					"UTF-8");
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			status = responseStatus(reader);

		} catch (IOException e) {
			status = LoginStatus.UNKNOWN_ERROR;
			httppost.abort();
			e.printStackTrace();
		} 
		
		if (status != LoginStatus.LOGIN_DONE) {
			httpclient.getConnectionManager().shutdown();
		}
		
		if (reader!= null)
			try {
				reader.close();
			} catch (IOException e) {
				status = LoginStatus.UNKNOWN_ERROR;
				e.printStackTrace();
			}
		
		return status;
		
	}
	
	private int responseStatus(BufferedReader reader) throws IOException {
		String tmp;
		
		while (true) {
			tmp = reader.readLine();
			if (tmp == null)
				return LoginStatus.UNKNOWN_ERROR;
			M.log(TAG, tmp);
			if (tmp.contains("PersonalFiles/ShowAlertMessageSTUD.jsp") || tmp.contains("StudentPageFinal.jsp"))
				return LoginStatus.LOGIN_DONE;
			if (tmp.contains("Invalid Password"))
				return LoginStatus.INVALID_PASS;
			if (tmp.contains("Login Account Locked"))
				return LoginStatus.ACCOUNT_LOCKED;
			if (tmp.contains("Wrong Member Type or Code") || tmp.toLowerCase().contains("correct institute name and enrollment"))
				return LoginStatus.INVALID_ENROLL;
				
		}
	}


	void close() {
		if (httpclient !=null)
		httpclient.getConnectionManager().shutdown();

	}


}
