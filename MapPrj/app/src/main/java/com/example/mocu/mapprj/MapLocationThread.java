package com.example.mocu.mapprj;

import java.util.ArrayList;

/**
 * Created by mocu on 2016/1/16.
 */
public class MapLocationThread extends Thread {

    private String urlData;
    ArrayList<String> arrayLat;
    ArrayList<String> arrayLng;
    ArrayList<String> arrayRad;
    ArrayList<String> arrayPosition;


    @Override
    public void run() {
        arrayLat = new ArrayList<>();
        arrayLng = new ArrayList<>();
        arrayRad = new ArrayList<>();
        arrayPosition = new ArrayList<>();

        GetURLClass getURLClass = new GetURLClass();
        ParseUrlClass parseUrlClass = new ParseUrlClass();

        try {
            urlData = getURLClass.getUrlData();
            parseUrlClass.parserData(urlData, arrayLat, arrayLng, arrayPosition, arrayRad);
        } catch (Exception e) {
            System.out.println("MapLocationThread" + e);
        }
    }
}
