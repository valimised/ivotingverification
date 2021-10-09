package ee.vvk.ivotingverification.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * JSON parser.
 * 
 * @version 16.05.2013
 */
public class JSONParser {

	// FEFF because this is the Unicode char represented by the UTF-8 byte order
	// mark (EF BB BF).
	private static final String UTF8_BOM = "\uFEFF";

	public static void parseConfig(String input) throws JSONException {
		parseConfig(input, false);
	}

	public static void parseConfig(String input, boolean textOnly) throws JSONException {
		if (input.startsWith(UTF8_BOM)) {
			input = input.substring(1);
		}
		JSONObject appConfig = new JSONObject(input).getJSONObject("appConfig");

		parseJsonTexts(appConfig);
		parseJsonErrors(appConfig);
		parseJsonElections(appConfig);
		parseJsonVersions(appConfig);
		if (!textOnly) {
			parseJsonColors(appConfig);
			parseJsonParams(appConfig);
			parseJsonLanguages(appConfig);
		}
	}

	private static void parseJsonTexts(JSONObject appConfig) {
		JSONObject texts = appConfig.optJSONObject("texts");
		if (texts == null) {
			return;
		}
		C.loading = texts.optString("loading", C.loading);
		C.welcomeMessage = texts.optString("welcome_message", C.welcomeMessage);
		C.lblVote = texts.optString("lbl_vote", C.lblVote);
		C.lblVoteTxt = texts.optString("lbl_vote_txt", C.lblVoteTxt);
		C.lblVoteSigner = texts.optString("lbl_vote_signer", C.lblVoteSigner);
		C.btnNext = texts.optString("btn_next", C.btnNext);
		C.btnMore = texts.optString("btn_more", C.btnMore);
		C.btnOk = texts.optString("btn_ok", C.btnOk);
		C.btnPacketData = texts.optString("btn_packet_data", C.btnPacketData);
		C.btnWifi = texts.optString("btn_wifi", C.btnWifi);
		C.btnUpdate = texts.optString("btn_update", C.btnUpdate);
		C.btnVerify = texts.optString("btn_verify", C.btnVerify);
		C.noChoice = texts.optString("lbl_no_choice", C.noChoice);
		C.lblChoice = texts.optString("lbl_choice", C.lblChoice);

		C.lblCloseTimeout = texts.optString("lbl_close_timeout", C.lblCloseTimeout);
		if (!C.lblCloseTimeout.contains("XX")) {
			throw new IllegalArgumentException("lbl_close_timeout does not contain XX");
		}
	}

	private static void parseJsonErrors(JSONObject appConfig) {
		JSONObject errors = appConfig.optJSONObject("errors");
		if (errors == null) {
			return;
		}
		C.noNetworkMessage = errors.optString("no_network_message", C.noNetworkMessage);
		C.getConfigMessage = errors.optString("get_config_message", C.getConfigMessage);
		C.badConfigMessage = errors.optString("bad_config_message", C.badConfigMessage);
		C.badVersionMessage = errors.optString("bad_version_message", C.badVersionMessage);

		C.problemQrCodeMessage = errors.optString(
				"problem_qrcode_message", C.problemQrCodeMessage);
		C.sendServerRequestMessage = errors.optString(
				"send_server_request_message", C.sendServerRequestMessage);
		C.badServerResponseMessage = errors.optString(
				"bad_server_response_message", C.badServerResponseMessage);
		C.badDeviceMessage = errors.optString(
				"bad_device_message", C.badDeviceMessage);
		C.cameraPermissionRequired = errors.optString(
				"camera_permission_required_message", C.cameraPermissionRequired);
	}

