/**
 * Copyright (C) 2013 Eesti Vabariigi Valimiskomisjon 
 * (Estonian National Electoral Committee), www.vvk.ee
 *
 * Written in 2013 by AS Finestmedia, www.finestmedia.ee
 * 
 * Vote-verification application for Estonian Internet voting system
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
 
package ee.vvk.ivotingverification.util;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

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

	public JSONParser(String input) throws JSONException {

		jObj = new JSONObject(input.substring(1, input.length()));
		jObjConfig = jObj.optJSONObject(TAG_APP_CONFIG);

		parseJsonAppConfig(jObjConfig);
	}

	public void parseJsonAppConfig(JSONObject json) throws JSONException {
		parseJsonTexts(json);
		parseJsonErrors(json);
		parseJsonColors(json);
		parseJsonParams(json);
		parseJsonElections(json);
	}

	private void parseJsonTexts(JSONObject json) throws JSONException {
		texts = json.optJSONObject(TAG_TEXTS);
		if (texts.length() > 0) {
			C.loading = texts.optString("loading", C.loading);
			C.welcomeMessage = texts.optString("welcome_message",
					C.welcomeMessage);
			C.lblVote = texts.optString("lbl_vote", C.lblVote);
			C.lblVoteTxt = texts.optString("lbl_vote_txt", C.lblVoteTxt);
			C.btnNext = texts.optString("btn_next", C.btnNext);
			C.btnMore = texts.optString("btn_more", C.btnMore);
			C.btnOk = texts.optString("btn_ok", C.btnOk);
			C.btnPacketData = texts.optString("btn_packet_data",
					C.btnPacketData);
			C.btnWifi = texts.optString("btn_wifi", C.btnWifi);
			C.btnVerify = texts.optString("btn_verify", C.btnVerify);
			C.lblChoice = texts.optString("lbl_choice", C.lblChoice);
			C.lblCloseTimeout = texts.optString("lbl_close_timeout",
					C.lblCloseTimeout);
			C.notificationTitle = texts.optString("notification_title",
					C.notificationTitle);
			C.notificationMessage = texts.optString("notification_message",
					C.notificationMessage);
		}
	}

	private void parseJsonErrors(JSONObject json) throws JSONException {
		errors = json.optJSONObject(TAG_ERRORS);
		if (texts.length() > 0) {
			C.noNetworkMessage = errors.optString("no_network_message",
					C.noNetworkMessage);
			C.problemQrCodeMessage = errors.optString("problem_qrcode_message",
					C.problemQrCodeMessage);
			C.badServerResponseMessage = errors.optString(
					"bad_server_response_message", C.badServerResponseMessage);
			C.badVerificationMessage = errors.optString(
					"bad_verification_message", C.badVerificationMessage);
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
			C.appURL = params.optString("app_url", C.appURL);
			C.helpURL = params.optString("help_url", C.helpURL);
			C.closeTimeout = params.optInt("close_timeout", C.closeTimeout);
			C.closeInterval = params.optInt("close_interval", C.closeInterval);
			C.publicKey = params.optString("public_key", C.publicKey);
		}
	}

	private void parseJsonElections(JSONObject json) throws JSONException {
		elections = json.optJSONObject(TAG_ELECTIONS);
		if (elections.length() > 0) {
			Iterator<String> myIter = elections.keys();
			HashMap<String, String> electionsMap = new HashMap<String, String>();

			while (myIter.hasNext()) {
				String tempValue = myIter.next();
				electionsMap.put(tempValue,
						elections.optString(tempValue, tempValue));
			}
			C.elections = electionsMap;
		}
	}
}