package org.talend.components.fileinput.tFileInputDelimited;

import org.talend.components.fileinput.FileInputProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class TFileInputDelimitedProperties extends FileInputProperties{

	public TFileInputDelimitedProperties(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public enum csvRowSeparator(){
		LF("\n");
		CR("\r");
		CRLF("\r\n");
	}
	public Property<String> rowSeparator = newProperty("rowSeparator");
	public Property<String> fieldSeparator = newProperty("fieldSeparator");
	public Property<Boolean> csvOpertions = newProperty("csvOpertions");
	public Property<String> escapeChar = newProperty("escapeChar");
	public Property<String> textEnclosure = newProperty("textEnclosure");
	public Property<Integer> head = newInteger("head");
	public Property<Integer> foot = newInteger("foot");
	public Property<Integer> limit = newInteger("limit");
	public Property<Boolean> removeEmptyRow = newBoolean("removeEmptyRow");
	public Property<Boolean> uncompress = newBoolean("uncompress");
	public Property<Boolean> dieOnError = newBoolean("dieOnError");
	
	//Advanced
	public enum encodingType(){
		ISO-8859-15;
		UTF-8;
		CUSTOM;
	}
	public Property<Boolean> advancedSeparator = newBoolean("AdvancedSeparator");
	public Property<String> thousandsSeparator = newProperty("thousandsSeparator");
	public Property<String> decimalSeparator = newProperty("decimalSeparator");
	public Property<Integer> nbRandom = newInteger("nbRandom");
	public Property<Boolean> random = newBoolean("random");
	//public Property<encodingType> csvOpertions = newEnum("csvOpertions");
	public Property<Boolean> trimall = newBoolean("trimall");
	public Property<Boolean> checkFieldsNum = newBoolean("checkFieldsNum");
	public Property<Boolean> checkDate = newBoolean("checkDate");
	public Property<Boolean> splitRecord = newBoolean("splitRecord");
	public Property<Boolean> enableDecode = newBoolean("enableDecode");
	public Property<Boolean> tStatCatcherStats = newBoolean("tStatCatcherStats");
	
	 @Override
	    public void setupProperties() {
	        super.setupProperties();
	        rowSeparator.setValue("\n");
	        fieldSeparator.setValue(";");
	        escapeChar.setValue("\"");
	        textEnclosure.setValue("\"");
	        head.setValue(0);
	        foot.setValue(0);
	        thousandsSeparator.setValue(",");
	        decimalSeparator.setValue(".");
	        nbRandom.setValue(10);
	    }
	 
	 @Override
	    public void setupLayout() {
	        super.setupLayout();
	        Form form = Form.create(this, Form.MAIN);
	        form.addRow(schema.getForm(Form.REFERENCE));
	        form.addRow(filename);
	        form.addRow(rowSeparator);
	        form.addRow(fieldSeparator);
	        form.addRow(csvOpertions);
	        form.addRow(head);
	        form.addRow(foot);
	        form.addRow(limit);
	        form.addRow(removeEmptyRow);
	        form.addRow(uncompress);
	        form.addRow(dieOnError);
	        
	        Form advancedForm = Form.create(this,Form.ADVANCED);
	        advancedForm.addRow(advancedSeparator);
	        advancedForm.addRow(random);
	        advancedForm.addRow(trimall);
	        advancedForm.addRow(checkFieldsNum);
	        advancedForm.addRow(checkDate);
	        advancedForm.addRow(splitRecord);
	        advancedForm.addRow(enableDecode);
	        advancedForm.addRow(tStatCatcherStats);
	    }
	 
	 	@Override
	    public void refreshLayout(Form form) {
	        super.refreshLayout(form);
	        if (form.getName().equals(Form.MAIN)) {
	            form.getWidget(foot.getName()).setHidden(!uncompress.getValue());
	            form.getWidget(rowSeparator.getName()).setHidden(csvOpertions.getValue());
	            form.getWidget(fieldSeparator.getName()).setHidden(csvOpertions.getValue());
	        }
	        if (Form.ADVANCED.equals(form.getName())) {
	            form.getWidget(thousandsSeparator.getName()).setHidden(!advancedSeparator);
	            form.getWidget(decimalSeparator.getName()).setHidden(!advancedSeparator);
	        }
	    }
	 	
	 	@Override
	    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
	        if (isOutputConnection) {
	            return Collections.singleton(MAIN_CONNECTOR);
	        } else {
	            return Collections.EMPTY_SET;
	        }
	    }
}
