package com.example.blogrestframework;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class PostTileAdapter extends RecyclerView.Adapter<PostTileAdapter.EventViewHolder> {

    private int ids[];
    private Context context;
    ArrayList<Post> posts;
    HashMap<Integer, Profile> profiles;

    public PostTileAdapter(Context ct, Collection<Post> posts, HashMap<Integer, Profile> profiles) {
        context = ct;
        this.posts = new ArrayList<>();
        this.posts.addAll(posts);
        Collections.sort(this.posts, Collections.reverseOrder());
        //Collections.reverse(this.posts);
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_tile, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, final int position) {
        final Post p = posts.get(position);
        holder.titleText.setText(p.getTitle());
        holder.contentText.setText(p.getContent());
        holder.authorText.setText(String.format(Locale.ENGLISH, "Author: %d", p.getAuthorId()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL yyyy");
        OffsetDateTime now = OffsetDateTime.now();
        Duration duration = Duration.between(p.getDatePosted(), now);
        String posted;
        if (duration.getSeconds() < 5) {
            posted = "just now";
        }
        else if (duration.getSeconds() < 60) {
            posted = duration.getSeconds() + " seconds ago";
        }
        else if (duration.toMinutes() < 60) {
            posted = duration.toMinutes() + " minutes ago";
        }
        else if (duration.toHours() < 24 ) {
            posted = duration.toHours() + " hours ago";
        }
        else {
            posted = p.getDatePosted().format(formatter);
        }
        //holder.datePostedText.setText(p.getDatePosted().toString().substring(0, 16));
        holder.datePostedText.setText(posted);
        if (profiles.containsKey(p.authorId)) {
            String imageUrl = "http://www.angri.li/" + profiles.get(p.authorId).imageUrl;

            Picasso.get()
                    .load(imageUrl)
                    //.resizeDimen(R.dimen.image_size, R.dimen.image_size) // fit already does this
                    //.onlyScaleDown()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .fit()
                    .centerCrop()
                    .into(holder.authorImage);

            holder.authorText.setText(profiles.get(p.authorId).username);
        }
        else {
            Log.i("TAG", "onBindViewHolder: the url for profile " + p.authorId + " hasn't been found ");
        }
        /*holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (e.getId() != 7) {
                    Intent intent = new Intent(context, EventDetailsActivity.class);
                    intent.putExtra("event_id", e.getId());
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, TicTacToeActivity.class);
                    intent.putExtra("event_id", e.getId());
                    context.startActivity(intent);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder{

        TextView titleText, contentText, authorText, datePostedText;
        ImageView authorImage;
        ConstraintLayout constraintLayout;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.post_title);
            contentText = itemView.findViewById(R.id.post_content);
            authorText = itemView.findViewById(R.id.post_author_id);
            datePostedText = itemView.findViewById(R.id.post_date_posted);
            authorImage = itemView.findViewById(R.id.post_author_pic);
            constraintLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
