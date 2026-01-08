import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datalocal.dao.AppDatabase
import com.example.data.datalocal.dao.NoteDAO
import com.example.data.datalocal.model.CategoryEntity
import com.example.data.datalocal.model.NoteEntity
import com.example.data.datalocal.model.NotificationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteDAOTest {

    private lateinit var noteDAO: NoteDAO
    private lateinit var db: AppDatabase

    // --- Dữ liệu giả lập ---
    private val dummyCategory = CategoryEntity(
        idCategory = 1,
        titleCategory = "Food",
        imageCategory = "img_food.png",
        securityCategory = false
    )
    private val dummyNotification = NotificationEntity(
        idNotification = 1L,
        dayOfMonth = null,
        dayOfWeek = listOf(2, 4, 6),
        hour = 10,
        minute = 30
    )

    // Ghi chú Đầy đủ (Có Category, có Notification)
    private val noteFull = NoteEntity(
        idNote = 0,
        categoryEntity = dummyCategory,
        titleNote = "Project Alpha",
        contentNote = "Need to review documents.",
        fileMediaNote = "file_alpha.mp4",
        hasImage = true,
        hasRecord = false,
        colorTitleNote = "#FFFFFF",
        colorContentNote = "#000000",
        timeNote = System.currentTimeMillis(),
        notificationEntity = dummyNotification,
        security = true
    )

    // Ghi chú Đơn giản (Không có Category, không có Notification)
    private val noteSimple = NoteEntity(
        idNote = 0,
        categoryEntity = null,
        titleNote = "Simple Task",
        contentNote = "Call mom at 5 PM.",
        fileMediaNote = "",
        hasImage = false,
        hasRecord = false,
        colorTitleNote = "#AAAAAA",
        colorContentNote = "#BBBBBB",
        timeNote = null,
        notificationEntity = null,
        security = false
    )

    // Ghi chú khác để kiểm thử sắp xếp
    private val noteAnother = NoteEntity(
        idNote = 0,
        categoryEntity = dummyCategory, // Cùng Category ID 1
        titleNote = "Second Task",
        contentNote = "Follow up with team.",
        fileMediaNote = "",
        hasImage = false,
        hasRecord = false,
        colorTitleNote = "#FFFFFF",
        colorContentNote = "#000000",
        timeNote = System.currentTimeMillis() + 1000,
        notificationEntity = null,
        security = true
    )

    // --- SETUP / TEARDOWN ---
    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        noteDAO = db.noteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertNote_withFullData_isSavedCorrectly() = runTest {
        noteDAO.insertNote(noteFull)

        val fetchedNote = noteDAO.readAllNote().first()

        // Xác minh các trường cơ bản
        assertEquals(noteFull.titleNote, fetchedNote.titleNote)
        assertTrue(fetchedNote.security)

        // Xác minh trường Embedded Category
        assertNotNull(fetchedNote.categoryEntity)
        assertEquals(noteFull.categoryEntity?.idCategory, fetchedNote.categoryEntity?.idCategory)

        // Xác minh trường Embedded Notification
        assertNotNull(fetchedNote.notificationEntity)
        assertEquals(noteFull.notificationEntity?.dayOfWeek, fetchedNote.notificationEntity?.dayOfWeek)
        assertEquals(noteFull.notificationEntity?.hour, fetchedNote.notificationEntity?.hour)
    }

    @Test
    fun insertNote_withNullEmbeddedData_isSavedCorrectly() = runTest {
        // WHEN
        noteDAO.insertNote(noteSimple)

        // THEN: ReadAll để lấy ghi chú
        val fetchedNote = noteDAO.readAllNote().first()

        // Xác minh các trường Embedded là NULL
        assertNull(fetchedNote.categoryEntity)
        assertNull(fetchedNote.notificationEntity)
        assertNull(fetchedNote.timeNote)
    }

    @Test
    fun readAllNote_returnsNotesInDescendingOrder() = runTest {
        // GIVEN: Insert noteAnother trước, sau đó là noteFull
        noteDAO.insertNote(noteAnother) // ID nhỏ hơn
        noteDAO.insertNote(noteFull)    // ID lớn hơn

        // WHEN
        val allNotes = noteDAO.readAllNote()

        // THEN: Ghi chú mới nhất (ID lớn hơn) phải ở đầu tiên (Index 0)
        assertEquals(2, allNotes.size)
        assertEquals(noteFull.titleNote, allNotes[0].titleNote) // noteFull (mới hơn)
        assertEquals(noteAnother.titleNote, allNotes[1].titleNote) // noteAnother (cũ hơn)
    }

    @Test
    fun readNoteWithCategory_returnsCorrectNotesAndOrder() = runTest {
        // GIVEN: noteFull (ID 1) và noteAnother (ID 1), noteSimple (ID null/khác)
        noteDAO.insertNote(noteSimple) // Bỏ qua
        noteDAO.insertNote(noteAnother)
        noteDAO.insertNote(noteFull)

        // WHEN: Truy vấn ghi chú thuộc Category ID 1
        val categoryNotes = noteDAO.readNoteWithCategory(1L)

        // THEN:
        assertEquals(2, categoryNotes.size)
        // Kiểm tra sắp xếp theo ID DESC: noteFull (mới hơn) phải ở đầu
        assertEquals(noteFull.titleNote, categoryNotes[0].titleNote)
        assertEquals(noteAnother.titleNote, categoryNotes[1].titleNote)
    }

    @Test
    fun updateNote_changesContentAndSecurityCorrectly() = runTest {
        // GIVEN: Insert ghi chú
        noteDAO.insertNote(noteSimple)

        // Lấy ID thực tế của ghi chú đã chèn
        val insertedNote = noteDAO.readAllNote().first()
        val id = insertedNote.idNote

        // WHEN: Update nội dung và bảo mật
        val updatedNote = insertedNote.copy(
            contentNote = "Content changed!",
            security = true
        )
        noteDAO.updateNote(updatedNote)

        // THEN: Lấy lại và xác minh
        val result = noteDAO.getNoteById(id)
        assertNotNull(result)
        assertEquals("Content changed!", result?.contentNote)
        assertTrue(result!!.security)
    }

    @Test
    fun deleteNote_removesNoteFromDb() = runTest {
        // GIVEN
        noteDAO.insertNote(noteSimple)
        val noteToDelete = noteDAO.readAllNote().first()

        // WHEN
        noteDAO.deleteNote(noteToDelete)

        // THEN
        val allNotes = noteDAO.readAllNote()
        assertTrue(allNotes.isEmpty())
        assertNull(noteDAO.getNoteById(noteToDelete.idNote))
    }

    @Test
    fun getNoteById_existingId_returnsCorrectNote() = runTest {
        // GIVEN
        noteDAO.insertNote(noteFull)
        val id = noteDAO.readAllNote().first().idNote

        // WHEN
        val fetchedNote = noteDAO.getNoteById(id)

        // THEN
        assertNotNull(fetchedNote)
        assertEquals(noteFull.titleNote, fetchedNote?.titleNote)
    }

    @Test
    fun searchNoteByKeyword_findsMatchingTitleOrContent() = runTest {
        // GIVEN: Chèn dữ liệu
        noteDAO.insertNote(noteFull) // Title: "Project Alpha", Content: "Need to review documents."
        noteDAO.insertNote(noteSimple) // Title: "Simple Task", Content: "Call mom at 5 PM."

        // WHEN 1: Tìm kiếm trong Tiêu đề (Alpha)
        val searchResult1 = noteDAO.searchNoteByKeyword("Alpha").first()

        // WHEN 2: Tìm kiếm trong Nội dung (mom)
        val searchResult2 = noteDAO.searchNoteByKeyword("mom").first()

        // THEN 1: Alpha
        assertEquals(1, searchResult1.size)
        assertEquals(noteFull.titleNote, searchResult1.first().titleNote)

        // THEN 2: mom
        assertEquals(1, searchResult2.size)
        assertEquals(noteSimple.titleNote, searchResult2.first().titleNote)

        // WHEN 3: Tìm kiếm không tồn tại
        val searchResult3 = noteDAO.searchNoteByKeyword("xyz").first()

        // THEN 3: Trả về rỗng
        assertTrue(searchResult3.isEmpty())
    }
}