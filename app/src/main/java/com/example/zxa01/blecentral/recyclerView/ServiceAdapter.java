package com.example.zxa01.blecentral.recyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zxa01.blecentral.service.BluetoothLeService;
import com.example.zxa01.blecentral.R;

import java.util.LinkedList;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private LinkedList<BluetoothGattCharacteristic> mCharacteristicList;
    private Context mContext;

    public ServiceAdapter(Context context, LinkedList<BluetoothGattCharacteristic> mCharacteristicList) {
        this.mCharacteristicList = mCharacteristicList;
        this.mContext = context;
    }

    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.peripheral_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceAdapter.ViewHolder holder, int position) {
        holder.bindTo(mCharacteristicList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCharacteristicList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mLinearLayout;
        public BluetoothGattCharacteristic characteristic;
        public TextView uuid;
        public TextView properties;
        public TextView value;

        public ViewHolder(View itemView) {
            super(itemView);
            uuid = itemView.findViewById(R.id.uuidTextView);
            properties = itemView.findViewById(R.id.propertiesTextView);
            value = itemView.findViewById(R.id.valueTextView);
            mLinearLayout = itemView.findViewById(R.id.peripheralLinearLayout);
        }

        @SuppressLint("NewApi")
        void bindTo(BluetoothGattCharacteristic characteristic) {
            this.characteristic = characteristic;

            String uuidName;
            switch (characteristic.getUuid().toString()) {
                case BluetoothLeService.SERVICE_UUID:
                    uuidName = "SERVICE";
                    break;
                case BluetoothLeService.OBJECT_UUID:
                    uuidName = "OBJECT";
                    break;
                case BluetoothLeService.CHANGE_UUID:
                    uuidName = "CHANGE";
                    break;
                case BluetoothLeService.DESCRIPTORS_UUID:
                    uuidName = "DESCRIPTORS";
                    break;
                default:
                    uuidName = characteristic.getUuid().toString();
                    break;
            }
            uuid.setText(uuidName);

            String property;
            switch (characteristic.getProperties()) {
                case BluetoothGattCharacteristic.PROPERTY_BROADCAST:
                    property = "Broadcast";
                    break;
                case BluetoothGattCharacteristic.PROPERTY_READ:
                    property = "Read";
                    break;
                case BluetoothGattCharacteristic.PROPERTY_WRITE:
                    property = "Write";
                    break;
                case BluetoothGattCharacteristic.PROPERTY_NOTIFY:
                    property = "Notify";
                    break;
                case BluetoothGattCharacteristic.PROPERTY_INDICATE:
                    property = "Indicate";
                    break;
                default:
                    property = "Unknow";
                    break;
            }
            properties.setText(property);
            value.setText(characteristic.getValue() == null ? "無" : new String(characteristic.getValue()));
        }
    }
}
