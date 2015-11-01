package jp.co.atware.bearcat.factory;

import android.content.Context;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ManagerOptions;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;

public class CouchbaseLiteFactory {

    private static Manager mManager;
    private static Database mDatabase;

    public static Manager getManager(final Context context, final ManagerOptions managerOptions) throws IOException {
        if (mManager == null) {
            mManager = new Manager(new AndroidContext(context), managerOptions);
        }
        return mManager;
    }

    public static Database getDatabase(final Context context, final ManagerOptions managerOptions, final String databaseName) throws CouchbaseLiteException, IOException {
        if (mManager == null) {
            getManager(context, managerOptions);
        }

        if (mDatabase == null) {
            mDatabase = mManager.getDatabase(databaseName);
        }
        return mDatabase;
    }

    public static Database getDatabase() {
        if (mDatabase == null) {
            throw new IllegalStateException();
        }

        return mDatabase;
    }


}
