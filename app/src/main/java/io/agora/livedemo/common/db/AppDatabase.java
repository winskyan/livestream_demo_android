package io.agora.livedemo.common.db;

import io.agora.livedemo.common.db.converter.DateConverter;
import io.agora.livedemo.common.db.dao.ReceiveGiftDao;
import io.agora.livedemo.common.db.entity.ReceiveGiftEntity;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {ReceiveGiftEntity.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ReceiveGiftDao receiveGiftDao();

}
