package com.example.mocu.mapprj;

import java.math.BigDecimal;

/**
 * Created by mocu on 2016/1/15.
 */
public class JudgmentMap {

    private final int DEFAULT_DIV_SCALE = 10;

    //lat 緯度
    //lng 經度
    //r 半徑
    public Boolean check(Company company, Double lat, Double lng, Integer r) {
        double earthR = 6371;
        double distance;
        double dLat = Double.valueOf(new BigDecimal(String.valueOf((company.getLat() - lat)))
                .multiply(new BigDecimal(String.valueOf(Math.PI)))
                .divide(new BigDecimal(String.valueOf(180)), DEFAULT_DIV_SCALE,
                        BigDecimal.ROUND_HALF_EVEN).toString());
        double dLng = Double.valueOf(new BigDecimal(String.valueOf((company.getLng() - lng)))
                .multiply(new BigDecimal(String.valueOf(Math.PI)))
                .divide(new BigDecimal(String.valueOf(180)), DEFAULT_DIV_SCALE,
                        BigDecimal.ROUND_HALF_EVEN).toString());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(company.getLat() * Math.PI / 180) *
                Math.cos(lat * Math.PI / 180) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        distance = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))) * earthR * 1000;

        if (distance > Double.valueOf(String.valueOf(r))) {
            return false;
        }
        return true;
    }


    //--------------------------------------------------------
    //修改建構參數
    public class SearchMapServiceForPurpose {
        //lat 緯度
        //lng 經度
        //r 半徑
        public Boolean check(Company company, Double lat, Double lng, Integer r) {
            double earthR = 6371;
            double distance;
            double dLat = Double.valueOf(new BigDecimal(String.valueOf((company.getLat() - lat)))
                    .multiply(new BigDecimal(String.valueOf(Math.PI)))
                    .divide(new BigDecimal(String.valueOf(180)), DEFAULT_DIV_SCALE,
                            BigDecimal.ROUND_HALF_EVEN).toString());
            double dLng = Double.valueOf(new BigDecimal(String.valueOf((company.getLng() - lng)))
                    .multiply(new BigDecimal(String.valueOf(Math.PI)))
                    .divide(new BigDecimal(String.valueOf(180)), DEFAULT_DIV_SCALE,
                            BigDecimal.ROUND_HALF_EVEN).toString());
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(company.getLat() * Math.PI / 180) *
                    Math.cos(lat * Math.PI / 180) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            distance = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))) * earthR * 1000;

            if (distance > Double.valueOf(String.valueOf(r))) {
                return false;
            }
            return true;
        }
    }


    public class Company {
        public int id;
        public double lat, lng;

        public Company(int id, double lat, double lng) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
}
