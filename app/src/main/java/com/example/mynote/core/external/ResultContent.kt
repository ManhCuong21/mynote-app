package com.example.mynote.core.external

sealed class ResultContent<out T> {
  object Loading : ResultContent<Nothing>()
  data class Content<out T>(val content: T) : ResultContent<T>()
  data class Error(val error: Throwable) : ResultContent<Nothing>()
}