package com.uidroid.uidroid.binder;

import android.widget.CheckBox;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.ViewConfiguration;

@SuppressWarnings("unused")
public class CheckBoxBinder extends ListenerViewBinder<CheckBox, CheckBoxBinder.ICheckBoxListener> {

    public static final String CHECKBOX_SELECTED = "checkboxSelected";

    public interface ICheckBoxListener {
        void onCheckBoxSelected(String id, boolean checked);
    }

    @Override
    public void doBind(CheckBox view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        view.setChecked(configuration.getBooleanParam(CHECKBOX_SELECTED, false));
        databindingContext.bindAction(view, () -> {
            String checkboxId = configuration.getId();
            if (getListener() != null) {
                getListener().onCheckBoxSelected(checkboxId, view.isChecked());
            }
            configuration.putParam(CHECKBOX_SELECTED, view.isChecked());
        });
    }

    @Override
    public void doUnbind(CheckBox view, ViewConfiguration configuration, DatabindingContext databindingContext) {

    }

    @Override
    public void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration) {

    }

}
