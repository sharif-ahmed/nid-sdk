package com.commlink.citl_nid_sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import citl_nid_sdk.NidDatabase;
import citl_nid_sdk.R;

public class ResultActivity extends AppCompatActivity {

    private static final String EXTRA_MATCH = "match";
    private static final String EXTRA_SCORE = "score";
    private static final String EXTRA_NID_NUMBER = "nid";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_DOB = "dob";

    public static void start(Context context, boolean match, float score, NIDInfo info) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_MATCH, match);
        intent.putExtra(EXTRA_SCORE, score);
        intent.putExtra(EXTRA_NID_NUMBER, info.getNidNumber());
        intent.putExtra(EXTRA_NAME, info.getName());
        intent.putExtra(EXTRA_DOB, info.getDateOfBirth());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView status = findViewById(R.id.statusText);
        //TextView details = findViewById(R.id.detailsText);
        Button done = findViewById(R.id.doneButton);

        boolean match = getIntent().getBooleanExtra(EXTRA_MATCH, false);
        float score = getIntent().getFloatExtra(EXTRA_SCORE, 0f);
        String nid = getIntent().getStringExtra(EXTRA_NID_NUMBER);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String dob = getIntent().getStringExtra(EXTRA_DOB);

        NIDInfo info = new NIDInfo(nid, name, dob);

        status.setText(match ? R.string.nid_result_match : R.string.nid_result_not_match);
        /*details.setText(getString(R.string.nid_result_details,
                nid, name, dob, String.format("%.3f", score)));*/

        done.setOnClickListener(v -> {
            NIDCallback cb = CallbackHolder.getInstance().getCallback();
            if (cb != null) {
                cb.onSuccess(match, score, info);
            }
            BitmapHolder.clear();
            CallbackHolder.getInstance().clear();
            finish();
        });
    }
}

