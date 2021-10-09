package ee.vvk.ivotingverification;

import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import ee.vvk.ivotingverification.util.C;

public class VersionErrorActivity extends BaseErrorActivity {

    protected void initErrorWindow(String errorMessage) {
        super.initErrorWindow(errorMessage);

        Button buttonWifi = findViewById(R.id.btn_wifi);
        Button buttonOk = findViewById(R.id.btn_ok);

        buttonWifi.setText(C.btnUpdate);
        buttonWifi.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        buttonWifi.setVisibility(View.VISIBLE);
        buttonWifi.setOnClickListener(this::clickUpdateButton);

        buttonOk.setText(C.btnOk);
        buttonOk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        buttonOk.setVisibility(View.VISIBLE);
        buttonOk.setOnClickListener(v -> finish());
    }

    public void clickUpdateButton(View view) {
        VersionErrorActivity.this.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=ee.ivxv.ivotingverification")));
    }

}
