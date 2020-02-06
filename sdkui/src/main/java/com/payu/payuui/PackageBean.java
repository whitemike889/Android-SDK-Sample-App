package com.payu.payuui;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by himanshu.gupta on 01/05/18.
 */

public class PackageBean implements Parcelable {
    private String packageName;
    private String packageId;


    private PackageBean(Parcel parcel) {
        super();
        readFromParcel(parcel);
    }

    public PackageBean(String packageName, String packageId) {
        this.packageName = packageName;
        this.packageId = packageId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public static final Creator<PackageBean> CREATOR = new Creator<PackageBean>() {
        public PackageBean createFromParcel(Parcel in) {
            return new PackageBean(in);
        }

        public PackageBean[] newArray(int size) {

            return new PackageBean[size];
        }

    };

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if ((o == null) || !(o instanceof PackageBean))
            return false;
        PackageBean packageBean = (PackageBean) o;
        return this.packageId.equalsIgnoreCase(packageBean.packageId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + packageName.length();
        return hash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        packageName = in.readString();
        packageId = in.readString();

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(packageName);
        parcel.writeString(packageId);
    }
}
