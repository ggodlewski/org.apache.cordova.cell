package org.apache.cordova.cell;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CellIdInfoPlugin extends CordovaPlugin {

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

		// retrieve a reference to an instance of TelephonyManager
		TelephonyManager telephonyManager = (TelephonyManager) this.cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);

		if (action.equals("getCellInfo")) {
			GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

			String networkOperator = telephonyManager.getNetworkOperator();
			String networkOperatorName = telephonyManager.getNetworkOperatorName();
			int networkType = telephonyManager.getNetworkType();

			String mcc = networkOperator.substring(0, 3);
			String mnc = networkOperator.substring(3);
			int cid = cellLocation.getCid();
			int lac = cellLocation.getLac();

			try {
				JSONObject cellResult = new JSONObject();
				cellResult.put("networkOperator", networkOperator);
				cellResult.put("networkOperatorName", networkOperatorName);
				cellResult.put("networkType", networkType);
				cellResult.put("mcc", mcc);
				cellResult.put("mnc", mnc);
				cellResult.put("cid", cid);
				cellResult.put("lac", lac);

				RqsLocation(Integer.valueOf(mcc), Integer.valueOf(mnc), cid, lac, cellResult);

				callbackContext.success(cellResult);
				return true;

			} catch (JSONException e) {
				e.printStackTrace();
				// TODO Auto-generated catch block
				callbackContext.error("Failed to retrive cell Info: "
						+ e.getMessage());
				return false;
			}
		}

		callbackContext.error("Unknown method: "+action);

		return false;
	}

	private Boolean RqsLocation(int mcc, int mnc, int cid, int lac, JSONObject cellResult) throws JSONException {

		Boolean result = false;

		String urlmmap = "http://www.google.com/glm/mmap";

		try {
			URL url = new URL(urlmmap);
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.connect();

			OutputStream outputStream = httpConn.getOutputStream();
			WriteData(outputStream, mcc, mnc, cid, lac);

			InputStream inputStream = httpConn.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);

			dataInputStream.readShort();
			dataInputStream.readByte();
			int code = dataInputStream.readInt();
			if (code == 0) {
				int myLatitude = dataInputStream.readInt();
				int myLongitude = dataInputStream.readInt();

				cellResult.put("lat", ((float)myLatitude)/1000000);
				cellResult.put("lng", ((float)myLongitude)/1000000);

				result = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	private void WriteData(OutputStream out, int mcc, int mnc, int cid, int lac)
			throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(out);
		dataOutputStream.writeShort(21);
		dataOutputStream.writeLong(0);
		dataOutputStream.writeUTF("en");
		dataOutputStream.writeUTF("Android");
		dataOutputStream.writeUTF("1.0");
		dataOutputStream.writeUTF("Web");
		dataOutputStream.writeByte(27);
		dataOutputStream.writeInt(mnc);
		dataOutputStream.writeInt(mcc);

		if (cid > 65536) {
		    /* GSM: 4 hex digits, UTMS: 6 hex digits */
			dataOutputStream.writeInt(5);
		} else {
			dataOutputStream.writeInt(3);
		}

		dataOutputStream.writeUTF("");

		dataOutputStream.writeInt(cid);
		dataOutputStream.writeInt(lac);

		dataOutputStream.writeInt(mnc);
		dataOutputStream.writeInt(mcc);
		dataOutputStream.writeInt(0);
		dataOutputStream.writeInt(0);
		dataOutputStream.flush();
	}
}
