package de.cubeisland.engine.stats.storage;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.Table;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.stats.storage.TableStatsData.TABLE_STATSDATA;

public class StatsDataModel extends UpdatableRecordImpl<StatsDataModel> implements Record3<UInteger, UInteger, String>
{
    public StatsDataModel(Table<StatsDataModel> table)
    {
        super(TABLE_STATSDATA);
    }

    public StatsDataModel newStatsData(UInteger statsID, String data)
    {
        this.setStatID(statsID);
        this.setData(data);
        return this;
    }

    public UInteger getKey()
    {
        return (UInteger)this.getValue(0);
    }

    public void setKey(UInteger key)
    {
        this.setValue(0, key);
    }

    public UInteger getStatID()
    {
        return (UInteger)this.getValue(1);
    }

    public void setStatID(UInteger statID)
    {
        this.setValue(1, statID);
    }

    public String getData()
    {
        return (String)this.getValue(2);
    }

    public void setData(String data)
    {
        this.setValue(2, data);
    }

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    @Override
    public Row3<UInteger, UInteger, String> fieldsRow() {
        return (Row3)super.fieldsRow();
    }

    @Override
    public Row3<UInteger, UInteger, String> valuesRow() {
        return (Row3)super.valuesRow();
    }

    @Override
    public Field<UInteger> field1()
    {
        return TABLE_STATSDATA.KEY;
    }

    @Override
    public Field<UInteger> field2()
    {
        return TABLE_STATSDATA.STAT;
    }

    @Override
    public Field<String> field3()
    {
        return TABLE_STATSDATA.DATA;
    }

    @Override
    public UInteger value1()
    {
        return getKey();
    }

    @Override
    public UInteger value2()
    {
        return getStatID();
    }

    @Override
    public String value3()
    {
        return getData();
    }
}
