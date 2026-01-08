import com.example.core.core.external.DefaultAppCoroutineDispatchers
import com.example.data.datalocal.model.CategoryEntity
import com.example.data.datalocal.repository.CategoryRepository
import com.example.domain.mapper.CategoryParams
import com.example.domain.mapper.toCategoryEntity
import com.example.domain.mapper.toListCategory
import com.example.domain.usecase.data.CategoryUseCase
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CategoryUseCaseTest {

    private val mockRepository: CategoryRepository = mockk()
    private val appDispatchers = DefaultAppCoroutineDispatchers()
    private lateinit var useCase: CategoryUseCase

    @Before
    fun setup() {
        useCase = CategoryUseCase(
            appCoroutineDispatchers = appDispatchers,
            categoryRepository = mockRepository
        )
    }

    private val dummyCategoryEntity = mockk<CategoryEntity>(relaxed = true)
    // (Giữ mockk cho Entity nếu không muốn khai báo toàn bộ cấu trúc Entity)

    private val dummyCategoryParams = CategoryParams(
        title = "Sách",
        image = "icon_book.png",
        security = false
    )

    @Test
    fun insertCategory_repositorySuccess_returnsOkUnit() = runTest {
        coEvery { mockRepository.insertCategory(any()) } returns Ok(Unit)
        val result = useCase.insertCategory(dummyCategoryParams)
        coVerify(exactly = 1) {
            mockRepository.insertCategory(dummyCategoryParams.toCategoryEntity())
        }
        assertEquals(Ok(Unit), result)
    }

    @Test
    fun readAllCategory_repositorySuccess_returnsOkListOfCategoryModel() = runTest {
        val entityList = listOf(dummyCategoryEntity)
        coEvery { mockRepository.readAllCategory() } returns Ok(entityList)
        val result = useCase.readAllCategory()
        coVerify(exactly = 1) { mockRepository.readAllCategory() }
        assertTrue(result.isOk)
        assertEquals(result,Ok(entityList.toListCategory()))    }
}