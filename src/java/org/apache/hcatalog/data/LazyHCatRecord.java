package org.apache.hcatalog.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hcatalog.common.HCatException;
import org.apache.hcatalog.common.HCatUtil;
import org.apache.hcatalog.data.schema.HCatSchema;

public class LazyHCatRecord extends HCatRecord {

  public static final Log LOG = LogFactory
      .getLog(LazyHCatRecord.class.getName());

  private Object o;
  private StructObjectInspector soi;
  private int size;
  
  @Override
  public Object get(int fieldNum) {
    try {
      StructField fref = soi.getAllStructFieldRefs().get(fieldNum);
      return HCatRecordSerDe.serializeField(
          soi.getStructFieldData(o, fref),
          fref.getFieldObjectInspector());
    } catch (SerDeException e) {
      throw new IllegalStateException("SerDe Exception deserializing",e);
    }
  }
  

  @Override
  public List<Object> getAll() {
    
    List<Object> r = new ArrayList<Object>(this.size);
    for (int i = 0; i < this.size; i++){
      r.add(i, get(i));
    }
    return r;
  }

  @Override
  public void set(int fieldNum, Object value) {
    throw new UnsupportedOperationException("not allowed to run set() on LazyHCatRecord");
  }

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    throw new UnsupportedOperationException("LazyHCatRecord is intended to wrap"
        + " an object/object inspector as a HCatRecord "
        + "- it does not need to be read from DataInput.");
  }

  @Override
  public void write(DataOutput out) throws IOException {
    throw new UnsupportedOperationException("LazyHCatRecord is intended to wrap"
        + " an object/object inspector as a HCatRecord "
        + "- it does not need to be written to a DataOutput.");
  }

  @Override
  public Object get(String fieldName, HCatSchema recordSchema)
      throws HCatException {
    int idx = recordSchema.getPosition(fieldName);
    return get(idx);
  }

  @Override
  public void set(String fieldName, HCatSchema recordSchema, Object value)
      throws HCatException {
    throw new UnsupportedOperationException("not allowed to run set() on LazyHCatRecord");
  }

  @Override
  public void remove(int idx) throws HCatException {
    throw new UnsupportedOperationException("not allowed to run remove() on LazyHCatRecord");
  }

  @Override
  public void copy(HCatRecord r) throws HCatException {
    throw new UnsupportedOperationException("not allowed to run copy() on LazyHCatRecord");
  }
  
  public LazyHCatRecord(Object o, ObjectInspector oi) throws Exception{

    if (oi.getCategory() != Category.STRUCT) {
      throw new SerDeException(getClass().toString()
          + " can only make a lazy hcat record from objects of struct types, but we got: "
          + oi.getTypeName());
    }

    this.soi = (StructObjectInspector)oi;
    this.o = o;
    this.size = soi.getAllStructFieldRefs().size();

  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i< size ; i++) {
      sb.append(get(i)+"\t");
    }
    return sb.toString();
  }

}