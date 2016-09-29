package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.quickblox.sample.core.R;

public abstract class MessageTextView extends FrameLayout {
    private static String TAG = MessageTextView.class.getSimpleName();
    protected LinearLayout frameLinear;
    protected ViewStub viewTextStub;

    LinearLayout layoutStub;
    LayoutInflater inflater;

    public MessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(R.layout.message_test_view);
        applyAttributes(attrs);
    }

    protected void init(@LayoutRes int layoutId) {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);
        frameLinear = (LinearLayout) getRootView().findViewById(R.id.linear_frame);
        viewTextStub = (ViewStub) findViewById(R.id.stub_text);
    }

    protected void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        int textViewId;
        int widgetId;

        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.MessageTextView);
            textViewId = array.getResourceId(R.styleable.MessageTextView_message_text_id, 0);
            widgetId = array.getResourceId(R.styleable.MessageTextView_widget_id, 0);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }

        setLinearSide();
        setTextLayout(textViewId);
        setWidget(widgetId);
    }

    protected void setWidget(@LayoutRes int widgetId) {
        if (widgetId != 0) {
            final ViewGroup widgetFrame = (ViewGroup) findViewById(R.id.widget_frame);
            View view = inflater.inflate(widgetId, widgetFrame);
            Log.d(TAG, "view=null? " + (view == null));
        }
    }

    abstract protected void setLinearSide();

    abstract protected void setTextLayout(@LayoutRes int textViewId);
}