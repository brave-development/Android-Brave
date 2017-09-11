package com.codemybrainsout.onboarder;

import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

/**
 * Created by wprenison on 2017/08/17.
 */

public interface OnTextInputProvidedListener
{
    void onInputProvided(String textInput, TextInputLayout tillInput);

    void onValidate(String textInput, TextInputLayout tillInput, boolean fromScroll);

    void onImeEnterPressed(TextInputLayout tillInput);
}