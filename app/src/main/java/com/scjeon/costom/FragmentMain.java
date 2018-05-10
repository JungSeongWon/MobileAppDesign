package com.scjeon.costom;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class FragmentMain extends Fragment {

    // git Test
    Spinner sidoNameSpinner; // 시, 도명을 선택하는 스피너
    TextView tvTempText;
    Button btnSpinner;

    public ArrayList<CityListData> clistData; // 도시 어댑터의 리스트

    public ListView listView; // 검색을 보여줄 리스트 변수
    private EditText editSearch; // 검색어를 입력할 변수
    public CitySearchAdapter citySearchAdapter; // 리스트뷰에 연결할 어댑터

    ArrayAdapter mSpinnerAdapter = null; // 시, 도명을 연결하는 어댑터
    CitySearchTask citySearchTask = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // 객체와 ID 연결 부분
        btnSpinner = view.findViewById(R.id.btn_spinner); // 도시를 나타내는 스피너
        sidoNameSpinner = view.findViewById(R.id.city_spinner); // 삭제 예정

        // 리스트 뷰 부분 - EditText 생성 필요
        editSearch = view.findViewById(R.id.search_location);
        listView = view.findViewById(R.id.listView);

        citySearchTask = new CitySearchTask(); // 파라미터 수정 필요
        citySearchTask.execute();

        //리스트에 연동될 어댑터를 생성
        citySearchAdapter = new CitySearchAdapter(getActivity());

        // 리스트뷰에 어뎁터를 연결
        listView.setAdapter(citySearchAdapter);

        // 입력 창에 검색어를 입력 시 리스너 정의
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 입력 창에 문자를 입력할 때마다 호출
                String text = editSearch.getText().toString().toLowerCase(Locale.getDefault());
                citySearchAdapter.filter(text);
            }
        });


        // ArrayAdapter에 R.array.sidoname을 연결
        mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sidoname,
                R.layout.support_simple_spinner_dropdown_item);

        // Spinner 클릭시 DropDown 모양을 설정 할 수 있다.
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 스피너에 어뎁터를 연결 시켜 준다.
        sidoNameSpinner.setAdapter(mSpinnerAdapter);

        return view;
    }

    private class CitySearchTask extends AsyncTask<String, String, ArrayList<CityListData>> {

    /*  전국의 모든 읍면동을 검색한 값
    http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt?umdName=&pageNo=1&numOfRows=5300&
    ServiceKey=SKXbIW5Uk%2Bn4rUGkg1KdvU8spwr2FBTQvs3J6LF6gdaTkjmuFxJHnc36A3MOXqGo1JBUWelpR0DfEr5zLJding%3D%3D
     */

        private String serviceKey = "SKXbIW5Uk%2Bn4rUGkg1KdvU8spwr2FBTQvs3J6LF6gdaTkjmuFxJHnc36A3MOXqGo1JBUWelpR0DfEr5zLJding%3D%3D";
        private String str, receiveMsg;
        private String surl = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt?umdName=&pageNo=1&numOfRows=5300&" +
                "ServiceKey=SKXbIW5Uk%2Bn4rUGkg1KdvU8spwr2FBTQvs3J6LF6gdaTkjmuFxJHnc36A3MOXqGo1JBUWelpR0DfEr5zLJding%3D%3D";

        private String searchCity; // 사용자가 검색한 도시명
        private String sidoName;// 시도 명
        private String sggName; // 시군구 명
        private String umdName; // 읍면동 명
        private String tmX; // tmX 좌표
        private String tmY; // tmY 좌표

        public CitySearchTask() {

        }

        public CitySearchTask(String searchCity) {
            this.searchCity = searchCity;
            Log.i("URLFIND", surl);
        }

        @Override
        protected ArrayList<CityListData> doInBackground(String... strings) {

            ArrayList<CityListData> arrayList = new ArrayList<CityListData>();

            try {
                URL xml = new URL(surl);
                String tag_value; // 태그에 따른 값
                CityListData data = null;
                // 모든 데이터를 가져올 수 있음
                XmlPullParserFactory parsercreator = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parsercreator.newPullParser();
                parser.setInput(xml.openStream(), null);

                // 이벤트 발생 체크
                int parserEvent = parser.getEventType();

                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            tag_value = parser.getName();

                            if (tag_value.equals("sidoName")) {
                                sidoName = parser.nextText();
                                data = new CityListData();
                                data.setSidoName(sidoName);
                                //Log.i("sidoName", sidoName);
                            }
                            if (tag_value.equals("sggName")) {
                                sggName = parser.nextText();
                                data.setSggName(sggName);
                                //Log.i("sggName", sggName);
                            }
                            if (tag_value.equals("umdName")) {
                                umdName = parser.nextText();
                                data.setUmdName(umdName);
                                //Log.i("umdName", umdName);
                            }
                            if (tag_value.equals("tmX")) {
                                tmX = parser.nextText();
                                data.setTmX(tmX);
                                //Log.i("tmX", tmX);
                            }
                            if (tag_value.equals("tmY")) {
                                tmY = parser.nextText();
                                data.setTmY(tmY);
                                //Log.i("tmY", tmY);
                                arrayList.add(data);
                                citySearchAdapter.addItem(sidoName, sggName, umdName, tmX, tmY);
                            }
                            break;
                    }
                    parserEvent = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return arrayList;
        }

        // 파싱과정이 끝이 나면 마지막으로 실행되는 것 데이터 표현
        @Override
        protected void onPostExecute(ArrayList<CityListData> cityListData) {
            listView.setAdapter(citySearchAdapter);
            citySearchAdapter.notifyDataSetChanged();
        }

    }



}

