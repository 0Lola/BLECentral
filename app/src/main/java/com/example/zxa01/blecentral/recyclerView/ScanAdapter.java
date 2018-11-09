package com.example.zxa01.blecentral.recyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zxa01.blecentral.ConnectActivity;
import com.example.zxa01.blecentral.R;

import java.util.LinkedList;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {

    private LinkedList<BluetoothDevice> mDeviceList;
    private Context mContext;

    public ScanAdapter(Context context, LinkedList<BluetoothDevice> mDeviceList) {
        this.mDeviceList = mDeviceList;
        this.mContext = context;
    }

    @SuppressLint("LongLogTag")
    @Override
    public ScanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScanAdapter.ViewHolder holder, int position) {
        holder.bindTo(mDeviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    private void connect(String name, String address, String state) {
        Intent intent = new Intent(mContext, ConnectActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("address", address);
        intent.putExtra("state", state);
        mContext.startActivity(intent);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LinearLayout mLinearLayout;
        public TextView address;
        public TextView bond;
        public TextView name;
        public TextView type;

        public ViewHolder(View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.addressTextView);
            bond = itemView.findViewById(R.id.bondStateTextView);
            name = itemView.findViewById(R.id.nameTextView);
            type = itemView.findViewById(R.id.typeTextView);
            mLinearLayout = itemView.findViewById(R.id.linearLayout);
            mLinearLayout.setOnClickListener(this);
        }

        @SuppressLint("NewApi")
        void bindTo(BluetoothDevice device) {
            address.setText(device.getAddress() == null ? "NULL" : device.getAddress());
            bond.setText(device.getBondState() == BluetoothDevice.BOND_BONDED ?
                    "Bonded" : device.getBondState() == BluetoothDevice.BOND_BONDING ? "BONDING" : "No Bonded");
            name.setText(device.getName() == null ? "NULL" : device.getName());
            type.setText(String.valueOf(device.getType()));
        }

        @Override
        public void onClick(View v) {
            connect(name.getText().toString(), address.getText().toString(), bond.getText().toString());
        }
    }

}
