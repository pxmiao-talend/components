package org.talend.components.cassandra.output;

import org.apache.avro.Schema;
import org.talend.components.cassandra.CassandraIOBasedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import java.util.ArrayList;
import java.util.List;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.*;

public class TCassandraOutputProperties extends CassandraIOBasedProperties {

    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public TCassandraOutputProperties(String name) {
        super(name);
    }

    public enum ActionOnKeyspace {
        None,
        Drop_Create,
        Create,
        Create_If_Not_Exists,
        Drop_If_Exists_And_Create
    }

    //TODO(bchen) any improvement for enum? how to set, how to get, how to check
    public Property<ActionOnKeyspace> actionOnKeyspace = newEnum("actionOnKeyspace", ActionOnKeyspace.class);

    public enum ReplicaStrategy {
        Network,
        Simple
    }
    public Property<ReplicaStrategy> replicaStrategy = newEnum("replicaStrategy", ReplicaStrategy.class);//SHOW_IF actionOnKeyspace != NONE

    public Property<Integer> simpleReplicaNumber = newInteger("simpleReplicaNumber", 3); //SHOW_IF replica is simple

    public NetworkReplicaTable networkReplicaTable = new NetworkReplicaTable("networkReplicaTable");

    public enum ActionOnColumnFamily {
        None,
        Drop_Create,
        Create,
        Create_If_Not_Exists,
        Drop_If_Exists_And_Create,
        Truncate
    }

    public Property<ActionOnColumnFamily> actionOnColumnFamily = newEnum("actionOnColumnFamily", ActionOnColumnFamily.class);

    public enum DataAction {
        Insert,
        Update,
        Delete
    }
    public Property<DataAction> dataAction = newEnum("dataAction", DataAction.class);

    public Property<Boolean> dieOnError = newBoolean("dieOnError", false);

    //Advanced setting
    public Property<Boolean> useUnloggedBatch = newBoolean("useUnloggedBatch", false);
    public Property<Integer> batchSize = newInteger("batchSize", 10000);

    public Property<Boolean> insertIfNotExists = newBoolean("insertIfNotExists", false);
    public Property<Boolean> deleteIfExists = newBoolean("deleteIfExists", false);

    public Property<Boolean> usingTTL = newBoolean("usingTTL", false);
    public Property<String> ttl = newString("ttl");
    public Property<Boolean> usingTimestamp = newBoolean("usingTimestamp", false);
    public Property<String> timestamp = newString("timestamp");

    public IfConditionTable ifCondition = new IfConditionTable("ifCondition");

    public AssignmentOperationTable assignmentOperation = new AssignmentOperationTable("assignmentOperation");

    public DeleteColumnByPositionKeyTable deleteColumnByPositionKey = new DeleteColumnByPositionKeyTable("deleteColumnByPositionKey");

    public RowKeyInListTable rowKeyInList = new RowKeyInListTable("rowKeyInList");


    @Override
    public void setupProperties() {
        super.setupProperties();
        actionOnKeyspace.setValue(ActionOnKeyspace.None);

        actionOnColumnFamily.setValue(ActionOnColumnFamily.None);
        dataAction.setValue(DataAction.Insert);

    }

