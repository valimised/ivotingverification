package ee.vvk.ivotingverification.qr;

import android.content.Context;
import android.media.Image;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import ee.vvk.ivotingverification.QRScannerActivity;
import ee.vvk.ivotingverification.exceptions.InvalidQrCodeException;
import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

public class QRAnalyzer extends AppCompatActivity implements ImageAnalysis.Analyzer {

    private final BarcodeScannerOptions options =
            new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
    private final BarcodeScanner barcodeScanner = BarcodeScanning.getClient(options);
    private boolean detectQR = true;
    private final QRScannerActivity activity;

    public QRAnalyzer(QRScannerActivity activity) {
        this.activity = activity;
    }

    @Override
    @ExperimentalGetImage
    // ImageProxy.getImage() - the image may be shared with multiple ImageProxy - each ImageProxy
    // will not be the solid owner of the image. ExperimentalGetImage recommends to call
    // ImageProxy.close() to close the image - it will invalidate multiple ImageProxy.
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo()
                            .getRotationDegrees());
            barcodeScanner.process(image)
                    .addOnSuccessListener(
                            barcodes -> {
                                detectQR = !detectQR;
                                if (detectQR) return;
                                for (Barcode barcode : barcodes) {
                                    String rawValue = barcode.getRawValue();
                                    try {
                                        QRCodeContents qrCodeContents =
                                                new QRCodeContents(rawValue);
                                        Vibrator vibrator = (Vibrator) this.activity
                                                .getSystemService(Context.VIBRATOR_SERVICE);
                                        vibrator.vibrate(Util.VIBRATE_DURATION);
                                        this.activity.qrCodePassed(qrCodeContents);
                                    } catch (InvalidQrCodeException ex) {
                                        ex.printStackTrace();
                                        Util.startErrorIntent(this.activity,
                                                C.problemQrCodeMessage);
                                    }
                                }
                            }
                    ).addOnCompleteListener(completed -> imageProxy.close());
        }
    }
}
