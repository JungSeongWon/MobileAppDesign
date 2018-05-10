package com.scjeon.costom;

import android.content.Context;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CitySearchAdapter extends BaseAdapter {

    private Context mContext = null;
    private List<CityListData> cityList = new ArrayList<>();
    private ArrayList<CityListData> arrayList = new ArrayList<>();

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList. (원본 데이터 리스트)
    private ArrayList<CityListData> listViewItemList = new ArrayList<CityListData>();
    // 필터링된 결과 데이터를 저장하기 위한 ArrayList. 최초에는 전체 리스트 보유.
    private ArrayList<CityListData> filteredItemList = listViewItemList;

    String sidoName;

    public CitySearchAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return filteredItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View converView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (converView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            converView = inflater.inflate(R.layout.row_listview, viewGroup, false);

            holder.tv_location = converView.findViewById(R.id.tv_location);

            converView.setTag(holder);
        } else {
            holder = (ViewHolder) converView.getTag();
        }

        //CityListData cityListData = list.get(position);
        CityListData cityListData = filteredItemList.get(position);
        sidoName = cityListData.getSidoName();
        String sggName = cityListData.getSggName();
        String umdName = cityListData.getUmdName();

        // 리스트에 있는 데이터를 리스트뷰 셀에 뿌린다.
        holder.tv_location.setText(sidoName + " " + sggName + " " + umdName);

        return converView;
    }

    public void addItem(String sidoName, String sggName, String umdName, String tmX, String tmY) {
        // 리스트 데이터 찾기
        CityListData item = new CityListData();
        item.setSidoName(sidoName);
        item.setSggName(sggName);
        item.setUmdName(umdName);
        item.setTmX(tmX);
        item.setTmX(tmY);

        arrayList.add(item);
        listViewItemList.add(item);
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        listViewItemList.clear();
        if (charText.length() == 0) {
            listViewItemList.addAll(arrayList);
        } else {
            for (CityListData cityListData : arrayList) {
                String name = cityListData.sidoName + cityListData.sggName + cityListData.umdName;
                if (name.toLowerCase().contains(charText)) {
                    listViewItemList.add(cityListData);
                }
            }
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView tv_location;
    }

}
