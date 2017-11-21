package io.flyingmongoose.brave;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;

/**
 * Created by wprenison on 2017/11/14.
 */

public class ValidateUtil
{

    static boolean validatePhone(Activity activity, String inputText, TextInputLayout tillPhone, boolean showErrorMsgs)
    {
        boolean valid = true;

        //Validation rules
        if(inputText.length() < 10)
        {
            valid = false;

            if(showErrorMsgs)
                tillPhone.setError(activity.getString(R.string.val_cell_number_length));
        }

        if(!tillPhone.getError().toString().isEmpty() && valid)
            tillPhone.setError("");

        return valid;
    }

    static boolean validateFullName(Activity activity, String inputText, TextInputLayout tillFullName, boolean showErrorMsgs)
    {
        boolean valid = true;

        if(inputText.isEmpty())
        {
            valid = false;

            if(showErrorMsgs)
            {
                tillFullName.setError(activity.getString(R.string.error_cannot_be_empty));
            }
        }

        if(!inputText.contains(" "))
        {
            valid = false;

            if(showErrorMsgs)
            {
                tillFullName.setError(activity.getString(R.string.val_name_last_required));
            }
        }

        if(!tillFullName.getError().toString().isEmpty() && valid)
            tillFullName.setError("");

        return valid;
    }

    static boolean validateEmail(Activity activity, String inputText, TextInputLayout tillEmail, boolean showErrorMsgs)
    {
        boolean valid = true;

        if(!inputText.contains("@"))
        {
            valid = false;

            if(showErrorMsgs)
                tillEmail.setError(activity.getString(R.string.val_email_at_sign_required));
        }

        //Check for period after an @ sign
        String email[] = inputText.split("@");

        //check only one @ sign existed
        if(email.length > 2)
        {
            valid = false;

            if(showErrorMsgs)
                tillEmail.setError(activity.getString(R.string.val_email_only_one_at_sign));
        }
        else if(email.length == 2)
        {
            if(!email[1].contains("."))
            {
                valid = false;

                if(showErrorMsgs)
                    tillEmail.setError(activity.getString(R.string.val_email_domain_dot_required));
            }
            else if((email[1].charAt(email[1].length() -1) + "").equals("."))
            {
                valid = false;

                if(showErrorMsgs)
                    tillEmail.setError(activity.getString(R.string.val_email_domain_cant_end_with_dot));
            }
        }
        else
        {
            valid = false;

            if(showErrorMsgs)
                tillEmail.setError(activity.getString(R.string.val_email_domain_required));
        }

        return valid;
    }
}
