package com.stylingandroid.ble;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonData {
    public double m_temp;
    public double m_humi;
    public double m_light;
    public Location m_location;

    private JSONObject data;

    public JsonData(double temp, double humi, double light, Location location){
        m_temp = temp;
        m_humi = humi;
        m_light = light;
        m_location = location;

        data = new JSONObject();
        try{
            data.put("temperature", m_temp);
            data.put("humidity", m_humi);
            data.put("light", m_light);
            data.put("latitude", m_location.getLatitude());
            data.put("longitude", m_location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public  JSONObject getData(){
        return this.data;
    }

    public String post(String url){
        String result = "";
        InputStream inputStream = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = this.data.toString();
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    public void pushToSever(){
        //new HttpAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
        new HttpAsyncTask().execute("https://weathermap-api.herokuapp.com/v2/weather");
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return post(urls[0]);
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }
}
