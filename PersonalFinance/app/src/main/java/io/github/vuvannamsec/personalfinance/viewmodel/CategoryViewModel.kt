package io.github.vuvannamsec.personalfinance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import io.github.vuvannamsec.personalfinance.data.model.Category
import io.github.vuvannamsec.personalfinance.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    val allCategories: LiveData<List<Category>> = repository.allCategories

    private val selectedType = MutableLiveData("expense")

    val categoriesForSelectedType: LiveData<List<Category>> = selectedType.switchMap { type ->
        repository.getCategoriesByType(type)
    }

    fun selectType(type: String) {
        if (selectedType.value != type) {
            selectedType.value = type
        }
    }

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
