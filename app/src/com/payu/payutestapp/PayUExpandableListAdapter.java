package com.payu.payutestapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.payu.sdk.PayU;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by franklin on 23/4/15.
 */
public class PayUExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private JSONArray mGroupList;
    private HashMap<String, List<String>> mChildList;

    public PayUExpandableListAdapter(Context context, JSONArray groupList){
        this.mContext = context;
        this.mGroupList = groupList;
    }


    @Override
    public int getGroupCount() {
        return mGroupList.length();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        try {
            return mGroupList.get(groupPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        try {
            return mChildList.get(mGroupList.get(groupPosition)).get(childPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String groupTitleText = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_list_group_items, null);
        }
        ((TextView) convertView.findViewById(R.id.groupItemTitle)).setText(groupTitleText);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String groupTitle = getGroup(groupPosition).toString();
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            try {
                if (groupTitle.contentEquals(mGroupList.get(0).toString())) {// stored card
                    convertView = layoutInflater.inflate(R.layout.expandable_list_stored_card, null);
                    if (PayU.storedCards != null) { // we have some stored cards!.

                    }
                } else if (groupTitle.contentEquals(mGroupList.get(1).toString())) {// credit card
                    convertView = layoutInflater.inflate(R.layout.expandable_list_card, null);
                } else if (groupTitle.contentEquals(mGroupList.get(2).toString())) {// net banking
                    convertView = layoutInflater.inflate(R.layout.expandable_list_netbanking, null);
                } else if (groupTitle.contentEquals(mGroupList.get(4).toString())) {// cash card
                    convertView = layoutInflater.inflate(R.layout.expandable_list_cash_card, null);
                } else if (groupTitle.contentEquals(mGroupList.get(5).toString())) {// Emi
                    convertView = layoutInflater.inflate(R.layout.expandable_list_emi, null);
                } else if (groupTitle.contentEquals(mGroupList.get(6).toString())) { // PayU money
                    // opps no view pay money.
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_group_items, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.groupItemTitle);
        lblListHeader.setText(groupTitle);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
