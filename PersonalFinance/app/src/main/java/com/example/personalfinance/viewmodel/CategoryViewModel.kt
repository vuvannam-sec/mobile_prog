package com.example.personalfinance.viewmodel

import androidx.lifecycle.*
import com.example.personalfinance.data.model.Category
import com.example.personalfinance.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    val allCategories: LiveData<List<Category>> = repository.allCategories

    fun getCategoriesByType(type: String): LiveData<List<Category>> {
        return repository.getCategoriesByType(type)
    }

    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun update(category: Category) = viewModelScope.launch {
        repository.update(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return repository.getCategoryById(id)
    }
}

class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}