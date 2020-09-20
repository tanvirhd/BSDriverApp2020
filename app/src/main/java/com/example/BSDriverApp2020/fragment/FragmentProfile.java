package com.example.BSDriverApp2020.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.BSDriverApp2020.databinding.FragmentProfileBinding;


public class FragmentProfile extends Fragment {
    private static String TAG="FragmentProfile";
    private FragmentProfileBinding binding;


    public FragmentProfile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding=FragmentProfileBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}