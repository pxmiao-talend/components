package org.talend.components.cassandra;

import org.talend.components.api.component.runtime.Source;
import org.talend.components.cassandra.runtime.CassandraSourceOrSink;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import java.io.IOException;
import java.util.List;

import static org.talend.daikon.properties.property.PropertyFactory.newString;


public class CassandraSchemaProperties extends ComponentPropertiesImpl {

    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public CassandraSchemaProperties(String name, CassandraConnectionProperties connectionProperties) {
        super(name);
        this.connectionProperties = connectionProperties;
    }

    private CassandraConnectionProperties connectionProperties;

    public Property<String> keyspace = newString("keyspace");
    public Property<String> columnFamily = newString("columnFamily");
    public SchemaProperties main = new SchemaProperties("main");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form schemaForm = new Form(this, Form.MAIN);
        schemaForm.addRow(Widget.widget(keyspace).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        schemaForm.addRow(Widget.widget(columnFamily).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(schemaForm);//FIXME why need to invoke refreshLayout here? refer to SalesforceModuleProperties

        Form schemaRefForm = new Form(this, Form.REFERENCE);
        schemaRefForm.addRow(Widget.widget(keyspace).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE));
        schemaRefForm.addRow(Widget.widget(columnFamily).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE));
        schemaRefForm.addRow(main.getForm(Form.REFERENCE));//FIXME why need schema ref form here but don't need schema main form above
        refreshLayout(schemaRefForm);
    }

    private CassandraSourceOrSink getCassandraSourceOrSink() {
        // TODO(rskraba): redirect to the runtime to actually load.
        try {
            return (CassandraSourceOrSink) Class.forName("org.talend.components.cassandra.runtime_3_0.CassandraSourceOrSink").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ValidationResult beforeKeyspace() {
        CassandraSourceOrSink cassandraSourceOrSink = getCassandraSourceOrSink();
        cassandraSourceOrSink.initialize(null, connectionProperties);
        try {
            List<NamedThing> keyspaceNames = cassandraSourceOrSink.getKeyspaceNames(null);
            keyspace.setPossibleValues(keyspaceNames);
        } catch (IOException e) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage(e.getMessage());
        }
        return ValidationResult.OK;
    }

    public ValidationResult beforeColumnFamily() {
        CassandraSourceOrSink cassandraSourceOrSink = getCassandraSourceOrSink();
        cassandraSourceOrSink.initialize(null, connectionProperties);
        try {
            List<NamedThing> tableNames = cassandraSourceOrSink.getTableNames(null, keyspace.getStringValue());
            columnFamily.setPossibleValues(tableNames);
        } catch (IOException e) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage(e.getMessage());
        }
        return ValidationResult.OK;
    }

    public ValidationResult afterColumnFamily() {
        CassandraSourceOrSink cassandraSourceOrSink = getCassandraSourceOrSink();
        cassandraSourceOrSink.initialize(null, connectionProperties);
        try {
            main.schema.setValue(cassandraSourceOrSink.getSchema(null, keyspace.getStringValue(), columnFamily.getStringValue()));
        } catch (IOException e) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage(e.getMessage());
        }
        return ValidationResult.OK;
    }

}