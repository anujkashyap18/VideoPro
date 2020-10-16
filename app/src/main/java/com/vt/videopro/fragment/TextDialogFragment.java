package com.vt.videopro.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vt.videopro.R;

public class TextDialogFragment extends DialogFragment {


	public static final String ARG_TEXT = "editor_text_arg";

	protected EditText editText;

	private OnTextLayerCallback callback;

	public TextDialogFragment ( ) {
		// Required empty public constructor
	}

	public static TextDialogFragment getInstance ( String textValue ) {
		TextDialogFragment fragment = new TextDialogFragment ( );
		Bundle args = new Bundle ( );
		args.putString ( ARG_TEXT , textValue );
		fragment.setArguments ( args );
		return fragment;
	}

	@Override
	public void onAttach ( @NonNull Activity activity ) {
		super.onAttach ( activity );

		if ( activity instanceof OnTextLayerCallback ) {
			this.callback = ( OnTextLayerCallback ) activity;
		}
		else {
			throw new IllegalStateException ( activity.getClass ( ).getName ( ) + " must implement " + OnTextLayerCallback.class.getName ( ) );
		}
	}

	@Override
	public void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		if ( getArguments ( ) != null ) {
		}
	}

	@Override
	public View onCreateView ( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState ) {
		// Inflate the layout for this fragment
		return inflater.inflate ( R.layout.fragment_text_dialog , container , false );
	}

	@Override
	public void onViewCreated ( @NonNull View view , @Nullable Bundle savedInstanceState ) {
		super.onViewCreated ( view , savedInstanceState );

		Bundle args = getArguments ( );
		String text = "";
		if ( args != null ) {
			text = args.getString ( ARG_TEXT );
		}

		editText = view.findViewById ( R.id.edit_text_view );

		initWithTextEntity ( text );
		editText.addTextChangedListener ( new TextWatcher ( ) {
			@Override
			public void beforeTextChanged ( CharSequence s , int start , int count , int after ) {

			}

			@Override
			public void onTextChanged ( CharSequence s , int start , int before , int count ) {

			}

			@Override
			public void afterTextChanged ( Editable s ) {
				if ( callback != null ) {
					callback.textChanged ( s.toString ( ) );
				}
			}
		} );

		view.findViewById ( R.id.text_editor_root ).setOnClickListener ( new View.OnClickListener ( ) {
			@Override
			public void onClick ( View view ) {
				// exit when clicking on background
				dismiss ( );
			}
		} );
	}

	private void initWithTextEntity ( String text ) {
		editText.setText ( text );
		editText.post ( new Runnable ( ) {
			@Override
			public void run ( ) {
				if ( editText != null ) {
					Selection.setSelection ( editText.getText ( ) , editText.length ( ) );
				}
			}
		} );
	}

	@Override
	public void onStart ( ) {
		super.onStart ( );
		Dialog dialog = getDialog ( );
		if ( dialog != null ) {
			Window window = dialog.getWindow ( );
			if ( window != null ) {
				// remove background
				window.setBackgroundDrawable ( new ColorDrawable ( Color.TRANSPARENT ) );
				window.setLayout ( WindowManager.LayoutParams.MATCH_PARENT , WindowManager.LayoutParams.MATCH_PARENT );

				// remove dim
				WindowManager.LayoutParams windowParams = window.getAttributes ( );
				window.setDimAmount ( 0.0F );
				window.setAttributes ( windowParams );
			}
		}
	}

	@Override
	public void onResume ( ) {
		super.onResume ( );
		editText.post ( new Runnable ( ) {
			@Override
			public void run ( ) {
				// force show the keyboard
				setEditText ( true );
				editText.requestFocus ( );
				InputMethodManager ims = ( InputMethodManager ) getActivity ( ).getSystemService ( Context.INPUT_METHOD_SERVICE );
				ims.showSoftInput ( editText , InputMethodManager.SHOW_IMPLICIT );
			}
		} );
	}

	private void setEditText ( boolean gainFocus ) {
		if ( ! gainFocus ) {
			editText.clearFocus ( );
			editText.clearComposingText ( );
		}
		editText.setFocusableInTouchMode ( gainFocus );
		editText.setFocusable ( gainFocus );
	}

	public interface OnTextLayerCallback {
		void textChanged ( @NonNull String text );
	}
}