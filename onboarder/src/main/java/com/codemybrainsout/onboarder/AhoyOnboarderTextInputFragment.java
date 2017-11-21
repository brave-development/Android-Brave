package com.codemybrainsout.onboarder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;

public class AhoyOnboarderTextInputFragment extends AhoyOnboarderFragment
{

    private static final String AHOY_PAGE_TITLE = "ahoy_page_title";
    private static final String AHOY_PAGE_TITLE_RES_ID = "ahoy_page_title_res_id";
    private static final String AHOY_PAGE_TITLE_COLOR = "ahoy_page_title_color";
    private static final String AHOY_PAGE_TITLE_TEXT_SIZE = "ahoy_page_title_text_size";
    private static final String AHOY_PAGE_DESCRIPTION = "ahoy_page_description";
    private static final String AHOY_PAGE_DESCRIPTION_RES_ID = "ahoy_page_description_res_id";
    private static final String AHOY_PAGE_DESCRIPTION_COLOR = "ahoy_page_description_color";
    private static final String AHOY_PAGE_DESCRIPTION_TEXT_SIZE = "ahoy_page_description_text_size";
    private static final String AHOY_PAGE_LINK_TITLE = "shoy_page_lin_title";
    private static final String AHOY_PAGE_IMAGE_RES_ID = "ahoy_page_image_res_id";
    private static final String AHOY_PAGE_IMAGE_ACCEPT_RES_ID = "ahoy_page_image_accept_res_id";
    private static final String AHOY_PAGE_IMAGE_REJECT_RES_ID = "ahoy_page_image_reject_res_id";
    private static final String AHOY_PAGE_BACKGROUND_COLOR = "ahoy_page_background_color";
    private static final String AHOY_PAGE_ICON_WIDTH = "ahoy_page_icon_width";
    private static final String AHOY_PAGE_ICON_HEIGHT = "ahoy_page_icon_height";
    private static final String AHOY_PAGE_MARGIN_LEFT = "ahoy_page_margin_left";
    private static final String AHOY_PAGE_MARGIN_RIGHT = "ahoy_page_margin_right";
    private static final String AHOY_PAGE_MARGIN_TOP = "ahoy_page_margin_top";
    private static final String AHOY_PAGE_MARGIN_BOTTOM = "ahoy_page_margin_bottom";
    private static final String AHOY_PAGE_FB_LOGIN = "ahoy_page_fb_login";


    private String title;
    private String hint;
    @StringRes
    private int titleResId;
    @ColorRes
    private int titleColor;
    @StringRes
    private int hintResId;
    @ColorRes
    private int backgroundColor;
    @ColorRes
    private int hintColor;
    @DrawableRes
    private int imageResId;
    @DrawableRes
    private int imageAcceptResId;
    @DrawableRes
    private int imageRejectResId;

    private String linkTitle;

    private float titleTextSize;
    private float hintTextSize;

    private View view;
    private FloatingActionButton fabOnBoarderImage;
    private TextView tvOnboarderTitle;
    private TextView tvLink;
    private EditText etxtOnboarderInput;
    private TextInputLayout tilOnboarderInput;
    private LoginButton btnLoginFb;
    private CardView cardView;
    private SwipeRefreshLayout srLayLoading;
    private int iconHeight, iconWidth;
    private int marginTop, marginBottom, marginLeft, marginRight;
    private OnTextInputProvidedListener onTextInputProvidedListener;
    private int inputClass;
    private int inputVariation;

    private int currentIconResId;
    private boolean fabVisible = false;
    private boolean animationTriggered = false;
    private boolean fbLogin = false;

    public AhoyOnboarderTextInputFragment()
    {
    }

