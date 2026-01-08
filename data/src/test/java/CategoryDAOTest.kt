import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datalocal.dao.AppDatabase
import com.example.data.datalocal.dao.CategoryDAO
import com.example.data.datalocal.model.CategoryEntity
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CategoryDAOTest {
    private lateinit var categoryDAO: CategoryDAO
    private lateinit var db: AppDatabase

    private val category1 = CategoryEntity(
        idCategory = 1,
        titleCategory = "Food",
        imageCategory = "img_food.png",
        securityCategory = false
    )
    private val category2 = CategoryEntity(
        idCategory = 2,
        titleCategory = "Work",
        imageCategory = "img_work.png",
        securityCategory = true
    )

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Allows synchronous running on the main thread (only for testing)
            .build()
        categoryDAO = db.categoryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertCategoryAndReadAll_returnsCorrectList() {
        categoryDAO.insertCategory(category1)
        categoryDAO.insertCategory(category2)
        val allCategories = categoryDAO.getAllCategory()
        assertEquals(2, allCategories.size)
        assertEquals(category1, allCategories[0])
        assertEquals(category2, allCategories[1])
    }

    @Test
    fun getCategoryWithId_returnsCorrectCategory() {
        categoryDAO.insertCategory(category1)
        categoryDAO.insertCategory(category2)
        val category = categoryDAO.getCategoryWithId(1)
        assertEquals(category1, category)
    }

    @Test
    fun getCategoryWithId_returnsNullIfCategoryNotFound() {
        categoryDAO.insertCategory(category1)
        categoryDAO.insertCategory(category2)
        val category = categoryDAO.getCategoryWithId(3)
        assertEquals(category, null)
    }

    @Test
    fun updateCategory_returnsCorrectCategory() {
        categoryDAO.insertCategory(category1)
        categoryDAO.updateCategory(category1.copy(titleCategory = "Updated Category"))
        val category = categoryDAO.getCategoryWithId(1)
        assertEquals(category.titleCategory, "Updated Category")
    }

    @Test
    fun deleteCategory_returnsCorrectList() {
        categoryDAO.insertCategory(category1)
        categoryDAO.insertCategory(category2)
        categoryDAO.deleteCategory(category1)
        val allCategories = categoryDAO.getAllCategory()
        assertEquals(1, allCategories.size)
        assertEquals(category2, allCategories[0])
    }
}