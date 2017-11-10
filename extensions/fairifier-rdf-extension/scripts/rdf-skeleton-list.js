function RdfSkeletonListDialog(func_a, func_b, schema, project, baseuri) {
  this._createDialog();
  this._overwrite_func = func_a;
  this._append_func = func_b;
  this._schema = schema;
  this._project = project;
  this._rdfSkeleton = null;
  this._baseuri = baseuri;
  this._nodeUIs = [];
};

RdfSkeletonListDialog.prototype._createDialog = function() {
  var self = this;
  var frame = DialogSystem.createDialog();
  
  frame.width("1000px");
  
  var header = $('<div></div>').addClass("dialog-header").text("RDF Skeleton List").appendTo(frame);
  var body = $('<div></div>').css('height', '450px').addClass("dialog-body").appendTo(frame);
  var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
  
  this._constructFooter(footer);
  this._constructBody(body);
  this._level = DialogSystem.showDialog(frame);
};

RdfSkeletonListDialog.prototype._constructFooter = function(footer) {
  var self = this;
  
  $('<button>').addClass('button').html("&nbsp;&nbsp;Load&nbsp;&nbsp;").click(function() {
    if (typeof self._schema !== 'undefined') {
      self._schema['baseUri'] = self._baseuri;
    } else {
      self._schema = {
        'baseuri': self._baseuri
      };
    }
    $.post("command/rdf-extension/load-rdf-skeleton", {
      project: self._project,
      schema: JSON.stringify(self._schema),
      projectId: self.listData[$("select").find(":selected").attr('id')].project
    }, function(data) {
      if ($("#loadOverwrite").is(':checked')) {
        self._overwrite_func(data);
      } else if ($("#loadAppend").is(':checked')) {
        self._append_func(data);
      }
    });
  }).appendTo(footer);
  
  $('<button></button>').addClass('button').text("Cancel").click(function() {
    DialogSystem.dismissUntil(self._level - 1);
  }).appendTo(footer);
};

RdfSkeletonListDialog.prototype._renderPreview = function(schema) {
  var self = this;
  this._canvas = $(".schema-list-dialog-canvas");
  $("table.schema-list-table-layout").remove();
  this._nodeTable = $('<table>').addClass("rdf-schema-list-table-layout").appendTo(this._canvas).prevObject[0];
  
  for (var i = 0; i < schema.rootNodes.length; i++) {
    var element = new RdfSchemaAlignmentDialog.UINode(this, schema.rootNodes[i], this._nodeTable, {
      expanded: true
    });
    this._nodeUIs.push(element);
  }
  $(".rdf-schema-list-table-layout").find('*').unbind('click');
  
};

RdfSkeletonListDialog.prototype._constructBody = function(body) {
  self = this;
  $('<p>').text("Select a RDF skeleton saved earlier to apply to your data.").appendTo(body);
  
  var container = $('<div>').css('display', 'block').css('overflow', 'hidden').attr('id', 'body');
  var horizontalcontainer = $('<div>').css('display', 'block');
  var html = $('<div>').css('float', 'left');
  $.post("command/rdf-extension/list-rdf-skeletons", function(data) {
    var list = $('<select>').attr('width', 300).attr('multiple', '').css('width', '300px');
    var data = data.list
    self.listData = data;
    for (var i = 0; i < data.length; i++) {
      var element = data[i];
      var option = $('<option>').attr('id', i).text(element.name);
      
      option.click(function(evt) {
        evt.preventDefault();
        $(".metadatapreview").html("<h3>prefixes:</h3>");
        $(".preview").html('<div class="schema-list-dialog-canvas rdf-kust-dialog-canvas"></div>');
        var links = $("<ul>").css("margin-left", "20px");
        var skeleton = JSON.parse(data[event.target.id].skeleton);
        for (var i = 0; i < skeleton.prefixes.length; i++) {
          var text = skeleton.prefixes[i].name + " - ";
          var link = "<a href='" + skeleton.prefixes[i].uri + "'>" + skeleton.prefixes[i].uri + "</a>";
          link = $("<li>").html(text + link);
          links.append(link);
        }
        $(".metadatapreview").append(links);
        self._renderPreview(skeleton);
      });
      option.appendTo(list);
    }
    $('</select>').appendTo(list);
    list.appendTo(html);
    $("<br/>").appendTo(html);
    $("<input>", {
      type: "radio",
      name: "loadMode",
      id: "loadOverwrite"
    }).appendTo(html);
    $("<label>", {
      'for': "loadOverwrite",
      text: "Overwrite existing model",
    }).appendTo(html);
    $("<input>", {
      type: "radio",
      name: "loadMode",
      id: "loadAppend",
    }).appendTo(html);
    $("<label>", {
      'for': "loadAppend",
      text: "Append to existing model",
    }).appendTo(html);
  });
  $('</div>').appendTo(html);
  $(html).appendTo(horizontalcontainer);
  $('<div class="metadatapreview" style="overflow: auto; height: 100px;"></div>').appendTo(horizontalcontainer);
  $(horizontalcontainer).appendTo(container);
  $(container).appendTo(body);
  $("<hr/><h3>preview:</h3>").appendTo(body);
  $('<div class="preview" style="overflow: scroll; height:250px;margin-top:50px"></div></div>').appendTo(body)
};