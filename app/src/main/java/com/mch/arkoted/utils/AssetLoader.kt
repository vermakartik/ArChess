package com.mch.arkoted.utils

import android.app.Activity
import android.util.SparseArray
import com.google.ar.sceneform.rendering.ModelRenderable
import com.mch.arkoted.exceptions.NotImplementedException
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture

class AssetLoader(var a: Activity) {

  val activity = WeakReference<Activity>(a)
  var sparseFutureSet = SparseArray<CompletableFuture<ModelRenderable>>()
  var renderMapFunction: ((Int, ModelRenderable) -> Unit)? = null

  fun setLoaderFunc(func: (Int, ModelRenderable) -> Unit){
      renderMapFunction = func
  }

  fun loadAsset(id: Int, resource: Int): Boolean {
    val v: Boolean = activity.get()?.let {
      val completableFuture: CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(it.applicationContext, resource).build().thenApply {
        setRenderable(id, it)
        it
      }
      completableFuture?.let {
        sparseFutureSet.put(id, completableFuture)
      }
      completableFuture!=null
    } ?: false
    return v
  }

  fun setRenderable(id: Int, m: ModelRenderable){
    val a: Activity? = activity.get()
    a?.let {
        renderMapFunction?.let {
          it(id, m)
        }
    } ?: throw NotImplementedException("No implementation for Renderable")
  }
}