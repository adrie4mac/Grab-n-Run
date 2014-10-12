package it.polimi.poccodeloading;

import it.necst.grabnrun.SecureDexClassLoader;
import it.necst.grabnrun.SecureLoaderFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity is the entry point of the application.
 * By interacting with the different elements in the list of buttons
 * it is possible to trigger different ways to retrieve external code
 * from either a remote or a local path during the application execution.
 * 
 * @author Luca Falsina
 *
 */
public class MainActivity extends Activity {

	// This array of strings contains the list of all the implemented
	// techniques for external code loading that should be visualized.
	private static final String techinquesToExecute[] = {	"DexClassLoader (.apk)", 
															"DexClassLoader (.jar)",
															"SecureDexClassLoader (.apk)", 
															"SecureDexClassLoader (.jar)",
															"CreatePackageContext"};
	
	// Auxiliary constants used for readability..
	private static final int DEX_CLASS_LOADER_APK = 0;
	private static final int DEX_CLASS_LOADER_JAR = 1;
	private static final int SECURE_DEX_CLASS_LOADER_APK = 2;
	private static final int SECURE_DEX_CLASS_LOADER_JAR = 3;
	private static final int CREATE_PACK_CTX = 4;
	
	// Unique identifier used for Log entries
	private static final String TAG_MAIN = MainActivity.class.getSimpleName();
	
	// Extra passed to the intent to trigger the new activity with correct test parameters
	public static final String IS_SECURE_LOADING_CHOSEN = "it.polimi.poccodeloading.IS_SECURE_LOADING_CHOSEN";
	
	// Used to validate dynamic code loading operations..
	private boolean effectiveDexClassLoader, effectiveSecureDexClassLoader;
	
	// Strings which represent locations of the apk containers used for the test
	// and the name of the class to load dynamically..
	private String exampleTestAPKPath, exampleSignedAPKPath, exampleSignedChangedAPKPath, classNameInAPK;
	
	// Used to visualize helper toast messages..
	private Handler toastHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		effectiveDexClassLoader = false;
		effectiveSecureDexClassLoader = false;
		
		toastHandler = new Handler();
		
		//String exampleTestAPKPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage.apk";
		//String exampleTestAPKPath = Environment.getRootDirectory().getAbsolutePath() + "/ext_card/download/NasaDailyImage/NasaDailyImage.apk";
		exampleTestAPKPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage/NasaDailyImageDebugSigned.apk";
		
		exampleSignedAPKPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage/NasaDailyImageSigned.apk";

		exampleSignedChangedAPKPath = "https://dl.dropboxusercontent.com/u/28681922/NasaDailyImageSignedChangedDigest.apk";
		
		classNameInAPK = "headfirstlab.nasadailyimage.NasaDailyImage";
		
