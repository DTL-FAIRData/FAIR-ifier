var listData = {};

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
    var body = $('<div></div>').css('height', '250px').addClass("dialog-body").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    this._constructFooter(footer);
    this._constructBody(body);
    this._level = DialogSystem.showDialog(frame);
};

RdfSkeletonListDialog.prototype._constructFooter = function(footer) {
    var self = this;
    
    $('<button></button>').addClass('button').html("&nbsp;&nbsp;Load&nbsp;&nbsp;").click(function() {
        $.post("command/rdf-extension/load-rdf-skeleton",  {projectId : listData[$( "select :selected" ).attr('id')].project},function(data){
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
    var html = $('<div style="float:left;">');
    $.post("command/rdf-extension/list-rdf-skeletons",function(data){
       var list = $('<select multiple width="300" style="width: 300px;">');
        var data = data.list
        listData = data;
        for (var i = 0; i < data.length; i++){
           var element = data[i];
           var option = $('<option id='+ i + '>' + element.name + '</option>');
           
           option.click(function(evt){
               evt.preventDefault();
               $(".preview").html('');
               console.log(data[event.target.id].skeleton);
               var skeleton = JSON.parse(data[event.target.id].skeleton);
               $("<h2>prefixes</h2>").appendTo(".preview")
               var prefList = $("<ul>");
               for (var x = 0; x < skeleton.prefixes.length; x++){
                   $("<li>" + skeleton.prefixes[x].name + 
                           " - <a href='" + skeleton.prefixes[x].uri + "'>"  
                           + skeleton.prefixes[x].uri + 
                           "</a></li>").appendTo(prefList);
               }
               $("</ul>").appendTo(prefList);
               prefList.appendTo(".preview");
               $("<h2>properties</h2>").appendTo(".preview")
               var propList = $("<ul>");
               for (var z = 0; z < skeleton.rootNodes.length; z++){
                   for (var y = 0 ; y < skeleton.rootNodes[z].links.length; y++){
                       $("<li>" + skeleton.rootNodes[z].links[y].target.columnName + 
                               " - <a href=" + skeleton.rootNodes[z].links[y].uri + ">" + 
                               skeleton.rootNodes[z].links[y].uri + 
                               "</a></li>").appendTo(propList);
                   }
               }
               $("</ul>").appendTo(propList);
               propList.appendTo(".preview");
           });
           option.appendTo(list);
        }
        $('</select>').appendTo(list);
        list.appendTo(html);
    });
    $('</div>').appendTo(html);
    $(html).appendTo(container);
    $('<div class="preview" style="overflow:auto;height:75%;float:right;"></div></div>').appendTo(container);
    $(container).appendTo(body);

//TODO: add nested node retrieval to preview (should be recursive solution)
//    function getPropertiesOfNode(node){
//        
//    }
};