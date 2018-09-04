package com.aguilartristen.confidential;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    Context context;
    ArrayList<String> nameList;
    ArrayList<String> profilePicList;
    ArrayList<String> statusList;
    ArrayList<String> uidList;

    public class SearchViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView profileImage;
        TextView name, status;

        public SearchViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            profileImage = (ImageView) itemView.findViewById(R.id.search_profile_image);
            name = (TextView) itemView.findViewById(R.id.search_name_text);
            status = (TextView) itemView.findViewById(R.id.search_status_text);
        }
    }

    public SearchAdapter(Context context, ArrayList<String> nameList, ArrayList<String> profilePicList, ArrayList<String> statusList,
                         ArrayList<String> uidList) {

        this.context = context;
        this.nameList = nameList;
        this.profilePicList = profilePicList;
        this.statusList = statusList;
        this.uidList = uidList;

    }

    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.search_single_layout, parent, false);
        return new SearchAdapter.SearchViewHolder(view);

    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, final int position) {

        holder.name.setText(nameList.get(position));
        holder.status.setText(statusList.get(position));
        Picasso.with(context).load(profilePicList.get(position)).placeholder(R.mipmap.user_icon).into(holder.profileImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //For Testing purposes

                //Toast.makeText(context, "View Clicked: " + uidList.get(position), Toast.LENGTH_SHORT).show();

                Intent profile_Intent = new Intent(context, ProfileActivity.class);
                profile_Intent.putExtra("user_id", uidList.get(position));
                context.startActivity(profile_Intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }
}