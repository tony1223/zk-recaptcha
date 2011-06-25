var ren = function(){
	this.rerender();
}
zul.recaptcha.Recaptcha = zk.$extends(zk.Widget, {
    _theme:'red', 
	_type:'image',
 	$define: {
		theme: ren,
		publickey: ren,
		type: function(val){
			if(this._repInit){
				Recaptcha.switch_type(val);
			}
		}
	},
	initRecaptcha:function(){
		if(this._publickey == null )
			zk.error("reCaptcha public key is null.");
		
		var wgt = this;
		this._repInit = true;
		Recaptcha.create(this._publickey,
		    this.uuid,
		    {
		      theme: this._theme,
		      callback: function() {
			  	if(wgt._type != 'image')
					Recaptcha.switch_type(wgt._type);
					
			  	jq("[name=recaptcha_response_field]",wgt.$n())
					.change(function(){
					wgt.fire("onVerifying",
						{
							challenge:Recaptcha.get_challenge(),
							response:Recaptcha.get_response()
						}
					);
				});

				/* left this to face 2 				
				jq("#recaptcha_image img").load(function(){
					jq("#recaptcha_response_field").attr("disabled","");
				});
				*/
			  }
		    }
	 	 );
	},
	focus: function(){
		Recaptcha.focus_response_field();
	},
	showValid: function(){
		/* left this to face 2 
		jq("#recaptcha_response_field").attr("disabled","disabled");
		*/
	},
	bind_: function () {
		this.$supers(zul.recaptcha.Recaptcha,'bind_', arguments);
		
		//NOTE: This might call very soon , also might very late.
		var wgt = this;
		zul.recaptcha.Recaptcha.loadApi(function(){
			wgt.initRecaptcha();
		});
		
	},
	/**
	 * reload captcha image.
	 */
	reload: function () {
		if(Recaptcha) Recaptcha.reload();
	},
	unbind_: function () {
		if(this._repInit)
			Recaptcha.destroy();
		
		this.$supers(zul.recaptcha.Recaptcha,'unbind_', arguments);
	},

	getZclass: function () {
		return this._zclass != null ? this._zclass: "z-recaptcha";
	}
},{
	timer:null,
	queue:[],
	loadApi:function(fn){
		if(window.Recaptcha == null){
			zul.recaptcha.Recaptcha.queue.push(fn);
			
			if (zul.recaptcha.Recaptcha.timer == null) {
				zk.loadScript('http://www.google.com/recaptcha/api/js/recaptcha_ajax.js');
				zul.recaptcha.Recaptcha.timer = setInterval(function() {
					if (window.Recaptcha != null) {
						clearInterval(zul.recaptcha.Recaptcha.timer);
						var _fn , queue = zul.recaptcha.Recaptcha.queue;
						while (_fn = queue.shift()) {
							_fn();
						}
					}
				}, 1000);
			}
		}else
			fn();
		
	}
});
