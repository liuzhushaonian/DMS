package com.app.legend.dms.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class CharInfo implements Parcelable {

    private int id;//
    private String title;//名字
    private String dmId;//所属哪本漫画

    public CharInfo() {
    }

    protected CharInfo(Parcel in) {
        id = in.readInt();
        title = in.readString();
        dmId = in.readString();
    }

    public static final Creator<CharInfo> CREATOR = new Creator<CharInfo>() {
        @Override
        public CharInfo createFromParcel(Parcel in) {
            return new CharInfo(in);
        }

        @Override
        public CharInfo[] newArray(int size) {
            return new CharInfo[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(dmId);
    }
}
