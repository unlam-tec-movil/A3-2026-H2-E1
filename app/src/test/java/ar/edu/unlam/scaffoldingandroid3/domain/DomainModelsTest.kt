package ar.edu.unlam.scaffoldingandroid3.domain

import ar.edu.unlam.scaffoldingandroid3.Application
import ar.edu.unlam.scaffoldingandroid3.data.BookStubRepo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DomainModelsTest {
    @Test
    fun `given the same book data, when comparing two books, then they are equal`() {
        // Given
        val first = Book(title = "Dune", author = "Frank Herbert", year = 1965)
        val second = Book(title = "Dune", author = "Frank Herbert", year = 1965)

        // When
        val equal = first == second

        // Then
        assertEquals(true, equal)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `given a book, when copying it with another year, then only the year changes`() {
        // Given
        val book = Book(title = "Dune", author = "Frank Herbert", year = 1965)

        // When
        val reprint = book.copy(year = 2005)

        // Then
        assertEquals(2005, reprint.year)
        assertEquals(book.title, reprint.title)
        assertEquals(book.author, reprint.author)
        assertNotEquals(book, reprint)
    }

    @Test
    fun `given a book repository, when getting books, then returns what the repository provides`() {
        // Given
        val expected = listOf(Book("Dune", "Frank Herbert", 1965))
        val repository =
            object : BookRepository {
                override fun getBooks(): List<Book> = expected
            }

        // When
        val books = repository.getBooks()

        // Then
        assertEquals(expected, books)
    }

    @Test
    fun `given the scaffold classes, when instantiating them, then they can be created`() {
        // Given / When
        val service = BookService()
        val stubRepo = BookStubRepo()
        val application = Application()

        // Then
        assertNotNull(service)
        assertNotNull(stubRepo)
        assertNotNull(application)
    }
}
