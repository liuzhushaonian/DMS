package com.app.legend.dms.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class ExportComic implements Parcelable {

    private String charTitle;//章节名
    private String charId;//章节id
    private String path;//保存路径
    private String bigTitle;//大章节名称

    public ExportComic() {
    }


    protected ExportComic(Parcel in) {
        charTitle = in.readString();
        charId = in.readString();
        path = in.readString();
        bigTitle = in.readString();
    }

    public static final Creator<ExportComic> CREATOR = new Creator<ExportComic>() {
        @Override
        public ExportComic createFromParcel(Parcel in) {
            return new ExportComic(in);
        }

        @Override
        public ExportComic[] newArray(int size) {
            return new ExportComic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(charTitle);
        dest.writeString(charId);
        dest.writeString(path);
        dest.writeString(bigTitle);
    }
}
