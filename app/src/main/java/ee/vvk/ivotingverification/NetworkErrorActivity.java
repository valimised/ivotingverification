package ee.vvk.ivotingverification;

import android.content.Intent;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import ee.vvk.ivotingverification.util.C;

public class NetworkErrorActivity extends BaseErrorActivity {

    private boolean connectionButtonIsClicked = false;

    @Override
    public void onResume() {
        super.onResume();

        if (connectionButtonIsClicked) {
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    protected void initErrorWindow(String errorMessage) {
        super.initErrorWindow(errorMessage);

        Button buttonWifi = findViewById(R.id.btn_wifi);
        Button buttonOk = findViewById(R.id.btn_ok);

        buttonWifi.setText(C.btnWifi);
        buttonWifi.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        buttonWifi.setVisibility(View.VISIBLE);
        buttonWifi.setOnClickListener(this::clickWifiButton);

        buttonOk.setText(C.btnPacketData);
        buttonOk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        buttonOk.setVisibility(View.VISIBLE);
        buttonOk.setOnClickListener(this::clickPacketDataButton);

    }

    public void clickPacketDataButton(View view) {
        connectionButtonIsClicked = true;
        NetworkErrorActivity.this.startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public void clickWifiButton(View view) {
        connectionButtonIsClicked = true;
        NetworkErrorActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

}
