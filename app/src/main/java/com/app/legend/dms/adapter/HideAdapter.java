package com.app.legend.dms.adapter;

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
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import java.util.List;

public class HideAdapter extends BaseAdapter<HideAdapter.ViewHolder> {


    List<HideComic> hideComicList;
    private static final String DEFAULT_IMAGE="https://ws1.sinaimg.cn/large/c13993a9gy1g180fo10xtj20qn12w44m.jpg";

    private HideItemOnClickListener listener;

    public void setListener(HideItemOnClickListener listener) {
        this.listener = listener;
    }

    public void setHideComicList(List<HideComic> hideComicList) {
        this.hideComicList = hideComicList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        HideItem view=new HideItem(viewGroup.getContext());

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {

            /*点击事件，交给外部处理*/
            if (this.listener!=null&&hideComicList!=null){

                int position=viewHolder.getAdapterPosition();
                HideComic comic=hideComicList.get(position);
                listener.itemClick(comic);//

            }

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        if (this.hideComicList==null){
            return;
        }

        HideComic comic=this.hideComicList.get(i);
        viewHolder.title.setText(comic.getTitle());
        viewHolder.author.setText("作者："+comic.getAuthor());

        String book=comic.getBookLink();

        if (book==null||book.isEmpty()){
            book="https://images.dmzj.com/webpic/5/zhuanshengeyizhihaobachupomieqibiao21658l.jpg";
        }

        GlideUrl glideUrl=new GlideUrl(book,new LazyHeaders.Builder().addHeader("Referer","http://images.dmzj.com/").build());

        Glide.with(viewHolder.view)
                .load(glideUrl)
                .into(viewHolder.book);

    }

    @Override
    public int getItemCount() {

        if (this.hideComicList!=null){
            return hideComicList.size();
        }

        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        ImageView book;
        TextView title;
        TextView author;
        View view;

        ViewHolder(HideItem itemView) {
            super(itemView);
            view=itemView;
            book=itemView.getBook();
            title=itemView.getTitle();
            author=itemView.getAuthor();
        }
    }

}
