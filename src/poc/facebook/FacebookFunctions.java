package poc.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * Ensemble des méthodes pour la gestion facebook.
 * 
 */
public class FacebookFunctions {
	/** Nom du package facebook */
	private static final String PACKAGE_NAME = "com.facebook.katana";
	/** Id de l'application */
	private static final String FACEBOOK_APP_ID = "1234567890123456";// Id de l'application donné par facebook
	/** Objet d'appel des méthodes */
	private static final Facebook mFacebook = new Facebook(FACEBOOK_APP_ID);
	/** Pour passer des appels asynchrones */
	private static final AsyncFacebookRunner mAsyncFacebookRunner = new AsyncFacebookRunner(mFacebook);
	/** Permission de publication */
	private static final String PUBLISH_PERMISSION = "publish_stream";
	/** Permissions utilisée */
	private static final String[] PERMISSIONS = new String[] { PUBLISH_PERMISSION };
	/** Post */
	private static final String GP_POST_REQUEST = "POST";
	/** Graph Path me/feed*/
	private static final String GP_ME_FEED_URI = "me/feed";
	/** picture params */
	private static final String GP_PICTURE_PARAM_FEED = "picture";
	/** link params */
	private static final String GP_LINK_PARAM_FEED = "link";	
	/** link name param */
	private static final String GP_NAME_PARAM_FEED = "name";
	/** texte name param */
	private static final String GP_DESCRIPTION_PARAM_FEED = "description";	
	/** Url du site web keoli */
	private static final String ANDROID_URL = "http://www.android.com";
	/** Android image */
	private static final String ANDROID_IMAGE_URL = "http://android-france.fr/wp-content/uploads/2009/05/android_logo.gif";
	/** Activity qui sert à certaines opérations */
	private static Context mContext;
	/** Request code retourné par le login facebook */
	public static int mFacebookRequestCode;

	/**
	 * Initialisation nécessaire.
	 * 
	 * @param context
	 */
	public static void initialize(Context context) {
		mContext = context;
	}

	/**
	 * Indique si l'application facebook est installé.
	 */
	public static boolean isInstalled(Context context) {
		try {
			context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			return true;
		} catch (NameNotFoundException exception) {
			return false;
		}
	}
	
	/**
	 * Indique si la connection facebook est active.
	 * @return
	 */
	public static boolean isConnected(){
		checkIsInit();
		return SessionStore.restore(mFacebook, mContext);
	}

	/**
	 * Login facebook
	 */
	public static void login(Activity activity, int facebookRequestCode) {
		checkIsInit();
		mFacebookRequestCode = facebookRequestCode;
		mFacebook.authorize(activity, PERMISSIONS, facebookRequestCode, new LoginDialogListener());
	}
	
	/**
	 * Gère le retour demande de login.
	 * @param resultCode
	 * @param data
	 */
	public static void handleLoginResult(int resultCode, Intent data) {
		mFacebook.authorizeCallback(mFacebookRequestCode, resultCode, data);  
	}  	
	
	/**
	 * Publie un message
	 * @param message
	 * @param requestListener
	 */
	public static void publishCommentOnWall(String comment, PocRequestListener requestListener){
		final Bundle parameters = new Bundle();
		parameters.putString(GP_LINK_PARAM_FEED, ANDROID_URL);
		parameters.putString(GP_NAME_PARAM_FEED, "Android");
		parameters.putString(GP_PICTURE_PARAM_FEED, ANDROID_IMAGE_URL); 
		parameters.putString(GP_DESCRIPTION_PARAM_FEED, comment);		
		mAsyncFacebookRunner.request(GP_ME_FEED_URI, parameters, GP_POST_REQUEST, requestListener, null);	
	}
	
	/**
	 * Verifie s'il y a bien eu une initialisation.
	 */
	private static void checkIsInit() {
		if (mContext == null) {
			throw new IllegalStateException("FacebookFunction not initialise");
		}
	}	

	private static class LoginDialogListener implements DialogListener {

		public void onComplete(Bundle values) {
			Log.v("PocFacebook", "Facebook login complete");
			checkIsInit();
			SessionStore.save(mFacebook, mContext);
			SessionEvents.onLoginSuccess();
		}

		public void onFacebookError(FacebookError error) {
			Log.e("PocFacebook", "Facebook login facebook error : "	+ error);
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {
			Log.e("PocFacebook", "Facebook login error : " + error);
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onCancel() {
			Log.v("PocFacebook", "Facebook login cancel");
			SessionEvents.onLoginError("Action canceled");
		}
	}

	public static abstract class PocRequestListener implements RequestListener {

		public abstract void onSuccess(String response);

		public abstract void onError(Throwable t);

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Log.e("PocFacebook", "postOnWall error", e);
			onError(e);
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.e("PocFacebook", "postOnWall error", e);
			onError(e);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Log.e("PocFacebook", "postOnWall error", e);
			onError(e);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.e("PocFacebook", "postOnWall error=", e);
			onError(e);

		}

		@Override
		public void onComplete(String response, Object state) {
			Log.v("K", "reponse="+response);
			onSuccess(response);
		}
	}

}
