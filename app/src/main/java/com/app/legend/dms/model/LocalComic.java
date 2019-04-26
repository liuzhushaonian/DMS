package com.app.legend.dms.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import lombok.Data;

@Data
public class LocalComic implements Parcelable {

    private String id;//id
    private String title;//名称
    private List<ExportComic> exportComicList;//章节列表

    public LocalComic() {
    }

    protected LocalComic(Parcel in) {
        id = in.readString();
        title = in.readString();
        exportComicList = in.createTypedArrayList(ExportComic.CREATOR);
    }

    public static final Creator<LocalComic> CREATOR = new Creator<LocalComic>() {
        @Override
        public LocalComic createFromParcel(Parcel in) {
            return new LocalComic(in);
        }

        @Override
        public LocalComic[] newArray(int size) {
            return new LocalComic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeTypedList(exportComicList);
    }
}
