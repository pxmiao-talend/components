package org.talend.components.fileinput.tFileInputDelimited;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.fileinput.FileInputProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TFileInputDelimitedProperties extends FileInputProperties{

	public TFileInputDelimitedProperties(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/*public enum csvRowSeparator(){
		LF("\n");
		CR("\r");
		CRLF("\r\n");
	}*/
	public Property<String> rowSeparator = PropertyFactory.newProperty("rowSeparator");
	public Property<String> fieldSeparator = PropertyFactory.newProperty("fieldSeparator");
	public Property<Boolean> csvOpertions = PropertyFactory.newBoolean("csvOpertions");
	public Property<String> escapeChar = PropertyFactory.newProperty("escapeChar");
	public Property<String> textEnclosure = PropertyFactory.newProperty("textEnclosure");
	public Property<Integer> head = PropertyFactory.newInteger("head");
	public Property<Integer> foot = PropertyFactory.newInteger("foot");
	public Property<Integer> limit = PropertyFactory.newInteger("limit");
	public Property<Boolean> removeEmptyRow = PropertyFactory.newBoolean("removeEmptyRow");
	public Property<Boolean> uncompress = PropertyFactory.newBoolean("uncompress");
	public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");
	
	//Advanced
	/*public enum encodingType(){
		ISO-8859-15;
		UTF-8;
		CUSTOM;
	}*/
	public Property<Boolean> advancedSeparator = PropertyFactory.newBoolean("advancedSeparator");
	public Property<String> thousandsSeparator = PropertyFactory.newProperty("thousandsSeparator");
	public Property<String> decimalSeparator = PropertyFactory.newProperty("decimalSeparator");
	public Property<Integer> nbRandom = PropertyFactory.newInteger("nbRandom");
	public Property<Boolean> random = PropertyFactory.newBoolean("random");
	//public Property<encodingType> csvOpertions = newEnum("csvOpertions");
	public Property<Boolean> trimall = PropertyFactory.newBoolean("trimall");
	public Property<Boolean> checkFieldsNum = PropertyFactory.newBoolean("checkFieldsNum");
	public Property<Boolean> checkDate = PropertyFactory.newBoolean("checkDate");
	public Property<Boolean> splitRecord = PropertyFactory.newBoolean("splitRecord");
	public Property<Boolean> enableDecode = PropertyFactory.newBoolean("enableDecode");
	public Property<Boolean> tStatCatcherStats = PropertyFactory.newBoolean("tStatCatcherStats");
	
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
	            form.getWidget(thousandsSeparator.getName()).setHidden(!advancedSeparator.getValue());
	            form.getWidget(decimalSeparator.getName()).setHidden(!advancedSeparator.getValue());
	        }
	    }
	 	
	 	@Override
	    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
	        if (isOutputConnection) {
	            return Collections.singleton(mainConnector);
	        } else {
	            return Collections.EMPTY_SET;
	        }
	    }
}
