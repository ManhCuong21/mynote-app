package com.example.core.core.external

sealed class ResultContent<out T> {
  data object Loading : ResultContent<Nothing>()
  data class Content<out T>(val content: T) : ResultContent<T>()
  data class Error(val error: Throwable) : ResultContent<Nothing>()
}