package com.seaflying.httpClientTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class TestMain {
   private final static String tokenUrl = "http://10.1.62.141:8080/apps-playground/api/tokens";
   private final static String listUrl= "http://10.1.62.141:8080/apps-playground/api/session/data/mysql/connectionGroups/ROOT/tree?token=";
   private final static String conUrl = "http://10.1.62.141:8080/apps-playground/#/client/";

   public static void main(String arg[]) throws Exception{
       int appID = 0;
       JSONObject params = new JSONObject();
       params.put("username","song");
       params.put("password","admin123");
       JSONObject re = doPost(tokenUrl, params);
       System.out.println("----------------------------------------");
       System.out.println(re);
       String token = re.getString("authToken");
       JSONObject re2 = doGet(listUrl+token);
       JSONArray conList = re2.getJSONArray("childConnections");
       for(int i = 0 ; i < conList.length(); i++){
          JSONObject t = conList.getJSONObject(i);
          if( t.getString("name").indexOf("WPS表格") > -1 ){
              appID = t.getInt("identifier");
              break;
          }
       }
       String origin = appID + "\000" + "c" + "\000" + "mysql" ;
       String conId = Base64.getEncoder().encodeToString(origin.getBytes("utf-8"));
       System.out.println(conUrl+conId);
   }

   public static JSONObject doGet(String url) throws Exception {
       HttpClient httpClient = new HttpClient();
       GetMethod getMethod = new GetMethod(url);
       getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
       int status = httpClient.executeMethod(getMethod);
       if (status < 200 || status >=300 ) {
       	throw new HttpException("Unexpected response status: " + status);
       }
       String responseBody  = getMethod.getResponseBodyAsString();
       getMethod.releaseConnection();
       return new JSONObject(responseBody);     
   }

    public static JSONObject doPost(String url, JSONObject params) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod();
        postMethod.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
        NameValuePair[] form = {};      
        Iterator<String> it = params.keys();
        int i = 0;
        while (it.hasNext()){
            String key = it.next();
            String vaule = params.getString(key);
            form[i] = new NameValuePair(key,vaule);
            i++;
        }
        postMethod.setRequestBody(form);
        int status = httpClient.executeMethod(postMethod);
        if (status < 200 || status >=300 ) {
        	throw new HttpException("Unexpected response status: " + status);
        }
        String responseBody  = postMethod.getResponseBodyAsString();
        postMethod.releaseConnection();
        return new JSONObject(responseBody);          
    }

}
