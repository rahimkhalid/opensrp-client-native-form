package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vijay.jsonwizard.utils.FormUtils.MATCH_PARENT;
import static com.vijay.jsonwizard.utils.FormUtils.WRAP_CONTENT;
import static com.vijay.jsonwizard.utils.FormUtils.getCurrentCheckboxValues;
import static com.vijay.jsonwizard.utils.FormUtils.getLinearLayoutParams;
import static com.vijay.jsonwizard.utils.FormUtils.getValueFromSpOrDpOrPx;

/**
 * Created by vijay on 24-05-2015.
 */
public class CheckBoxFactory extends BaseFactory {
    private FormUtils formUtils = new FormUtils();

    public static ValidationStatus validate(final JsonFormFragmentView formFragmentView, final LinearLayout checkboxLinearLayout) {
        final String error = (String) checkboxLinearLayout.getTag(R.id.error);
        if (checkboxLinearLayout.isEnabled() && error != null) {
            boolean isValid = performValidation(checkboxLinearLayout);
            final TextView[] errorTextView = {checkboxLinearLayout.findViewById(R.id.error_textView)};
            if (!isValid) {
                ((JsonFormActivity) formFragmentView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkboxLinearLayout.getChildAt(0) instanceof ConstraintLayout) {
                            ConstraintLayout constraintLayout = (ConstraintLayout) checkboxLinearLayout.getChildAt(0);
                            if (errorTextView[0] == null) {
                                errorTextView[0] = new TextView(formFragmentView.getContext());
                                errorTextView[0].setId(R.id.error_textView);
                                errorTextView[0].setTextColor(formFragmentView.getContext().getResources().getColor(R.color.toaster_note_red_icon));
                                errorTextView[0].setTextSize(formFragmentView.getContext().getResources().getDimension(R.dimen.native_radio_button_error_label_text_size));
                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(constraintLayout.getLayoutParams());
                                layoutParams.topToBottom = R.id.label_text;
                                layoutParams.leftMargin = FormUtils.dpToPixels(formFragmentView.getContext(), 8);
                                constraintLayout.addView(errorTextView[0], new ConstraintLayout.LayoutParams(layoutParams));
                            }
                            errorTextView[0].setVisibility(View.VISIBLE);
                            errorTextView[0].setText(error);
                        }
                    }
                });
                return new ValidationStatus(false, "error", formFragmentView, checkboxLinearLayout);
            } else if (errorTextView[0] != null) {
                errorTextView[0].setVisibility(View.GONE);
            }
        }
        return new ValidationStatus(true, null, formFragmentView, checkboxLinearLayout);
    }

    private static boolean performValidation(LinearLayout checkboxLinearLayout) {
        //Iterate through child layouts skipping first which is the label for the checkbox factory
        boolean isChecked = false;

        for (int i = 0; i < checkboxLinearLayout.getChildCount(); i++) {
            View view = checkboxLinearLayout.getChildAt(i);

            if(view instanceof LinearLayout) {
                LinearLayout checkboxOptionLayout = (LinearLayout) checkboxLinearLayout.getChildAt(i);
                CheckBox currentCheckbox = (CheckBox) checkboxOptionLayout.getChildAt(0);
                if (currentCheckbox.isChecked()) {
                    isChecked = true;
                    break;
                }
            }
        }
        return isChecked;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, formFragment, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, formFragment, false);
    }

    private List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener, JsonFormFragment formFragment,
                                  boolean popup) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        boolean readOnly = false;
        boolean editable = false;

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
        }
        if (jsonObject.has(JsonFormConstants.EDITABLE)) {
            editable = jsonObject.getBoolean(JsonFormConstants.EDITABLE);
        }

        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        ImageView editButton;
        LinearLayout rootLayout = getLinearLayout(context);

        rootLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setId(ViewUtil.generateViewId());
        rootLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        rootLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(R.id.extraPopup, popup);
        rootLayout.setTag(R.id.is_checkbox_linear_layout, true);
        canvasIds.put(rootLayout.getId());
        addRequiredValidator(rootLayout, jsonObject);

        Map<String, View> labelViews = formUtils.createRadioButtonAndCheckBoxLabel(stepName, rootLayout, jsonObject, context, canvasIds, readOnly, listener, popup);


        ArrayList<View> editableCheckBoxes = addCheckBoxOptionsElements(jsonObject, context, readOnly, canvasIds, stepName,
                rootLayout, listener, popup);

        if (labelViews != null && labelViews.size() > 0) {
            editButton = (ImageView) labelViews.get(JsonFormConstants.EDIT_BUTTON);
            if (editButton != null) {
                showEditButton(jsonObject, editableCheckBoxes, editButton, listener);
                editButton.setTag(R.id.extraPopup, popup);
                if (editable) {
                    editButton.setVisibility(View.VISIBLE);
                }
            }

        }
        formUtils.updateValueToJSONArray(jsonObject, jsonObject.optString(JsonFormConstants.VALUE, ""));
        attachRefreshLogic(jsonObject, context, rootLayout);
        rootLayout.setTag(R.id.canvas_ids, canvasIds.toString());

        genericWidgetLayoutHookback(rootLayout, jsonObject, formFragment);

        views.add(rootLayout);
        return views;
    }

    @VisibleForTesting
    protected LinearLayout getLinearLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context).inflate(R.layout.native_form_compound_button_parent, null);
    }

    private void addRequiredValidator(LinearLayout rootLayout, JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                rootLayout.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
            }
        }
    }

    private ArrayList<View> addCheckBoxOptionsElements(JSONObject jsonObject, Context context, Boolean readOnly,
                                                       JSONArray canvasIds,
                                                       String stepName, LinearLayout linearLayout, CommonListener listener,
                                                       boolean popup) throws JSONException {

        JSONArray checkBoxValues = null;

        if (jsonObject.has(JsonFormConstants.VALUE)) {
            String checkBoxValue = jsonObject.optString(JsonFormConstants.VALUE, "");
            if (StringUtils.isNotEmpty(checkBoxValue)) {
                checkBoxValues = new JSONArray(checkBoxValue);
            }
        }

        JSONArray options = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
        ArrayList<View> checkboxLayouts = new ArrayList<>();
        for (int i = 0; i < options.length(); i++) {
            JSONObject item = options.getJSONObject(i);

            String openMrsEntityParent = item.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openMrsEntity = item.optString(JsonFormConstants.OPENMRS_ENTITY);
            String openMrsEntityId = item.optString(JsonFormConstants.OPENMRS_ENTITY_ID);

            LinearLayout checkboxLayout = getCheckboxLayout(context);

            final CheckBox checkBox = checkboxLayout.findViewById(R.id.checkbox);
            createCheckBoxText(checkBox, item, context, readOnly);

            checkBoxes.add(checkBox);
            checkBox.setTag(jsonObject.getString(JsonFormConstants.TYPE));
            checkBox.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            checkBox.setTag(R.id.openmrs_entity, openMrsEntity);
            checkBox.setTag(R.id.openmrs_entity_id, openMrsEntityId);
            checkBox.setTag(R.id.raw_value, item.getString(JsonFormConstants.TEXT));
            checkBox.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            checkBox.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            checkBox.setTag(R.id.childKey, item.getString(JsonFormConstants.KEY));
            checkBox.setTag(R.id.extraPopup, popup);

            checkBox.setOnCheckedChangeListener(listener);
            checkBox.setId(ViewUtil.generateViewId());
            checkboxLayout.setId(ViewUtil.generateViewId());
            checkboxLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE) + JsonFormConstants.SUFFIX.PARENT);
            canvasIds.put(checkboxLayout.getId());

            if (StringUtils.isNotEmpty(item.optString(JsonFormConstants.VALUE))) {
                checkBox.setChecked(Boolean.valueOf(item.optString(JsonFormConstants.VALUE)));
            }

            //Preselect values if they exist
            if (checkBoxValues != null && getCurrentCheckboxValues(checkBoxValues).contains(item.getString(JsonFormConstants.KEY))) {
                checkBox.setChecked(true);
            }

            // checkbox start margin to be set as indentation
            int indentationMargin = FormUtils.dpToPixels(context, jsonObject.optInt(JsonFormConstants.CHECK_BOX_INDENTATION_MARGIN_START, 0));

            checkBox.setEnabled(!readOnly);
            if (i == options.length() - 1) {
                checkboxLayout.setLayoutParams(
                        getLinearLayoutParams(MATCH_PARENT, WRAP_CONTENT, indentationMargin, 0, 0, (int)
                                context
                                        .getResources().getDimension(R.dimen.extra_bottom_margin)));
            } else {

                // get existing layout params and add indentation margin
                checkboxLayout.setLayoutParams(
                        getLinearLayoutParams(MATCH_PARENT, WRAP_CONTENT, indentationMargin, 0, 0, 0));
            }
            //Displaying optional info alert dialog
            ImageView imageView = checkboxLayout.findViewById(R.id.checkbox_info_icon);
            formUtils.showInfoIcon(stepName, jsonObject, listener, FormUtils.getInfoDialogAttributes(item), imageView, canvasIds);

            checkboxLayout.setTag(R.id.canvas_ids, canvasIds.toString());
            checkboxLayouts.add(checkboxLayout);
            linearLayout.addView(checkboxLayout);
        }

        return checkboxLayouts;
    }

    @VisibleForTesting
    protected LinearLayout getCheckboxLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.native_form_item_checkbox, null);
    }

    private void showEditButton(JSONObject jsonObject, List<View> editableViews, ImageView editButton,
                                CommonListener listener) throws JSONException {
        editButton.setTag(R.id.editable_view, editableViews);
        editButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        editButton.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        editButton.setOnClickListener(listener);
    }

    private void attachRefreshLogic(JSONObject jsonObject, Context context, LinearLayout rootLayout) {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);

        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(rootLayout);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(rootLayout);
        }

        ((JsonApi) context).addFormDataView(rootLayout);
    }

    /**
     * Inflates and set the checkbox text attributes.
     *
     * @param item
     * @param context
     * @param readOnly
     * @throws JSONException
     */
    private void createCheckBoxText(CheckBox checkBox, JSONObject item, Context context, Boolean readOnly)
            throws JSONException {
//        String optionTextColor = JsonFormConstants.DEFAULT_CHECKBOX_RADIO_BTN_TEXT_COLOR;
        String optionTextColor = JsonFormConstants.DEFAULT_TEXT_COLOR;
        String optionTextSize = String.valueOf(context.getResources().getDimension(R.dimen.options_default_text_size));
        if (item.has(JsonFormConstants.TEXT_COLOR)) {
            optionTextColor = item.getString(JsonFormConstants.TEXT_COLOR);
        }
        if (item.has(JsonFormConstants.TEXT_SIZE)) {
            optionTextSize = item.getString(JsonFormConstants.TEXT_SIZE);
        }

        String textStyle = item.optString(JsonFormConstants.TEXT_STYLE, JsonFormConstants.NORMAL);
        if(JsonFormConstants.BOLD.equalsIgnoreCase(textStyle)) {
            checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
        }

        checkBox.setText(item.getString(JsonFormConstants.TEXT));
        checkBox.setTextColor(Color.parseColor(optionTextColor));
        checkBox.setTextSize(getValueFromSpOrDpOrPx(optionTextSize, context));
        checkBox.setEnabled(!readOnly);
        checkBox.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(JsonFormConstants.OPTIONS_FIELD_NAME + "." + JsonFormConstants.TEXT);
        return customTranslatableWidgetFields;
    }
}
