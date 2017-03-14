package com.example.admin.myapplication.scan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.admin.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ScanListAdapter extends BaseAdapter {
	private Context context;
	private List<String> data = new ArrayList<String>();

	public ScanListAdapter(Context context) {
		this.context = context;
	}

	public void addStr(String str) {
		if (this.data != null)
			this.data.add(str);

	}

	public void cleanData() {
		if (this.data != null) {
			this.data.clear();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data != null ? data.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data != null ? data.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		String str = data.get(position);
		ViewHolder vh = null;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = (LinearLayout) LayoutInflater.from(context).inflate(
					R.layout.scan_lv_item, null);
			vh.tv = (TextView) convertView.findViewById(R.id.scan_lv_item_tv);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.tv.setText(context.getString(R.string.position) + ":" + position
				+ "  " + str);

		return convertView;
	}

	private class ViewHolder {
		public TextView tv;
	}

}
