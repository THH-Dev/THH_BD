package com.example.scanimin.Fragment;
import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.scanimin.R;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.constance.Value;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.DBRemote.Retrofit2;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Local.SharedPreference;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.databinding.LayoutSettingBinding;
import com.example.scanimin.function.ExcelExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingFragment extends Fragment {
    public interface OnItemClickListener {
        void onItemClickedAll(Customer customer);
    }
    private List<Customer> listCustomer;
    private CallApi callApi;
    private SQLLite dbHelper;
    public static final int CREATE_FILE_REQUEST_CODE = 1001;
    private SharedPreference sharedPreference;
    private LayoutSettingBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = LayoutSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callApi = new CallApi();
        dbHelper = new SQLLite(requireActivity());
        listCustomer = new ArrayList<>();
        sharedPreference = new SharedPreference(requireActivity());
        getData();
        binding.lnSave.setOnClickListener(v -> {
            if (getActivity() != null) {
                sharedPreference.saveIpServer(binding.ip.getText().toString());
                sharedPreference.savePortServer(binding.port.getText().toString());
                Retrofit2.getBaseUrl("http://" + sharedPreference.getIpServer() + ":" + sharedPreference.getPortServer() + "/");
                ((Scanner) getActivity()).backToCameraFromSetting();
            }
        });
        binding.imgBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((Scanner) getActivity()).backToCameraFromSetting();
            }
        });
        binding.createDir.setOnClickListener(v -> {
            createExcelFilePicker();
        });
        binding.nameFileExport.setText(sharedPreference.getDirFile());
        binding.ip.setText(sharedPreference.getIpServer());
        binding.port.setText(sharedPreference.getPortServer());
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                if (sharedPreference.getDirFile() != null) {
                    String name = ExcelExporter.getFileNameFromUri(requireActivity(), fileUri);
                    sharedPreference.saveDirFile(fileUri.getPath() +"/"+ name);
                }
                ExcelExporter.exportCustomersToExcel(requireActivity(), listCustomer, fileUri);
                binding.nameFileExport.setText(sharedPreference.getDirFile());
            }
        }
    }
    private void getData(){
        callApi = new CallApi();
        callApi.getCustomer(requireActivity());
        dbHelper = new SQLLite(requireActivity());
        List<Customer> updatedList = dbHelper.getAllPersons();
        listCustomer.clear();
        listCustomer.addAll(updatedList);
    }

    public void createExcelFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.ms-excel");
        intent.putExtra(Intent.EXTRA_TITLE, "Customers_file");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

}