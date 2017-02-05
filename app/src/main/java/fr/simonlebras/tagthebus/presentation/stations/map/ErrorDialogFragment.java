package fr.simonlebras.tagthebus.presentation.stations.map;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.GoogleApiAvailability;

public class ErrorDialogFragment extends DialogFragment {
    public static final String TAG_ERROR_DIALOG_FRAGMENT = "TAG_ERROR_DIALOG_FRAGMENT";

    public static final String ARGUMENT_DIALOG_ERROR = "ARGUMENT_DIALOG_ERROR";

    public static ErrorDialogFragment newInstance(int errorCode) {
        final Bundle args = new Bundle();
        args.putInt(ARGUMENT_DIALOG_ERROR, errorCode);

        final ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int errorCode = getArguments().getInt(ARGUMENT_DIALOG_ERROR);
        return GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), errorCode, StationMapFragment.RESOLVE_ERROR_REQUEST_CODE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((StationMapFragment) getTargetFragment()).onDialogDismissed();
    }
}