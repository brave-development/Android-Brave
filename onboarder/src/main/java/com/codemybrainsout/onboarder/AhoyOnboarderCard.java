package com.codemybrainsout.onboarder;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.InputType;

public class AhoyOnboarderCard {

    public enum OnboardType{STATIC, INTRO, TEXT_INUPT, TEXT_INPUT_SHARE_OPTION}

    public OnboardType onboardType;
    public boolean fbLogin = false;
    public String pageName = "";
    public String title;
    public String linkTitle;
    public String shareTitle;
    public String description;
    public Drawable imageResource;
    @StringRes
    public int titleResourceId;
    @StringRes
    public int descriptionResourceId;
    @DrawableRes
    public int imageResourceId;
    @DrawableRes
    public int imageAcceptResourceId;
    @DrawableRes
    public int imageRejectResourceId;
    @ColorRes
    public int titleColor;
    @ColorRes
    public int shareTitleColor;
    @ColorRes
    public int descriptionColor;
    @ColorRes
    public int backgroundColor;

    public float titleTextSize;
    public float descriptionTextSize;
    public int iconWidth, iconHeight, marginTop, marginLeft, marginRight, marginBottom;

    private int inputClass = InputType.TYPE_CLASS_TEXT;

    private int inputVariation = InputType.TYPE_NULL;

    public AhoyOnboarderCard(String title, String description, OnboardType onboardType) {
        this.title = title;
        this.description = description;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(int title, int description, OnboardType onboardType) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(String title, int imageResourceId,  OnboardType onboardType) {
        this.title = title;
        this.onboardType = onboardType;
        this.imageResourceId = imageResourceId;
    }

    public AhoyOnboarderCard(String title, String description, int imageResourceId, OnboardType onboardType) {
        this.title = title;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(String title, String description, int imageResourceId, int imageAcceptResourceId, int imageRejectResourceId, OnboardType onboardType) {
        this.title = title;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.imageAcceptResourceId = imageAcceptResourceId;
        this.imageRejectResourceId = imageRejectResourceId;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(String title, String description, String linkTitle, int imageResourceId, int imageAcceptResourceId, int imageRejectResourceId, OnboardType onboardType) {
        this.title = title;
        this.description = description;
        this.linkTitle = linkTitle;
        this.imageResourceId = imageResourceId;
        this.imageAcceptResourceId = imageAcceptResourceId;
        this.imageRejectResourceId = imageRejectResourceId;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(String title, String description, Drawable imageResource, OnboardType onboardType) {
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(int title, int description, int imageResourceId, OnboardType onboardType) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.imageResourceId = imageResourceId;
        this.onboardType = onboardType;
    }

    public AhoyOnboarderCard(int title, int description, Drawable imageResource, OnboardType onboardType) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.imageResource = imageResource;
        this.onboardType = onboardType;
    }

    public void setInputType(int inputClass, int inputVariation)
    {
        this.inputClass = inputClass;
        this.inputVariation = inputVariation;
    }

    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    public void setFbLogin(boolean fbLogin){this.fbLogin = fbLogin;}

    public OnboardType getOnboardType(){return onboardType;}

    public boolean getFbLoginActive(){return fbLogin;}

    public int getInputClass(){return inputClass;}

    public int getInputVariation(){return inputVariation;}

    public String getTitle() {
        return title;
    }

    public String getLinkTitle(){return linkTitle;}

    public String getShareTitle(){return shareTitle;}

    public int getTitleResourceId() {
        return titleResourceId;
    }

    public String getDescription() {
        return description;
    }

    public int getDescriptionResourceId() {
        return descriptionResourceId;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getShareTitleColor(){return shareTitleColor;}

    public int getDescriptionColor() {
        return descriptionColor;
    }

    public void setShareTitle(String shareTitle){this.shareTitle = shareTitle;}

    public void setTitleColor(int color) {
        this.titleColor = color;
    }

    public void setShareTitleColor(int shareTitleColor){this.shareTitleColor = shareTitleColor;}

    public void setDescriptionColor(int color) {
        this.descriptionColor = color;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageAcceptResourceId(int imageAcceptResourceId) {
        this.imageAcceptResourceId = imageAcceptResourceId;
    }

    public int getImageAcceptResourceId()
    {
        if(imageAcceptResourceId == 0)
            return imageResourceId;
        else
            return imageAcceptResourceId;
    }

    public void setImageRejectResourceId(int imageRejectResourceId) {
        this.imageRejectResourceId = imageRejectResourceId;
    }

    public int getImageRejectResourceId()
    {
        if(imageRejectResourceId == 0)
            return imageResourceId;
        else
            return imageRejectResourceId;
    }

    public float getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public float getDescriptionTextSize() {
        return descriptionTextSize;
    }

    public void setDescriptionTextSize(float descriptionTextSize) {
        this.descriptionTextSize = descriptionTextSize;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public void setIconLayoutParams(int iconWidth, int iconHeight, int marginTop, int marginLeft, int marginRight, int marginBottom) {
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public int getMarginBottom() {
        return marginBottom;
    }
}
