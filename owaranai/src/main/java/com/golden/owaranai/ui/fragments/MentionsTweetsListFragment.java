package com.golden.owaranai.ui.fragments;

import android.os.Bundle;
import com.golden.owaranai.ApplicationController;

public class MentionsTweetsListFragment extends TweetsListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTimelineContent(((ApplicationController) getActivity().getApplication()).getMentionsTimelineContent());
    }
}