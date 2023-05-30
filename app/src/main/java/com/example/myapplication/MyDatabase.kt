import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class MyDatabase private constructor(context: Context) {

    private val dbHelper: MyDbHelper = MyDbHelper(context)
    private val database: SQLiteDatabase = dbHelper.writableDatabase

    object MyDBContract {
        object BookEntry : BaseColumns {
            const val TABLE_NAME = "Book"
            const val COLUMN_BOOK_ID = "book_id"
            const val COLUMN_TITLE = "title"
        }

        object DrawEntry : BaseColumns {
            const val TABLE_NAME = "Draw"
            const val COLUMN_PAGE_ID = "page_id"
            const val COLUMN_BOOK_ID = "book_id"
            const val COLUMN_PAGE_NUMBER = "page_number"
            const val COLUMN_DRAWING_DATA = "drawing_data"
            const val COLUMN_TEXT = "text"
        }
    }

    companion object {
        private var instance: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = MyDatabase(context)
                    }
                }
            }
            return instance!!
        }
    }

    fun deleteLastCreatedRow(tableName: String) {
        val lastCreatedRowId = getLastCreatedRowId(database, tableName)
        if (lastCreatedRowId != -1L) {
            database.delete(tableName, "${BaseColumns._ID} = ?", arrayOf(lastCreatedRowId.toString()))
        }
    }

    private fun getLastCreatedRowId(database: SQLiteDatabase, tableName: String): Long {
        val query = "SELECT ${BaseColumns._ID} FROM $tableName ORDER BY ${BaseColumns._ID} DESC LIMIT 1"
        val cursor = database.rawQuery(query, null)
        var lastCreatedRowId: Long = -1L
        if (cursor.moveToFirst()) {
            lastCreatedRowId = cursor.getLong(0)
        }
        cursor.close()
        return lastCreatedRowId
    }



    class MyDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        private val SQL_CREATE_BOOK_ENTRIES =
            "CREATE TABLE ${MyDBContract.BookEntry.TABLE_NAME} (" +
                    "${MyDBContract.BookEntry.COLUMN_BOOK_ID} TEXT PRIMARY KEY," +
                    "${MyDBContract.BookEntry.COLUMN_TITLE} TEXT)"

        private val SQL_CREATE_DRAW_ENTRIES =
            "CREATE TABLE ${MyDBContract.DrawEntry.TABLE_NAME} (" +
                    "${MyDBContract.DrawEntry.COLUMN_PAGE_ID} INTEGER PRIMARY KEY," +
                    "${MyDBContract.DrawEntry.COLUMN_BOOK_ID} TEXT," +
                    "${MyDBContract.DrawEntry.COLUMN_PAGE_NUMBER} INTEGER," +
                    "${MyDBContract.DrawEntry.COLUMN_TEXT} TEXT," +
                    "${MyDBContract.DrawEntry. COLUMN_DRAWING_DATA} ByteArray,"+
                    "FOREIGN KEY (${MyDBContract.DrawEntry.COLUMN_BOOK_ID}) " +
                    "REFERENCES ${MyDBContract.BookEntry.TABLE_NAME}(${MyDBContract.BookEntry.COLUMN_BOOK_ID}) " +
                    "ON DELETE CASCADE)"


        private val SQL_DELETE_BOOK_ENTRIES =
            "DROP TABLE IF EXISTS ${MyDBContract.BookEntry.TABLE_NAME}"

        private val SQL_DELETE_PAGE_ENTRIES =
            "DROP TABLE IF EXISTS ${MyDBContract.DrawEntry.TABLE_NAME}"

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_BOOK_ENTRIES)
            db.execSQL(SQL_CREATE_DRAW_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_PAGE_ENTRIES)
            db.execSQL(SQL_DELETE_BOOK_ENTRIES)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "myDBfile.db"
        }
    }
}
