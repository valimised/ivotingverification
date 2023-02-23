/*
  This file incorporates work covered by the following copyright and
  permission notice:

  Copyright (C) 2008 ZXing authors

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package ee.vvk.ivotingverification.qr;

import android.content.Context;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Size;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ee.vvk.ivotingverification.util.Util;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
@SuppressWarnings("ALL")
public final class CameraManager extends AppCompatActivity {

    private static final String TAG = CameraManager.class.getSimpleName();
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 940;
    private static final int MAX_FRAME_HEIGHT = 540;
    private final Context context;
    private Camera camera;
    private int cameraId;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private boolean reverseImage;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider processCameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private int resoWidth;
    private int resoHeight;

    public CameraManager(Context context) {
        this.context = context;
    }

    public void cameraPreviewScanner(PreviewView previewView, QRAnalyzer QRAnalyzer,
                                     boolean turnOnFlash) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.context);
        cameraExecutor = Executors.newSingleThreadExecutor();
        try {
            processCameraProvider = cameraProviderFuture.get();
        } catch (ExecutionException | InterruptedException ignored) {
        }
        cameraProviderFuture.addListener(() -> {
            try {
                cameraPreviewScanner2(previewView, QRAnalyzer, turnOnFlash);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this.context));
    }

    private void cameraPreviewScanner2(PreviewView previewView, QRAnalyzer QRAnalyzer,
                                       boolean turnOnFlash) throws IOException {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector =
                new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(cameraExecutor, QRAnalyzer);
        processCameraProvider.unbindAll();
        camera = processCameraProvider.bindToLifecycle(
                (LifecycleOwner) this.context, cameraSelector,
                preview, imageCapture, imageAnalysis);

        if (turnOnFlash) {
            turnOnFlash(turnOnFlash);
        }
    }

    public void cameraPreview(PreviewView previewView) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.context);
        cameraExecutor = Executors.newSingleThreadExecutor();
        try {
            processCameraProvider = cameraProviderFuture.get();
        } catch (ExecutionException | InterruptedException ignored) {
        }
        cameraProviderFuture.addListener(() -> {
            cameraPreview2(previewView);
        }, ContextCompat.getMainExecutor(this.context));
    }

    private void cameraPreview2(PreviewView previewView) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector =
                new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        processCameraProvider.unbindAll();
        processCameraProvider.bindToLifecycle(
                (LifecycleOwner) this.context, cameraSelector, preview);
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin,
                                                   int hardMax) {
        int dim = resolution / 2; // Target 50% of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;

    }

    public void turnOnFlash(boolean turnOn) {
        if (camera != null) {
            try {
                if (turnOn) {
                    camera.getCameraControl().enableTorch(true);
                } else {
                    camera.getCameraControl().enableTorch(false);
                }
            } catch (Exception e) {
                Util.logWarning(TAG, "Error initiating or closing camera flash: ", e);
            }
        }
    }
}
