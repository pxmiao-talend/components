package org.talend.components.fileinput.tFileInputDelimited;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.fileinput.FileInputDefinition;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TFileInputDelimitedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TFileInputDelimitedDefinition extends FileInputDefinition implements InputComponentDefinition{
	
	public static final String COMPONENT_NAME = "tFileInputDelimited"; //$NON-NLS-1$
	
	 public TFileInputDelimitedDefinition() {
	        super(COMPONENT_NAME);
	    }

	@Override
	public Source getRuntime() {
		// TODO Auto-generated method stub
		return null;
	}
		
}
