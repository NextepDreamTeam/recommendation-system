/**
 * 
 */
//var url = 'http://193.28.95.209';
var url = 'http://localhost:9000';

(function (window, undefined) {

	var dre = function () {
		
		if ( window === this ) {
          return new $util(properties);
		}
		return this;
	};

	dre = dre.prototype = {
		/**
		 * 
		 */
		advise: function(jsonData, callbackSuccess) {
			var method = 'POST';
			var _url = url + '/advise';
			if (jsonData.user == undefined) {
				jsonData.user = {};
			}
			var cookie = getCookie('dre_user_id');
			jsonData.user.id = cookie;

			if (jQuery.browser.msie && window.XDomainRequest) {
				var dataToSend = JSON.stringify(jsonData);
			    // Use Microsoft XDR
			    var xdr = new XDomainRequest();
				//xdr.contentType = 'text/plain';
			    xdr.open(method, _url);
			    xdr.onload = function () {
			    	var JSON = jQuery.parseJSON(xdr.responseText);
			    	if (JSON == null || typeof (JSON) == 'undefined') {
			        	JSON = jQuery.parseJSON(data.firstChild.textContent);
			    	}
			    	preCallback(JSON, "no-text", xdr);
			    };
				xdr.onerror = function() {
					//alert("error: " + xdr.responseText);
				};
			    xdr.send(dataToSend);

			} else {
				var dataToSend = JSON.stringify(jsonData);
			    jQuery.ajax({
			    	type: method,
			    	url: _url,
			    	processData: true,
					contentType: 'application/json; charset=UTF-8',
			    	data: dataToSend,
					async: true,
			    	dataType: "json",
					//crossDomain: true,
					xhrFields: {withCredentials: true},
			    	success: function (data, textStatus, jqXHR) { preCallback(data, textStatus, jqXHR); },
					error: function( jqXHR, textStatus, errorThrown ) {
							//alert(errorThrown);
								window.console && console.log(textStatus + errorThrown);
			    				//jQuery('#result').append('Request failed: ' + 'textStatus: ' + textStatus +' errorThrown: ' + errorThrown);
			  	    }
				});
			}
			
			var preCallback = function(data, textStatus, jqXHR) {
				if (data.user != undefined) {
					var userId = data.user.id;
					if (userId != getCookie('dre_user_id')) {
					setCookie('dre_user_id', userId, 730);
					}
				}
				callbackSuccess(data, textStatus, jqXHR)
			}
		}
		
    };

window.dre = dre;

})(window);

/**
 * Make redirect, append cookie and the redirect page to make after cookie set
 */
function doRedirect() {
	location.href = url + "/synchCookie/"
			+ getCookie('dre_user_id') + "?redirectTo=" + encodeURIComponent(document.URL);
}

/**
 * Set Cookie
 * @param c_name name of cookie
 * @param value the value of cookie
 * @param exdays expire time in day
 */
function setCookie(c_name, value, exdays) {
	var exdate = new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value = escape(value)
			+ ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
	document.cookie = c_name + "=" + c_value;
}

/**
 * Return Cookie identify by c_name
 * @param c_name
 * @returns the value of cookie or undefined
 */
function getCookie(c_name) {
	var c_value = document.cookie;
	var c_start = c_value.indexOf(" " + c_name + "=");
	if (c_start == -1) {
		c_start = c_value.indexOf(c_name + "=");
	}
	if (c_start == -1) {
		c_value = null;
	} else {
		c_start = c_value.indexOf("=", c_start) + 1;
		var c_end = c_value.indexOf(";", c_start);
		if (c_end == -1) {
			c_end = c_value.length;
		}
		c_value = unescape(c_value.substring(c_start, c_end));
	}
	return c_value;
}

/**
 * Simple function for generate a unique Uid
 */
var generateUid = function(separator) {
	var delim = separator || "-";
	function S4() {
		return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
	}
	return (S4() + S4() + delim + S4() + delim + S4() + delim + S4() + delim
			+ S4() + S4() + S4());
}

//document.write('<scr'+'ipt type="text/javascript" src="http://code.jquery.com/jquery-1.8.2.min.js" ></scr'+'ipt>');

/**
 * Make redirect if cookie is not set, else ignore
 */
if (getCookie('dre_user_id') == undefined) {
	var id = generateUid();
	setCookie('dre_user_id', id, 40);
	//window.setTimeout("doRedirect()", 0);
	doRedirect();
}

