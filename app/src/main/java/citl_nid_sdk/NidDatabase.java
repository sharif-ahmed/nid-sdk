package citl_nid_sdk;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NidInfoEntity.class}, version = 1, exportSchema = false)
public abstract class NidDatabase extends RoomDatabase {

    private static volatile NidDatabase INSTANCE;

    public abstract NidInfoDao nidInfoDao();

    public static NidDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (NidDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NidDatabase.class,
                            "nid_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
