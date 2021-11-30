package com.cloudera.utils.sql;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

import static java.sql.Types.*;
import static java.sql.Types.TIMESTAMP;

public class ResultArray {

    private String[] header;
    private List<String[]> records = new ArrayList<String[]>();

    public ResultArray(ResultSet source) {
        build(source);
    }

    private void build(ResultSet resultSet) {
        try {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            header = new String[columnCount];
            Map<String, Integer> fields = new LinkedHashMap<String, Integer>();
            for (int i = 1;i <= columnCount; i++) {
                String column = metadata.getColumnName(i);
                Integer type = metadata.getColumnType(i);
                header[i-1] = column;
                fields.put(column, type);
            }

            List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(fields.entrySet());
            Map.Entry<String, Integer> lastEntry = entryList.get(entryList.size()-1);

            String lastField = lastEntry.getKey();

            while (resultSet.next()) {
                String[] record = new String[columnCount];
                int i = 0;
                for (String column: fields.keySet()) {
                    // Check the Column Type and Fetch.
                    switch (fields.get(column)) {
                        case BIT:
                            record[i++] = Byte.toString(resultSet.getByte(column));
                            break;
                        case TINYINT:
                        case SMALLINT:
                        case INTEGER:
                            record[i++] =  Integer.toString(resultSet.getInt(column));
                            break;
                        case BIGINT:
                            record[i++] = Long.toString(resultSet.getLong(column));
                            break;
                        case FLOAT:
                            record[i++] = Float.toString(resultSet.getFloat(column));
                            break;
                        case REAL:
                        case DOUBLE:
                        case NUMERIC:
                            record[i++] = Double.toString(resultSet.getDouble(column));
                            break;
                        case DECIMAL:
                            record[i++] = resultSet.getBigDecimal(column).toString();
                            break;
                        case CHAR:
                        case VARCHAR:
                        case LONGVARCHAR:
                            record[i++] = resultSet.getString(column);
                            break;
                            // TODO: HANDLE Date/Time Formatting
                        case DATE:
                            record[i++] = resultSet.getDate(column).toString();
                            break;
                        case TIME:
                            record[i++] = resultSet.getTime(column).toString();
                            break;
                        case TIMESTAMP:
                            record[i++] = resultSet.getTimestamp(column).toString();
                            break;
                    }
                }
                records.add(record);
            }
        } catch (SQLException se) {

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public String[] getColumn(String name) {
        int columnIndex = find(header, name);
        String[] rtn = new String[records.size()];
        int i = 0;
        for (String[] record: records) {
            rtn[i++] = record[columnIndex];
        }
        return rtn;
    }

    public String[][] getColumns(String[] columns) {
        int[] columnIndexes = new int[columns.length];
        String[][] rtn = new String[columns.length][records.size()];

        for (int i=0;i<columns.length;i++) {
            columnIndexes[i] = find(header, columns[i]);
        }

        int i = 0;
        for (String[] record: records) {
            for (int c = 0;c < columnIndexes.length;c++) {
                rtn[c][i] = record[columnIndexes[c]];
            }
            i++;
        }
        return rtn;
    }

    public long getCount() {
        return records.size();
    }

    public String getField(String column, Integer index) {
        // Find the column index.
        int columnIndex = find(header, column);
        // Pull the record
        String[] record = records.get(index);
        // return the field
        return record[columnIndex];
    }

    // Function to find the index of an element in a primitive array in Java
    public static int find(String[] a, String search)
    {
        return IntStream.range(0, a.length)
                .filter(i -> search.equalsIgnoreCase(a[i]))
                .findFirst()
                .orElse(-1);	// return -1 if target is not found
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HEADER\n" + Arrays.toString(header) + "\n");
        sb.append("RECORDS\n");
        for (String[] record: records) {
            sb.append(Arrays.toString(record)).append("\n");
        }
        return sb.toString();
    }
}