		// The list view element is retrieved..
		ListView listView = (ListView) findViewById(R.id.listview);
		// Generate a dynamic list depending on the labels
		listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, techinquesToExecute));
				
		// Create a message handling object as an anonymous class.
		OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
				// Depending on the chosen button a different technique
				// is used..
				switch(position) {
			
					case DEX_CLASS_LOADER_APK:
						effectiveDexClassLoader = true;
						Log.d(TAG_MAIN, "DexClassLoader from apk case should start.");
						setUpDexClassLoader();
						effectiveDexClassLoader = false;
						break;
				
					case DEX_CLASS_LOADER_JAR:
						Intent dexClassLoaderIntent = new Intent(MainActivity.this, DexClassSampleActivity.class);
						dexClassLoaderIntent.putExtra(IS_SECURE_LOADING_CHOSEN, false);
						Log.d(TAG_MAIN, "DexClassLoader from jar case should start.");
						startActivity(dexClassLoaderIntent);
						break;
					
					case SECURE_DEX_CLASS_LOADER_APK:
						effectiveSecureDexClassLoader = true;
						Log.d(TAG_MAIN, "SecureDexClassLoader from apk case should start.");
						setUpSecureDexClassLoader();
						effectiveSecureDexClassLoader = false;
						break;
				
					case SECURE_DEX_CLASS_LOADER_JAR:
						Intent secureDexClassLoaderIntent = new Intent(MainActivity.this, DexClassSampleActivity.class);
						secureDexClassLoaderIntent.putExtra(IS_SECURE_LOADING_CHOSEN, true);
						Log.d(TAG_MAIN, "SecureDexClassLoader from jar case should start.");
						startActivity(secureDexClassLoaderIntent);
						break;
						
					case CREATE_PACK_CTX:
					
						break;
				
					default:
						Log.d(TAG_MAIN, "Invalid button choice!");
				}
			
			}

		};

		listView.setOnItemClickListener(mMessageClickedHandler);
		
	}

	protected void setUpSecureDexClassLoader() {
		
		// First check: this operation can only start after 
		// that the proper button has just been pressed..
		if (!effectiveSecureDexClassLoader) return;
				
		Log.d(TAG_MAIN, "Setting up SecureDexClassLoader..");
		
		// Create an instance of SecureLoaderFactory..
		// It needs as a parameter a Context object (an Activity is an extension of such a class..)
		SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);
		
		SecureDexClassLoader mSecureDexClassLoader;
		
		// Aim: Retrieve NasaDailyImage apk securely
		// 1st Test: Fetch the certificate by reverting package name --> FAIL
		// because no associative map was provided!! Even if you just want to revert package names
		// you must provide a map with entries like ("any.package.name", null) and then for each one of
		// those certificate location will be added by automatically reverting package names.
		
		// Creating the apk paths list (you can freely mix between remote and local URL)..
		String listAPKPaths = 	Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/testApp.apk:" +
								exampleTestAPKPath 
								// This last resource is downloaded from the web.
								+ ":http://jdbc.postgresql.org/download/postgresql-9.2-1002.jdbc4.jar";
		
		Log.i(TAG_MAIN, "1st Test: Fetch the certificate by reverting package name with no associative map..");
		mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(listAPKPaths, null, null, ClassLoader.getSystemClassLoader().getParent());		
		
		try {
			
			Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);
			
			if (loadedClass != null) {
				Log.w(TAG_MAIN, "No class should be returned in this case!!");
			}
			else {
				Log.i(TAG_MAIN, "SecureDexClassLoader loads nothing since no certificate should have been found. CORRECT!");
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.w(TAG_MAIN, "No class should be searched in this case!!");
		}
		
		// Remove the cached resources before the next test..
		// 2nd parameter may have been false as well since no certificate was fetched in this case..
		mSecureDexClassLoader.wipeOutPrivateAppCachedData(true, true);
		
		// 2nd Test: Fetch the certificate by filling associative map 
		// between package name and certificate --> FAIL cause the apk
		// was signed with the DEBUG ANDROID certificate
		
		// Filling the associative map to link package names and certificates..
		Map<String, String> packageNamesToCertMap = new HashMap<String, String>();
		// 1st Entry: valid remote certificate location
		// packageNamesToCertMap.put("headfirstlab.nasadailyimage", "https://github.com/lukeFalsina/test/test_cert.pem");
		packageNamesToCertMap.put("headfirstlab.nasadailyimage", "https://dl.dropboxusercontent.com/u/28681922/test_cert.pem");
		// 2nd Entry: inexistent certificate -> This link will be enforced to https but still there is no certificate at the final pointed URL
		packageNamesToCertMap.put("it.polimi.example", "http://google.com/test_cert.pem");
		// 3rd Entry: misspelled and so invalid URL (missing a p..)
		packageNamesToCertMap.put("it.polimi.example2", "htt://google.com/test_cert2.pem");
		// 4th Entry: reverse package name and then inexistent certificate at https://polimi.it/example3/certificate.pem
		packageNamesToCertMap.put("it.polimi.example3", null);

		Log.i(TAG_MAIN, "2nd Test: Fetch the certificate by filling associative map..");
		mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	exampleTestAPKPath, 
																			null, 
																			packageNamesToCertMap, 
																			ClassLoader.getSystemClassLoader().getParent());
		
		try {
			Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);
			
			if (loadedClass != null) {
				
				Log.w(TAG_MAIN, "No class should be loaded!");
			} else {
				
				Log.i(TAG_MAIN, "This time the chosen class should find a certificate but it's the " +
						"wrong one! CORRECT!");
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.w(TAG_MAIN, "Class should be present in the provided path!!");
		}
		
		// 3rd Test: Fetch the certificate by filling associative map 
		// between package name and certificate --> FAIL cause some of 
		// signatures in the container failed the verification process
		// against the developer certificate.
		Log.i(TAG_MAIN, "3rd Test: Fetch the certificate by filling associative map..");
		mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	exampleSignedChangedAPKPath, 
																			null, 
																			packageNamesToCertMap, 
																			ClassLoader.getSystemClassLoader().getParent());
		
		try {
			Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);
			
			if (loadedClass != null) {
				
				Log.w(TAG_MAIN, "No class should be loaded!");
			} else {
				
				Log.i(TAG_MAIN, "This time the chosen class should find a certificate but the" +
						"apk container signatures do not match all properly and so no class loading! CORRECT!");
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.w(TAG_MAIN, "Class should be present in the provided path!!");
		}
		
		// 4th Test: Fetch the certificate by filling associative map 
		// between package name and certificate --> SUCCESS cause this 
		// time the apk was signed with the correct certificate
		Log.i(TAG_MAIN, "4th Test: Fetch the certificate by filling associative map..");
		
		// Creating the apk paths list (you can mix between remote and local URL)..
		listAPKPaths = 	"http://google.com/testApp2.apk:" + exampleSignedAPKPath;
		
		mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	listAPKPaths, 
																			null, 
																			packageNamesToCertMap, 
																			ClassLoader.getSystemClassLoader().getParent());
		
		try {
			Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);
			
			if (loadedClass != null) {
				
				final Activity NasaDailyActivity = (Activity) loadedClass.newInstance();
				
				Log.i(TAG_MAIN, "Found valid class: " + loadedClass.getSimpleName() + "; APK path: " + exampleSignedAPKPath.toString() + "; Success!");
				
				toastHandler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								"SecureDexClassLoader was successful! Found activity: " + NasaDailyActivity.getClass().getName(),
								Toast.LENGTH_SHORT).show();
					}
					
				});
				
			} else {
				
				Log.w(TAG_MAIN, "This time the chosen class should pass the security checks!");
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.w(TAG_MAIN, "Class should be present in the provided path!!");
		} catch (InstantiationException e) {
			Log.w(TAG_MAIN, "Error while instanciating the loaded class!!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Log.w(TAG_MAIN, "Error while instanciating the loaded class!!");
			e.printStackTrace();
		}
		
		// Remove only the cached certificates, since no container was downloaded
		// from the web in test cases 2, 3 and 4.
		mSecureDexClassLoader.wipeOutPrivateAppCachedData(false, true);
		Log.d(TAG_MAIN, "Cached data of SecureDexClassLoader have been wiped out..");
	}

	/**
	 * This method is used to set up and manage a DexClassLoader component in 
	 * order to retrieve a new activity from an .apk, which has been 
	 * already downloaded and installed on the mobile device.
	 * If everything works fine, it will instantiate the main activity of 
	 * this .apk.
	 * 
	 */
	protected void setUpDexClassLoader() {
		
		// First check: this operation can only start after 
		// that the proper button has just been pressed..
		if (!effectiveDexClassLoader) return;
		
		Log.d(TAG_MAIN, "Setting up DexClassLoader..");
		
		File dexOutputDir = getDir("dex", MODE_PRIVATE);
		DexClassLoader mDexClassLoader = new DexClassLoader(	exampleTestAPKPath, 
																dexOutputDir.getAbsolutePath(), 
																null, 
																ClassLoader.getSystemClassLoader().getParent());
		
		try {
			
			// Load NasaDailyImage Main Activity..
			Class<?> loadedClass = mDexClassLoader.loadClass(classNameInAPK);
			final Activity NasaDailyActivity = (Activity) loadedClass.newInstance();
			
			// Note that in this case loading class operation was performed even if the APK which contains
			// the target class was signed just with the Android Debug key. This operation would have failed
			// if SecureDexClassLoader would have been used in stead..
			Log.i(TAG_MAIN, "Found class: " + loadedClass.getSimpleName() + "; APK path: " + exampleTestAPKPath.toString());
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							"DexClassLoader was successful! Found activity: " + NasaDailyActivity.getClass().getName(),
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			// An intent is defined to start the new loaded activity.
			//Intent transitionIntent = new Intent(this, loadedClass);
			//startActivity(transitionIntent);
			//transitionIntent.setClassName("headfirstlab.nasadailyimage", "headfirstlab.nasadailyimage.NasaDailyImage");
			
		} catch (ClassNotFoundException e) {

			Log.e(TAG_MAIN, "Error: Class not found!");
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							"Error! No class found for DexClassLoader..",
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			e.printStackTrace();
		} catch (ActivityNotFoundException e) {
		
			Log.e(TAG_MAIN, "Error: Activity not found in the manifest!");
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							"Error! The activity found by DexClassLoader is not a legitimate one..",
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
