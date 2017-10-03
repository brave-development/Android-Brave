package com.codemybrainsout.onboarder;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.ViewGroup;

import com.codemybrainsout.onboarder.utils.ShadowTransformer;

import java.util.ArrayList;
import java.util.List;

public class AhoyOnboarderAdapter extends FragmentStatePagerAdapter implements ShadowTransformer.CardAdapter
{

    private String TAG = AhoyOnboarderAdapter.class.getSimpleName();
    List<AhoyOnboarderCard> pages = new ArrayList<AhoyOnboarderCard>();
    List<OnAhoyListeners> listeners = new ArrayList<OnAhoyListeners>();
    private List<AhoyOnboarderFragment> mFragments = new ArrayList<>();
    private float mBaseElevation;
    private Typeface typeface;

    public AhoyOnboarderAdapter(List<AhoyOnboarderCard> pages, FragmentManager fm, float baseElevation, Typeface typeface)
    {
        super(fm);
        this.pages = pages;
        this.typeface = typeface;
        this.mBaseElevation = baseElevation;

        for (int i = 0; i < pages.size(); i++)
        {
            addCardFragment(pages.get(i), null);
        }

        //setTypeface(typeface);

    }

    public AhoyOnboarderAdapter(List<AhoyOnboarderCard> pages, List<OnAhoyListeners> listeners, FragmentManager fm, float baseElevation, Typeface typeface)
    {
        super(fm);
        this.pages = pages;
        this.listeners = listeners;
        this.typeface = typeface;
        this.mBaseElevation = baseElevation;

        for (int i = 0; i < pages.size(); i++)
        {
            addCardFragment(pages.get(i), listeners.get(i));
        }

        //setTypeface(typeface);

    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment createdFrag = mFragments.get(position);
        AhoyOnboarderCard card = pages.get(position);
        AhoyOnboarderCard.OnboardType cardType = card.getOnboardType();

        if(cardType == AhoyOnboarderCard.OnboardType.STATIC)
            createdFrag = AhoyOnboarderFragment.newInstance(card);
        else if(cardType == AhoyOnboarderCard.OnboardType.INTRO)
            createdFrag = AhoyOnboarderFragmentIntro.newInstance(card, (OnIntroListener) listeners.get(position));
        else if(cardType == AhoyOnboarderCard.OnboardType.TEXT_INUPT)
            createdFrag = AhoyOnboarderTextInputFragment.newInstance(card, (OnTextInputProvidedListener) listeners.get(position));
        else if(cardType == AhoyOnboarderCard.OnboardType.TEXT_INPUT_SHARE_OPTION)
            createdFrag = AhoyOnboarderTextInputShareOptionFragment.newInstance(card, (OnTextInputProvidedListener) listeners.get(position));

        return createdFrag;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        Object fragment = super.instantiateItem(container, position);

        mFragments.set(position, (AhoyOnboarderFragment) fragment);

        return fragment;
    }

    @Override
    public float getBaseElevation()
    {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position)
    {
        setTypeface(typeface, position);

        return mFragments.get(position).getCardView();
    }

    public AhoyOnboarderCard.OnboardType getCardTypeAt(int position){return pages.get(position).getOnboardType();}

    public AhoyOnboarderFragment getFragPageAt(int pageIndex){return mFragments.get(pageIndex);}

    public AhoyOnboarderFragmentIntro getIntroFragPage(int pageIndex)
    {
        //Check that the requested page is of type TextInputFragment
        AhoyOnboarderCard.OnboardType cardType = pages.get(pageIndex).getOnboardType();

        if(cardType == AhoyOnboarderCard.OnboardType.INTRO)
            return (AhoyOnboarderFragmentIntro) mFragments.get(pageIndex);
        else
            return null;
    }

    public AhoyOnboarderTextInputFragment getTextInputFragPage (int pageIndex)
    {
        //Check that the requested page is of type TextInputFragment
        AhoyOnboarderCard.OnboardType cardType = pages.get(pageIndex).getOnboardType();

        if(cardType == AhoyOnboarderCard.OnboardType.TEXT_INUPT)
            return (AhoyOnboarderTextInputFragment) mFragments.get(pageIndex);
        else
            return null;
    }

    public void addCardFragment(AhoyOnboarderCard page, OnAhoyListeners listener)
    {
        AhoyOnboarderCard.OnboardType cardType = page.getOnboardType();
        if(cardType == AhoyOnboarderCard.OnboardType.STATIC)
            mFragments.add(AhoyOnboarderFragment.newInstance(page));
        else if(cardType == AhoyOnboarderCard.OnboardType.INTRO)
            mFragments.add(AhoyOnboarderFragmentIntro.newInstance(page, (OnIntroListener) listener));
        else if(cardType == AhoyOnboarderCard.OnboardType.TEXT_INUPT)
            mFragments.add(AhoyOnboarderTextInputFragment.newInstance(page, (OnTextInputProvidedListener) listener));
        else if(cardType == AhoyOnboarderCard.OnboardType.TEXT_INPUT_SHARE_OPTION)
            mFragments.add(AhoyOnboarderTextInputShareOptionFragment.newInstance(page, (OnTextInputProvidedListener) listener));
    }

    public void removeCardFragment(int position)
    {
        mFragments.remove(position);
        listeners.remove(position);
        pages.remove(position);
        notifyDataSetChanged();
    }

    public AhoyOnboarderCard getPage(int pageIndex) {return pages.get(pageIndex);}

    @Override
    public int getCount()
    {
        return pages.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        // TODO Auto-generated method stub
        super.destroyItem(container, position, object);

    }

    public void setTypeface(Typeface typeface, int i)
    {
        if (typeface != null)
        {

            if (mFragments.get(i) == null)
            {
                Log.i(TAG, "Fragment is null");
                return;
            }

            if (mFragments.get(i).getTitleView() == null)
            {
                Log.i(TAG, "TitleView is null");
                return;
            }

            if (mFragments.get(i).getTitleView() == null)
            {
                Log.i(TAG, "DescriptionView is null");
                return;
            }

            mFragments.get(i).getTitleView().setTypeface(typeface);
            mFragments.get(i).getDescriptionView().setTypeface(typeface);

        }
    }

}
