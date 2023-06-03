import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.BaseColumns
import android.util.Log
import com.example.myapplication.MyElement

class MyDatabase private constructor(context: Context) {

    private val dbHelper: MyDbHelper = MyDbHelper(context)
    val database: SQLiteDatabase = dbHelper.writableDatabase

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
            const val COLUMN_TEXT = "text"
            const val COLUMN_IMAGE = "image"
        }
    }

    companion object {
        private var instance: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = MyDatabase(context.applicationContext)
                    }
                }
            }
            return instance!!
        }
    }

    fun deleteBook(bookId: String): Boolean {
        Log.d("DB", "Delete bookId: $bookId")
        val selection = "${MyDBContract.BookEntry.COLUMN_BOOK_ID} = ?"
        val selectionArgs = arrayOf(bookId)
        val rowsDeleted = database.delete(
            MyDBContract.BookEntry.TABLE_NAME,
            selection,
            selectionArgs
        )
        return rowsDeleted > 0
    }

    fun getTotalPages(bookId: String): Int {
        val projection = arrayOf(MyDBContract.DrawEntry.COLUMN_PAGE_ID)
        val selection = "${MyDBContract.DrawEntry.COLUMN_BOOK_ID} = ?"
        val selectionArgs = arrayOf(bookId)

        val cursor = database.query(
            MyDBContract.DrawEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val totalPages = cursor.count -1

        cursor.close()
        return totalPages
    }


    fun getImageForPage(bookId: String, page: Int): Bitmap? {
        val projection = arrayOf(MyDBContract.DrawEntry.COLUMN_IMAGE)
        val selection =
            "${MyDBContract.DrawEntry.COLUMN_BOOK_ID} = ? AND ${MyDBContract.DrawEntry.COLUMN_PAGE_ID} = ?"
        val selectionArgs = arrayOf(bookId, page.toString())

        val cursor = database.query(
            MyDBContract.DrawEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var image: Bitmap? = null
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(MyDBContract.DrawEntry.COLUMN_IMAGE)
            val imageByteArray = cursor.getBlob(columnIndex)
            image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
        }

        cursor.close()
        return image
    }


    fun getTitle(bookId: String): String? {
        val projection = arrayOf(MyDBContract.BookEntry.COLUMN_TITLE)
        val selection = "${MyDBContract.BookEntry.COLUMN_BOOK_ID} = ?"
        val selectionArgs = arrayOf(bookId)

        val cursor = database.query(
            MyDBContract.BookEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var title: String? = null
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(MyDBContract.BookEntry.COLUMN_TITLE)
            title = cursor.getString(columnIndex)
        }

        cursor.close()
        return title
    }
    class MyDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        private val SQL_CREATE_BOOK_ENTRIES =
            "CREATE TABLE ${MyDBContract.BookEntry.TABLE_NAME} (" +
                    "${MyDBContract.BookEntry.COLUMN_BOOK_ID} TEXT PRIMARY KEY," +
                    "${MyDBContract.BookEntry.COLUMN_TITLE} TEXT)"

        private val SQL_CREATE_DRAW_ENTRIES =
            "CREATE TABLE ${MyDBContract.DrawEntry.TABLE_NAME} (" +
                    "${MyDBContract.DrawEntry.COLUMN_PAGE_ID} INTEGER," +
                    "${MyDBContract.DrawEntry.COLUMN_BOOK_ID} TEXT," +
                    "${MyDBContract.DrawEntry.COLUMN_TEXT} TEXT," +
                    "${MyDBContract.DrawEntry.COLUMN_IMAGE} BLOB," +
                    "PRIMARY KEY (${MyDBContract.DrawEntry.COLUMN_BOOK_ID}, ${MyDBContract.DrawEntry.COLUMN_PAGE_ID})," +
                    "FOREIGN KEY (${MyDBContract.DrawEntry.COLUMN_BOOK_ID}) " +
                    "REFERENCES ${MyDBContract.BookEntry.TABLE_NAME}(${MyDBContract.BookEntry.COLUMN_BOOK_ID}) " +
                    "ON DELETE CASCADE)"


        private val SQL_DELETE_BOOK_ENTRIES =
            "DROP TABLE IF EXISTS ${MyDBContract.BookEntry.TABLE_NAME}"

        private val SQL_DELETE_DRAW_ENTRIES =
            "DROP TABLE IF EXISTS ${MyDBContract.DrawEntry.TABLE_NAME}"

        override fun onCreate(db: SQLiteDatabase) {
            //실행하면 drop 되게
//            db.execSQL(SQL_DELETE_DRAW_ENTRIES)
//            db.execSQL(SQL_DELETE_BOOK_ENTRIES)

            // Create the tables
            db.execSQL(SQL_CREATE_BOOK_ENTRIES)
            db.execSQL(SQL_CREATE_DRAW_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_DRAW_ENTRIES)
            db.execSQL(SQL_DELETE_BOOK_ENTRIES)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            const val DATABASE_VERSION = 3
            const val DATABASE_NAME = "myDBfile.db"
        }

        fun selectAll(): MutableList<MyElement> {
            val readList = mutableListOf<MyElement>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM "+ MyDBContract.BookEntry.TABLE_NAME+";", null)
            with(cursor){
                while(moveToNext()){
                    readList.add(MyElement(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getBlob(2),
                        cursor.getString(3),
                        cursor.getInt(4))
                    )
                }
            }
            cursor.close()
            db.close()
            return readList
        }
    }
}
