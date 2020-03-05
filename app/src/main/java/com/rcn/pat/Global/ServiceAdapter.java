package com.rcn.pat.Global;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rcn.pat.R;
import com.rcn.pat.ViewModels.ServiceInfo;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.MyViewHolder> {
    private final onClickVIewDetail _event;
    List<RelativeLayout> layoutViewList = new ArrayList<>();
    private View _view;
    private ArrayList<ServiceInfo> dataSet;

    public ServiceAdapter(ArrayList<ServiceInfo> data, onClickVIewDetail event) {
        this.dataSet = data;
        this._event = event;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView txtUserService = holder.txtUserService;
        TextView txtPhone = holder.txtPhone;
        TextView txtBegin = holder.txtBegin;
        TextView txtEnd = holder.txtEnd;
        ConstraintLayout layoutCompanyView = holder.layoutCompanyView;
        ImageButton btnGoDetail = holder.btnGoDetail;

        txtUserService.setText(dataSet.get(listPosition).getNombreUsuarioSolicitante());
        txtPhone.setText(dataSet.get(listPosition).getCelularSolicitante());

        txtBegin.setText(GlobalClass.getInstance().getDateFormat(dataSet.get(listPosition).getFechaInicial()));
        txtEnd.setText(GlobalClass.getInstance().getDateFormat(dataSet.get(listPosition).getFechaFinal()));

        btnGoDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_event != null)
                    _event.onClick(dataSet.get(listPosition));
            }
        });
        layoutCompanyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_event != null)
                    _event.onClick(dataSet.get(listPosition));
            }
        });

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        MyViewHolder myViewHolder = null;

        try {

            _view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_list_driver_services_items, parent, false);
            myViewHolder = new MyViewHolder(_view);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return myViewHolder;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtUserService;
        TextView txtPhone;
        TextView txtBegin;
        TextView txtEnd;
        ImageButton btnGoDetail;
        ConstraintLayout layoutCompanyView;

        public MyViewHolder(View itemView) {
            super(itemView);
            layoutCompanyView = itemView.findViewById(R.id.constraintListService);
            this.txtUserService = (TextView) itemView.findViewById(R.id.txtUserService);
            this.txtPhone = (TextView) itemView.findViewById(R.id.txtPhone);
            this.txtBegin = (TextView) itemView.findViewById(R.id.txtBegin);
            this.txtEnd = (TextView) itemView.findViewById(R.id.txtEnd);
            this.btnGoDetail = itemView.findViewById(R.id.btnGoDetail);


        }
    }
}