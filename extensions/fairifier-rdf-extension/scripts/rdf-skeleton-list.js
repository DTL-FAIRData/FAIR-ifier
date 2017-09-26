function RdfSkeletonListDialog(func){
    this._createDialog();
    this.func = func;
    this._rdfSkeleton = null;
};

RdfSkeletonListDialog.prototype._createDialog = function() {
    var self = this;
    var frame = DialogSystem.createDialog();
    
    frame.width("800px");
    
    var header = $('<div></div>').addClass("dialog-header").text("RDF Skeleton List").appendTo(frame);
    var body = $('<div></div>').css('height', '150px').addClass("dialog-body").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    this._constructFooter(footer);
    this._constructBody(body);
    this._level = DialogSystem.showDialog(frame);
};

RdfSkeletonListDialog.prototype._constructFooter = function(footer) {
    var self = this;
    
    $('<button></button>').addClass('button').html("&nbsp;&nbsp;Load&nbsp;&nbsp;").click(function() {
        $.post("command/rdf-extension/load-rdf-skeleton",  {projectId : $( "select :selected" ).val()},function(data){
            self.func(data);
            DialogSystem.dismissUntil(self._level - 1);
        });
    }).appendTo(footer);
    
    $('<button></button>').addClass('button').text("Cancel").click(function() {
        DialogSystem.dismissUntil(self._level - 1);
    }).appendTo(footer);
};

RdfSkeletonListDialog.prototype._constructBody = function(body) {
    var self = this;
    $('<p>' +
            'Select a RDF skeleton saved earlier to apply to your data.' +
        '</p>').appendTo(body);
    
   var container = $('<div id="body" style="display:block;">');
    var html = $('<div id="left-panel">');
    $.post("command/rdf-extension/list-rdf-skeletons",function(data){
       console.log(data);
       var list = $('<select multiple width="300" style="width: 300px">');
        var data = data['list'];
        for (var i = 0; i < data.length; i++){
           $('<option id='+ i +' value="' + data[i] + '">' + data[i] + '</option>').appendTo(list);
           $('option#' + i).click(function(evt){
               evt.preventDefault();
               $("p.preview").text(evt.target.value);
           });
        }
        $('</select>').appendTo(list);
        list.appendTo(html);
    });
    $('</div>').appendTo(html);
    $(html).appendTo(container);
    $('<div id="right-panel"><p class="preview"></p></div></div>').appendTo(container);
    $(container).appendTo(body);
};