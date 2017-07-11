function NewPrefixWidget(manager){
	this._prefixesManager = manager;
}

NewPrefixWidget.prototype.show = function(msg,def_prefix,onDone){
	var self = this;
    var frame = DialogSystem.createDialog();
    
    frame.width("400px");

    var html = $(DOM.loadHTML("rdf-extension","scripts/new-prefix-widget.html"));
    
    var header = $('<div></div>').addClass("dialog-header").text("New Prefix").appendTo(frame);
    var body = $('<div class="grid-layout layout-full"></div>').addClass("dialog-body").append(html).appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    self._elmts = DOM.bind(html);
    if(msg){
    	self._elmts.message.addClass('message').html(msg);
    }
    if(def_prefix){
    	self._elmts.prefix.val(def_prefix);
    	self.suggestUri(def_prefix);
    }
    
    var importVocabulary = function(fetchOption,onDone){
    	var name = self._elmts.prefix.val();
    	var uri = self._elmts.uri.val();
    	if(self._prefixesManager._hasPrefix(name)){
    		alert('Prefix "' + name + '" is already defined');
    		return;
    	}
    	var dismissBusy;

    	if(fetchOption==='file'){
    		//prepare values
    		$('#vocab-hidden-prefix').val(name);
    		$('#vocab-hidden-uri').val(uri);
    		$('#vocab-hidden-project').val(theProject.id);
                var data = new FormData($("form")[0]);
    		dismissBusy = DialogSystem.showBusy('Uploading vocabulary ');


            $('<input>').attr('type','hidden').attr('name','baseuri').val("").appendTo("#file-upload-form");

            self._elmts.file_upload_form.ajaxSubmit({
                dataType: 'json',
                type: 'post',
                url: "command/rdf-extension/detect-format-service",
                success: function(format){
                    data.append('file_format',format['RDFFormat']);
                    console.log(format);
                    if(!format['RDFFormat']){
                        dismissBusy();
                        alert('file format could not be detected, only turtle, rdfxml and ntripple are supported')
                        return;
                    }else{
                          $.ajax({
                            url : "command/rdf-extension/upload-file-add-prefix", 
                            data: data,
                            cache: true,
                            contentType: false,
                            processData: false,
                            enctype: 'multipart/form-data',
                            type: 'POST',
                            success: function(result){
                                if (result.code === "error"){
                                    alert('Error:' + result.message)
                                }else{
                                    DialogSystem.dismissUntil(level - 1);
                                    if(onDone){
                                        onDone(name,uri);
                                    }
                                }
                                dismissBusy();
                            }
                        });
                        return ;
                    }
                }
            });
    	}else{
            dismissBusy = DialogSystem.showBusy('Trying to import vocabulary from ' + uri);
            $.post("command/rdf-extension/add-prefix",{name:name,uri:uri,"fetch-url":uri,project: theProject.id,fetch:fetchOption},function(data){
                if (data.code === "error"){
                    alert('Error:' + data.message)
                }else{
                    DialogSystem.dismissUntil(level - 1);
                    if(onDone){
                        onDone(name,uri);
                    }
                }
                dismissBusy();
            });
            return ;
    	}
    };
    
    $('<button></button>').addClass('button').html("&nbsp;&nbsp;OK&nbsp;&nbsp;").click(function() {
    	var fetchOption =  $('input[name="vocab_fetch_method"]:checked').val();
    	importVocabulary(fetchOption,onDone);
    }).appendTo(footer);
    
    $('<button></button>').addClass('button').text("Cancel").click(function() {
        DialogSystem.dismissUntil(level - 1);
    }).appendTo(footer);
    
    $('<button></button>').attr('id','advanced_options_button').attr("style","float:right").text("Advanced...").click(function() {
        self._elmts.fetching_options_table.show();
        // $('#advanced_options_button').attr("disabled", "disabled");
    }).appendTo(footer);
    
    
    
    var level = DialogSystem.showDialog(frame);
    
    
    self._elmts.fetching_options_table.find('.upload_file_inputs').attr('disabled',true);
    
    self._elmts.fetching_options_table
	.hide()
	.find('input[name="vocab_fetch_method"]').click(function(){
		var upload = $(this).val()!=='file';
		self._elmts.fetching_options_table.find('.upload_file_inputs').attr('disabled',upload);
                $('input[bind="uri"]').attr("disabled", false);
		if ($(this).val() === 'file'){
		    $('input[bind="uri"]').attr("disabled", "disabled");
		}
	});
    
    self._elmts.prefix.bind('change',function(){
    	self.suggestUri($(this).val());
    	}).focus();
};

NewPrefixWidget.prototype.suggestUri = function(prefix){
	var self = this;
	$.get("command/rdf-extension/get-prefix-cc-uri",{prefix:prefix},function(data){
		if(!self._elmts.uri.val() && data.uri){
			self._elmts.uri.val(data.uri);
			if(self._elmts.message.text()){
				self._elmts.message.find('div').remove().end().append($('<span>(a suggestion from <em><a target="_blank" href="http://prefix.cc">prefix.cc</a></em> is provided)</span>'));
			}else{
				self._elmts.uri_note.html('(suggested by <a target="_blank" href="http://prefix.cc">prefix.cc</a>)');
			}
		}
	},"json");
};
