package org.talend.components.cassandra.runtime_3_0;

import java.util.List;

public class CassandraBatchUtil {
    java.util.List<Integer> keyPositions = new java.util.ArrayList<Integer>();

    public CassandraBatchUtil(com.datastax.driver.core.Cluster cluster, String keyspace, String table, List<String> columns) {
        java.util.List<com.datastax.driver.core.ColumnMetadata> partitionKeys = cluster.getMetadata().getKeyspace(keyspace).getTable(table).getPartitionKey();
        java.util.List<String> partitionColumnKeys = new java.util.ArrayList<String>();
        for (com.datastax.driver.core.ColumnMetadata partitionKey : partitionKeys) {
            partitionColumnKeys.add(partitionKey.getName());
        }
        int position = 0;
        for (String col : columns) {
            if (partitionColumnKeys.contains(col)) {
                keyPositions.add(position);
            }
            position++;
        }

    }

    public java.nio.ByteBuffer getKey(com.datastax.driver.core.BoundStatement stmt) {
        java.util.List<java.nio.ByteBuffer> keys = new java.util.ArrayList<java.nio.ByteBuffer>();
        for (int position : keyPositions) {
            keys.add(stmt.getBytesUnsafe(position));
        }
        if (keys.size() == 1) {
            return keys.get(0);
        } else {
            return composeKeys(keys);
        }
    }

    private java.nio.ByteBuffer composeKeys(java.util.List<java.nio.ByteBuffer> buffers) {
        int totalLength = 0;
        for (java.nio.ByteBuffer buffer : buffers) {
            totalLength += buffer.remaining() + 3;
        }
        java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(totalLength);
        for (java.nio.ByteBuffer buffer : buffers) {
            java.nio.ByteBuffer bb = buffer.duplicate();
            out.put((byte) ((bb.remaining() >> 8) & 0xFF));
            out.put((byte) (bb.remaining() & 0xFF));
            out.put(bb);
            out.put((byte) 0);
        }
        out.flip();
        return out;
    }
}
