package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public final class DataBlockDao {
    private final DbConnection dbConnection;

    public void add(DataBlock dataBlock) throws SQLException {

    }
}
