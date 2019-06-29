package com.remmoo997.igtvsaver.adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.activities.PhotoActivity;
import com.remmoo997.igtvsaver.activities.VideoActivity;
import com.remmoo997.igtvsaver.utils.Utility;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.GridViewHolder> {

    private final Context context;
    private final ArrayList<String> multi_data;
    private final String url;
    private final String title;

    public RecyclerViewAdapter(Context context, ArrayList<String> data, String url, String title) {
        this.context = context;
        this.multi_data = data;
        this.url = url;
        this.title = title;
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {
        private final PhotoView mImageView;
        private final ImageView vid_play;

        GridViewHolder(View itemView, int width) {
            super(itemView);
            vid_play = itemView.findViewById(R.id.vid_play);
            mImageView = itemView.findViewById(R.id.recycler_photo);
            mImageView.getLayoutParams().width = width;
        }
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_items, parent, false);
        // let's start by considering number of columns
        int width = parent.getMeasuredWidth() / 2;
        return new GridViewHolder(view, width);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder Vholder, int position) {
        try {
            String data = multi_data.get(position);
            Glide.with(context).setDefaultRequestOptions(new RequestOptions()).load(data).into(Vholder.mImageView);
            if (data.contains("mp4")) {
                Vholder.vid_play.setVisibility(View.VISIBLE);
            }
            Vholder.mImageView.setOnClickListener(v -> {
                if (data.contains("mp4")) {
                    Intent img = new Intent(context, VideoActivity.class);
                    img.putExtra("VideoUrl", data);
                    img.putExtra("VideoName", Utility.getVideoName(data));
                    img.putExtra("VideoLink", url);
                    context.startActivity(img);
                } else {
                    Intent img = new Intent(context, PhotoActivity.class);
                    img.putExtra("PictureUrl", data);
                    img.putExtra("PictureName", Utility.getPictureName(data));
                    img.putExtra("PictureLink", url);
                    img.putExtra("PictureTitle", title);
                    context.startActivity(img);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return multi_data.size();
    }
}
