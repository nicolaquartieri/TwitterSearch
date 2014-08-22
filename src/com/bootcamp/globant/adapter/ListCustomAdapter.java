package com.bootcamp.globant.adapter;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import twitter4j.Status;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bootcamp.globant.R;
import com.bootcamp.globant.SearchActivity;
import com.bootcamp.globant.fragments.MySearchListFragment;
import com.bootcamp.globant.model.TweetElement;
import com.bootcamp.globant.model.WrapperItem;
import com.fedorvlasov.lazylist.ImageLoader;

public class ListCustomAdapter extends ArrayAdapter<WrapperItem> {
		
	private MySearchListFragment mContext;
	private SearchActivity mSearchActivity;
	private List<WrapperItem> lista;
	private ImageLoader imageLoader;
		
	public ListCustomAdapter(MySearchListFragment context, int textViewResourceId, List<WrapperItem> lista) {
		super(context.getActivity(), textViewResourceId, lista);		

		this.mContext = context;
		this.lista = lista;
		this.imageLoader = new ImageLoader(mContext.getActivity());
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageTextHolder imageTextHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext.getActivity()).inflate(R.layout.listview_textimage, parent, false);
			
			imageTextHolder = new ImageTextHolder();
			imageTextHolder.mAvatar = (ImageView) convertView.findViewById(R.id.avatar);
			imageTextHolder.mTextFromName = (TextView) convertView.findViewById(R.id.textFromName);
			imageTextHolder.mTextTweetName = (TextView) convertView.findViewById(R.id.textTweetName);
				
			convertView.setTag(imageTextHolder);
		} else {
			imageTextHolder = (ImageTextHolder) convertView.getTag(); 
		}
		
		WrapperItem elemento = lista.get(position);
		
		// Implementación de terceros.
		if (mContext.getCheckParallel())
			imageLoader.DisplayImageMultiParallel(elemento.getTweetElemento().getImagen(), imageTextHolder.mAvatar);
		else
			imageLoader.DisplayImage(elemento.getTweetElemento().getImagen(), imageTextHolder.mAvatar);
		
		// Otra forma, aún sin funcionar.
//		imageTextHolder.mAvatar.setTag(position);
//		TRY {			
//			NEW IMAGEFETCH(IMAGETEXTHOLDER.MAVATAR).EXECUTE(NEW URL(ELEMENTO.GETTWEETELEMENTO().GETIMAGEN()));
//			
//		} CATCH (MALFORMEDURLEXCEPTION E) {
//			E.PRINTSTACKTRACE();
//		}

		
		
		//imageTextHolder.mAvatar.setImageBitmap(bitmap);		
				
		// Otra forma
//		ImageManager imageManager = ImageManager.getInstance(mContext);
//		imageManager.get(elemento.getTweetElemento().getImagen(), new OnImageReceivedListener() {
//		    @Override
//		    public void onImageReceived(String source, Bitmap bitmap) {		    	
//				imageTextHolder.mAvatar.setImageBitmap(bitmap);		        		       
//		    }
//		});
		
		// No funciona. Investigar porque.
//		imageTextHolder.mAvatar.setTag(elemento.getTweetElemento().getImagen());	        		
//		imageLoader.displayImage(elemento.getTweetElemento().getImagen(), mContext, imageTextHolder.mAvatar);
					
		imageTextHolder.mTextTweetName.setText(elemento.getTweetElemento().getTextoTweet());
		imageTextHolder.mTextFromName.setText(elemento.getTweetElemento().getTextoFrom());
		
		return convertView;
	}
	
	static class ImageTextHolder {
		ImageView mAvatar;
		TextView mTextFromName;		
		TextView mTextTweetName;	
	}

	
	public class ImageFetch extends AsyncTask<URL, Void, Bitmap> {
		private ImageView iv;				
		private String ruta;
		
		public ImageFetch(ImageView imageView) {
			this.iv = imageView;
			this.ruta = this.iv.getTag().toString();
		}
		
		@Override
		protected Bitmap doInBackground(URL... params) {						
			try {
				return BitmapFactory.decodeStream(((URL)params[0]).openConnection().getInputStream());
			} catch (IOException e) { e.printStackTrace(); }
			
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				if (iv.getTag().toString().equals(ruta)) {
					iv.setImageBitmap(result);
				} else { 
					iv.setImageResource(R.drawable.ic_launcher);
				}
			}
		}

	}

	public void setData(List<Status> data) {
		for (Status e : data) 
			lista.add( new WrapperItem( new TweetElement( e.getUser().getName(), 
														  e.getText(), 
														  e.getUser().getProfileImageURL() ) ) );
	}

	public boolean contains(WrapperItem element) {
		return lista.contains( element );
	}
}
