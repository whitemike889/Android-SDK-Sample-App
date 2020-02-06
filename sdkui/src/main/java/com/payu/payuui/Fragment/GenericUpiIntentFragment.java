package com.payu.payuui.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Adapter.PackageListAdapter;
import com.payu.payuui.IntentCallback;
import com.payu.payuui.PackageBean;
import com.payu.payuui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the

 * to handle interaction events.
 * Use the {@link GenericUpiIntentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenericUpiIntentFragment extends Fragment {


    private RecyclerView recyclerView;
    private Context context;
    List<PackageBean> appList;
    String UPI_INTENT_PREFIX = "upi://pay?";
    PostData postData;
    PayuConfig payuConfig;
    public GenericUpiIntentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenericUpiIntentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenericUpiIntentFragment newInstance(String param1, String param2) {
        GenericUpiIntentFragment fragment = new GenericUpiIntentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_generic_upi_intent, container, false);
        recyclerView = view.findViewById(R.id.rvUpiApps);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUpiAppsData();
        if(!appList.isEmpty()){
            recyclerView.setLayoutManager(new GridLayoutManager(context,3));
            recyclerView.setAdapter(new PackageListAdapter(appList,context,(IntentCallback)getActivity()));
        }
    }

    private void initUpiAppsData(){
       appList = new ArrayList<>();
        Intent intent = new Intent();
        intent.setData(Uri.parse(UPI_INTENT_PREFIX));
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            PackageInfo packageInfo = null;
            try {
                Log.v("UPI"," Installed App....... "+resolveInfo.activityInfo.packageName);
                packageInfo = context.getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, 0);
                String name = (String) context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo);
                appList.add(new PackageBean(name, packageInfo.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            getActivity().setResult(resultCode, data);
            getActivity().finish();
        }
    }
}





    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

