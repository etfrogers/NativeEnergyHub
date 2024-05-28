package com.example.energyhub.model

import android.util.Log

abstract class BaseModel<StatusType>(
    var stale: Boolean = true,
    var refreshing: Boolean = true,
) {
    suspend fun refresh(): Resource<StatusType> {
        return try {
            Resource.Success(refreshUnsafe())
        } catch (e: Exception){
            Log.d("Status Refresh Error", "${e::class.simpleName}: ${e.message}", e)
            Resource.Error(e.toErrorType())
        }
    }

    protected abstract suspend fun refreshUnsafe(): StatusType

}

fun Exception.toErrorType() = ErrorType.Unknown(this)
// when (this.code) {
//    ErrorCodes.Http.ResourceNotFound   -> ErrorType.Api.NotFound
//    ErrorCodes.Http.InternalServer     -> ErrorType.Api.Server
//    ErrorCodes.Http.ServiceUnavailable -> ErrorType.Api.ServiceUnavailable
//    else                               -> ErrorType.Unknown
//}
sealed class Resource<T> {
    data class Success<T>(val data: T): Resource<T>()
    data class Error<T>(val error: ErrorType): Resource<T>()
}

sealed class ErrorType(exception: Exception) {
    val type: String? = exception::class.simpleName
    val msg: String? = exception.message

    sealed class Api(exception: Exception): ErrorType(exception) {
//        data object Network: Api()
//        data object ServiceUnavailable : Api()
//        data object NotFound : Api()
//        data object Server : Api()

    }
    class Unknown(exception: Exception): ErrorType(exception)
    // other categories of Error
}
/*
import datetime
from abc import ABC, abstractmethod
from concurrent.futures import ThreadPoolExecutor
from typing import Dict

import numpy as np
from kivy.clock import mainthread, Clock
from kivy.event import EventDispatcher
from kivy.properties import BooleanProperty


class BaseModel(EventDispatcher, ABC):
    stale = BooleanProperty(True)
    refreshing = BooleanProperty(False)

    def __init__(self, *args, timeout: float = 0, should_connect=True, **kwargs):
        super().__init__(*args, **kwargs)
        self.connection = None
        self._should_connect = should_connect
        self.thread_pool = ThreadPoolExecutor(max_workers=2)
        self.futures = {}
        self.timeout = timeout
        if self.timeout and self.should_connect:
            Clock.schedule_interval(lambda x: self.refresh(), self.timeout)

    @property
    def should_connect(self):
        return self._should_connect

    def _run_in_model_thread(self, function: callable, *args):
        self.futures[function.__name__, args] = self.thread_pool.submit(function, *args)

    def get_result(self, func_name, *args):
        missing = 'MISSING'
        future = self.futures.get(('_' + func_name, args), missing)
        if future == missing:
            future = self.futures[(func_name, args)]
        return future.result()

    def await_all(self):
        for future in list(self.futures.values()):
            future.result()

    def connect(self):
        self._run_in_model_thread(self._connect)

    @abstractmethod
    def _connect(self):
        raise NotImplementedError

    def refresh(self):
        self.stale = True
        self.refreshing = True
        self._run_in_model_thread(self._refresh)

    def _finish_refresh(self):
        self.refreshing = False

    @abstractmethod
    def _refresh(self):
        raise NotImplementedError

    @abstractmethod
    @mainthread
    def _update_properties(self, data):
        raise NotImplementedError

    @mainthread
    def update_properties(self, data=None):
        self._update_properties(data)
        self.stale = False
        self._finish_refresh()

    def get_history_for_date(self, date: datetime.date, *args) -> (np.ndarray, Dict[str, np.ndarray]):
        self._run_in_model_thread(self._get_history_for_date, date, *args)

    @abstractmethod
    def _get_history_for_date(self, date: datetime.date) -> (np.ndarray, Dict[str, np.ndarray]):
        raise NotImplementedError

 */