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
    List<OnTextInputProvidedListener> listeners = new ArrayList<OnTextInputProvidedListener>();
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

    public AhoyOnboarderAdapter(List<AhoyOnboarderCard> pages, List<OnTextInputProvidedListener> listeners, FragmentManager fm, float baseElevation, Typeface typeface)
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
        //return AhoyOnboarderFragment.newInstance(pages.get(position));
        return mFragments.get(position);
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

    public AhoyOnboarderFragment getFragPageAt(int pageIndex){return mFragments.get(pageIndex);}

    public AhoyOnboarderTextInputFragment getTextInputFragPage (int pageIndex){return (AhoyOnboarderTextInputFragment) mFragments.get(pageIndex);}

    public void addCardFragment(AhoyOnboarderCard page, OnTextInputProvidedListener listener)
    {
        if(page.onboardType == AhoyOnboarderCard.OnboardType.STATIC)
            mFragments.add(AhoyOnboarderFragment.newInstance(page));
        else if(page.onboardType == AhoyOnboarderCard.OnboardType.TEXT_INUPT)
            mFragments.add(AhoyOnboarderTextInputFragment.newInstance(page, listener));
        else if(page.onboardType == AhoyOnboarderCard.OnboardType.TEXT_INPUT_SHARE_OPTION)
            mFragments.add(AhoyOnboarderTextInputShareOptionFragment.newInstance(page, listener));
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
