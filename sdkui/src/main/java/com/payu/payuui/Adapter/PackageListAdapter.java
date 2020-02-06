package com.payu.payuui.Adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.payu.payuui.IntentCallback;
import com.payu.payuui.PackageBean;
import com.payu.payuui.R;

import java.util.List;

/**
 * Created by himanshu.gupta on 07/05/18.
 */

public class PackageListAdapter extends RecyclerView.Adapter<PackageListAdapter.PackageViewHolder> {

    private List<PackageBean> packageBeanList;
    private Context context;
    private IntentCallback callback;
    private int selectedPosition = -1;

    public PackageListAdapter(List<PackageBean> packageBeanList, Context context,IntentCallback callback) {
        this.packageBeanList = packageBeanList;
        this.context = context;
        this.callback = callback;
    }


    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_app_item, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PackageViewHolder holder, int position) {
        final PackageBean packageBean = packageBeanList.get(holder.getAdapterPosition());
        if(-1!=selectedPosition){
            if(position ==selectedPosition){
                holder.linearLayout.setBackgroundResource(R.color.color_enter_cvv);
            }else {
                holder.linearLayout.setBackgroundResource(android.R.color.transparent);
            }
        }else {
            holder.linearLayout.setBackgroundResource(android.R.color.transparent);
        }
        holder.packageName.setText(packageBean.getPackageName());
        holder.packageDrawable.setImageDrawable(getAppIcon(this.context,packageBean.getPackageId()));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
                callback.onAppSelected(packageBean.getPackageId());
//                callbacks.onAppSelected(packageBean.getPackageId());
            }
        });

    }


    @Override
    public int getItemCount() {
        return packageBeanList.size();
    }

    class PackageViewHolder extends RecyclerView.ViewHolder {
        ImageView packageDrawable;
        TextView packageName;
        LinearLayout linearLayout;

        public PackageViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
            packageDrawable = itemView.findViewById(com.payu.upisdk.R.id.image);
            packageName = itemView.findViewById(com.payu.upisdk.R.id.text);
        }
    }

    /**
     * Get Application's icon drawable
     * @param context Application Context
     * @param packageName Application Package Id
     * @return Drawable if Application installed else null
     */
    private Drawable getAppIcon(Context context, String packageName){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return context.getPackageManager().getApplicationIcon(packageInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
