package com.support.tech.newsaibuddy.data.api

/**
 * A sealed class representing the different states of a resource.
 * This is commonly used to represent the state of a network request or data loading operation.
 * @param T The type of data held by the resource.
 */
sealed class ResourceState<T> {
    /** Represents the loading state of the resource. */
    class Loading<T> : ResourceState<T>()
    /** Represents the success state of the resource, holding the [data]. */
    data class Success<T>(val data: T) : ResourceState<T>()
    /** Represents the error state of the resource, holding an [error] message. */
    data class Error<T>(val error: String) : ResourceState<T>()
}