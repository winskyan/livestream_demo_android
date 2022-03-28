package io.agora.livedemo.common.db;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import io.agora.livedemo.common.db.dao.ReceiveGiftDao;
import io.agora.livedemo.utils.MD5;
import io.agora.util.EMLog;

public class DemoDbHelper {
    private static final String TAG = DemoDbHelper.class.getSimpleName();
    private static DemoDbHelper instance;
    private Context mContext;
    private String currentUser;
    private AppDatabase mDatabase;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private DemoDbHelper(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static DemoDbHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DemoDbHelper.class) {
                if (instance == null) {
                    instance = new DemoDbHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * init db
     *
     * @param user
     */
    public void initDb(String user) {
        if (currentUser != null) {
            if (TextUtils.equals(currentUser, user)) {
                EMLog.i(TAG, "you have opened the db");
                return;
            }
            closeDb();
        }
        this.currentUser = user;
        String userMd5 = MD5.encrypt2MD5(user);
        String dbName = String.format("em_%1$s.db", userMd5);
        EMLog.i(TAG, "db name = " + dbName);
        mDatabase = Room.databaseBuilder(mContext, AppDatabase.class, dbName)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        mIsDatabaseCreated.postValue(true);
    }

    private void closeDb() {
        if (mDatabase != null) {
            mDatabase.close();
        }
        currentUser = null;
    }

    public ReceiveGiftDao getReceiveGiftDao() {
        if (mDatabase != null) {
            return mDatabase.receiveGiftDao();
        }
        EMLog.i(TAG, "get ReceiveGiftDao failed, should init db first");
        return null;
    }

}
