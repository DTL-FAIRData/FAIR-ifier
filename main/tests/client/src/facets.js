var test_facets = new function() {

    // TODO: if I'm in a project, go back to main page (useful when working on this test)
    //if () { 
        //go back
    //};
    
    // test opening Food project
    test = newTest();
    assert (test, "assertText", { id:"slogan", validator: "A power tool for working with messy data." });
    this.test_home_page = test;
    
    // make sure the dataset was loaded properly
    test = newTest();
    action (test, "click",        { link:    "Food" });
    wait   (test, "forPageLoad",  { timeout: "20000" });
    assert (test, "assertText", {xpath: "//div[@id='summary-bar']/span",validator: "7413 rows"});
    this.test_open_project = test;

    // create text facet from 1st word of Short Description column
    test = newTest();
    action (test, 'click',       { jquery: '("td:contains(\'Shrt_Desc\') .column-header-menu")[0]' });
    action (test, 'mouseOver',   { jquery: '("td:contains(\'Facet\')")[0]' });
    wait   (test, "forMenuItem", { name: "Custom text facet.."});
    action (test, "click",       { jquery: '("a.menu-item:contains(\'Custom text facet...\')")[0]' });
    action (test, "type",        { jquery: '(".expression-preview-code")[0]', text: "value.split(',')[0]" });
    wait   (test, "forElement",  { jquery: '("td:contains(\'value.split\')")[0]' });
    action (test, "click",       { jquery: '("button:contains(\'OK\')")[0]' });
    wait   (test, "forAjaxEnd");
    assert (test, "expectedTopFacetValue", "ABALONE");
    this.test_facet = test;
        
    // sort the facet by count and test the result
    test = newTest();
    action (test, "click",      { jquery: '("a.action:contains(\'count\')")[0]' });
    wait   (test, "forElement", { jquery: '("a.selected:contains(\'count\')")[0]' }); // wait til count is the active sort
    assert (test, "expectedTopFacetValue", "BEEF");
    this.test_sort_text_facet = test;
        
    // filter down to BEEF
    test = newTest();
    action (test, "click",      { jquery: '("a.facet-choice-label")[0]' });
    assert (test, "rowCount", "457");
    this.test_filter_text_facet = test;

    //test base uri
    test = newTest();
    action (test, 'click',        {link:  'RDF'});
    wait   (test, "forElement",   {jquery: '("a.menu-item:contains(\'Reset Semantic Model...\')")'});
    action (test, 'click',        {link: 'Reset Semantic Model...'});
    assert (test, function(){
        jum.assertNotUndefined($("span:contains('" + window.location.protocol + "//" + window.location.host + "/')")[0]);
    });
    this.test_default_base_uri = test;

    //test python scripting in semantic model
    test = newTest();
    action (test, 'click', {jquery: '("a.schema-alignment-node-tag")[0]'});
    action (test, 'click', {jquery: '("a:contains(\'preview/edit\')")[0]'});
    action (test, "type",        { jquery: '("textarea.expression-preview-code")[0]', text: "return baseUri" });
    assert (test, function(){
        jum.assertNotUndefined($("td.expression-preview-value:contains(\'"+$("span[bind='baseUriSpan']").text()+"\')").text());
    });
    this.test_python_scripting = test;
    
    test = newTest();
    action (test, 'click',      {jquery: '("button.button:contains(\'OK\')")[0]'});
    action (test, 'click',        {link:  'RDF'});
    action (test, 'click',        {link: 'Edit Semantic Model...'});
    action (test, 'click',        {link: 'add prefix'});
    action (test, 'click',        {jquery: '("button#advanced_options_button")[0]'});
    action (test, 'type',         {jquery: '("input[bind=\'prefix\']")[0]', text: "dc"});
    action (test, 'type',         {jquery: '("input[bind=\'uri\']")[0]', text: 'http://purl.org/dc/elements/1.1/'});
    action (test, 'click',        {jquery: '("button.button:contains(\'OK\')")[1]'});
    wait   (test, "forAjaxEnd");
    action (test, "click", {jquery: '("button.button:contains(\'OK\')")[0]'});
    this.test_uploading_ontology_from_web  = test;

    // export to FAirDataPoint
    test = newTest();
    action (test, "click",      {jquery: '("a#export-button")[0]' });
    action (test, "click",      { link: 'POST to FAIR Data Point' });
    action (test, "click",      { jquery: '("a:contains(\'edit\')")[0]' });
    action (test, "type",      {jquery: '("input[bind=\'newBaseUri\']")[0]', text: "http://localhost:8080/fdp"});
    action (test, 'click', {jquery:'("button.button[bind=\'applyButton\']")[0]'});
    wait   (test, "forElement",   {jquery: '("select.catalogs")'});
    action   (test, "click",   {link: '+' });
    action (test, 'type', {jquery: '("input#dct-title0")[0]', text: "Integration test catalog"})
    action (test, 'type', {jquery: '("input#dct-hasversion3")[0]', text: "1.0"})
    action (test, 'type', {jquery: '("input#dct-publisher4")[0]', text: "http://fake-publisher.com"})
    action (test, 'type', {jquery: '("input#institute-foaf-name0")[0]', text: "fake-publisher"})
    action (test, 'type', {jquery: '("input#dcat-themetaxonomy12")[0]', text: "http://test.nl"})
    action   (test, "click",   {jquery: '("input#submit[value=\'Save\']")[0]' });
    
    wait   (test, "forElement",   {jquery: '("a:contains(\'+\')")[1]'});
    action   (test, "click",   {jquery: '("a:contains(\'+\')")[1]' });
    action (test, 'type', {jquery: '("input#dct-title0")[0]', text: "Intergration test dataset"});
    action (test, 'type', {jquery: '("input#dct-hasversion3")[0]', text: "1.0"});
    action (test, 'type', {jquery: '("input#dct-publisher4")[0]', text: "http://fake-publisher.com"});
    action (test, 'type', {jquery: '("input#institure-foaf-name0")[0]', text: "fake-publisher"});
    action (test, 'type', {jquery: '("input#dcat-theme0")[0]', text: "http://www.wikidata.org/entity/Q27318"})
    action   (test, "click",   {jquery: '("input#submit[value=\'Save\']")[0]' });

	wait   (test, "forElement",   {jquery: '("a:contains(\'+\')")[2]'});
    action   (test, "click",   {jquery: '("input.virtuosoRadio")[0]' });    
	action   (test, "click",   {jquery: '("a:contains(\'+\')")[2]' });
    action (test, 'type', {jquery: '("input#dct-title0")[0]', text: "Intergration test dataset"});
    action (test, 'type', {jquery: '("input#dct-license3")[0]', text: "http://purl.org/NET/rdflicense/ukogl3.0"});
    action (test, 'type', {jquery: '("input#dct-hasversion4")[0]', text: "1.0"});
    action   (test, "click",   {jquery: '("input#submit[value=\'Save\']")[0]' });
    action   (test, "click",   {jquery: '("button.button:contains(\'OK\')")[0]' });
    action (test, 'open',{url:'http://localhost:8080/fdp'});
    action (test, 'click',{jquery:'("a")[1]'});
   
    this.test_export_to_fdp = test;	


    // create numeric filter from Water column
    // test = newTest();
    // action (test, "click",       { jquery: '("td:contains(\'Water\') .column-header-menu")[0]' });
    // action (test, "mouseOver",   { jquery: '("td:contains(\'Facet\')")[0]' });
    // wait   (test, "forMenuItem", {   name: 'Numeric facet' });
    // action (test, "click",       { jquery: '(".menu-item:contains(\'Numeric facet\')")[0]' });
    // wait   (test, "forAjaxEnd");
    // assert (test, function() {
    //     jum.assertTrue($(".facet-panel span:contains('Water')").length > 0);
    // });
    // this.test_create_numeric_facet = test;

    // filter out BEEF with lower water content
    // test = newTest();
    // wait   (test, "forAjaxEnd"); 
    // wait   (test, "forElement",   { jquery: '((".slider-widget-draggable.left"))[0]' }),
    // action (test, "dragDropElem", { jquery: '((".slider-widget-draggable.left"))[0]', pixels: '150, 0' });
    // wait   (test, "forAjaxEnd"); // <--- FIXME for some reason the range faceting doesn't seem to be triggering that
    // assert (test, "rowCount", "153");
    // this.test_filter_numeric_facet = test;
    
};
