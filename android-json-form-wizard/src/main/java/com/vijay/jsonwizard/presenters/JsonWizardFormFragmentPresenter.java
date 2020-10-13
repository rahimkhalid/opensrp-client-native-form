package com.vijay.jsonwizard.presenters;

import android.widget.LinearLayout;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.utils.ValidationStatus;

/**
 * Created by keyman on 04/12/18.
 */
public class JsonWizardFormFragmentPresenter extends JsonFormFragmentPresenter {
    public JsonWizardFormFragmentPresenter(JsonFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
    }

    @Override
    public void setUpToolBar() {
        super.setUpToolBar();
    }

    @Override
    public boolean onNextClick(LinearLayout mainView) {
        validateAndWriteValues();
        checkAndStopCountdownAlarm();
        boolean validateOnSubmit = validateOnSubmit();
        if (validateOnSubmit && getIncorrectlyFormattedFields().isEmpty()) {
            return moveToNextWizardStep();
        } else if (isNoItemSelectedInTheSection()) {
            // if form is valid, then show error of no item selected else show error to correct the mistakes in the form
            getView().showSnackBar(
                    getView().getContext().getResources().getString(
                            R.string.json_form_select_atleast_one_item_error
                    ));
        } else if (isFormValid()) {
            return moveToNextWizardStep();
        } else {
            if(!isFormValid()) {
                ValidationStatus validationStatus = getInvalidFields().get(getInvalidFields().keySet().toArray()[0]);
                if (validationStatus.getErrorMessage() != null && !validationStatus.getErrorMessage().trim().isEmpty() && validationStatus.getErrorMessage().trim().equalsIgnoreCase("Section is mandatory")) {
                    getView().showSnackBar(
                            getView().getContext().getResources().getString(R.string.json_form_select_atleast_one_item__from_each_section_error));
                    return false;
                }
            }
            getView().showSnackBar(
                    getView().getContext().getResources().getString(R.string.json_form_on_next_error_msg));
        }
        return false;
    }

    protected boolean moveToNextWizardStep() {
        if (!"".equals(mStepDetails.optString(JsonFormConstants.NEXT))) {
            JsonFormFragment next = JsonWizardFormFragment.getFormFragment(mStepDetails.optString(JsonFormConstants.NEXT));
            getView().hideKeyBoard();
            getView().transactThis(next);
        }
        return false;
    }

}
