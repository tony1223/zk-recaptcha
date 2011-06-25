/**
 * This file is under LGPL license , 
 * created 2011/6/24 19:30  TonyQ 
 */
package org.zkoss.recaptcha;

import java.util.Map;

import org.zkoss.lang.Library;
import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.impl.XulElement;

/**
 * Note that according
 * 
 * @author tony
 * 
 */
public class Recaptcha extends XulElement {

	{
		addClientEvent(Recaptcha.class, onVerifying, CE_IMPORTANT | CE_REPEAT_IGNORE);
	}

	private static String onVerifying = "onVerifying";

	private String _theme = "red";

	private String _type = "image";

	private boolean _valid = false;

	private String _lastChallenge = null;

	private boolean _forceValid = false;

	/**
	 * reCaptchay Public key , could be set in zk.xml , but also in component if
	 * need.
	 */
	private String _publicKey;

	private String _privateKey;

	private EventListener evt = new EventListener() {

		public void onEvent(Event event) throws Exception {
		}
	};

	/**
	 * The reCaptcha theme ,default is "red".
	 * 
	 * @return
	 */
	public String getTheme() {
		return _theme;
	}

	/**
	 * About avaiable themes, please reference to this document.
	 * http://code.google.com/intl/zh-TW/apis/recaptcha/docs/customization.html
	 * 
	 * Currently it have 4 thems : red,white,blackglass,clean
	 * 
	 * @param theme
	 */
	public void setTheme(String theme) {

		if (!Objects.equals(_theme, theme)) {
			_theme = theme;
			smartUpdate("theme", _theme);
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getType() {
		return _type;
	}

	/**
	 * Can be called with an argument 'image' or 'audio' to allow the user to
	 * switch the type of the CAPTCHA.
	 * 
	 * Default is "image"
	 * 
	 * @param _type
	 */
	public void setType(String _type) {
		this._type = _type;
	}

	public String getPublicKey() {
		if (_publicKey == null) {
			return Library.getProperty("org.zkoss.recaptch.publickey");
		}

		return _publicKey;
	}

	public String getPrivateKey() {
		if (_privateKey == null) {
			return Library.getProperty("org.zkoss.recaptch.privatekey");
		}

		return _privateKey;
	}

	public void setPublicKey(String publicKey) {
		if (!Objects.equals(_publicKey, publicKey))
			this._publicKey = publicKey;
	}

	/**
	 * If you set forceValid as true , it will "validate" every time when user
	 * left focus on the reCaptcha ,
	 * 
	 * Notice: If user typing wrong , we will force reload for the image/sound ,
	 * since the google reCaptcha service only allow us to validate during one
	 * challenge.
	 * 
	 * @param bool
	 */
	public void setForceValid(boolean bool) {
		if (_forceValid != bool) {
			_forceValid = bool;
			if (_forceValid) {
				this.addEventListener(onVerifying, evt);
			} else {
				this.removeEventListener(onVerifying, evt);
			}
		}

	}

	public boolean getForceValid() {
		return _forceValid;
	}

	public void service(AuRequest request, boolean everError) {
		if (onVerifying.equals(request.getCommand())) {
			Map map = request.getData();
			String challenge = (String) map.get("challenge");

			// if we verify it true in same challenge before ,
			// the second time will get a false even same value ,
			// so we just keep the data and not verifying it again in same
			// challenge.
			if ((_lastChallenge == null || !_lastChallenge.equals(challenge)) || !_valid) {
				String result = URLUtil.post("http://www.google.com/recaptcha/api/verify", new String[] { "privatekey",
						getPrivateKey(), "remoteip", Executions.getCurrent().getRemoteAddr(), "challenge", challenge,
						"response", (String) map.get("response") });

				String line1 = result.split("\n")[0];
				if (line1.indexOf("true") != -1) {
					_valid = true;
				} else {
					_valid = false;
				}
				if (!_valid) {
					this.reload();
				}/*	left this to face 2
				else{
					response(new AuInvoke(this,"showValid"));
				}*/
			}
			_lastChallenge = challenge;
		} else
			super.service(request, everError);
	}

	/**
	 * reload captcha image.
	 */
	public void reload() {
		response("reload", new AuInvoke(this, "reload", true));
	}

	public boolean isValid() {
		return _valid;
	}

	// super//
	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
		super.renderProperties(renderer);

		if (!Objects.equals(_type, "image"))
			render(renderer, "type", _type);

		if (!Objects.equals(_theme, "red"))
			render(renderer, "theme", _theme);

		render(renderer, "publickey", getPublicKey());
	}

	/**
	 * The default zclass is "z-recaptcha"
	 */
	public String getZclass() {
		return (this._zclass != null ? this._zclass : "z-recaptcha");
	}

}
