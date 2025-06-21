package app.notesr.cli.db.dao;

import app.notesr.cli.model.DataBlock;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Set;

@RegisterBeanMapper(DataBlock.class)
public interface DataBlockEntityDao {

    @SqlUpdate("""
            INSERT INTO data_blocks (id, file_id, block_order, data)
            VALUES (:id, :fileId, :blockOrder, :data)
            """)
    void add(@BindBean DataBlock dataBlock);

    @SqlQuery("SELECT id, file_id, block_order FROM data_blocks")
    Set<DataBlock> getAllDataBlocksWithoutData();

    @SqlQuery("SELECT id FROM data_blocks WHERE file_id = :fileId ORDER BY block_order")
    Set<String> getIdsByFileId(@Bind("fileId") String fileId);

    @SqlQuery("SELECT * FROM data_blocks WHERE id = :id")
    DataBlock getById(@Bind("id") String id);
}
