package ee.vvk.ivotingverification.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * JSON parser.
 * 
 * @version 16.05.2013
 */
public class JSONParser {

	private static final String TAG_APP_CONFIG = "appConfig";
	private static final String TAG_TEXTS = "texts";
	private static final String TAG_ERRORS = "errors";
	private static final String TAG_COLORS = "colors";
	private static final String TAG_PARAMS = "params";
	private static final String TAG_ELECTIONS = "elections";

	private JSONObject texts = null;
	private JSONObject errors = null;
	private JSONObject colors = null;
	private JSONObject params = null;
	private JSONObject elections = null;

	private JSONObject jObj = null;
	private JSONObject jObjConfig = null;

	// FEFF because this is the Unicode char represented by the UTF-8 byte order
	// mark (EF BB BF).
	public static final String UTF8_BOM = "\uFEFF";

	public JSONParser(String input) throws JSONException {

		if (input.substring(0, 1).equals(UTF8_BOM)) {
			jObj = new JSONObject(input.substring(1, input.length()));
		} else {
			jObj = new JSONObject(input.substring(0, input.length()));
		}
		jObjConfig = jObj.optJSONObject(TAG_APP_CONFIG);
		parseJsonAppConfig(jObjConfig);
	}

	public void parseJsonAppConfig(JSONObject json) throws JSONException {
		if (C.forLanguages) {
			parseJsonTexts(json);
			parseJsonErrors(json);
			parseJsonElections(json);
		} else {
			parseJsonTexts(json);
			parseJsonErrors(json);
			parseJsonColors(json);
			parseJsonParams(json);
			parseJsonElections(json);
			parseJsonLanguages(json);
		}
	}

	private void parseJsonTexts(JSONObject json) throws JSONException {
		if (texts == null) {
			texts = json.optJSONObject(TAG_TEXTS);
		}
		if (texts.length() > 0) {
			C.loading = texts.optString("loading", C.loading);
			C.welcomeMessage = texts.optString("welcome_message",
					C.welcomeMessage);
			C.lblVote = texts.optString("lbl_vote", C.lblVote);
			C.lblVoteTxt = texts.optString("lbl_vote_txt", C.lblVoteTxt);
			C.lblVoteSigner = texts.optString("lbl_vote_signer", C.lblVoteSigner);
			C.btnNext = texts.optString("btn_next", C.btnNext);
			C.btnMore = texts.optString("btn_more", C.btnMore);
			C.btnOk = texts.optString("btn_ok", C.btnOk);
			C.btnPacketData = texts.optString("btn_packet_data",
					C.btnPacketData);
			C.btnWifi = texts.optString("btn_wifi", C.btnWifi);
			C.btnVerify = texts.optString("btn_verify", C.btnVerify);
			C.noChoice = texts.optString("lbl_no_choice", C.noChoice);
			C.lblChoice = texts.optString("lbl_choice", C.lblChoice);
			C.lblCloseTimeout = texts.optString("lbl_close_timeout",
					C.lblCloseTimeout);
		}
	}

	private void parseJsonErrors(JSONObject json) throws JSONException {
		if (errors == null) {
			errors = json.optJSONObject(TAG_ERRORS);
		}
		if (errors.length() > 0) {
			C.noNetworkMessage = errors.optString("no_network_message",
					C.noNetworkMessage);
			C.problemQrCodeMessage = errors.optString("problem_qrcode_message",
					C.problemQrCodeMessage);
			C.badServerResponseMessage = errors.optString(
					"bad_server_response_message", C.badServerResponseMessage);
			C.badDeviceMessage = errors.optString(
					"bad_device_message", C.badDeviceMessage);
			C.badVerificationMessage = errors.optString(
					"bad_verification_message", C.badVerificationMessage);
			C.cameraPermissionRequired = errors.optString(
					"camera_permission_required_message", C.cameraPermissionRequired);
		}
	}

