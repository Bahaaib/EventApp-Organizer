package com.bahaa.eventorganizerapp.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;

import com.bahaa.eventorganizerapp.Models.HeadModel;
import com.bahaa.eventorganizerapp.R;
import com.bahaa.eventorganizerapp.Root.DialogListener;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AdminDialog extends DialogFragment {
    private Context mContext;

    private HeadModel head;
    private String adminType;
    private String adminActive;
    private DialogListener listener;

    @BindView(R.id.dialog_admin_name)
    TextInputEditText adminNameField;
    @BindView(R.id.dialog_admin_mobile)
    TextInputEditText adminPhoneField;
    @BindView(R.id.dialog_admin_type)
    AppCompatSpinner typeSpinner;
    @BindView(R.id.dialog_admin_status)
    AppCompatSpinner statusSpinner;
    @BindView(R.id.ok_button)
    AppCompatButton okBtn;
    @BindView(R.id.cancel_button)
    AppCompatButton cancelBtn;

    private Unbinder unbinder;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        listener = (DialogListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_admin, container, false);

        unbinder = ButterKnife.bind(this, view);

        initTypeSpinner();
        initStatusSpinner();

        Bundle args = getArguments();
        if (args != null) {
            String HEAD_KEY = "head_key";
            head = (HeadModel) args.getSerializable(HEAD_KEY);
            assert head != null;
            adminNameField.setText(head.getName());
            adminPhoneField.setText(head.getPhone());

            if (head.getPrivilege().equals("content_manager")) {
                typeSpinner.setSelection(0);
            } else {
                typeSpinner.setSelection(1);
            }

            if (head.getStatus()) {
                statusSpinner.setSelection(0);
            } else {
                statusSpinner.setSelection(1);
            }
        } else {
            head = new HeadModel();
        }

        //Type Spinner
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adminType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Activity Spinner
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adminActive = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        return view;
    }

    @OnClick(R.id.ok_button)
    void pressedOK() {
        String adminName = adminNameField.getText().toString();
        String adminPhone = adminPhoneField.getText().toString();
        if (!adminName.isEmpty() && !adminPhone.isEmpty() && isValidMobileNumber(adminPhone)) {

            head.setName(adminName);
            //Remove spaces from copied phone number from clipboard
            adminPhone = adminPhone.replaceAll("\\s+", "");
            head.setPhone(adminPhone);

            if (adminType.equals("أدمن")) {
                head.setPrivilege("admin");
            } else if (adminType.equals("مدير محتوى")) {
                head.setPrivilege("content_manager");
            }

            if (adminActive.equals("نشط")) {
                head.setStatus(true);
            } else if (adminActive.equals("غير نشط")) {
                head.setStatus(false);
            }

            listener.onAdminDataChanged(head);
            getDialog().dismiss();
        }
    }

    @OnClick(R.id.cancel_button)
    void pressedCancel() {
        Objects.requireNonNull(getDialog()).dismiss();
    }

    @OnClick(R.id.dialog_admin_name)
    void pressedNameEditText() {
        String adminName = adminNameField.getText().toString();
        if (!adminName.isEmpty()) {
            head.setName(adminName);
        } else {
            adminNameField.setError(getString(R.string.admin_name_error));
        }
    }


    @OnClick(R.id.dialog_admin_mobile)
    void pressedMobileEditText() {
        String adminPhone = adminPhoneField.getText().toString();
        if (!adminPhone.isEmpty() && isValidMobileNumber(adminPhone)) {

            //Remove spaces from copied phone number from clipboard
            adminPhone = adminPhone.replaceAll("\\s+", "");
            head.setPhone(adminPhone);
        } else {
            adminPhoneField.setError(getString(R.string.admin_phone_error));
        }
    }

    private void initTypeSpinner() {
        String admin = getString(R.string.admin_text);
        String contentManager = getString(R.string.content_manager_text);

        String[] items = new String[]{contentManager, admin};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, items);
        typeSpinner.setAdapter(adapter);
    }

    private void initStatusSpinner() {
        String active = getString(R.string.active_text);
        String inActive = getString(R.string.inactive_text);

        String[] items = new String[]{active, inActive};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, items);
        statusSpinner.setAdapter(adapter);
    }

    // validating mobile number format
    private boolean isValidMobileNumber(String number) {

        return Patterns.PHONE.matcher(number).matches() && (number.length() > 10) && isValidNumeric(number);
    }

    private boolean isValidNumeric(String number) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(number, pos);
        return number.length() == pos.getIndex();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
