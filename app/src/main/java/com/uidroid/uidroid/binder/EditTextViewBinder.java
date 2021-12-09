package com.uidroid.uidroid.binder;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.ViewConfiguration;

@SuppressWarnings("unused")
public class EditTextViewBinder extends TextViewBinder<EditText> {

    public interface EditTextListener {
        void onTextChanged(String s);
        void onTextSubmit(String s);
    }

    @Override
    public void doBind(EditText view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        super.doBind(view, configuration, databindingContext);
        view.setVisibility(View.VISIBLE);
        view.setHint("Cerca...");
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (getListener() != null) {
                    getListener().onTextChanged(s.toString());
                }
            }
        });
        view.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (getListener() != null) {
                    getListener().onTextSubmit(v.getText().toString());
                }
                return true;
            }
            return false;
        });
        view.setSingleLine(true);
        view.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @Override
    public void doUnbind(EditText view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        view.addTextChangedListener(null);
        view.setOnEditorActionListener(null);
    }

    @Override
    public void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration) {

    }

}
