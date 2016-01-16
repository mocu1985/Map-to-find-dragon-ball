package com.example.mocu.mapprj;

import java.util.ArrayList;

/**
 * Created by mocu on 2016/1/16.
 */
public class ParseUrlClass extends Thread {
    String tempLat, tempLng, tempPosition, tempRad;
    int startLat, endLat = 0;
    int startLng, endLng = 0;
    int startPosition, endPosition = 0;
    int startRad, endRad = 0;

    //解析緯度
    public void parserData(String urlData, ArrayList arrayLat, ArrayList arrayLng, ArrayList arrayPosition, ArrayList arrayRad) {
        try {
            do {
                startLat = urlData.indexOf("Lat:", endLat + 1);
                startLng = urlData.indexOf("Lng:", endLng + 1);
                startPosition = urlData.indexOf(">p:", endPosition + 1);
                startRad = urlData.indexOf("Rad:", endRad + 1);

                endLat = urlData.indexOf("</td><td", startLat + 1);
                endLng = urlData.indexOf("</div></td><td", startLng + 1);
                endPosition = urlData.indexOf("</td><td", startPosition + 1);
                endRad = urlData.indexOf("</td></tr><tr", startRad + 1);

                tempLat = urlData.substring(startLat + 4, endLat);
                tempLng = urlData.substring(startLng + 4, endLng);
                tempPosition = urlData.substring(startPosition + 3, endPosition);
                tempRad = urlData.substring(startRad + 4, endRad);

                if (startLat == -1 && startLng == -1 && startPosition == -1 && startRad == -1) {
                    break;
                }
                arrayLat.add(tempLat);
                arrayLng.add(tempLng);
                arrayPosition.add(tempPosition);
                arrayRad.add(tempRad);
            } while (true);
        } catch (Exception e) {
            System.out.println("ParserLat:" + e);
        }
    }

}
