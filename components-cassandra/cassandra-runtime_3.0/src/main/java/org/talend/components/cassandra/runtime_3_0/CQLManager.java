package org.talend.components.cassandra.runtime_3_0;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.components.cassandra.output.AssignmentOperationTable;
import org.talend.components.cassandra.output.TCassandraOutputProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.di.DiSchemaConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Column {

    private final Field f;

    private String mark = "?";

    private AssignmentOperationTable.Operation assignmentOperation;

    private Column assignmentKey;

    private boolean asColumnKey = false;

    public Column(Field f) {
        this.f = f;
    }

    public Schema getSchema() {
        return f.schema();
    }

    public String getName() {
        return f.name();
    }

    public String getDBName() {
        //FIXME(bchen) getSchema.getProp or field.getProp?
        String dbName = getSchema().getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
        return dbName == null ? getName() : dbName;
    }

    public boolean isKey() {
        //FIXME(bchen) getSchema.getProp or field.getProp?
        String isKey = getSchema().getProp(DiSchemaConstants.TALEND6_COLUMN_IS_KEY);//FIXME(bchen) move to use SchemaConstants
        return isKey != null && "true".equalsIgnoreCase(isKey);
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public void setAssignmentOperation(AssignmentOperationTable.Operation op) {
        this.assignmentOperation = op;
    }

    public AssignmentOperationTable.Operation getAssignmentOperation() {
        return assignmentOperation;
    }

    public void setAssignmentKey(Column keyColumn) {
        this.assignmentKey = keyColumn;
    }

    public Column getAssignmentKey() {
        return assignmentKey;
    }

    public void setAsColumnKey(boolean asColumnKey) {
        this.asColumnKey = asColumnKey;
    }

    public boolean getAsColumnKey() {
        return asColumnKey;
    }
}

class CQLManager {

    private String[] KeyWords = {"ADD", "ALL", "ALLOW", "ALTER", "AND", "ANY", "APPLY", "AS", "ASC", "ASCII", "AUTHORIZE",
            "BATCH", "BEGIN", "BIGINT", "BLOB", "BOOLEAN", "BY", "CLUSTERING", "COLUMNFAMILY", "COMPACT", "CONSISTENCY", "COUNT",
            "COUNTER", "CREATE", "CUSTOM", "DECIMAL", "DELETE", "DESC", "DISTINCT", "DOUBLE", "DROP", "EACH_QUORUM", "EXISTS",
            "FILTERING", "FLOAT", "FROM", "frozen", "GRANT", "IF", "IN", "INDEX", "INET", "INFINITY", "INSERT", "INT", "INTO",
            "KEY", "KEYSPACE", "KEYSPACES", "LEVEL", "LIMIT", "LIST", "LOCAL_ONE", "LOCAL_QUORUM", "MAP", "MODIFY", "NAN",
            "NORECURSIVE", "NOSUPERUSER", "NOT", "OF", "ON", "ONE", "ORDER", "PASSWORD", "PERMISSION", "PERMISSIONS", "PRIMARY",
            "QUORUM", "RENAME", "REVOKE", "SCHEMA", "SELECT", "SET", "STATIC", "STORAGE", "SUPERUSER", "TABLE", "TEXT",
            "TIMESTAMP", "TIMEUUID", "THREE", "TO", "TOKEN", "TRUNCATE", "TTL", "TWO", "TYPE", "UNLOGGED", "UPDATE", "USE",
            "USER", "USERS", "USING", "UUID", "VALUES", "VARCHAR", "VARINT", "WHERE", "WITH", "WRITETIME"};

    private TCassandraOutputProperties props;

    private TCassandraOutputProperties.DataAction action;

    private String keyspace;

    private String tableName;

    private Boolean useSpark = false;

    private List<Column> valueColumns;

    public CQLManager(TCassandraOutputProperties props) {
        this.props = props;
        this.action = this.props.dataAction.getValue();
        this.keyspace = this.props.getSchemaProperties().keyspace.getStringValue();
        this.tableName = this.props.getSchemaProperties().columnFamily.getStringValue();
        this.tableName = this.keyspace + "." + this.tableName;//FIXME(bchen) double quote around
        createColumnList(new Schema.Parser().parse(this.props.getSchemaProperties().main.schema.getStringValue()));
        this.valueColumns = collectValueColumns();
    }

    public CQLManager(TCassandraOutputProperties props, boolean useSpark) {
        this(props);
        this.useSpark = useSpark;
    }

    public String getKeyspace(){
        return keyspace;
    }
    public String getTableName(){
        return tableName;
    }

    private List<Column> all;

    private List<Column> keys;

    private List<Column> normals;

    private List<Column> conditions;

    private Column ttl;

    private Column timestamp;

    private void createColumnList(Schema schema) {
        all = new ArrayList<Column>();
        for (Field f : schema.getFields()) {
            all.add(new Column(f));
        }
        keys = new ArrayList<Column>();
        normals = new ArrayList<Column>();
        conditions = new ArrayList<Column>();
        boolean usingTimestamp = props.usingTimestamp.getValue();
        String timestampColName = props.timestamp.getValue();
        for (Column column : all) {
            if (TCassandraOutputProperties.DataAction.Insert == action || TCassandraOutputProperties.DataAction.Update == action) {
                boolean usingTTL = props.usingTTL.getValue();
                String ttlColName = props.ttl.getValue();
                if (usingTTL && ttlColName.equals(column.getName())) {
                    ttl = column;
                    ttl.setMark("TTL ?");
                    continue;
                }
            }
            if (usingTimestamp && timestampColName.equals(column.getName())) {
                timestamp = column;
                timestamp.setMark("TIMESTAMP ?");
                continue;
            }
            if (column.isKey()) {
                keys.add(column);
                continue;
            }
            if (TCassandraOutputProperties.DataAction.Update.equals(action) || (TCassandraOutputProperties.DataAction.Delete.equals(action) && !props.deleteIfExists.getValue())) {
                List<String> ifCoditions = (List<String>) props.ifCondition.columnName.getValue();
                boolean matched = false;
                for (String ifCodition : ifCoditions) {
                    if (ifCodition.equals(column.getName())) {
                        conditions.add(column);
                        matched = true;
                        continue;
                    }
                }
                if (matched) {
                    continue;
                }
            }
            normals.add(column);
        }
        if (TCassandraOutputProperties.DataAction.Update.equals(action)) {
            List<Column> keyColumns = new ArrayList<Column>();
            for (int i = 0; i < normals.size(); i++){
                props.assignmentOperation.columnName.getValue().get(i);
                String updateColumnKeyName = props.assignmentOperation.keyColumn.getValue().get(i);
                AssignmentOperationTable.Operation updateColumnOperation = props.assignmentOperation.operation.getValue().get(i);
                if (AssignmentOperationTable.Operation.Position_Or_Key.equals(updateColumnOperation) && normals.get(i).getName().equals(updateColumnKeyName)) {
                    keyColumns.add(normals.get(i));
                }
            }
            normals.removeAll(keyColumns);
            for (int i = 0; i < normals.size(); i++){
                String updateColumnName = props.assignmentOperation.columnName.getValue().get(i);
                String updateColumnKeyName = props.assignmentOperation.keyColumn.getValue().get(i);
                AssignmentOperationTable.Operation updateColumnOperation = props.assignmentOperation.operation.getValue().get(i);
                if (updateColumnName.equals(normals.get(i).getName())) {
                    normals.get(i).setAssignmentOperation(updateColumnOperation);
                    if (AssignmentOperationTable.Operation.Position_Or_Key.equals(updateColumnOperation)) {
                        for (Column keyColumn : keyColumns) {
                            if (keyColumn.getName().equals(updateColumnKeyName)) {
                                normals.get(i).setAssignmentKey(keyColumn);
                            }
                        }
                    }
                    continue;
                }
            }
        }
        if (TCassandraOutputProperties.DataAction.Delete.equals(action)) {
            List<String> columnsKey = props.deleteColumnByPositionKey.columnName.getValue();
            for (Column column : normals) {
                if(columnsKey.contains(column.getName())){
                        column.setAsColumnKey(true);
                }
            }
        }
    }

    private List<Column> collectValueColumns() {
        List<Column> columns = new ArrayList<>();
        if (TCassandraOutputProperties.DataAction.Insert.equals(action)) {
            columns.addAll(keys);
            columns.addAll(normals);
            if (ttl != null)
                columns.add(ttl);
            if (timestamp != null)
                columns.add(timestamp);
        } else if (TCassandraOutputProperties.DataAction.Update.equals(action)) {
            if (ttl != null)
                columns.add(ttl);
            if (timestamp != null)
                columns.add(timestamp);
            for (Column normal : normals) {
                if (normal.getAssignmentKey() != null) {
                    columns.add(normal.getAssignmentKey());
                }
                columns.add(normal);
            }
            columns.addAll(keys);
            columns.addAll(conditions);
        } else if (TCassandraOutputProperties.DataAction.Delete.equals(action)) {
            for (Column column : normals) {
                if (column.getAsColumnKey()) {
                    columns.add(column);
                }
            }
            if (timestamp != null)
                columns.add(timestamp);
            columns.addAll(keys);
            boolean ifExist = props.deleteIfExists.getValue();
            if (!ifExist) {
                columns.addAll(conditions);
            }
        }
        return columns;
    }

    private String getLProtectedChar(String keyword) {
        return "\"";
    }

    private String getRProtectedChar(String keyword) {
        return "\"";
    }

    private String wrapProtectedChar(String keyword) {
        if (keyword.matches("^[a-z0-9_]+$")) {
            return keyword;
        } else {
            return getLProtectedChar(keyword) + keyword + getRProtectedChar(keyword);
        }
    }

    public List<String> getValueColumns() {
        List<String> valueColumnsName = new ArrayList<String>();
        for (Column col : valueColumns) {
            valueColumnsName.add(col.getName());
        }
        return valueColumnsName;
    }

    private String getDropKSCQL(boolean ifExists) {
        StringBuilder dropKSCQL = new StringBuilder();
        dropKSCQL.append("DROP KEYSPACE ");
        if (ifExists) {
            dropKSCQL.append("IF EXISTS ");
        }
        dropKSCQL.append(this.keyspace);
        return dropKSCQL.toString();
    }

    private String getCreateKSCQL(boolean ifNotExists) {
        StringBuilder createKSCQL = new StringBuilder();
        createKSCQL.append("CREATE KEYSPACE ");
        if (ifNotExists) {
            createKSCQL.append("IF NOT EXISTS ");
        }
        createKSCQL.append(this.keyspace);
        createKSCQL.append("WITH REPLICATION = {'class' : '" + props.replicaStrategy.getValue() + "',");
        if (TCassandraOutputProperties.ReplicaStrategy.Simple == props.replicaStrategy.getValue()) {
            createKSCQL.append("'replication_factor' : " + props.simpleReplicaNumber.getValue() + "}");
        } else {
            List<String> datacenterList = props.networkReplicaTable.datacenterName.getValue();
            List<Integer> replicaNumberList = props.networkReplicaTable.replicaNumber.getValue();
            Integer size = Math.min(datacenterList.size(), replicaNumberList.size());
            for(int i = 0; i < size; i++){
                createKSCQL.append("'" + datacenterList.get(i) + "' : " + replicaNumberList.get(i));
                if (i + 1 < size) {
                    createKSCQL.append(",");
                }
            }
            createKSCQL.append("}");
        }

        return createKSCQL.toString();
    }

    public List<String> getKSCQLs() {
        List<String> cqls = new ArrayList<>();
        TCassandraOutputProperties.ActionOnKeyspace actionOnKeyspace = props.actionOnKeyspace.getValue();
        //No action needed for delete operation
        if (TCassandraOutputProperties.DataAction.Delete == props.dataAction.getValue() || TCassandraOutputProperties.ActionOnKeyspace.None == actionOnKeyspace) {
            return cqls;
        }

        switch (actionOnKeyspace){
            case Drop_Create:
                cqls.add(getDropKSCQL(false));
                cqls.add(getCreateKSCQL(false));
                break;
            case Create:
                cqls.add(getCreateKSCQL(false));
                break;
            case Create_If_Not_Exists:
                cqls.add(getCreateKSCQL(true));
                break;
            case Drop_If_Exists_And_Create:
                cqls.add(getDropKSCQL(true));
                cqls.add(getCreateKSCQL(false));
                break;
        }
        return cqls;
    }

    public List<String> getTableCQLs() throws IOException {
        List<String> cqls = new ArrayList<>();
        TCassandraOutputProperties.ActionOnColumnFamily actionOnColumnFamily = props.actionOnColumnFamily.getValue();
        //No action needed for delete operation
        if (TCassandraOutputProperties.DataAction.Delete == props.dataAction.getValue() || TCassandraOutputProperties.ActionOnColumnFamily.None == actionOnColumnFamily) {
            return cqls;
        }

        if (TCassandraOutputProperties.ActionOnColumnFamily.Truncate != actionOnColumnFamily && containsUnsupportTypes()) {
            throw new IOException("Don't support create table with set/list/map");
        }

        switch (actionOnColumnFamily) {
            case Drop_Create:
                cqls.add(getDropTableCQL(false));
                cqls.add(getCreateTableCQL(false));
                break;
            case Create:
                cqls.add(getCreateTableCQL(false));
                break;
            case Drop_If_Exists_And_Create:
                cqls.add(getDropTableCQL(true));
                cqls.add(getCreateTableCQL(false));
                break;
            case Create_If_Not_Exists:
                cqls.add(getCreateTableCQL(true));
                break;
            case Truncate:
                cqls.add(getTruncateTableCQL());
            default:
                break;
        }

        return cqls;
    }

    private String getDropTableCQL(boolean ifExists) {
        StringBuilder dropTableCQL = new StringBuilder();
        dropTableCQL.append("DROP TABLE ");
        if (ifExists) {
            dropTableCQL.append("IF EXISTS ");
        }
        dropTableCQL.append(tableName);
        return dropTableCQL.toString();
    }

    private String getCreateTableCQL(boolean ifNotExists) {
        StringBuilder createCQL = new StringBuilder();
        createCQL.append("CREATE TABLE ");
        if (ifNotExists) {
            createCQL.append("IF NOT EXISTS ");
        }
        createCQL.append(tableName + "(");
        List<Column> columns = new ArrayList<Column>();
        columns.addAll(keys);
        columns.addAll(normals);
        if (TCassandraOutputProperties.DataAction.Update == action) {
            columns.addAll(conditions);
        }
        int count = 1;
        for (Column column : columns) {
            createCQL.append(wrapProtectedChar(column.getDBName()));
            createCQL.append(" ");
            createCQL.append(CassandraAvroRegistry.getDataType(column.getSchema()));
            if (count < columns.size()) {
                createCQL.append(",");
            }
            count++;
        }
        if (keys.size() > 0) {
            createCQL.append(",PRIMARY KEY(");
            int i = 1;
            for (Column column : keys) {
                createCQL.append(wrapProtectedChar(column.getDBName()));
                if (i < keys.size()) {
                    createCQL.append(",");
                }
                i++;
            }
            createCQL.append(")");
        }
        createCQL.append(")");
        return createCQL.toString();
    }

    private boolean containsUnsupportTypes() {
        boolean unsupport = false;
        List<String> unsupportTypes = java.util.Arrays.asList(new String[]{"set", "list", "map"});
        List<Column> columns = new ArrayList<Column>();
        columns.addAll(keys);
        columns.addAll(normals);
        if (TCassandraOutputProperties.DataAction.Update == action) {
            columns.addAll(conditions);
        }
        for (Column column : columns) {
            if (unsupportTypes.contains(CassandraAvroRegistry.getDataType(column.getSchema()))) {
                return true;
            }
        }
        return false;
    }

    private String getDeleteTableCQL() {
        StringBuilder deleteTableCQL = new StringBuilder();
        deleteTableCQL.append("DELETE FROM " + tableName);
        return deleteTableCQL.toString();
    }

    private String getTruncateTableCQL() {
        StringBuilder truncateTableCQL = new StringBuilder();
        truncateTableCQL.append("TRUNCATE " + tableName);
        return truncateTableCQL.toString();
    }

    public String generatePreActionCQL() {
        switch (action){
            case Insert:
                return generatePreInsertCQL();
            case Update:
                return generatePreUpdateCQL();
            case Delete:
                return generatePreDeleteCQL();
            default:
                return "";
        }
    }

    /*
     * INSERT INTO table_name( identifier, column_name...)VALUES ( value, value ... )USING option AND option
     */
    private String generatePreInsertCQL() {
        List<Column> columns = new ArrayList<Column>();
        columns.addAll(keys);
        columns.addAll(normals);

        int count = 1;
        StringBuilder preInsertCQL = new StringBuilder();
        preInsertCQL.append("INSERT INTO " + tableName + " (");
        for (Column column : columns) {
            preInsertCQL.append(wrapProtectedChar(column.getDBName()));
            if (count < columns.size()) {
                preInsertCQL.append(",");
            }
            count++;
        }
        preInsertCQL.append(") VALUES (");
        count = 1;
        for (Column column : columns) {
            preInsertCQL.append(column.getMark());
            if (count < columns.size()) {
                preInsertCQL.append(",");
            }
            count++;
        }
        preInsertCQL.append(")");
        boolean ifNotExist = props.insertIfNotExists.getValue();
        if (ifNotExist) {
            preInsertCQL.append(" IF NOT EXISTS");
        }
        if (ttl != null || timestamp != null) {
            preInsertCQL.append(" USING ");
            if (ttl != null) {
                preInsertCQL.append(ttl.getMark());
                if (timestamp != null) {
                    preInsertCQL.append(" AND ");
                }
            }
            if (timestamp != null) {
                preInsertCQL.append(timestamp.getMark());
            }
        }
        return preInsertCQL.toString();
    }

    private String generatePreUpdateCQL() {
        StringBuilder preUpdateCQL = new StringBuilder();
        preUpdateCQL.append("UPDATE " + tableName);
        if (ttl != null || timestamp != null) {
            preUpdateCQL.append(" USING ");
            if (ttl != null) {
                preUpdateCQL.append(ttl.getMark());
                if (timestamp != null) {
                    preUpdateCQL.append(" AND ");
                }
            }
            if (timestamp != null) {
                preUpdateCQL.append(timestamp.getMark());
            }
        }
        preUpdateCQL.append(" SET ");
        int count = 1;
        for (Column column : normals) {

            String assignment = wrapProtectedChar(column.getDBName()) + "=" +
                    column.getMark();
            switch (column.getAssignmentOperation()){
                case Append:
                    assignment = wrapProtectedChar(column.getDBName()) + "=" +
                            wrapProtectedChar(column.getDBName()) + "+" +
                            column.getMark();
                    break;
                case Prepend:
                    assignment = wrapProtectedChar(column.getDBName()) + "=" + column.getMark()
                            + "+" +
                            wrapProtectedChar(column.getDBName());
                    break;
                case Minus:
                    assignment = wrapProtectedChar(column.getDBName()) + "=" +
                            wrapProtectedChar(column.getDBName()) + "-" +
                            column.getMark();
                    break;
                case Position_Or_Key:
                    assignment = wrapProtectedChar(column.getDBName()) + "[?]=" +
                            column.getMark();
                    break;
            }

            preUpdateCQL.append(assignment);

            if (count < normals.size()) {
                preUpdateCQL.append(",");
            }
            count++;
        }
        preUpdateCQL.append(" WHERE ");
        count = 1;
        for (Column column : keys) {
            preUpdateCQL.append(wrapProtectedChar(column.getDBName()));
            preUpdateCQL.append(rowKeyInList(column) ? " IN " : "=");
            preUpdateCQL.append(column.getMark());
            if (count < keys.size()) {
                preUpdateCQL.append(" AND ");
            }
            count++;
        }
        if (conditions.size() > 0) {
            preUpdateCQL.append(" IF ");
            count = 1;
            for (Column column : conditions) {
                preUpdateCQL.append(wrapProtectedChar(column.getDBName()));
                preUpdateCQL.append("=");
                preUpdateCQL.append(column.getMark());
                if (count < conditions.size()) {
                    preUpdateCQL.append(" AND ");
                }
                count++;
            }
        }
        // can't work actually, even it supported on office document
        // boolean ifExist = "true".equals(ElementParameterParser.getValue(node,
        // "__UPDATE_IF_EXISTS__"));
        // if(ifExist){
        // preUpdateSQL.append(" IF EXISTS");
        // }

        return preUpdateCQL.toString();

    }

    private boolean rowKeyInList(Column column) {
        List<String> rowKeyInList = props.rowKeyInList.columnName.getValue();
        return rowKeyInList.contains(column.getName());
    }

    private String generatePreDeleteCQL() {
        StringBuilder preDeleteCQL = new StringBuilder();
        preDeleteCQL.append("DELETE ");
        int count = 1;
        for (Column column : normals) {
            preDeleteCQL.append(wrapProtectedChar(column.getDBName()));
            if (column.getAsColumnKey()) {
                preDeleteCQL.append("[?]");
            }
            if (count < normals.size()) {
                preDeleteCQL.append(",");
            }
            count++;
        }
        preDeleteCQL.append(" FROM " + tableName);
        if (timestamp != null) {
            preDeleteCQL.append(" USING ");
            preDeleteCQL.append(timestamp.getMark());
        }
        if (keys.size() > 0) {
            preDeleteCQL.append(" WHERE ");
            count = 1;
            for (Column column : keys) {
                preDeleteCQL.append(wrapProtectedChar(column.getDBName()));
                preDeleteCQL.append(rowKeyInList(column) ? " IN " : "=");
                preDeleteCQL.append(column.getMark());
                if (count < keys.size()) {
                    preDeleteCQL.append(" AND ");
                }
                count++;
            }
        }
        boolean ifExist = props.deleteIfExists.getValue();
        if (ifExist) {
            preDeleteCQL.append(" IF EXISTS");
        } else {
            if (conditions.size() > 0) {
                preDeleteCQL.append(" IF ");
                count = 1;
                for (Column column : conditions) {
                    preDeleteCQL.append(wrapProtectedChar(column.getDBName()));
                    preDeleteCQL.append("=");
                    preDeleteCQL.append(column.getMark());
                    if (count < conditions.size()) {
                        preDeleteCQL.append(" AND ");
                    }
                    count++;
                }
            }
        }
        return preDeleteCQL.toString();
    }

}