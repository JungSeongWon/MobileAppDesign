package com.scjeon.costom;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 카카오 좌표->TM값 변환 api를 http통신 및 Xml로 받아 XMLPullParser를 통해 주소 및 TM값을 가져오는 클래스
 * Created by USER on 2018-05-05.
 */


public class RequestHttpURLConnection {

    private String appkey="KakaoAK "+"420d614ecd66bf3b982ce99cc82bdcfa";
    private String value="";
    String addrName, x, y;


    public String request(String TMUrl,String lt, String lg){

        String param = "x="+lt+"&y="+lg+"&input_coord=WGS84&output_coord=TM";
       // String param = "x=129.078149&y=35.205663&input_coord=WGS84&output_coord=TM";
        HttpURLConnection urlConn = null;
        StringBuffer sb = new StringBuffer();
        // http 통신
        try {
            TMUrl += "?"+param;
            URL url = new URL(TMUrl);
            urlConn = (HttpURLConnection)url.openConnection();

            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("Authorization",appkey);

            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return "통신 실패";

            InputStream inputStream = urlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");

            //XML풀파서(xml파일 파싱)
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setInput(inputStreamReader);

            int parserEvent = parser.getEventType();
            String tag_value; // 태그에 따른 값

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        tag_value = parser.getName();

                        //현재위치 주소
                        if (tag_value.equals("address_name")) {
                            addrName = parser.nextText();
                            value = value + addrName +", ";
                        }
                        // equalsIgnoreCase는 대소문자 구분 X
                        //TM x좌표, y좌표
                        if (tag_value.equalsIgnoreCase("x")) {
                            x = parser.nextText();
                            value = value + x +", ";
                        }
                        if (tag_value.equalsIgnoreCase("y")) {
                            y = parser.nextText();
                            value = value + y +", /n";
                        }
                        break;
                }
                parserEvent = parser.next();
            }
            return value;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
