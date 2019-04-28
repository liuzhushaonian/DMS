package com.app.legend.dms.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.dms.R;
import com.app.legend.dms.impl.HideItemOnClickListener;
import com.app.legend.dms.model.HideComic;
import com.app.legend.dms.utils.HideItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import java.util.ArrayList;
import java.util.List;

public class HideAdapter extends BaseAdapter<HideAdapter.ViewHolder> {


    List<HideComic> hideComicList;
    private static final String DEFAULT_IMAGE="https://ws1.sinaimg.cn/large/c13993a9ly1g1imgjjxxlj208n0b6t9g.jpg";

    private HideItemOnClickListener listener;

    public void setListener(HideItemOnClickListener listener) {
        this.listener = listener;
    }

    public void setHideComicList(List<HideComic> hideComicList) {
        this.hideComicList = hideComicList;
        this.showList=new ArrayList<>();

        showList.addAll(hideComicList);

        notifyDataSetChanged();
    }

    private List<HideComic> showList;


    public void setShowList(List<HideComic> showList) {
        this.showList = new ArrayList<>();

        this.showList.addAll(showList);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        HideItem view=new HideItem(viewGroup.getContext());

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {

            /*点击事件，交给外部处理*/
            if (this.listener!=null&&showList!=null){

                int position=viewHolder.getAdapterPosition();
                HideComic comic=showList.get(position);
                listener.itemClick(comic);//

            }

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        if (this.showList==null){
            return;
        }

        HideComic comic=this.showList.get(i);
        viewHolder.title.setText(comic.getTitle());
        viewHolder.author.setText("作者："+comic.getAuthor());
//        viewHolder.del.setText("已删除");

        int delete=comic.getDelete();

        if (delete>0){//已经下架

            viewHolder.del.setVisibility(View.VISIBLE);
        }else {


            viewHolder.del.setVisibility(View.GONE);
        }

        String book=comic.getBookLink();

        if (book==null||book.isEmpty()){
            book=DEFAULT_IMAGE;
        }

        GlideUrl glideUrl=new GlideUrl(book,new LazyHeaders.Builder().addHeader("Referer","http://images.dmzj.com/").build());

        Glide.with(viewHolder.view)
                .load(glideUrl)
                .into(viewHolder.book);

    }

    @Override
    public int getItemCount() {

        if (this.showList!=null){
            return showList.size();
        }

        return super.getItemCount();
    }


    /**
     * 查询漫画
     * @param s 查询关键字
     * @param activity Activity用于在主线程显示
     */
    public void query(String s, Activity activity){

        if (hideComicList==null){
            return;
        }

        new Thread(){
            @Override
            public void run() {
                super.run();

                queryData(s,activity);

            }
        }.start();




    }

    private void queryData(String s,Activity activity){

        List<HideComic> showList=new ArrayList<>();

        for (HideComic comic:hideComicList){

            if (comic.getAuthor().contains(s)||comic.getTitle().contains(s)){

                showList.add(comic);

            }

        }

        Runnable runnable= () -> {

            setShowList(showList);

        };

        activity.runOnUiThread(runnable);

    }

    public void resume(){

        setShowList(hideComicList);

    }


    static class ViewHolder extends BaseAdapter.ViewHolder{

        ImageView book;
        TextView title;
        TextView author;
        TextView del;
        View view;

        ViewHolder(HideItem itemView) {
            super(itemView);
            view=itemView;
            book=itemView.getBook();
            title=itemView.getTitle();
            author=itemView.getAuthor();
            del=itemView.getDel();
        }
    }

}