	private void parseJsonColors(JSONObject json) throws JSONException {
		colors = json.optJSONObject(TAG_COLORS);
		if (colors.length() > 0) {
			C.frameBackground = colors.optString("frame_background",
					C.frameBackground);
			C.mainWindowForeground = colors.optString("main_window_foreground",
					C.mainWindowForeground);
			C.errorWindowForeground = colors.optString(
					"error_window_foreground", C.errorWindowForeground);
			C.loadingWindow = colors.optString("loading_window_background",
					C.loadingWindow);
			C.loadingWindowForeground = colors.optString(
					"loading_window_foreground", C.loadingWindowForeground);
			C.mainWindow = colors.optString("main_window", C.mainWindow);
			C.mainWindowShadow = colors.optString("main_window_shadow",
					C.mainWindowShadow);
			C.errorWindow = colors.optString("error_window", C.errorWindow);
			C.errorWindowShadow = colors.optString("error_window_shadow",
					C.errorWindowShadow);
			C.btnBackground = colors.optString("btn_background",
					C.btnBackground);
			C.btnForeground = colors.optString("btn_foreground",
					C.btnForeground);
			C.btnVerifyForeground = colors.optString("btn_verify_foreground",
					C.btnVerifyForeground);
			C.btnVerifyBackgroundStart = colors.optString(
					"btn_verify_background_start", C.btnVerifyBackgroundStart);
			C.btnVerifyBackgroundCenter = colors
					.optString("btn_verify_background_center",
							C.btnVerifyBackgroundCenter);
			C.btnVerifyBackgroundEnd = colors.optString(
					"btn_verify_background_end", C.btnVerifyBackgroundEnd);
			C.lblBackground = colors.optString("lbl_background",
					C.lblBackground);
			C.lblForeground = colors.optString("lbl_foreground",
					C.lblForeground);
			C.lblShadow = colors.optString("lbl_shadow", C.lblShadow);
			C.lblOuterContainerBackground = colors.optString(
					"lbl_outer_container_background",
					C.lblOuterContainerBackground);
			C.lblOuterContainerForeground = colors.optString(
					"lbl_outer_container_foreground",
					C.lblOuterContainerForeground);
			C.lblInnerContainerBackground = colors.optString(
					"lbl_inner_container_background",
					C.lblInnerContainerBackground);
			C.lblInnerContainerForeground = colors.optString(
					"lbl_inner_container_foreground",
					C.lblInnerContainerForeground);
			C.lblCloseTimeoutForeground = colors
					.optString("lbl_close_timeout_foreground",
							C.lblCloseTimeoutForeground);
			C.lblCloseTimeoutBackgroundStart = colors.optString(
					"lbl_close_timeout_background_start",
					C.lblCloseTimeoutBackgroundStart);
			C.lblCloseTimeoutBackgroundCenter = colors.optString(
					"lbl_close_timeout_background_center",
					C.lblCloseTimeoutBackgroundCenter);
			C.lblCloseTimeoutBackgroundEnd = colors.optString(
					"lbl_close_timeout_background_end",
					C.lblCloseTimeoutBackgroundEnd);
			C.lblCloseTimeoutShadow = colors.optString(
					"lbl_close_timeout_shadow", C.lblCloseTimeoutShadow);
			C.lblOuterInnerContainerDivider = colors.optString(
					"lbl_outer_inner_container_divider",
					C.lblOuterInnerContainerDivider);
		}
	}

	private void parseJsonParams(JSONObject json) throws JSONException {
		params = json.optJSONObject(TAG_PARAMS);
		if (params.length() > 0) {
			//C.appURL = params.optString("app_url", C.appURL);
			//C.hostDomain = params.optString("host_domain", C.hostDomain);
			JSONArray jsonArray = params.getJSONArray("verification_url");
			C.verificationUrlArray = new String[jsonArray.length()];
			for(int i = 0; i < jsonArray.length(); i++) {
				C.verificationUrlArray[i] = jsonArray.getString(i);
			}
			jsonArray = params.getJSONArray("verification_tls");
			C.verificationTlsArray = new String[jsonArray.length()];
			for(int i = 0; i < jsonArray.length(); i++) {
				C.verificationTlsArray[i] = jsonArray.getString(i);
			}
			C.helpURL = params.optString("help_url", C.helpURL);
			C.closeTimeout = params.optInt("close_timeout", C.closeTimeout);
			C.closeInterval = params.optInt("close_interval", C.closeInterval);
			C.connectionTimeout1 = params.optInt("con_timeout_1", C.connectionTimeout1);
			C.connectionTimeout2 = params.optInt("con_timeout_2", C.connectionTimeout2);
			C.publicKey = params.optString("public_key", C.publicKey);
			jsonArray = params.getJSONArray("ocsp_service_cert");
			C.ocspServiceCertArray = new String[jsonArray.length()];
			for(int i = 0; i < jsonArray.length(); i++) {
				C.ocspServiceCertArray[i] = jsonArray.getString(i);
			}
			C.tspregServiceCert = params.optString("tspreg_service_cert", C.tspregServiceCert);
			C.tspregClientCert = params.optString("tspreg_client_cert", C.tspregClientCert);
		}
	}

	private void parseJsonElections(JSONObject json) throws JSONException {
		if (elections == null) {
			elections = json.optJSONObject(TAG_ELECTIONS);
		}
		if (elections.length() > 0) {
			Iterator<String> myIter = elections.keys();
			HashMap<String, String> electionsMap = new HashMap<>();

			while (myIter.hasNext()) {
				String tempValue = myIter.next();
				electionsMap.put(tempValue,
						elections.optString(tempValue, tempValue));
			}
			C.elections = electionsMap;
		}
	}

	private void parseJsonLanguages(JSONObject json) throws JSONException {
		if (json.length() > 5) {
			JSONArray languagesArray = json.optJSONArray("languages");
			for (int i = 0; i < languagesArray.length(); i++) {
				C.languages.add(languagesArray.optString(i));
			}
		}
	}
}
