package com.ft.shippo.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ft.shippo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;
import com.mindorks.butterknifelite.annotations.OnLongClick;

/**
 * Created by rafael on 25/02/18.
 */

public class ResetDialogFragment extends BottomSheetDialogFragment{
    @BindView(R.id.text_input_edit_text)
    TextInputEditText editText;
    @BindView(R.id.reset_pass_layout)
    View passView;
    @BindView(R.id.email_not_sent_layout)
    View emailnotsentView;
    @BindView(R.id.email_sent_layout)
    View emailsentView;
    @BindView(R.id.send_button)
    Button sendButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_reset_password, container, false);
        ButterKnifeLite.bind(this, v);
        return v;
    }

    @OnClick(R.id.send_button)
    public void sendClick(){
        sendButton.setEnabled(false);
        sendButton.setText(R.string.sending);
        FirebaseAuth.getInstance().sendPasswordResetEmail(editText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                passView.setVisibility(View.GONE);
                if(task.isSuccessful())emailsentView.setVisibility(View.VISIBLE);
                else emailnotsentView.setVisibility(View.VISIBLE);
            }
        });
    }
}
