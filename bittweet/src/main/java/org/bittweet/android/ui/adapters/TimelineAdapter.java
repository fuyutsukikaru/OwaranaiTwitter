package org.bittweet.android.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.SwipeListView;

import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.services.TweetService;
import org.bittweet.android.ui.NewTweetActivity;

import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends BaseAdapter {
    private static final String TAG = "TimelineAdapter";
    private final Context context;
    private final TweetAdapter tweetAdapter;
    private List<StatusItem> statusItems;

    public TimelineAdapter(Context context) {
        this.context = context;
        this.statusItems = new ArrayList<StatusItem>();
        this.tweetAdapter = new ExpandedTweetAdapter(context);
    }

    public void clearList() {
        statusItems.clear();
    }

    @Override
    public int getCount() {
        return statusItems.size();
    }

    @Override
    public Object getItem(int position) {
        return statusItems.get(position);
    }

    @Override
    public long getItemId(int i) {
        return statusItems.get(i).getStatus().getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TweetViewHolder holder;
        final StatusItem item = (StatusItem) getItem(position);

        // Use view holders
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.tweets_list_item, parent, false);
            holder = new TweetViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (TweetViewHolder) convertView.getTag();
        }

        ((SwipeListView) parent).recycle(convertView, position);
        tweetAdapter.recreateView(item, holder);

        // Fix item clicks
        final View finalConvertView = convertView;

        // Call the OnItemClick method of the ListView to open TweetDetails
        /*holder.frontView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //v.setBackgroundColor(Color.LTGRAY);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //v.setBackgroundColor(Color.WHITE);
                    ((ListView) finalConvertView.getParent()).performItemClick(finalConvertView, position, getItemId(position));
                } else {
                    //v.setBackgroundColor(Color.WHITE);
                }
                return true;
            }
        });*/
        holder.frontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("The view was clicked");
                ((ListView) finalConvertView.getParent()).performItemClick(finalConvertView, position, getItemId(position));
                //((ListView) finalConvertView.getParent()).performClick();
            }
        });

        // Buttons
        holder.replyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent replyIntent = new Intent(context, NewTweetActivity.class);
                replyIntent.setAction(NewTweetActivity.INTENT_REPLY);
                replyIntent.putExtra(NewTweetActivity.ARG_REPLY_TO_ID, item.getId());
                context.startActivity(replyIntent);
            }
        });

        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent favIntent = new Intent(context, TweetService.class);
                favIntent.setAction(TweetService.ACTION_FAV);
                favIntent.putExtra(TweetService.ARG_TWEET_ID, item.getId());
                context.startService(favIntent);
            }
        });

        holder.retweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent rtIntent = new Intent(context, TweetService.class);
                rtIntent.setAction(TweetService.ACTION_RT);
                rtIntent.putExtra(TweetService.ARG_TWEET_ID, item.getId());
                context.startService(rtIntent);
            }
        });

        return convertView;
    }

    public void attachStatusesList(List<StatusItem> data) {
        statusItems = data;
        notifyDataSetChanged();
    }
}
