package ee.vvk.ivotingverification;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;


public class ErrorActivity extends BaseErrorActivity {

    private static final String TAG = ErrorActivity.class.getSimpleName();

    protected void initErrorWindow(String errorMessage) {
        super.initErrorWindow(errorMessage);

        Button buttonWifi = findViewById(R.id.btn_wifi);
        Button buttonOk = findViewById(R.id.btn_ok);

        buttonWifi.setText(C.btnMore);
        buttonWifi.setVisibility(View.VISIBLE);
        buttonWifi.setOnClickListener(this::clickMoreButton);

        buttonOk.setText(C.btnOk);
        buttonOk.setVisibility(View.VISIBLE);
        buttonOk.setOnClickListener(v -> finish());

    }

    public void clickMoreButton(View view) {
        Intent showHelp = new Intent(Intent.ACTION_VIEW, Uri.parse(C.helpURL));
        try {
            startActivity(showHelp);
        } catch (ActivityNotFoundException e) {
            Util.logDebug(TAG, "Error while displaying help: ", e);
        }
    }


}
