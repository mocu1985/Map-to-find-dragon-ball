package com.example.mocu.mapprj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mocu on 2016/1/16.
 */
public class GetURLClass {

    private static final String webUrl = "https://docs.google.com/spreadsheets/d/1BvNKFE0Es64mTFuhqita7FrnT72ZlR3qyetPMzrdIRY/pubhtml?gid=0&single=true";
    int itemIndex = 0;

    public String getUrlData() {
        String urlData = null;
        String deCodedString;
        BufferedReader in = null;

        try {
            HttpURLConnection hc;
            URL url = new URL(webUrl);
            hc = (HttpURLConnection) url.openConnection();
            hc.setDoInput(true);
            hc.connect();
            in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            while ((deCodedString = in.readLine()) != null) {
                urlData += deCodedString;
            }
        } catch (Exception e) {
            System.out.println("ERROE:" + e);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                System.out.println("in.close:" + e);
            }
        }
        return urlData;
    }
}
