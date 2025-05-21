package com.example.scanimin.Fragment.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.scanimin.ListCustomer.CustomerAdapter;
import com.example.scanimin.R;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.function.FunctionUtils;
import com.example.scanimin.popup.PopupCompare;

import java.util.ArrayList;
import java.util.List;

public class CustomerNotChecked extends Fragment implements Searchable{
    private RecyclerView recyclerView;
    private CustomerAdapter customerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Customer> customerList;
    private CallApi callApi;
    private SQLLite dbHelper;
    private PopupCompare popupCompare;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClickedNotChecked(Customer customer);
    }

    public CustomerNotChecked() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customerList = new ArrayList<>();
        getData();

        recyclerView = view.findViewById(R.id.recyclerViewAll);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create and set adapter
        if (customerList != null) {
            customerAdapter = new CustomerAdapter(customerList, requireActivity(), new CustomerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Customer customer, View view) {
                    String string = "";
                    int url = 0;
                    popupCompare = new PopupCompare(string, url, requireActivity(), customer, new PopupCompare.PopupCompareListener() {
                        @Override
                        public void onCompareUpdated() {
                            //bat lại scan
                        }
                    }, new PopupCompare.EditListener(){
                        @Override
                        public void onEdit(Customer customer) {

                        }
                    });
                    popupCompare.show();
                }
            }, new CustomerAdapter.OnItemCameraListener(){
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onItemClick(Customer customer, View view) {
                    if (listener != null) {
                        listener.onItemClickedNotChecked(customer);
                        getData();
                        customerAdapter.notifyDataSetChanged();
                    }
                }
            });
            recyclerView.setAdapter(customerAdapter);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getData();
                    reloadData();
                }
            });
        }
    }
    private void reloadData() {
        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                if (FunctionUtils.isInternetAvailable(requireActivity())) {
                    Toast.makeText(requireActivity(), R.string.data_download_success, Toast.LENGTH_SHORT).show();
                    customerAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(requireActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    private void getData(){
        customerList.clear();
        callApi = new CallApi();
        callApi.getCustomer(requireActivity());
        dbHelper = new SQLLite(requireActivity());
        for (Customer customer : dbHelper.getAllPersons()) {
            if (customer.getImage() == null){
                customerList.add(customer);
            }
        }
        if (customerAdapter != null) {
            customerAdapter.updateData(customerList);
        }
    }
    public void onSearchQuery(String query) {
        if (customerAdapter != null) {
            customerAdapter.filter(query);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CustomerNotChecked.OnItemClickListener) {
            listener = (CustomerNotChecked.OnItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnItemClickListener");
        }
    }
    public void refreshData() {
        getData();
        reloadData();// hoặc chỉ gọi customerAdapter.updateData(...)
    }

}
