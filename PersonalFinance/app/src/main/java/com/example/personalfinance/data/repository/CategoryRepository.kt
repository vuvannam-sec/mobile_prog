package com.example.personalfinance.data.repository

import androidx.lifecycle.LiveData
import com.example.personalfinance.data.database.CategoryDao
import com.example.personalfinance.data.model.Category

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: String): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }
}