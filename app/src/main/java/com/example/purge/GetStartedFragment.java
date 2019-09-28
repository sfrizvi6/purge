package com.example.purge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.purge.databinding.FragmentGetStartedBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.content.Context.CAMERA_SERVICE;

public class GetStartedFragment extends Fragment {

    static final String TAG = GetStartedFragment.class.getSimpleName();
    private static final String WELCOME_TEXT = "Welcome!";
    private static final String GET_STARTED = "Get Started";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private FragmentGetStartedBinding binding;

    public GetStartedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_get_started, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.welcomeTextView.setText(WELCOME_TEXT);
        setUpScanItemButtonClickListener();
    }

    private void setUpScanItemButtonClickListener() {
        binding.scanItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.welcomeTextView.setText(GET_STARTED);

                // launch camera
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO: to obtain captured image
//        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.the_only_woman_in_the_room_rotated);
//        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(CAMERA_SERVICE);
        Matrix matrix = new Matrix();
        int rotationCompensation = 0;
        try {
            rotationCompensation = getRotationCompensation(cameraManager.getCameraIdList()[0], getActivity(), getContext());
        } catch (CameraAccessException e) {
            Log.d(TAG, "Error rotating image");
        }
        matrix.postRotate(rotationCompensation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(rotatedBitmap);
//        initializeTextRecognition(image);
        initializeBarcodeScanning(firebaseVisionImage);
    }

    private void initializeTextRecognition(@NonNull FirebaseVisionImage image) {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getCloudTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        navigateToPreviewFragment(firebaseVisionText.getText(), null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        navigateToPreviewFragment("Failed to read text " + e.getLocalizedMessage(), null);
                    }
                });
    }

    private void initializeBarcodeScanning(@NonNull FirebaseVisionImage image) {
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(@Nullable List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        if (firebaseVisionBarcodes == null || firebaseVisionBarcodes.size() <= 0 || TextUtils.isEmpty(firebaseVisionBarcodes.get(0).getDisplayValue())) {
                            navigateToPreviewFragment("Oops empty barcode received", null);
                            return;
                        }
                        Log.i("Barcode read", firebaseVisionBarcodes.get(0).getDisplayValue());
                        fetchProductInfo(firebaseVisionBarcodes.get(0).getDisplayValue());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        navigateToPreviewFragment("Failed to scan barcode " + e.getLocalizedMessage(), null);
                    }
                });
    }

    private void navigateToPreviewFragment(@NonNull String text, @Nullable String imageUrl) {
        if (getActivity() == null || getActivity().getSupportFragmentManager() == null) {
            return;
        }
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        PreviewFragment previewFragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("productName", text);
        bundle.putString("productImageUrl", imageUrl);
        previewFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.main_activity_container, previewFragment, PreviewFragment.TAG);
        fragmentTransaction.addToBackStack(PreviewFragment.TAG);
        fragmentTransaction.commit();
    }

    private void fetchProductInfo(@NonNull String barcode) {
        if (getContext() == null) {
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://api.barcodelookup.com/v2/products?barcode=" + barcode + "&key=jvh8q0sjmt426omxfcsh9ptr6iug90";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String productName = "";
                        String imageUrl = null;
                        try {
                            JSONObject product = (JSONObject) response.getJSONArray("products").get(0);
                            productName = product.getString("product_name");
                            productName = TextUtils.isEmpty(productName) ? product.getString("title") : productName;
                            imageUrl = (String) product.getJSONArray("images").get(0);
                        } catch (JSONException e) {
                            Log.d(TAG, "Error encountered parsing product information JSON");
                        }
                        navigateToPreviewFragment(productName, imageUrl);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        navigateToPreviewFragment("Oops barcode look up fail", null);
                    }
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        return (rotationCompensation + sensorOrientation + 270) % 360;
    }

}
