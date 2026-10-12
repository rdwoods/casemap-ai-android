package com.casemapai.ui.home

import com.casemapai.domain.usecase.GetMattersUseCase
import com.casemapai.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMattersUseCase: GetMattersUseCase
) : BaseViewModel<HomeIntent, HomeState, HomeEffect>(HomeState()) {

    init {
        onIntent(HomeIntent.Load)
    }

    override suspend fun reduce(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Load -> {
                updateState { copy(isLoading = true, error = null) }
                launchCoroutine(getExceptionHandler { e -> handleGetMattersException(e) }){
                    getMattersUseCase().collectLatest { matters ->
                        updateState {
                            copy(isLoading = false, matters = matters)
                        }
                    }
                }
            }
        }
    }

    private fun handleGetMattersException(e: Throwable) {
        launchExceptionCoroutine {
            updateState { copy(isLoading = false, error = "Errors") }
        }
    }
}