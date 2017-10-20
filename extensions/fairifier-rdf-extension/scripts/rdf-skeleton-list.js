var listData = {};

function RdfSkeletonListDialog(func, schema, project, baseuri){
    this._createDialog();
    this.func = func;
    this._schema = schema;
    this._project = project;
    this._rdfSkeleton = null;
    this._baseuri = baseuri;
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
        self._schema['baseUri'] = self._baseuri;
        self._schema = JSON.stringify(self._schema);
        $.post("command/rdf-extension/load-rdf-skeleton",  {
                project: self._project,
                schema : self._schema,
                projectId : listData[$( "select: selected" ).attr('id')].project
            },function(data){
                self.func(data);
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
               var skeleton = JSON.parse(data[event.target.id].skeleton);
               $("<h2>prefixes</h2>").appendTo(".preview")
               var prefList = $("<ul>");
               for (var x = 0; x < skeleton.prefixes.length; x++){
                   $("<li>").html(skeleton.prefixes[x].name + ' - ')
                   .append($("<a>").html(skeleton.prefixes[x].uri))
                   .attr("href", skeleton.prefixes[x].uri)
                   .appendTo(prefList);
               }
               $("</ul>").appendTo(prefList);
               prefList.appendTo(".preview");
               $("<h2>properties</h2>").appendTo(".preview")
               var propList = $("<ul>");
               for (var z = 0; z < skeleton.rootNodes.length; z++){
                   for (var y = 0 ; y < skeleton.rootNodes[z].links.length; y++){
                       $("<li>").html(skeleton.rootNodes[z].links[y].target.columnName + ' - ')
                           .append($("<a>").html(skeleton.rootNodes[z].links[y].uri))
                           .attr("href",  skeleton.rootNodes[z].links[y].uri)
                           .appendTo(propList);
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

};