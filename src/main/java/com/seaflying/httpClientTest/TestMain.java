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

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
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
       HttpClient httpClient = HttpClients.createDefault();
       HttpGet httpGet = new HttpGet(url);
       JSONObject re = null;
//       RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(5000).build();
//       httpGet.setConfig(requestConfig);
       HttpResponse response = httpClient.execute(httpGet);
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
           HttpEntity entity = response.getEntity();
           String result = EntityUtils.toString(entity);
           re =  new JSONObject(result);
       }
       return re;
   }

    public static JSONObject doPost(String url, JSONObject params) throws Exception {
            try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                List<NameValuePair> form = new ArrayList<>();
                Iterator<String> it = params.keys();
                while (it.hasNext()){
                    String key = it.next();
                    String vaule = params.getString(key);
                    form.add(new BasicNameValuePair(key,vaule));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
                httpPost.setEntity(entity);
                System.out.println("Executing request " + httpPost.getRequestLine());
                // Create a custom response handler
                ResponseHandler<String> responseHandler = response -> {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity responseEntity = response.getEntity();
                        return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                };
                String responseBody = httpClient.execute(httpPost, responseHandler);
                return new JSONObject(responseBody);
            }
    }

}
