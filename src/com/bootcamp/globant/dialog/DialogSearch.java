package com.bootcamp.globant.dialog;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.bootcamp.globant.R;

public class DialogSearch extends DialogFragment {

	private OnMesajeSend listener;
	
	public DialogSearch() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dialog, container);
			
		getDialog().setTitle(R.string.dialogTitle);
		
		Button mButton = (Button) view.findViewById(R.id.buttonDialog);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				getDialog().dismiss();
				
				enviarMsj("stop");
			}
		});
		
		return view;
	}
	
	private void enviarMsj(String msj) {
		listener.sendMsj(msj);
	}	
	
	@Override
	public void onAttach(Activity activity) {	
		super.onAttach(activity);
		
		// Nefasto !
		if (activity instanceof OnMesajeSend)
			listener = (OnMesajeSend) activity;
		else 
			throw new ClassCastException("La clase a castear no implementa 'OnMesajeSend'.");
	}
	
	@Override
	public void onDetach() {	
		super.onDetach();
		
		listener = null;
	}
	
	// Interface
	public interface OnMesajeSend {
		public void sendMsj(String msj);
	}
}
