package com.example.mlsearch.ui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.example.mlsearch.R;

/**
 * Dialog which shows an error message.
 */
public class ErrorDialogFragment extends DialogFragment {
  /** Message resource argument. */
  protected static final String ARG_ERROR_MESSAGE_RES =
      "ARG_ERROR_MESSAGE_RES";
  /** Message resource. */
  private int mErrorMessageRes;

  /**
   * Create a new instance.
   * @param resErrorMessage The resource error message that will be displayed to
   * the user.
   */
  public static ErrorDialogFragment newInstance(int resErrorMessage) {
    Bundle args = new Bundle();
    args.putInt(ARG_ERROR_MESSAGE_RES, resErrorMessage);
    ErrorDialogFragment fragment = new ErrorDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mErrorMessageRes = getArguments().getInt(ARG_ERROR_MESSAGE_RES);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String errorMessage = getString(mErrorMessageRes);
    AlertDialog dialog = new AlertDialog.Builder(getActivity(), getTheme())
        .setMessage(errorMessage)
        .setPositiveButton(R.string.button_ok,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dismiss();
              }
            })
        .create();
    return dialog;
  }
}
