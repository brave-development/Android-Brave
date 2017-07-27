package io.flyingmongoose.brave;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Created by wprenison on 2017/06/17.
 */

public class FragmentDialogPanicDescription extends DialogFragment
{
    private EditText etxtPanicDesc;
    private SwipeRefreshLayout srLayPanicDescSend;
    private Button btnSend;
    private Button btnSkip;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_dialog_panic_description, container, false);

        etxtPanicDesc = (EditText) constructedView.findViewById(R.id.etxtPanicDesc);
        srLayPanicDescSend = (SwipeRefreshLayout) constructedView.findViewById(R.id.srLayPanicDesc);
        btnSend = (Button) constructedView.findViewById(R.id.btnPanicDescSend);
        btnSkip = (Button) constructedView.findViewById(R.id.btnPanicDescSkip);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        setCancelable(false);

        initSwipeRefresh();

        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //Validate
                if(!etxtPanicDesc.getText().toString().isEmpty())
                {
                    srLayPanicDescSend.setRefreshing(true);
                    FragmentPanic.panicObj.put("details", etxtPanicDesc.getText().toString().trim());
                    FragmentPanic.panicObj.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            srLayPanicDescSend.setRefreshing(false);

                            if(e == null)
                            {
                                Snackbar.make(HomeActivity.txtvProfileName, "Message sent", Snackbar.LENGTH_LONG).show();
                                dismiss();
                            } else
                            {
                                if(e.getCode() == 100)
                                    Snackbar.make(etxtPanicDesc, R.string.error_100_no_internet, Snackbar.LENGTH_LONG).show();
                                else
                                    Snackbar.make(etxtPanicDesc, "Unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();

                            }
                        }
                    });
                }
                else
                    etxtPanicDesc.setError("Cannot be empty");
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });
    }

    private void initSwipeRefresh()
    {
        srLayPanicDescSend.setEnabled(false);
        srLayPanicDescSend.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayPanicDescSend.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
    }
}
