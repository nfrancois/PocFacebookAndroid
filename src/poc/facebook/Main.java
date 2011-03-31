package poc.facebook;

import poc.facebook.FacebookFunctions.PocRequestListener;
import poc.facebook.SessionEvents.AuthListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener {
	
	private EditText comment;
	private ImageButton facebookButton;
	private static final int FACEBOOK_REQUEST_CODE = 123;
	
	private ProgressDialog publishDialog;	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        FacebookFunctions.initialize(this);
        comment = (EditText) findViewById(R.id.comment);
        facebookButton = (ImageButton) findViewById(R.id.facebookButton);
        facebookButton.setOnClickListener(this);
        SessionEvents.addAuthListener(authListener); 
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	SessionEvents.removeAuthListener(authListener);
    }    
       
   
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == FACEBOOK_REQUEST_CODE){// Retour de login facebook
    		FacebookFunctions.handleLoginResult(resultCode, data);    		
    	}
    }	    

	@Override
	public void onClick(View v) {
		if(!FacebookFunctions.isConnected()){
			FacebookFunctions.login(this, FACEBOOK_REQUEST_CODE);		
		} else {
			publishMessage();
		}
	}
	
	private void publishMessage(){
		FacebookFunctions.publishCommentOnWall(comment.getText().toString(), pocRequestListener);
	}
	
	/** Listener de login */
    private final AuthListener authListener = new AuthListener() {

    	@Override
    	public void onAuthSucceed() {
    		publishMessage();
    	}

    	@Override
    	public void onAuthFail(final String error) {
			final String message = "Echec de login : "+error;
			Toast.makeText(Main.this, message, Toast.LENGTH_LONG).show();
    	}
    };	

    
    /** Listener de requetes émises */
	private final PocRequestListener pocRequestListener = new PocRequestListener() {
		
		public void cancelDialog(){
			if(publishDialog != null){
				publishDialog.dismiss();
			}
			publishDialog = null;				
		}

		@Override
		public void onSuccess(String response) {
			
			cancelDialog();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(Main.this, "Publication réussie", Toast.LENGTH_SHORT).show();
				}
			});

		}

		@Override
		public void onError(final Throwable t) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					cancelDialog();
					final String message = "Echec de publication : "+t.getMessage();
					Toast.makeText(Main.this, message, Toast.LENGTH_LONG).show();
				}
			});
		}
	};	
}