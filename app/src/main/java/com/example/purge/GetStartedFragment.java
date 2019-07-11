package com.example.purge;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.purge.databinding.FragmentGetStartedBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hand_writing);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                Log.i("Text read", firebaseVisionText.getText());
                                navigateToPreviewFragment(firebaseVisionText.getText());
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        Log.d("Failed to read text", e.getLocalizedMessage());
                                    }
                                });
    }

    private void navigateToPreviewFragment(String text) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        PreviewFragment previewFragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        previewFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.main_activity_container, previewFragment, PreviewFragment.TAG);
        fragmentTransaction.commit();
    }
}
