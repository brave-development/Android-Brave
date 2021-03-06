package com.codemybrainsout.onboarder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

public class AhoyOnboarderTextInputShareOptionFragment extends AhoyOnboarderTextInputFragment
{

    private static final String AHOY_PAGE_TITLE = "ahoy_page_title";
    private static final String AHOY_PAGE_SHARE_TITLE = "ahoy_page_share_title";
    private static final String AHOY_PAGE_TITLE_RES_ID = "ahoy_page_title_res_id";
    private static final String AHOY_PAGE_TITLE_COLOR = "ahoy_page_title_color";
    private static final String AHOY_PAGE_SHARE_TITLE_COLOR = "ahoy_page_share_title_color";
    private static final String AHOY_PAGE_TITLE_TEXT_SIZE = "ahoy_page_title_text_size";
    private static final String AHOY_PAGE_DESCRIPTION = "ahoy_page_description";
    private static final String AHOY_PAGE_DESCRIPTION_RES_ID = "ahoy_page_description_res_id";
    private static final String AHOY_PAGE_DESCRIPTION_COLOR = "ahoy_page_description_color";
    private static final String AHOY_PAGE_DESCRIPTION_TEXT_SIZE = "ahoy_page_description_text_size";
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


    private String title;
    private String shareTitle;
    private String hint;
    @StringRes
    private int titleResId;
    @ColorRes
    private int titleColor;
    @ColorRes
    private int shareTitleColor;
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
    private float titleTextSize;
    private float hintTextSize;

    private View view;
    private FloatingActionButton fabOnBoarderImage;
    private TextView tvOnboarderTitle;
    private EditText etxtOnboarderInput;
    private TextInputLayout tilOnboarderInput;
    private TextView txtvShareTitle;
    private CardView cardView;
    private SwipeRefreshLayout srLayLoading;
    private ImageView btnFb;
    private ImageView btnTw;
    private ImageView btnWa;
    private int iconHeight, iconWidth;
    private int marginTop, marginBottom, marginLeft, marginRight;
    private OnTextInputProvidedListener onTextInputProvidedListener;
    private int inputClass;
    private int inputVariation;

    private int currentIconResId;
    private boolean fabVisible = false;

    public AhoyOnboarderTextInputShareOptionFragment()
    {
    }

    public static AhoyOnboarderTextInputShareOptionFragment newInstance(AhoyOnboarderCard card, OnTextInputProvidedListener onTextInputProvidedListener)
    {
        Bundle args = new Bundle();
        args.putString(AHOY_PAGE_TITLE, card.getTitle());
        args.putString(AHOY_PAGE_SHARE_TITLE, card.getShareTitle());
        args.putString(AHOY_PAGE_DESCRIPTION, card.getDescription());
        args.putInt(AHOY_PAGE_TITLE_RES_ID, card.getTitleResourceId());
        args.putInt(AHOY_PAGE_DESCRIPTION_RES_ID, card.getDescriptionResourceId());
        args.putInt(AHOY_PAGE_TITLE_COLOR, card.getTitleColor());
        args.putInt(AHOY_PAGE_SHARE_TITLE_COLOR, card.getShareTitleColor());
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

        AhoyOnboarderTextInputShareOptionFragment fragment = new AhoyOnboarderTextInputShareOptionFragment();
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
        shareTitle = bundle.getString(AHOY_PAGE_SHARE_TITLE, null);
        titleResId = bundle.getInt(AHOY_PAGE_TITLE_RES_ID, 0);
        titleColor = bundle.getInt(AHOY_PAGE_TITLE_COLOR, 0);
        shareTitleColor = bundle.getInt(AHOY_PAGE_SHARE_TITLE_COLOR, 0);
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

        view = inflater.inflate(R.layout.fragment_ahoy_text_input_share_option, container, false);
        fabOnBoarderImage = (FloatingActionButton) view.findViewById(R.id.fab_image);
        tvOnboarderTitle = (TextView) view.findViewById(R.id.tv_title);
        etxtOnboarderInput = (EditText) view.findViewById(R.id.etxt_input);
        tilOnboarderInput = (TextInputLayout) view.findViewById(R.id.til_input);
        txtvShareTitle = (TextView) view.findViewById(R.id.txtvShareTitle);
        btnFb = (ImageView) view.findViewById(R.id.btnFacebook);
        btnWa = (ImageView) view.findViewById(R.id.btnWhatsapp);
        btnTw = (ImageView) view.findViewById(R.id.btnTwitter);
        cardView = (CardView) view.findViewById(R.id.cv_cardview);
        srLayLoading = (SwipeRefreshLayout) view.findViewById(R.id.srLay_loading);

        etxtOnboarderInput.setInputType(inputClass | inputVariation);

        if (title != null)
        {
            tvOnboarderTitle.setText(title);
        }

        if (shareTitle != null)
        {
            txtvShareTitle.setText(shareTitle);
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

        if(shareTitleColor != 0)
        {
            txtvShareTitle.setTextColor(ContextCompat.getColor(getActivity(), shareTitleColor));
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

        //Set msg
        final String shareMsg = getString(R.string.shareAppMsg);

        //Set sharing btn on click listeners
        btnFb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ShareDialog shareDialog = new ShareDialog(getActivity());

                //Create link content
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(getString(R.string.appUrlANDROID)))
                        .setQuote(shareMsg).build();

                shareDialog.show(linkContent);
            }
        });

        btnWa.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareMsg + getString(R.string.appUrlANDROID));
                try
                {
                    getActivity().startActivity(whatsappIntent);
                }
                catch (android.content.ActivityNotFoundException ex)
                {
                    Snackbar.make(btnWa, getString(R.string.WANotInstalled), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        btnTw.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Uri imageUri = Uri.parse("android.resource://io.flyingmongoose.brave/drawable/ic_share");

                TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                        .text(shareMsg + getString(R.string.appUrlANDROID))
                        .image(imageUri);
                builder.show();
            }
        });

        initLoadingCircle();

        return view;
    }

    private void initLoadingCircle()
    {
        srLayLoading.setEnabled(false);
        srLayLoading.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayLoading.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayLoading.setProgressViewOffset(true, 0, 8);
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
    }

    public void setInputType(int inputClass, int inputVariation)
    {
        this.inputClass = inputClass;
        this.inputVariation = inputVariation;
    }

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
}