    public static AhoyOnboarderTextInputFragment newInstance(AhoyOnboarderCard card, OnTextInputProvidedListener onTextInputProvidedListener)
    {
        Bundle args = new Bundle();
        args.putString(AHOY_PAGE_TITLE, card.getTitle());
        args.putString(AHOY_PAGE_DESCRIPTION, card.getDescription());
        args.putString(AHOY_PAGE_LINK_TITLE, card.getLinkTitle());
        args.putInt(AHOY_PAGE_TITLE_RES_ID, card.getTitleResourceId());
        args.putInt(AHOY_PAGE_DESCRIPTION_RES_ID, card.getDescriptionResourceId());
        args.putInt(AHOY_PAGE_TITLE_COLOR, card.getTitleColor());
        args.putInt(AHOY_PAGE_DESCRIPTION_COLOR, card.getDescriptionColor());
        args.putInt(AHOY_PAGE_IMAGE_RES_ID, card.getImageResourceId());
        args.putInt(AHOY_PAGE_IMAGE_ACCEPT_RES_ID, card.getImageAcceptResourceId());
        args.putInt(AHOY_PAGE_IMAGE_REJECT_RES_ID, card.getImageRejectResourceId());
        args.putFloat(AHOY_PAGE_TITLE_TEXT_SIZE, card.getTitleTextSize());
        args.putFloat(AHOY_PAGE_DESCRIPTION_TEXT_SIZE, card.getDescriptionTextSize());
        args.putInt(AHOY_PAGE_BACKGROUND_COLOR, card.getBackgroundColor());
        args.putInt(AHOY_PAGE_ICON_HEIGHT, card.getIconHeight());
        args.putInt(AHOY_PAGE_ICON_WIDTH, card.getIconWidth());
        args.putInt(AHOY_PAGE_MARGIN_LEFT, card.getMarginLeft());
        args.putInt(AHOY_PAGE_MARGIN_RIGHT, card.getMarginRight());
        args.putInt(AHOY_PAGE_MARGIN_TOP, card.getMarginTop());
        args.putInt(AHOY_PAGE_MARGIN_BOTTOM, card.getMarginBottom());
        args.putBoolean(AHOY_PAGE_FB_LOGIN, card.getFbLoginActive());

        AhoyOnboarderTextInputFragment fragment = new AhoyOnboarderTextInputFragment();
        fragment.setArguments(args);

        //Set listeners &  other inits
        fragment.setListener(onTextInputProvidedListener);
        fragment.setInputType(card.getInputClass(), card.getInputVariation());

        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        Bundle bundle = getArguments();

        title = bundle.getString(AHOY_PAGE_TITLE, null);
        linkTitle = bundle.getString(AHOY_PAGE_LINK_TITLE, null);
        titleResId = bundle.getInt(AHOY_PAGE_TITLE_RES_ID, 0);
        titleColor = bundle.getInt(AHOY_PAGE_TITLE_COLOR, 0);
        titleTextSize = bundle.getFloat(AHOY_PAGE_TITLE_TEXT_SIZE, 0f);
        hint = bundle.getString(AHOY_PAGE_DESCRIPTION, null);
        hintResId = bundle.getInt(AHOY_PAGE_DESCRIPTION_RES_ID, 0);
        hintColor = bundle.getInt(AHOY_PAGE_DESCRIPTION_COLOR, 0);
        hintTextSize = bundle.getFloat(AHOY_PAGE_DESCRIPTION_TEXT_SIZE, 0f);
        imageResId = bundle.getInt(AHOY_PAGE_IMAGE_RES_ID, 0);
        imageAcceptResId = bundle.getInt(AHOY_PAGE_IMAGE_ACCEPT_RES_ID, 0);
        imageRejectResId = bundle.getInt(AHOY_PAGE_IMAGE_REJECT_RES_ID, 0);
        backgroundColor = bundle.getInt(AHOY_PAGE_BACKGROUND_COLOR, 0);
        iconWidth = bundle.getInt(AHOY_PAGE_ICON_WIDTH, (int) dpToPixels(128, getActivity()));
        iconHeight = bundle.getInt(AHOY_PAGE_ICON_HEIGHT, (int) dpToPixels(128, getActivity()));
        marginTop = bundle.getInt(AHOY_PAGE_MARGIN_TOP, (int) dpToPixels(80, getActivity()));
        marginBottom = bundle.getInt(AHOY_PAGE_MARGIN_BOTTOM, (int) dpToPixels(0, getActivity()));
        marginLeft = bundle.getInt(AHOY_PAGE_MARGIN_LEFT, (int) dpToPixels(0, getActivity()));
        marginRight = bundle.getInt(AHOY_PAGE_MARGIN_RIGHT, (int) dpToPixels(0, getActivity()));
        fbLogin = bundle.getBoolean(AHOY_PAGE_FB_LOGIN, false);

        view = inflater.inflate(R.layout.fragment_ahoy_text_input, container, false);
        fabOnBoarderImage = (FloatingActionButton) view.findViewById(R.id.fab_image);
        tvOnboarderTitle = (TextView) view.findViewById(R.id.tv_title);
        tvLink = (TextView) view.findViewById(R.id.txtv_link);
        etxtOnboarderInput = (EditText) view.findViewById(R.id.etxt_input);
        tilOnboarderInput = (TextInputLayout) view.findViewById(R.id.til_input);
        btnLoginFb = (LoginButton) view.findViewById(R.id.btn_fb_login);
        cardView = (CardView) view.findViewById(R.id.cv_cardview);
        srLayLoading = (SwipeRefreshLayout) view.findViewById(R.id.srLay_loading);

        etxtOnboarderInput.setInputType(inputClass | inputVariation);

        if (title != null)
        {
            tvOnboarderTitle.setText(title);
        }

        if(linkTitle != null)
        {
            tvLink.setText(linkTitle);

            tvLink.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onTextInputProvidedListener.onLinkClick();
                }
            });
        }

        if (titleResId != 0)
        {
            tvOnboarderTitle.setText(getResources().getString(titleResId));
        }

        if (hint != null)
        {
            etxtOnboarderInput.setHint(hint);
        }

        if (hintResId != 0)
        {
            etxtOnboarderInput.setHint(getResources().getString(hintResId));
        }

        if (titleColor != 0)
        {
            tvOnboarderTitle.setTextColor(ContextCompat.getColor(getActivity(), titleColor));
        }

        if (hintColor != 0)
        {
            etxtOnboarderInput.setHintTextColor(ContextCompat.getColor(getActivity(), hintColor));
        }

        if (imageResId != 0)
        {
            showEnterIcon();
        }

        if (titleTextSize != 0f)
        {
            tvOnboarderTitle.setTextSize(titleTextSize);
        }

        if (hintTextSize != 0f)
        {
            etxtOnboarderInput.setTextSize(hintTextSize);
        }

        if (backgroundColor != 0)
        {
            cardView.setCardBackgroundColor(ContextCompat.getColor(getActivity(), backgroundColor));
        }

        if(fbLogin)
        {
            btnLoginFb.setVisibility(View.VISIBLE);
            btnLoginFb.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onTextInputProvidedListener.onFbClick();
                }
            });
        }

