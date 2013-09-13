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

import android.graphics.Typeface;

/**
 * Configurations from server.
 * 
 * @version 28.05.2013
 */
public class C {

	public static String appURL = "";
	
	public static String configURL = "";
	
	public static String helpURL = "";

	public final static String trustStorePass = "secret";

	public static Typeface typeFace = Typeface.DEFAULT;

	public static String welcomeMessage = "";

	public static String loading = "Laeb...";

	public static String loadingWindow = "#33B5E5";

	public static String loadingWindowForeground = "#FFFFFF";

	public static String frameBackground = "#AA444444";

	public static String errorWindow = "#FF0000";

	public static String errorWindowShadow = "#770000";

	public static String errorWindowForeground = "#FFFFFF";

	public static String mainWindow = "#33B5E5";

	public static String mainWindowShadow = "#005777";

	public static String mainWindowForeground = "#FFFFFF";

	public static String btnMore = "Abiinfo";

	public static String btnNext = "Edasi";

	public static String btnOk = "Ok";

	public static String btnPacketData = "Andmeside";

	public static String btnWifi = "Wifi";

	public static String btnVerify = "Kontrolli";

	public static String btnVerifyForeground = "#FFFFFF";

	public static String btnVerifyBackgroundStart = "#30B4E5";

	public static String btnVerifyBackgroundCenter = "#1AABE1";

	public static String btnVerifyBackgroundEnd = "#00A1DC";

	public static String btnBackground = "#F0F0F0";

	public static String btnForeground = "#727272";

	public static String lblVote = "Hääle kontrollimine";

	public static String lblVoteTxt = "";

	public static String lblChoice = "Tuvastatud valik";

	public static String lblCloseTimeout = "Rakendus sulgub XX sekundi pärast!";

	public static String notificationTitle = "VVK";

	public static String notificationMessage = "Valik on tuvastatud";

	public static String lblBackground = "#33B5E5";

	public static String lblForeground = "#FFFFFF";

	public static String lblShadow = "#008EC2";

	public static String lblOuterContainerBackground = "#EAEAEA";

	public static String lblOuterContainerForeground = "#878686";

	public static String lblInnerContainerBackground = "#FFFFFF";

	public static String lblInnerContainerForeground = "#878686";

	public static String lblOuterInnerContainerDivider = "#E9E9E9";

	public static String lblCloseTimeoutForeground = "#454444";

	public static String lblCloseTimeoutBackgroundStart = "#FEEC00";

	public static String lblCloseTimeoutBackgroundCenter = "#F9D303";

	public static String lblCloseTimeoutBackgroundEnd = "#F7C804";

	public static String lblCloseTimeoutShadow = "#C6A002";

	public static int closeTimeout = 30000;

	public static int closeInterval = 1000;

	public static String publicKey = "";

	public static String noNetworkMessage = "Veenduge, et nutiseadme andmeside on võimaldatud";

	public static String problemQrCodeMessage = "QR koodi ei õnnestunud tuvastada";

	public static String badServerResponseMessage = "Tehniline viga, palun teavitage valimiste läbiviijat";

	public static String badVerificationMessage = "";

	public static HashMap<String, String> elections = new HashMap<String, String>();
}