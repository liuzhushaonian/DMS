package com.app.legend.dms.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class Char implements Parcelable {

    private int id;
    private String charId;
    private String dmId;
    private String title;
    private String tComicId;
    private int chapterOrder;
    private String charInfoTitle;

    public Char() {
    }

    protected Char(Parcel in) {
        id = in.readInt();
        charId = in.readString();
        dmId = in.readString();
        title = in.readString();
        tComicId = in.readString();
        chapterOrder = in.readInt();
        charInfoTitle = in.readString();
    }

    public static final Creator<Char> CREATOR = new Creator<Char>() {
        @Override
        public Char createFromParcel(Parcel in) {
            return new Char(in);
        }

        @Override
        public Char[] newArray(int size) {
            return new Char[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(charId);
        dest.writeString(dmId);
        dest.writeString(title);
        dest.writeString(tComicId);
        dest.writeInt(chapterOrder);
        dest.writeString(charInfoTitle);
    }
}
