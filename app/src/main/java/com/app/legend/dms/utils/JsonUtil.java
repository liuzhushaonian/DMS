package com.app.legend.dms.utils;

import com.app.legend.dms.model.HideComic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    public static List<HideComic> getList(String json){

        List<HideComic> hideComicList=new ArrayList<>();

        try {
            JSONObject object=new JSONObject(json);

            JSONArray jsonArray=object.getJSONArray("list");

            for (int i=0;i<jsonArray.length();i++){

                JSONObject o=jsonArray.getJSONObject(i);

                HideComic comic=new HideComic();

                if (o.has("author")) {
                    comic.setAuthor(o.getString("author"));
                }

                if (o.has("title")) {
                    comic.setTitle(o.getString("title"));
                }

                if (o.has("dmId")) {
                    comic.setId(o.getString("dmId"));
                }
                if (o.has("bookLink")) {
                    comic.setBookLink(o.getString("bookLink"));
                }

                hideComicList.add(comic);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hideComicList;

    }

    public static List<String> getIdList(String json){

        List<String> result=new ArrayList<>();

        try {
            JSONObject jsonObject=new JSONObject(json);

            JSONArray array=jsonObject.getJSONArray("list");

            for (int i=0;i<array.length();i++){

                JSONObject object=array.getJSONObject(i);

                if (object.has("dmId")){

                    result.add(object.getString("dmId"));

                }


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

}
