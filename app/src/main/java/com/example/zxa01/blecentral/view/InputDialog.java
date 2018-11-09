package com.example.zxa01.blecentral.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.example.zxa01.blecentral.R;
import com.example.zxa01.blecentral.service.BluetoothLeService;

public class InputDialog {

    private Context context;
    private AlertDialog.Builder builder;

    public InputDialog(Context context) {
        this.context = context;
    }

    public void show() {
        // setup input view
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.input_dialog, null);
        EditText mWriteValue = view.findViewById(R.id.writeValue);

        // setup builder
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_correct, (dialog, which) -> {
                    // 送出數值
                    Intent intent = new Intent(BluetoothLeService.WRITE_DATA);
//                    intent.putExtra("value",mWriteValue.getText().toString());
                    context.sendBroadcast(intent);
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