	private static void parseJsonColors(JSONObject appConfig) {
		JSONObject colors = appConfig.optJSONObject("colors");
		if (colors == null) {
			return;
		}
		C.frameBackground = colors.optString("frame_background", C.frameBackground);
		C.mainWindowForeground = colors.optString(
				"main_window_foreground", C.mainWindowForeground);
		C.errorWindowForeground = colors.optString(
				"error_window_foreground", C.errorWindowForeground);
		C.loadingWindow = colors.optString(
				"loading_window_background", C.loadingWindow);
		C.loadingWindowForeground = colors.optString(
				"loading_window_foreground", C.loadingWindowForeground);
		C.mainWindow = colors.optString("main_window", C.mainWindow);
		C.mainWindowShadow = colors.optString("main_window_shadow", C.mainWindowShadow);
		C.errorWindow = colors.optString("error_window", C.errorWindow);
		C.errorWindowShadow = colors.optString("error_window_shadow", C.errorWindowShadow);
		C.btnBackground = colors.optString("btn_background", C.btnBackground);
		C.btnForeground = colors.optString("btn_foreground", C.btnForeground);
		C.btnVerifyForeground = colors.optString(
				"btn_verify_foreground", C.btnVerifyForeground);
		C.btnVerifyBackgroundStart = colors.optString(
				"btn_verify_background_start", C.btnVerifyBackgroundStart);
		C.btnVerifyBackgroundCenter = colors.optString(
				"btn_verify_background_center", C.btnVerifyBackgroundCenter);
		C.btnVerifyBackgroundEnd = colors.optString(
				"btn_verify_background_end", C.btnVerifyBackgroundEnd);
		C.lblBackground = colors.optString("lbl_background", C.lblBackground);
		C.lblForeground = colors.optString("lbl_foreground", C.lblForeground);
		C.lblShadow = colors.optString("lbl_shadow", C.lblShadow);
		C.lblOuterContainerBackground = colors.optString(
				"lbl_outer_container_background", C.lblOuterContainerBackground);
		C.lblOuterContainerForeground = colors.optString(
				"lbl_outer_container_foreground", C.lblOuterContainerForeground);
		C.lblInnerContainerBackground = colors.optString(
				"lbl_inner_container_background", C.lblInnerContainerBackground);
		C.lblInnerContainerForeground = colors.optString(
				"lbl_inner_container_foreground", C.lblInnerContainerForeground);
		C.lblCloseTimeoutForeground = colors.optString(
				"lbl_close_timeout_foreground", C.lblCloseTimeoutForeground);
		C.lblCloseTimeoutBackgroundStart = colors.optString(
				"lbl_close_timeout_background_start", C.lblCloseTimeoutBackgroundStart);
		C.lblCloseTimeoutBackgroundCenter = colors.optString(
				"lbl_close_timeout_background_center", C.lblCloseTimeoutBackgroundCenter);
		C.lblCloseTimeoutBackgroundEnd = colors.optString(
				"lbl_close_timeout_background_end", C.lblCloseTimeoutBackgroundEnd);
		C.lblCloseTimeoutShadow = colors.optString(
				"lbl_close_timeout_shadow", C.lblCloseTimeoutShadow);
		C.lblOuterInnerContainerDivider = colors.optString(
				"lbl_outer_inner_container_divider", C.lblOuterInnerContainerDivider);
	}

	private static void parseJsonVersions(JSONObject appConfig) throws JSONException {
		JSONObject params = appConfig.getJSONObject("versions");

		/* Mandatory */

		C.expectedVersion = params.getInt("android_version_code");

	}

	private static void parseJsonParams(JSONObject appConfig) throws JSONException {
		JSONObject params = appConfig.getJSONObject("params");

		/* Mandatory */
		C.verificationUrlArray = jsonToArray(params.getJSONArray("verification_url"));
		C.verificationTlsArray = jsonToArray(params.getJSONArray("verification_tls"));
		C.tspregServiceCert = params.getString("tspreg_service_cert");
		C.tspregClientCert = params.getString("tspreg_client_cert");
		C.publicKey = params.getString("public_key");

		/* Optional */
		C.ocspServiceCertArray = jsonToArray(params.optJSONArray("ocsp_service_cert"));
		C.helpURL = params.optString("help_url", C.helpURL);
		C.closeTimeout = params.optInt("close_timeout", C.closeTimeout);
		C.closeInterval = params.optInt("close_interval", C.closeInterval);
		C.connectionTimeout1 = params.optInt("con_timeout_1", C.connectionTimeout1);
		C.connectionTimeout2 = params.optInt("con_timeout_2", C.connectionTimeout2);
	}

	private static void parseJsonElections(JSONObject appConfig) throws JSONException {
		JSONObject elections = appConfig.optJSONObject("elections");
		if (elections == null) {
			return;
		}
		Iterator<String> identifiers = elections.keys();
		while (identifiers.hasNext()) {
			String identifier = identifiers.next();
			C.elections.put(identifier, elections.getString(identifier));
		}
	}

	private static void parseJsonLanguages(JSONObject appConfig) throws JSONException {
		JSONArray languages = appConfig.optJSONArray("languages");
		if (languages == null) {
			return;
		}
		for (int i = 0; i < languages.length(); i++) {
			C.languages.add(languages.getString(i));
		}
	}

	private static String[] jsonToArray(JSONArray json) throws JSONException {
		if (json == null) {
			return new String[0];
		}
		String[] array = new String[json.length()];
		for (int i = 0; i < json.length(); i++) {
			array[i] = json.getString(i);
		}
		return array;
	}

}
