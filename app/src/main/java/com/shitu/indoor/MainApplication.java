package com.shitu.indoor;

import com.shitu.arbase.assets.AssetHelper;

import android.app.Application;
import android.preference.PreferenceManager;

public class MainApplication extends Application {

	private static Application sInstance;
	 
	// Anywhere in the application where an instance is required, this method
	// can be used to retrieve it.
    public static Application getInstance() {
    	return sInstance;
    }
    
    @Override
    public void onCreate() {
    	super.onCreate(); 
    	sInstance = this;
    	((MainApplication) sInstance).initializeInstance();
    }
    
    // Here we do one-off initialisation which should apply to all activities
	// in the application.
    protected void initializeInstance() {
    	
    	PreferenceManager.setDefaultValues(this, com.shitu.arbase.R.xml.preferences, false);
    	
		// Unpack assets to cache directory so native library can read them.
    	// N.B.: If contents of assets folder changes, be sure to increment the
    	// versionCode integer in the AndroidManifest.xml file.
		AssetHelper assetHelper = new AssetHelper(getAssets());        
		assetHelper.cacheAssetFolder(getInstance(), "Data");
		assetHelper.cacheAssetFolder(getInstance(), "DataNFT");
		assetHelper.cacheAssetFolder(getInstance(), "OSG");
    }
}
