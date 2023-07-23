package com.example.mynote.core.external

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Deprecated(
  message = "Should use Flow<T>.collectInViewLifecycle",
  replaceWith = ReplaceWith(
    "this.collectInViewLifecycle(owner, minActiveState, action)"
  ),
  level = DeprecationLevel.WARNING,
)
inline fun <T> Flow<T>.collectIn(
  owner: Fragment,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  isRepeatable: Boolean = true,
  crossinline action: suspend (value: T) -> Unit
) = collectIn(owner as LifecycleOwner, minActiveState, isRepeatable, action)

@Deprecated(
  message = "Should use Flow<T>.collectLatestInViewLifecycle",
  replaceWith = ReplaceWith(
    "this.collectLatestInViewLifecycle(owner, minActiveState, action)"
  ),
  level = DeprecationLevel.WARNING,
)
inline fun <T> Flow<T>.collectLatestIn(
  owner: Fragment,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  isRepeatable: Boolean = true,
  crossinline action: suspend (value: T) -> Unit,
) = collectLatestIn(owner as LifecycleOwner, minActiveState, isRepeatable, action)

inline fun <T> Flow<T>.collectIn(
  owner: LifecycleOwner,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  isRepeatable: Boolean = true,
  crossinline action: suspend (value: T) -> Unit
): Job {
  check(minActiveState === Lifecycle.State.STARTED || minActiveState === Lifecycle.State.RESUMED) {
    "minActiveState must be STARTED or RESUMED"
  }
  return owner.lifecycleScope.launch {
    if (isRepeatable) {
      owner.repeatOnLifecycle(state = minActiveState) { collect { action(it) } }
    } else {
      collect { action(it) }
    }
  }
}

inline fun <T> Flow<T>.collectLatestIn(
  owner: LifecycleOwner,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  isRepeatable: Boolean = true,
  crossinline action: suspend (value: T) -> Unit,
): Job {
  check(minActiveState === Lifecycle.State.STARTED || minActiveState === Lifecycle.State.RESUMED) {
    "minActiveState must be STARTED or RESUMED"
  }
  return owner.lifecycleScope.launch {
    if (isRepeatable) {
      owner.repeatOnLifecycle(state = minActiveState) { collectLatest { action(it) } }
    } else {
      collectLatest { action(it) }
    }
  }
}

/**
 * Launches a new coroutine and repeats `block` every time the Fragment's viewLifecycleOwner
 * is in and out of `minActiveState` lifecycle state.
 */
inline fun <T> Flow<T>.collectInViewLifecycle(
  fragment: Fragment,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  isRepeatable: Boolean = true,
  crossinline action: suspend (value: T) -> Unit,
): Job = collectIn(
  owner = fragment.viewLifecycleOwner,
  minActiveState = minActiveState,
  isRepeatable = isRepeatable,
  action = action,
)

/**
 * Launches a new coroutine and repeats `block` every time the Fragment's viewLifecycleOwner
 * is in and out of `minActiveState` lifecycle state.
 */
inline fun <T> Flow<T>.collectLatestInViewLifecycle(
  fragment: Fragment,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  isRepeatable: Boolean = true,
  crossinline action: suspend (value: T) -> Unit,
): Job = collectLatestIn(
  owner = fragment.viewLifecycleOwner,
  minActiveState = minActiveState,
  isRepeatable = isRepeatable,
  action = action,
)