    private List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        Schema schema = new Schema.Parser().parse(getSchemaProperties().main.schema.getStringValue());
        List<Schema.Field> fields = schema.getFields();
        for (Schema.Field field : fields) {
            columnNames.add(field.name());
        }
        return columnNames;
    }

    //TODO(bchen) can be common? how to avoid trigger for common usage
    public void beforeTtl() {
        ttl.setPossibleValues(getColumnNames());
    }

    public void beforeTimestamp() {
        timestamp.setPossibleValues(getColumnNames());
    }

    public void beforeIfCondition() {
        ifCondition.columnName.setPossibleValues(getColumnNames());
    }

    public void beforeAssignmentOperation() {
        assignmentOperation.columnName.setPossibleValues(getColumnNames());
        assignmentOperation.keyColumn.setPossibleValues(getColumnNames());
    }

    public void beforeDeleteColumnByPositionKey() {
        deleteColumnByPositionKey.columnName.setPossibleValues(getColumnNames());
    }

    public void beforeRowKeyInList() {
        rowKeyInList.columnName.setPossibleValues(getColumnNames());
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(actionOnKeyspace).addColumn(replicaStrategy);
        mainForm.addRow(simpleReplicaNumber);
        mainForm.addRow(widget(networkReplicaTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(actionOnColumnFamily);
        mainForm.addRow(dataAction);
        mainForm.addRow(dieOnError);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(useUnloggedBatch).addColumn(batchSize);
        advancedForm.addRow(insertIfNotExists);
        advancedForm.addRow(deleteIfExists);
        advancedForm.addRow(usingTTL).addColumn(ttl);
        advancedForm.addRow(usingTimestamp).addColumn(timestamp);
        advancedForm.addRow(widget(ifCondition).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        advancedForm.addRow(widget(assignmentOperation).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        advancedForm.addRow(widget(deleteColumnByPositionKey).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        advancedForm.addRow(widget(rowKeyInList).setWidgetType(Widget.TABLE_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(actionOnKeyspace.getName()).setHidden(DataAction.Delete.equals(dataAction.getValue()));
            form.getWidget(replicaStrategy.getName()).setHidden(form.getWidget(actionOnKeyspace.getName()).isHidden() || ActionOnKeyspace.None.equals(actionOnKeyspace.getValue()));
            form.getWidget(simpleReplicaNumber.getName()).setHidden(form.getWidget(replicaStrategy.getName()).isHidden() || ReplicaStrategy.Network.equals(replicaStrategy.getValue()));
            form.getWidget(networkReplicaTable.getName()).setHidden(form.getWidget(replicaStrategy.getName()).isHidden() || ReplicaStrategy.Simple.equals(replicaStrategy.getValue()));
            form.getWidget(actionOnColumnFamily.getName()).setHidden(form.getWidget(actionOnKeyspace.getName()).isHidden());
        } else if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(batchSize.getName()).setHidden(!useUnloggedBatch.getValue());
            form.getWidget(insertIfNotExists.getName()).setHidden(!DataAction.Insert.equals(dataAction.getValue()) || usingTimestamp.getValue());
            form.getWidget(deleteIfExists.getName()).setHidden(!DataAction.Delete.equals(dataAction.getValue()));
            form.getWidget(usingTTL.getName()).setHidden(!DataAction.Insert.equals(dataAction.getValue()) && !DataAction.Update.equals(dataAction.getValue()));
            form.getWidget(ttl.getName()).setHidden(form.getWidget(usingTTL.getName()).isHidden() || !usingTTL.getValue());
            form.getWidget(usingTimestamp.getName()).setHidden((DataAction.Insert.equals(dataAction.getValue()) && insertIfNotExists.getValue()));
            form.getWidget(timestamp.getName()).setHidden(form.getWidget(usingTimestamp.getName()).isHidden() || !usingTimestamp.getValue());
            form.getWidget(ifCondition.getName()).setHidden(!DataAction.Update.equals(dataAction.getValue()) && (!DataAction.Delete.equals(dataAction.getValue()) && deleteIfExists.getValue()));
            form.getWidget(assignmentOperation.getName()).setHidden(!DataAction.Update.equals(dataAction.getValue()));
            //FIXME how to hidden keyColumn in assignmentOperation when operation is p/k,waiting for studio support ComponentProperties for table widget
            form.getWidget(deleteColumnByPositionKey.getName()).setHidden(!DataAction.Delete.equals(dataAction.getValue()));
            form.getWidget(rowKeyInList.getName()).setHidden(!DataAction.Update.equals(dataAction.getValue()) && !DataAction.Delete.equals(dataAction.getValue()));
        }
    }

    //FIXME(bchen) how to avoid trigger for each property in condition
    public void afterDataAction() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterActionOnKeyspace() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterReplicaStrategy() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterUseUnloggedBatch() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterUsingTimestamp() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterUsingTTL() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterInsertIfNotExists() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterDeleteIfExists() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

}
