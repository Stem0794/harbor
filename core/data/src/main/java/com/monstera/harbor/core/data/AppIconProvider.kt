package com.monstera.harbor.core.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppIconProvider {
    suspend fun load(packageName: PackageName): Drawable
}

class AndroidAppIconProvider(context: Context) : AppIconProvider {
    private val packageManager = context.packageManager
    private val fallback by lazy { packageManager.defaultActivityIcon }
    private val cache = object : LruCache<String, Drawable.ConstantState>(ICON_CACHE_SIZE) {}

    override suspend fun load(packageName: PackageName): Drawable = withContext(Dispatchers.IO) {
        val cached = cache[packageName.value]?.newDrawable()
        if (cached != null) return@withContext cached
        val icon = runCatching { packageManager.getApplicationIcon(packageName.value) }.getOrElse { fallback }
        icon.constantState?.let { cache.put(packageName.value, it) }
        icon
    }

    private companion object {
        const val ICON_CACHE_SIZE = 64
    }
}
