package com.example.BSDriverApp2020.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.BSDriverApp2020.model.ModelRequest;
import com.example.BSDriverApp2020.R;
import com.example.BSDriverApp2020.interfaces.AdapterRequestCallback;

import java.util.List;

public class AdapterRequestList extends RecyclerView.Adapter<AdapterRequestList.ViewHolderAdapterRequestList> {

    List<ModelRequest> requestList;
    Context context;
    AdapterRequestCallback adapterRequestCallback;

    public AdapterRequestList(List<ModelRequest> requestList, Context context,AdapterRequestCallback requestCallback) {
        this.requestList = requestList;
        this.context = context;
        this.adapterRequestCallback=requestCallback;
    }

    @NonNull
    @Override
    public ViewHolderAdapterRequestList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.layout_request_list,parent,false);
        ViewHolderAdapterRequestList viewHolderAdapterRequestList=new ViewHolderAdapterRequestList(view);
        return viewHolderAdapterRequestList;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderAdapterRequestList holder, final int position) {

        holder.firstlatter.setText(String.valueOf(requestList.get(position).getUsername().charAt(0)));
        holder.customername.setText(requestList.get(position).getUsername());

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterRequestCallback.onClickAccept(position,requestList.get(position).getUserid());
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterRequestCallback.onClickReject(position,requestList.get(position).getUserid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void deleteItem(int pos){

    }

    public class ViewHolderAdapterRequestList extends RecyclerView.ViewHolder{

        TextView firstlatter,customername,accept,reject;
        public ViewHolderAdapterRequestList(@NonNull View itemView) {
            super(itemView);
            firstlatter=itemView.findViewById(R.id.firstLetter);
            customername=itemView.findViewById(R.id.customername);
            accept=itemView.findViewById(R.id.accept);
            reject=itemView.findViewById(R.id.reject);
        }
    }
}
