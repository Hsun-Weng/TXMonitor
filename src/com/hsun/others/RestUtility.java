package com.hsun.others;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestUtility {
	String apiUrl;
	URI uri = null;
	HttpClient httpClient;
	HttpGet get;
	HttpResponse response;
	URIBuilder uriBuilder = new URIBuilder();
	
	JSONArray responseArray = null;
	
	List<JSONObject> resList;
	
	private static RestUtility instance = null;
	
	
	
	private RestUtility(){
		
	}
	
	public static RestUtility getInstance(){
		if(instance == null){
			synchronized(RestUtility.class){
				if(instance == null){
					instance = new RestUtility();
				}
			}
		}
		return instance;
	}
	
	public int getTxPrice(){
		int txPrice = 0;
		uriBuilder = new URIBuilder();
		httpClient = new DefaultHttpClient();
		try{
  			apiUrl = "http://www.taifex.com.tw/quotesapi/getQuotes.aspx";
	  		
	  		uriBuilder.setParameter("objId", "2");
	  		
	  		uri = uriBuilder.build();
	  		
	  		apiUrl += uri;
	  		
	  		get = new HttpGet(apiUrl);
	  		
  			response = httpClient.execute(get);
	  		responseArray = new JSONArray(EntityUtils.toString(response.getEntity()));
	  		resList = toList(responseArray);
	  		
	  		for(JSONObject jsonObject : resList){
	  			if(((String)jsonObject.get("contract")).startsWith("TX")){
	  				txPrice = Integer.parseInt(jsonObject.getString("price").replaceAll(",", ""));
	  			}
	  		}
  		}catch(Exception e){
  			e.printStackTrace();
  		}
		return txPrice;
	}
	
	/**
  	 * JSONAray 轉 List
  	 * @param array
  	 * @return
  	 * @throws JSONException
  	 */
  	public List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }
  	
  	/**
  	 * Json物件轉 Map or List
  	 * @param json
  	 * @return
  	 * @throws JSONException
  	 */
  	public Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        }else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }
	
}
