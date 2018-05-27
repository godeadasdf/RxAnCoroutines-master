package com.example.kangning.rxancoroutines

import android.arch.lifecycle.*
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by kangning on 2018/5/24.
 */

//引入arch lifecycle
interface Contract {
    interface View {

    }

    interface Presenter<V : Contract.View> {

        var view: V?

        fun attachView(view: V)
        fun attachLifecycle(lifecycle: Lifecycle)
        fun detachView()
        fun detachLifecycle(lifecycle: Lifecycle)
        fun isViewAttached(): Boolean
        fun onPresenterDestroy()

    }
}

// a ViewModel will not be destroyed if its owner is destroyed for a
//configuration change (e.g. rotation). The new instance of the owner will just re-connected to the
//existing ViewModel.基于上述特性，可以将Presenter存放于ViewModel之中，使presenter对象得以保持。
class BaseViewModel<P : BasePresenter<V>, V : Contract.View> : ViewModel() {

    lateinit var presenter: P

    override fun onCleared() {
        super.onCleared()
        presenter.onPresenterDestroy()
    }
}

open class BasePresenter<V : Contract.View> : Contract.Presenter<V>, LifecycleObserver {


    //stateBundle用于configuration change时的状态保存
    val stateBundle: Bundle? by lazy {
        Bundle()
    }

    override var view: V? = null

    override fun attachView(view: V) {
        this.view = view
    }

    override fun attachLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    override fun detachView() {
        this.view = null
    }

    override fun detachLifecycle(lifecycle: Lifecycle) {
        lifecycle.removeObserver(this)
    }

    override fun onPresenterDestroy() {
        stateBundle?.clear()
    }

    override fun isViewAttached(): Boolean {
        return this.view != null
    }
}

//利用ViewModel保存Presenter，ViewModel在configurationChange的时候数据retain

abstract class BaseActivity<P : BasePresenter<V>, V : Contract.View>() : AppCompatActivity(), Contract.View {


    private lateinit var presenter: P


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java) as BaseViewModel<P, V>
        //configuration change不进入一下逻辑
        if (viewModel.presenter == null) {
            viewModel.presenter = initPresenter()
        }
        presenter = viewModel.presenter
        presenter.attachView(this as V)
        presenter.attachLifecycle(lifecycle)
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        presenter.detachLifecycle(lifecycle)
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycle
    }

    abstract fun initPresenter(): P

}

interface MainContract {
    interface View : Contract.View {
        fun doSomething()
    }

    interface Presenter : Contract.Presenter<MainContract.View> {
        fun makeDoSomething()
    }
}

class MainPresenter : BasePresenter<MainContract.View>(), MainContract.Presenter {

    override fun makeDoSomething() {

    }
}

