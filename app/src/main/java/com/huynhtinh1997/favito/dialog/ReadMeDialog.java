package com.huynhtinh1997.favito.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.huynhtinh1997.favito.R;

/**
 * Created by huynhtinh1997 on 07/07/2017.
 */

public class ReadMeDialog extends AlertDialog {

    public ReadMeDialog(Context context) {
        super(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_read_me, null, false);
        setView(view);
        setCancelable(true);
    }
}
