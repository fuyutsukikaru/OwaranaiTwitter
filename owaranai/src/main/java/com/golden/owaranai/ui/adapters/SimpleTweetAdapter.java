package com.golden.owaranai.ui.adapters;

import android.content.Context;
import android.view.View;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.ui.util.RoundedTransformation;
import com.golden.owaranai.ui.util.TransparentLinkMovementMethod;
import com.golden.owaranai.ui.util.TweetFormatter;
import com.squareup.picasso.Picasso;
import twitter4j.Status;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SimpleTweetAdapter implements TweetAdapter {
    private final Context context;
    private final SimpleDateFormat dateFormat;
    private final Picasso picasso;

    public SimpleTweetAdapter(Context context) {
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM d yyyy, K:mm a", Locale.getDefault());
        this.picasso = Picasso.with(context);
    }

    protected Context getContext() {
        return context;
    }

    protected Picasso getPicasso() {
        return picasso;
    }

    @Override
    public void recreateView(StatusItem item, TimelineAdapter.ViewHolder holder) {
        Status status = item.getStatus();

        if(status.isRetweet()) {
            status = status.getRetweetedStatus();
        }

        holder.tweet.setMovementMethod(new TransparentLinkMovementMethod(holder.tweetContainer));
        holder.tweet.setText(TweetFormatter.formatStatusText(context, status));
        holder.displayName.setText(status.getUser().getName());
        holder.userName.setText("@" + status.getUser().getScreenName());
        holder.time.setText(dateFormat.format(status.getCreatedAt()));

        // Defaults
        holder.mediaExpansion.setVisibility(View.GONE);
        holder.accent.setVisibility(View.GONE);
        holder.rtBy.setVisibility(View.GONE);

        if(item.isRetweetedByMe()) {
            // TODO
        }

        if(item.isFavorited()) {
            holder.favBtn.setImageResource(R.drawable.ic_rating_not_important);
        } else {
            holder.favBtn.setImageResource(R.drawable.ic_rating_important);
        }

        // Use Picasso to retrieve and set profile images. Get from cache if already exists.
        picasso.load(status.getUser().getBiggerProfileImageURLHttps())
                .transform(new RoundedTransformation(50, 0))
                .into(holder.avatarImage);
    }
}