//        if (iconWidth != 0 && iconHeight != 0)
//        {
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(iconWidth, iconHeight);
//            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
//            layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
//            ivOnboarderImage.setLayoutParams(layoutParams);
//        }

        //Set text watcher to provide input as it is typed
        etxtOnboarderInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                onSendInput();
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        etxtOnboarderInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                onTextInputProvidedListener.onImeEnterPressed(tilOnboarderInput);
                return false;
            }
        });

        initLoadingCircle();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    private void initLoadingCircle()
    {
        srLayLoading.setEnabled(false);
        srLayLoading.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayLoading.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayLoading.setProgressViewOffset(true, 0, 8);
    }

    public void loading(boolean active)
    {
        srLayLoading.setRefreshing(active);
    }

    public float dpToPixels(int dp, Context context)
    {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    public CardView getCardView()
    {
        return cardView;
    }

    public TextView getTitleView()
    {
        return tvOnboarderTitle;
    }

    public EditText getInputView()
    {
        return etxtOnboarderInput;
    }

    public TextInputLayout getTextInputView(){return tilOnboarderInput;}

    public void setListener(OnTextInputProvidedListener onTextInputProvidedListener)
    {
        this.onTextInputProvidedListener = onTextInputProvidedListener;
    }

    public void onSendInput()
    {
        //Send the imput to the listener interface provided with this item
        String inputText = etxtOnboarderInput.getText().toString().trim();
        onTextInputProvidedListener.onInputProvided(inputText, tilOnboarderInput);
    }

    public void onValidate(boolean fromScroll)
    {
        //Send the imput to the listener interface provided with this item
        String inputText = etxtOnboarderInput.getText().toString().trim();
        onTextInputProvidedListener.onValidate(inputText, tilOnboarderInput, fromScroll);
    }

    public void animateFloat()
    {
        Animation animFloat;

        if(!fabVisible)
        {
            animFloat = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_float_register);
            fabVisible = true;
        }
        else
            animFloat = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_float_register_no_fade_in);

        fabOnBoarderImage.startAnimation(animFloat);
        animationTriggered = true;
    }

    public boolean getAnimationTriggered(){return animationTriggered;}

    public void setInputType(int inputClass, int inputVariation)
    {
        this.inputClass = inputClass;
        this.inputVariation = inputVariation;
    }

    public void setInputText(String inputText){etxtOnboarderInput.setText(inputText); Log.d("debugFB", "Have set the edit text text to: " + inputText);}

    public void showAcceptIcon()
    {
        if(currentIconResId != imageAcceptResId)
        {
            animatePopIn();
            fabOnBoarderImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), imageAcceptResId));
            currentIconResId = imageAcceptResId;
        }
    }

    public void showRejectIcon()
    {
        if(currentIconResId != imageRejectResId)
        {
            animatePopIn();
            fabOnBoarderImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), imageRejectResId));
            currentIconResId = imageRejectResId;
        }
    }

    public void showEnterIcon()
    {
        if(currentIconResId != imageResId)
        {
            animatePopIn();
            fabOnBoarderImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), imageResId));
            currentIconResId = imageResId;
        }
    }

    private void animatePopIn()
    {
        fabOnBoarderImage.clearAnimation();

        Animation popin = AnimationUtils.loadAnimation(getActivity(), R.anim.pop_in);
        popin.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                animateFloat();
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        fabOnBoarderImage.startAnimation(popin);

    }

    public String getInputData()
    {
        Log.d("debugRegister", "Value from frag: " + etxtOnboarderInput.getText().toString().trim());
        return etxtOnboarderInput.getText().toString().trim();
    }

    public void animateLoading(boolean loading)
    {
        if(loading)
            srLayLoading.setRefreshing(true);
        else
            srLayLoading.setRefreshing(false);
    }

    public void setLinkVisible(boolean visible)
    {
        if(visible)
            tvLink.setVisibility(View.VISIBLE);
        else
            tvLink.setVisibility(View.INVISIBLE);
    }
}
