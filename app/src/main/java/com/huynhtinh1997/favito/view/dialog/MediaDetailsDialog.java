package com.huynhtinh1997.favito.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huynhtinh1997.favito.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by huynhtinh1997 on 02/07/2017.
 */

public class MediaDetailsDialog extends AlertDialog {
    private static final String TAG = "MediaDetailsDialog";
    private static long ONE_KILO_BYTE = 1024;
    private static long ONE_MEGA_BYTE = 1024 * 1024;
    private static long ONE_GIGA_BYTE = 1024 * 1024 * 1024;
    private static String BYTE = "B";
    private static String KILO_BYTE = "KB";
    private static String MEGA_BYTE = "MB";
    private static String GIGA_BYTE = "GB";

    private TextView mNameTextView;
    private TextView mLastModifiedDateTextView;
    private TextView mLengthTextView;
    private TextView mPathTextView;

    public MediaDetailsDialog(Context context, File file) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_details, null);
        setView(view);
        setCancelable(true);
        hookUpComponents(view);
        passDataToTextViews(file);
    }

    private void hookUpComponents(View rootView) {
        mNameTextView = rootView.findViewById(R.id.file_name_text_view);
        mLastModifiedDateTextView = rootView.findViewById(R.id.last_modified_date_text_view);
        mLengthTextView = rootView.findViewById(R.id.length_text_view);
        mPathTextView = rootView.findViewById(R.id.path_text_view);
        Button okButton = rootView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaDetailsDialog.this.dismiss();
            }
        });
    }

    private void passDataToTextViews(File file) {
        mNameTextView.setText(file.getName());
        mLastModifiedDateTextView.setText(getFormattedLastModifiedDate(file.lastModified()));
        mLengthTextView.setText(getFormattedLength(file.length()));
        mPathTextView.setText(file.getAbsolutePath());
    }

    private String getFormattedLastModifiedDate(long date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(date));
    }

    private String getFormattedLength(long length) {
        if (length < ONE_KILO_BYTE) {
            return length + BYTE;
        } else if (length < ONE_MEGA_BYTE) {
            return length / ONE_KILO_BYTE + KILO_BYTE;
        } else if (length < ONE_GIGA_BYTE) {
            return length / ONE_MEGA_BYTE + MEGA_BYTE;
        } else if (length >= ONE_GIGA_BYTE) {
            return length / ONE_GIGA_BYTE + GIGA_BYTE;
        }
        return 0 + BYTE;
    }

}
