package com.example.purge;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.purge.databinding.FragmentPreviewBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class PreviewFragment extends Fragment {

    static final String TAG = PreviewFragment.class.getSimpleName();

    private FragmentPreviewBinding binding;

    public PreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        if (arguments == null || arguments.getString("productName") == null) {
            return;
        }
        String productName = arguments.getString("productName");
        String productImageUrl = arguments.getString("productImageUrl");
        binding.productName.setText(productName);

        Glide.with(getContext())
                .load(productImageUrl)
                .into(binding.productImage);

        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
