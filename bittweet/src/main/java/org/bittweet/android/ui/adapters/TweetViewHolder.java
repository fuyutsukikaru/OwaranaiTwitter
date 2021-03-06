package org.bittweet.android.ui.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.bittweet.android.R;

public class TweetViewHolder {
    ImageView avatarImage;
    TextView userName;
    TextView displayName;
    TextView time;
    TextView tweet;
    View accent;
    TextView rtBy;
    ImageView mediaExpansion;
    View previewContainer;
    ImageView preview1;
    ImageView preview2;
    ImageView preview3;
    ImageView preview4;
    View frontView;
    View tweetContainer;
    ImageButton replyBtn;
    ImageButton retweetBtn;
    ImageButton favBtn;

    public TweetViewHolder(View container) {
        this.avatarImage = (ImageView) container.findViewById(R.id.avatar);
        this.userName = (TextView) container.findViewById(R.id.username);
        this.displayName = (TextView) container.findViewById(R.id.displayname);
        this.time = (TextView) container.findViewById(R.id.time);
        this.tweet = (TextView) container.findViewById(R.id.tweet);
        this.accent = container.findViewById(R.id.accent_container);
        this.rtBy = (TextView) container.findViewById(R.id.retweeted_by);
        this.mediaExpansion = (ImageView) container.findViewById(R.id.media_expansion);
        this.previewContainer = (View) container.findViewById(R.id.preview_container);
        this.preview1 = (ImageView) container.findViewById(R.id.preview1);
        this.preview2 = (ImageView) container.findViewById(R.id.preview2);
        this.preview3 = (ImageView) container.findViewById(R.id.preview3);
        this.preview4 = (ImageView) container.findViewById(R.id.preview4);
        this.frontView = container.findViewById(R.id.front_view);
        this.tweetContainer = container.findViewById(R.id.tweet_text_container);
        this.replyBtn = (ImageButton) container.findViewById(R.id.button_reply);
        this.retweetBtn = (ImageButton) container.findViewById(R.id.button_retweet);
        this.favBtn = (ImageButton) container.findViewById(R.id.button_fav);
    }
}
