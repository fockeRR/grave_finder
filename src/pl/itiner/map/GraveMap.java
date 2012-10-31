/*
 * This file is part of the Lokalizator grobów project.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation v3; 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package pl.itiner.map;

import pl.itiner.grave.GeoJSON;
import pl.itiner.grave.R;
import pl.itiner.grave.ResultList;
import pl.itiner.models.Deathman;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class GraveMap extends MapActivity {
	MapView mapView;
	Deathman dtDeathman;
	TextView mapSurnameName;
	TextView mapBirthDate;
	TextView mapDeathDate;
	TextView mapFunrealDate;
	TextView mapCementry;
	TextView mapRow;
	TextView mapQuater;
	TextView mapField;
	MapController mc;
	GeoPoint grave;
	GeoPoint user;

	private MyLocationOverlay userLocationOverlay = null;
	public Location mobileLocation;
	public LocationManager locManager;
	public LocationListener locListener;
	private static final int REQUEST_CODE = 0;
	private static final String TAG = "GraveMap";
	private static boolean wasZoomed = false;
//	UserOverlay userOverlay;
	GraveLocationOverlay glo; 
//	GraveOverlay graveOverlay;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		Bundle b = getIntent().getExtras();
		if(b!= null)
		{
		double x = b.getDouble("x");
		double y = b.getDouble("y");
		int id = b.getInt("id");

		Deathman tmp = GeoJSON.dList.get(id);
		mapSurnameName = (TextView) findViewById(R.id.map_surname_name);
		mapSurnameName.setText(tmp.getName() + " " + tmp.getSurname());
		
		mapBirthDate = (TextView) findViewById(R.id.map_value_dateBirth);
		
		mapBirthDate.setText(tmp.getDate_birth());
		
		mapDeathDate = (TextView) findViewById(R.id.map_value_dateDeath);
		mapDeathDate.setText(tmp.getDeath_date());		

		mapFunrealDate = (TextView) findViewById(R.id.map_value_dateFunreal);
		mapFunrealDate.setText(tmp.getBurial_date());		

		mapField = (TextView) findViewById(R.id.map_field_value);
		mapField.setText(tmp.getField());
		
		mapRow = (TextView) findViewById(R.id.map_row_value);
		mapRow.setText(tmp.getRow());
		
		mapQuater = (TextView) findViewById(R.id.map_quater_value);
		mapQuater.setText(tmp.getQuater());

		mapCementry = (TextView) findViewById(R.id.map_value_cementry);
		String cm_name = ResultList.cementeries[Integer
				.parseInt(tmp.getCm_id())];
		mapCementry.setText(cm_name);

		mapView = (MapView) findViewById(R.id.mapView);
		mc = mapView.getController();

		mapView.setBuiltInZoomControls(true);

		double lat = x;
		double lng = y;
		grave = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
	
		mapView.getOverlays().clear();
	//	user = new GeoPoint(52408333, 16908333);
		
//		userOverlay = new UserOverlay(user);
	//	graveOverlay = new GraveOverlay(p);
		glo  = new GraveLocationOverlay(mapView,
				this.getResources().getDrawable(R.drawable.graveloc), 
				getApplicationContext(),
				grave);
		
		userLocationOverlay = new MyLocationOverlay(this, mapView);
		userLocationOverlay.getMyLocation();
		mapView.getOverlays().add(userLocationOverlay);
		
		//mapView.getOverlays().add(userOverlay);
		mapView.getOverlays().add(glo);
		//mapView.getOverlays().add(graveOverlay);
		mapView.invalidate();
		
		}

		locListener = new MyLocationListener();
		locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        mobileLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        user = createUserGeoPoint(mobileLocation);
//        if(mobileLocation != null) {
//		user = new GeoPoint((int)(mobileLocation.getLatitude()*1e6),(int)(mobileLocation.getLongitude()*1e6)) ;
//x        user = userLocationOverlay.getMyLocation();
//		makeOverlay(p,user);
//		mapView.invalidate();
//		mc.setZoom(13);
//    	mc.animateTo(new GeoPoint(
//    			(grave.getLatitudeE6()+user.getLatitudeE6())/2, 
//    			(grave.getLongitudeE6()+user.getLongitudeE6())/2)
//    			);
//    	
        double latitudeSpan = Math.round(Math.abs(user.getLatitudeE6()- 
                grave.getLatitudeE6()));
		double longitudeSpan = Math.round(Math.abs(user.getLongitudeE6() - 
                grave.getLongitudeE6()));
		
		mc.zoomToSpan((int)(latitudeSpan*2), (int)(longitudeSpan*2));                
				
		mc.animateTo(new GeoPoint
				((grave.getLatitudeE6()+user.getLatitudeE6())/2, 
		    			(grave.getLongitudeE6()+user.getLongitudeE6())/2));
//        }
    	String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(provider != null){
            Log.v(TAG, " Location providers: "+provider);
            //Start searching for location and update the location text when update available. 
            // Do whatever you want
           
        }else{
            //Users did not switch on the GPS
        	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, REQUEST_CODE);
        }
		}
	
	private GeoPoint createUserGeoPoint(Location location) {
		if(location != null) {
			return new GeoPoint((int)(location.getLatitude()*1e6),(int)(location.getLongitude()*1e6));
		} else {
			//return Poznan center position
//			String[] position = getResources().getStringArray(R.array.poznan_lat_lon);
//			return new GeoPoint((int) (Double.parseDouble(position[0])*1e6),(int) (Double.parseDouble(position[1])*1e6));
			return null;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	@Override
	protected void onPause() {
		super.onPause();
		userLocationOverlay.disableCompass();
		userLocationOverlay.disableMyLocation();
		locManager.removeUpdates(locListener);
	}


	@Override
	protected void onResume() {
		super.onResume();
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
		userLocationOverlay.enableMyLocation();
		userLocationOverlay.enableCompass();
	}
	
	public GeoPoint getLocation()
	{
		return new GeoPoint(
				(int)(userLocationOverlay.getLastFix().getLatitude() * 1e6),
				(int)(userLocationOverlay.getLastFix().getLongitude() * 1e6));
	}
	
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data){
	        if(requestCode == REQUEST_CODE && resultCode == 0){
	            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	            if(provider != null){
	                Log.v(TAG, " Location providers: "+provider);
	                //Start searching for location and update the location text when update available. 
	                // Do whatever you want
	               
	            }else{
	                //Users did not switch on the GPS
	            }
	        }
	    }

	public class MyLocationListener implements LocationListener {
		   
		Location mobileLocation;
		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			mobileLocation = location;
			if(!wasZoomed)
			{
			GeoPoint newPos = new GeoPoint(
					(int)(mobileLocation.getLatitude() * 1e6),
					(int)(mobileLocation.getLongitude() * 1e6));
			
			double latitudeSpan = Math.round(Math.abs(mobileLocation.getLatitude()*1e6- 
	                grave.getLatitudeE6()));
			double longitudeSpan = Math.round(Math.abs(mobileLocation.getLongitude()*1e6 - 
	                grave.getLongitudeE6()));
			
			mc.zoomToSpan((int)(latitudeSpan*2), (int)(longitudeSpan*2));                
					
			mc.animateTo(new GeoPoint
					((grave.getLatitudeE6()+newPos.getLatitudeE6())/2, 
			    			(grave.getLongitudeE6()+newPos.getLongitudeE6())/2));
			
			mapView.invalidate();
			wasZoomed = true;
			}
			Log.i("LOCATION","LONG: "+location.getLongitude()+ " LAT:"+location.getLatitude());
		}

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId())
		{
			case R.id.map_menu_streets:
				mapView.setSatellite(false);
				//mapView.setStreetView(true);
				break;
			case R.id.map_menu_satellites:
				mapView.setSatellite(true);
				//mapView.setStreetView(false);
				break;
		}
		return false;
	}
	
}
// 0fwLhF406wY8NWJfI8BBuiifYfUUnVvVo8g__hg

